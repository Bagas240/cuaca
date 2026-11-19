package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.security.TelemetryCrypto
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Provides Jetpack Security [EncryptedSharedPreferences] backed by AndroidKeyStore
 * (AES256_SIV keys + AES256_GCM values). When AndroidKeyStore is absent in headless JVM
 * test runners (e.g. Robolectric), falls back to an AES-256-GCM + SHA-256-keyed wrapper
 * so zero plaintext GPS coordinates or user settings ever touch disk.
 */
object SecurePreferencesProvider {

    const val ENCRYPTED_PREFS_NAME = "veridia_encrypted_prefs"
    private const val FALLBACK_PREFS_NAME = "veridia_encrypted_prefs_jvm_fallback"

    @Volatile
    var isHardwareEncryptedPrefsActive: Boolean = false
        private set

    @Suppress("DEPRECATION")
    fun createEncryptedPrefs(context: Context): SharedPreferences {
        return runCatching {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val prefs = EncryptedSharedPreferences.create(
                context.applicationContext,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
            isHardwareEncryptedPrefsActive = true
            prefs
        }.getOrElse {
            isHardwareEncryptedPrefsActive = false
            val raw = context.applicationContext.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
            AesEncryptedFallbackPrefs(raw)
        }
    }
}

private class AesEncryptedFallbackPrefs(
    private val delegate: SharedPreferences,
) : SharedPreferences {

    private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    private fun hashKey(key: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("veridia_key:$key".toByteArray(StandardCharsets.UTF_8))
        return "K_" + Base64.encodeToString(digest, Base64.NO_WRAP or Base64.URL_SAFE)
    }

    private fun readDecrypted(key: String): String? {
        val raw = delegate.getString(hashKey(key), null) ?: return null
        val plain = TelemetryCrypto.decrypt(raw)
        return plain.takeIf { it.isNotEmpty() || raw.isEmpty() }
    }

    override fun getAll(): MutableMap<String, *> = delegate.all.toMutableMap()

    override fun getString(key: String, defValue: String?): String? {
        val decoded = readDecrypted(key) ?: return defValue
        return if (decoded.startsWith("S:")) decoded.removePrefix("S:") else decoded
    }

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        val decoded = readDecrypted(key) ?: return defValues
        if (!decoded.startsWith("SS:")) return defValues
        return decoded.removePrefix("SS:")
            .split('\u001F')
            .filter { it.isNotEmpty() }
            .toMutableSet()
    }

    override fun getInt(key: String, defValue: Int): Int {
        val decoded = readDecrypted(key) ?: return defValue
        return decoded.removePrefix("I:").toIntOrNull() ?: defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        val decoded = readDecrypted(key) ?: return defValue
        return decoded.removePrefix("L:").toLongOrNull() ?: defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        val decoded = readDecrypted(key) ?: return defValue
        return decoded.removePrefix("F:").toFloatOrNull() ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        val decoded = readDecrypted(key) ?: return defValue
        return when (decoded.removePrefix("B:")) {
            "true" -> true
            "false" -> false
            else -> defValue
        }
    }

    override fun contains(key: String): Boolean = delegate.contains(hashKey(key))

    override fun edit(): SharedPreferences.Editor = FallbackEditor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) {
        if (listener != null) {
            synchronized(listeners) { listeners.add(listener) }
        }
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) {
        if (listener != null) {
            synchronized(listeners) { listeners.remove(listener) }
        }
    }

    private fun notifyListeners(changedKeys: List<String>) {
        val snapshot = synchronized(listeners) { listeners.toList() }
        snapshot.forEach { listener ->
            changedKeys.forEach { key ->
                listener.onSharedPreferenceChanged(this, key)
            }
        }
    }

    private inner class FallbackEditor : SharedPreferences.Editor {
        private val delegateEditor = delegate.edit()
        private val touchedKeys = mutableListOf<String>()

        override fun putString(key: String, value: String?): SharedPreferences.Editor = apply {
            touchedKeys.add(key)
            if (value == null) {
                delegateEditor.remove(hashKey(key))
            } else {
                delegateEditor.putString(hashKey(key), TelemetryCrypto.encrypt("S:$value"))
            }
        }

        override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor = apply {
            touchedKeys.add(key)
            if (values == null) {
                delegateEditor.remove(hashKey(key))
            } else {
                val joined = values.joinToString("\u001F")
                delegateEditor.putString(hashKey(key), TelemetryCrypto.encrypt("SS:$joined"))
            }
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor = apply {
            touchedKeys.add(key)
            delegateEditor.putString(hashKey(key), TelemetryCrypto.encrypt("I:$value"))
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor = apply {
            touchedKeys.add(key)
            delegateEditor.putString(hashKey(key), TelemetryCrypto.encrypt("L:$value"))
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = apply {
            touchedKeys.add(key)
            delegateEditor.putString(hashKey(key), TelemetryCrypto.encrypt("F:$value"))
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = apply {
            touchedKeys.add(key)
            delegateEditor.putString(hashKey(key), TelemetryCrypto.encrypt("B:$value"))
        }

        override fun remove(key: String): SharedPreferences.Editor = apply {
            touchedKeys.add(key)
            delegateEditor.remove(hashKey(key))
        }

        override fun clear(): SharedPreferences.Editor = apply {
            delegateEditor.clear()
        }

        override fun commit(): Boolean {
            val ok = delegateEditor.commit()
            if (ok) notifyListeners(touchedKeys.toList())
            return ok
        }

        override fun apply() {
            delegateEditor.apply()
            notifyListeners(touchedKeys.toList())
        }
    }
}

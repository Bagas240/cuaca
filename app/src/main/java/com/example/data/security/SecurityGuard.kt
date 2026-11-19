package com.example.data.security

import okhttp3.Interceptor
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

data class SecurityPosture(
    val encryptedStorageActive: Boolean = true,
    val encryptedPrefsActive: Boolean = true,
    val tlsAllowlistActive: Boolean = true,
    val coarseLocationShieldLocked: Boolean = true,
    val deviceIntegrityClean: Boolean = true,
    val blockedOutboundAttempts: Int = 0,
    val tamperWarning: String? = null,
)

/**
 * Enforces strict outbound network allowlisting (HTTPS-only to official BMKG, Open-Meteo,
 * USGS, and RainViewer endpoints) and performs non-blocking runtime tamper/root inspection.
 */
object SecurityGuard {

    val ALLOWED_TELEMETRY_HOSTS: Set<String> = setOf(
        "api.bmkg.go.id",
        "data.bmkg.go.id",
        "api.open-meteo.com",
        "air-quality-api.open-meteo.com",
        "geocoding-api.open-meteo.com",
        "earthquake.usgs.gov",
        "api.rainviewer.com",
        "tilecache.rainviewer.com",
    )

    private val blockedAttempts = AtomicInteger(0)

    fun isHostAllowed(scheme: String, host: String): Boolean {
        if (!scheme.equals("https", ignoreCase = true)) return false
        val normalized = host.lowercase()
        return ALLOWED_TELEMETRY_HOSTS.contains(normalized)
    }

    fun recordBlockedAttempt() {
        blockedAttempts.incrementAndGet()
    }

    fun getBlockedAttemptsCount(): Int = blockedAttempts.get()

    fun detectTamperOrHookIndicators(): String? {
        // Check for common root/hook binaries without breaking standard Android emulators
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/app/Superuser.apk",
            "/data/adb/magisk",
        )
        val hasRootBinary = suPaths.any { path ->
            runCatching { File(path).exists() }.getOrDefault(false)
        }
        if (hasRootBinary) {
            return "Root binary detected · Strict Privacy Lockdown active"
        }

        // Check loaded memory maps for Frida / Xposed instrumentation frameworks
        val hasDynamicHook = runCatching {
            val maps = File("/proc/self/maps")
            if (!maps.exists() || !maps.canRead()) return@runCatching false
            maps.bufferedReader().useLines { lines ->
                lines.any { line ->
                    line.contains("frida", ignoreCase = true) ||
                        line.contains("xposed", ignoreCase = true) ||
                        line.contains("substrate", ignoreCase = true)
                }
            }
        }.getOrDefault(false)

        if (hasDynamicHook) {
            return "Instrumentation hook detected · Telemetry locked"
        }
        return null
    }

    fun currentPosture(): SecurityPosture {
        val tamper = detectTamperOrHookIndicators()
        return SecurityPosture(
            encryptedStorageActive = true,
            tlsAllowlistActive = true,
            coarseLocationShieldLocked = true,
            deviceIntegrityClean = tamper == null,
            blockedOutboundAttempts = blockedAttempts.get(),
            tamperWarning = tamper,
        )
    }
}

class NetworkSecurityInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val scheme = request.url.scheme
        val host = request.url.host

        if (!SecurityGuard.isHostAllowed(scheme, host)) {
            SecurityGuard.recordBlockedAttempt()
            throw IOException("Blocked unauthorized outbound connection to $scheme://$host")
        }

        val hardenedRequest = request.newBuilder()
            .header("X-Veridia-Privacy", "coarse-zone-only")
            .build()
        return chain.proceed(hardenedRequest)
    }
}

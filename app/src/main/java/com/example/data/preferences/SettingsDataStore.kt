package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.location.LocationMaskingUtility
import com.example.data.model.AppThemeMode
import com.example.data.model.PressureUnit
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserSettings
import com.example.data.model.WindUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class StoredEncryptedGpsLocation(
    val latitude: Double,
    val longitude: Double,
    val localityName: String,
    val countryName: String,
    val zoneLabel: String,
    val savedAtMillis: Long,
)

/**
 * Encrypted local storage backed by Jetpack [EncryptedSharedPreferences] (AES256_SIV keys +
 * AES256_GCM values) for all user settings and masked GPS coordinates at rest.
 */
class SettingsDataStore(context: Context) {

    private object Keys {
        const val SELECTED_CITY_ID = "selected_city_id"
        const val SEISMIC_ALERT = "seismic_alert"
        const val TYPHOON_WATCH = "typhoon_watch"
        const val AQI_INVERSION = "aqi_inversion"
        const val TEMP_UNIT = "temp_unit"
        const val WIND_UNIT = "wind_unit"
        const val PRESSURE_UNIT = "pressure_unit"
        const val GYRO_PARALLAX = "gyro_parallax"
        const val GPS_PRIVACY_MODE = "gps_privacy_mode"
        const val OFFLINE_CACHE_LABEL = "offline_cache_label"
        const val BACKGROUND_SYNC_ENABLED = "background_sync_enabled"
        const val LAST_BACKGROUND_SYNC_MILLIS = "last_background_sync_millis"

        const val GPS_LAT = "gps_masked_lat"
        const val GPS_LON = "gps_masked_lon"
        const val GPS_LOCALITY = "gps_locality"
        const val GPS_COUNTRY = "gps_country"
        const val GPS_ZONE_LABEL = "gps_zone_label"
        const val GPS_UPDATED_AT = "gps_updated_at"
        const val THEME_MODE = "theme_mode"
    }

    private val encryptedPrefs: SharedPreferences =
        SecurePreferencesProvider.createEncryptedPrefs(context.applicationContext)

    private val _settingsFlow = MutableStateFlow(readUserSettings())
    val settingsFlow: Flow<UserSettings> = _settingsFlow.asStateFlow()
    val currentSettings: UserSettings
        get() = _settingsFlow.value

    private val _storedGpsFlow = MutableStateFlow(getStoredGpsCoordinates())
    val storedGpsFlow: Flow<StoredEncryptedGpsLocation?> = _storedGpsFlow.asStateFlow()

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        _settingsFlow.value = readUserSettings()
        _storedGpsFlow.value = getStoredGpsCoordinates()
    }

    init {
        // Purge any legacy unencrypted Preferences DataStore file from earlier builds
        runCatching {
            val legacyDataStoreFile = File(context.applicationContext.filesDir, "datastore/veridia_settings.preferences_pb")
            if (legacyDataStoreFile.exists()) {
                legacyDataStoreFile.delete()
            }
        }
        encryptedPrefs.registerOnSharedPreferenceChangeListener(prefListener)
    }

    private fun readUserSettings(): UserSettings {
        val tempName = encryptedPrefs.getString(Keys.TEMP_UNIT, null)
        val windName = encryptedPrefs.getString(Keys.WIND_UNIT, null)
        val pressureName = encryptedPrefs.getString(Keys.PRESSURE_UNIT, null)

        val themeName = encryptedPrefs.getString(Keys.THEME_MODE, null)

        return UserSettings(
            selectedCityId = encryptedPrefs.getString(Keys.SELECTED_CITY_ID, null) ?: "jakarta",
            seismicPWaveAlert = encryptedPrefs.getBoolean(Keys.SEISMIC_ALERT, true),
            typhoonFlashWatch = encryptedPrefs.getBoolean(Keys.TYPHOON_WATCH, true),
            aqiSmogInversion = encryptedPrefs.getBoolean(Keys.AQI_INVERSION, true),
            temperatureUnit = tempName?.let {
                runCatching { TemperatureUnit.valueOf(it) }.getOrNull()
            } ?: TemperatureUnit.CELSIUS,
            windUnit = windName?.let {
                runCatching { WindUnit.valueOf(it) }.getOrNull()
            } ?: WindUnit.KMH,
            pressureUnit = pressureName?.let {
                runCatching { PressureUnit.valueOf(it) }.getOrNull()
            } ?: PressureUnit.HPA,
            gyroParallaxEnabled = encryptedPrefs.getBoolean(Keys.GYRO_PARALLAX, true),
            gpsPrivacyMode = encryptedPrefs.getBoolean(Keys.GPS_PRIVACY_MODE, true),
            offlineCacheLabel = encryptedPrefs.getString(Keys.OFFLINE_CACHE_LABEL, null) ?: "Cached · Nov 2026",
            backgroundSyncEnabled = encryptedPrefs.getBoolean(Keys.BACKGROUND_SYNC_ENABLED, true),
            lastBackgroundSyncMillis = encryptedPrefs.getLong(Keys.LAST_BACKGROUND_SYNC_MILLIS, 0L),
            themeMode = themeName?.let {
                runCatching { AppThemeMode.valueOf(it) }.getOrNull()
            } ?: AppThemeMode.AUTO_SOLAR,
        )
    }

    fun getStoredGpsCoordinates(): StoredEncryptedGpsLocation? {
        val latStr = encryptedPrefs.getString(Keys.GPS_LAT, null) ?: return null
        val lonStr = encryptedPrefs.getString(Keys.GPS_LON, null) ?: return null
        val lat = latStr.toDoubleOrNull() ?: return null
        val lon = lonStr.toDoubleOrNull() ?: return null
        val locality = encryptedPrefs.getString(Keys.GPS_LOCALITY, null) ?: "Protected Zone"
        val country = encryptedPrefs.getString(Keys.GPS_COUNTRY, null) ?: "Indonesia"
        val zoneLabel = encryptedPrefs.getString(Keys.GPS_ZONE_LABEL, null)
            ?: LocationMaskingUtility.formatMaskedZoneLabel(lat, lon)
        val savedAt = encryptedPrefs.getLong(Keys.GPS_UPDATED_AT, 0L)
        return StoredEncryptedGpsLocation(
            latitude = lat,
            longitude = lon,
            localityName = locality,
            countryName = country,
            zoneLabel = zoneLabel,
            savedAtMillis = savedAt,
        )
    }

    suspend fun saveEncryptedGpsCoordinates(
        latitude: Double,
        longitude: Double,
        localityName: String,
        countryName: String,
        zoneLabel: String,
    ) {
        // Strictly mask to coarse regency resolution before encrypting at rest
        val masked = LocationMaskingUtility.maskCoordinates(
            latitude = latitude,
            longitude = longitude,
            enabled = true,
        )
        encryptedPrefs.edit()
            .putString(Keys.GPS_LAT, masked.latitude.toString())
            .putString(Keys.GPS_LON, masked.longitude.toString())
            .putString(Keys.GPS_LOCALITY, localityName)
            .putString(Keys.GPS_COUNTRY, countryName)
            .putString(Keys.GPS_ZONE_LABEL, zoneLabel)
            .putLong(Keys.GPS_UPDATED_AT, System.currentTimeMillis())
            .apply()
        _storedGpsFlow.value = getStoredGpsCoordinates()
    }

    suspend fun clearStoredGpsCoordinates() {
        encryptedPrefs.edit()
            .remove(Keys.GPS_LAT)
            .remove(Keys.GPS_LON)
            .remove(Keys.GPS_LOCALITY)
            .remove(Keys.GPS_COUNTRY)
            .remove(Keys.GPS_ZONE_LABEL)
            .remove(Keys.GPS_UPDATED_AT)
            .apply()
        _storedGpsFlow.value = null
    }

    suspend fun setSelectedCityId(cityId: String) {
        encryptedPrefs.edit().putString(Keys.SELECTED_CITY_ID, cityId).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setSeismicPWaveAlert(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(Keys.SEISMIC_ALERT, enabled).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setTyphoonFlashWatch(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(Keys.TYPHOON_WATCH, enabled).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setAqiSmogInversion(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(Keys.AQI_INVERSION, enabled).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        encryptedPrefs.edit().putString(Keys.TEMP_UNIT, unit.name).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setWindUnit(unit: WindUnit) {
        encryptedPrefs.edit().putString(Keys.WIND_UNIT, unit.name).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setPressureUnit(unit: PressureUnit) {
        encryptedPrefs.edit().putString(Keys.PRESSURE_UNIT, unit.name).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setGyroParallaxEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(Keys.GYRO_PARALLAX, enabled).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setGpsPrivacyMode(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(Keys.GPS_PRIVACY_MODE, enabled).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setOfflineCacheLabel(label: String) {
        encryptedPrefs.edit().putString(Keys.OFFLINE_CACHE_LABEL, label).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean(Keys.BACKGROUND_SYNC_ENABLED, enabled).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setLastBackgroundSyncMillis(millis: Long) {
        encryptedPrefs.edit().putLong(Keys.LAST_BACKGROUND_SYNC_MILLIS, millis).apply()
        _settingsFlow.value = readUserSettings()
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        encryptedPrefs.edit().putString(Keys.THEME_MODE, mode.name).apply()
        _settingsFlow.value = readUserSettings()
    }
}

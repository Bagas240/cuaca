package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.PressureUnit
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserSettings
import com.example.data.model.WindUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.veridiaDataStore: DataStore<Preferences> by preferencesDataStore(name = "veridia_settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val SELECTED_CITY_ID = stringPreferencesKey("selected_city_id")
        val SEISMIC_ALERT = booleanPreferencesKey("seismic_alert")
        val TYPHOON_WATCH = booleanPreferencesKey("typhoon_watch")
        val AQI_INVERSION = booleanPreferencesKey("aqi_inversion")
        val TEMP_UNIT = stringPreferencesKey("temp_unit")
        val WIND_UNIT = stringPreferencesKey("wind_unit")
        val PRESSURE_UNIT = stringPreferencesKey("pressure_unit")
        val GYRO_PARALLAX = booleanPreferencesKey("gyro_parallax")
        val GPS_PRIVACY_MODE = booleanPreferencesKey("gps_privacy_mode")
        val OFFLINE_CACHE_LABEL = stringPreferencesKey("offline_cache_label")
    }

    val settingsFlow: Flow<UserSettings> = context.veridiaDataStore.data.map { prefs ->
        UserSettings(
            selectedCityId = prefs[Keys.SELECTED_CITY_ID] ?: "jakarta",
            seismicPWaveAlert = prefs[Keys.SEISMIC_ALERT] ?: true,
            typhoonFlashWatch = prefs[Keys.TYPHOON_WATCH] ?: true,
            aqiSmogInversion = prefs[Keys.AQI_INVERSION] ?: true,
            temperatureUnit = prefs[Keys.TEMP_UNIT]?.let {
                runCatching { TemperatureUnit.valueOf(it) }.getOrNull()
            } ?: TemperatureUnit.CELSIUS,
            windUnit = prefs[Keys.WIND_UNIT]?.let {
                runCatching { WindUnit.valueOf(it) }.getOrNull()
            } ?: WindUnit.KMH,
            pressureUnit = prefs[Keys.PRESSURE_UNIT]?.let {
                runCatching { PressureUnit.valueOf(it) }.getOrNull()
            } ?: PressureUnit.HPA,
            gyroParallaxEnabled = prefs[Keys.GYRO_PARALLAX] ?: true,
            gpsPrivacyMode = prefs[Keys.GPS_PRIVACY_MODE] ?: true,
            offlineCacheLabel = prefs[Keys.OFFLINE_CACHE_LABEL] ?: "Cached · Nov 2026",
        )
    }

    suspend fun setSelectedCityId(cityId: String) {
        context.veridiaDataStore.edit { it[Keys.SELECTED_CITY_ID] = cityId }
    }

    suspend fun setSeismicPWaveAlert(enabled: Boolean) {
        context.veridiaDataStore.edit { it[Keys.SEISMIC_ALERT] = enabled }
    }

    suspend fun setTyphoonFlashWatch(enabled: Boolean) {
        context.veridiaDataStore.edit { it[Keys.TYPHOON_WATCH] = enabled }
    }

    suspend fun setAqiSmogInversion(enabled: Boolean) {
        context.veridiaDataStore.edit { it[Keys.AQI_INVERSION] = enabled }
    }

    suspend fun setTemperatureUnit(unit: TemperatureUnit) {
        context.veridiaDataStore.edit { it[Keys.TEMP_UNIT] = unit.name }
    }

    suspend fun setWindUnit(unit: WindUnit) {
        context.veridiaDataStore.edit { it[Keys.WIND_UNIT] = unit.name }
    }

    suspend fun setPressureUnit(unit: PressureUnit) {
        context.veridiaDataStore.edit { it[Keys.PRESSURE_UNIT] = unit.name }
    }

    suspend fun setGyroParallaxEnabled(enabled: Boolean) {
        context.veridiaDataStore.edit { it[Keys.GYRO_PARALLAX] = enabled }
    }

    suspend fun setGpsPrivacyMode(enabled: Boolean) {
        context.veridiaDataStore.edit { it[Keys.GPS_PRIVACY_MODE] = enabled }
    }

    suspend fun setOfflineCacheLabel(label: String) {
        context.veridiaDataStore.edit { it[Keys.OFFLINE_CACHE_LABEL] = label }
    }
}

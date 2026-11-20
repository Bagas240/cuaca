package com.example.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.alerts.DisasterNotificationManager
import com.example.data.model.CityLocation
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.VeridiaRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Periodic power-aware background worker dispatched by WorkManager only when
 * the device is charging or connected to an unmetered Wi-Fi network.
 * Inspects [android.os.BatteryManager] state before doing network/DB work to
 * throttle on low battery and defer entirely during critical power states.
 */
class WeatherSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_TRIGGER_CHANNEL = "trigger_channel"
        const val KEY_STATUS = "sync_status"
        const val KEY_CITY_ID = "city_id"
        const val KEY_SYNCED_AT_MILLIS = "synced_at_millis"
        const val KEY_BYPASS_DEDUP = "bypass_dedup"
        const val KEY_BATTERY_PERCENT = "battery_percent"
        const val KEY_BATTERY_TIER = "battery_tier"
        const val KEY_SYNC_INTERVAL_MINUTES = "sync_interval_minutes"

        const val MIN_DEDUP_INTERVAL_MS = 5 * 60_000L
        const val LOW_BATTERY_MIN_DEDUP_INTERVAL_MS = 60 * 60_000L

        @Volatile
        internal var repositoryFactory: (Context) -> VeridiaRepository = { context ->
            VeridiaRepository.create(context)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val settingsStore = SettingsDataStore(applicationContext)
        val settings = settingsStore.currentSettings
        val triggerChannel = inputData.getString(KEY_TRIGGER_CHANNEL) ?: "periodic"

        if (!settings.backgroundSyncEnabled) {
            return@coroutineScope Result.success(
                workDataOf(
                    KEY_STATUS to "disabled",
                    KEY_TRIGGER_CHANNEL to triggerChannel,
                )
            )
        }

        val batteryProfile = WeatherSyncScheduler.readBatteryPowerProfile(applicationContext)
        runCatching {
            WeatherSyncScheduler.adaptScheduleForBatteryProfile(applicationContext, batteryProfile)
        }

        // Prevent background processing during critical power states (<=10% and unplugged)
        if (batteryProfile.tier == SyncBatteryTier.CRITICAL_BATTERY) {
            return@coroutineScope Result.success(
                workDataOf(
                    KEY_STATUS to "deferred_critical_battery",
                    KEY_TRIGGER_CHANNEL to triggerChannel,
                    KEY_BATTERY_PERCENT to batteryProfile.batteryPercent,
                    KEY_BATTERY_TIER to batteryProfile.tier.name,
                    KEY_SYNC_INTERVAL_MINUTES to batteryProfile.repeatIntervalMinutes,
                )
            )
        }

        val nowMillis = System.currentTimeMillis()
        val bypassDedup = inputData.getBoolean(KEY_BYPASS_DEDUP, false)
        val requiredCooldownMs = if (batteryProfile.tier == SyncBatteryTier.LOW_BATTERY) {
            LOW_BATTERY_MIN_DEDUP_INTERVAL_MS
        } else {
            MIN_DEDUP_INTERVAL_MS
        }

        if (!bypassDedup && settings.lastBackgroundSyncMillis > 0L &&
            (nowMillis - settings.lastBackgroundSyncMillis) < requiredCooldownMs
        ) {
            val status = if (batteryProfile.tier == SyncBatteryTier.LOW_BATTERY) {
                "throttled_low_battery"
            } else {
                "deduplicated"
            }
            return@coroutineScope Result.success(
                workDataOf(
                    KEY_STATUS to status,
                    KEY_TRIGGER_CHANNEL to triggerChannel,
                    KEY_SYNCED_AT_MILLIS to settings.lastBackgroundSyncMillis,
                    KEY_BATTERY_PERCENT to batteryProfile.batteryPercent,
                    KEY_BATTERY_TIER to batteryProfile.tier.name,
                    KEY_SYNC_INTERVAL_MINUTES to batteryProfile.repeatIntervalMinutes,
                )
            )
        }

        val repository = repositoryFactory(applicationContext)
        val targetCity = resolveTargetCity(settingsStore, repository)
        val maskCoords = settings.gpsPrivacyMode

        val weatherDeferred = async {
            repository.fetchLiveWeatherWithCache(targetCity, maskCoordinates = maskCoords)
        }
        val aqiDeferred = async {
            repository.fetchLiveAirQuality(targetCity, maskCoordinates = maskCoords)
        }
        val seismicDeferred = async {
            repository.fetchRecentEarthquakesWithCache()
        }

        val weatherOutcome = weatherDeferred.await().getOrNull()
        val liveAqi = aqiDeferred.await().getOrNull()
        val seismicOutcome = seismicDeferred.await().getOrNull()

        val hasLiveWeather = weatherOutcome != null && !weatherOutcome.fromCache
        val hasLiveSeismic = seismicOutcome != null && !seismicOutcome.fromCache

        if (hasLiveWeather || hasLiveSeismic || liveAqi != null) {
            val weatherForEval = weatherOutcome?.data ?: repository.defaultWeatherFor(targetCity)
            val aqiForEval = liveAqi ?: repository.defaultAirQuality(targetCity)
            val seismicForEval = seismicOutcome?.data ?: repository.defaultSeismicTelemetry()

            if (hasLiveWeather) {
                repository.saveWeatherBundleToCache(
                    cityId = targetCity.id,
                    weather = weatherOutcome!!.data,
                    airQuality = liveAqi,
                    stations = null,
                    fetchedAtMillis = weatherOutcome.fetchedAtMillis,
                )
                settingsStore.setOfflineCacheLabel(repository.formatCurrentTimestamp())
            }
            if (hasLiveSeismic) {
                repository.saveSeismicToCache(
                    seismic = seismicOutcome!!.data,
                    fetchedAtMillis = seismicOutcome.fetchedAtMillis,
                )
            }
            settingsStore.setLastBackgroundSyncMillis(nowMillis)

            val disasterManager = DisasterNotificationManager(applicationContext)
            val alert = disasterManager.evaluateTelemetryForDisaster(
                city = weatherForEval.city,
                weather = weatherForEval,
                airQuality = aqiForEval,
                seismic = seismicForEval,
                settings = settings,
            )
            if (alert != null) {
                disasterManager.notifyIfNew(alert, nowMillis = nowMillis)
            }

            return@coroutineScope Result.success(
                workDataOf(
                    KEY_STATUS to "synced",
                    KEY_CITY_ID to targetCity.id,
                    KEY_TRIGGER_CHANNEL to triggerChannel,
                    KEY_SYNCED_AT_MILLIS to nowMillis,
                    KEY_BATTERY_PERCENT to batteryProfile.batteryPercent,
                    KEY_BATTERY_TIER to batteryProfile.tier.name,
                    KEY_SYNC_INTERVAL_MINUTES to batteryProfile.repeatIntervalMinutes,
                )
            )
        }

        if (runAttemptCount < 3) {
            Result.retry()
        } else {
            Result.failure(
                workDataOf(
                    KEY_STATUS to "network_unavailable",
                    KEY_CITY_ID to targetCity.id,
                    KEY_TRIGGER_CHANNEL to triggerChannel,
                    KEY_BATTERY_PERCENT to batteryProfile.batteryPercent,
                    KEY_BATTERY_TIER to batteryProfile.tier.name,
                )
            )
        }
    }

    private fun resolveTargetCity(
        settingsStore: SettingsDataStore,
        repository: VeridiaRepository,
    ): CityLocation {
        val settings = settingsStore.currentSettings
        val storedGps = settingsStore.getStoredGpsCoordinates()
        if (settings.selectedCityId == "gps_current" && storedGps != null) {
            return CityLocation(
                id = "gps_current",
                name = storedGps.localityName,
                country = storedGps.countryName,
                latitude = storedGps.latitude,
                longitude = storedGps.longitude,
                summaryCondition = "Protected Zone",
                summaryTempC = repository.presetCities.first().summaryTempC,
                isCurrent = true,
            )
        }
        return repository.presetCities.firstOrNull { it.id == settings.selectedCityId }
            ?: repository.presetCities.first()
    }
}

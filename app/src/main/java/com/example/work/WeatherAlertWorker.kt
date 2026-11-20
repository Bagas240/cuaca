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
 * Dedicated periodic WorkManager worker that polls live weather, air quality, and BMKG/USGS
 * seismic alert feeds in the background and dispatches local push notifications for urgent events.
 */
class WeatherAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_TRIGGER_CHANNEL = "trigger_channel"
        const val KEY_STATUS = "alert_status"
        const val KEY_CITY_ID = "city_id"
        const val KEY_CHECKED_AT_MILLIS = "checked_at_millis"
        const val KEY_ALERT_DISPATCHED = "alert_dispatched"
        const val KEY_ALERT_TITLE = "alert_title"
        const val KEY_ALERT_KIND = "alert_kind"
        const val KEY_FORCE_NOTIFY = "force_notify"

        @Volatile
        internal var repositoryFactory: (Context) -> VeridiaRepository = { context ->
            VeridiaRepository.create(context)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val settingsStore = SettingsDataStore(applicationContext)
        val settings = settingsStore.currentSettings
        val triggerChannel = inputData.getString(KEY_TRIGGER_CHANNEL) ?: "periodic_alerts"
        val isManualTrigger = triggerChannel == "manual_immediate"
        val forceNotify = inputData.getBoolean(KEY_FORCE_NOTIFY, false)

        val anyAlertEnabled = settings.seismicPWaveAlert ||
            settings.typhoonFlashWatch ||
            settings.aqiSmogInversion

        if (!anyAlertEnabled) {
            return@coroutineScope Result.success(
                workDataOf(
                    KEY_STATUS to "alerts_disabled",
                    KEY_TRIGGER_CHANNEL to triggerChannel,
                    KEY_ALERT_DISPATCHED to false,
                )
            )
        }

        val batteryProfile = WeatherSyncScheduler.readBatteryPowerProfile(applicationContext)
        if (!isManualTrigger && batteryProfile.tier == SyncBatteryTier.CRITICAL_BATTERY) {
            return@coroutineScope Result.success(
                workDataOf(
                    KEY_STATUS to "deferred_critical_battery",
                    KEY_TRIGGER_CHANNEL to triggerChannel,
                    KEY_ALERT_DISPATCHED to false,
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
        val hasAnyLiveTelemetry = hasLiveWeather || hasLiveSeismic || liveAqi != null

        if (!hasAnyLiveTelemetry) {
            return@coroutineScope if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure(
                    workDataOf(
                        KEY_STATUS to "network_unavailable",
                        KEY_CITY_ID to targetCity.id,
                        KEY_TRIGGER_CHANNEL to triggerChannel,
                        KEY_ALERT_DISPATCHED to false,
                    )
                )
            }
        }

        val nowMillis = System.currentTimeMillis()
        val weatherForEval = weatherOutcome?.data ?: repository.defaultWeatherFor(targetCity)
        val aqiForEval = liveAqi ?: repository.defaultAirQuality(targetCity)
        val seismicForEval = seismicOutcome?.data ?: repository.defaultSeismicTelemetry()

        if (hasLiveWeather) {
            repository.saveWeatherBundleToCache(
                cityId = targetCity.id,
                weather = weatherForEval,
                airQuality = liveAqi,
                stations = null,
                fetchedAtMillis = weatherOutcome!!.fetchedAtMillis,
            )
            settingsStore.setOfflineCacheLabel(repository.formatCurrentTimestamp())
            settingsStore.setLastBackgroundSyncMillis(nowMillis)
        }
        if (hasLiveSeismic) {
            repository.saveSeismicToCache(
                seismic = seismicForEval,
                fetchedAtMillis = seismicOutcome!!.fetchedAtMillis,
            )
        }

        val disasterManager = DisasterNotificationManager(applicationContext)
        val alert = disasterManager.evaluateTelemetryForDisaster(
            city = weatherForEval.city,
            weather = weatherForEval,
            airQuality = aqiForEval,
            seismic = seismicForEval,
            settings = settings,
        )

        val notified = if (alert != null) {
            disasterManager.notifyIfNew(alert, force = forceNotify, nowMillis = nowMillis)
        } else {
            false
        }

        val status = when {
            alert != null && notified -> "urgent_alert_notified"
            alert != null -> "urgent_alert_deduplicated"
            else -> "all_clear"
        }

        Result.success(
            workDataOf(
                KEY_STATUS to status,
                KEY_CITY_ID to targetCity.id,
                KEY_TRIGGER_CHANNEL to triggerChannel,
                KEY_CHECKED_AT_MILLIS to nowMillis,
                KEY_ALERT_DISPATCHED to notified,
                KEY_ALERT_TITLE to (alert?.title ?: ""),
                KEY_ALERT_KIND to (alert?.kind?.name ?: ""),
            )
        )
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

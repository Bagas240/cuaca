package com.example.work

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.PowerManager
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

enum class SyncBatteryTier {
    CHARGING,
    NORMAL,
    LOW_BATTERY,
    CRITICAL_BATTERY,
}

data class SyncBatteryProfile(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val isBatteryUnknown: Boolean = false,
    val tier: SyncBatteryTier = SyncBatteryTier.NORMAL,
    val repeatIntervalMinutes: Long = WeatherSyncScheduler.SYNC_INTERVAL_MINUTES,
    val flexIntervalMinutes: Long = WeatherSyncScheduler.FLEX_INTERVAL_MINUTES,
)

/**
 * Schedules Veridia's periodic WorkManager weather telemetry tasks under power-saving
 * constraints: background updates execute only when the device is charging OR connected
 * to an unmetered Wi-Fi network, and dynamically decrease update frequency via [BatteryManager]
 * when battery levels drop to low or critical states.
 */
object WeatherSyncScheduler {

    const val UNIQUE_WORK_WIFI = "veridia_weather_sync_wifi"
    const val UNIQUE_WORK_CHARGING = "veridia_weather_sync_charging"
    const val WORK_TAG = "veridia_weather_periodic_sync"

    const val SYNC_INTERVAL_MINUTES = 15L
    const val FLEX_INTERVAL_MINUTES = 5L
    const val SYNC_INTERVAL_LOW_BATTERY_MINUTES = 60L
    const val FLEX_INTERVAL_LOW_BATTERY_MINUTES = 15L
    const val SYNC_INTERVAL_CRITICAL_BATTERY_MINUTES = 180L
    const val FLEX_INTERVAL_CRITICAL_BATTERY_MINUTES = 30L

    const val LOW_BATTERY_THRESHOLD_PERCENT = 20
    const val CRITICAL_BATTERY_THRESHOLD_PERCENT = 10

    /**
     * Queries Android [BatteryManager] (with [Intent.ACTION_BATTERY_CHANGED] and [PowerManager]
     * battery saver fallback) to determine current battery capacity and adaptive sync cadence.
     */
    fun readBatteryPowerProfile(context: Context): SyncBatteryProfile {
        val appContext = context.applicationContext
        val batteryManager = appContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val batteryIntent = runCatching {
            appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }.getOrNull()

        val bmCapacity = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            ?: Int.MIN_VALUE
        val bmStatus = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            ?: Int.MIN_VALUE

        val intentLevel = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val intentScale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val intentPercent = if (intentLevel >= 0 && intentScale > 0) {
            ((intentLevel * 100f) / intentScale).roundToInt().coerceIn(0, 100)
        } else {
            null
        }
        val intentStatus = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val intentPlugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0

        val isCharging = (batteryManager?.isCharging == true) ||
            bmStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
            bmStatus == BatteryManager.BATTERY_STATUS_FULL ||
            intentStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
            intentStatus == BatteryManager.BATTERY_STATUS_FULL ||
            intentPlugged != 0

        val isUnknown: Boolean
        val batteryPercent = when {
            bmCapacity in 1..100 -> {
                isUnknown = false
                bmCapacity
            }
            intentPercent != null -> {
                isUnknown = false
                intentPercent
            }
            bmCapacity == 0 && batteryIntent != null -> {
                isUnknown = false
                0
            }
            else -> {
                isUnknown = true
                100
            }
        }

        val isPowerSave = powerManager?.isPowerSaveMode == true
        return resolveSyncPowerProfile(
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            isPowerSaveMode = isPowerSave,
            isBatteryUnknown = isUnknown,
        )
    }

    fun resolveSyncPowerProfile(
        batteryPercent: Int,
        isCharging: Boolean,
        isPowerSaveMode: Boolean,
        isBatteryUnknown: Boolean = false,
    ): SyncBatteryProfile {
        val clamped = batteryPercent.coerceIn(0, 100)
        val tier = when {
            isCharging -> SyncBatteryTier.CHARGING
            clamped <= CRITICAL_BATTERY_THRESHOLD_PERCENT -> SyncBatteryTier.CRITICAL_BATTERY
            clamped <= LOW_BATTERY_THRESHOLD_PERCENT || isPowerSaveMode -> SyncBatteryTier.LOW_BATTERY
            else -> SyncBatteryTier.NORMAL
        }
        val (intervalMin, flexMin) = when (tier) {
            SyncBatteryTier.CHARGING,
            SyncBatteryTier.NORMAL -> SYNC_INTERVAL_MINUTES to FLEX_INTERVAL_MINUTES
            SyncBatteryTier.LOW_BATTERY -> SYNC_INTERVAL_LOW_BATTERY_MINUTES to FLEX_INTERVAL_LOW_BATTERY_MINUTES
            SyncBatteryTier.CRITICAL_BATTERY -> SYNC_INTERVAL_CRITICAL_BATTERY_MINUTES to FLEX_INTERVAL_CRITICAL_BATTERY_MINUTES
        }
        return SyncBatteryProfile(
            batteryPercent = clamped,
            isCharging = isCharging,
            isPowerSaveMode = isPowerSaveMode,
            isBatteryUnknown = isBatteryUnknown,
            tier = tier,
            repeatIntervalMinutes = intervalMin,
            flexIntervalMinutes = flexMin,
        )
    }

    /**
     * Constraints allowing periodic background weather sync over unmetered Wi-Fi
     * while protecting battery health when unplugged.
     */
    fun buildWifiConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresBatteryNotLow(true)
        .build()

    /**
     * Constraints allowing periodic background weather sync while the device
     * is plugged in and charging on any active network connection.
     */
    fun buildChargingConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresCharging(true)
        .build()

    fun buildWifiPeriodicWorkRequest(
        batteryProfile: SyncBatteryProfile = SyncBatteryProfile(),
    ): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<WeatherSyncWorker>(
            repeatInterval = batteryProfile.repeatIntervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = batteryProfile.flexIntervalMinutes,
            flexTimeIntervalUnit = TimeUnit.MINUTES,
        )
            .setConstraints(buildWifiConstraints())
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            )
            .setInputData(
                workDataOf(
                    WeatherSyncWorker.KEY_TRIGGER_CHANNEL to "wifi",
                    WeatherSyncWorker.KEY_BATTERY_TIER to batteryProfile.tier.name,
                    WeatherSyncWorker.KEY_SYNC_INTERVAL_MINUTES to batteryProfile.repeatIntervalMinutes,
                )
            )
            .addTag(WORK_TAG)
            .addTag("tier_${batteryProfile.tier.name.lowercase()}")
            .build()

    fun buildChargingPeriodicWorkRequest(): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<WeatherSyncWorker>(
            repeatInterval = SYNC_INTERVAL_MINUTES,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = FLEX_INTERVAL_MINUTES,
            flexTimeIntervalUnit = TimeUnit.MINUTES,
        )
            .setConstraints(buildChargingConstraints())
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS,
            )
            .setInputData(
                workDataOf(
                    WeatherSyncWorker.KEY_TRIGGER_CHANNEL to "charging",
                    WeatherSyncWorker.KEY_BATTERY_TIER to SyncBatteryTier.CHARGING.name,
                    WeatherSyncWorker.KEY_SYNC_INTERVAL_MINUTES to SYNC_INTERVAL_MINUTES,
                )
            )
            .addTag(WORK_TAG)
            .build()

    /**
     * Enqueues both complementary unique periodic work requests so updates fire
     * periodically whenever charging OR on Wi-Fi, automatically scaling the Wi-Fi
     * update interval down when [BatteryManager] reports low or critical battery.
     */
    fun schedulePeriodicWeatherSync(context: Context): SyncBatteryProfile {
        val profile = readBatteryPowerProfile(context)
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_WIFI,
            ExistingPeriodicWorkPolicy.UPDATE,
            buildWifiPeriodicWorkRequest(profile),
        )
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_CHARGING,
            ExistingPeriodicWorkPolicy.UPDATE,
            buildChargingPeriodicWorkRequest(),
        )
        return profile
    }

    /**
     * Adapts the periodic Wi-Fi sync cadence when battery state shifts in the background
     * without disturbing the charging-only trigger channel.
     */
    fun adaptScheduleForBatteryProfile(
        context: Context,
        profile: SyncBatteryProfile = readBatteryPowerProfile(context),
    ): SyncBatteryProfile {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_WIFI,
            ExistingPeriodicWorkPolicy.UPDATE,
            buildWifiPeriodicWorkRequest(profile),
        )
        return profile
    }

    fun cancelPeriodicWeatherSync(context: Context) {
        val workManager = WorkManager.getInstance(context.applicationContext)
        workManager.cancelUniqueWork(UNIQUE_WORK_WIFI)
        workManager.cancelUniqueWork(UNIQUE_WORK_CHARGING)
    }

    fun isDeviceChargingOrOnWifi(context: Context): Boolean {
        return isDeviceCharging(context) || isOnUnmeteredWifi(context)
    }

    fun isDeviceCharging(context: Context): Boolean {
        return readBatteryPowerProfile(context).isCharging
    }

    fun isOnUnmeteredWifi(context: Context): Boolean {
        val cm = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
    }
}

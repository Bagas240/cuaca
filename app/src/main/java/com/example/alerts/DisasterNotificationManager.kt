package com.example.alerts

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.AirQualityTelemetry
import com.example.data.model.CityLocation
import com.example.data.model.SeismicTelemetry
import com.example.data.model.UserSettings
import com.example.data.model.WeatherConditionKind
import com.example.data.model.WeatherTelemetry
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class DisasterAlertEvent(
    val signature: String,
    val kind: DisasterKind,
    val title: String,
    val message: String,
    val guidance: String,
    val distanceKm: Int?,
    val timestampMillis: Long = System.currentTimeMillis(),
)

/**
 * Evaluates real-time telemetry near the user's protected zone, posts high-priority
 * disaster notifications tailored to the disaster type, and manages alert deduplication.
 */
class DisasterNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "veridia_disaster_ews_v1"
        private const val CHANNEL_NAME = "Natural Disaster & BMKG Alerts"
        private const val NOTIFICATION_ID_BASE = 7001
        private const val PREFS_NAME = "veridia_alert_dedup_prefs"
        private const val KEY_LAST_SIGNATURE = "last_notified_signature"
        private const val KEY_LAST_NOTIFIED_AT = "last_notified_at_millis"
        const val DEFAULT_DEDUP_COOLDOWN_MS = 6 * 60 * 60_000L
        const val EXTRA_NAV_DESTINATION = "extra_nav_destination"
        const val EXTRA_ALERT_KIND = "extra_alert_kind"
        const val DESTINATION_ALERTS = "disaster_monitor"
    }

    private val dedupPrefs by lazy {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private var lastNotifiedSignature: String? = null

    init {
        ensureNotificationChannel()
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Critical earthquake, tsunami, extreme storm, and hazardous air alerts near your zone."
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun evaluateTelemetryForDisaster(
        city: CityLocation,
        weather: WeatherTelemetry,
        airQuality: AirQualityTelemetry,
        seismic: SeismicTelemetry,
        settings: UserSettings,
    ): DisasterAlertEvent? {
        val topEvent = seismic.recentEvents.firstOrNull()
        val distanceKm = haversineKm(
            city.latitude,
            city.longitude,
            seismic.epicenterLat,
            seismic.epicenterLon,
        ).roundToInt()

        // 1. Tsunami Warning (highest priority)
        val hasTsunamiPotential = !seismic.tsunamiStatus.contains("Tidak", ignoreCase = true) &&
            !seismic.tsunamiStatus.contains("No Tsunami", ignoreCase = true) &&
            (seismic.tsunamiStatus.contains("Waspada", ignoreCase = true) ||
                seismic.tsunamiStatus.contains("Berpotensi", ignoreCase = true))
        if (settings.seismicPWaveAlert && hasTsunamiPotential) {
            return DisasterAlertEvent(
                signature = "tsunami_${seismic.bannerMagnitude}_${topEvent?.timeAgoLabel ?: seismic.activeEventTitle}",
                kind = DisasterKind.TSUNAMI,
                title = "Tsunami Warning · ${seismic.bannerMagnitude}",
                message = "${seismic.activeEventDescription} (${distanceKm} km from ${city.name})",
                guidance = "Move immediately to higher ground away from coastal areas.",
                distanceKm = distanceKm,
            )
        }

        // 2. Earthquake near user zone (or major M 5.5+ regional earthquake)
        val topMag = topEvent?.magnitude ?: 0.0
        val isNearbyQuake = distanceKm <= 380 && topMag >= 4.5
        val isMajorQuake = topMag >= 5.5
        if (settings.seismicPWaveAlert && (isNearbyQuake || isMajorQuake)) {
            return DisasterAlertEvent(
                signature = "quake_${topMag}_${topEvent?.location ?: seismic.activeEventTitle}_${topEvent?.timeAgoLabel ?: ""}",
                kind = DisasterKind.EARTHQUAKE,
                title = "Earthquake ${seismic.bannerMagnitude} Detected",
                message = "${topEvent?.location ?: seismic.activeEventTitle} · ${distanceKm} km from ${city.name} · Depth ${seismic.depthKm.roundToInt()} km",
                guidance = "Drop, Cover, and Hold On. Stay clear of glass and unstable structures.",
                distanceKm = distanceKm,
            )
        }

        // 3. Severe Storm / Gale Wind / Imminent Downpour Warning
        val imminentSevereSlot = weather.hourlyForecast.drop(1).take(3).firstOrNull { slot ->
            slot.condition == WeatherConditionKind.THUNDERSTORM || slot.precipMm >= 12.0
        }
        val isHeavyTroughRain = weather.conditionKind == WeatherConditionKind.RAIN &&
            weather.precipChancePercent >= 85 &&
            weather.pressureHpa <= 1004
        val isSevereWeather = weather.conditionKind == WeatherConditionKind.THUNDERSTORM ||
            weather.windSpeedKmh >= 48 ||
            weather.windGustKmh >= 65 ||
            isHeavyTroughRain ||
            imminentSevereSlot != null

        if (settings.typhoonFlashWatch && isSevereWeather) {
            val triggerTag = when {
                weather.conditionKind == WeatherConditionKind.THUNDERSTORM -> "thunderstorm"
                weather.windGustKmh >= 65 || weather.windSpeedKmh >= 48 -> "gale_${weather.windGustKmh / 10}"
                isHeavyTroughRain -> "trough_rain_${weather.pressureHpa / 5}"
                else -> "imminent_${imminentSevereSlot?.timeLabel ?: "soon"}"
            }
            val headlineDetail = when {
                weather.conditionKind == WeatherConditionKind.THUNDERSTORM -> weather.conditionLabel
                weather.windGustKmh >= 65 -> "Damaging Wind Gusts ${weather.windGustKmh} km/h"
                weather.windSpeedKmh >= 48 -> "Gale Winds ${weather.windSpeedKmh} km/h"
                isHeavyTroughRain -> "Heavy Monsoon Downpour (${weather.pressureHpa} hPa)"
                else -> "Incoming Storm Cell at ${imminentSevereSlot?.timeLabel}"
            }
            return DisasterAlertEvent(
                signature = "storm_${city.id}_$triggerTag",
                kind = DisasterKind.SEVERE_STORM,
                title = "Urgent Weather Alert · ${city.name}",
                message = "$headlineDetail · Wind ${weather.windSpeedKmh} km/h ${weather.windDirectionLabel} · ${weather.precipChancePercent}% precip",
                guidance = "Seek shelter indoors and avoid open water, trees, and power lines.",
                distanceKm = 0,
            )
        }

        // 4. Hazardous Air Quality / Smog Emergency
        if (settings.aqiSmogInversion && (airQuality.pm25 >= 100.0 || airQuality.regionalAqi >= 151)) {
            return DisasterAlertEvent(
                signature = "aqi_${city.id}_${airQuality.regionalAqi / 20}",
                kind = DisasterKind.HAZARDOUS_AIR,
                title = "Hazardous Air Alert · AQI ${airQuality.regionalAqi}",
                message = "${city.name} PM2.5 ${String.format(Locale.US, "%.1f", airQuality.pm25)} µg/m³ (${airQuality.regionalCategory})",
                guidance = "Keep windows closed, enable air filtration, and wear an N95 mask outdoors.",
                distanceKm = 0,
            )
        }

        return null
    }

    fun notifyIfNew(
        event: DisasterAlertEvent,
        force: Boolean = false,
        cooldownMillis: Long = DEFAULT_DEDUP_COOLDOWN_MS,
        nowMillis: Long = System.currentTimeMillis(),
    ): Boolean {
        val persistedSignature = dedupPrefs.getString(KEY_LAST_SIGNATURE, null)
        val persistedAt = dedupPrefs.getLong(KEY_LAST_NOTIFIED_AT, 0L)
        val withinCooldown = (nowMillis - persistedAt) in 0 until cooldownMillis
        val matchesRecent = event.signature == lastNotifiedSignature ||
            (event.signature == persistedSignature && withinCooldown)

        if (!force && matchesRecent) return false

        lastNotifiedSignature = event.signature
        dedupPrefs.edit()
            .putString(KEY_LAST_SIGNATURE, event.signature)
            .putLong(KEY_LAST_NOTIFIED_AT, nowMillis)
            .apply()
        postSystemNotification(event)
        return true
    }

    fun clearDedupHistory() {
        lastNotifiedSignature = null
        dedupPrefs.edit().clear().apply()
    }

    fun postSystemNotification(event: DisasterAlertEvent) {
        if (!hasNotificationPermission()) return
        ensureNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAV_DESTINATION, DESTINATION_ALERTS)
            putExtra(EXTRA_ALERT_KIND, event.kind.name)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            event.kind.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(event.title)
            .setContentText(event.message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${event.message}\n\n${event.guidance}")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_BASE + event.kind.ordinal,
                notification,
            )
        }
    }

    fun buildSampleEvent(kind: DisasterKind, city: CityLocation): DisasterAlertEvent {
        return when (kind) {
            DisasterKind.EARTHQUAKE -> DisasterAlertEvent(
                signature = "test_quake_${System.currentTimeMillis()}",
                kind = DisasterKind.EARTHQUAKE,
                title = "Earthquake M 5.8 · ${city.name} Zone",
                message = "Simulated BMKG TEWS shake alert 28 km from ${city.name} · Depth 12 km · MMI IV",
                guidance = "Drop, Cover, and Hold On. Evacuate calmly after shaking stops.",
                distanceKm = 28,
            )
            DisasterKind.TSUNAMI -> DisasterAlertEvent(
                signature = "test_tsunami_${System.currentTimeMillis()}",
                kind = DisasterKind.TSUNAMI,
                title = "Tsunami Warning · Coastal ${city.name}",
                message = "Simulated coastal sea-level surge advisory near ${city.name} shoreline",
                guidance = "Evacuate immediately to elevation above 20 meters.",
                distanceKm = 35,
            )
            DisasterKind.SEVERE_STORM -> DisasterAlertEvent(
                signature = "test_storm_${System.currentTimeMillis()}",
                kind = DisasterKind.SEVERE_STORM,
                title = "Severe Thunderstorm & Gale · ${city.name}",
                message = "Simulated convective squall · Gusts 68 km/h · Heavy rain & lightning",
                guidance = "Stay indoors away from windows and avoid flooded roads.",
                distanceKm = 5,
            )
            DisasterKind.HAZARDOUS_AIR -> DisasterAlertEvent(
                signature = "test_aqi_${System.currentTimeMillis()}",
                kind = DisasterKind.HAZARDOUS_AIR,
                title = "Hazardous Air Emergency · ${city.name}",
                message = "Simulated PM2.5 spike 142.0 µg/m³ · AQI 195 (Unhealthy)",
                guidance = "Limit outdoor exertion and use respiratory protection.",
                distanceKm = 0,
            )
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * r * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }
}

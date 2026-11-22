package com.example.data.location

import com.example.data.model.AppThemeMode
import com.example.data.model.SolarThemePhase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

data class MoonPhaseInfo(
    val phaseName: String,
    val illuminationPercent: Int,
    val phaseFraction: Float,
    val ageDays: Double,
)

data class SolarDaySchedule(
    val sunriseTime: String,
    val sunsetTime: String,
    val solarNoonTime: String,
    val daylightDuration: String,
    val solarProgress: Float,
    val todayDateLabel: String,
    val nextSolarEventLabel: String,
    val moonPhase: MoonPhaseInfo,
)

/**
 * Computes astronomical sunrise, sunset, solar noon, daylight duration, live solar progress,
 * moon phase from current date, and solar time-of-day theme phases anchored to location coordinates.
 */
object SolarScheduleCalculator {

    private const val ZENITH_DEG = 90.833
    private const val SYNODIC_MONTH_DAYS = 29.53058867
    // Reference astronomical New Moon: 2000-01-06 18:14:00 UTC
    private const val REF_NEW_MOON_UTC_MILLIS = 947182440000L
    private const val MILLIS_PER_DAY = 86_400_000.0

    fun computeMoonPhase(dateMillis: Long = System.currentTimeMillis()): MoonPhaseInfo {
        val daysSinceRef = (dateMillis - REF_NEW_MOON_UTC_MILLIS) / MILLIS_PER_DAY
        val cycles = daysSinceRef / SYNODIC_MONTH_DAYS
        val fraction = (cycles - floor(cycles)).toFloat().coerceIn(0f, 0.9999f)
        val ageDays = fraction * SYNODIC_MONTH_DAYS
        val illumination = (((1.0 - cos(2.0 * PI * fraction)) / 2.0) * 100.0)
            .roundToInt()
            .coerceIn(0, 100)

        val phaseName = when {
            fraction < 0.035f || fraction >= 0.965f -> "New Moon"
            fraction < 0.215f -> "Waxing Crescent"
            fraction < 0.285f -> "First Quarter"
            fraction < 0.465f -> "Waxing Gibbous"
            fraction < 0.535f -> "Full Moon"
            fraction < 0.715f -> "Waning Gibbous"
            fraction < 0.785f -> "Last Quarter"
            else -> "Waning Crescent"
        }

        return MoonPhaseInfo(
            phaseName = phaseName,
            illuminationPercent = illumination,
            phaseFraction = fraction,
            ageDays = (ageDays * 10.0).roundToInt() / 10.0,
        )
    }

    fun resolveSolarThemePhase(
        sunriseTime: String,
        sunsetTime: String,
        longitude: Double,
        dateMillis: Long = System.currentTimeMillis(),
        timeZoneOffsetHours: Double? = null,
    ): SolarThemePhase {
        val locationTz = resolveLocationTimeZone(longitude, dateMillis, timeZoneOffsetHours)
        val cal = Calendar.getInstance(locationTz).apply {
            timeInMillis = dateMillis
        }
        val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        return resolveSolarThemePhaseForMinute(
            nowMinutes = nowMinutes,
            sunriseMinutes = parseHHmm(sunriseTime, 5 * 60 + 38),
            sunsetMinutes = parseHHmm(sunsetTime, 17 * 60 + 52),
        )
    }

    fun resolveSolarThemePhaseForMinute(
        nowMinutes: Int,
        sunriseMinutes: Int,
        sunsetMinutes: Int,
    ): SolarThemePhase {
        val dawnStart = sunriseMinutes - 45
        val dawnEnd = sunriseMinutes + 60
        val afternoonStart = (sunsetMinutes - 175).coerceAtLeast(dawnEnd + 1)
        val sunsetWindowStart = sunsetMinutes - 40
        val sunsetWindowEnd = sunsetMinutes + 35
        val duskEnd = sunsetMinutes + 75

        return when {
            nowMinutes in dawnStart..dawnEnd -> SolarThemePhase.SUNRISE_SUNSET
            nowMinutes in sunsetWindowStart..sunsetWindowEnd -> SolarThemePhase.SUNRISE_SUNSET
            nowMinutes in (dawnEnd + 1) until afternoonStart -> SolarThemePhase.DAY
            nowMinutes in afternoonStart until sunsetWindowStart -> SolarThemePhase.AFTERNOON_DUSK
            nowMinutes in (sunsetWindowEnd + 1)..duskEnd -> SolarThemePhase.AFTERNOON_DUSK
            else -> SolarThemePhase.NIGHT
        }
    }

    fun resolveEffectiveThemePhase(
        themeMode: AppThemeMode,
        sunriseTime: String,
        sunsetTime: String,
        longitude: Double,
        dateMillis: Long = System.currentTimeMillis(),
        timeZoneOffsetHours: Double? = null,
    ): SolarThemePhase {
        return when (themeMode) {
            AppThemeMode.AUTO_SOLAR -> resolveSolarThemePhase(
                sunriseTime = sunriseTime,
                sunsetTime = sunsetTime,
                longitude = longitude,
                dateMillis = dateMillis,
                timeZoneOffsetHours = timeZoneOffsetHours,
            )
            AppThemeMode.DAY -> SolarThemePhase.DAY
            AppThemeMode.NIGHT -> SolarThemePhase.NIGHT
            AppThemeMode.SUNRISE_SUNSET -> SolarThemePhase.SUNRISE_SUNSET
            AppThemeMode.AFTERNOON_DUSK -> SolarThemePhase.AFTERNOON_DUSK
        }
    }

    fun computeForLocation(
        latitude: Double,
        longitude: Double,
        dateMillis: Long = System.currentTimeMillis(),
        timeZoneOffsetHours: Double? = null,
    ): SolarDaySchedule {
        val clampedLat = latitude.coerceIn(-66.0, 66.0)
        val locationTz = resolveLocationTimeZone(longitude, dateMillis, timeZoneOffsetHours)
        val effectiveOffsetHours = locationTz.rawOffset / 3_600_000.0
        val cal = Calendar.getInstance(locationTz).apply {
            timeInMillis = dateMillis
        }

        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)

        val riseMinutes = computeSolarEventMinutes(
            dayOfYear = dayOfYear,
            approxLocalHour = 6.0,
            latitudeDeg = clampedLat,
            longitudeDeg = longitude,
            tzOffsetHours = effectiveOffsetHours,
            isSunrise = true,
        )
        val setMinutes = computeSolarEventMinutes(
            dayOfYear = dayOfYear,
            approxLocalHour = 18.0,
            latitudeDeg = clampedLat,
            longitudeDeg = longitude,
            tzOffsetHours = effectiveOffsetHours,
            isSunrise = false,
        )
        val noonMinutes = computeSolarNoonMinutes(
            dayOfYear = dayOfYear,
            longitudeDeg = longitude,
            tzOffsetHours = effectiveOffsetHours,
        )

        val sunriseStr = formatMinutesHHmm(riseMinutes)
        val sunsetStr = formatMinutesHHmm(setMinutes)
        val noonStr = formatMinutesHHmm(noonMinutes)

        return buildMetrics(
            sunriseMinutes = riseMinutes,
            sunsetMinutes = setMinutes,
            solarNoonMinutes = noonMinutes,
            nowMinutes = nowMinutes,
            sunriseTime = sunriseStr,
            sunsetTime = sunsetStr,
            solarNoonTime = noonStr,
            dateMillis = dateMillis,
            locationTimeZone = locationTz,
        )
    }

    fun enrichFromTimes(
        sunriseTime: String,
        sunsetTime: String,
        longitude: Double,
        dateMillis: Long = System.currentTimeMillis(),
    ): SolarDaySchedule {
        val locationTz = resolveLocationTimeZone(longitude, dateMillis, timeZoneOffsetHours = null)
        val cal = Calendar.getInstance(locationTz).apply {
            timeInMillis = dateMillis
        }
        val nowMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val riseMinutes = parseHHmm(sunriseTime, 5 * 60 + 38)
        val setMinutes = parseHHmm(sunsetTime, 17 * 60 + 52)
        val noonMinutes = (riseMinutes + setMinutes) / 2

        return buildMetrics(
            sunriseMinutes = riseMinutes,
            sunsetMinutes = setMinutes,
            solarNoonMinutes = noonMinutes,
            nowMinutes = nowMinutes,
            sunriseTime = formatMinutesHHmm(riseMinutes),
            sunsetTime = formatMinutesHHmm(setMinutes),
            solarNoonTime = formatMinutesHHmm(noonMinutes),
            dateMillis = dateMillis,
            locationTimeZone = locationTz,
        )
    }

    private fun resolveLocationTimeZone(
        longitude: Double,
        dateMillis: Long,
        timeZoneOffsetHours: Double?,
    ): TimeZone {
        val deviceTz = TimeZone.getDefault()
        val deviceOffsetHours = deviceTz.getOffset(dateMillis) / 3_600_000.0
        val effectiveOffsetHours = timeZoneOffsetHours ?: run {
            val deviceMeridianLon = deviceOffsetHours * 15.0
            if (abs(longitude - deviceMeridianLon) <= 22.5) {
                deviceOffsetHours
            } else {
                (longitude / 15.0).roundToInt().toDouble()
            }
        }
        return TimeZone.getTimeZone("UTC").apply {
            rawOffset = (effectiveOffsetHours * 3_600_000.0).roundToInt()
        }
    }

    private fun buildMetrics(
        sunriseMinutes: Int,
        sunsetMinutes: Int,
        solarNoonMinutes: Int,
        nowMinutes: Int,
        sunriseTime: String,
        sunsetTime: String,
        solarNoonTime: String,
        dateMillis: Long,
        locationTimeZone: TimeZone,
    ): SolarDaySchedule {
        val totalMin = (sunsetMinutes - sunriseMinutes).coerceAtLeast(60)
        val progress = ((nowMinutes - sunriseMinutes).toFloat() / totalMin.toFloat()).coerceIn(0.04f, 0.96f)
        val daylightHours = totalMin / 60
        val daylightMins = totalMin % 60
        val daylightLabel = "${daylightHours}h ${daylightMins}m Daylight"

        val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).apply {
            timeZone = locationTimeZone
        }
        val todayLabel = "Today · ${dateFormat.format(Date(dateMillis))}"

        val nextEventLabel = when {
            nowMinutes < sunriseMinutes -> {
                val diff = (sunriseMinutes - nowMinutes).coerceAtLeast(1)
                "Sunrise in ${formatDurationShort(diff)}"
            }
            nowMinutes < sunsetMinutes -> {
                val diff = (sunsetMinutes - nowMinutes).coerceAtLeast(1)
                "Sunset in ${formatDurationShort(diff)}"
            }
            else -> {
                val diff = ((24 * 60 - nowMinutes) + sunriseMinutes).coerceAtLeast(1)
                "Next sunrise in ${formatDurationShort(diff)}"
            }
        }

        return SolarDaySchedule(
            sunriseTime = sunriseTime,
            sunsetTime = sunsetTime,
            solarNoonTime = solarNoonTime,
            daylightDuration = daylightLabel,
            solarProgress = progress,
            todayDateLabel = todayLabel,
            nextSolarEventLabel = nextEventLabel,
            moonPhase = computeMoonPhase(dateMillis),
        )
    }

    private fun computeSolarEventMinutes(
        dayOfYear: Int,
        approxLocalHour: Double,
        latitudeDeg: Double,
        longitudeDeg: Double,
        tzOffsetHours: Double,
        isSunrise: Boolean,
    ): Int {
        val gamma = (2.0 * PI / 365.0) * (dayOfYear - 1 + (approxLocalHour - 12.0) / 24.0)
        val eqTime = 229.18 * (
            0.000075 +
                0.001868 * cos(gamma) -
                0.032077 * sin(gamma) -
                0.014615 * cos(2.0 * gamma) -
                0.040849 * sin(2.0 * gamma)
            )
        val decl = 0.006918 -
            0.399912 * cos(gamma) +
            0.070257 * sin(gamma) -
            0.006758 * cos(2.0 * gamma) +
            0.000907 * sin(2.0 * gamma) -
            0.002697 * cos(3.0 * gamma) +
            0.00148 * sin(3.0 * gamma)

        val latRad = Math.toRadians(latitudeDeg)
        val zenithRad = Math.toRadians(ZENITH_DEG)
        val cosHa = ((cos(zenithRad) / (cos(latRad) * cos(decl))) - (tan(latRad) * tan(decl)))
            .coerceIn(-0.999, 0.999)
        val haDeg = Math.toDegrees(acos(cosHa))

        val utcMinutes = if (isSunrise) {
            720.0 - 4.0 * (longitudeDeg + haDeg) - eqTime
        } else {
            720.0 - 4.0 * (longitudeDeg - haDeg) - eqTime
        }
        val localMinutes = utcMinutes + tzOffsetHours * 60.0
        return normalizeDayMinutes(localMinutes.roundToInt())
    }

    private fun computeSolarNoonMinutes(
        dayOfYear: Int,
        longitudeDeg: Double,
        tzOffsetHours: Double,
    ): Int {
        val gamma = (2.0 * PI / 365.0) * (dayOfYear - 1)
        val eqTime = 229.18 * (
            0.000075 +
                0.001868 * cos(gamma) -
                0.032077 * sin(gamma) -
                0.014615 * cos(2.0 * gamma) -
                0.040849 * sin(2.0 * gamma)
            )
        val utcNoon = 720.0 - 4.0 * longitudeDeg - eqTime
        return normalizeDayMinutes((utcNoon + tzOffsetHours * 60.0).roundToInt())
    }

    private fun normalizeDayMinutes(minutes: Int): Int {
        val mod = minutes % (24 * 60)
        return if (mod < 0) mod + 24 * 60 else mod
    }

    private fun parseHHmm(hhmm: String, defaultMinutes: Int): Int {
        val parts = hhmm.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: return defaultMinutes
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return (h.coerceIn(0, 23) * 60) + m.coerceIn(0, 59)
    }

    private fun formatMinutesHHmm(totalMinutes: Int): String {
        val normalized = normalizeDayMinutes(totalMinutes)
        val h = normalized / 60
        val m = normalized % 60
        return String.format(Locale.US, "%02d:%02d", h, m)
    }

    private fun formatDurationShort(totalMinutes: Int): String {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}

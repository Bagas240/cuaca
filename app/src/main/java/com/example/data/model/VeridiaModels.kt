package com.example.data.model

enum class TemperatureUnit { CELSIUS, FAHRENHEIT }
enum class WindUnit { KMH, MPH, KNOTS }
enum class PressureUnit { HPA, INHG }
enum class AtmosphereStratum { SURFACE, MID_CLOUD, UPPER_STRATUM }
enum class RadarLayer { RAIN_DOPPLER, WIND_STREAM, SATELLITE }
enum class AqiMapLayer { AQI, PM25, PM10, NO2 }
enum class WeatherMapLayer { RAIN_RADAR, CLOUDS_TEMP, WIND_VECTOR }

enum class WeatherConditionKind {
    RAIN,
    CLOUDY,
    PARTLY_CLOUDY,
    SUNNY,
    THUNDERSTORM,
    SUNSET,
    FOG,
}

data class CityLocation(
    val id: String,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val summaryCondition: String,
    val summaryTempC: Int,
    val isCurrent: Boolean = false,
    val bmkgAdm4Code: String? = null,
)

data class HourlyForecastItem(
    val timeLabel: String,
    val tempC: Int,
    val condition: WeatherConditionKind,
    val isCurrent: Boolean = false,
    val isSunset: Boolean = false,
    val precipPercent: Int = 20,
    val precipMm: Double = 0.0,
)

data class DailyForecastItem(
    val dayLabel: String,
    val precipPercent: Int,
    val condition: WeatherConditionKind,
    val minTempC: Int,
    val maxTempC: Int,
)

data class WeatherTelemetry(
    val city: CityLocation,
    val currentTempC: Int,
    val feelsLikeC: Int,
    val conditionLabel: String,
    val conditionKind: WeatherConditionKind,
    val precipChancePercent: Int,
    val precipStatusLabel: String,
    val windSpeedKmh: Int,
    val windGustKmh: Int,
    val windBearingDeg: Int,
    val windDirectionLabel: String,
    val pressureHpa: Int,
    val pressureTrendLabel: String,
    val humidityPercent: Int,
    val dewPointC: Double,
    val uvIndex: Double,
    val uvLevelLabel: String,
    val uvPeakWindow: String,
    val sunriseTime: String,
    val sunsetTime: String,
    val daylightDuration: String,
    val solarProgress: Float,
    val hourlyForecast: List<HourlyForecastItem>,
    val dailyForecast: List<DailyForecastItem>,
    val next24hForecast: List<HourlyForecastItem> = emptyList(),
    val isLiveSynced: Boolean = false,
    val dataSourceLabel: String = "BMKG (api.bmkg.go.id)",
)

data class DistrictAqiItem(
    val name: String,
    val description: String,
    val aqi: Int,
    val statusLabel: String,
    val severity: AqiSeverity,
)

data class RegionalAqiSector(
    val id: String,
    val name: String,
    val directionLabel: String,
    val gridX: Float,
    val gridY: Float,
    val latitude: Double,
    val longitude: Double,
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val no2Ppb: Double,
    val statusLabel: String,
    val severity: AqiSeverity,
)

enum class AqiSeverity {
    EXCELLENT,
    GOOD,
    MODERATE,
    UNHEALTHY,
}

data class AirQualityTelemetry(
    val regionalAqi: Int,
    val regionalCategory: String,
    val summaryDescription: String,
    val homeCardAqi: Int,
    val homeCardPm25: Double,
    val pm25: Double,
    val pm10: Double,
    val coPpb: Int,
    val no2Ppb: Double,
    val activeSensorsCount: Int,
    val districts: List<DistrictAqiItem>,
    val regionalGrid: List<RegionalAqiSector> = emptyList(),
    val dataSourceLabel: String = "BMKG & Copernicus Regional AQI",
)

data class SeismicEventItem(
    val location: String,
    val depthKm: Int,
    val timeAgoLabel: String,
    val magnitude: Double,
    val shindoLabel: String,
    val severity: SeismicSeverity,
    val latitude: Double = -6.84,
    val longitude: Double = 107.05,
    val shakemapUrl: String? = null,
)

data class WeatherMapStation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val tempC: Int,
    val humidityPercent: Int,
    val windSpeedKmh: Int,
    val windBearingDeg: Int,
    val precipChancePercent: Int,
    val precipMm: Double = 0.0,
    val cloudCoverPercent: Int = 50,
    val conditionLabel: String,
    val conditionKind: WeatherConditionKind,
    val isBmkg: Boolean = true,
)

enum class SeismicSeverity { HIGH, MODERATE, LOW }

data class SeismicTelemetry(
    val bannerTitle: String,
    val bannerMagnitude: String,
    val bannerSubtitle: String,
    val warningHeader: String,
    val shindoBadge: String,
    val activeEventTitle: String,
    val activeEventDescription: String,
    val depthKm: Double,
    val peakPgaGal: Int,
    val tsunamiStatus: String,
    val pWaveLeadSec: Double,
    val sWaveJma: Double,
    val tsunamiSurgeMeters: Double,
    val volcanicLevel: String,
    val volcanicNote: String,
    val volcanicTiltUrad: Double,
    val feltRadiusLabel: String,
    val epicenterLat: Double = -6.84,
    val epicenterLon: Double = 107.05,
    val shakemapUrl: String? = null,
    val recentEvents: List<SeismicEventItem>,
    val dataSourceLabel: String = "BMKG TEWS (data.bmkg.go.id)",
)

data class RadarTelemetry(
    val echoCoreDbz: Int,
    val intensityBadge: String,
    val summaryDescription: String,
    val cellSpeedKmh: Int,
    val echoTopMeters: Int,
    val accumulationMmH: Int,
    val surfaceRainMmH: Double,
    val next60mMm: Double,
    val cellVectorLabel: String,
    val cellDestination: String,
    val hailRiskLabel: String,
    val irCloudTopC: Int,
    val satelliteName: String,
    val typhoonTitle: String,
    val typhoonMeta: String,
    val spotTemperaturesC: List<Pair<String, String>>,
)

data class UserSettings(
    val selectedCityId: String = "jakarta",
    val seismicPWaveAlert: Boolean = true,
    val typhoonFlashWatch: Boolean = true,
    val aqiSmogInversion: Boolean = true,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val windUnit: WindUnit = WindUnit.KMH,
    val pressureUnit: PressureUnit = PressureUnit.HPA,
    val gyroParallaxEnabled: Boolean = true,
    val gpsPrivacyMode: Boolean = true,
    val offlineCacheLabel: String = "Cached · Nov 2026",
)


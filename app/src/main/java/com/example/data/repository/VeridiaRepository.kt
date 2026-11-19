package com.example.data.repository

import android.content.Context
import com.example.data.local.CachedSeismicEntity
import com.example.data.local.CachedWeatherEntity
import com.example.data.local.TelemetryCacheDao
import com.example.data.local.VeridiaDatabase
import com.example.data.location.LocationMaskingUtility
import com.example.data.model.AirQualityTelemetry
import com.example.data.model.AqiSeverity
import com.example.data.model.CityLocation
import com.example.data.model.DailyForecastItem
import com.example.data.model.DistrictAqiItem
import com.example.data.model.HourlyForecastItem
import com.example.data.model.RadarTelemetry
import com.example.data.model.RegionalAqiSector
import com.example.data.model.SeismicEventItem
import com.example.data.model.SeismicSeverity
import com.example.data.model.SeismicTelemetry
import com.example.data.model.WeatherConditionKind
import com.example.data.model.WeatherMapStation
import com.example.data.model.WeatherTelemetry
import com.example.data.network.BmkgEarthquakeApi
import com.example.data.network.BmkgForecastApi
import com.example.data.network.BmkgGempaDto
import com.example.data.network.OpenMeteoAirQualityApi
import com.example.data.network.OpenMeteoForecastApi
import com.example.data.network.OpenMeteoGeocodingApi
import com.example.data.network.RainViewerApi
import com.example.data.network.UsgsEarthquakeApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class CachedWeatherSnapshot(
    val cityId: String,
    val weather: WeatherTelemetry,
    val airQuality: AirQualityTelemetry?,
    val stations: List<WeatherMapStation>?,
    val fetchedAtMillis: Long,
)

data class CachedSeismicSnapshot(
    val seismic: SeismicTelemetry,
    val fetchedAtMillis: Long,
)

data class TelemetryFetchOutcome<T>(
    val data: T,
    val fromCache: Boolean,
    val fetchedAtMillis: Long,
)

class VeridiaRepository(
    private val cacheDao: TelemetryCacheDao? = null,
) {

    companion object {
        fun create(context: Context): VeridiaRepository {
            val db = VeridiaDatabase.getInstance(context)
            return VeridiaRepository(cacheDao = db.telemetryCacheDao())
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(7, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val weatherAdapter by lazy { moshi.adapter(WeatherTelemetry::class.java) }
    private val airQualityAdapter by lazy { moshi.adapter(AirQualityTelemetry::class.java) }
    private val seismicAdapter by lazy { moshi.adapter(SeismicTelemetry::class.java) }
    private val stationsAdapter by lazy {
        val listType = Types.newParameterizedType(List::class.java, WeatherMapStation::class.java)
        moshi.adapter<List<WeatherMapStation>>(listType)
    }

    private fun <T> createApi(baseUrl: String, serviceClass: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(serviceClass)
    }

    private val forecastApi: OpenMeteoForecastApi by lazy {
        createApi("https://api.open-meteo.com/", OpenMeteoForecastApi::class.java)
    }

    private val airQualityApi: OpenMeteoAirQualityApi by lazy {
        createApi("https://air-quality-api.open-meteo.com/", OpenMeteoAirQualityApi::class.java)
    }

    private val geocodingApi: OpenMeteoGeocodingApi by lazy {
        createApi("https://geocoding-api.open-meteo.com/", OpenMeteoGeocodingApi::class.java)
    }

    private val usgsApi: UsgsEarthquakeApi by lazy {
        createApi("https://earthquake.usgs.gov/", UsgsEarthquakeApi::class.java)
    }

    private val bmkgForecastApi: BmkgForecastApi by lazy {
        createApi("https://api.bmkg.go.id/", BmkgForecastApi::class.java)
    }

    private val bmkgEarthquakeApi: BmkgEarthquakeApi by lazy {
        createApi("https://data.bmkg.go.id/", BmkgEarthquakeApi::class.java)
    }

    private val rainViewerApi: RainViewerApi by lazy {
        createApi("https://api.rainviewer.com/", RainViewerApi::class.java)
    }

    private data class BmkgStationSeed(
        val adm4: String,
        val name: String,
        val lat: Double,
        val lon: Double,
    )

    private val bmkgStations = listOf(
        BmkgStationSeed("31.71.03.1001", "Jakarta", -6.1754, 106.8272),
        BmkgStationSeed("32.73.01.1001", "Bandung", -6.9175, 107.6191),
        BmkgStationSeed("33.74.01.1001", "Semarang", -6.9667, 110.4167),
        BmkgStationSeed("34.71.01.1001", "Yogyakarta", -7.7956, 110.3695),
        BmkgStationSeed("35.78.01.1001", "Surabaya", -7.2575, 112.7521),
        BmkgStationSeed("51.71.01.1001", "Denpasar", -8.6705, 115.2126),
        BmkgStationSeed("12.71.01.1001", "Medan", 3.5952, 98.6722),
        BmkgStationSeed("16.71.01.1001", "Palembang", -2.9761, 104.7754),
        BmkgStationSeed("73.71.01.1001", "Makassar", -5.1477, 119.4327),
        BmkgStationSeed("64.71.01.1001", "Balikpapan", -1.2379, 116.8529),
        BmkgStationSeed("71.71.01.1001", "Manado", 1.4748, 124.8421),
        BmkgStationSeed("91.71.01.1001", "Jayapura", -2.5916, 140.6690),
    )

    val presetCities: List<CityLocation> = listOf(
        CityLocation(
            id = "jakarta",
            name = "Jakarta",
            country = "Indonesia",
            latitude = -6.1754,
            longitude = 106.8272,
            summaryCondition = "Cloudy & Rain",
            summaryTempC = 29,
            isCurrent = true,
            bmkgAdm4Code = "31.71.03.1001",
        ),
        CityLocation(
            id = "bandung",
            name = "Bandung",
            country = "Indonesia",
            latitude = -6.9175,
            longitude = 107.6191,
            summaryCondition = "Rain",
            summaryTempC = 23,
            bmkgAdm4Code = "32.73.01.1001",
        ),
        CityLocation(
            id = "surabaya",
            name = "Surabaya",
            country = "Indonesia",
            latitude = -7.2575,
            longitude = 112.7521,
            summaryCondition = "Partly Cloudy",
            summaryTempC = 31,
            bmkgAdm4Code = "35.78.01.1001",
        ),
        CityLocation(
            id = "yogyakarta",
            name = "Yogyakarta",
            country = "Indonesia",
            latitude = -7.7956,
            longitude = 110.3695,
            summaryCondition = "Light Rain",
            summaryTempC = 27,
            bmkgAdm4Code = "34.71.01.1001",
        ),
        CityLocation(
            id = "denpasar",
            name = "Denpasar",
            country = "Indonesia",
            latitude = -8.6705,
            longitude = 115.2126,
            summaryCondition = "Clear",
            summaryTempC = 30,
            bmkgAdm4Code = "51.71.01.1001",
        ),
        CityLocation(
            id = "tokyo",
            name = "Tokyo",
            country = "Japan",
            latitude = 35.6762,
            longitude = 139.6503,
            summaryCondition = "Rain",
            summaryTempC = 21,
        ),
        CityLocation(
            id = "kyoto",
            name = "Kyoto",
            country = "Japan",
            latitude = 35.0116,
            longitude = 135.7681,
            summaryCondition = "Clear",
            summaryTempC = 22,
        ),
        CityLocation(
            id = "sapporo",
            name = "Sapporo",
            country = "Japan",
            latitude = 43.0618,
            longitude = 141.3545,
            summaryCondition = "Light Rain",
            summaryTempC = 14,
        ),
    )

    private fun isWithinIndonesia(lat: Double, lon: Double): Boolean {
        return lat in -11.5..6.5 && lon in 94.0..142.0
    }

    private fun resolveAdm4ForCity(city: CityLocation): String? {
        if (!city.bmkgAdm4Code.isNullOrBlank()) return city.bmkgAdm4Code
        if (!isWithinIndonesia(city.latitude, city.longitude) &&
            !city.country.contains("Indonesia", ignoreCase = true)
        ) {
            return null
        }
        return bmkgStations.minByOrNull { station ->
            (station.lat - city.latitude).pow(2) + (station.lon - city.longitude).pow(2)
        }?.adm4
    }

    fun defaultWeatherFor(city: CityLocation): WeatherTelemetry {
        val baseTemp = city.summaryTempC
        val conditionKind = when {
            city.summaryCondition.contains("Clear", ignoreCase = true) -> WeatherConditionKind.SUNNY
            city.summaryCondition.contains("Fog", ignoreCase = true) -> WeatherConditionKind.FOG
            city.summaryCondition.contains("Rain", ignoreCase = true) -> WeatherConditionKind.RAIN
            city.summaryCondition.contains("Cloud", ignoreCase = true) -> WeatherConditionKind.CLOUDY
            else -> WeatherConditionKind.PARTLY_CLOUDY
        }
        val isBmkg = resolveAdm4ForCity(city) != null
        val defaultTrend = listOf(
            HourlyForecastItem("Now", baseTemp, conditionKind, isCurrent = true, precipPercent = if (conditionKind == WeatherConditionKind.RAIN) 78 else 22, precipMm = if (conditionKind == WeatherConditionKind.RAIN) 3.4 else 0.2),
            HourlyForecastItem("+3h", baseTemp - 1, conditionKind, precipPercent = if (conditionKind == WeatherConditionKind.RAIN) 84 else 30, precipMm = if (conditionKind == WeatherConditionKind.RAIN) 4.8 else 0.4),
            HourlyForecastItem("+6h", baseTemp - 2, WeatherConditionKind.CLOUDY, precipPercent = 65, precipMm = 2.1),
            HourlyForecastItem("+9h", baseTemp - 3, WeatherConditionKind.CLOUDY, precipPercent = 45, precipMm = 0.8),
            HourlyForecastItem("+12h", baseTemp - 4, WeatherConditionKind.FOG, isSunset = true, precipPercent = 32, precipMm = 0.3),
            HourlyForecastItem("+15h", baseTemp - 4, WeatherConditionKind.PARTLY_CLOUDY, precipPercent = 24, precipMm = 0.1),
            HourlyForecastItem("+18h", baseTemp - 2, WeatherConditionKind.PARTLY_CLOUDY, precipPercent = 28, precipMm = 0.2),
            HourlyForecastItem("+21h", baseTemp, conditionKind, precipPercent = 52, precipMm = 1.6),
            HourlyForecastItem("+24h", baseTemp + 1, conditionKind, precipPercent = 68, precipMm = 2.9),
        )
        return WeatherTelemetry(
            city = city,
            currentTempC = baseTemp,
            feelsLikeC = baseTemp + 1,
            conditionLabel = city.summaryCondition,
            conditionKind = conditionKind,
            precipChancePercent = if (conditionKind == WeatherConditionKind.RAIN) 78 else 20,
            precipStatusLabel = if (conditionKind == WeatherConditionKind.RAIN) "78% Precip · Steady" else "20% Precip · Mild",
            windSpeedKmh = if (isBmkg) 16 else 18,
            windGustKmh = 34,
            windBearingDeg = 245,
            windDirectionLabel = "Southwest",
            pressureHpa = 1011,
            pressureTrendLabel = "Steady Barometer",
            humidityPercent = if (isBmkg) 78 else 68,
            dewPointC = (baseTemp - 4).toDouble(),
            uvIndex = 6.8,
            uvLevelLabel = "High",
            uvPeakWindow = "Peak solar window 11:00 - 14:00",
            sunriseTime = "05:38",
            sunsetTime = "17:52",
            daylightDuration = "12h 14m Daylight",
            solarProgress = 0.62f,
            hourlyForecast = defaultTrend.take(5),
            dailyForecast = listOf(
                DailyForecastItem("Today", 75, conditionKind, baseTemp - 4, baseTemp + 1),
                DailyForecastItem("Thu", 40, WeatherConditionKind.PARTLY_CLOUDY, baseTemp - 4, baseTemp + 2),
                DailyForecastItem("Fri", 70, WeatherConditionKind.RAIN, baseTemp - 5, baseTemp),
                DailyForecastItem("Sat", 25, WeatherConditionKind.CLOUDY, baseTemp - 4, baseTemp + 2),
                DailyForecastItem("Sun", 60, WeatherConditionKind.THUNDERSTORM, baseTemp - 3, baseTemp + 1),
                DailyForecastItem("Mon", 15, WeatherConditionKind.SUNNY, baseTemp - 4, baseTemp + 3),
                DailyForecastItem("Tue", 20, WeatherConditionKind.PARTLY_CLOUDY, baseTemp - 4, baseTemp + 2),
            ),
            next24hForecast = defaultTrend,
            isLiveSynced = false,
            dataSourceLabel = if (isBmkg) "BMKG (api.bmkg.go.id)" else "Open-Meteo Live",
        )
    }

    fun defaultAirQuality(city: CityLocation = presetCities.first()): AirQualityTelemetry {
        val baseAqi = if (city.id == "jakarta") 86 else 48
        val grid = buildFallbackRegionalGrid(
            city = city,
            centerAqi = baseAqi,
            centerPm25 = if (city.id == "jakarta") 28.5 else 11.4,
            centerPm10 = if (city.id == "jakarta") 44.2 else 19.6,
            centerNo2 = if (city.id == "jakarta") 34.0 else 18.2,
        )
        return AirQualityTelemetry(
            regionalAqi = baseAqi,
            regionalCategory = categoryForAqi(baseAqi),
            summaryDescription = "Regional aerosol dispersion & particulate gradient mapped across ${city.name} sectors.",
            homeCardAqi = baseAqi,
            homeCardPm25 = if (city.id == "jakarta") 28.5 else 11.4,
            pm25 = if (city.id == "jakarta") 28.5 else 11.4,
            pm10 = if (city.id == "jakarta") 44.2 else 19.6,
            coPpb = 410,
            no2Ppb = if (city.id == "jakarta") 34.0 else 18.2,
            activeSensorsCount = grid.size,
            districts = buildDistrictSummaries(city.name, grid),
            regionalGrid = grid,
            dataSourceLabel = "BMKG & Copernicus Regional AQI",
        )
    }

    fun defaultSeismicTelemetry(): SeismicTelemetry = SeismicTelemetry(
        bannerTitle = "BMKG Gempa Terkini",
        bannerMagnitude = "M 5.1",
        bannerSubtitle = "14 km Tenggara Cianjur · BMKG TEWS",
        warningHeader = "BMKG Early Warning",
        shindoBadge = "MMI III-IV",
        activeEventTitle = "M 5.1 Gempa Terkini",
        activeEventDescription = "Pusat gempa berada di darat 14 km Tenggara Kab. Cianjur · Tidak berpotensi tsunami",
        depthKm = 10.0,
        peakPgaGal = 142,
        tsunamiStatus = "Tidak Berpotensi",
        pWaveLeadSec = 4.2,
        sWaveJma = 3.8,
        tsunamiSurgeMeters = 0.0,
        volcanicLevel = "Waspada",
        volcanicNote = "Aktivitas tektonik aktif · BMKG",
        volcanicTiltUrad = 0.02,
        feltRadiusLabel = "Radius: 65km Felt",
        epicenterLat = -6.84,
        epicenterLon = 107.05,
        shakemapUrl = null,
        recentEvents = listOf(
            SeismicEventItem("14 km Tenggara Cianjur", 10, "18 min ago", 5.1, "MMI III-IV", SeismicSeverity.HIGH, -6.84, 107.05),
            SeismicEventItem("78 km Barat Daya Garut", 24, "4 hours ago", 4.3, "MMI II-III", SeismicSeverity.MODERATE, -7.68, 107.42),
            SeismicEventItem("95 km Timur Laut Halmahera", 32, "Yesterday", 4.8, "MMI II", SeismicSeverity.LOW, 1.48, 128.45),
        ),
        dataSourceLabel = "BMKG TEWS (data.bmkg.go.id)",
    )

    fun defaultRadarTelemetry(): RadarTelemetry = RadarTelemetry(
        echoCoreDbz = 32,
        intensityBadge = "Heavy",
        summaryDescription = "Convective band moving northeast at 26 km/h. Peak intensity expected in 35 min.",
        cellSpeedKmh = 26,
        echoTopMeters = 7400,
        accumulationMmH = 14,
        surfaceRainMmH = 14.2,
        next60mMm = 8.5,
        cellVectorLabel = "NE 055°",
        cellDestination = "Tracking across metro basin",
        hailRiskLabel = "< 5%",
        irCloudTopC = -48,
        satelliteName = "Himawari-9",
        typhoonTitle = "Monsoon Cell",
        typhoonMeta = "58 km/h · WSW",
        spotTemperaturesC = listOf(
            "Center" to "29°C",
            "North" to "28°C",
            "Highland" to "23°C",
            "SST" to "28.4°",
        ),
    )

    fun observeLatestCachedWeather(): Flow<CachedWeatherSnapshot?> {
        val dao = cacheDao ?: return flowOf(null)
        return dao.observeLatestWeather().map { entity -> entity?.let(::decodeWeatherEntity) }
    }

    fun observeCachedSeismic(): Flow<CachedSeismicSnapshot?> {
        val dao = cacheDao ?: return flowOf(null)
        return dao.observeLatestSeismic().map { entity -> entity?.let(::decodeSeismicEntity) }
    }

    suspend fun getCachedWeatherSnapshot(cityId: String): CachedWeatherSnapshot? = withContext(Dispatchers.IO) {
        val dao = cacheDao ?: return@withContext null
        val entity = dao.getWeatherForCity(cityId) ?: dao.getLatestWeather() ?: return@withContext null
        decodeWeatherEntity(entity)
    }

    suspend fun getCachedSeismicSnapshot(): CachedSeismicSnapshot? = withContext(Dispatchers.IO) {
        val dao = cacheDao ?: return@withContext null
        val entity = dao.getLatestSeismic() ?: return@withContext null
        decodeSeismicEntity(entity)
    }

    suspend fun saveWeatherBundleToCache(
        cityId: String,
        weather: WeatherTelemetry,
        airQuality: AirQualityTelemetry? = null,
        stations: List<WeatherMapStation>? = null,
        fetchedAtMillis: Long = System.currentTimeMillis(),
    ) = withContext(Dispatchers.IO) {
        val dao = cacheDao ?: return@withContext
        val existing = dao.getWeatherForCity(cityId)
        val masked = LocationMaskingUtility.maskCoordinates(
            latitude = weather.city.latitude,
            longitude = weather.city.longitude,
            enabled = true,
        )
        val weatherJson = runCatching { weatherAdapter.toJson(weather) }.getOrNull() ?: return@withContext
        val aqiJson = airQuality?.let { runCatching { airQualityAdapter.toJson(it) }.getOrNull() }
            ?: existing?.airQualityJson
        val stationsJson = stations?.takeIf { it.isNotEmpty() }
            ?.let { runCatching { stationsAdapter.toJson(it) }.getOrNull() }
            ?: existing?.stationsJson

        dao.upsertWeather(
            CachedWeatherEntity(
                cityId = cityId,
                cityName = weather.city.name,
                maskedLat = masked.latitude,
                maskedLon = masked.longitude,
                currentTempC = weather.currentTempC,
                conditionLabel = weather.conditionLabel,
                dataSourceLabel = weather.dataSourceLabel,
                weatherJson = weatherJson,
                airQualityJson = aqiJson,
                stationsJson = stationsJson,
                fetchedAtMillis = fetchedAtMillis,
            )
        )
    }

    suspend fun saveSeismicToCache(
        seismic: SeismicTelemetry,
        fetchedAtMillis: Long = System.currentTimeMillis(),
    ) = withContext(Dispatchers.IO) {
        val dao = cacheDao ?: return@withContext
        val seismicJson = runCatching { seismicAdapter.toJson(seismic) }.getOrNull() ?: return@withContext
        dao.upsertSeismic(
            CachedSeismicEntity(
                cacheKey = CachedSeismicEntity.LATEST_KEY,
                bannerMagnitude = seismic.bannerMagnitude,
                activeEventTitle = seismic.activeEventTitle,
                tsunamiStatus = seismic.tsunamiStatus,
                epicenterLat = seismic.epicenterLat,
                epicenterLon = seismic.epicenterLon,
                seismicJson = seismicJson,
                fetchedAtMillis = fetchedAtMillis,
            )
        )
    }

    private fun decodeWeatherEntity(entity: CachedWeatherEntity): CachedWeatherSnapshot? {
        val weather = runCatching { weatherAdapter.fromJson(entity.weatherJson) }.getOrNull() ?: return null
        val aqi = entity.airQualityJson?.let { runCatching { airQualityAdapter.fromJson(it) }.getOrNull() }
        val stations = entity.stationsJson?.let { runCatching { stationsAdapter.fromJson(it) }.getOrNull() }
        return CachedWeatherSnapshot(
            cityId = entity.cityId,
            weather = weather.copy(isLiveSynced = false),
            airQuality = aqi,
            stations = stations,
            fetchedAtMillis = entity.fetchedAtMillis,
        )
    }

    private fun decodeSeismicEntity(entity: CachedSeismicEntity): CachedSeismicSnapshot? {
        val seismic = runCatching { seismicAdapter.fromJson(entity.seismicJson) }.getOrNull() ?: return null
        return CachedSeismicSnapshot(
            seismic = seismic,
            fetchedAtMillis = entity.fetchedAtMillis,
        )
    }

    suspend fun fetchLiveWeatherWithCache(
        city: CityLocation,
        maskCoordinates: Boolean = true,
    ): Result<TelemetryFetchOutcome<WeatherTelemetry>> {
        val liveResult = fetchLiveWeather(city = city, maskCoordinates = maskCoordinates)
        val liveWeather = liveResult.getOrNull()
        if (liveWeather != null) {
            val now = System.currentTimeMillis()
            saveWeatherBundleToCache(
                cityId = city.id,
                weather = liveWeather,
                fetchedAtMillis = now,
            )
            return Result.success(
                TelemetryFetchOutcome(
                    data = liveWeather,
                    fromCache = false,
                    fetchedAtMillis = now,
                )
            )
        }
        val cached = getCachedWeatherSnapshot(city.id)
        if (cached != null) {
            return Result.success(
                TelemetryFetchOutcome(
                    data = cached.weather.copy(isLiveSynced = false),
                    fromCache = true,
                    fetchedAtMillis = cached.fetchedAtMillis,
                )
            )
        }
        return Result.failure(liveResult.exceptionOrNull() ?: IllegalStateException("Weather unavailable offline"))
    }

    suspend fun fetchRecentEarthquakesWithCache(): Result<TelemetryFetchOutcome<SeismicTelemetry>> {
        val liveResult = fetchRecentEarthquakes()
        val liveSeismic = liveResult.getOrNull()
        if (liveSeismic != null) {
            val now = System.currentTimeMillis()
            saveSeismicToCache(liveSeismic, fetchedAtMillis = now)
            return Result.success(
                TelemetryFetchOutcome(
                    data = liveSeismic,
                    fromCache = false,
                    fetchedAtMillis = now,
                )
            )
        }
        val cached = getCachedSeismicSnapshot()
        if (cached != null) {
            return Result.success(
                TelemetryFetchOutcome(
                    data = cached.seismic,
                    fromCache = true,
                    fetchedAtMillis = cached.fetchedAtMillis,
                )
            )
        }
        return Result.failure(liveResult.exceptionOrNull() ?: IllegalStateException("Seismic telemetry unavailable offline"))
    }

    suspend fun fetchLiveWeather(
        city: CityLocation,
        maskCoordinates: Boolean = true,
    ): Result<WeatherTelemetry> = withContext(Dispatchers.IO) {
        val apiCity = LocationMaskingUtility.maskCityForApi(city, enabled = maskCoordinates)
        val adm4 = resolveAdm4ForCity(apiCity)
        val openMeteoDeferred = async { runCatching { forecastApi.getForecast(apiCity.latitude, apiCity.longitude) } }
        val bmkgDeferred = if (adm4 != null) {
            async { runCatching { bmkgForecastApi.getForecast(adm4) } }
        } else {
            null
        }

        val openMeteoResult = openMeteoDeferred.await()
        val bmkgResult = bmkgDeferred?.await()

        val bmkgResponse = bmkgResult?.getOrNull()
        val bmkgSlots = bmkgResponse?.data?.firstOrNull()?.cuaca?.flatten().orEmpty()
        val omResponse = openMeteoResult.getOrNull()

        if (bmkgSlots.isNotEmpty()) {
            val currentSlot = bmkgSlots.first()
            val tempC = (currentSlot.t ?: city.summaryTempC.toDouble()).roundToInt()
            val humidity = (currentSlot.hu ?: 75.0).roundToInt()
            val windSpeed = (currentSlot.ws ?: 12.0).roundToInt()
            val windBearing = (currentSlot.wdDeg ?: 240.0).roundToInt()
            val windDirLabel = currentSlot.wd?.takeIf { it.isNotBlank() }?.let(::expandBmkgDirection)
                ?: bearingToLabel(windBearing)
            val bmkgCode = currentSlot.weather ?: 3
            val kind = mapBmkgCodeToKind(bmkgCode, currentSlot.weatherDescEn ?: currentSlot.weatherDesc)
            val conditionLabel = currentSlot.weatherDescEn?.takeIf { it.isNotBlank() }
                ?: currentSlot.weatherDesc?.takeIf { it.isNotBlank() }
                ?: mapWmoCodeToLabel(bmkgCode)
            val precipMm = currentSlot.tp ?: 0.0
            val cloudCover = (currentSlot.tcc ?: 50.0).roundToInt()
            val precipProb = when {
                kind == WeatherConditionKind.THUNDERSTORM -> 90
                kind == WeatherConditionKind.RAIN -> maxOf(65, (precipMm * 15).roundToInt().coerceAtMost(95))
                else -> (cloudCover * 0.35f).roundToInt().coerceIn(5, 45)
            }

            val trend24hList = bmkgSlots.take(9).mapIndexed { index, slot ->
                val rawTime = slot.localDatetime ?: slot.datetime ?: ""
                val hhmm = rawTime.substringAfter(" ", rawTime.substringAfter("T", "12:00")).take(5)
                val slotKind = mapBmkgCodeToKind(slot.weather ?: bmkgCode, slot.weatherDescEn ?: slot.weatherDesc)
                val slotPrecipMm = ((slot.tp ?: 0.0) * 10.0).roundToInt() / 10.0
                val slotCloud = (slot.tcc ?: 50.0).roundToInt().coerceIn(0, 100)
                val slotPrecipPct = when {
                    slotKind == WeatherConditionKind.THUNDERSTORM -> maxOf(80, (slotPrecipMm * 14).roundToInt().coerceAtMost(98))
                    slotKind == WeatherConditionKind.RAIN -> maxOf(60, (slotPrecipMm * 16).roundToInt().coerceAtMost(95))
                    slotPrecipMm >= 0.2 -> maxOf(45, (slotPrecipMm * 20).roundToInt().coerceAtMost(85))
                    else -> (slotCloud * 0.38).roundToInt().coerceIn(5, 45)
                }
                HourlyForecastItem(
                    timeLabel = if (index == 0) "Now" else hhmm,
                    tempC = (slot.t ?: tempC.toDouble()).roundToInt(),
                    condition = slotKind,
                    isCurrent = index == 0,
                    isSunset = hhmm.startsWith("17:") || hhmm.startsWith("18:"),
                    precipPercent = slotPrecipPct,
                    precipMm = slotPrecipMm,
                )
            }
            val hourlyList = trend24hList.take(6)

            val omCurrent = omResponse?.current
            val pressureHpa = (omCurrent?.surfacePressure ?: 1011.0).roundToInt()
            val windGust = (omCurrent?.windGusts10m ?: (windSpeed + 12.0)).roundToInt()
            val feelsC = (omCurrent?.apparentTemperature ?: (tempC + if (humidity > 75) 2 else 0).toDouble()).roundToInt()
            val dew = omResponse?.hourly?.dewPoint2m?.firstOrNull()
                ?: (tempC - ((100 - humidity) / 5.0))
            val uvMax = omResponse?.daily?.uvIndexMax?.firstOrNull() ?: 7.0
            val sunrise = omResponse?.daily?.sunrise?.firstOrNull()?.substringAfter("T")?.take(5) ?: "05:38"
            val sunset = omResponse?.daily?.sunset?.firstOrNull()?.substringAfter("T")?.take(5) ?: "17:52"
            val dailyList = parseOpenMeteoDaily(omResponse, tempC, kind)

            val resolvedLocationName = bmkgResponse?.lokasi?.kotkab
                ?.replace("Kota ", "", ignoreCase = true)
                ?.replace("Kab. ", "", ignoreCase = true)
                ?.takeIf { it.isNotBlank() && city.id == "gps_current" }
                ?: city.name

            return@withContext Result.success(
                WeatherTelemetry(
                    city = apiCity.copy(
                        name = resolvedLocationName,
                        summaryCondition = conditionLabel,
                        summaryTempC = tempC,
                        bmkgAdm4Code = adm4,
                    ),
                    currentTempC = tempC,
                    feelsLikeC = feelsC,
                    conditionLabel = conditionLabel,
                    conditionKind = kind,
                    precipChancePercent = precipProb,
                    precipStatusLabel = "$precipProb% Precip · ${currentSlot.weatherDesc ?: conditionLabel}",
                    windSpeedKmh = windSpeed,
                    windGustKmh = windGust,
                    windBearingDeg = windBearing,
                    windDirectionLabel = windDirLabel,
                    pressureHpa = pressureHpa,
                    pressureTrendLabel = if (pressureHpa >= 1012) "Steady Barometer" else "Low Tropical Trough",
                    humidityPercent = humidity,
                    dewPointC = dew,
                    uvIndex = uvMax,
                    uvLevelLabel = uvLabelFor(uvMax),
                    uvPeakWindow = "Peak solar window 11:00 - 14:00",
                    sunriseTime = sunrise,
                    sunsetTime = sunset,
                    daylightDuration = "12h 14m Daylight",
                    solarProgress = 0.62f,
                    hourlyForecast = if (hourlyList.isNotEmpty()) hourlyList else defaultWeatherFor(city).hourlyForecast,
                    dailyForecast = if (dailyList.isNotEmpty()) dailyList else defaultWeatherFor(city).dailyForecast,
                    next24hForecast = if (trend24hList.isNotEmpty()) trend24hList else defaultWeatherFor(city).next24hForecast,
                    isLiveSynced = true,
                    dataSourceLabel = "BMKG (api.bmkg.go.id)",
                )
            )
        }

        // Open-Meteo primary fallback (global / GPS outside Indonesia)
        runCatching {
            val response = omResponse ?: error("Missing live weather response")
            val current = response.current ?: error("Missing current weather")
            val tempC = (current.temperature2m ?: city.summaryTempC.toDouble()).roundToInt()
            val feelsC = (current.apparentTemperature ?: tempC.toDouble()).roundToInt()
            val code = current.weatherCode ?: 61
            val kind = mapWmoCodeToKind(code)
            val conditionLabel = mapWmoCodeToLabel(code)
            val windBearing = current.windDirection10m ?: 315
            val windDirLabel = bearingToLabel(windBearing)
            val pressureHpa = (current.surfacePressure ?: 1014.0).roundToInt()
            val humidity = current.relativeHumidity2m ?: 68
            val windSpeed = (current.windSpeed10m ?: 18.0).roundToInt()
            val windGust = (current.windGusts10m ?: (windSpeed + 12.0)).roundToInt()

            val trend24hList = mutableListOf<HourlyForecastItem>()
            val hourlyTimes = response.hourly?.time.orEmpty()
            val hourlyTemps = response.hourly?.temperature2m.orEmpty()
            val hourlyCodes = response.hourly?.weatherCode.orEmpty()
            val hourlyPrecip = response.hourly?.precipitationProbability.orEmpty()
            val hourlyPrecipMm = response.hourly?.precipitation.orEmpty()
            val hourlyUv = response.hourly?.uvIndex.orEmpty()
            val hourlyDew = response.hourly?.dewPoint2m.orEmpty()

            for (i in 0 until minOf(25, hourlyTimes.size)) {
                val timeStr = hourlyTimes[i].substringAfter("T", "15:00").take(5)
                val hTemp = hourlyTemps.getOrNull(i)?.roundToInt() ?: tempC
                val hKind = mapWmoCodeToKind(hourlyCodes.getOrNull(i) ?: code)
                val hPrecipPct = hourlyPrecip.getOrNull(i) ?: if (hKind == WeatherConditionKind.RAIN) 75 else 15
                val hPrecipMm = ((hourlyPrecipMm.getOrNull(i) ?: 0.0) * 10.0).roundToInt() / 10.0
                trend24hList.add(
                    HourlyForecastItem(
                        timeLabel = if (i == 0) "Now" else timeStr,
                        tempC = hTemp,
                        condition = hKind,
                        isCurrent = i == 0,
                        isSunset = i == 4,
                        precipPercent = hPrecipPct.coerceIn(0, 100),
                        precipMm = hPrecipMm,
                    )
                )
            }
            val hourlyList = trend24hList.take(6)

            val dailyList = parseOpenMeteoDaily(response, tempC, kind)
            val precipProb = hourlyPrecip.firstOrNull() ?: if (kind == WeatherConditionKind.RAIN) 85 else 15
            val uvMax = response.daily?.uvIndexMax?.firstOrNull() ?: hourlyUv.firstOrNull() ?: 5.0
            val dew = hourlyDew.firstOrNull() ?: 14.0
            val sunrise = response.daily?.sunrise?.firstOrNull()?.substringAfter("T")?.take(5) ?: "05:38"
            val sunset = response.daily?.sunset?.firstOrNull()?.substringAfter("T")?.take(5) ?: "18:42"

            WeatherTelemetry(
                city = apiCity.copy(summaryCondition = conditionLabel, summaryTempC = tempC),
                currentTempC = tempC,
                feelsLikeC = feelsC,
                conditionLabel = conditionLabel,
                conditionKind = kind,
                precipChancePercent = precipProb,
                precipStatusLabel = "$precipProb% Precip · ${if (precipProb >= 50) "Steady" else "Light"}",
                windSpeedKmh = windSpeed,
                windGustKmh = windGust,
                windBearingDeg = windBearing,
                windDirectionLabel = windDirLabel,
                pressureHpa = pressureHpa,
                pressureTrendLabel = if (pressureHpa >= 1013) "Steady Barometer" else "Falling Pressure",
                humidityPercent = humidity,
                dewPointC = dew,
                uvIndex = uvMax,
                uvLevelLabel = uvLabelFor(uvMax),
                uvPeakWindow = "Peak solar window 11:30 - 14:00",
                sunriseTime = sunrise,
                sunsetTime = sunset,
                daylightDuration = "12h 48m Daylight",
                solarProgress = 0.65f,
                hourlyForecast = if (hourlyList.isNotEmpty()) hourlyList else defaultWeatherFor(city).hourlyForecast,
                dailyForecast = if (dailyList.isNotEmpty()) dailyList else defaultWeatherFor(city).dailyForecast,
                next24hForecast = if (trend24hList.isNotEmpty()) trend24hList else defaultWeatherFor(city).next24hForecast,
                isLiveSynced = true,
                dataSourceLabel = if (adm4 != null) "BMKG Fallback · Open-Meteo Live" else "Open-Meteo Live",
            )
        }
    }

    suspend fun fetchLiveAirQuality(
        city: CityLocation,
        maskCoordinates: Boolean = true,
    ): Result<AirQualityTelemetry> = withContext(Dispatchers.IO) {
        runCatching {
            val apiCity = LocationMaskingUtility.maskCityForApi(city, enabled = maskCoordinates)
            val gridDefs = regionalGridOffsets(apiCity)
            val latCsv = gridDefs.joinToString(",") { String.format(Locale.US, "%.4f", it.lat) }
            val lonCsv = gridDefs.joinToString(",") { String.format(Locale.US, "%.4f", it.lon) }

            val gridResponses = runCatching {
                airQualityApi.getRegionalAirQualityGrid(latCsv, lonCsv)
            }.getOrNull()

            val sectors: List<RegionalAqiSector> = if (gridResponses != null && gridResponses.size == gridDefs.size) {
                gridDefs.mapIndexed { idx, def ->
                    val dto = gridResponses[idx].current
                    val aqi = dto?.usAqi ?: 42
                    val pm25 = dto?.pm2_5 ?: 10.0
                    val pm10 = dto?.pm10 ?: 18.0
                    val no2 = dto?.nitrogenDioxide ?: 18.0
                    RegionalAqiSector(
                        id = def.id,
                        name = "${city.name} ${def.label}",
                        directionLabel = def.label,
                        gridX = def.gridX,
                        gridY = def.gridY,
                        latitude = def.lat,
                        longitude = def.lon,
                        aqi = aqi,
                        pm25 = pm25,
                        pm10 = pm10,
                        no2Ppb = no2,
                        statusLabel = categoryForAqi(aqi),
                        severity = severityForAqi(aqi),
                    )
                }
            } else {
                val centerDto = airQualityApi.getAirQuality(apiCity.latitude, apiCity.longitude).current
                    ?: error("Missing AQI data")
                val cAqi = centerDto.usAqi ?: 42
                val cPm25 = centerDto.pm2_5 ?: 10.0
                val cPm10 = centerDto.pm10 ?: 18.0
                val cNo2 = centerDto.nitrogenDioxide ?: 18.0
                buildFallbackRegionalGrid(apiCity, cAqi, cPm25, cPm10, cNo2)
            }

            val centerSector = sectors.firstOrNull { it.gridX == 0f && it.gridY == 0f } ?: sectors.first()
            val peakAqi = sectors.maxOfOrNull { it.aqi } ?: centerSector.aqi
            val regionalAvgAqi = sectors.map { it.aqi }.average().roundToInt()
            val category = categoryForAqi(regionalAvgAqi)
            val centerSingle = gridResponses?.getOrNull(4)?.current

            AirQualityTelemetry(
                regionalAqi = regionalAvgAqi,
                regionalCategory = category,
                summaryDescription = when {
                    peakAqi >= 151 -> "Elevated particulate plume detected in ${sectors.maxByOrNull { it.aqi }?.name ?: city.name} (Peak AQI $peakAqi)."
                    regionalAvgAqi >= 51 -> "Moderate aerosol gradient across ${city.name} sectors · Peak AQI $peakAqi."
                    else -> "Clean regional airflow across ${city.name} · Average AQI $regionalAvgAqi."
                },
                homeCardAqi = centerSector.aqi,
                homeCardPm25 = centerSector.pm25,
                pm25 = centerSector.pm25,
                pm10 = centerSector.pm10,
                coPpb = (centerSingle?.carbonMonoxide ?: 410.0).roundToInt(),
                no2Ppb = centerSector.no2Ppb,
                activeSensorsCount = sectors.size,
                districts = buildDistrictSummaries(city.name, sectors),
                regionalGrid = sectors,
                dataSourceLabel = if (resolveAdm4ForCity(city) != null) {
                    "BMKG Region & Copernicus CAMS Grid"
                } else {
                    "Copernicus CAMS 9-Node Regional Grid"
                },
            )
        }
    }

    suspend fun fetchRecentEarthquakes(): Result<SeismicTelemetry> = withContext(Dispatchers.IO) {
        val autoDeferred = async { runCatching { bmkgEarthquakeApi.getLatestAutoGempa() } }
        val feltDeferred = async { runCatching { bmkgEarthquakeApi.getFeltEarthquakes() } }
        val m5Deferred = async { runCatching { bmkgEarthquakeApi.getRecentM5Earthquakes() } }

        val autoGempa = autoDeferred.await().getOrNull()?.infoGempa?.gempa
        val feltList = feltDeferred.await().getOrNull()?.infoGempa?.gempa.orEmpty()
        val m5List = m5Deferred.await().getOrNull()?.infoGempa?.gempa.orEmpty()

        val combinedBmkg = buildList {
            if (autoGempa != null) add(autoGempa)
            addAll(feltList)
            if (size < 3) addAll(m5List)
        }.distinctBy { "${it.tanggal}_${it.jam}_${it.wilayah}" }

        if (combinedBmkg.isNotEmpty()) {
            val items = combinedBmkg.take(5).map { gempa ->
                val mag = gempa.magnitude?.replace(",", ".")?.toDoubleOrNull() ?: 4.5
                val depthKm = gempa.kedalaman
                    ?.replace("km", "", ignoreCase = true)
                    ?.trim()
                    ?.toDoubleOrNull()
                    ?.roundToInt() ?: 10
                val region = gempa.wilayah ?: "Indonesia"
                val felt = gempa.dirasakan?.takeIf { it.isNotBlank() && it != "-" }
                val mmiLabel = felt?.split(",")?.firstOrNull()?.trim()?.take(16)
                    ?: when {
                        mag >= 5.5 -> "MMI IV"
                        mag >= 4.5 -> "MMI III"
                        else -> "MMI II"
                    }
                val timeLabel = listOfNotNull(
                    gempa.tanggal?.takeIf { it.isNotBlank() },
                    gempa.jam?.takeIf { it.isNotBlank() },
                ).joinToString(" · ").ifBlank { "Live BMKG" }
                val severity = when {
                    mag >= 5.0 -> SeismicSeverity.HIGH
                    mag >= 4.0 -> SeismicSeverity.MODERATE
                    else -> SeismicSeverity.LOW
                }
                val coordParts = gempa.coordinates?.split(",")?.mapNotNull { it.trim().toDoubleOrNull() }
                val lat = coordParts?.getOrNull(0) ?: -6.84
                val lon = coordParts?.getOrNull(1) ?: 107.05
                val eventShakemap = gempa.shakemap?.trim()?.takeIf { it.isNotBlank() }
                    ?.let { "https://data.bmkg.go.id/DataMKG/TEWS/$it" }
                SeismicEventItem(
                    location = region,
                    depthKm = depthKm,
                    timeAgoLabel = timeLabel,
                    magnitude = mag,
                    shindoLabel = mmiLabel,
                    severity = severity,
                    latitude = lat,
                    longitude = lon,
                    shakemapUrl = eventShakemap,
                )
            }
            val topDto: BmkgGempaDto = combinedBmkg.first()
            val topItem = items.first()
            val coordParts = topDto.coordinates?.split(",")?.mapNotNull { it.trim().toDoubleOrNull() }
            val parsedLat = coordParts?.getOrNull(0) ?: -6.84
            val parsedLon = coordParts?.getOrNull(1) ?: 107.05
            val shakemapFile = topDto.shakemap?.trim()?.takeIf { it.isNotBlank() }
            val shakemapUrl = shakemapFile?.let { "https://data.bmkg.go.id/DataMKG/TEWS/$it" }

            val potensi = topDto.potensi?.takeIf { it.isNotBlank() } ?: "Tidak berpotensi tsunami"
            val tsunamiStatus = when {
                potensi.contains("Tidak berpotensi", ignoreCase = true) -> "Tidak Berpotensi"
                potensi.contains("Tsunami", ignoreCase = true) -> "Waspada Tsunami"
                else -> "Monitoring"
            }
            val dirasakan = topDto.dirasakan?.takeIf { it.isNotBlank() && it != "-" }
            val description = buildString {
                append(topItem.location)
                append(" · Kedalaman ${topItem.depthKm} km · ")
                append(potensi)
                if (dirasakan != null) {
                    append(" · Dirasakan: $dirasakan")
                }
            }

            return@withContext Result.success(
                defaultSeismicTelemetry().copy(
                    bannerTitle = "BMKG Gempa Terkini",
                    bannerMagnitude = "M ${String.format(Locale.US, "%.1f", topItem.magnitude)}",
                    bannerSubtitle = "${topItem.location} · ${topItem.timeAgoLabel}",
                    warningHeader = "BMKG TEWS Live Alert",
                    shindoBadge = topItem.shindoLabel,
                    activeEventTitle = "M ${String.format(Locale.US, "%.1f", topItem.magnitude)} · BMKG Gempa",
                    activeEventDescription = description,
                    depthKm = topItem.depthKm.toDouble(),
                    tsunamiStatus = tsunamiStatus,
                    epicenterLat = parsedLat,
                    epicenterLon = parsedLon,
                    shakemapUrl = shakemapUrl,
                    recentEvents = items,
                    dataSourceLabel = "BMKG TEWS (data.bmkg.go.id)",
                )
            )
        }

        // USGS fallback if BMKG endpoint is temporarily unreachable
        runCatching {
            val response = usgsApi.getRecentEarthquakes()
            val features = response.features
            if (features.isEmpty()) return@runCatching defaultSeismicTelemetry()

            val now = System.currentTimeMillis()
            val items = features.take(5).map { feature ->
                val mag = feature.properties?.mag ?: 4.0
                val place = feature.properties?.place ?: "Pacific Ring"
                val timeMs = feature.properties?.time ?: now
                val depth = feature.geometry?.coordinates?.getOrNull(2)?.roundToInt() ?: 24
                val diffMin = ((now - timeMs) / 60_000L).coerceAtLeast(1L)
                val timeLabel = when {
                    diffMin < 60 -> "$diffMin min ago"
                    diffMin < 1440 -> "${diffMin / 60} hours ago"
                    else -> "${diffMin / 1440}d ago"
                }
                val severity = when {
                    mag >= 5.0 -> SeismicSeverity.HIGH
                    mag >= 4.0 -> SeismicSeverity.MODERATE
                    else -> SeismicSeverity.LOW
                }
                val shindo = when {
                    mag >= 5.5 -> "MMI IV"
                    mag >= 4.5 -> "MMI III"
                    mag >= 3.8 -> "MMI II"
                    else -> "MMI I"
                }
                val eqCoords = feature.geometry?.coordinates
                val eqLon = eqCoords?.getOrNull(0) ?: 107.05
                val eqLat = eqCoords?.getOrNull(1) ?: -6.84
                SeismicEventItem(
                    location = place,
                    depthKm = depth,
                    timeAgoLabel = timeLabel,
                    magnitude = mag,
                    shindoLabel = shindo,
                    severity = severity,
                    latitude = eqLat,
                    longitude = eqLon,
                )
            }
            val topFeature = features.first()
            val topCoords = topFeature.geometry?.coordinates
            val topLon = topCoords?.getOrNull(0) ?: 107.05
            val topLat = topCoords?.getOrNull(1) ?: -6.84
            val top = items.first()
            defaultSeismicTelemetry().copy(
                bannerTitle = "Seismic Monitor",
                bannerMagnitude = "M ${String.format(Locale.US, "%.1f", top.magnitude)}",
                bannerSubtitle = "${top.location} · ${top.timeAgoLabel}",
                activeEventTitle = "M ${String.format(Locale.US, "%.1f", top.magnitude)} Seismic Event",
                activeEventDescription = "${top.location} · Depth ${top.depthKm} km",
                depthKm = top.depthKm.toDouble(),
                shindoBadge = top.shindoLabel,
                epicenterLat = topLat,
                epicenterLon = topLon,
                shakemapUrl = null,
                recentEvents = items,
                dataSourceLabel = "USGS Fallback · BMKG TEWS",
            )
        }
    }

    suspend fun searchCities(query: String): List<CityLocation> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext presetCities
        val localMatches = presetCities.filter {
            it.name.contains(query, ignoreCase = true) || it.country.contains(query, ignoreCase = true)
        }
        val remoteMatches = runCatching {
            geocodingApi.searchLocations(query).results.orEmpty().map { dto ->
                val adm4 = if (isWithinIndonesia(dto.latitude, dto.longitude)) {
                    bmkgStations.minByOrNull { st ->
                        (st.lat - dto.latitude).pow(2) + (st.lon - dto.longitude).pow(2)
                    }?.adm4
                } else null
                CityLocation(
                    id = "geo_${dto.id}",
                    name = dto.name,
                    country = dto.country ?: "Global",
                    latitude = dto.latitude,
                    longitude = dto.longitude,
                    summaryCondition = "Live Telemetry",
                    summaryTempC = 25,
                    bmkgAdm4Code = adm4,
                )
            }
        }.getOrDefault(emptyList())

        (localMatches + remoteMatches).distinctBy { "${it.name}_${it.country}" }
    }

    private data class GridOffsetDef(
        val id: String,
        val label: String,
        val gridX: Float,
        val gridY: Float,
        val lat: Double,
        val lon: Double,
    )

    private fun regionalGridOffsets(city: CityLocation): List<GridOffsetDef> {
        val latDelta = 0.12
        val lonDelta = (0.14 / cos(Math.toRadians(city.latitude)).coerceAtLeast(0.35)).coerceIn(0.14, 0.28)
        val specs = listOf(
            Triple("nw", "North-West", -1f to -1f),
            Triple("n", "North Sector", 0f to -1f),
            Triple("ne", "North-East", 1f to -1f),
            Triple("w", "West Basin", -1f to 0f),
            Triple("c", "Center", 0f to 0f),
            Triple("e", "East Corridor", 1f to 0f),
            Triple("sw", "South-West", -1f to 1f),
            Triple("s", "South Ridge", 0f to 1f),
            Triple("se", "South-East", 1f to 1f),
        )
        return specs.map { (id, label, grid) ->
            GridOffsetDef(
                id = id,
                label = label,
                gridX = grid.first,
                gridY = grid.second,
                lat = city.latitude - (grid.second * latDelta),
                lon = city.longitude + (grid.first * lonDelta),
            )
        }
    }

    private fun buildFallbackRegionalGrid(
        city: CityLocation,
        centerAqi: Int,
        centerPm25: Double,
        centerPm10: Double,
        centerNo2: Double,
    ): List<RegionalAqiSector> {
        val offsets = regionalGridOffsets(city)
        val multipliers = listOf(0.84, 0.94, 1.18, 0.90, 1.00, 1.26, 0.72, 0.80, 1.08)
        return offsets.mapIndexed { idx, def ->
            val factor = multipliers[idx]
            val aqi = (centerAqi * factor).roundToInt().coerceIn(12, 260)
            RegionalAqiSector(
                id = def.id,
                name = "${city.name} ${def.label}",
                directionLabel = def.label,
                gridX = def.gridX,
                gridY = def.gridY,
                latitude = def.lat,
                longitude = def.lon,
                aqi = aqi,
                pm25 = (centerPm25 * factor * 10.0).roundToInt() / 10.0,
                pm10 = (centerPm10 * factor * 10.0).roundToInt() / 10.0,
                no2Ppb = (centerNo2 * factor * 10.0).roundToInt() / 10.0,
                statusLabel = categoryForAqi(aqi),
                severity = severityForAqi(aqi),
            )
        }
    }

    private fun buildDistrictSummaries(cityName: String, grid: List<RegionalAqiSector>): List<DistrictAqiItem> {
        val highlights = listOf("c", "e", "n", "sw").mapNotNull { id -> grid.firstOrNull { it.id == id } }
        val chosen = if (highlights.size >= 4) highlights else grid.take(4)
        return chosen.map { sector ->
            val desc = when (sector.severity) {
                AqiSeverity.EXCELLENT -> "Clean atmospheric circulation · PM2.5 ${sector.pm25} µg/m³"
                AqiSeverity.GOOD -> "Balanced regional air mass · PM2.5 ${sector.pm25} µg/m³"
                AqiSeverity.MODERATE -> "Urban aerosol accumulation · PM2.5 ${sector.pm25} µg/m³"
                AqiSeverity.UNHEALTHY -> "Elevated particulate hotspot · PM2.5 ${sector.pm25} µg/m³"
            }
            DistrictAqiItem(
                name = if (sector.id == "c") "$cityName Core" else sector.name,
                description = desc,
                aqi = sector.aqi,
                statusLabel = sector.statusLabel,
                severity = sector.severity,
            )
        }
    }

    private fun parseOpenMeteoDaily(
        response: com.example.data.network.OpenMeteoForecastResponse?,
        fallbackTemp: Int,
        fallbackKind: WeatherConditionKind,
    ): List<DailyForecastItem> {
        val dailyList = mutableListOf<DailyForecastItem>()
        val dailyTimes = response?.daily?.time.orEmpty()
        val dailyCodes = response?.daily?.weatherCode.orEmpty()
        val dailyMax = response?.daily?.temperature2mMax.orEmpty()
        val dailyMin = response?.daily?.temperature2mMin.orEmpty()
        val dailyPrecip = response?.daily?.precipitationProbabilityMax.orEmpty()
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outDayFormat = SimpleDateFormat("EEE", Locale.US)

        for (i in 0 until minOf(7, dailyTimes.size)) {
            val label = if (i == 0) "Today" else {
                runCatching {
                    val parsed = dayFormat.parse(dailyTimes[i])
                    if (parsed != null) outDayFormat.format(parsed) else "Day ${i + 1}"
                }.getOrDefault("Day ${i + 1}")
            }
            dailyList.add(
                DailyForecastItem(
                    dayLabel = label,
                    precipPercent = dailyPrecip.getOrNull(i) ?: 15,
                    condition = mapWmoCodeToKind(dailyCodes.getOrNull(i) ?: 3),
                    minTempC = (dailyMin.getOrNull(i) ?: (fallbackTemp - 4).toDouble()).roundToInt(),
                    maxTempC = (dailyMax.getOrNull(i) ?: (fallbackTemp + 2).toDouble()).roundToInt(),
                )
            )
        }
        return dailyList
    }

    private fun categoryForAqi(aqi: Int): String = when {
        aqi <= 25 -> "Excellent"
        aqi <= 50 -> "Good"
        aqi <= 100 -> "Moderate"
        aqi <= 150 -> "Sensitive"
        else -> "Unhealthy"
    }

    private fun severityForAqi(aqi: Int): AqiSeverity = when {
        aqi <= 30 -> AqiSeverity.EXCELLENT
        aqi <= 50 -> AqiSeverity.GOOD
        aqi <= 100 -> AqiSeverity.MODERATE
        else -> AqiSeverity.UNHEALTHY
    }

    private fun uvLabelFor(uvMax: Double): String = when {
        uvMax >= 8 -> "Very High"
        uvMax >= 6 -> "High"
        uvMax >= 3 -> "Moderate"
        else -> "Low"
    }

    private fun mapBmkgCodeToKind(code: Int, desc: String?): WeatherConditionKind {
        val text = desc.orEmpty()
        return when {
            text.contains("petir", ignoreCase = true) ||
                text.contains("thunder", ignoreCase = true) ||
                code in listOf(95, 96, 97, 99) -> WeatherConditionKind.THUNDERSTORM
            text.contains("hujan", ignoreCase = true) ||
                text.contains("rain", ignoreCase = true) ||
                text.contains("drizzle", ignoreCase = true) ||
                code in listOf(51, 53, 55, 60, 61, 63, 65, 80, 81, 82) -> WeatherConditionKind.RAIN
            text.contains("kabut", ignoreCase = true) ||
                text.contains("fog", ignoreCase = true) ||
                text.contains("mist", ignoreCase = true) ||
                text.contains("haze", ignoreCase = true) ||
                code in listOf(10, 45, 48) -> WeatherConditionKind.FOG
            text.contains("berawan tebal", ignoreCase = true) ||
                text.contains("mendung", ignoreCase = true) ||
                text.contains("overcast", ignoreCase = true) ||
                text.contains("mostly cloudy", ignoreCase = true) ||
                code in listOf(3, 4) -> WeatherConditionKind.CLOUDY
            text.contains("berawan", ignoreCase = true) ||
                text.contains("partly cloudy", ignoreCase = true) ||
                code == 2 -> WeatherConditionKind.PARTLY_CLOUDY
            code in listOf(0, 1) -> WeatherConditionKind.SUNNY
            else -> WeatherConditionKind.PARTLY_CLOUDY
        }
    }

    private fun expandBmkgDirection(wd: String): String = when (wd.uppercase(Locale.US)) {
        "N" -> "North"
        "NE" -> "Northeast"
        "E" -> "East"
        "SE" -> "Southeast"
        "S" -> "South"
        "SW" -> "Southwest"
        "W" -> "West"
        "NW" -> "Northwest"
        "VARIABLE" -> "Variable"
        else -> wd
    }

    private fun mapWmoCodeToKind(code: Int): WeatherConditionKind = when (code) {
        0, 1 -> WeatherConditionKind.SUNNY
        2 -> WeatherConditionKind.PARTLY_CLOUDY
        3 -> WeatherConditionKind.CLOUDY
        45, 48 -> WeatherConditionKind.FOG
        95, 96, 99 -> WeatherConditionKind.THUNDERSTORM
        else -> WeatherConditionKind.RAIN
    }

    private fun mapWmoCodeToLabel(code: Int): String = when (code) {
        0 -> "Clear"
        1, 2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Fog"
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> "Rain"
        71, 73, 75 -> "Snow"
        95, 96, 99 -> "Thunderstorm"
        else -> "Rain"
    }

    private fun bearingToLabel(deg: Int): String = when (((deg % 360) + 360) % 360) {
        in 23..67 -> "Northeast"
        in 68..112 -> "East"
        in 113..157 -> "Southeast"
        in 158..202 -> "South"
        in 203..247 -> "Southwest"
        in 248..292 -> "West"
        in 293..337 -> "Northwest"
        else -> "North"
    }

    suspend fun fetchRainViewerTilePrefix(): String? = withContext(Dispatchers.IO) {
        runCatching {
            val maps = rainViewerApi.getWeatherMaps()
            val host = maps.host?.trimEnd('/') ?: "https://tilecache.rainviewer.com"
            val latestPath = maps.radar?.past?.lastOrNull()?.path
                ?: maps.radar?.nowcast?.firstOrNull()?.path
                ?: return@runCatching null
            "$host$latestPath"
        }.getOrNull()
    }

    suspend fun fetchWeatherMapStations(
        centerCity: CityLocation,
        maskCoordinates: Boolean = true,
    ): List<WeatherMapStation> =
        withContext(Dispatchers.IO) {
            val apiCity = LocationMaskingUtility.maskCityForApi(centerCity, enabled = maskCoordinates)
            val deferreds = bmkgStations.map { station ->
                async {
                    val slot = runCatching {
                        bmkgForecastApi.getForecast(station.adm4).data.firstOrNull()?.cuaca?.flatten()?.firstOrNull()
                    }.getOrNull()
                    if (slot != null) {
                        val kind = mapBmkgCodeToKind(slot.weather ?: 3, slot.weatherDescEn ?: slot.weatherDesc)
                        val tpMm = slot.tp ?: when (kind) {
                            WeatherConditionKind.THUNDERSTORM -> 12.4
                            WeatherConditionKind.RAIN -> 4.2
                            WeatherConditionKind.CLOUDY -> 0.4
                            else -> 0.0
                        }
                        val cloudPct = (slot.tcc ?: 50.0).roundToInt().coerceIn(0, 100)
                        val precipProb = when {
                            kind == WeatherConditionKind.THUNDERSTORM -> 90
                            kind == WeatherConditionKind.RAIN -> maxOf(65, (tpMm * 15).roundToInt().coerceAtMost(95))
                            else -> (cloudPct * 0.35).roundToInt().coerceIn(5, 45)
                        }
                        WeatherMapStation(
                            id = station.adm4,
                            name = station.name,
                            latitude = station.lat,
                            longitude = station.lon,
                            tempC = (slot.t ?: 28.0).roundToInt(),
                            humidityPercent = (slot.hu ?: 76.0).roundToInt(),
                            windSpeedKmh = (slot.ws ?: 14.0).roundToInt(),
                            windBearingDeg = (slot.wdDeg ?: 240.0).roundToInt(),
                            precipChancePercent = precipProb,
                            precipMm = ((tpMm * 10.0).roundToInt()) / 10.0,
                            cloudCoverPercent = cloudPct,
                            conditionLabel = slot.weatherDescEn?.takeIf { it.isNotBlank() }
                                ?: slot.weatherDesc?.takeIf { it.isNotBlank() }
                                ?: "Cloudy",
                            conditionKind = kind,
                            isBmkg = true,
                        )
                    } else {
                        defaultStationFor(station)
                    }
                }
            }
            val bmkgStationsList = deferreds.map { it.await() }
            if (!isWithinIndonesia(apiCity.latitude, apiCity.longitude)) {
                val activeWeather = fetchLiveWeather(apiCity, maskCoordinates = maskCoordinates)
                    .getOrElse { defaultWeatherFor(apiCity) }
                val activeStation = WeatherMapStation(
                    id = apiCity.id,
                    name = apiCity.name,
                    latitude = apiCity.latitude,
                    longitude = apiCity.longitude,
                    tempC = activeWeather.currentTempC,
                    humidityPercent = activeWeather.humidityPercent,
                    windSpeedKmh = activeWeather.windSpeedKmh,
                    windBearingDeg = activeWeather.windBearingDeg,
                    precipChancePercent = activeWeather.precipChancePercent,
                    precipMm = if (activeWeather.conditionKind == WeatherConditionKind.RAIN ||
                        activeWeather.conditionKind == WeatherConditionKind.THUNDERSTORM
                    ) 3.8 else 0.0,
                    cloudCoverPercent = if (activeWeather.conditionKind == WeatherConditionKind.SUNNY) 15 else 65,
                    conditionLabel = activeWeather.conditionLabel,
                    conditionKind = activeWeather.conditionKind,
                    isBmkg = false,
                )
                listOf(activeStation) + bmkgStationsList
            } else {
                bmkgStationsList
            }
        }

    fun defaultWeatherMapStations(): List<WeatherMapStation> = bmkgStations.map(::defaultStationFor)

    private fun defaultStationFor(station: BmkgStationSeed): WeatherMapStation {
        data class SeedWeather(
            val temp: Int,
            val kind: WeatherConditionKind,
            val label: String,
            val precipPct: Int,
            val precipMm: Double,
            val cloudPct: Int,
        )
        val seed = when (station.name) {
            "Bandung" -> SeedWeather(23, WeatherConditionKind.RAIN, "Light Rain", 75, 3.6, 82)
            "Palembang" -> SeedWeather(27, WeatherConditionKind.RAIN, "Moderate Rain", 80, 6.2, 88)
            "Surabaya" -> SeedWeather(31, WeatherConditionKind.PARTLY_CLOUDY, "Partly Cloudy", 25, 0.0, 42)
            "Denpasar" -> SeedWeather(30, WeatherConditionKind.SUNNY, "Clear", 15, 0.0, 18)
            "Medan" -> SeedWeather(28, WeatherConditionKind.THUNDERSTORM, "Thunderstorm", 90, 14.5, 95)
            "Manado" -> SeedWeather(28, WeatherConditionKind.RAIN, "Coastal Showers", 72, 4.8, 78)
            "Jayapura" -> SeedWeather(29, WeatherConditionKind.THUNDERSTORM, "Tropical Storm", 86, 11.2, 92)
            "Balikpapan" -> SeedWeather(28, WeatherConditionKind.RAIN, "Passing Rain", 68, 2.9, 76)
            else -> SeedWeather(29, WeatherConditionKind.CLOUDY, "Cloudy", 45, 0.5, 68)
        }
        return WeatherMapStation(
            id = station.adm4,
            name = station.name,
            latitude = station.lat,
            longitude = station.lon,
            tempC = seed.temp,
            humidityPercent = 78,
            windSpeedKmh = 14,
            windBearingDeg = 240,
            precipChancePercent = seed.precipPct,
            precipMm = seed.precipMm,
            cloudCoverPercent = seed.cloudPct,
            conditionLabel = seed.label,
            conditionKind = seed.kind,
            isBmkg = true,
        )
    }

    fun formatCurrentTimestamp(): String {
        val fmt = SimpleDateFormat("MMM yyyy · HH:mm", Locale.US)
        return "Synced · ${fmt.format(Date())}"
    }

    fun formatLiveClockTimestamp(): String {
        val fmt = SimpleDateFormat("HH:mm:ss", Locale.US)
        return "Live · ${fmt.format(Date())}"
    }

    fun formatOfflineCacheTimestamp(millis: Long): String {
        val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.US)
        return "Offline Cache · ${fmt.format(Date(millis))}"
    }
}

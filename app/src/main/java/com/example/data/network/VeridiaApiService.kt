package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class OpenMeteoForecastResponse(
    val current: CurrentWeatherDto? = null,
    val hourly: HourlyWeatherDto? = null,
    val daily: DailyWeatherDto? = null,
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "temperature_2m") val temperature2m: Double? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: Double? = null,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Int? = null,
    @Json(name = "weather_code") val weatherCode: Int? = null,
    @Json(name = "surface_pressure") val surfacePressure: Double? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double? = null,
    @Json(name = "wind_direction_10m") val windDirection10m: Int? = null,
    @Json(name = "wind_gusts_10m") val windGusts10m: Double? = null,
)

@JsonClass(generateAdapter = true)
data class HourlyWeatherDto(
    val time: List<String> = emptyList(),
    @Json(name = "temperature_2m") val temperature2m: List<Double> = emptyList(),
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int> = emptyList(),
    val precipitation: List<Double> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "uv_index") val uvIndex: List<Double> = emptyList(),
    @Json(name = "dew_point_2m") val dewPoint2m: List<Double> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class DailyWeatherDto(
    val time: List<String> = emptyList(),
    @Json(name = "weather_code") val weatherCode: List<Int> = emptyList(),
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double> = emptyList(),
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double> = emptyList(),
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int> = emptyList(),
    val sunrise: List<String> = emptyList(),
    val sunset: List<String> = emptyList(),
    @Json(name = "uv_index_max") val uvIndexMax: List<Double> = emptyList(),
    @Json(name = "precipitation_sum") val precipitationSum: List<Double> = emptyList(),
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class OpenMeteoAirQualityResponse(
    val current: CurrentAirQualityDto? = null,
)

@JsonClass(generateAdapter = true)
data class CurrentAirQualityDto(
    @Json(name = "us_aqi") val usAqi: Int? = null,
    val pm2_5: Double? = null,
    val pm10: Double? = null,
    @Json(name = "carbon_monoxide") val carbonMonoxide: Double? = null,
    @Json(name = "nitrogen_dioxide") val nitrogenDioxide: Double? = null,
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResultDto>? = null,
)

@JsonClass(generateAdapter = true)
data class GeocodingResultDto(
    val id: Long,
    val name: String,
    val country: String? = null,
    val latitude: Double,
    val longitude: Double,
)

@JsonClass(generateAdapter = true)
data class UsgsEarthquakeResponse(
    val features: List<UsgsFeatureDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class UsgsFeatureDto(
    val properties: UsgsPropertiesDto? = null,
    val geometry: UsgsGeometryDto? = null,
)

@JsonClass(generateAdapter = true)
data class UsgsPropertiesDto(
    val mag: Double? = null,
    val place: String? = null,
    val time: Long? = null,
)

@JsonClass(generateAdapter = true)
data class UsgsGeometryDto(
    val coordinates: List<Double> = emptyList(),
)

interface OpenMeteoForecastApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m",
        @Query("hourly") hourly: String = "temperature_2m,precipitation_probability,precipitation,weather_code,uv_index,dew_point_2m",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset,uv_index_max,precipitation_sum,wind_speed_10m_max",
        @Query("timezone") timezone: String = "auto",
    ): OpenMeteoForecastResponse
}

interface OpenMeteoAirQualityApi {
    @GET("v1/air-quality")
    suspend fun getAirQuality(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "us_aqi,pm2_5,pm10,carbon_monoxide,nitrogen_dioxide",
        @Query("timezone") timezone: String = "auto",
    ): OpenMeteoAirQualityResponse

    @GET("v1/air-quality")
    suspend fun getRegionalAirQualityGrid(
        @Query("latitude") latitudes: String,
        @Query("longitude") longitudes: String,
        @Query("current") current: String = "us_aqi,pm2_5,pm10,carbon_monoxide,nitrogen_dioxide",
        @Query("timezone") timezone: String = "auto",
    ): List<OpenMeteoAirQualityResponse>
}

interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun searchLocations(
        @Query("name") query: String,
        @Query("count") count: Int = 6,
        @Query("language") language: String = "en",
    ): GeocodingResponse
}

interface UsgsEarthquakeApi {
    @GET("fdsnws/event/1/query")
    suspend fun getRecentEarthquakes(
        @Query("format") format: String = "geojson",
        @Query("limit") limit: Int = 6,
        @Query("minmagnitude") minMagnitude: Double = 3.5,
        @Query("minlatitude") minLat: Double = 24.0,
        @Query("maxlatitude") maxLat: Double = 46.0,
        @Query("minlongitude") minLon: Double = 123.0,
        @Query("maxlongitude") maxLon: Double = 148.0,
    ): UsgsEarthquakeResponse
}

// --- BMKG Public Weather Forecast API (https://api.bmkg.go.id/) ---

@JsonClass(generateAdapter = true)
data class BmkgForecastResponse(
    val lokasi: BmkgLocationDto? = null,
    val data: List<BmkgDataBlockDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class BmkgLocationDto(
    val provinsi: String? = null,
    val kotkab: String? = null,
    val kecamatan: String? = null,
    val desa: String? = null,
    val lon: Double? = null,
    val lat: Double? = null,
    val timezone: String? = null,
)

@JsonClass(generateAdapter = true)
data class BmkgDataBlockDto(
    val lokasi: BmkgLocationDto? = null,
    val cuaca: List<List<BmkgWeatherSlotDto>> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class BmkgWeatherSlotDto(
    val datetime: String? = null,
    val t: Double? = null,
    val tcc: Double? = null,
    val tp: Double? = null,
    val weather: Int? = null,
    @Json(name = "weather_desc") val weatherDesc: String? = null,
    @Json(name = "weather_desc_en") val weatherDescEn: String? = null,
    @Json(name = "wd_deg") val wdDeg: Double? = null,
    val wd: String? = null,
    val ws: Double? = null,
    val hu: Double? = null,
    @Json(name = "vs_text") val vsText: String? = null,
    @Json(name = "local_datetime") val localDatetime: String? = null,
)

interface BmkgForecastApi {
    @GET("publik/prakiraan-cuaca")
    suspend fun getForecast(@Query("adm4") adm4Code: String): BmkgForecastResponse
}

// --- BMKG Earthquake & Tsunami TEWS API (https://data.bmkg.go.id/) ---

@JsonClass(generateAdapter = true)
data class BmkgAutoGempaResponse(
    @Json(name = "Infogempa") val infoGempa: BmkgInfogempaSingleDto? = null,
)

@JsonClass(generateAdapter = true)
data class BmkgInfogempaSingleDto(
    val gempa: BmkgGempaDto? = null,
)

@JsonClass(generateAdapter = true)
data class BmkgGempaListResponse(
    @Json(name = "Infogempa") val infoGempa: BmkgInfogempaListDto? = null,
)

@JsonClass(generateAdapter = true)
data class BmkgInfogempaListDto(
    val gempa: List<BmkgGempaDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class BmkgGempaDto(
    @Json(name = "Tanggal") val tanggal: String? = null,
    @Json(name = "Jam") val jam: String? = null,
    @Json(name = "DateTime") val dateTime: String? = null,
    @Json(name = "Coordinates") val coordinates: String? = null,
    @Json(name = "Lintang") val lintang: String? = null,
    @Json(name = "Bujur") val bujur: String? = null,
    @Json(name = "Magnitude") val magnitude: String? = null,
    @Json(name = "Kedalaman") val kedalaman: String? = null,
    @Json(name = "Wilayah") val wilayah: String? = null,
    @Json(name = "Potensi") val potensi: String? = null,
    @Json(name = "Dirasakan") val dirasakan: String? = null,
    @Json(name = "Shakemap") val shakemap: String? = null,
)

interface BmkgEarthquakeApi {
    @GET("DataMKG/TEWS/autogempa.json")
    suspend fun getLatestAutoGempa(): BmkgAutoGempaResponse

    @GET("DataMKG/TEWS/gempadirasakan.json")
    suspend fun getFeltEarthquakes(): BmkgGempaListResponse

    @GET("DataMKG/TEWS/gempaterkini.json")
    suspend fun getRecentM5Earthquakes(): BmkgGempaListResponse
}

// --- RainViewer Public Live Weather Radar Tiles API (No API key required) ---

@JsonClass(generateAdapter = true)
data class RainViewerMapsResponse(
    val host: String? = null,
    val radar: RainViewerRadarBlockDto? = null,
)

@JsonClass(generateAdapter = true)
data class RainViewerRadarBlockDto(
    val past: List<RainViewerFrameDto> = emptyList(),
    val nowcast: List<RainViewerFrameDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class RainViewerFrameDto(
    val time: Long = 0L,
    val path: String = "",
)

interface RainViewerApi {
    @GET("public/weather-maps.json")
    suspend fun getWeatherMaps(): RainViewerMapsResponse
}


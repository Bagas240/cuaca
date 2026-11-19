package com.example

import android.Manifest
import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.VeridiaDatabase
import com.example.data.location.LocationMaskingUtility
import com.example.data.location.LocationTracker
import com.example.data.model.PressureUnit
import com.example.data.model.SeismicEventItem
import com.example.data.model.SeismicSeverity
import com.example.data.model.TemperatureUnit
import com.example.data.model.WindUnit
import com.example.data.repository.VeridiaRepository
import com.example.ui.components.formatPressureValue
import com.example.ui.components.formatTempValue
import com.example.ui.components.formatWindValue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Veridia", appName)
    }

    @Test
    fun `unit conversions format accurately`() {
        assertEquals(21, formatTempValue(21, TemperatureUnit.CELSIUS))
        assertEquals(70, formatTempValue(21, TemperatureUnit.FAHRENHEIT))
        assertEquals("18" to "km/h", formatWindValue(18, WindUnit.KMH))
        assertEquals("11" to "mph", formatWindValue(18, WindUnit.MPH))
        assertEquals("1,014" to "hPa", formatPressureValue(1014, PressureUnit.HPA))
    }

    @Test
    fun `location tracker formats gps coordinates across hemispheres`() {
        assertEquals("37.77°N, 122.42°W", LocationTracker.formatCoordLabel(37.7749, -122.4194))
        assertEquals("33.87°S, 151.21°E", LocationTracker.formatCoordLabel(-33.8688, 151.2093))
    }

    @Test
    fun `location tracker detects granted and denied location permissions`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val shadowApp = Shadows.shadowOf(app)
        val tracker = LocationTracker(app)

        shadowApp.denyPermissions(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
        assertFalse(tracker.hasLocationPermission())

        shadowApp.grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        assertTrue(tracker.hasLocationPermission())
        assertFalse(tracker.hasFineLocationPermission())

        shadowApp.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        assertTrue(tracker.hasLocationPermission())
        assertTrue(tracker.hasFineLocationPermission())
    }

    @Test
    fun `repository initializes BMKG Jakarta city and 9-node regional AQI grid`() {
        val repo = VeridiaRepository()
        val jakarta = repo.presetCities.first()
        assertEquals("jakarta", jakarta.id)
        assertNotNull(jakarta.bmkgAdm4Code)

        val weather = repo.defaultWeatherFor(jakarta)
        assertTrue(weather.dataSourceLabel.contains("BMKG"))

        val aqi = repo.defaultAirQuality(jakarta)
        assertEquals(9, aqi.regionalGrid.size)
        assertTrue(aqi.regionalGrid.any { it.id == "c" && it.gridX == 0f && it.gridY == 0f })

        val seismic = repo.defaultSeismicTelemetry()
        assertTrue(seismic.dataSourceLabel.contains("BMKG"))
    }

    @Test
    fun `slippy map math computes valid 2d tile coordinates and multi-provider urls`() {
        val (wx, wy) = com.example.ui.components.SlippyMapMath.latLonToWorldPx(-6.1754, 106.8272, 10)
        assertTrue(wx > 0.0 && wy > 0.0)
        val url = com.example.ui.components.SlippyMapMath.tileUrl(10, 815, 530)
        assertEquals("https://basemaps.cartocdn.com/rastertiles/voyager/10/815/530.png", url)

        val providers = com.example.ui.components.SlippyMapMath.multiProviderTileUrls(6, 50, 33)
        assertEquals(3, providers.size)
        assertTrue(providers[0].contains("arcgisonline.com"))
        assertTrue(providers[1].contains("tile.openstreetmap.org"))
        assertTrue(providers[2].contains("basemaps.cartocdn.com"))

        val radarUrl = com.example.ui.components.SlippyMapMath.rainViewerTileUrl(
            "https://tilecache.rainviewer.com/v2/radar/1700000000",
            6,
            50,
            33,
        )
        assertEquals("https://tilecache.rainviewer.com/v2/radar/1700000000/256/6/50/33/2/1_1.png", radarUrl)
    }

    @Test
    fun `gps privacy obfuscation masks exact coordinates onto coarse district zone`() {
        val exactLat = -6.175392
        val exactLon = 106.827153
        val (maskedLat, maskedLon) = LocationTracker.obfuscateForPrivacy(exactLat, exactLon)

        assertTrue(kotlin.math.abs(maskedLat - exactLat) <= 0.05)
        assertTrue(kotlin.math.abs(maskedLon - exactLon) <= 0.05)
        assertFalse(maskedLat == exactLat)
        assertFalse(maskedLon == exactLon)

        val zoneLabel = LocationTracker.formatPrivateZoneLabel(exactLat, exactLon)
        assertTrue(zoneLabel.startsWith("~"))
        assertTrue(zoneLabel.contains("Masked Zone"))
        assertFalse(zoneLabel.contains("1753"))
        assertFalse(zoneLabel.contains("8271"))
    }

    @Test
    fun `repository initializes 12 BMKG weather map stations with precipitation data`() {
        val repo = VeridiaRepository()
        val stations = repo.defaultWeatherMapStations()
        assertEquals(12, stations.size)
        assertTrue(stations.all { it.isBmkg })
        assertTrue(stations.any { it.precipMm > 1.0 && it.precipChancePercent >= 60 })
        assertTrue(stations.any { it.name == "Jakarta" } && stations.any { it.name == "Jayapura" })
    }

    @Test
    fun `location masking utility rounds coordinates to lower precision before API calls`() {
        val masked = LocationMaskingUtility.maskCoordinates(
            latitude = -6.175392,
            longitude = 106.827153,
            precisionDecimals = 1,
            enabled = true,
        )
        assertTrue(masked.isMasked)
        assertEquals(-6.2, masked.latitude, 0.0001)
        assertEquals(106.8, masked.longitude, 0.0001)

        val unmasked = LocationMaskingUtility.maskCoordinates(
            latitude = -6.175392,
            longitude = 106.827153,
            precisionDecimals = 1,
            enabled = false,
        )
        assertFalse(unmasked.isMasked)
        assertEquals(-6.175392, unmasked.latitude, 0.000001)
        assertEquals(106.827153, unmasked.longitude, 0.000001)

        val zeroNormalized = LocationMaskingUtility.maskCoordinates(-0.02, 195.0, precisionDecimals = 1, enabled = true)
        assertEquals(0.0, zeroNormalized.latitude, 0.0001)
        assertEquals(180.0, zeroNormalized.longitude, 0.0001)
    }

    @Test
    fun `room database persists and restores weather and seismic alerts when offline`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, VeridiaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val dao = db.telemetryCacheDao()
            val repo = VeridiaRepository(cacheDao = dao)
            val jakarta = repo.presetCities.first()
            val liveWeatherSample = repo.defaultWeatherFor(jakarta).copy(
                currentTempC = 33,
                conditionLabel = "Cached Monsoon Rain",
                isLiveSynced = true,
            )
            val liveAqiSample = repo.defaultAirQuality(jakarta).copy(regionalAqi = 74)
            val liveStations = repo.defaultWeatherMapStations()
            val liveSeismicSample = repo.defaultSeismicTelemetry().copy(
                bannerMagnitude = "M 6.2",
                activeEventTitle = "M 6.2 Cached Alert",
                recentEvents = listOf(
                    SeismicEventItem(
                        location = "Selat Sunda",
                        depthKm = 15,
                        timeAgoLabel = "5 min ago",
                        magnitude = 6.2,
                        shindoLabel = "MMI IV",
                        severity = SeismicSeverity.HIGH,
                    )
                ),
            )

            repo.saveWeatherBundleToCache(
                cityId = jakarta.id,
                weather = liveWeatherSample,
                airQuality = liveAqiSample,
                stations = liveStations,
                fetchedAtMillis = 1_700_000_000_000L,
            )
            repo.saveSeismicToCache(liveSeismicSample, fetchedAtMillis = 1_700_000_000_000L)

            val observedWeather = repo.observeLatestCachedWeather().first()
            assertNotNull(observedWeather)
            assertEquals(33, observedWeather?.weather?.currentTempC)
            assertEquals("Cached Monsoon Rain", observedWeather?.weather?.conditionLabel)
            assertEquals(74, observedWeather?.airQuality?.regionalAqi)
            assertEquals(12, observedWeather?.stations?.size)

            val observedSeismic = repo.observeCachedSeismic().first()
            assertNotNull(observedSeismic)
            assertEquals("M 6.2", observedSeismic?.seismic?.bannerMagnitude)
            assertEquals("Selat Sunda", observedSeismic?.seismic?.recentEvents?.firstOrNull()?.location)
        } finally {
            db.close()
        }
    }
}



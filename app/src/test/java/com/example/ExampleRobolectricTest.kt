package com.example

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.BatteryManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import androidx.work.workDataOf
import com.example.alerts.DisasterKind
import com.example.alerts.DisasterNotificationManager
import com.example.alerts.SeismicAccelerometerDetector
import com.example.data.local.VeridiaDatabase
import com.example.data.location.LocationMaskingUtility
import com.example.data.location.LocationTracker
import com.example.data.location.SolarScheduleCalculator
import com.example.data.model.AppThemeMode
import com.example.data.model.HourlyForecastItem
import com.example.data.model.PressureUnit
import com.example.data.model.SeismicEventItem
import com.example.data.model.SeismicSeverity
import com.example.data.model.SolarThemePhase
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserSettings
import com.example.data.model.WeatherConditionKind
import com.example.data.model.WindUnit
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.VeridiaRepository
import com.example.data.security.SecurityGuard
import com.example.data.security.TelemetryCrypto
import com.example.ui.components.ActiveDisasterAlertOverlay
import com.example.ui.components.AnimatedHomeWeatherAvdIcon
import com.example.ui.components.AnimatedWindSwayAvdIcon
import com.example.ui.components.HomeWeatherIconAnimationController
import com.example.ui.components.HourlyForecast24hCard
import com.example.ui.components.PulsatingAqiGlowAvdBadge
import com.example.ui.components.SolarThemeSwitcherCard
import com.example.ui.components.SunCycleCard
import com.example.ui.components.SunriseSunsetDashboardCard
import com.example.ui.components.VeridiaBottomDock
import com.example.ui.components.VeridiaRoute
import com.example.ui.components.WindIndicatorStyle
import com.example.ui.components.WindSpeedDirectionIndicator
import com.example.ui.components.aqiGlowColorFor
import com.example.ui.components.formatPressureValue
import com.example.ui.components.formatTempValue
import com.example.ui.components.formatWindConditionsLabel
import com.example.ui.components.formatWindValue
import com.example.ui.components.normalizeWindBearing
import com.example.ui.components.rememberHomeWeatherIconAnimationController
import com.example.ui.components.shortestAngleDelta
import com.example.ui.components.windConditionSummary
import com.example.ui.navigation.DestinationPlaybackState
import com.example.ui.navigation.LocalNavStateController
import com.example.ui.navigation.NavStateController
import com.example.ui.navigation.ProvideNavDestinationState
import com.example.ui.navigation.rememberIsDestinationAnimating
import com.example.ui.navigation.rememberNavStateController
import com.example.ui.theme.AfternoonDuskVeridiaPalette
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.DayVeridiaPalette
import com.example.ui.theme.NightVeridiaPalette
import com.example.ui.theme.PastelAccent
import com.example.ui.theme.SunriseSunsetVeridiaPalette
import com.example.ui.theme.VeridiaTheme
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.veridiaColors
import com.example.ui.viewmodel.WeatherViewModel
import com.example.work.SyncBatteryTier
import com.example.work.WeatherSyncScheduler
import com.example.work.WeatherSyncWorker
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

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

    @Test
    fun `wind indicator helpers compute condition summaries and shortest arc bearing deltas`() {
        assertEquals("Calm", windConditionSummary(1))
        assertEquals("Light Breeze", windConditionSummary(8))
        assertEquals("Gentle Breeze", windConditionSummary(18))
        assertEquals("Moderate Breeze", windConditionSummary(30))
        assertEquals("Fresh Breeze", windConditionSummary(45))
        assertEquals("Strong Wind", windConditionSummary(62))

        assertEquals(350f, normalizeWindBearing(-10), 0.01f)
        assertEquals(20f, shortestAngleDelta(350f, 10f), 0.01f)
        assertEquals(-20f, shortestAngleDelta(10f, 350f), 0.01f)
        assertEquals(
            "18 km/h · NW (315°) · Gentle Breeze",
            formatWindConditionsLabel(18, WindUnit.KMH, "NW", 315),
        )
    }

    @Test
    fun `wind speed and direction indicator renders compact pill and detailed dial labels`() {
        composeRule.setContent {
            VeridiaTheme {
                WindSpeedDirectionIndicator(
                    windSpeedKmh = 18,
                    windBearingDeg = 315,
                    windDirectionLabel = "NW",
                    windUnit = WindUnit.KMH,
                    style = WindIndicatorStyle.COMPACT_PILL,
                )
            }
        }
        composeRule.onNodeWithTag("wind_indicator_pill").assertIsDisplayed()
        composeRule.onNodeWithText("18 km/h · NW · Gentle Breeze").assertIsDisplayed()
    }

    @Test
    fun `telemetry crypto encrypts and decrypts payloads with AES-256-GCM envelope`() {
        val plaintext = """{"city":"Jakarta","temp":31,"wind":18}"""
        val encrypted = TelemetryCrypto.encrypt(plaintext)
        assertTrue(encrypted.startsWith("ENC:v1:"))
        assertFalse(encrypted.contains("Jakarta"))
        val decrypted = TelemetryCrypto.decrypt(encrypted)
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `security guard allows only verified HTTPS telemetry domains`() {
        assertTrue(SecurityGuard.isHostAllowed("https", "api.bmkg.go.id"))
        assertTrue(SecurityGuard.isHostAllowed("https", "data.bmkg.go.id"))
        assertTrue(SecurityGuard.isHostAllowed("https", "api.open-meteo.com"))
        assertFalse(SecurityGuard.isHostAllowed("http", "api.bmkg.go.id"))
        assertFalse(SecurityGuard.isHostAllowed("https", "evil.tracker.example"))
    }

    @Test
    fun `disaster notification manager detects tsunami and nearby earthquake hazards`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = DisasterNotificationManager(context)
        val repo = VeridiaRepository()
        val jakarta = repo.presetCities.first()
        val weather = repo.defaultWeatherFor(jakarta)
        val aqi = repo.defaultAirQuality(jakarta)

        val tsunamiSeismic = repo.defaultSeismicTelemetry().copy(
            bannerMagnitude = "M 7.1",
            tsunamiStatus = "Berpotensi Tsunami",
            epicenterLat = jakarta.latitude,
            epicenterLon = jakarta.longitude,
        )
        val tsunamiAlert = manager.evaluateTelemetryForDisaster(
            city = jakarta,
            weather = weather,
            airQuality = aqi,
            seismic = tsunamiSeismic,
            settings = UserSettings(seismicPWaveAlert = true),
        )
        assertNotNull(tsunamiAlert)
        assertEquals(DisasterKind.TSUNAMI, tsunamiAlert?.kind)

        val nearbyQuake = repo.defaultSeismicTelemetry().copy(
            bannerMagnitude = "M 5.4",
            tsunamiStatus = "No Tsunami Threat",
            epicenterLat = jakarta.latitude + 0.1,
            epicenterLon = jakarta.longitude + 0.1,
            recentEvents = listOf(
                SeismicEventItem(
                    location = "Cianjur",
                    depthKm = 10,
                    timeAgoLabel = "Just now",
                    magnitude = 5.4,
                    shindoLabel = "MMI IV",
                    severity = SeismicSeverity.HIGH,
                ),
            ),
        )
        val quakeAlert = manager.evaluateTelemetryForDisaster(
            city = jakarta,
            weather = weather,
            airQuality = aqi,
            seismic = nearbyQuake,
            settings = UserSettings(seismicPWaveAlert = true),
        )
        assertNotNull(quakeAlert)
        assertEquals(DisasterKind.EARTHQUAKE, quakeAlert?.kind)
    }

    @Test
    fun `hourly 24h forecast card renders scrollable 24h slots`() {
        val slots = (0 until 24).map { hour ->
            HourlyForecastItem(
                timeLabel = if (hour == 0) "Now" else String.format("%02d:00", hour),
                tempC = 26 + (hour % 6),
                condition = if (hour % 3 == 0) WeatherConditionKind.RAIN else WeatherConditionKind.PARTLY_CLOUDY,
                isCurrent = hour == 0,
                precipPercent = 30,
            )
        }
        composeRule.setContent {
            VeridiaTheme {
                HourlyForecast24hCard(
                    hourlyItems = slots,
                    tempUnit = TemperatureUnit.CELSIUS,
                )
            }
        }
        composeRule.onNodeWithTag("hourly_forecast_24h_list").assertIsDisplayed()
        composeRule.onNodeWithText("Now").assertIsDisplayed()
        composeRule.onNodeWithText("24-HOUR FORECAST").assertIsDisplayed()
    }

    @Test
    fun `sun cycle card renders sunrise and sunset times`() {
        composeRule.setContent {
            VeridiaTheme {
                SunCycleCard(
                    locationName = "Jakarta",
                    sunriseTime = "05:44",
                    sunsetTime = "17:58",
                    daylightDuration = "12h 14m Daylight",
                    solarProgress = 0.55f,
                )
            }
        }
        composeRule.onNodeWithTag("sun_cycle_card").assertIsDisplayed()
        composeRule.onNodeWithText("05:44").assertIsDisplayed()
        composeRule.onNodeWithText("17:58").assertIsDisplayed()
        composeRule.onNodeWithText("12h 14m Daylight").assertIsDisplayed()
    }

    @Test
    fun `seismic accelerometer detector ignores single tap but detects oscillatory earthquake vibration`() {
        val detector = SeismicAccelerometerDetector()

        // Baseline gravity calibration
        val baseline = detector.onAccelerometerReading(0f, 0f, 9.81f, 1_000_000_000L)
        assertFalse(baseline.patternRecognized)

        // Single brief bump should NOT trigger
        val singleBump = detector.onAccelerometerReading(4.2f, 0f, 9.81f, 1_100_000_000L)
        assertFalse(singleBump.patternRecognized)
        detector.reset()
        detector.onAccelerometerReading(0f, 0f, 9.81f, 2_000_000_000L)

        // Sustained oscillatory horizontal seismic shaking across 600ms with reversals
        val samples = listOf(
            Triple(5.5f, 0.4f, 2_120_000_000L),
            Triple(-5.8f, -0.5f, 2_260_000_000L),
            Triple(6.0f, 0.6f, 2_420_000_000L),
            Triple(-5.9f, -0.4f, 2_580_000_000L),
        )
        var detected = false
        for ((x, y, ts) in samples) {
            val res = detector.onAccelerometerReading(x, y, 9.81f, ts)
            if (res.patternRecognized) detected = true
        }
        assertTrue(detected)
    }

    @Test
    fun `weather viewmodel triggers siren alert ui when seismic vibration pattern is recognized`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = WeatherViewModel(app)

        composeRule.setContent {
            val uiState by vm.uiState.collectAsState()
            VeridiaTheme {
                ActiveDisasterAlertOverlay(
                    alert = uiState.activeDisasterAlert,
                    isSirenPlaying = uiState.isSirenPlaying,
                    onSilenceSiren = vm::stopDisasterSiren,
                    onDismiss = vm::dismissActiveDisasterAlert,
                    onOpenDisasterMonitor = {},
                )
            }
        }

        // Feed gravity baseline then sustained oscillatory seismic vibration
        vm.onAccelerometerSample(0f, 0f, 9.81f, 5_000_000_000L)
        vm.onAccelerometerSample(5.6f, 0.3f, 9.81f, 5_120_000_000L)
        vm.onAccelerometerSample(-5.9f, -0.2f, 9.81f, 5_260_000_000L)
        vm.onAccelerometerSample(6.2f, 0.4f, 9.81f, 5_420_000_000L)
        vm.onAccelerometerSample(-6.0f, -0.3f, 9.81f, 5_600_000_000L)

        composeRule.waitForIdle()

        assertEquals(DisasterKind.EARTHQUAKE, vm.uiState.value.activeDisasterAlert?.kind)
        assertEquals("Seismic Vibration Detected", vm.uiState.value.activeDisasterAlert?.title)
        assertTrue(vm.uiState.value.seismicPatternDetectedCount >= 1)
        composeRule.onNodeWithTag("active_disaster_alert_overlay").assertIsDisplayed()
        composeRule.onNodeWithText("Drop, Cover under sturdy furniture, and Hold On.").assertIsDisplayed()

        vm.dismissActiveDisasterAlert()
    }

    @Test
    fun `encrypted settings store encrypts user settings and gps coordinates at rest`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SettingsDataStore(context)

        store.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        store.setSelectedCityId("gps_current")
        store.saveEncryptedGpsCoordinates(
            latitude = -6.21462,
            longitude = 106.84513,
            localityName = "Jakarta Selatan",
            countryName = "Indonesia",
            zoneLabel = "~6.2°S, 106.8°E · Masked Zone",
        )

        val settings = store.settingsFlow.first()
        assertEquals(TemperatureUnit.FAHRENHEIT, settings.temperatureUnit)
        assertEquals("gps_current", settings.selectedCityId)

        val restoredGps = store.getStoredGpsCoordinates()
        assertNotNull(restoredGps)
        assertEquals(-6.2, restoredGps!!.latitude, 0.001)
        assertEquals(106.8, restoredGps.longitude, 0.001)
        assertEquals("Jakarta Selatan", restoredGps.localityName)

        // Verify raw underlying storage does NOT contain plaintext GPS strings or city names
        val rawFallback = context.getSharedPreferences("veridia_encrypted_prefs_jvm_fallback", Context.MODE_PRIVATE)
        val rawContents = rawFallback.all.entries.joinToString { "${it.key}=${it.value}" }
        assertFalse(rawContents.contains("Jakarta Selatan"))
        assertFalse(rawContents.contains("-6.2"))
        assertFalse(rawContents.contains("FAHRENHEIT"))
    }

    @Test
    fun `bottom dock toggles between home and weather with spring transition`() {
        var currentRoute by mutableStateOf(VeridiaRoute.HOME)

        composeRule.setContent {
            var route by remember { mutableStateOf(VeridiaRoute.HOME) }
            VeridiaTheme {
                VeridiaBottomDock(
                    currentRoute = route,
                    onNavigate = { target ->
                        route = target
                        currentRoute = target
                    },
                )
            }
        }

        composeRule.onNodeWithTag("dock_tab_home").assertIsDisplayed()
        composeRule.onNodeWithTag("dock_tab_weather").assertIsDisplayed()

        // Only the selected Home icon should animate; all other tabs stay idle to save battery
        val isAnimating = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "animating")
        val isIdle = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "idle")

        composeRule.onNodeWithTag("dock_icon_${VeridiaRoute.HOME}", useUnmergedTree = true).assert(isAnimating)
        composeRule.onNodeWithTag("dock_icon_${VeridiaRoute.WEATHER}", useUnmergedTree = true).assert(isIdle)
        composeRule.onNodeWithTag("dock_icon_${VeridiaRoute.AIR}", useUnmergedTree = true).assert(isIdle)
        composeRule.onNodeWithTag("dock_icon_${VeridiaRoute.ALERTS}", useUnmergedTree = true).assert(isIdle)
        composeRule.onNodeWithTag("dock_icon_${VeridiaRoute.SETTINGS}", useUnmergedTree = true).assert(isIdle)

        composeRule.onNodeWithTag("dock_tab_weather").performClick()
        composeRule.waitForIdle()
        assertEquals(VeridiaRoute.WEATHER, currentRoute)

        // Switching to Weather turns Weather animation on and turns Home animation off
        composeRule.onNodeWithTag("dock_icon_${VeridiaRoute.WEATHER}", useUnmergedTree = true).assert(isAnimating)
        composeRule.onNodeWithTag("dock_icon_${VeridiaRoute.HOME}", useUnmergedTree = true).assert(isIdle)

        composeRule.onNodeWithTag("dock_tab_home").performClick()
        composeRule.waitForIdle()
        assertEquals(VeridiaRoute.HOME, currentRoute)
        composeRule.onNodeWithTag("dock_icon_${VeridiaRoute.HOME}", useUnmergedTree = true).assert(isAnimating)
        composeRule.onNodeWithTag("dock_icon_${VeridiaRoute.WEATHER}", useUnmergedTree = true).assert(isIdle)
    }

    @Test
    fun `home weather icon animation controller drives avd wind sway and aqi glow`() {
        assertEquals(PastelAccent, aqiGlowColorFor(32))
        assertEquals(WarningAmber, aqiGlowColorFor(75))
        assertEquals(DangerCrimson, aqiGlowColorFor(145))

        var capturedController: HomeWeatherIconAnimationController? = null

        composeRule.setContent {
            val controller = rememberHomeWeatherIconAnimationController(
                windSpeedKmh = 28,
                aqiScore = 44,
            )
            capturedController = controller
            VeridiaTheme {
                androidx.compose.foundation.layout.Row {
                    AnimatedHomeWeatherAvdIcon(controller = controller)
                    AnimatedWindSwayAvdIcon(controller = controller)
                    PulsatingAqiGlowAvdBadge(aqiScore = 44, controller = controller)
                }
            }
        }

        composeRule.onNodeWithTag("avd_weather_icon").assertIsDisplayed()
        composeRule.onNodeWithTag("avd_wind_sway_icon").assertIsDisplayed()
        composeRule.onNodeWithTag("avd_aqi_pulse_badge").assertIsDisplayed()

        val controller = capturedController
        assertNotNull(controller)
        assertTrue(controller!!.isPlaying)

        val initialWeatherEnd = controller.weatherAvdAtEnd
        val initialWindEnd = controller.windAvdAtEnd
        val initialAqiEnd = controller.aqiAvdAtEnd

        composeRule.runOnIdle {
            controller.triggerTelemetryImpulse()
            controller.pause()
        }

        assertFalse(controller.isPlaying)
        assertEquals(!initialWeatherEnd, controller.weatherAvdAtEnd)
        assertEquals(!initialWindEnd, controller.windAvdAtEnd)
        assertEquals(!initialAqiEnd, controller.aqiAvdAtEnd)
        assertEquals(1, controller.telemetryImpulseCount)

        composeRule.runOnIdle {
            controller.play()
        }
        assertTrue(controller.isPlaying)
    }

    @Test
    fun `workmanager schedules periodic weather sync only when charging or on wifi`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        val workManager = WorkManager.getInstance(context)

        val wifiConstraints = WeatherSyncScheduler.buildWifiConstraints()
        assertEquals(NetworkType.UNMETERED, wifiConstraints.requiredNetworkType)
        assertTrue(wifiConstraints.requiresBatteryNotLow())
        assertFalse(wifiConstraints.requiresCharging())

        val chargingConstraints = WeatherSyncScheduler.buildChargingConstraints()
        assertEquals(NetworkType.CONNECTED, chargingConstraints.requiredNetworkType)
        assertTrue(chargingConstraints.requiresCharging())

        WeatherSyncScheduler.schedulePeriodicWeatherSync(context)

        val wifiInfos = workManager.getWorkInfosForUniqueWork(WeatherSyncScheduler.UNIQUE_WORK_WIFI).get()
        val chargingInfos = workManager.getWorkInfosForUniqueWork(WeatherSyncScheduler.UNIQUE_WORK_CHARGING).get()
        assertEquals(1, wifiInfos.size)
        assertEquals(1, chargingInfos.size)
        assertEquals(WorkInfo.State.ENQUEUED, wifiInfos.first().state)
        assertEquals(WorkInfo.State.ENQUEUED, chargingInfos.first().state)
        assertEquals(NetworkType.UNMETERED, wifiInfos.first().constraints.requiredNetworkType)
        assertTrue(chargingInfos.first().constraints.requiresCharging())

        WeatherSyncScheduler.cancelPeriodicWeatherSync(context)
        val wifiAfterCancel = workManager.getWorkInfosForUniqueWork(WeatherSyncScheduler.UNIQUE_WORK_WIFI).get()
        val chargingAfterCancel = workManager.getWorkInfosForUniqueWork(WeatherSyncScheduler.UNIQUE_WORK_CHARGING).get()
        assertEquals(WorkInfo.State.CANCELLED, wifiAfterCancel.first().state)
        assertEquals(WorkInfo.State.CANCELLED, chargingAfterCancel.first().state)
    }

    @Test
    fun `weather sync worker skips when disabled and deduplicates overlapping charging wifi triggers`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SettingsDataStore(context)

        // 1. Disabled background sync returns clean success with "disabled"
        store.setBackgroundSyncEnabled(false)
        val disabledWorker = TestListenableWorkerBuilder<WeatherSyncWorker>(context)
            .setInputData(workDataOf(WeatherSyncWorker.KEY_TRIGGER_CHANNEL to "wifi"))
            .build()
        val disabledResult = disabledWorker.doWork()
        assertTrue(disabledResult is ListenableWorker.Result.Success)
        assertEquals("disabled", disabledResult.outputData.getString(WeatherSyncWorker.KEY_STATUS))

        // 2. Enabled with recent sync timestamp deduplicates back-to-back charging + wifi runs
        store.setBackgroundSyncEnabled(true)
        store.setLastBackgroundSyncMillis(System.currentTimeMillis())
        val dedupWorker = TestListenableWorkerBuilder<WeatherSyncWorker>(context)
            .setInputData(workDataOf(WeatherSyncWorker.KEY_TRIGGER_CHANNEL to "charging"))
            .build()
        val dedupResult = dedupWorker.doWork()
        assertTrue(dedupResult is ListenableWorker.Result.Success)
        assertEquals("deduplicated", dedupResult.outputData.getString(WeatherSyncWorker.KEY_STATUS))
    }

    @Test
    fun `navState controller pauses animations when destination is hidden or backgrounded and resumes on transition`() {
        val hostRegistryHolder = object : LifecycleOwner {
            val registry = LifecycleRegistry(this).apply {
                currentState = Lifecycle.State.RESUMED
            }
            override val lifecycle: Lifecycle get() = registry
        }
        var navStateCtrl: NavStateController? = null
        var homeIconCtrl: HomeWeatherIconAnimationController? = null
        var homeAnimating = false
        var weatherAnimating = false
        var navigateToWeather: (() -> Unit)? = null
        var navigateToHome: (() -> Unit)? = null

        composeRule.setContent {
            CompositionLocalProvider(
                androidx.lifecycle.compose.LocalLifecycleOwner provides hostRegistryHolder,
            ) {
                val navController = rememberNavController()
                val controller = rememberNavStateController(navController)
                navStateCtrl = controller
                navigateToWeather = { navController.navigate(VeridiaRoute.WEATHER) }
                navigateToHome = { navController.popBackStack() }

                CompositionLocalProvider(LocalNavStateController provides controller) {
                    NavHost(
                        navController = navController,
                        startDestination = VeridiaRoute.HOME,
                    ) {
                        composable(VeridiaRoute.HOME) {
                            ProvideNavDestinationState(VeridiaRoute.HOME, controller) {
                                homeAnimating = rememberIsDestinationAnimating()
                                homeIconCtrl = rememberHomeWeatherIconAnimationController(
                                    windSpeedKmh = 18,
                                    aqiScore = 38,
                                )
                                AnimatedHomeWeatherAvdIcon(controller = homeIconCtrl)
                            }
                        }
                        composable(VeridiaRoute.WEATHER) {
                            ProvideNavDestinationState(VeridiaRoute.WEATHER, controller) {
                                weatherAnimating = rememberIsDestinationAnimating()
                            }
                        }
                    }
                }
            }
        }

        composeRule.waitForIdle()
        val ctrl = requireNotNull(navStateCtrl)
        assertTrue(homeAnimating)
        assertEquals(DestinationPlaybackState.ACTIVE, ctrl.navState.playbackStateFor(VeridiaRoute.HOME))
        assertTrue(requireNotNull(homeIconCtrl).shouldAnimate)

        // Navigating to WEATHER immediately pauses HOME and immediately resumes WEATHER
        composeRule.runOnIdle {
            navigateToWeather?.invoke()
        }
        composeRule.waitForIdle()
        assertEquals(VeridiaRoute.WEATHER, ctrl.navState.currentRoute)
        assertEquals(DestinationPlaybackState.PAUSED, ctrl.navState.playbackStateFor(VeridiaRoute.HOME))
        assertEquals(DestinationPlaybackState.ACTIVE, ctrl.navState.playbackStateFor(VeridiaRoute.WEATHER))
        assertTrue(weatherAnimating)

        // Backgrounding host lifecycle (ON_STOP -> CREATED) pauses ALL destinations
        composeRule.runOnIdle {
            hostRegistryHolder.registry.currentState = Lifecycle.State.CREATED
        }
        composeRule.waitForIdle()
        assertFalse(ctrl.navState.isAppForeground)
        assertEquals(DestinationPlaybackState.PAUSED, ctrl.navState.playbackStateFor(VeridiaRoute.WEATHER))
        assertFalse(weatherAnimating)

        // Returning to foreground & popping back to HOME immediately resumes HOME animations
        composeRule.runOnIdle {
            hostRegistryHolder.registry.currentState = Lifecycle.State.RESUMED
            navigateToHome?.invoke()
        }
        composeRule.waitForIdle()
        assertTrue(ctrl.navState.isAppForeground)
        assertEquals(VeridiaRoute.HOME, ctrl.navState.currentRoute)
        assertEquals(DestinationPlaybackState.ACTIVE, ctrl.navState.playbackStateFor(VeridiaRoute.HOME))
        assertTrue(homeAnimating)
        assertTrue(requireNotNull(homeIconCtrl).shouldAnimate)
    }

    @Test
    fun `batteryManager api decreases WorkManager sync frequency on low and critical battery states`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        val workManager = WorkManager.getInstance(context)
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val shadowBattery = Shadows.shadowOf(batteryManager)
        val store = SettingsDataStore(context)
        store.setBackgroundSyncEnabled(true)

        // 1. Normal battery (78%, unplugged) -> 15m standard Wi-Fi interval
        shadowBattery.setIsCharging(false)
        shadowBattery.setIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY, 78)
        val normalProfile = WeatherSyncScheduler.schedulePeriodicWeatherSync(context)
        assertEquals(SyncBatteryTier.NORMAL, normalProfile.tier)
        assertEquals(WeatherSyncScheduler.SYNC_INTERVAL_MINUTES, normalProfile.repeatIntervalMinutes)

        // 2. Low battery (16%, unplugged) -> interval decreases to 60m + worker throttles recent syncs
        shadowBattery.setIsCharging(false)
        shadowBattery.setIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY, 16)
        val lowProfile = WeatherSyncScheduler.schedulePeriodicWeatherSync(context)
        assertEquals(SyncBatteryTier.LOW_BATTERY, lowProfile.tier)
        assertEquals(WeatherSyncScheduler.SYNC_INTERVAL_LOW_BATTERY_MINUTES, lowProfile.repeatIntervalMinutes)

        val wifiLowInfo = workManager.getWorkInfosForUniqueWork(WeatherSyncScheduler.UNIQUE_WORK_WIFI).get().first()
        assertEquals(
            WeatherSyncScheduler.SYNC_INTERVAL_LOW_BATTERY_MINUTES * 60_000L,
            wifiLowInfo.periodicityInfo?.repeatIntervalMillis,
        )

        store.setLastBackgroundSyncMillis(System.currentTimeMillis() - 15 * 60_000L) // 15m ago (< 60m low-battery cooldown)
        val lowBatteryWorker = TestListenableWorkerBuilder<WeatherSyncWorker>(context)
            .setInputData(workDataOf(WeatherSyncWorker.KEY_TRIGGER_CHANNEL to "wifi"))
            .build()
        val lowResult = lowBatteryWorker.doWork()
        assertTrue(lowResult is ListenableWorker.Result.Success)
        assertEquals("throttled_low_battery", lowResult.outputData.getString(WeatherSyncWorker.KEY_STATUS))
        assertEquals(16, lowResult.outputData.getInt(WeatherSyncWorker.KEY_BATTERY_PERCENT, -1))

        // 3. Critical battery (8%, unplugged) -> interval drops to 180m & worker defers immediately
        shadowBattery.setIsCharging(false)
        shadowBattery.setIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY, 8)
        val criticalProfile = WeatherSyncScheduler.schedulePeriodicWeatherSync(context)
        assertEquals(SyncBatteryTier.CRITICAL_BATTERY, criticalProfile.tier)
        assertEquals(WeatherSyncScheduler.SYNC_INTERVAL_CRITICAL_BATTERY_MINUTES, criticalProfile.repeatIntervalMinutes)

        val criticalWorker = TestListenableWorkerBuilder<WeatherSyncWorker>(context)
            .setInputData(workDataOf(WeatherSyncWorker.KEY_TRIGGER_CHANNEL to "wifi"))
            .build()
        val criticalResult = criticalWorker.doWork()
        assertTrue(criticalResult is ListenableWorker.Result.Success)
        assertEquals("deferred_critical_battery", criticalResult.outputData.getString(WeatherSyncWorker.KEY_STATUS))
        assertEquals(SyncBatteryTier.CRITICAL_BATTERY.name, criticalResult.outputData.getString(WeatherSyncWorker.KEY_BATTERY_TIER))

        // 4. Plugging in charger even at 8% restores 15m charging cadence
        shadowBattery.setIsCharging(true)
        val chargingProfile = WeatherSyncScheduler.schedulePeriodicWeatherSync(context)
        assertEquals(SyncBatteryTier.CHARGING, chargingProfile.tier)
        assertEquals(WeatherSyncScheduler.SYNC_INTERVAL_MINUTES, chargingProfile.repeatIntervalMinutes)
    }

    @Test
    fun `solar schedule calculator computes distinct location-aware sunrise sunset and next event for Jakarta vs London`() {
        // June 21 solstice at 09:00 UTC -> mid-afternoon in Jakarta (+7), morning in London (+1 BST)
        val solsticeMillis = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(2025, java.util.Calendar.JUNE, 21, 9, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val jakarta = SolarScheduleCalculator.computeForLocation(
            latitude = -6.20,
            longitude = 106.85,
            dateMillis = solsticeMillis,
            timeZoneOffsetHours = 7.0,
        )
        val london = SolarScheduleCalculator.computeForLocation(
            latitude = 51.51,
            longitude = -0.13,
            dateMillis = solsticeMillis,
            timeZoneOffsetHours = 1.0,
        )

        assertTrue(jakarta.sunriseTime.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(jakarta.sunsetTime.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(london.sunriseTime.matches(Regex("\\d{2}:\\d{2}")))
        assertTrue(london.sunsetTime.matches(Regex("\\d{2}:\\d{2}")))
        // London summer solstice has much earlier sunrise (~04:43 BST) and later sunset (~21:21 BST) than Jakarta
        assertTrue(london.sunriseTime < jakarta.sunriseTime)
        assertTrue(london.sunsetTime > jakarta.sunsetTime)
        assertTrue(london.daylightDuration.contains("16h"))
        assertTrue(jakarta.daylightDuration.contains("11h"))
        assertTrue(jakarta.todayDateLabel.startsWith("Today"))
        assertTrue(jakarta.nextSolarEventLabel.startsWith("Sunset in "))
    }

    @Test
    fun `sunrise sunset dashboard card renders location coordinates date solar noon and next event`() {
        composeRule.setContent {
            VeridiaTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    SunriseSunsetDashboardCard(
                        locationName = "Jakarta",
                        coordinatesLabel = "6.2°S, 106.8°E (Zone)",
                        isGpsActive = true,
                        sunriseTime = "05:49",
                        sunsetTime = "17:48",
                        solarNoonTime = "11:48",
                        daylightDuration = "11h 59m Daylight",
                        solarProgress = 0.62f,
                        todayDateLabel = "Today · Sat, Jun 21",
                        nextSolarEventLabel = "Sunset in 1h 48m",
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("sunrise_sunset_dashboard_card").assertIsDisplayed()
        composeRule.onNodeWithTag("sunrise_sunset_date").assertIsDisplayed()
        composeRule.onNodeWithText("Today · Sat, Jun 21").assertIsDisplayed()
        composeRule.onNodeWithTag("sunrise_sunset_location").assertIsDisplayed()
        composeRule.onNodeWithText("Jakarta").assertIsDisplayed()
        composeRule.onNodeWithText("GPS · 6.2°S, 106.8°E (Zone)").assertIsDisplayed()
        composeRule.onNodeWithTag("sunrise_sunset_sunrise_tile").assertIsDisplayed()
        composeRule.onNodeWithText("05:49").assertIsDisplayed()
        composeRule.onNodeWithTag("sunrise_sunset_sunset_tile").assertIsDisplayed()
        composeRule.onNodeWithText("17:48").assertIsDisplayed()
        composeRule.onNodeWithTag("sunrise_sunset_moon_phase").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Waxing Gibbous").assertIsDisplayed()
        composeRule.onNodeWithText("68% Lit").assertIsDisplayed()
        composeRule.onNodeWithTag("sunrise_sunset_solar_noon").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Solar Noon · 11:48").assertIsDisplayed()
        composeRule.onNodeWithTag("sunrise_sunset_next_event").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Sunset in 1h 48m").assertIsDisplayed()
    }

    @Test
    fun `solar schedule calculator computes accurate moon phases across synodic cycle`() {
        val refNewMoon = 947182440000L
        val millisPerDay = 86_400_000.0

        val newMoon = SolarScheduleCalculator.computeMoonPhase(refNewMoon)
        assertEquals("New Moon", newMoon.phaseName)
        assertTrue(newMoon.illuminationPercent < 5)

        val firstQuarter = SolarScheduleCalculator.computeMoonPhase(
            refNewMoon + (7.3826 * millisPerDay).toLong()
        )
        assertEquals("First Quarter", firstQuarter.phaseName)
        assertTrue(firstQuarter.illuminationPercent in 45..55)

        val fullMoon = SolarScheduleCalculator.computeMoonPhase(
            refNewMoon + (14.7653 * millisPerDay).toLong()
        )
        assertEquals("Full Moon", fullMoon.phaseName)
        assertTrue(fullMoon.illuminationPercent >= 98)

        val lastQuarter = SolarScheduleCalculator.computeMoonPhase(
            refNewMoon + (22.1479 * millisPerDay).toLong()
        )
        assertEquals("Last Quarter", lastQuarter.phaseName)
        assertTrue(lastQuarter.illuminationPercent in 45..55)
    }

    @Test
    fun `solar schedule calculator resolves four time-of-day theme phases from sunrise and sunset`() {
        val sunriseMin = 5 * 60 + 45 // 05:45
        val sunsetMin = 17 * 60 + 55 // 17:55

        // Morning sunrise & sunset windows -> rose-white SUNRISE_SUNSET
        assertEquals(
            SolarThemePhase.SUNRISE_SUNSET,
            SolarScheduleCalculator.resolveSolarThemePhaseForMinute(sunriseMin - 20, sunriseMin, sunsetMin),
        )
        assertEquals(
            SolarThemePhase.SUNRISE_SUNSET,
            SolarScheduleCalculator.resolveSolarThemePhaseForMinute(sunsetMin + 10, sunriseMin, sunsetMin),
        )

        // Bright midday -> DAY
        assertEquals(
            SolarThemePhase.DAY,
            SolarScheduleCalculator.resolveSolarThemePhaseForMinute(11 * 60 + 30, sunriseMin, sunsetMin),
        )

        // Late afternoon & post-sunset dusk -> AFTERNOON_DUSK ("sedikit menggelap")
        assertEquals(
            SolarThemePhase.AFTERNOON_DUSK,
            SolarScheduleCalculator.resolveSolarThemePhaseForMinute(sunsetMin - 90, sunriseMin, sunsetMin),
        )
        assertEquals(
            SolarThemePhase.AFTERNOON_DUSK,
            SolarScheduleCalculator.resolveSolarThemePhaseForMinute(sunsetMin + 50, sunriseMin, sunsetMin),
        )

        // Late night & pre-dawn -> NIGHT
        assertEquals(
            SolarThemePhase.NIGHT,
            SolarScheduleCalculator.resolveSolarThemePhaseForMinute(22 * 60 + 30, sunriseMin, sunsetMin),
        )
        assertEquals(
            SolarThemePhase.NIGHT,
            SolarScheduleCalculator.resolveSolarThemePhaseForMinute(2 * 60 + 0, sunriseMin, sunsetMin),
        )

        // Manual theme modes override solar calculation
        assertEquals(
            SolarThemePhase.NIGHT,
            SolarScheduleCalculator.resolveEffectiveThemePhase(AppThemeMode.NIGHT, "05:45", "17:55", 106.8),
        )
        assertEquals(
            SolarThemePhase.SUNRISE_SUNSET,
            SolarScheduleCalculator.resolveEffectiveThemePhase(AppThemeMode.SUNRISE_SUNSET, "05:45", "17:55", 106.8),
        )
    }

    @Test
    fun `solar theme switcher card and VeridiaTheme update palettes across all solar phases`() {
        var currentMode by mutableStateOf(AppThemeMode.DAY)
        var capturedPhase: SolarThemePhase? = null
        var capturedIsDark = false

        composeRule.setContent {
            val phase = when (currentMode) {
                AppThemeMode.AUTO_SOLAR -> SolarThemePhase.DAY
                AppThemeMode.SUNRISE_SUNSET -> SolarThemePhase.SUNRISE_SUNSET
                AppThemeMode.DAY -> SolarThemePhase.DAY
                AppThemeMode.AFTERNOON_DUSK -> SolarThemePhase.AFTERNOON_DUSK
                AppThemeMode.NIGHT -> SolarThemePhase.NIGHT
            }
            VeridiaTheme(themePhase = phase) {
                capturedPhase = veridiaColors.phase
                capturedIsDark = veridiaColors.isDark
                SolarThemeSwitcherCard(
                    themeMode = currentMode,
                    activePhase = phase,
                    sunriseTime = "05:45",
                    sunsetTime = "17:55",
                    onSelectThemeMode = { currentMode = it },
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("solar_theme_switcher_card").assertIsDisplayed()
        assertEquals(SolarThemePhase.DAY, capturedPhase)
        assertFalse(capturedIsDark)
        assertFalse(DayVeridiaPalette.isDark)

        // Switch to Dawn/Sunset (rose-white "putih kemerahan")
        composeRule.onNodeWithTag("theme_switch_sunrise_sunset").performClick()
        composeRule.waitForIdle()
        assertEquals(AppThemeMode.SUNRISE_SUNSET, currentMode)
        assertEquals(SolarThemePhase.SUNRISE_SUNSET, capturedPhase)
        assertFalse(SunriseSunsetVeridiaPalette.isDark)
        composeRule.onNodeWithText("Sunrise/Sunset · Rose-White").assertIsDisplayed()

        // Switch to Dusk (slightly darker afternoon)
        composeRule.onNodeWithTag("theme_switch_afternoon_dusk").performClick()
        composeRule.waitForIdle()
        assertEquals(AppThemeMode.AFTERNOON_DUSK, currentMode)
        assertEquals(SolarThemePhase.AFTERNOON_DUSK, capturedPhase)
        assertFalse(AfternoonDuskVeridiaPalette.isDark)
        composeRule.onNodeWithText("Afternoon · Shaded Dusk").assertIsDisplayed()

        // Switch to Night (dark & cool)
        composeRule.onNodeWithTag("theme_switch_night").performClick()
        composeRule.waitForIdle()
        assertEquals(AppThemeMode.NIGHT, currentMode)
        assertEquals(SolarThemePhase.NIGHT, capturedPhase)
        assertTrue(capturedIsDark)
        assertTrue(NightVeridiaPalette.isDark)
        composeRule.onNodeWithText("Night · Dark & Cool").assertIsDisplayed()

        // Switch back to Auto
        composeRule.onNodeWithTag("theme_switch_auto").performClick()
        composeRule.waitForIdle()
        assertEquals(AppThemeMode.AUTO_SOLAR, currentMode)
        composeRule.onNodeWithText("Auto · 05:45–17:55").assertIsDisplayed()
    }

    @Test
    fun `encrypted settings store persists theme mode securely at rest`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = SettingsDataStore(context)

        store.setThemeMode(AppThemeMode.SUNRISE_SUNSET)
        assertEquals(AppThemeMode.SUNRISE_SUNSET, store.settingsFlow.first().themeMode)

        store.setThemeMode(AppThemeMode.NIGHT)
        assertEquals(AppThemeMode.NIGHT, store.settingsFlow.first().themeMode)

        val rawFallback = context.getSharedPreferences("veridia_encrypted_prefs_jvm_fallback", Context.MODE_PRIVATE)
        val rawContents = rawFallback.all.entries.joinToString { "${it.key}=${it.value}" }
        assertFalse(rawContents.contains("SUNRISE_SUNSET"))
        assertFalse(rawContents.contains("NIGHT"))
    }
}



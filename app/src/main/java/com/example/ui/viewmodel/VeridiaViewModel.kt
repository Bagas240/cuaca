package com.example.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alerts.DisasterAlertEvent
import com.example.alerts.DisasterKind
import com.example.alerts.DisasterNotificationManager
import com.example.alerts.DisasterSirenPlayer
import com.example.alerts.SeismicAccelerometerDetector
import com.example.data.location.LocationMaskingUtility
import com.example.data.location.LocationTracker
import com.example.data.location.SolarScheduleCalculator
import com.example.data.model.AirQualityTelemetry
import com.example.data.model.AppThemeMode
import com.example.data.model.AqiMapLayer
import com.example.data.model.AtmosphereStratum
import com.example.data.model.CityLocation
import com.example.data.model.PressureUnit
import com.example.data.model.RadarLayer
import com.example.data.model.RadarTelemetry
import com.example.data.model.SeismicTelemetry
import com.example.data.model.SolarThemePhase
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserSettings
import com.example.data.model.WeatherMapLayer
import com.example.data.model.WeatherMapStation
import com.example.data.model.WeatherTelemetry
import com.example.data.model.WindUnit
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.VeridiaRepository
import com.example.data.security.SecurityGuard
import com.example.data.security.SecurityPosture
import com.example.work.SyncBatteryProfile
import com.example.work.WeatherSyncScheduler
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

enum class DashboardMetricCard {
    TEMPERATURE,
    HUMIDITY,
    WIND_SPEED,
}

data class VeridiaUiState(
    val selectedCity: CityLocation,
    val presetCities: List<CityLocation>,
    val searchQuery: String = "",
    val searchResults: List<CityLocation> = emptyList(),
    val isCityModalOpen: Boolean = false,
    val isSosModalOpen: Boolean = false,
    val selectedStratum: AtmosphereStratum = AtmosphereStratum.SURFACE,
    val selectedRadarLayer: RadarLayer = RadarLayer.RAIN_DOPPLER,
    val selectedWeatherMapLayer: WeatherMapLayer = WeatherMapLayer.RAIN_RADAR,
    val radarPlaybackOffsetMinutes: Int = 0,
    val selectedAqiLayer: AqiMapLayer = AqiMapLayer.AQI,
    val selectedAqiSectorId: String = "c",
    val weather: WeatherTelemetry,
    val weatherMapStations: List<WeatherMapStation> = emptyList(),
    val rainViewerTilePrefix: String? = null,
    val airQuality: AirQualityTelemetry,
    val seismic: SeismicTelemetry,
    val radar: RadarTelemetry,
    val settings: UserSettings = UserSettings(),
    val isSyncing: Boolean = false,
    val isOfflineMode: Boolean = false,
    val cacheRefreshing: Boolean = false,
    val lastUpdatedLabel: String = "Syncing...",
    val hasLocationPermission: Boolean = false,
    val locationPermissionDenied: Boolean = false,
    val isLocatingGps: Boolean = false,
    val isGpsActive: Boolean = false,
    val gpsCoordinatesLabel: String? = null,
    val gpsErrorMessage: String? = null,
    val expandedMetricCard: DashboardMetricCard? = null,
    val activeDisasterAlert: DisasterAlertEvent? = null,
    val isSirenPlaying: Boolean = false,
    val seismicSensorAvailable: Boolean = false,
    val seismicSensorMonitoringEnabled: Boolean = true,
    val liveAccelerometerMs2: Float = 0f,
    val seismicPatternDetectedCount: Int = 0,
    val securityPosture: SecurityPosture = SecurityGuard.currentPosture(),
    val syncBatteryProfile: SyncBatteryProfile = SyncBatteryProfile(),
    val activeThemePhase: SolarThemePhase = SolarThemePhase.DAY,
)

typealias WeatherViewModel = VeridiaViewModel

class VeridiaViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val repository = VeridiaRepository.create(application.applicationContext)
    private val settingsStore = SettingsDataStore(application.applicationContext)
    private val locationTracker = LocationTracker(application.applicationContext)
    private val disasterManager = DisasterNotificationManager(application.applicationContext)
    private val sirenPlayer = DisasterSirenPlayer(application.applicationContext)
    private val sensorManager = application.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val seismicDetector = SeismicAccelerometerDetector()

    private var lastSensorUiEmitMs: Long = 0L
    private var isAccelerometerRegistered: Boolean = false

    private val initialCity: CityLocation
    private val restoredGpsLabel: String?
    private val restoredGpsActive: Boolean

    init {
        val storedGps = settingsStore.getStoredGpsCoordinates()
        val initialSettings = settingsStore.currentSettings
        val presetMatch = repository.presetCities.first()
        if (storedGps != null && initialSettings.selectedCityId == "gps_current") {
            initialCity = CityLocation(
                id = "gps_current",
                name = storedGps.localityName,
                country = storedGps.countryName,
                latitude = storedGps.latitude,
                longitude = storedGps.longitude,
                summaryCondition = "Protected Zone",
                summaryTempC = presetMatch.summaryTempC,
                isCurrent = true,
            )
            restoredGpsLabel = storedGps.zoneLabel
            restoredGpsActive = true
        } else {
            initialCity = presetMatch
            restoredGpsLabel = null
            restoredGpsActive = false
        }
    }

    private val _uiState = MutableStateFlow(
        run {
            val defaultWeather = repository.defaultWeatherFor(initialCity)
            val initialSettings = settingsStore.currentSettings
            val initialPhase = SolarScheduleCalculator.resolveEffectiveThemePhase(
                themeMode = initialSettings.themeMode,
                sunriseTime = defaultWeather.sunriseTime,
                sunsetTime = defaultWeather.sunsetTime,
                longitude = initialCity.longitude,
            )
            VeridiaUiState(
                selectedCity = initialCity,
                presetCities = repository.presetCities,
                searchResults = repository.presetCities,
                weather = defaultWeather,
                weatherMapStations = repository.defaultWeatherMapStations(),
                airQuality = repository.defaultAirQuality(initialCity),
                seismic = repository.defaultSeismicTelemetry(),
                radar = repository.defaultRadarTelemetry(),
                settings = initialSettings,
                hasLocationPermission = locationTracker.hasLocationPermission(),
                isGpsActive = restoredGpsActive,
                gpsCoordinatesLabel = restoredGpsLabel,
                seismicSensorAvailable = accelerometerSensor != null,
                syncBatteryProfile = WeatherSyncScheduler.readBatteryPowerProfile(application.applicationContext),
                activeThemePhase = initialPhase,
            )
        }
    )
    val uiState: StateFlow<VeridiaUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var isBatteryReceiverRegistered = false

    private val batteryStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshBatterySyncProfile()
        }
    }

    init {
        observeSettings()
        observeRoomTelemetryCache()
        registerBatteryStateReceiver()
        if (_uiState.value.seismicSensorMonitoringEnabled) {
            startSeismicAccelerometerMonitoring()
        }
        if (settingsStore.currentSettings.backgroundSyncEnabled) {
            runCatching {
                val profile = WeatherSyncScheduler.schedulePeriodicWeatherSync(application.applicationContext)
                _uiState.update { it.copy(syncBatteryProfile = profile) }
            }
        }
        if (locationTracker.hasLocationPermission()) {
            fetchWeatherForCurrentGps()
        } else {
            refreshTelemetryFor(initialCity, preserveGpsActive = restoredGpsActive)
        }
        startRealtimeAutoRefresh()
        startSolarThemeTicker()
    }

    private fun startSolarThemeTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(60_000L)
                _uiState.update { current ->
                    val nextPhase = resolveThemePhaseForState(current)
                    if (nextPhase == current.activeThemePhase) current else current.copy(activeThemePhase = nextPhase)
                }
            }
        }
    }

    private fun resolveThemePhaseForState(state: VeridiaUiState): SolarThemePhase {
        return SolarScheduleCalculator.resolveEffectiveThemePhase(
            themeMode = state.settings.themeMode,
            sunriseTime = state.weather.sunriseTime,
            sunsetTime = state.weather.sunsetTime,
            longitude = state.selectedCity.longitude,
        )
    }

    private fun registerBatteryStateReceiver() {
        val appContext = getApplication<Application>().applicationContext
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }
        runCatching {
            ContextCompat.registerReceiver(
                appContext,
                batteryStateReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            isBatteryReceiverRegistered = true
        }
    }

    fun refreshBatterySyncProfile() {
        val appContext = getApplication<Application>().applicationContext
        val previousTier = _uiState.value.syncBatteryProfile.tier
        val nextProfile = WeatherSyncScheduler.readBatteryPowerProfile(appContext)
        _uiState.update { it.copy(syncBatteryProfile = nextProfile) }
        if (settingsStore.currentSettings.backgroundSyncEnabled && nextProfile.tier != previousTier) {
            runCatching {
                WeatherSyncScheduler.adaptScheduleForBatteryProfile(appContext, nextProfile)
            }
        }
    }

    private fun observeRoomTelemetryCache() {
        viewModelScope.launch {
            repository.observeLatestCachedWeather().collectLatest { snapshot ->
                if (snapshot == null) return@collectLatest
                _uiState.update { state ->
                    if (state.weather.isLiveSynced || state.selectedCity.id != snapshot.cityId) {
                        state
                    } else {
                        state.copy(
                            selectedCity = snapshot.weather.city,
                            weather = snapshot.weather,
                            airQuality = snapshot.airQuality ?: state.airQuality,
                            weatherMapStations = snapshot.stations?.takeIf { it.isNotEmpty() }
                                ?: state.weatherMapStations,
                            lastUpdatedLabel = repository.formatOfflineCacheTimestamp(snapshot.fetchedAtMillis),
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.observeCachedSeismic().collectLatest { snapshot ->
                if (snapshot == null) return@collectLatest
                _uiState.update { state ->
                    if (!state.isOfflineMode && state.weather.isLiveSynced) {
                        state
                    } else {
                        state.copy(seismic = snapshot.seismic)
                    }
                }
            }
        }
    }

    private fun startRealtimeAutoRefresh() {
        viewModelScope.launch {
            var tickCount = 0
            while (isActive) {
                delay(20_000L) // Real-time 20-second telemetry cycle for wind, weather, AQI, radar & seismic
                tickCount += 1
                val activeCity = _uiState.value.selectedCity
                val maskCoords = _uiState.value.settings.gpsPrivacyMode
                val seismicDeferred = async { repository.fetchRecentEarthquakesWithCache() }
                val radarDeferred = async { repository.fetchRainViewerTilePrefix() }
                val weatherDeferred = async { repository.fetchLiveWeatherWithCache(activeCity, maskCoordinates = maskCoords) }
                val aqiDeferred = async { repository.fetchLiveAirQuality(activeCity, maskCoordinates = maskCoords) }
                val stationsDeferred = if (tickCount % 3 == 0) {
                    async { runCatching { repository.fetchWeatherMapStations(activeCity, maskCoordinates = maskCoords) } }
                } else null

                val seismicResult = seismicDeferred.await()
                val radarPrefix = radarDeferred.await()
                val weatherResult = weatherDeferred.await()
                val aqiResult = aqiDeferred.await()
                val stationsResult = stationsDeferred?.await()

                val seismicOutcome = seismicResult.getOrNull()
                val weatherOutcome = weatherResult.getOrNull()
                val liveAqi = aqiResult.getOrNull()
                val liveStations = stationsResult?.getOrNull()?.takeIf { it.isNotEmpty() }

                if (weatherOutcome != null && !weatherOutcome.fromCache) {
                    repository.saveWeatherBundleToCache(
                        cityId = activeCity.id,
                        weather = weatherOutcome.data,
                        airQuality = liveAqi,
                        stations = liveStations,
                        fetchedAtMillis = weatherOutcome.fetchedAtMillis,
                    )
                    settingsStore.setOfflineCacheLabel(repository.formatCurrentTimestamp())
                }

                val isOfflineFallback = (seismicOutcome?.fromCache == true) ||
                    (weatherOutcome?.fromCache == true) ||
                    (seismicOutcome == null && !_uiState.value.weather.isLiveSynced)
                val cachedStamp = weatherOutcome?.takeIf { it.fromCache }?.fetchedAtMillis
                    ?: seismicOutcome?.takeIf { it.fromCache }?.fetchedAtMillis

                _uiState.update { state ->
                    val nextWeather = weatherOutcome?.data ?: state.weather
                    val nextAqi = liveAqi ?: state.airQuality
                    val nextSeismic = seismicOutcome?.data ?: state.seismic
                    state.copy(
                        seismic = nextSeismic,
                        rainViewerTilePrefix = radarPrefix ?: state.rainViewerTilePrefix,
                        weather = nextWeather,
                        airQuality = nextAqi,
                        weatherMapStations = liveStations ?: state.weatherMapStations,
                        isOfflineMode = isOfflineFallback,
                        securityPosture = SecurityGuard.currentPosture(),
                        activeThemePhase = SolarScheduleCalculator.resolveEffectiveThemePhase(
                            themeMode = state.settings.themeMode,
                            sunriseTime = nextWeather.sunriseTime,
                            sunsetTime = nextWeather.sunsetTime,
                            longitude = state.selectedCity.longitude,
                        ),
                        lastUpdatedLabel = if (isOfflineFallback && cachedStamp != null) {
                            repository.formatOfflineCacheTimestamp(cachedStamp)
                        } else if (!isOfflineFallback) {
                            repository.formatLiveClockTimestamp()
                        } else {
                            state.lastUpdatedLabel
                        },
                    )
                }

                if (!isOfflineFallback) {
                    evaluateDisasterConditions(
                        city = _uiState.value.selectedCity,
                        weather = _uiState.value.weather,
                        airQuality = _uiState.value.airQuality,
                        seismic = _uiState.value.seismic,
                    )
                }
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsStore.settingsFlow.collectLatest { savedSettings ->
                val matchedPreset = repository.presetCities.firstOrNull { it.id == savedSettings.selectedCityId }
                _uiState.update { current ->
                    val shouldSwitchToPreset = matchedPreset != null &&
                        savedSettings.selectedCityId != "gps_current" &&
                        !current.isGpsActive &&
                        matchedPreset.id != current.selectedCity.id
                    val nextCity = if (shouldSwitchToPreset) matchedPreset!! else current.selectedCity
                    current.copy(
                        settings = savedSettings,
                        selectedCity = nextCity,
                        activeThemePhase = SolarScheduleCalculator.resolveEffectiveThemePhase(
                            themeMode = savedSettings.themeMode,
                            sunriseTime = current.weather.sunriseTime,
                            sunsetTime = current.weather.sunsetTime,
                            longitude = nextCity.longitude,
                        ),
                    ).also {
                        if (shouldSwitchToPreset && matchedPreset != null) {
                            refreshTelemetryFor(matchedPreset)
                        }
                    }
                }
            }
        }
    }

    fun syncLocationPermissionState() {
        val granted = locationTracker.hasLocationPermission()
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                locationPermissionDenied = if (granted) false else it.locationPermissionDenied,
            )
        }
        if (granted && !_uiState.value.isGpsActive && !_uiState.value.isLocatingGps) {
            fetchWeatherForCurrentGps()
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                locationPermissionDenied = !granted,
                gpsErrorMessage = null,
            )
        }
        if (granted) {
            fetchWeatherForCurrentGps()
        }
    }

    fun fetchWeatherForCurrentGps() {
        if (!locationTracker.hasLocationPermission()) {
            _uiState.update { it.copy(hasLocationPermission = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLocatingGps = true,
                    gpsErrorMessage = null,
                    isCityModalOpen = false,
                )
            }
            val privacyMasking = _uiState.value.settings.gpsPrivacyMode
            val gpsResult = locationTracker.fetchCurrentCoordinates(privacyMasking = privacyMasking)
            gpsResult.onSuccess { resolved ->
                val maskedCoords = LocationMaskingUtility.maskCoordinates(
                    latitude = resolved.latitude,
                    longitude = resolved.longitude,
                    enabled = privacyMasking,
                )
                val coordLabel = LocationTracker.formatPrivateZoneLabel(maskedCoords.latitude, maskedCoords.longitude)
                val gpsCity = CityLocation(
                    id = "gps_current",
                    name = resolved.localityName,
                    country = resolved.countryName,
                    latitude = maskedCoords.latitude,
                    longitude = maskedCoords.longitude,
                    summaryCondition = "Protected Zone",
                    summaryTempC = _uiState.value.weather.currentTempC,
                    isCurrent = true,
                )
                settingsStore.setSelectedCityId("gps_current")
                settingsStore.saveEncryptedGpsCoordinates(
                    latitude = maskedCoords.latitude,
                    longitude = maskedCoords.longitude,
                    localityName = resolved.localityName,
                    countryName = resolved.countryName,
                    zoneLabel = coordLabel,
                )
                _uiState.update {
                    it.copy(
                        isLocatingGps = false,
                        isGpsActive = true,
                        gpsCoordinatesLabel = coordLabel,
                        gpsErrorMessage = null,
                    )
                }
                refreshTelemetryFor(gpsCity, preserveGpsActive = true)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLocatingGps = false,
                        gpsErrorMessage = error.message ?: "Could not acquire coarse location.",
                    )
                }
            }
        }
    }

    fun refreshLiveTelemetryNow() {
        refreshTelemetryFor(_uiState.value.selectedCity, preserveGpsActive = _uiState.value.isGpsActive)
    }

    fun refreshTelemetryFor(city: CityLocation, preserveGpsActive: Boolean = false) {
        viewModelScope.launch {
            val maskCoords = _uiState.value.settings.gpsPrivacyMode
            val maskedCity = LocationMaskingUtility.maskCityForApi(city, enabled = maskCoords)
            val preloadedCache = repository.getCachedWeatherSnapshot(maskedCity.id)
            val preloadedSeismic = repository.getCachedSeismicSnapshot()

            _uiState.update {
                it.copy(
                    isSyncing = true,
                    selectedCity = maskedCity,
                    isGpsActive = preserveGpsActive,
                    gpsCoordinatesLabel = if (preserveGpsActive) it.gpsCoordinatesLabel else null,
                    weather = preloadedCache?.weather ?: repository.defaultWeatherFor(maskedCity),
                    airQuality = preloadedCache?.airQuality ?: it.airQuality,
                    weatherMapStations = preloadedCache?.stations?.takeIf { s -> s.isNotEmpty() } ?: it.weatherMapStations,
                    seismic = preloadedSeismic?.seismic ?: it.seismic,
                )
            }
            val weatherDeferred = async {
                repository.fetchLiveWeatherWithCache(maskedCity, maskCoordinates = maskCoords)
            }
            val aqiDeferred = async {
                repository.fetchLiveAirQuality(maskedCity, maskCoordinates = maskCoords)
            }
            val seismicDeferred = async { repository.fetchRecentEarthquakesWithCache() }
            val stationsDeferred = async {
                runCatching { repository.fetchWeatherMapStations(maskedCity, maskCoordinates = maskCoords) }
            }
            val radarDeferred = async { repository.fetchRainViewerTilePrefix() }

            val weatherOutcome = weatherDeferred.await().getOrNull()
            val liveAqi = aqiDeferred.await().getOrNull()
            val seismicOutcome = seismicDeferred.await().getOrNull()
            val liveStations = stationsDeferred.await().getOrNull()?.takeIf { it.isNotEmpty() }
            val radarPrefix = radarDeferred.await()

            val nextWeather = weatherOutcome?.data ?: preloadedCache?.weather ?: _uiState.value.weather
            val nextAqi = liveAqi ?: preloadedCache?.airQuality ?: _uiState.value.airQuality
            val nextSeismic = seismicOutcome?.data ?: preloadedSeismic?.seismic ?: _uiState.value.seismic
            val nextStations = liveStations ?: preloadedCache?.stations?.takeIf { it.isNotEmpty() }
                ?: _uiState.value.weatherMapStations

            val hasLiveWeather = weatherOutcome != null && !weatherOutcome.fromCache
            val hasLiveSeismic = seismicOutcome != null && !seismicOutcome.fromCache
            if (hasLiveWeather) {
                repository.saveWeatherBundleToCache(
                    cityId = maskedCity.id,
                    weather = nextWeather,
                    airQuality = liveAqi,
                    stations = liveStations,
                    fetchedAtMillis = weatherOutcome!!.fetchedAtMillis,
                )
                settingsStore.setOfflineCacheLabel(repository.formatCurrentTimestamp())
            }

            val offlineFallback = !hasLiveWeather || !hasLiveSeismic
            val oldestCacheMillis = weatherOutcome?.takeIf { it.fromCache }?.fetchedAtMillis
                ?: seismicOutcome?.takeIf { it.fromCache }?.fetchedAtMillis
                ?: preloadedCache?.fetchedAtMillis
                ?: preloadedSeismic?.fetchedAtMillis

            _uiState.update { state ->
                val updatedPresets = state.presetCities.map { preset ->
                    if (!preserveGpsActive && preset.id == maskedCity.id) {
                        preset.copy(
                            summaryCondition = nextWeather.conditionLabel,
                            summaryTempC = nextWeather.currentTempC,
                            isCurrent = true,
                        )
                    } else {
                        preset.copy(isCurrent = false)
                    }
                }
                state.copy(
                    isSyncing = false,
                    isOfflineMode = offlineFallback,
                    selectedCity = nextWeather.city,
                    presetCities = updatedPresets,
                    weather = nextWeather,
                    weatherMapStations = nextStations,
                    rainViewerTilePrefix = radarPrefix ?: state.rainViewerTilePrefix,
                    airQuality = nextAqi,
                    seismic = nextSeismic,
                    securityPosture = SecurityGuard.currentPosture(),
                    activeThemePhase = SolarScheduleCalculator.resolveEffectiveThemePhase(
                        themeMode = state.settings.themeMode,
                        sunriseTime = nextWeather.sunriseTime,
                        sunsetTime = nextWeather.sunsetTime,
                        longitude = nextWeather.city.longitude,
                    ),
                    lastUpdatedLabel = if (offlineFallback && oldestCacheMillis != null) {
                        repository.formatOfflineCacheTimestamp(oldestCacheMillis)
                    } else if (offlineFallback) {
                        "Offline · Cached Fallback"
                    } else {
                        repository.formatLiveClockTimestamp()
                    },
                )
            }

            if (hasLiveSeismic || hasLiveWeather) {
                evaluateDisasterConditions(
                    city = nextWeather.city,
                    weather = nextWeather,
                    airQuality = nextAqi,
                    seismic = nextSeismic,
                )
            }
        }
    }

    fun toggleMetricCard(card: DashboardMetricCard) {
        _uiState.update { current ->
            val next = if (current.expandedMetricCard == card) null else card
            current.copy(expandedMetricCard = next)
        }
    }

    fun openCityModal() {
        _uiState.update {
            it.copy(
                isCityModalOpen = true,
                searchQuery = "",
                searchResults = it.presetCities,
            )
        }
    }

    fun closeCityModal() {
        _uiState.update { it.copy(isCityModalOpen = false) }
    }

    fun openSosModal() {
        _uiState.update { it.copy(isSosModalOpen = true) }
    }

    fun closeSosModal() {
        _uiState.update { it.copy(isSosModalOpen = false) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(200)
            val results = repository.searchCities(query)
            _uiState.update { it.copy(searchResults = results) }
        }
    }

    fun selectCity(city: CityLocation) {
        _uiState.update { it.copy(isCityModalOpen = false, isGpsActive = false, gpsCoordinatesLabel = null) }
        viewModelScope.launch {
            settingsStore.setSelectedCityId(city.id)
        }
        refreshTelemetryFor(city, preserveGpsActive = false)
    }

    fun selectStratum(stratum: AtmosphereStratum) {
        _uiState.update { it.copy(selectedStratum = stratum) }
    }

    fun selectRadarLayer(layer: RadarLayer) {
        _uiState.update { it.copy(selectedRadarLayer = layer) }
    }

    fun selectWeatherMapLayer(layer: WeatherMapLayer) {
        _uiState.update { it.copy(selectedWeatherMapLayer = layer) }
    }

    fun setRadarPlaybackOffset(minutes: Int) {
        _uiState.update { it.copy(radarPlaybackOffsetMinutes = minutes.coerceIn(-60, 30)) }
    }

    fun selectAqiLayer(layer: AqiMapLayer) {
        _uiState.update { it.copy(selectedAqiLayer = layer) }
    }

    fun selectAqiSector(sectorId: String) {
        _uiState.update { it.copy(selectedAqiSectorId = sectorId) }
    }

    fun setSeismicPWaveAlert(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setSeismicPWaveAlert(enabled) }
    }

    fun setTyphoonFlashWatch(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setTyphoonFlashWatch(enabled) }
    }

    fun setAqiSmogInversion(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setAqiSmogInversion(enabled) }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch { settingsStore.setTemperatureUnit(unit) }
    }

    fun setWindUnit(unit: WindUnit) {
        viewModelScope.launch { settingsStore.setWindUnit(unit) }
    }

    fun setPressureUnit(unit: PressureUnit) {
        viewModelScope.launch { settingsStore.setPressureUnit(unit) }
    }

    fun setGyroParallaxEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setGyroParallaxEnabled(enabled) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { current ->
            val updatedSettings = current.settings.copy(themeMode = mode)
            current.copy(
                settings = updatedSettings,
                activeThemePhase = SolarScheduleCalculator.resolveEffectiveThemePhase(
                    themeMode = mode,
                    sunriseTime = current.weather.sunriseTime,
                    sunsetTime = current.weather.sunsetTime,
                    longitude = current.selectedCity.longitude,
                ),
            )
        }
        viewModelScope.launch { settingsStore.setThemeMode(mode) }
    }

    fun setGpsPrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setGpsPrivacyMode(enabled)
            if (_uiState.value.isGpsActive) {
                fetchWeatherForCurrentGps()
            }
        }
    }

    fun setBackgroundSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setBackgroundSyncEnabled(enabled)
        }
        val appContext = getApplication<Application>().applicationContext
        runCatching {
            if (enabled) {
                val profile = WeatherSyncScheduler.schedulePeriodicWeatherSync(appContext)
                _uiState.update { it.copy(syncBatteryProfile = profile) }
            } else {
                WeatherSyncScheduler.cancelPeriodicWeatherSync(appContext)
            }
        }
    }

    fun refreshOfflineCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(cacheRefreshing = true) }
            refreshTelemetryFor(_uiState.value.selectedCity, preserveGpsActive = _uiState.value.isGpsActive)
            delay(450)
            val stamp = repository.formatCurrentTimestamp()
            settingsStore.setOfflineCacheLabel(stamp)
            _uiState.update { it.copy(cacheRefreshing = false) }
        }
    }

    private fun evaluateDisasterConditions(
        city: CityLocation,
        weather: WeatherTelemetry,
        airQuality: AirQualityTelemetry,
        seismic: SeismicTelemetry,
    ) {
        val alert = disasterManager.evaluateTelemetryForDisaster(
            city = city,
            weather = weather,
            airQuality = airQuality,
            seismic = seismic,
            settings = _uiState.value.settings,
        ) ?: return

        if (disasterManager.notifyIfNew(alert)) {
            _uiState.update { it.copy(activeDisasterAlert = alert, isSirenPlaying = true) }
            sirenPlayer.playSiren(kind = alert.kind, cycles = 2) {
                _uiState.update { it.copy(isSirenPlaying = false) }
            }
        }
    }

    fun triggerSampleDisasterAlert(kind: DisasterKind) {
        val sample = disasterManager.buildSampleEvent(kind, _uiState.value.selectedCity)
        disasterManager.notifyIfNew(sample, force = true)
        _uiState.update { it.copy(activeDisasterAlert = sample, isSirenPlaying = true) }
        sirenPlayer.playSiren(kind = kind, cycles = 2) {
            _uiState.update { it.copy(isSirenPlaying = false) }
        }
    }

    fun stopDisasterSiren() {
        sirenPlayer.stopSiren()
        _uiState.update { it.copy(isSirenPlaying = false) }
    }

    fun dismissActiveDisasterAlert() {
        sirenPlayer.stopSiren()
        _uiState.update { it.copy(activeDisasterAlert = null, isSirenPlaying = false) }
    }

    fun startSeismicAccelerometerMonitoring() {
        val sensor = accelerometerSensor
        if (sensor != null && !isAccelerometerRegistered) {
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
            isAccelerometerRegistered = true
        }
        _uiState.update { it.copy(seismicSensorMonitoringEnabled = true) }
    }

    fun stopSeismicAccelerometerMonitoring() {
        if (isAccelerometerRegistered) {
            sensorManager?.unregisterListener(this)
            isAccelerometerRegistered = false
        }
        seismicDetector.reset()
        _uiState.update { it.copy(seismicSensorMonitoringEnabled = false, liveAccelerometerMs2 = 0f) }
    }

    fun setSeismicSensorMonitoring(enabled: Boolean) {
        if (enabled) {
            startSeismicAccelerometerMonitoring()
        } else {
            stopSeismicAccelerometerMonitoring()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        if (!_uiState.value.seismicSensorMonitoringEnabled) return
        onAccelerometerSample(
            rawX = event.values.getOrElse(0) { 0f },
            rawY = event.values.getOrElse(1) { 0f },
            rawZ = event.values.getOrElse(2) { 0f },
            timestampNanos = event.timestamp,
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    fun onAccelerometerSample(
        rawX: Float,
        rawY: Float,
        rawZ: Float,
        timestampNanos: Long = System.nanoTime(),
    ) {
        if (!_uiState.value.seismicSensorMonitoringEnabled) return
        val result = seismicDetector.onAccelerometerReading(rawX, rawY, rawZ, timestampNanos)

        val nowMs = System.currentTimeMillis()
        if (result.patternRecognized) {
            val activeCity = _uiState.value.selectedCity
            val alert = DisasterAlertEvent(
                signature = "accel_seismic_${nowMs / 15_000L}",
                kind = DisasterKind.EARTHQUAKE,
                title = "Seismic Vibration Detected",
                message = String.format(
                    Locale.US,
                    "Device accelerometer detected oscillatory tremor (%.1f m/s²) in %s.",
                    result.peakMagnitudeMs2,
                    activeCity.name,
                ),
                guidance = "Drop, Cover under sturdy furniture, and Hold On.",
                distanceKm = 0,
            )
            disasterManager.notifyIfNew(alert, force = true)
            _uiState.update {
                it.copy(
                    liveAccelerometerMs2 = result.linearMagnitudeMs2,
                    seismicPatternDetectedCount = it.seismicPatternDetectedCount + 1,
                    activeDisasterAlert = alert,
                    isSirenPlaying = true,
                )
            }
            sirenPlayer.playSiren(kind = DisasterKind.EARTHQUAKE, cycles = 2) {
                _uiState.update { state -> state.copy(isSirenPlaying = false) }
            }
        } else if (nowMs - lastSensorUiEmitMs >= 140L) {
            lastSensorUiEmitMs = nowMs
            _uiState.update { it.copy(liveAccelerometerMs2 = result.linearMagnitudeMs2) }
        }
    }

    override fun onCleared() {
        stopSeismicAccelerometerMonitoring()
        sirenPlayer.stopSiren()
        if (isBatteryReceiverRegistered) {
            runCatching {
                getApplication<Application>().applicationContext.unregisterReceiver(batteryStateReceiver)
            }
            isBatteryReceiverRegistered = false
        }
        super.onCleared()
    }
}

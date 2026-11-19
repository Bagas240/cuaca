package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.location.LocationMaskingUtility
import com.example.data.location.LocationTracker
import com.example.data.model.AirQualityTelemetry
import com.example.data.model.AqiMapLayer
import com.example.data.model.AtmosphereStratum
import com.example.data.model.CityLocation
import com.example.data.model.PressureUnit
import com.example.data.model.RadarLayer
import com.example.data.model.RadarTelemetry
import com.example.data.model.SeismicTelemetry
import com.example.data.model.TemperatureUnit
import com.example.data.model.UserSettings
import com.example.data.model.WeatherMapLayer
import com.example.data.model.WeatherMapStation
import com.example.data.model.WeatherTelemetry
import com.example.data.model.WindUnit
import com.example.data.preferences.SettingsDataStore
import com.example.data.repository.VeridiaRepository
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
)

class VeridiaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VeridiaRepository.create(application.applicationContext)
    private val settingsStore = SettingsDataStore(application.applicationContext)
    private val locationTracker = LocationTracker(application.applicationContext)

    private val initialCity = repository.presetCities.first()

    private val _uiState = MutableStateFlow(
        VeridiaUiState(
            selectedCity = initialCity,
            presetCities = repository.presetCities,
            searchResults = repository.presetCities,
            weather = repository.defaultWeatherFor(initialCity),
            weatherMapStations = repository.defaultWeatherMapStations(),
            airQuality = repository.defaultAirQuality(initialCity),
            seismic = repository.defaultSeismicTelemetry(),
            radar = repository.defaultRadarTelemetry(),
            hasLocationPermission = locationTracker.hasLocationPermission(),
        )
    )
    val uiState: StateFlow<VeridiaUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        observeSettings()
        observeRoomTelemetryCache()
        if (locationTracker.hasLocationPermission()) {
            fetchWeatherForCurrentGps()
        } else {
            refreshTelemetryFor(initialCity)
        }
        startRealtimeAutoRefresh()
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
                delay(60_000L) // Refresh BMKG seismic alerts & radar every 60 seconds
                tickCount += 1
                val activeCity = _uiState.value.selectedCity
                val maskCoords = _uiState.value.settings.gpsPrivacyMode
                val seismicDeferred = async { repository.fetchRecentEarthquakesWithCache() }
                val radarDeferred = async { repository.fetchRainViewerTilePrefix() }

                val shouldRefreshWeather = tickCount % 3 == 0 // Every 3 minutes refresh weather, AQI & BMKG stations
                val weatherDeferred = if (shouldRefreshWeather) {
                    async { repository.fetchLiveWeatherWithCache(activeCity, maskCoordinates = maskCoords) }
                } else null
                val aqiDeferred = if (shouldRefreshWeather) {
                    async { repository.fetchLiveAirQuality(activeCity, maskCoordinates = maskCoords) }
                } else null
                val stationsDeferred = if (shouldRefreshWeather) {
                    async { runCatching { repository.fetchWeatherMapStations(activeCity, maskCoordinates = maskCoords) } }
                } else null

                val seismicResult = seismicDeferred.await()
                val radarPrefix = radarDeferred.await()
                val weatherResult = weatherDeferred?.await()
                val aqiResult = aqiDeferred?.await()
                val stationsResult = stationsDeferred?.await()

                val seismicOutcome = seismicResult.getOrNull()
                val weatherOutcome = weatherResult?.getOrNull()
                val liveAqi = aqiResult?.getOrNull()
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
                    state.copy(
                        seismic = seismicOutcome?.data ?: state.seismic,
                        rainViewerTilePrefix = radarPrefix ?: state.rainViewerTilePrefix,
                        weather = weatherOutcome?.data ?: state.weather,
                        airQuality = liveAqi ?: state.airQuality,
                        weatherMapStations = liveStations ?: state.weatherMapStations,
                        isOfflineMode = isOfflineFallback,
                        lastUpdatedLabel = if (isOfflineFallback && cachedStamp != null) {
                            repository.formatOfflineCacheTimestamp(cachedStamp)
                        } else if (!isOfflineFallback) {
                            repository.formatLiveClockTimestamp()
                        } else {
                            state.lastUpdatedLabel
                        },
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
                    current.copy(
                        settings = savedSettings,
                        selectedCity = if (shouldSwitchToPreset) matchedPreset!! else current.selectedCity,
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
                    lastUpdatedLabel = if (offlineFallback && oldestCacheMillis != null) {
                        repository.formatOfflineCacheTimestamp(oldestCacheMillis)
                    } else if (offlineFallback) {
                        "Offline · Cached Fallback"
                    } else {
                        repository.formatLiveClockTimestamp()
                    },
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

    fun setGpsPrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setGpsPrivacyMode(enabled)
            if (_uiState.value.isGpsActive) {
                fetchWeatherForCurrentGps()
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
}

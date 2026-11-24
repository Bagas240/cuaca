package com.example.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import com.example.alerts.DisasterKind
import com.example.data.model.AppLanguage
import com.example.data.model.SolarThemePhase
import com.example.data.model.WeatherConditionKind
import com.example.work.SyncBatteryTier
import java.util.Locale

@Immutable
interface VeridiaStrings {
    val language: AppLanguage

    // Bottom Dock & Common
    val navHome: String
    val navWeather: String
    val navAir: String
    val navAlerts: String
    val navSettings: String
    val cdBack: String
    val cdSelectCity: String
    val cdOpenRadar: String
    val cdSearchLocations: String
    val cdRefreshLiveTelemetry: String
    val cdRefreshGps: String
    val dismissAlert: String
    val silenceSirenPrefix: String

    // Live / Cache / Data Attribution
    val liveBadge: String
    val cachedBadge: String
    val offlineCachedFallback: String
    val offlineRoomCacheCd: String
    val syncingDots: String
    val minAgoShort: String
    val hoursAgoShort: String
    val daysAgoShort: String
    val sourcePrefix: String

    // Hero & Home Dashboard
    val feelsLikePrefix: String
    val heroFeelsWord: String
    val temperatureTileTitle: String
    val humidityTileTitle: String
    val windSpeedTileTitle: String
    val airQualityTileTitle: String
    val toggleTemperatureCd: String
    val toggleHumidityCd: String
    val toggleWindCd: String
    val highShort: String
    val lowShort: String
    val uvShort: String
    val fullAtmosphereLink: String
    val humidityHumid: String
    val humidityBalanced: String
    val humidityDry: String
    val dewPointLabel: String
    val precipChanceLabel: String
    val solarAndMoistureLink: String
    val pressurePrefix: String
    val gustsPrefix: String
    val windRadarLink: String
    val livePrecipitationTrackerTitle: String
    val livePrecipitationTrackerSubtitle: String
    val sevenDayForecastUpper: String
    val fullDetailsLink: String
    val inYourAreaBadge: String
    val nearYourAreaKm: (Int) -> String
    val detailsButton: String

    // Home GPS Card & Locations Modal
    val gpsPromptTitle: String
    val gpsPromptBody: String
    val gpsDeniedBody: String
    val gpsEnableCta: String
    val gpsRetryCta: String
    val gpsOpenSettings: String
    val gpsAcquiringZone: String
    val gpsProtectedZonePrefix: String
    val gpsViewingPrefix: String
    val gpsPrivacyProtectedCd: String
    val gpsPrivacyMaskedFallback: String
    val gpsButtonLabel: String
    val gpsErrorCouldNotAcquire: String
    val gpsErrorPlayServices: String
    val locationsTitle: String
    val closeCitiesCd: String
    val usePrivacyProtectedZone: String
    val acquiringMaskedZone: String
    val maskedGridHint: String
    val tapToAllowApprox: String
    val searchCityPlaceholder: String
    val currentSelectionPrefix: String

    // Hourly 24h Card
    val hourlyForecastTitle: String
    val next24HoursBadge: String
    val nowLabel: String

    // Solar Theme Switcher
    val adaptiveSolarThemeTitle: String
    val activeSolarThemeCd: String
    val autoLabel: String
    val manualLabel: String
    val modeAuto: String
    val modeDawn: String
    val modeDay: String
    val modeDusk: String
    val modeNight: String
    val themeModeCdSuffix: String
    val phaseLabelSunriseSunset: String
    val phaseLabelDay: String
    val phaseLabelAfternoonDusk: String
    val phaseLabelNight: String

    // Sunrise, Sunset & Moon Phase Card
    val sunriseSunsetTitle: String
    val sunriseSunsetCd: String
    val todayLabel: String
    val tomorrowLabel: String
    val dayAfterTomorrowLabel: String
    val currentGpsZoneCd: String
    val selectedCityCd: String
    val sunriseLabel: String
    val sunsetLabel: String
    val sunriseTimeCd: String
    val sunsetTimeCd: String
    val moonPhaseTitle: String
    val litSuffix: String
    val solarNoonLabel: String
    val sunCycleTitle: String
    val sunCycleDaylightSuffix: String
    val nextSunriseIn: (String) -> String
    val nextSunsetIn: (String) -> String

    // Detailed Weather Screen
    val weatherAtmosphereTitle: String
    val openRadarCd: String
    val radarButton: String
    val realTimeAtmosphereBadge: String
    val surfaceTab: String
    val midCloudTab: String
    val upperStratumTab: String
    val atmosphereStratumUpper: String
    val altitudeSurface: String
    val altitudeMidCloud: String
    val altitudeUpper: String
    val strataRangeSurface: String
    val strataRangeMidCloud: String
    val strataRangeUpper: String
    val upperStratosphereTitle: String
    val midTroposphereTitle: String
    val groundBoundaryTitle: String
    val windUpper: String
    val gustsLabel: String
    val uvIndexUpper: String
    val uvPeakWindowText: String
    val humidityUpper: String
    val dewPointSentence: (String) -> String
    val pressureUpper: String
    val referenceLabel: String
    val referenceStdValue: String
    val forecast7DayTitle: String
    val next168HoursLabel: String
    val tapDayForDetailsHint: String
    val detailConditionLabel: String
    val detailRainfallLabel: String
    val detailRainChanceLabel: String
    val detailWindMaxLabel: String
    val detailHumidityLabel: String
    val detailUvPeakLabel: String
    val detailTempRangeLabel: String

    // Wind Indicator
    val windCalm: String
    val windLightBreeze: String
    val windGentleBreeze: String
    val windModerateBreeze: String
    val windFreshBreeze: String
    val windStrongWind: String
    val cdWindIndicator: (String, String, String, Int, String) -> String

    // Weather Interactive Map (osmdroid) & Radar Screen
    val mapLayerRainRadar: String
    val mapLayerWindVector: String
    val mapLayerCloudsTemp: String
    val refreshPrecipitationMapCd: String
    val osmPrecipPrefix: String
    val openFullRadarCd: String
    val zoomInMapCd: String
    val zoomOutMapCd: String
    val recenterMapCd: String
    val legendLight: String
    val presetRegion: String
    val presetMetro: String
    val presetLocal: String
    val stationPrecipLabel: String
    val stationWindLabel: String
    val weatherRadarTitle: String
    val weatherRadarSubtitle: String
    val liveSweepLabel: String
    val radarTabRain: String
    val radarTabWind: String
    val radarTabSatellite: String
    val activePrecipCellUpper: String
    val dbzRainCoreLabel: String
    val radarSummaryFallback: String
    val cellSpeedLabel: String
    val echoTopLabel: String
    val accumulationLabel: String
    val surfaceRainUpper: String
    val moderateSteadyRain: String
    val next60mLabel: String
    val cellVectorUpper: String
    val velocityLabel: String
    val reflectivityUpper: String
    val dopplerBandEcho: String
    val hailRiskLabel: String
    val irCloudTopUpper: String
    val deepConvectiveDeck: String
    val satelliteLabel: String
    val radarPlaybackTrackUpper: String
    val liveNowLabel: String
    val minShort: String

    // Air Quality Map & Screen
    val airQualitySmogTitle: String
    val aerosolSensorArray: String
    val sensorsLiveSuffix: String
    val regionalAqiUpper: String
    val aqiScale0Good: String
    val aqiScale50Mod: String
    val aqiScale150Poor: String
    val aqiScale300Alert: String
    val pm25ParticlesUpper: String
    val pm25WhoThreshold: String
    val pm10CoarseUpper: String
    val withinSafetyBand: String
    val carbonMonoxideUpper: String
    val normalUrbanBase: String
    val nitrogenDioxideUpper: String
    val trafficCorridor: String
    val healthActivityProtocols: String
    val wearMaskTitle: String
    val wearMaskSubtitle: String
    val limitCardioTitle: String
    val limitCardioSubtitle: String
    val closeVentsTitle: String
    val closeVentsSubtitle: String
    val districtComparisonTitle: String
    val keySectorsSuffix: String
    val aqiLayerUsAqi: String
    val refreshAqiMapCd: String
    val aqiMapSuffix: String
    val zoomInLabel: String
    val zoomOutLabel: String
    val recenterLabel: String

    // Disaster Monitor & BMKG Shakemap
    val disasterMonitorTitle: String
    val emergencySosCd: String
    val sosHelpButton: String
    val depthLabel: String
    val peakPgaLabel: String
    val tsunamiLabel: String
    val civilDefenseSirenUpper: String
    val zoneEarlyWarningSubtitle: String
    val sirenActiveBadge: String
    val armedBadge: String
    val sirenDescriptionForCity: (String) -> String
    val disasterEarthquakeChip: String
    val disasterTsunamiChip: String
    val disasterStormChip: String
    val disasterAirHazardChip: String
    val testSirenCd: String
    val backgroundAlertServiceUpper: String
    val scanNowButton: String
    val scanningLabel: String
    val silenceDisasterSirenButton: String
    val silenceDisasterSirenCd: String
    val accelerometerSeismicUpper: String
    val accelUnavailable: String
    val accelArmed: String
    val accelPaused: String
    val liveLinearVibrationLabel: String
    val detectionsLabel: String
    val accelMonitorsDesc: String
    val pWaveLeadUpper: String
    val sWaveShockUpper: String
    val tsunamiBuoyUpper: String
    val volcanicArrayUpper: String
    val primaryWaveElapsed: String
    val statusLabel: String
    val decayingLabel: String
    val feltStrongIndoors: String
    val structuralLabel: String
    val safeLabel: String
    val secUnit: String
    val mSurgeUnit: String
    val coastalMarineAdvisory: String
    val baysideLabel: String
    val restrictedLabel: String
    val normalLabel: String
    val tiltmeterLabel: String
    val recentSeismicActivity: String
    val past48HoursLabel: String
    val emergencySafetyProtocol: String
    val closeSosCd: String
    val immediateShakeResponse: String
    val sosChecklistBody: String
    val fireAmbulanceLabel: String
    val policeRescueLabel: String
    val acknowledgeChecklistButton: String
    val bmkgShakemapTab: String
    val radar2dMapTab: String
    val refreshBmkgSeismicCd: String
    val autoUpdatingPrefix: String
    val dragIslandsHint: String

    // Settings Screen
    val settingsTitle: String
    val appLanguageCardTitle: String
    val appLanguageCardSubtitle: String
    val languageEnglishOption: String
    val languageIndonesianOption: String
    val gpsPrivacySyncTitle: String
    val gpsPrivacyShieldTitle: String
    val gpsPrivacyShieldSubtitle: String
    val powerAwareSyncTitle: String
    val realTimeTelemetryStream: String
    val adaptiveWorkManagerPrefix: String
    val encryptionHardeningTitle: String
    val secSettingsAndGps: String
    val secRoomDbCache: String
    val secDomainAllowlist: String
    val secCoarseGpsGuard: String
    val secBinaryIntegrity: String
    val secEncryptedPrefs: String
    val secAesEncrypted: String
    val secKeystoreEncrypted: String
    val secStandard: String
    val secHttpsOnlyBlocked: (Int) -> String
    val secCoarseEnforced: String
    val secR8Hardened: String
    val emergencyAlertsTitle: String
    val seismicPWaveAlertTitle: String
    val seismicPWaveAlertSubtitle: String
    val typhoonFlashWatchTitle: String
    val typhoonFlashWatchSubtitle: String
    val aqiSmogInversionTitle: String
    val aqiSmogInversionSubtitle: String
    val unitsTelemetryTitle: String
    val temperatureUnitTitle: String
    val currentScalePrefix: String
    val celsiusName: String
    val fahrenheitName: String
    val tempMetricOption: String
    val tempImperialOption: String
    val windVelocityTitle: String
    val pressureScaleTitle: String
    val displayParallaxTitle: String
    val gyroParallaxTitle: String
    val gyroParallaxSubtitle: String
    val glassRefractionTitle: String
    val glassRefractionSubtitle: String
    val glassRefractionValue: String
    val offlineDisasterCacheTitle: String
    val cacheActiveSize: String
    val faultLineVectorsLabel: String
    val emergencyShelterMarkersLabel: String
    val offlineReadyLabel: String
    val refreshOfflineCacheButton: String
    val syncingRegionalCache: String
}

private object EnglishStrings : VeridiaStrings {
    override val language = AppLanguage.ENGLISH
    override val navHome = "Home"
    override val navWeather = "Weather"
    override val navAir = "Air"
    override val navAlerts = "Alerts"
    override val navSettings = "Settings"
    override val cdBack = "Back"
    override val cdSelectCity = "Select city"
    override val cdOpenRadar = "Open weather radar"
    override val cdSearchLocations = "Search locations"
    override val cdRefreshLiveTelemetry = "Refresh Live Telemetry"
    override val cdRefreshGps = "Refresh GPS"
    override val dismissAlert = "Dismiss alert"
    override val silenceSirenPrefix = "Silence Disaster Siren"

    override val liveBadge = "LIVE"
    override val cachedBadge = "Cached"
    override val offlineCachedFallback = "Offline · Cached Fallback"
    override val offlineRoomCacheCd = "Offline Room Cache"
    override val syncingDots = "Syncing..."
    override val minAgoShort = "min ago"
    override val hoursAgoShort = "hours ago"
    override val daysAgoShort = "d ago"
    override val sourcePrefix = "Source"

    override val feelsLikePrefix = "Feels like"
    override val heroFeelsWord = "Feels"
    override val temperatureTileTitle = "TEMPERATURE"
    override val humidityTileTitle = "HUMIDITY"
    override val windSpeedTileTitle = "WIND SPEED"
    override val airQualityTileTitle = "AIR QUALITY"
    override val toggleTemperatureCd = "Toggle Temperature Details"
    override val toggleHumidityCd = "Toggle Humidity Details"
    override val toggleWindCd = "Toggle Wind Details"
    override val highShort = "H"
    override val lowShort = "L"
    override val uvShort = "UV"
    override val fullAtmosphereLink = "Full Atmosphere"
    override val humidityHumid = "Humid"
    override val humidityBalanced = "Balanced"
    override val humidityDry = "Dry"
    override val dewPointLabel = "Dew point"
    override val precipChanceLabel = "Precip chance"
    override val solarAndMoistureLink = "Solar & Moisture"
    override val pressurePrefix = "Pressure"
    override val gustsPrefix = "Gusts"
    override val windRadarLink = "Wind Radar"
    override val livePrecipitationTrackerTitle = "LIVE PRECIPITATION TRACKER"
    override val livePrecipitationTrackerSubtitle = "OpenStreetMap (osmdroid) · RainViewer & BMKG"
    override val sevenDayForecastUpper = "7-DAY FORECAST"
    override val fullDetailsLink = "Full Details"
    override val inYourAreaBadge = "In Your Area"
    override val nearYourAreaKm: (Int) -> String = { km -> "Near Your Area · $km km" }
    override val detailsButton = "Details"

    override val gpsPromptTitle = "Enable Coarse Zone Location"
    override val gpsPromptBody = "Allows Veridia to automatically track your regional regency weather & seismic radar."
    override val gpsDeniedBody = "Location permission was denied. Enable coarse location in Settings or tap retry."
    override val gpsEnableCta = "Allow Location"
    override val gpsRetryCta = "Retry GPS Access"
    override val gpsOpenSettings = "Open Settings"
    override val gpsAcquiringZone = "Acquiring coarse zone..."
    override val gpsProtectedZonePrefix = "Protected Zone GPS"
    override val gpsViewingPrefix = "Viewing"
    override val gpsPrivacyProtectedCd = "GPS Privacy Protected"
    override val gpsPrivacyMaskedFallback = "Privacy-masked (~9 km)"
    override val gpsButtonLabel = "GPS"
    override val gpsErrorCouldNotAcquire = "Could not acquire coarse location."
    override val gpsErrorPlayServices = "Google Play Services unavailable for location."
    override val locationsTitle = "Locations"
    override val closeCitiesCd = "Close Cities"
    override val usePrivacyProtectedZone = "Use Privacy-Protected Zone"
    override val acquiringMaskedZone = "Acquiring masked zone..."
    override val maskedGridHint = "Masked ~9 km grid · exact GPS never exposed"
    override val tapToAllowApprox = "Tap to allow approximate location"
    override val searchCityPlaceholder = "Search city or airport..."
    override val currentSelectionPrefix = "Current Selection"

    override val hourlyForecastTitle = "24-HOUR FORECAST"
    override val next24HoursBadge = "Next 24 Hours"
    override val nowLabel = "Now"

    override val adaptiveSolarThemeTitle = "ADAPTIVE SOLAR THEME"
    override val activeSolarThemeCd = "Active solar theme phase"
    override val autoLabel = "Auto"
    override val manualLabel = "Manual"
    override val modeAuto = "Auto"
    override val modeDawn = "Dawn"
    override val modeDay = "Day"
    override val modeDusk = "Dusk"
    override val modeNight = "Night"
    override val themeModeCdSuffix = "theme mode"
    override val phaseLabelSunriseSunset = "Sunrise/Sunset · Whitish Gold"
    override val phaseLabelDay = "Morning/Day · Warm Cream"
    override val phaseLabelAfternoonDusk = "Afternoon · Cool Blue"
    override val phaseLabelNight = "Night · Dark Mode"

    override val sunriseSunsetTitle = "SUNRISE & SUNSET"
    override val sunriseSunsetCd = "Sunrise and sunset schedule"
    override val todayLabel = "Today"
    override val tomorrowLabel = "Tomorrow"
    override val dayAfterTomorrowLabel = "Day After"
    override val currentGpsZoneCd = "Current GPS zone"
    override val selectedCityCd = "Selected city location"
    override val sunriseLabel = "Sunrise"
    override val sunsetLabel = "Sunset"
    override val sunriseTimeCd = "Sunrise time"
    override val sunsetTimeCd = "Sunset time"
    override val moonPhaseTitle = "MOON PHASE"
    override val litSuffix = "Lit"
    override val solarNoonLabel = "Solar Noon"
    override val sunCycleTitle = "SUN CYCLE"
    override val sunCycleDaylightSuffix = "Daylight"
    override val nextSunriseIn: (String) -> String = { time -> "Next Sunrise in $time" }
    override val nextSunsetIn: (String) -> String = { time -> "Sunset in $time" }

    override val weatherAtmosphereTitle = "Weather & Atmosphere"
    override val openRadarCd = "Open Radar"
    override val radarButton = "Radar"
    override val realTimeAtmosphereBadge = "REAL-TIME ATMOSPHERE"
    override val surfaceTab = "Surface"
    override val midCloudTab = "Mid-Cloud"
    override val upperStratumTab = "Upper Stratum"
    override val atmosphereStratumUpper = "ATMOSPHERE STRATUM"
    override val altitudeSurface = "Surface · 42m"
    override val altitudeMidCloud = "Mid-Cloud · 4,200m"
    override val altitudeUpper = "Stratosphere · 12,000m"
    override val strataRangeSurface = "0 - 1,200m Ground Level"
    override val strataRangeMidCloud = "1,200 - 8,000m Troposphere"
    override val strataRangeUpper = "8,000 - 15,000m Stratum"
    override val upperStratosphereTitle = "Upper Stratosphere"
    override val midTroposphereTitle = "Mid-Troposphere Rain Clouds"
    override val groundBoundaryTitle = "Ground Boundary & Precipitation"
    override val windUpper = "WIND"
    override val gustsLabel = "Gusts"
    override val uvIndexUpper = "UV INDEX"
    override val uvPeakWindowText = "Peak solar window 11:00 - 14:00"
    override val humidityUpper = "HUMIDITY"
    override val dewPointSentence: (String) -> String = { dew -> "Dew point is ${dew}°C right now" }
    override val pressureUpper = "PRESSURE"
    override val referenceLabel = "Reference"
    override val referenceStdValue = "1,013.25 Std"
    override val forecast7DayTitle = "7-Day Detailed Forecast"
    override val next168HoursLabel = "Today, Tomorrow & Week"
    override val tapDayForDetailsHint = "Tap any day to inspect detailed forecast"
    override val detailConditionLabel = "Condition"
    override val detailRainfallLabel = "Rainfall"
    override val detailRainChanceLabel = "Rain Chance"
    override val detailWindMaxLabel = "Max Wind"
    override val detailHumidityLabel = "Humidity"
    override val detailUvPeakLabel = "Peak UV"
    override val detailTempRangeLabel = "Thermal Range"

    override val windCalm = "Calm"
    override val windLightBreeze = "Light Breeze"
    override val windGentleBreeze = "Gentle Breeze"
    override val windModerateBreeze = "Moderate Breeze"
    override val windFreshBreeze = "Fresh Breeze"
    override val windStrongWind = "Strong Wind"
    override val cdWindIndicator: (String, String, String, Int, String) -> String = { spd, unit, dir, deg, cond ->
        "Wind $spd $unit from $dir ($deg degrees), $cond"
    }

    override val mapLayerRainRadar = "Rain Radar"
    override val mapLayerWindVector = "Wind Vector"
    override val mapLayerCloudsTemp = "Clouds & Temp"
    override val refreshPrecipitationMapCd = "Refresh BMKG Precipitation Map"
    override val osmPrecipPrefix = "OSM Precip"
    override val openFullRadarCd = "Open Full Precipitation Radar"
    override val zoomInMapCd = "Zoom in precipitation map"
    override val zoomOutMapCd = "Zoom out precipitation map"
    override val recenterMapCd = "Recenter precipitation map"
    override val legendLight = "Light"
    override val presetRegion = "Region"
    override val presetMetro = "Metro"
    override val presetLocal = "Local"
    override val stationPrecipLabel = "Precip"
    override val stationWindLabel = "Wind"
    override val weatherRadarTitle = "Weather Radar"
    override val weatherRadarSubtitle = "Doppler Precipitation & Satellite Array"
    override val liveSweepLabel = "Live Sweep"
    override val radarTabRain = "Rain Doppler"
    override val radarTabWind = "Wind Stream"
    override val radarTabSatellite = "Satellite"
    override val activePrecipCellUpper = "ACTIVE PRECIPITATION CELL"
    override val dbzRainCoreLabel = "dBZ Rain Core"
    override val radarSummaryFallback = "Active convective rainband detected across regional telemetry stations."
    override val cellSpeedLabel = "Cell Speed"
    override val echoTopLabel = "Echo Top"
    override val accumulationLabel = "Accumulation"
    override val surfaceRainUpper = "SURFACE RAIN"
    override val moderateSteadyRain = "Moderate steady rain"
    override val next60mLabel = "Next 60m"
    override val cellVectorUpper = "CELL VECTOR"
    override val velocityLabel = "Velocity"
    override val reflectivityUpper = "REFLECTIVITY"
    override val dopplerBandEcho = "Doppler band echo"
    override val hailRiskLabel = "Hail Risk"
    override val irCloudTopUpper = "IR CLOUD TOP"
    override val deepConvectiveDeck = "Deep convective deck"
    override val satelliteLabel = "Satellite"
    override val radarPlaybackTrackUpper = "RADAR PLAYBACK TRACK"
    override val liveNowLabel = "Live (Now)"
    override val minShort = "min"

    override val airQualitySmogTitle = "Air Quality & Smog"
    override val aerosolSensorArray = "Aerosol Sensor Array"
    override val sensorsLiveSuffix = "Sensors Live"
    override val regionalAqiUpper = "REGIONAL AIR QUALITY INDEX"
    override val aqiScale0Good = "0 Good"
    override val aqiScale50Mod = "50 Moderate"
    override val aqiScale150Poor = "150 Poor"
    override val aqiScale300Alert = "300+ Alert"
    override val pm25ParticlesUpper = "PM2.5 PARTICLES"
    override val pm25WhoThreshold = "2.4x WHO Threshold"
    override val pm10CoarseUpper = "PM10 COARSE"
    override val withinSafetyBand = "Within Safety Band"
    override val carbonMonoxideUpper = "CARBON MONOXIDE"
    override val normalUrbanBase = "Normal Urban Base"
    override val nitrogenDioxideUpper = "NITROGEN DIOXIDE"
    override val trafficCorridor = "Traffic Corridor"
    override val healthActivityProtocols = "Health & Activity Protocols"
    override val wearMaskTitle = "Wear Mask"
    override val wearMaskSubtitle = "N95 for sensitive lungs"
    override val limitCardioTitle = "Limit Cardio"
    override val limitCardioSubtitle = "Reduce outdoor training"
    override val closeVentsTitle = "Close Vents"
    override val closeVentsSubtitle = "Engage HEPA purifiers"
    override val districtComparisonTitle = "District Comparison"
    override val keySectorsSuffix = "Key Sectors"
    override val aqiLayerUsAqi = "US AQI"
    override val refreshAqiMapCd = "Refresh AQI Map"
    override val aqiMapSuffix = "AQI"
    override val zoomInLabel = "Zoom In"
    override val zoomOutLabel = "Zoom Out"
    override val recenterLabel = "Recenter Map"

    override val disasterMonitorTitle = "Disaster Monitor"
    override val emergencySosCd = "Emergency SOS"
    override val sosHelpButton = "SOS Help"
    override val depthLabel = "Depth"
    override val peakPgaLabel = "Peak PGA"
    override val tsunamiLabel = "Tsunami"
    override val civilDefenseSirenUpper = "CIVIL-DEFENSE DISASTER SIREN"
    override val zoneEarlyWarningSubtitle = "Zone Early Warning & Notification"
    override val sirenActiveBadge = "SIREN ACTIVE"
    override val armedBadge = "ARMED"
    override val sirenDescriptionForCity: (String) -> String = { cityName ->
        "Automatically dispatches disaster-specific notifications & a sweeping civil-defense siren (non-ambulance) when earthquakes, tsunami advisories, extreme storms, or hazardous air strike $cityName."
    }
    override val disasterEarthquakeChip = "Earthquake"
    override val disasterTsunamiChip = "Tsunami"
    override val disasterStormChip = "Storm"
    override val disasterAirHazardChip = "Air Hazard"
    override val testSirenCd = "Test Siren"
    override val backgroundAlertServiceUpper = "BACKGROUND ALERT SERVICE"
    override val scanNowButton = "Scan Now"
    override val scanningLabel = "Scanning..."
    override val silenceDisasterSirenButton = "Silence Civil-Defense Siren"
    override val silenceDisasterSirenCd = "Silence Disaster Siren"
    override val accelerometerSeismicUpper = "ACCELEROMETER SEISMIC SENSOR"
    override val accelUnavailable = "Hardware Accelerometer Unavailable"
    override val accelArmed = "Active · Pattern Detector Armed"
    override val accelPaused = "Sensor Monitoring Paused"
    override val liveLinearVibrationLabel = "Live Linear Vibration"
    override val detectionsLabel = "Detections"
    override val accelMonitorsDesc = "Monitors sustained oscillatory tremor patterns (>=2.4 m/s² with axis reversals) to trigger instant siren alerts."
    override val pWaveLeadUpper = "P-WAVE LEAD"
    override val sWaveShockUpper = "S-WAVE SHOCK"
    override val tsunamiBuoyUpper = "TSUNAMI BUOY"
    override val volcanicArrayUpper = "VOLCANIC ARRAY"
    override val primaryWaveElapsed = "Primary wave elapsed"
    override val statusLabel = "Status"
    override val decayingLabel = "Decaying"
    override val feltStrongIndoors = "Felt strong indoors"
    override val structuralLabel = "Structural"
    override val safeLabel = "Safe"
    override val secUnit = "sec"
    override val mSurgeUnit = "m surge"
    override val coastalMarineAdvisory = "Coastal marine advisory"
    override val baysideLabel = "Bayside"
    override val restrictedLabel = "Restricted"
    override val normalLabel = "Normal"
    override val tiltmeterLabel = "Tiltmeter"
    override val recentSeismicActivity = "Recent Seismic Activity"
    override val past48HoursLabel = "Past 48 Hours"
    override val emergencySafetyProtocol = "Emergency Safety Protocol"
    override val closeSosCd = "Close SOS"
    override val immediateShakeResponse = "Immediate Shake Response"
    override val sosChecklistBody = "1. Drop, Cover under sturdy furniture, and Hold On.\n2. Keep away from glass windows and unsecured shelving.\n3. Avoid coastal bayside zones during Tsunami Advisory."
    override val fireAmbulanceLabel = "Fire / Ambulance"
    override val policeRescueLabel = "Police / Rescue"
    override val acknowledgeChecklistButton = "Acknowledge Safety Checklist"
    override val bmkgShakemapTab = "BMKG Shakemap"
    override val radar2dMapTab = "2D Radar Map"
    override val refreshBmkgSeismicCd = "Refresh BMKG Seismic Feed"
    override val autoUpdatingPrefix = "Auto-Updating"
    override val dragIslandsHint = "Drag islands · Tap M pin"

    override val settingsTitle = "Settings"
    override val appLanguageCardTitle = "App Language"
    override val appLanguageCardSubtitle = "Switch the entire app between English & full Bahasa Indonesia"
    override val languageEnglishOption = "English"
    override val languageIndonesianOption = "Bahasa Indonesia"
    override val gpsPrivacySyncTitle = "GPS Privacy & Real-Time Sync"
    override val gpsPrivacyShieldTitle = "GPS Location Privacy Shield"
    override val gpsPrivacyShieldSubtitle = "Locks telemetry to regency/kabupaten zone (~9-11 km); exact GPS discarded"
    override val powerAwareSyncTitle = "Power-Aware Background Sync"
    override val realTimeTelemetryStream = "Real-Time Telemetry Stream"
    override val adaptiveWorkManagerPrefix = "Adaptive WorkManager"
    override val encryptionHardeningTitle = "Encryption & Hardening"
    override val secSettingsAndGps = "User Settings & GPS Coordinates"
    override val secRoomDbCache = "Local Room Database Cache"
    override val secDomainAllowlist = "BMKG & Telemetry Domain Allowlist"
    override val secCoarseGpsGuard = "Coarse Regency GPS Guard"
    override val secBinaryIntegrity = "Binary & Runtime Integrity"
    override val secEncryptedPrefs = "EncryptedSharedPreferences · AES-256"
    override val secAesEncrypted = "AES Encrypted"
    override val secKeystoreEncrypted = "AndroidKeyStore AES-256 Encrypted"
    override val secStandard = "Standard"
    override val secHttpsOnlyBlocked: (Int) -> String = { count -> "HTTPS Only · $count Blocked" }
    override val secCoarseEnforced = "Enforced (~11 km · No Pinpoint)"
    override val secR8Hardened = "R8 Hardened · No Hook Detected"
    override val emergencyAlertsTitle = "Emergency Alerts"
    override val seismicPWaveAlertTitle = "Seismic P-Wave Alert"
    override val seismicPWaveAlertSubtitle = "Bypass silent mode for M 4.5+ shocks"
    override val typhoonFlashWatchTitle = "Typhoon Flash Watch"
    override val typhoonFlashWatchSubtitle = "Barometer drops and storm surge alerts"
    override val aqiSmogInversionTitle = "AQI Smog Inversion"
    override val aqiSmogInversionSubtitle = "Notify when PM2.5 exceeds 100 µg/m³"
    override val unitsTelemetryTitle = "Units & Telemetry Standards"
    override val temperatureUnitTitle = "Temperature Unit"
    override val currentScalePrefix = "Current scale"
    override val celsiusName = "Celsius"
    override val fahrenheitName = "Fahrenheit"
    override val tempMetricOption = "°C Metric"
    override val tempImperialOption = "°F Imperial"
    override val windVelocityTitle = "Wind Velocity"
    override val pressureScaleTitle = "Barometric Scale"
    override val displayParallaxTitle = "Display & Parallax"
    override val gyroParallaxTitle = "Gyroscope Camera Parallax"
    override val gyroParallaxSubtitle = "Tilt device to orbit 3D viewpoints"
    override val glassRefractionTitle = "Glass Refraction Depth"
    override val glassRefractionSubtitle = "Hardware accelerated blur"
    override val glassRefractionValue = "20px Clean"
    override val offlineDisasterCacheTitle = "Offline Disaster Cache"
    override val cacheActiveSize = "128 MB Active"
    override val faultLineVectorsLabel = "Fault line & topographic vectors"
    override val emergencyShelterMarkersLabel = "Emergency shelter GPS markers"
    override val offlineReadyLabel = "Offline Ready"
    override val refreshOfflineCacheButton = "Refresh Regional Offline Cache"
    override val syncingRegionalCache = "Syncing Regional Cache..."
}

private object IndonesianStrings : VeridiaStrings {
    override val language = AppLanguage.INDONESIAN
    override val navHome = "Beranda"
    override val navWeather = "Cuaca"
    override val navAir = "Udara"
    override val navAlerts = "Siaga"
    override val navSettings = "Setelan"
    override val cdBack = "Kembali"
    override val cdSelectCity = "Pilih kota"
    override val cdOpenRadar = "Buka radar cuaca"
    override val cdSearchLocations = "Cari lokasi"
    override val cdRefreshLiveTelemetry = "Perbarui Telemetri Langsung"
    override val cdRefreshGps = "Perbarui GPS"
    override val dismissAlert = "Tutup peringatan"
    override val silenceSirenPrefix = "Matikan Sirine Bencana"

    override val liveBadge = "LANGSUNG"
    override val cachedBadge = "Tersimpan"
    override val offlineCachedFallback = "Luring · Data Tersimpan"
    override val offlineRoomCacheCd = "Penyimpanan Luring Lokal"
    override val syncingDots = "Menyelaraskan..."
    override val minAgoShort = "mnt lalu"
    override val hoursAgoShort = "jam lalu"
    override val daysAgoShort = "hr lalu"
    override val sourcePrefix = "Sumber"

    override val feelsLikePrefix = "Terasa seperti"
    override val heroFeelsWord = "Terasa"
    override val temperatureTileTitle = "SUHU UDARA"
    override val humidityTileTitle = "KELEMBAPAN"
    override val windSpeedTileTitle = "ANGIN"
    override val airQualityTileTitle = "KUALITAS UDARA"
    override val toggleTemperatureCd = "Buka Rincian Suhu"
    override val toggleHumidityCd = "Buka Rincian Kelembapan"
    override val toggleWindCd = "Buka Rincian Angin"
    override val highShort = "T"
    override val lowShort = "R"
    override val uvShort = "UV"
    override val fullAtmosphereLink = "Atmosfer Lengkap"
    override val humidityHumid = "Lembap"
    override val humidityBalanced = "Seimbang"
    override val humidityDry = "Kering"
    override val dewPointLabel = "Titik embun"
    override val precipChanceLabel = "Peluang hujan"
    override val solarAndMoistureLink = "Surya & Kelembapan"
    override val pressurePrefix = "Tekanan"
    override val gustsPrefix = "Hembusan"
    override val windRadarLink = "Radar Angin"
    override val livePrecipitationTrackerTitle = "PELACAK CURAH HUJAN LANGSUNG"
    override val livePrecipitationTrackerSubtitle = "OpenStreetMap (osmdroid) · RainViewer & BMKG"
    override val sevenDayForecastUpper = "PRAKIRAAN 7 HARI"
    override val fullDetailsLink = "Selengkapnya"
    override val inYourAreaBadge = "Di Daerah Anda"
    override val nearYourAreaKm: (Int) -> String = { km -> "Dekat Daerah Anda · $km km" }
    override val detailsButton = "Rincian"

    override val gpsPromptTitle = "Aktifkan Lokasi Zona Wilayah"
    override val gpsPromptBody = "Memungkinkan Veridia melacak prakiraan cuaca dan radar gempa tingkat kabupaten/kota Anda."
    override val gpsDeniedBody = "Izin lokasi ditolak. Aktifkan lokasi perkiraan di Setelan atau tekan coba lagi."
    override val gpsEnableCta = "Izinkan Lokasi"
    override val gpsRetryCta = "Coba Akses GPS"
    override val gpsOpenSettings = "Buka Setelan"
    override val gpsAcquiringZone = "Menangkap zona wilayah..."
    override val gpsProtectedZonePrefix = "GPS Zona Terlindungi"
    override val gpsViewingPrefix = "Menampilkan"
    override val gpsPrivacyProtectedCd = "Privasi GPS Dilindungi"
    override val gpsPrivacyMaskedFallback = "Zona disamarkan (~9 km)"
    override val gpsButtonLabel = "GPS"
    override val gpsErrorCouldNotAcquire = "Tidak dapat menangkap lokasi zona wilayah."
    override val gpsErrorPlayServices = "Layanan lokasi perangkat tidak tersedia."
    override val locationsTitle = "Daftar Lokasi"
    override val closeCitiesCd = "Tutup Daftar Lokasi"
    override val usePrivacyProtectedZone = "Gunakan Zona Terlindungi Privasi"
    override val acquiringMaskedZone = "Menangkap zona samaran..."
    override val maskedGridHint = "Zona ~9 km · koordinat presisi tidak pernah dikirim"
    override val tapToAllowApprox = "Ketuk untuk mengizinkan perkiraan lokasi"
    override val searchCityPlaceholder = "Cari kota atau bandara..."
    override val currentSelectionPrefix = "Pilihan Saat Ini"

    override val hourlyForecastTitle = "PRAKIRAAN 24 JAM"
    override val next24HoursBadge = "24 Jam Berikutnya"
    override val nowLabel = "Kini"

    override val adaptiveSolarThemeTitle = "TEMA SURYA ADAPTIF"
    override val activeSolarThemeCd = "Fase tema surya aktif"
    override val autoLabel = "Otomatis"
    override val manualLabel = "Manual"
    override val modeAuto = "Otomatis"
    override val modeDawn = "Fajar"
    override val modeDay = "Siang"
    override val modeDusk = "Sore"
    override val modeNight = "Malam"
    override val themeModeCdSuffix = "mode tema"
    override val phaseLabelSunriseSunset = "Terbit/Terbenam · Putih Keemasan"
    override val phaseLabelDay = "Pagi/Siang · Krem Hangat"
    override val phaseLabelAfternoonDusk = "Sore · Biru Sejuk"
    override val phaseLabelNight = "Malam · Mode Gelap"

    override val sunriseSunsetTitle = "TERBIT & TERBENAM"
    override val sunriseSunsetCd = "Jadwal matahari terbit dan terbenam"
    override val todayLabel = "Hari Ini"
    override val tomorrowLabel = "Besok"
    override val dayAfterTomorrowLabel = "Lusa"
    override val currentGpsZoneCd = "Zona GPS saat ini"
    override val selectedCityCd = "Lokasi kota terpilih"
    override val sunriseLabel = "Terbit"
    override val sunsetLabel = "Terbenam"
    override val sunriseTimeCd = "Waktu matahari terbit"
    override val sunsetTimeCd = "Waktu matahari terbenam"
    override val moonPhaseTitle = "FASE BULAN"
    override val litSuffix = "Terang"
    override val solarNoonLabel = "Tengah Hari"
    override val sunCycleTitle = "SIKLUS MATAHARI"
    override val sunCycleDaylightSuffix = "Siang Hari"
    override val nextSunriseIn: (String) -> String = { time -> "Terbit Berikutnya $time" }
    override val nextSunsetIn: (String) -> String = { time -> "Terbenam dalam $time" }

    override val weatherAtmosphereTitle = "Cuaca & Atmosfer"
    override val openRadarCd = "Buka Radar"
    override val radarButton = "Radar"
    override val realTimeAtmosphereBadge = "ATMOSFER WAKTU NYATA"
    override val surfaceTab = "Permukaan"
    override val midCloudTab = "Awan Tengah"
    override val upperStratumTab = "Lapisan Atas"
    override val atmosphereStratumUpper = "LAPISAN ATMOSFER"
    override val altitudeSurface = "Permukaan · 42m"
    override val altitudeMidCloud = "Awan Tengah · 4.200m"
    override val altitudeUpper = "Stratosfer · 12.000m"
    override val strataRangeSurface = "0 - 1.200m Permukaan Tanah"
    override val strataRangeMidCloud = "1.200 - 8.000m Troposfer"
    override val strataRangeUpper = "8.000 - 15.000m Lapisan Atas"
    override val upperStratosphereTitle = "Stratosfer Atas"
    override val midTroposphereTitle = "Awan Hujan Troposfer Tengah"
    override val groundBoundaryTitle = "Batas Permukaan & Curah Hujan"
    override val windUpper = "ANGIN"
    override val gustsLabel = "Hembusan"
    override val uvIndexUpper = "INDEKS UV"
    override val uvPeakWindowText = "Puncak radiasi surya 11.00 - 14.00"
    override val humidityUpper = "KELEMBAPAN"
    override val dewPointSentence: (String) -> String = { dew -> "Titik embun saat ini ${dew}°C" }
    override val pressureUpper = "TEKANAN"
    override val referenceLabel = "Standar"
    override val referenceStdValue = "1.013,25 Std"
    override val forecast7DayTitle = "Prakiraan Cuaca Harian Terperinci"
    override val next168HoursLabel = "Hari Ini, Besok, Lusa & Pekan Ini"
    override val tapDayForDetailsHint = "Ketuk hari apa saja untuk melihat prakiraan lengkap"
    override val detailConditionLabel = "Kondisi"
    override val detailRainfallLabel = "Curah Hujan"
    override val detailRainChanceLabel = "Peluang Hujan"
    override val detailWindMaxLabel = "Angin Maks"
    override val detailHumidityLabel = "Kelembapan"
    override val detailUvPeakLabel = "UV Puncak"
    override val detailTempRangeLabel = "Rentang Suhu"

    override val windCalm = "Tenang"
    override val windLightBreeze = "Sepoi Lemah"
    override val windGentleBreeze = "Angin Sepoi"
    override val windModerateBreeze = "Angin Sedang"
    override val windFreshBreeze = "Angin Segar"
    override val windStrongWind = "Angin Kencang"
    override val cdWindIndicator: (String, String, String, Int, String) -> String = { spd, unit, dir, deg, cond ->
        "Angin $spd $unit dari arah $dir ($deg derajat), $cond"
    }

    override val mapLayerRainRadar = "Radar Hujan"
    override val mapLayerWindVector = "Arah Angin"
    override val mapLayerCloudsTemp = "Awan & Suhu"
    override val refreshPrecipitationMapCd = "Perbarui Peta Hujan BMKG"
    override val osmPrecipPrefix = "Hujan OSM"
    override val openFullRadarCd = "Buka Radar Hujan Penuh"
    override val zoomInMapCd = "Perbesar peta hujan"
    override val zoomOutMapCd = "Perkecil peta hujan"
    override val recenterMapCd = "Pusatkan ulang peta hujan"
    override val legendLight = "Ringan"
    override val presetRegion = "Wilayah"
    override val presetMetro = "Kota"
    override val presetLocal = "Lokal"
    override val stationPrecipLabel = "Hujan"
    override val stationWindLabel = "Angin"
    override val weatherRadarTitle = "Radar Cuaca"
    override val weatherRadarSubtitle = "Pantauan Doppler Curah Hujan & Satelit"
    override val liveSweepLabel = "Sapuan Langsung"
    override val radarTabRain = "Doppler Hujan"
    override val radarTabWind = "Arus Angin"
    override val radarTabSatellite = "Satelit"
    override val activePrecipCellUpper = "SEL HUJAN AKTIF"
    override val dbzRainCoreLabel = "dBZ Inti Hujan"
    override val radarSummaryFallback = "Pita awan hujan konvektif terdeteksi di stasiun pemantauan wilayah."
    override val cellSpeedLabel = "Laju Sel"
    override val echoTopLabel = "Puncak Gema"
    override val accumulationLabel = "Akumulasi"
    override val surfaceRainUpper = "HUJAN PERMUKAAN"
    override val moderateSteadyRain = "Hujan sedang merata"
    override val next60mLabel = "60 Menit Depan"
    override val cellVectorUpper = "VEKTOR SEL"
    override val velocityLabel = "Kecepatan"
    override val reflectivityUpper = "REFLEKTIVITAS"
    override val dopplerBandEcho = "Gema pita Doppler"
    override val hailRiskLabel = "Risiko Es"
    override val irCloudTopUpper = "PUNCAK AWAN IR"
    override val deepConvectiveDeck = "Lapisan konvektif pekat"
    override val satelliteLabel = "Satelit"
    override val radarPlaybackTrackUpper = "LINIMASA PEMUTARAN RADAR"
    override val liveNowLabel = "Langsung (Kini)"
    override val minShort = "mnt"

    override val airQualitySmogTitle = "Kualitas Udara & Asap"
    override val aerosolSensorArray = "Jaringan Sensor Aerosol"
    override val sensorsLiveSuffix = "Sensor Aktif"
    override val regionalAqiUpper = "INDEKS KUALITAS UDARA WILAYAH"
    override val aqiScale0Good = "0 Baik"
    override val aqiScale50Mod = "50 Sedang"
    override val aqiScale150Poor = "150 Buruk"
    override val aqiScale300Alert = "300+ Bahaya"
    override val pm25ParticlesUpper = "PARTIKEL PM2.5"
    override val pm25WhoThreshold = "2,4x Ambang Batas WHO"
    override val pm10CoarseUpper = "PARTIKEL PM10"
    override val withinSafetyBand = "Dalam Batas Aman"
    override val carbonMonoxideUpper = "KARBON MONOKSIDA"
    override val normalUrbanBase = "Ambang Perkotaan Normal"
    override val nitrogenDioxideUpper = "NITROGEN DIOKSIDA"
    override val trafficCorridor = "Koridor Lalu Lintas"
    override val healthActivityProtocols = "Panduan Kesehatan & Aktivitas"
    override val wearMaskTitle = "Pakai Masker"
    override val wearMaskSubtitle = "N95 untuk pernapasan sensitif"
    override val limitCardioTitle = "Kurangi Kardio"
    override val limitCardioSubtitle = "Batasi olahraga luar ruang"
    override val closeVentsTitle = "Tutup Ventilasi"
    override val closeVentsSubtitle = "Nyalakan pembersih udara"
    override val districtComparisonTitle = "Perbandingan Wilayah"
    override val keySectorsSuffix = "Sektor Utama"
    override val aqiLayerUsAqi = "Indeks AQI"
    override val refreshAqiMapCd = "Perbarui Peta AQI"
    override val aqiMapSuffix = "AQI"
    override val zoomInLabel = "Perbesar"
    override val zoomOutLabel = "Perkecil"
    override val recenterLabel = "Pusatkan Peta"

    override val disasterMonitorTitle = "Pantauan Bencana"
    override val emergencySosCd = "SOS Darurat"
    override val sosHelpButton = "Bantuan SOS"
    override val depthLabel = "Kedalaman"
    override val peakPgaLabel = "PGA Puncak"
    override val tsunamiLabel = "Tsunami"
    override val civilDefenseSirenUpper = "SIRINE PERINGATAN DINI BENCANA"
    override val zoneEarlyWarningSubtitle = "Peringatan Dini & Notifikasi Zona"
    override val sirenActiveBadge = "SIRINE AKTIF"
    override val armedBadge = "SIAGA"
    override val sirenDescriptionForCity: (String) -> String = { cityName ->
        "Mengirimkan notifikasi darurat & membunyikan sirine pertahanan sipil saat gempa bumi, potensi tsunami, badai ekstrem, atau udara berbahaya melanda $cityName."
    }
    override val disasterEarthquakeChip = "Gempa"
    override val disasterTsunamiChip = "Tsunami"
    override val disasterStormChip = "Badai"
    override val disasterAirHazardChip = "Udara Buruk"
    override val testSirenCd = "Uji Sirine"
    override val backgroundAlertServiceUpper = "LAYANAN PEMANTAUAN LATAR"
    override val scanNowButton = "Pindai"
    override val scanningLabel = "Memindai..."
    override val silenceDisasterSirenButton = "Matikan Sirine Bencana"
    override val silenceDisasterSirenCd = "Matikan Sirine Bencana"
    override val accelerometerSeismicUpper = "SENSOR GETARAN AKSELEROMETER"
    override val accelUnavailable = "Sensor Akselerometer Tidak Tersedia"
    override val accelArmed = "Aktif · Detektor Pola Getaran Siap"
    override val accelPaused = "Pemantauan Sensor Dijeda"
    override val liveLinearVibrationLabel = "Getaran Linear Langsung"
    override val detectionsLabel = "Deteksi"
    override val accelMonitorsDesc = "Memantau pola getaran osilasi beruntun (>=2,4 m/dt² dengan pembalikan sumbu) untuk memicu sirine instan."
    override val pWaveLeadUpper = "GELOMBANG-P"
    override val sWaveShockUpper = "GETARAN-S"
    override val tsunamiBuoyUpper = "PELAMPUNG TSUNAMI"
    override val volcanicArrayUpper = "PANTAUAN VULKANIK"
    override val primaryWaveElapsed = "Waktu gelombang primer"
    override val statusLabel = "Status"
    override val decayingLabel = "Mereda"
    override val feltStrongIndoors = "Terasa kuat di dalam ruang"
    override val structuralLabel = "Struktur"
    override val safeLabel = "Aman"
    override val secUnit = "dtk"
    override val mSurgeUnit = "m pasang"
    override val coastalMarineAdvisory = "Peringatan gelombang pesisir"
    override val baysideLabel = "Pesisir"
    override val restrictedLabel = "Terbatas"
    override val normalLabel = "Normal"
    override val tiltmeterLabel = "Kemiringan"
    override val recentSeismicActivity = "Aktivitas Gempa Terkini"
    override val past48HoursLabel = "48 Jam Terakhir"
    override val emergencySafetyProtocol = "Protokol Keselamatan Darurat"
    override val closeSosCd = "Tutup SOS"
    override val immediateShakeResponse = "Tindakan Cepat Saat Guncangan"
    override val sosChecklistBody = "1. Merunduk, berlindung di bawah meja kokoh, dan bertahan.\n2. Jauhi kaca jendela serta rak yang tidak terikat.\n3. Hindari wilayah pesisir pantai saat Peringatan Tsunami."
    override val fireAmbulanceLabel = "Damkar / Ambulans"
    override val policeRescueLabel = "Polisi / SAR"
    override val acknowledgeChecklistButton = "Pahami Panduan Keselamatan"
    override val bmkgShakemapTab = "Peta Guncangan BMKG"
    override val radar2dMapTab = "Peta Radar 2D"
    override val refreshBmkgSeismicCd = "Perbarui Data Gempa BMKG"
    override val autoUpdatingPrefix = "Pembaruan Otomatis"
    override val dragIslandsHint = "Geser peta · Ketuk pin M"

    override val settingsTitle = "Setelan"
    override val appLanguageCardTitle = "Bahasa Aplikasi"
    override val appLanguageCardSubtitle = "Ganti seluruh tampilan aplikasi ke Bahasa Indonesia penuh atau Inggris"
    override val languageEnglishOption = "English"
    override val languageIndonesianOption = "Bahasa Indonesia"
    override val gpsPrivacySyncTitle = "Privasi GPS & Sinkronisasi Langsung"
    override val gpsPrivacyShieldTitle = "Perisai Privasi Lokasi GPS"
    override val gpsPrivacyShieldSubtitle = "Mengunci telemetri ke zona kabupaten/kota (~9-11 km); koordinat persis dibuang"
    override val powerAwareSyncTitle = "Sinkronisasi Latar Hemat Daya"
    override val realTimeTelemetryStream = "Aliran Telemetri Waktu Nyata"
    override val adaptiveWorkManagerPrefix = "WorkManager Adaptif"
    override val encryptionHardeningTitle = "Enkripsi & Keamanan Aplikasi"
    override val secSettingsAndGps = "Setelan & Koordinat GPS"
    override val secRoomDbCache = "Basis Data Luring Lokal"
    override val secDomainAllowlist = "Daftar Domain Resmi BMKG & Cuaca"
    override val secCoarseGpsGuard = "Penjaga Zona GPS Kasar"
    override val secBinaryIntegrity = "Integritas Biner & Runtime"
    override val secEncryptedPrefs = "Preferensi Terenkripsi · AES-256"
    override val secAesEncrypted = "Terenkripsi AES"
    override val secKeystoreEncrypted = "Terenkripsi AES-256 KeyStore"
    override val secStandard = "Standar"
    override val secHttpsOnlyBlocked: (Int) -> String = { count -> "Khusus HTTPS · $count Diblokir" }
    override val secCoarseEnforced = "Aktif (~11 km · Tanpa Titik Persis)"
    override val secR8Hardened = "Terlindungi R8 · Aman"
    override val emergencyAlertsTitle = "Peringatan Darurat"
    override val seismicPWaveAlertTitle = "Peringatan Gelombang-P Gempa"
    override val seismicPWaveAlertSubtitle = "Bunyikan peringatan untuk gempa M 4,5+"
    override val typhoonFlashWatchTitle = "Siaga Badai & Gelombang"
    override val typhoonFlashWatchSubtitle = "Peringatan penurunan tekanan udara dan badai"
    override val aqiSmogInversionTitle = "Peringatan Asap & Polusi Udara"
    override val aqiSmogInversionSubtitle = "Beri tahu saat PM2.5 melebihi 100 µg/m³"
    override val unitsTelemetryTitle = "Satuan & Standar Telemetri"
    override val temperatureUnitTitle = "Satuan Suhu"
    override val currentScalePrefix = "Skala aktif"
    override val celsiusName = "Celcius"
    override val fahrenheitName = "Fahrenheit"
    override val tempMetricOption = "°C Metrik"
    override val tempImperialOption = "°F Imperial"
    override val windVelocityTitle = "Kecepatan Angin"
    override val pressureScaleTitle = "Skala Tekanan Udara"
    override val displayParallaxTitle = "Tampilan & Paralaks"
    override val gyroParallaxTitle = "Paralaks Giroskop"
    override val gyroParallaxSubtitle = "Miringkan ponsel untuk menggerakkan sudut pandang"
    override val glassRefractionTitle = "Kedalaman Efek Kaca"
    override val glassRefractionSubtitle = "Buram akselerasi perangkat keras"
    override val glassRefractionValue = "20px Jernih"
    override val offlineDisasterCacheTitle = "Penyimpanan Luring Bencana"
    override val cacheActiveSize = "128 MB Aktif"
    override val faultLineVectorsLabel = "Garis patahan & vektor topografi"
    override val emergencyShelterMarkersLabel = "Penanda lokasi evakuasi darurat"
    override val offlineReadyLabel = "Siap Luring"
    override val refreshOfflineCacheButton = "Perbarui Penyimpanan Luring Wilayah"
    override val syncingRegionalCache = "Menyimpan Data Wilayah..."
}

fun veridiaStringsFor(language: AppLanguage): VeridiaStrings = when (language) {
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.INDONESIAN -> IndonesianStrings
}

val LocalVeridiaStrings = compositionLocalOf<VeridiaStrings> { EnglishStrings }

val veridiaStrings: VeridiaStrings
    @Composable
    @ReadOnlyComposable
    get() = LocalVeridiaStrings.current

// Domain text translators ensuring 100% pure Indonesian even for cached or backend strings
object VeridiaLocalizer {

    fun solarPhaseLabel(phase: SolarThemePhase, strings: VeridiaStrings): String = when (phase) {
        SolarThemePhase.SUNRISE_SUNSET -> strings.phaseLabelSunriseSunset
        SolarThemePhase.DAY -> strings.phaseLabelDay
        SolarThemePhase.AFTERNOON_DUSK -> strings.phaseLabelAfternoonDusk
        SolarThemePhase.NIGHT -> strings.phaseLabelNight
    }

    fun conditionLabel(raw: String, kind: WeatherConditionKind, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        val lower = raw.lowercase(Locale.US)
        return when {
            lower.contains("thunder") || lower.contains("petir") || kind == WeatherConditionKind.THUNDERSTORM -> "Hujan Petir"
            lower.contains("heavy rain") || lower.contains("hujan lebat") -> "Hujan Lebat"
            lower.contains("light rain") || lower.contains("drizzle") || lower.contains("hujan ringan") || lower.contains("gerimis") -> "Hujan Ringan"
            lower.contains("rain") || lower.contains("hujan") || kind == WeatherConditionKind.RAIN -> "Hujan"
            lower.contains("partly cloudy") || lower.contains("cerah berawan") || kind == WeatherConditionKind.PARTLY_CLOUDY -> "Cerah Berawan"
            lower.contains("overcast") || lower.contains("mostly cloudy") || lower.contains("berawan tebal") || lower.contains("mendung") -> "Berawan Tebal"
            lower.contains("cloud") || lower.contains("berawan") || kind == WeatherConditionKind.CLOUDY -> "Berawan"
            lower.contains("fog") || lower.contains("mist") || lower.contains("haze") || lower.contains("kabut") || kind == WeatherConditionKind.FOG -> "Berkabut"
            lower.contains("clear") || lower.contains("sunny") || lower.contains("cerah") || kind == WeatherConditionKind.SUNNY -> "Cerah"
            lower.contains("sunset") || kind == WeatherConditionKind.SUNSET -> "Senja"
            lower.contains("protected zone") -> "Zona Terlindungi"
            lower.contains("live telemetry") -> "Telemetri Langsung"
            else -> raw
        }
    }

    fun rawSummaryCondition(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        val lower = raw.lowercase(Locale.US)
        val kind = when {
            lower.contains("thunder") -> WeatherConditionKind.THUNDERSTORM
            lower.contains("rain") || lower.contains("shower") -> WeatherConditionKind.RAIN
            lower.contains("fog") || lower.contains("mist") || lower.contains("haze") -> WeatherConditionKind.FOG
            lower.contains("partly") -> WeatherConditionKind.PARTLY_CLOUDY
            lower.contains("cloud") || lower.contains("overcast") -> WeatherConditionKind.CLOUDY
            else -> WeatherConditionKind.SUNNY
        }
        return conditionLabel(raw, kind, strings)
    }

    fun windDirection(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when (raw.trim().lowercase(Locale.US)) {
            "north", "n", "utara" -> "Utara"
            "northeast", "ne", "timur laut" -> "Timur Laut"
            "east", "e", "timur" -> "Timur"
            "southeast", "se", "tenggara" -> "Tenggara"
            "south", "s", "selatan" -> "Selatan"
            "southwest", "sw", "barat daya" -> "Barat Daya"
            "west", "w", "barat" -> "Barat"
            "northwest", "nw", "barat laut" -> "Barat Laut"
            "variable", "berubah-ubah" -> "Berubah-ubah"
            else -> raw
        }
    }

    fun windSummary(speedKmh: Int, strings: VeridiaStrings): String = when {
        speedKmh < 2 -> strings.windCalm
        speedKmh < 12 -> strings.windLightBreeze
        speedKmh < 25 -> strings.windGentleBreeze
        speedKmh < 39 -> strings.windModerateBreeze
        speedKmh < 55 -> strings.windFreshBreeze
        else -> strings.windStrongWind
    }

    fun moonPhaseName(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when (raw.trim().lowercase(Locale.US)) {
            "new moon" -> "Bulan Baru"
            "waxing crescent" -> "Sabit Muda"
            "first quarter" -> "Kuartal Pertama"
            "waxing gibbous" -> "Cembung Muda"
            "full moon" -> "Bulan Purnama"
            "waning gibbous" -> "Cembung Tua"
            "last quarter", "third quarter" -> "Kuartal Akhir"
            "waning crescent" -> "Sabit Tua"
            else -> raw
        }
    }

    fun daylightDuration(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        // e.g., "12h 14m Daylight" -> "12j 14m Siang"
        return raw
            .replace("h ", "j ")
            .replace("m Daylight", "m Siang")
            .replace("Daylight", "Siang")
    }

    fun solarDateLabel(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        val months = mapOf(
            "Jan" to "Jan", "Feb" to "Feb", "Mar" to "Mar", "Apr" to "Apr",
            "May" to "Mei", "Jun" to "Jun", "Jul" to "Jul", "Aug" to "Agu",
            "Sep" to "Sep", "Oct" to "Okt", "Nov" to "Nov", "Dec" to "Des",
        )
        val days = mapOf(
            "Mon" to "Sen", "Tue" to "Sel", "Wed" to "Rab", "Thu" to "Kam",
            "Fri" to "Jum", "Sat" to "Sab", "Sun" to "Min",
        )
        var out = raw
        days.forEach { (en, id) -> out = out.replace(en, id) }
        months.forEach { (en, id) -> out = out.replace(en, id) }
        return out
    }

    fun nextSolarEventLabel(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH || raw.isBlank()) return raw
        return when {
            raw.startsWith("Next Sunrise in ", ignoreCase = true) -> {
                val dur = raw.removePrefix("Next Sunrise in ").replace("h ", "j ")
                strings.nextSunriseIn(dur)
            }
            raw.startsWith("Sunset in ", ignoreCase = true) -> {
                val dur = raw.removePrefix("Sunset in ").replace("h ", "j ")
                strings.nextSunsetIn(dur)
            }
            else -> raw
        }
    }

    fun timeSlotLabel(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return if (raw.equals("Now", ignoreCase = true)) strings.nowLabel else raw
    }

    fun dayRelativeLabel(index: Int, rawDayLabel: String, strings: VeridiaStrings): String {
        return when (index) {
            0 -> strings.todayLabel
            1 -> strings.tomorrowLabel
            2 -> if (strings.language == AppLanguage.INDONESIAN) strings.dayAfterTomorrowLabel else dayAbbrev(rawDayLabel, strings)
            else -> dayAbbrev(rawDayLabel, strings)
        }
    }

    fun dayAbbrev(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when (raw.trim().lowercase(Locale.US)) {
            "today" -> "Hari Ini"
            "tomorrow" -> "Besok"
            "mon", "monday" -> "Senin"
            "tue", "tuesday" -> "Selasa"
            "wed", "wednesday" -> "Rabu"
            "thu", "thursday" -> "Kamis"
            "fri", "friday" -> "Jumat"
            "sat", "saturday" -> "Sabtu"
            "sun", "sunday" -> "Minggu"
            else -> raw.replace("Day ", "Hari ")
        }
    }

    fun dateLabel(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH || raw.isBlank()) return raw
        return solarDateLabel(raw, strings)
    }

    fun pressureTrend(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when {
            raw.contains("Steady", ignoreCase = true) -> "Barometer Stabil"
            raw.contains("Trough", ignoreCase = true) -> "Palung Tekanan Rendah"
            raw.contains("Falling", ignoreCase = true) -> "Tekanan Menurun"
            else -> raw
        }
    }

    fun uvLevel(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when (raw.trim().lowercase(Locale.US)) {
            "very high" -> "Sangat Tinggi"
            "high" -> "Tinggi"
            "moderate" -> "Sedang"
            "low" -> "Rendah"
            else -> raw
        }
    }

    fun aqiCategory(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when (raw.trim().lowercase(Locale.US)) {
            "excellent" -> "Sangat Baik"
            "good" -> "Baik"
            "moderate" -> "Sedang"
            "sensitive" -> "Sensitif"
            "unhealthy" -> "Tidak Sehat"
            else -> raw
        }
    }

    fun aqiSummary(raw: String, aqi: Int, cityName: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when {
            aqi >= 151 -> "Konsentrasi partikulat meningkat di $cityName (Puncak AQI $aqi)."
            aqi >= 51 -> "Sebaran aerosol tingkat sedang di wilayah $cityName · AQI $aqi."
            else -> "Sirkulasi udara bersih di $cityName · Rata-rata AQI $aqi."
        }
    }

    fun aqiDistrictDesc(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return raw
            .replace("Clean atmospheric circulation", "Sirkulasi udara bersih")
            .replace("Balanced regional air mass", "Massa udara wilayah seimbang")
            .replace("Urban aerosol accumulation", "Akumulasi aerosol perkotaan")
            .replace("Elevated particulate hotspot", "Titik panas partikulat tinggi")
    }

    fun aqiSectorName(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return raw
            .replace(" North-West", " Barat Laut")
            .replace(" North Sector", " Sektor Utara")
            .replace(" North-East", " Timur Laut")
            .replace(" West Basin", " Cekungan Barat")
            .replace(" Center", " Pusat")
            .replace(" Core", " Inti Kota")
            .replace(" East Corridor", " Koridor Timur")
            .replace(" South-West", " Barat Daya")
            .replace(" South Ridge", " Punggung Selatan")
            .replace(" South-East", " Tenggara")
    }

    fun aqiSectorDirection(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when (raw.trim()) {
            "North-West" -> "Barat Laut"
            "North Sector" -> "Utara"
            "North-East" -> "Timur Laut"
            "West Basin" -> "Barat"
            "Center" -> "Pusat"
            "East Corridor" -> "Timur"
            "South-West" -> "Barat Daya"
            "South Ridge" -> "Selatan"
            "South-East" -> "Tenggara"
            else -> raw
        }
    }

    fun shortSectorCode(id: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) {
            return when (id.lowercase(Locale.US)) {
                "c" -> "CTR"
                "nw" -> "NW"
                "n" -> "N"
                "ne" -> "NE"
                "w" -> "W"
                "e" -> "E"
                "sw" -> "SW"
                "s" -> "S"
                "se" -> "SE"
                else -> id.uppercase(Locale.US).take(3)
            }
        }
        return when (id.lowercase(Locale.US)) {
            "c" -> "PST"
            "nw" -> "BL"
            "n" -> "U"
            "ne" -> "TL"
            "w" -> "B"
            "e" -> "T"
            "sw" -> "BD"
            "s" -> "S"
            "se" -> "TG"
            else -> id.uppercase(Locale.US).take(3)
        }
    }

    fun seismicBannerTitle(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when {
            raw.contains("BMKG", ignoreCase = true) -> "BMKG GEMPA TERKINI"
            raw.contains("Seismic", ignoreCase = true) -> "PANTAUAN GEMPA BMKG"
            else -> raw
        }
    }

    fun seismicBannerSubtitle(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return raw
            .replace("min ago", strings.minAgoShort)
            .replace("hours ago", strings.hoursAgoShort)
            .replace("d ago", strings.daysAgoShort)
            .replace("Live BMKG", "BMKG Langsung")
    }

    fun seismicDescription(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return raw.replace("Depth ", "Kedalaman ")
    }

    fun seismicEventTitle(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return raw
            .replace("Seismic Event", "Aktivitas Gempa")
            .replace("Earthquake", "Gempa Bumi")
    }

    fun seismicWarningHeader(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when {
            raw.contains("EEW", ignoreCase = true) -> "PERINGATAN DINI GEMPA BMKG"
            else -> raw
        }
    }

    fun tsunamiStatus(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when {
            raw.contains("No Tsunami", ignoreCase = true) -> "Tidak Berpotensi"
            else -> raw
        }
    }

    fun hailRisk(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when (raw.trim().lowercase(Locale.US)) {
            "none" -> "Nihil"
            "low" -> "Rendah"
            "moderate" -> "Sedang"
            "high" -> "Tinggi"
            else -> raw
        }
    }

    fun timestampLabel(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when {
            raw == "Syncing..." -> strings.syncingDots
            raw == "Offline · Cached Fallback" -> strings.offlineCachedFallback
            raw.startsWith("Synced ") -> "Sinkron ${raw.removePrefix("Synced ")}"
            raw.startsWith("Cached · ") -> "Tersimpan · ${solarDateLabel(raw.removePrefix("Cached · "), strings)}"
            else -> solarDateLabel(raw, strings)
        }
    }

    fun dataSourceLabel(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return raw
            .replace("BMKG & Copernicus Regional AQI", "BMKG & AQI Regional Copernicus")
            .replace("BMKG Region & Copernicus CAMS Grid", "Wilayah BMKG & Kisi CAMS Copernicus")
            .replace("Copernicus CAMS 9-Node Regional Grid", "Kisi Regional 9-Titik CAMS Copernicus")
            .replace("Open-Meteo Live", "Open-Meteo Langsung")
            .replace("BMKG Fallback · Open-Meteo Live", "Cadangan BMKG · Open-Meteo Langsung")
            .replace("USGS Fallback · BMKG TEWS", "Cadangan USGS · BMKG TEWS")
    }

    fun batteryTierLabel(tier: SyncBatteryTier, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return tier.name.lowercase().replace('_', ' ')
        return when (tier) {
            SyncBatteryTier.CRITICAL_BATTERY -> "baterai kritis"
            SyncBatteryTier.LOW_BATTERY -> "baterai lemah"
            SyncBatteryTier.CHARGING -> "sedang mengisi daya"
            SyncBatteryTier.NORMAL -> "normal"
        }
    }

    fun disasterKindBadge(kind: DisasterKind, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return kind.badge
        return when (kind) {
            DisasterKind.EARTHQUAKE -> "PERINGATAN GEMPA"
            DisasterKind.TSUNAMI -> "SIRINE TSUNAMI"
            DisasterKind.SEVERE_STORM -> "SIAGA BADAI EKSTREM"
            DisasterKind.HAZARDOUS_AIR -> "DARURAT UDARA"
        }
    }

    fun disasterKindSirenProfile(kind: DisasterKind, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return kind.sirenProfileLabel
        return when (kind) {
            DisasterKind.EARTHQUAKE -> "Sirine Gempa Pertahanan Sipil"
            DisasterKind.TSUNAMI -> "Sirine Tsunami Pesisir"
            DisasterKind.SEVERE_STORM -> "Klakson Badai Ekstrem"
            DisasterKind.HAZARDOUS_AIR -> "Nada Peringatan Atmosfer"
        }
    }

    fun alertScanStatus(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when {
            raw.startsWith("Auto-monitoring every") -> "Pemantauan otomatis tiap 15 mnt (Wi-Fi/Seluler)"
            raw.startsWith("Scanning live weather") -> "Memindai cuaca langsung, AQI & data gempa..."
            raw.startsWith("Alert sent (") -> raw.replace("Alert sent (", "Peringatan terkirim (")
            raw.startsWith("Active alert (") -> raw.replace("Active alert (", "Peringatan aktif (")
            raw.startsWith("All clear · Checked at ") -> raw.replace("All clear · Checked at ", "Aman · Diperiksa pukul ")
            raw == "Alert triggers disabled in Settings" -> "Pemicu peringatan dinonaktifkan di Setelan"
            raw.contains("Critical battery saver active") -> "Ditunda · Mode hemat baterai kritis aktif"
            else -> raw
        }
    }

    fun volcanicNote(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return when {
            raw.contains("No eruption", ignoreCase = true) -> "Tidak ada anomali erupsi"
            else -> raw
        }
    }

    fun typhoonMeta(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return raw.replace("Steady", "Stabil").replace("Sustained", "Kecepatan")
    }

    fun radarSummaryDescription(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return "Pita hujan Doppler bergerak menuju pusat kota dengan hembusan sedang."
    }

    fun radarCellDestination(raw: String, strings: VeridiaStrings): String {
        if (strings.language == AppLanguage.ENGLISH) return raw
        return raw
            .replace("Moving towards", "Bergerak menuju")
            .replace("North", "Utara")
            .replace("South", "Selatan")
            .replace("East", "Timur")
            .replace("West", "Barat")
    }
}

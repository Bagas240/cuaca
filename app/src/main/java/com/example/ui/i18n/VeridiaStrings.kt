package com.example.ui.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import com.example.alerts.DisasterKind
import com.example.data.model.AppLanguage
import com.example.data.model.SolarThemePhase
import com.example.data.model.WeatherConditionKind
import com.example.work.SyncBatteryTier
import java.util.Locale

data class VeridiaStrings(
    val language: AppLanguage,
    // Bottom Dock & Common
    val navHome: String,
    val navWeather: String,
    val navAir: String,
    val navAlerts: String,
    val navSettings: String,
    val cdBack: String,
    val cdSelectCity: String,
    val cdOpenRadar: String,
    val cdSearchLocations: String,
    val cdRefreshLiveTelemetry: String,
    val cdRefreshGps: String,
    val dismissAlert: String,
    val silenceSirenPrefix: String,

    // Live / Cache / Data Attribution
    val liveBadge: String,
    val cachedBadge: String,
    val offlineCachedFallback: String,
    val offlineRoomCacheCd: String,
    val syncingDots: String,
    val minAgoShort: String,
    val hoursAgoShort: String,
    val daysAgoShort: String,
    val sourcePrefix: String,

    // Hero & Home Dashboard
    val feelsLikePrefix: String,
    val heroFeelsWord: String,
    val temperatureTileTitle: String,
    val humidityTileTitle: String,
    val windSpeedTileTitle: String,
    val airQualityTileTitle: String,
    val toggleTemperatureCd: String,
    val toggleHumidityCd: String,
    val toggleWindCd: String,
    val highShort: String,
    val lowShort: String,
    val uvShort: String,
    val fullAtmosphereLink: String,
    val humidityHumid: String,
    val humidityBalanced: String,
    val humidityDry: String,
    val dewPointLabel: String,
    val precipChanceLabel: String,
    val solarAndMoistureLink: String,
    val pressurePrefix: String,
    val gustsPrefix: String,
    val windRadarLink: String,
    val livePrecipitationTrackerTitle: String,
    val livePrecipitationTrackerSubtitle: String,
    val sevenDayForecastUpper: String,
    val fullDetailsLink: String,
    val inYourAreaBadge: String,
    val nearYourAreaKm: (Int) -> String,
    val detailsButton: String,

    // Home GPS Card & Locations Modal
    val gpsPromptTitle: String,
    val gpsPromptBody: String,
    val gpsDeniedBody: String,
    val gpsEnableCta: String,
    val gpsRetryCta: String,
    val gpsOpenSettings: String,
    val gpsAcquiringZone: String,
    val gpsProtectedZonePrefix: String,
    val gpsViewingPrefix: String,
    val gpsPrivacyProtectedCd: String,
    val gpsPrivacyMaskedFallback: String,
    val gpsButtonLabel: String,
    val gpsErrorCouldNotAcquire: String,
    val gpsErrorPlayServices: String,
    val locationsTitle: String,
    val closeCitiesCd: String,
    val usePrivacyProtectedZone: String,
    val acquiringMaskedZone: String,
    val maskedGridHint: String,
    val tapToAllowApprox: String,
    val searchCityPlaceholder: String,
    val currentSelectionPrefix: String,

    // Hourly 24h Card
    val hourlyForecastTitle: String,
    val next24HoursBadge: String,
    val nowLabel: String,

    // Solar Theme Switcher
    val adaptiveSolarThemeTitle: String,
    val activeSolarThemeCd: String,
    val autoLabel: String,
    val manualLabel: String,
    val modeAuto: String,
    val modeDawn: String,
    val modeDay: String,
    val modeDusk: String,
    val modeNight: String,
    val themeModeCdSuffix: String,
    val phaseLabelSunriseSunset: String,
    val phaseLabelDay: String,
    val phaseLabelAfternoonDusk: String,
    val phaseLabelNight: String,

    // Sunrise, Sunset & Moon Phase Card
    val sunriseSunsetTitle: String,
    val sunriseSunsetCd: String,
    val todayLabel: String,
    val tomorrowLabel: String,
    val dayAfterTomorrowLabel: String,
    val currentGpsZoneCd: String,
    val selectedCityCd: String,
    val sunriseLabel: String,
    val sunsetLabel: String,
    val sunriseTimeCd: String,
    val sunsetTimeCd: String,
    val moonPhaseTitle: String,
    val litSuffix: String,
    val solarNoonLabel: String,
    val sunCycleTitle: String,
    val sunCycleDaylightSuffix: String,
    val nextSunriseIn: (String) -> String,
    val nextSunsetIn: (String) -> String,

    // Detailed Weather Screen
    val weatherAtmosphereTitle: String,
    val openRadarCd: String,
    val radarButton: String,
    val realTimeAtmosphereBadge: String,
    val surfaceTab: String,
    val midCloudTab: String,
    val upperStratumTab: String,
    val atmosphereStratumUpper: String,
    val altitudeSurface: String,
    val altitudeMidCloud: String,
    val altitudeUpper: String,
    val strataRangeSurface: String,
    val strataRangeMidCloud: String,
    val strataRangeUpper: String,
    val upperStratosphereTitle: String,
    val midTroposphereTitle: String,
    val groundBoundaryTitle: String,
    val windUpper: String,
    val gustsLabel: String,
    val uvIndexUpper: String,
    val uvPeakWindowText: String,
    val humidityUpper: String,
    val dewPointSentence: (String) -> String,
    val pressureUpper: String,
    val referenceLabel: String,
    val referenceStdValue: String,
    val forecast7DayTitle: String,
    val next168HoursLabel: String,
    val tapDayForDetailsHint: String,
    val detailConditionLabel: String,
    val detailRainfallLabel: String,
    val detailRainChanceLabel: String,
    val detailWindMaxLabel: String,
    val detailHumidityLabel: String,
    val detailUvPeakLabel: String,
    val detailTempRangeLabel: String,

    // Wind Indicator
    val windCalm: String,
    val windLightBreeze: String,
    val windGentleBreeze: String,
    val windModerateBreeze: String,
    val windFreshBreeze: String,
    val windStrongWind: String,
    val cdWindIndicator: (String, String, String, Int, String) -> String,

    // Weather Interactive Map (osmdroid) & Radar Screen
    val mapLayerRainRadar: String,
    val mapLayerWindVector: String,
    val mapLayerCloudsTemp: String,
    val refreshPrecipitationMapCd: String,
    val osmPrecipPrefix: String,
    val openFullRadarCd: String,
    val zoomInMapCd: String,
    val zoomOutMapCd: String,
    val recenterMapCd: String,
    val legendLight: String,
    val presetRegion: String,
    val presetMetro: String,
    val presetLocal: String,
    val stationPrecipLabel: String,
    val stationWindLabel: String,
    val weatherRadarTitle: String,
    val weatherRadarSubtitle: String,
    val liveSweepLabel: String,
    val radarTabRain: String,
    val radarTabWind: String,
    val radarTabSatellite: String,
    val activePrecipCellUpper: String,
    val dbzRainCoreLabel: String,
    val radarSummaryFallback: String,
    val cellSpeedLabel: String,
    val echoTopLabel: String,
    val accumulationLabel: String,
    val surfaceRainUpper: String,
    val moderateSteadyRain: String,
    val next60mLabel: String,
    val cellVectorUpper: String,
    val velocityLabel: String,
    val reflectivityUpper: String,
    val dopplerBandEcho: String,
    val hailRiskLabel: String,
    val irCloudTopUpper: String,
    val deepConvectiveDeck: String,
    val satelliteLabel: String,
    val radarPlaybackTrackUpper: String,
    val liveNowLabel: String,
    val minShort: String,

    // Air Quality Map & Screen
    val airQualitySmogTitle: String,
    val aerosolSensorArray: String,
    val sensorsLiveSuffix: String,
    val regionalAqiUpper: String,
    val aqiScale0Good: String,
    val aqiScale50Mod: String,
    val aqiScale150Poor: String,
    val aqiScale300Alert: String,
    val pm25ParticlesUpper: String,
    val pm25WhoThreshold: String,
    val pm10CoarseUpper: String,
    val withinSafetyBand: String,
    val carbonMonoxideUpper: String,
    val normalUrbanBase: String,
    val nitrogenDioxideUpper: String,
    val trafficCorridor: String,
    val healthActivityProtocols: String,
    val wearMaskTitle: String,
    val wearMaskSubtitle: String,
    val limitCardioTitle: String,
    val limitCardioSubtitle: String,
    val closeVentsTitle: String,
    val closeVentsSubtitle: String,
    val districtComparisonTitle: String,
    val keySectorsSuffix: String,
    val aqiLayerUsAqi: String,
    val refreshAqiMapCd: String,
    val aqiMapSuffix: String,
    val zoomInLabel: String,
    val zoomOutLabel: String,
    val recenterLabel: String,

    // Disaster Monitor & BMKG Shakemap
    val disasterMonitorTitle: String,
    val emergencySosCd: String,
    val sosHelpButton: String,
    val depthLabel: String,
    val peakPgaLabel: String,
    val tsunamiLabel: String,
    val civilDefenseSirenUpper: String,
    val zoneEarlyWarningSubtitle: String,
    val sirenActiveBadge: String,
    val armedBadge: String,
    val sirenDescriptionForCity: (String) -> String,
    val disasterEarthquakeChip: String,
    val disasterTsunamiChip: String,
    val disasterStormChip: String,
    val disasterAirHazardChip: String,
    val testSirenCd: String,
    val backgroundAlertServiceUpper: String,
    val scanNowButton: String,
    val scanningLabel: String,
    val silenceDisasterSirenButton: String,
    val silenceDisasterSirenCd: String,
    val accelerometerSeismicUpper: String,
    val accelUnavailable: String,
    val accelArmed: String,
    val accelPaused: String,
    val liveLinearVibrationLabel: String,
    val detectionsLabel: String,
    val accelMonitorsDesc: String,
    val pWaveLeadUpper: String,
    val sWaveShockUpper: String,
    val tsunamiBuoyUpper: String,
    val volcanicArrayUpper: String,
    val primaryWaveElapsed: String,
    val statusLabel: String,
    val decayingLabel: String,
    val feltStrongIndoors: String,
    val structuralLabel: String,
    val safeLabel: String,
    val secUnit: String,
    val mSurgeUnit: String,
    val coastalMarineAdvisory: String,
    val baysideLabel: String,
    val restrictedLabel: String,
    val normalLabel: String,
    val tiltmeterLabel: String,
    val recentSeismicActivity: String,
    val past48HoursLabel: String,
    val emergencySafetyProtocol: String,
    val closeSosCd: String,
    val immediateShakeResponse: String,
    val sosChecklistBody: String,
    val fireAmbulanceLabel: String,
    val policeRescueLabel: String,
    val acknowledgeChecklistButton: String,
    val bmkgShakemapTab: String,
    val radar2dMapTab: String,
    val refreshBmkgSeismicCd: String,
    val autoUpdatingPrefix: String,
    val dragIslandsHint: String,

    // Settings Screen
    val settingsTitle: String,
    val appLanguageCardTitle: String,
    val appLanguageCardSubtitle: String,
    val languageEnglishOption: String,
    val languageIndonesianOption: String,
    val gpsPrivacySyncTitle: String,
    val gpsPrivacyShieldTitle: String,
    val gpsPrivacyShieldSubtitle: String,
    val powerAwareSyncTitle: String,
    val realTimeTelemetryStream: String,
    val adaptiveWorkManagerPrefix: String,
    val encryptionHardeningTitle: String,
    val secSettingsAndGps: String,
    val secRoomDbCache: String,
    val secDomainAllowlist: String,
    val secCoarseGpsGuard: String,
    val secBinaryIntegrity: String,
    val secEncryptedPrefs: String,
    val secAesEncrypted: String,
    val secKeystoreEncrypted: String,
    val secStandard: String,
    val secHttpsOnlyBlocked: (Int) -> String,
    val secCoarseEnforced: String,
    val secR8Hardened: String,
    val emergencyAlertsTitle: String,
    val seismicPWaveAlertTitle: String,
    val seismicPWaveAlertSubtitle: String,
    val typhoonFlashWatchTitle: String,
    val typhoonFlashWatchSubtitle: String,
    val aqiSmogInversionTitle: String,
    val aqiSmogInversionSubtitle: String,
    val unitsTelemetryTitle: String,
    val temperatureUnitTitle: String,
    val currentScalePrefix: String,
    val celsiusName: String,
    val fahrenheitName: String,
    val tempMetricOption: String,
    val tempImperialOption: String,
    val windVelocityTitle: String,
    val pressureScaleTitle: String,
    val displayParallaxTitle: String,
    val gyroParallaxTitle: String,
    val gyroParallaxSubtitle: String,
    val glassRefractionTitle: String,
    val glassRefractionSubtitle: String,
    val glassRefractionValue: String,
    val offlineDisasterCacheTitle: String,
    val cacheActiveSize: String,
    val faultLineVectorsLabel: String,
    val emergencyShelterMarkersLabel: String,
    val offlineReadyLabel: String,
    val refreshOfflineCacheButton: String,
    val syncingRegionalCache: String,
)

private val EnglishStrings = VeridiaStrings(
    language = AppLanguage.ENGLISH,
    navHome = "Home",
    navWeather = "Weather",
    navAir = "Air",
    navAlerts = "Alerts",
    navSettings = "Settings",
    cdBack = "Back",
    cdSelectCity = "Select city",
    cdOpenRadar = "Open weather radar",
    cdSearchLocations = "Search locations",
    cdRefreshLiveTelemetry = "Refresh Live Telemetry",
    cdRefreshGps = "Refresh GPS",
    dismissAlert = "Dismiss alert",
    silenceSirenPrefix = "Silence Disaster Siren",

    liveBadge = "LIVE",
    cachedBadge = "Cached",
    offlineCachedFallback = "Offline · Cached Fallback",
    offlineRoomCacheCd = "Offline Room Cache",
    syncingDots = "Syncing...",
    minAgoShort = "min ago",
    hoursAgoShort = "hours ago",
    daysAgoShort = "d ago",
    sourcePrefix = "Source",

    feelsLikePrefix = "Feels like",
    heroFeelsWord = "Feels",
    temperatureTileTitle = "TEMPERATURE",
    humidityTileTitle = "HUMIDITY",
    windSpeedTileTitle = "WIND SPEED",
    airQualityTileTitle = "AIR QUALITY",
    toggleTemperatureCd = "Toggle Temperature Details",
    toggleHumidityCd = "Toggle Humidity Details",
    toggleWindCd = "Toggle Wind Details",
    highShort = "H",
    lowShort = "L",
    uvShort = "UV",
    fullAtmosphereLink = "Full Atmosphere",
    humidityHumid = "Humid",
    humidityBalanced = "Balanced",
    humidityDry = "Dry",
    dewPointLabel = "Dew point",
    precipChanceLabel = "Precip chance",
    solarAndMoistureLink = "Solar & Moisture",
    pressurePrefix = "Pressure",
    gustsPrefix = "Gusts",
    windRadarLink = "Wind Radar",
    livePrecipitationTrackerTitle = "LIVE PRECIPITATION TRACKER",
    livePrecipitationTrackerSubtitle = "OpenStreetMap (osmdroid) · RainViewer & BMKG",
    sevenDayForecastUpper = "7-DAY FORECAST",
    fullDetailsLink = "Full Details",
    inYourAreaBadge = "In Your Area",
    nearYourAreaKm = { km -> "Near Your Area · $km km" },
    detailsButton = "Details",

    gpsPromptTitle = "Enable Coarse Zone Location",
    gpsPromptBody = "Allows Veridia to automatically track your regional regency weather & seismic radar.",
    gpsDeniedBody = "Location permission was denied. Enable coarse location in Settings or tap retry.",
    gpsEnableCta = "Allow Location",
    gpsRetryCta = "Retry GPS Access",
    gpsOpenSettings = "Open Settings",
    gpsAcquiringZone = "Acquiring coarse zone...",
    gpsProtectedZonePrefix = "Protected Zone GPS",
    gpsViewingPrefix = "Viewing",
    gpsPrivacyProtectedCd = "GPS Privacy Protected",
    gpsPrivacyMaskedFallback = "Privacy-masked (~9 km)",
    gpsButtonLabel = "GPS",
    gpsErrorCouldNotAcquire = "Could not acquire coarse location.",
    gpsErrorPlayServices = "Google Play Services unavailable for location.",
    locationsTitle = "Locations",
    closeCitiesCd = "Close Cities",
    usePrivacyProtectedZone = "Use Privacy-Protected Zone",
    acquiringMaskedZone = "Acquiring masked zone...",
    maskedGridHint = "Masked ~9 km grid · exact GPS never exposed",
    tapToAllowApprox = "Tap to allow approximate location",
    searchCityPlaceholder = "Search city or airport...",
    currentSelectionPrefix = "Current Selection",

    hourlyForecastTitle = "24-HOUR FORECAST",
    next24HoursBadge = "Next 24 Hours",
    nowLabel = "Now",

    adaptiveSolarThemeTitle = "ADAPTIVE SOLAR THEME",
    activeSolarThemeCd = "Active solar theme phase",
    autoLabel = "Auto",
    manualLabel = "Manual",
    modeAuto = "Auto",
    modeDawn = "Dawn",
    modeDay = "Day",
    modeDusk = "Dusk",
    modeNight = "Night",
    themeModeCdSuffix = "theme mode",
    phaseLabelSunriseSunset = "Sunrise/Sunset · Whitish Gold",
    phaseLabelDay = "Morning/Day · Warm Cream",
    phaseLabelAfternoonDusk = "Afternoon · Cool Blue",
    phaseLabelNight = "Night · Dark Mode",

    sunriseSunsetTitle = "SUNRISE & SUNSET",
    sunriseSunsetCd = "Sunrise and sunset schedule",
    todayLabel = "Today",
    tomorrowLabel = "Tomorrow",
    dayAfterTomorrowLabel = "Day After",
    currentGpsZoneCd = "Current GPS zone",
    selectedCityCd = "Selected city location",
    sunriseLabel = "Sunrise",
    sunsetLabel = "Sunset",
    sunriseTimeCd = "Sunrise time",
    sunsetTimeCd = "Sunset time",
    moonPhaseTitle = "MOON PHASE",
    litSuffix = "Lit",
    solarNoonLabel = "Solar Noon",
    sunCycleTitle = "SUN CYCLE",
    sunCycleDaylightSuffix = "Daylight",
    nextSunriseIn = { time -> "Next Sunrise in $time" },
    nextSunsetIn = { time -> "Sunset in $time" },

    weatherAtmosphereTitle = "Weather & Atmosphere",
    openRadarCd = "Open Radar",
    radarButton = "Radar",
    realTimeAtmosphereBadge = "REAL-TIME ATMOSPHERE",
    surfaceTab = "Surface",
    midCloudTab = "Mid-Cloud",
    upperStratumTab = "Upper Stratum",
    atmosphereStratumUpper = "ATMOSPHERE STRATUM",
    altitudeSurface = "Surface · 42m",
    altitudeMidCloud = "Mid-Cloud · 4,200m",
    altitudeUpper = "Stratosphere · 12,000m",
    strataRangeSurface = "0 - 1,200m Ground Level",
    strataRangeMidCloud = "1,200 - 8,000m Troposphere",
    strataRangeUpper = "8,000 - 15,000m Stratum",
    upperStratosphereTitle = "Upper Stratosphere",
    midTroposphereTitle = "Mid-Troposphere Rain Clouds",
    groundBoundaryTitle = "Ground Boundary & Precipitation",
    windUpper = "WIND",
    gustsLabel = "Gusts",
    uvIndexUpper = "UV INDEX",
    uvPeakWindowText = "Peak solar window 11:00 - 14:00",
    humidityUpper = "HUMIDITY",
    dewPointSentence = { dew -> "Dew point is ${dew}°C right now" },
    pressureUpper = "PRESSURE",
    referenceLabel = "Reference",
    referenceStdValue = "1,013.25 Std",
    forecast7DayTitle = "7-Day Detailed Forecast",
    next168HoursLabel = "Today, Tomorrow & Week",
    tapDayForDetailsHint = "Tap any day to inspect detailed forecast",
    detailConditionLabel = "Condition",
    detailRainfallLabel = "Rainfall",
    detailRainChanceLabel = "Rain Chance",
    detailWindMaxLabel = "Max Wind",
    detailHumidityLabel = "Humidity",
    detailUvPeakLabel = "Peak UV",
    detailTempRangeLabel = "Thermal Range",

    windCalm = "Calm",
    windLightBreeze = "Light Breeze",
    windGentleBreeze = "Gentle Breeze",
    windModerateBreeze = "Moderate Breeze",
    windFreshBreeze = "Fresh Breeze",
    windStrongWind = "Strong Wind",
    cdWindIndicator = { spd, unit, dir, deg, cond ->
        "Wind $spd $unit from $dir ($deg degrees), $cond"
    },

    mapLayerRainRadar = "Rain Radar",
    mapLayerWindVector = "Wind Vector",
    mapLayerCloudsTemp = "Clouds & Temp",
    refreshPrecipitationMapCd = "Refresh BMKG Precipitation Map",
    osmPrecipPrefix = "OSM Precip",
    openFullRadarCd = "Open Full Precipitation Radar",
    zoomInMapCd = "Zoom in precipitation map",
    zoomOutMapCd = "Zoom out precipitation map",
    recenterMapCd = "Recenter precipitation map",
    legendLight = "Light",
    presetRegion = "Region",
    presetMetro = "Metro",
    presetLocal = "Local",
    stationPrecipLabel = "Precip",
    stationWindLabel = "Wind",
    weatherRadarTitle = "Weather Radar",
    weatherRadarSubtitle = "Doppler Precipitation & Satellite Array",
    liveSweepLabel = "Live Sweep",
    radarTabRain = "Rain Doppler",
    radarTabWind = "Wind Stream",
    radarTabSatellite = "Satellite",
    activePrecipCellUpper = "ACTIVE PRECIPITATION CELL",
    dbzRainCoreLabel = "dBZ Rain Core",
    radarSummaryFallback = "Active convective rainband detected across regional telemetry stations.",
    cellSpeedLabel = "Cell Speed",
    echoTopLabel = "Echo Top",
    accumulationLabel = "Accumulation",
    surfaceRainUpper = "SURFACE RAIN",
    moderateSteadyRain = "Moderate steady rain",
    next60mLabel = "Next 60m",
    cellVectorUpper = "CELL VECTOR",
    velocityLabel = "Velocity",
    reflectivityUpper = "REFLECTIVITY",
    dopplerBandEcho = "Doppler band echo",
    hailRiskLabel = "Hail Risk",
    irCloudTopUpper = "IR CLOUD TOP",
    deepConvectiveDeck = "Deep convective deck",
    satelliteLabel = "Satellite",
    radarPlaybackTrackUpper = "RADAR PLAYBACK TRACK",
    liveNowLabel = "Live (Now)",
    minShort = "min",

    airQualitySmogTitle = "Air Quality & Smog",
    aerosolSensorArray = "Aerosol Sensor Array",
    sensorsLiveSuffix = "Sensors Live",
    regionalAqiUpper = "REGIONAL AIR QUALITY INDEX",
    aqiScale0Good = "0 Good",
    aqiScale50Mod = "50 Moderate",
    aqiScale150Poor = "150 Poor",
    aqiScale300Alert = "300+ Alert",
    pm25ParticlesUpper = "PM2.5 PARTICLES",
    pm25WhoThreshold = "2.4x WHO Threshold",
    pm10CoarseUpper = "PM10 COARSE",
    withinSafetyBand = "Within Safety Band",
    carbonMonoxideUpper = "CARBON MONOXIDE",
    normalUrbanBase = "Normal Urban Base",
    nitrogenDioxideUpper = "NITROGEN DIOXIDE",
    trafficCorridor = "Traffic Corridor",
    healthActivityProtocols = "Health & Activity Protocols",
    wearMaskTitle = "Wear Mask",
    wearMaskSubtitle = "N95 for sensitive lungs",
    limitCardioTitle = "Limit Cardio",
    limitCardioSubtitle = "Reduce outdoor training",
    closeVentsTitle = "Close Vents",
    closeVentsSubtitle = "Engage HEPA purifiers",
    districtComparisonTitle = "District Comparison",
    keySectorsSuffix = "Key Sectors",
    aqiLayerUsAqi = "US AQI",
    refreshAqiMapCd = "Refresh AQI Map",
    aqiMapSuffix = "AQI",
    zoomInLabel = "Zoom In",
    zoomOutLabel = "Zoom Out",
    recenterLabel = "Recenter Map",

    disasterMonitorTitle = "Disaster Monitor",
    emergencySosCd = "Emergency SOS",
    sosHelpButton = "SOS Help",
    depthLabel = "Depth",
    peakPgaLabel = "Peak PGA",
    tsunamiLabel = "Tsunami",
    civilDefenseSirenUpper = "CIVIL-DEFENSE DISASTER SIREN",
    zoneEarlyWarningSubtitle = "Zone Early Warning & Notification",
    sirenActiveBadge = "SIREN ACTIVE",
    armedBadge = "ARMED",
    sirenDescriptionForCity = { cityName ->
        "Automatically dispatches disaster-specific notifications & a sweeping civil-defense siren (non-ambulance) when earthquakes, tsunami advisories, extreme storms, or hazardous air strike $cityName."
    },
    disasterEarthquakeChip = "Earthquake",
    disasterTsunamiChip = "Tsunami",
    disasterStormChip = "Storm",
    disasterAirHazardChip = "Air Hazard",
    testSirenCd = "Test Siren",
    backgroundAlertServiceUpper = "BACKGROUND ALERT SERVICE",
    scanNowButton = "Scan Now",
    scanningLabel = "Scanning...",
    silenceDisasterSirenButton = "Silence Civil-Defense Siren",
    silenceDisasterSirenCd = "Silence Disaster Siren",
    accelerometerSeismicUpper = "ACCELEROMETER SEISMIC SENSOR",
    accelUnavailable = "Hardware Accelerometer Unavailable",
    accelArmed = "Active · Pattern Detector Armed",
    accelPaused = "Sensor Monitoring Paused",
    liveLinearVibrationLabel = "Live Linear Vibration",
    detectionsLabel = "Detections",
    accelMonitorsDesc = "Monitors sustained oscillatory tremor patterns (>=2.4 m/s² with axis reversals) to trigger instant siren alerts.",
    pWaveLeadUpper = "P-WAVE LEAD",
    sWaveShockUpper = "S-WAVE SHOCK",
    tsunamiBuoyUpper = "TSUNAMI BUOY",
    volcanicArrayUpper = "VOLCANIC ARRAY",
    primaryWaveElapsed = "Primary wave elapsed",
    statusLabel = "Status",
    decayingLabel = "Decaying",
    feltStrongIndoors = "Felt strong indoors",
    structuralLabel = "Structural",
    safeLabel = "Safe",
    secUnit = "sec",
    mSurgeUnit = "m surge",
    coastalMarineAdvisory = "Coastal marine advisory",
    baysideLabel = "Bayside",
    restrictedLabel = "Restricted",
    normalLabel = "Normal",
    tiltmeterLabel = "Tiltmeter",
    recentSeismicActivity = "Recent Seismic Activity",
    past48HoursLabel = "Past 48 Hours",
    emergencySafetyProtocol = "Emergency Safety Protocol",
    closeSosCd = "Close SOS",
    immediateShakeResponse = "Immediate Shake Response",
    sosChecklistBody = "1. Drop, Cover under sturdy furniture, and Hold On.\n2. Keep away from glass windows and unsecured shelving.\n3. Avoid coastal bayside zones during Tsunami Advisory.",
    fireAmbulanceLabel = "Fire / Ambulance",
    policeRescueLabel = "Police / Rescue",
    acknowledgeChecklistButton = "Acknowledge Safety Checklist",
    bmkgShakemapTab = "BMKG Shakemap",
    radar2dMapTab = "2D Radar Map",
    refreshBmkgSeismicCd = "Refresh BMKG Seismic Feed",
    autoUpdatingPrefix = "Auto-Updating",
    dragIslandsHint = "Drag islands · Tap M pin",

    settingsTitle = "Settings",
    appLanguageCardTitle = "App Language",
    appLanguageCardSubtitle = "Switch the entire app between English & full Bahasa Indonesia",
    languageEnglishOption = "English",
    languageIndonesianOption = "Bahasa Indonesia",
    gpsPrivacySyncTitle = "GPS Privacy & Real-Time Sync",
    gpsPrivacyShieldTitle = "GPS Location Privacy Shield",
    gpsPrivacyShieldSubtitle = "Locks telemetry to regency/kabupaten zone (~9-11 km); exact GPS discarded",
    powerAwareSyncTitle = "Power-Aware Background Sync",
    realTimeTelemetryStream = "Real-Time Telemetry Stream",
    adaptiveWorkManagerPrefix = "Adaptive WorkManager",
    encryptionHardeningTitle = "Encryption & Hardening",
    secSettingsAndGps = "User Settings & GPS Coordinates",
    secRoomDbCache = "Local Room Database Cache",
    secDomainAllowlist = "BMKG & Telemetry Domain Allowlist",
    secCoarseGpsGuard = "Coarse Regency GPS Guard",
    secBinaryIntegrity = "Binary & Runtime Integrity",
    secEncryptedPrefs = "EncryptedSharedPreferences · AES-256",
    secAesEncrypted = "AES Encrypted",
    secKeystoreEncrypted = "AndroidKeyStore AES-256 Encrypted",
    secStandard = "Standard",
    secHttpsOnlyBlocked = { count -> "HTTPS Only · $count Blocked" },
    secCoarseEnforced = "Enforced (~11 km · No Pinpoint)",
    secR8Hardened = "R8 Hardened · No Hook Detected",
    emergencyAlertsTitle = "Emergency Alerts",
    seismicPWaveAlertTitle = "Seismic P-Wave Alert",
    seismicPWaveAlertSubtitle = "Bypass silent mode for M 4.5+ shocks",
    typhoonFlashWatchTitle = "Typhoon Flash Watch",
    typhoonFlashWatchSubtitle = "Barometer drops and storm surge alerts",
    aqiSmogInversionTitle = "AQI Smog Inversion",
    aqiSmogInversionSubtitle = "Notify when PM2.5 exceeds 100 µg/m³",
    unitsTelemetryTitle = "Units & Telemetry Standards",
    temperatureUnitTitle = "Temperature Unit",
    currentScalePrefix = "Current scale",
    celsiusName = "Celsius",
    fahrenheitName = "Fahrenheit",
    tempMetricOption = "°C Metric",
    tempImperialOption = "°F Imperial",
    windVelocityTitle = "Wind Velocity",
    pressureScaleTitle = "Barometric Scale",
    displayParallaxTitle = "Display & Parallax",
    gyroParallaxTitle = "Gyroscope Camera Parallax",
    gyroParallaxSubtitle = "Tilt device to orbit 3D viewpoints",
    glassRefractionTitle = "Glass Refraction Depth",
    glassRefractionSubtitle = "Hardware accelerated blur",
    glassRefractionValue = "20px Clean",
    offlineDisasterCacheTitle = "Offline Disaster Cache",
    cacheActiveSize = "128 MB Active",
    faultLineVectorsLabel = "Fault line & topographic vectors",
    emergencyShelterMarkersLabel = "Emergency shelter GPS markers",
    offlineReadyLabel = "Offline Ready",
    refreshOfflineCacheButton = "Refresh Regional Offline Cache",
    syncingRegionalCache = "Syncing Regional Cache...",
)

private val IndonesianStrings = VeridiaStrings(
    language = AppLanguage.INDONESIAN,
    navHome = "Beranda",
    navWeather = "Cuaca",
    navAir = "Udara",
    navAlerts = "Siaga",
    navSettings = "Setelan",
    cdBack = "Kembali",
    cdSelectCity = "Pilih kota",
    cdOpenRadar = "Buka radar cuaca",
    cdSearchLocations = "Cari lokasi",
    cdRefreshLiveTelemetry = "Perbarui Telemetri Langsung",
    cdRefreshGps = "Perbarui GPS",
    dismissAlert = "Tutup peringatan",
    silenceSirenPrefix = "Matikan Sirine Bencana",

    liveBadge = "LANGSUNG",
    cachedBadge = "Tersimpan",
    offlineCachedFallback = "Luring · Data Tersimpan",
    offlineRoomCacheCd = "Penyimpanan Luring Lokal",
    syncingDots = "Menyelaraskan...",
    minAgoShort = "mnt lalu",
    hoursAgoShort = "jam lalu",
    daysAgoShort = "hr lalu",
    sourcePrefix = "Sumber",

    feelsLikePrefix = "Terasa seperti",
    heroFeelsWord = "Terasa",
    temperatureTileTitle = "SUHU UDARA",
    humidityTileTitle = "KELEMBAPAN",
    windSpeedTileTitle = "ANGIN",
    airQualityTileTitle = "KUALITAS UDARA",
    toggleTemperatureCd = "Buka Rincian Suhu",
    toggleHumidityCd = "Buka Rincian Kelembapan",
    toggleWindCd = "Buka Rincian Angin",
    highShort = "T",
    lowShort = "R",
    uvShort = "UV",
    fullAtmosphereLink = "Atmosfer Lengkap",
    humidityHumid = "Lembap",
    humidityBalanced = "Seimbang",
    humidityDry = "Kering",
    dewPointLabel = "Titik embun",
    precipChanceLabel = "Peluang hujan",
    solarAndMoistureLink = "Surya & Kelembapan",
    pressurePrefix = "Tekanan",
    gustsPrefix = "Hembusan",
    windRadarLink = "Radar Angin",
    livePrecipitationTrackerTitle = "PELACAK CURAH HUJAN LANGSUNG",
    livePrecipitationTrackerSubtitle = "OpenStreetMap (osmdroid) · RainViewer & BMKG",
    sevenDayForecastUpper = "PRAKIRAAN 7 HARI",
    fullDetailsLink = "Selengkapnya",
    inYourAreaBadge = "Di Daerah Anda",
    nearYourAreaKm = { km -> "Dekat Daerah Anda · $km km" },
    detailsButton = "Rincian",

    gpsPromptTitle = "Aktifkan Lokasi Zona Wilayah",
    gpsPromptBody = "Memungkinkan Veridia melacak prakiraan cuaca dan radar gempa tingkat kabupaten/kota Anda.",
    gpsDeniedBody = "Izin lokasi ditolak. Aktifkan lokasi perkiraan di Setelan atau tekan coba lagi.",
    gpsEnableCta = "Izinkan Lokasi",
    gpsRetryCta = "Coba Akses GPS",
    gpsOpenSettings = "Buka Setelan",
    gpsAcquiringZone = "Menangkap zona wilayah...",
    gpsProtectedZonePrefix = "GPS Zona Terlindungi",
    gpsViewingPrefix = "Menampilkan",
    gpsPrivacyProtectedCd = "Privasi GPS Dilindungi",
    gpsPrivacyMaskedFallback = "Zona disamarkan (~9 km)",
    gpsButtonLabel = "GPS",
    gpsErrorCouldNotAcquire = "Tidak dapat menangkap lokasi zona wilayah.",
    gpsErrorPlayServices = "Layanan lokasi perangkat tidak tersedia.",
    locationsTitle = "Daftar Lokasi",
    closeCitiesCd = "Tutup Daftar Lokasi",
    usePrivacyProtectedZone = "Gunakan Zona Terlindungi Privasi",
    acquiringMaskedZone = "Menangkap zona samaran...",
    maskedGridHint = "Zona ~9 km · koordinat presisi tidak pernah dikirim",
    tapToAllowApprox = "Ketuk untuk mengizinkan perkiraan lokasi",
    searchCityPlaceholder = "Cari kota atau bandara...",
    currentSelectionPrefix = "Pilihan Saat Ini",

    hourlyForecastTitle = "PRAKIRAAN 24 JAM",
    next24HoursBadge = "24 Jam Berikutnya",
    nowLabel = "Kini",

    adaptiveSolarThemeTitle = "TEMA SURYA ADAPTIF",
    activeSolarThemeCd = "Fase tema surya aktif",
    autoLabel = "Otomatis",
    manualLabel = "Manual",
    modeAuto = "Otomatis",
    modeDawn = "Fajar",
    modeDay = "Siang",
    modeDusk = "Sore",
    modeNight = "Malam",
    themeModeCdSuffix = "mode tema",
    phaseLabelSunriseSunset = "Terbit/Terbenam · Putih Keemasan",
    phaseLabelDay = "Pagi/Siang · Krem Hangat",
    phaseLabelAfternoonDusk = "Sore · Biru Sejuk",
    phaseLabelNight = "Malam · Mode Gelap",

    sunriseSunsetTitle = "TERBIT & TERBENAM",
    sunriseSunsetCd = "Jadwal matahari terbit dan terbenam",
    todayLabel = "Hari Ini",
    tomorrowLabel = "Besok",
    dayAfterTomorrowLabel = "Lusa",
    currentGpsZoneCd = "Zona GPS saat ini",
    selectedCityCd = "Lokasi kota terpilih",
    sunriseLabel = "Terbit",
    sunsetLabel = "Terbenam",
    sunriseTimeCd = "Waktu matahari terbit",
    sunsetTimeCd = "Waktu matahari terbenam",
    moonPhaseTitle = "FASE BULAN",
    litSuffix = "Terang",
    solarNoonLabel = "Tengah Hari",
    sunCycleTitle = "SIKLUS MATAHARI",
    sunCycleDaylightSuffix = "Siang Hari",
    nextSunriseIn = { time -> "Terbit Berikutnya $time" },
    nextSunsetIn = { time -> "Terbenam dalam $time" },

    weatherAtmosphereTitle = "Cuaca & Atmosfer",
    openRadarCd = "Buka Radar",
    radarButton = "Radar",
    realTimeAtmosphereBadge = "ATMOSFER WAKTU NYATA",
    surfaceTab = "Permukaan",
    midCloudTab = "Awan Tengah",
    upperStratumTab = "Lapisan Atas",
    atmosphereStratumUpper = "LAPISAN ATMOSFER",
    altitudeSurface = "Permukaan · 42m",
    altitudeMidCloud = "Awan Tengah · 4.200m",
    altitudeUpper = "Stratosfer · 12.000m",
    strataRangeSurface = "0 - 1.200m Permukaan Tanah",
    strataRangeMidCloud = "1.200 - 8.000m Troposfer",
    strataRangeUpper = "8.000 - 15.000m Lapisan Atas",
    upperStratosphereTitle = "Stratosfer Atas",
    midTroposphereTitle = "Awan Hujan Troposfer Tengah",
    groundBoundaryTitle = "Batas Permukaan & Curah Hujan",
    windUpper = "ANGIN",
    gustsLabel = "Hembusan",
    uvIndexUpper = "INDEKS UV",
    uvPeakWindowText = "Puncak radiasi surya 11.00 - 14.00",
    humidityUpper = "KELEMBAPAN",
    dewPointSentence = { dew -> "Titik embun saat ini ${dew}°C" },
    pressureUpper = "TEKANAN",
    referenceLabel = "Standar",
    referenceStdValue = "1.013,25 Std",
    forecast7DayTitle = "Prakiraan Cuaca Harian Terperinci",
    next168HoursLabel = "Hari Ini, Besok, Lusa & Pekan Ini",
    tapDayForDetailsHint = "Ketuk hari apa saja untuk melihat prakiraan lengkap",
    detailConditionLabel = "Kondisi",
    detailRainfallLabel = "Curah Hujan",
    detailRainChanceLabel = "Peluang Hujan",
    detailWindMaxLabel = "Angin Maks",
    detailHumidityLabel = "Kelembapan",
    detailUvPeakLabel = "UV Puncak",
    detailTempRangeLabel = "Rentang Suhu",

    windCalm = "Tenang",
    windLightBreeze = "Sepoi Lemah",
    windGentleBreeze = "Angin Sepoi",
    windModerateBreeze = "Angin Sedang",
    windFreshBreeze = "Angin Segar",
    windStrongWind = "Angin Kencang",
    cdWindIndicator = { spd, unit, dir, deg, cond ->
        "Angin $spd $unit dari arah $dir ($deg derajat), $cond"
    },

    mapLayerRainRadar = "Radar Hujan",
    mapLayerWindVector = "Arah Angin",
    mapLayerCloudsTemp = "Awan & Suhu",
    refreshPrecipitationMapCd = "Perbarui Peta Hujan BMKG",
    osmPrecipPrefix = "Hujan OSM",
    openFullRadarCd = "Buka Radar Hujan Penuh",
    zoomInMapCd = "Perbesar peta hujan",
    zoomOutMapCd = "Perkecil peta hujan",
    recenterMapCd = "Pusatkan ulang peta hujan",
    legendLight = "Ringan",
    presetRegion = "Wilayah",
    presetMetro = "Kota",
    presetLocal = "Lokal",
    stationPrecipLabel = "Hujan",
    stationWindLabel = "Angin",
    weatherRadarTitle = "Radar Cuaca",
    weatherRadarSubtitle = "Pantauan Doppler Curah Hujan & Satelit",
    liveSweepLabel = "Sapuan Langsung",
    radarTabRain = "Doppler Hujan",
    radarTabWind = "Arus Angin",
    radarTabSatellite = "Satelit",
    activePrecipCellUpper = "SEL HUJAN AKTIF",
    dbzRainCoreLabel = "dBZ Inti Hujan",
    radarSummaryFallback = "Pita awan hujan konvektif terdeteksi di stasiun pemantauan wilayah.",
    cellSpeedLabel = "Laju Sel",
    echoTopLabel = "Puncak Gema",
    accumulationLabel = "Akumulasi",
    surfaceRainUpper = "HUJAN PERMUKAAN",
    moderateSteadyRain = "Hujan sedang merata",
    next60mLabel = "60 Menit Depan",
    cellVectorUpper = "VEKTOR SEL",
    velocityLabel = "Kecepatan",
    reflectivityUpper = "REFLEKTIVITAS",
    dopplerBandEcho = "Gema pita Doppler",
    hailRiskLabel = "Risiko Es",
    irCloudTopUpper = "PUNCAK AWAN IR",
    deepConvectiveDeck = "Lapisan konvektif pekat",
    satelliteLabel = "Satelit",
    radarPlaybackTrackUpper = "LINIMASA PEMUTARAN RADAR",
    liveNowLabel = "Langsung (Kini)",
    minShort = "mnt",

    airQualitySmogTitle = "Kualitas Udara & Asap",
    aerosolSensorArray = "Jaringan Sensor Aerosol",
    sensorsLiveSuffix = "Sensor Aktif",
    regionalAqiUpper = "INDEKS KUALITAS UDARA WILAYAH",
    aqiScale0Good = "0 Baik",
    aqiScale50Mod = "50 Sedang",
    aqiScale150Poor = "150 Buruk",
    aqiScale300Alert = "300+ Bahaya",
    pm25ParticlesUpper = "PARTIKEL PM2.5",
    pm25WhoThreshold = "2,4x Ambang Batas WHO",
    pm10CoarseUpper = "PARTIKEL PM10",
    withinSafetyBand = "Dalam Batas Aman",
    carbonMonoxideUpper = "KARBON MONOKSIDA",
    normalUrbanBase = "Ambang Perkotaan Normal",
    nitrogenDioxideUpper = "NITROGEN DIOKSIDA",
    trafficCorridor = "Koridor Lalu Lintas",
    healthActivityProtocols = "Panduan Kesehatan & Aktivitas",
    wearMaskTitle = "Pakai Masker",
    wearMaskSubtitle = "N95 untuk pernapasan sensitif",
    limitCardioTitle = "Kurangi Kardio",
    limitCardioSubtitle = "Batasi olahraga luar ruang",
    closeVentsTitle = "Tutup Ventilasi",
    closeVentsSubtitle = "Nyalakan pembersih udara",
    districtComparisonTitle = "Perbandingan Wilayah",
    keySectorsSuffix = "Sektor Utama",
    aqiLayerUsAqi = "Indeks AQI",
    refreshAqiMapCd = "Perbarui Peta AQI",
    aqiMapSuffix = "AQI",
    zoomInLabel = "Perbesar",
    zoomOutLabel = "Perkecil",
    recenterLabel = "Pusatkan Peta",

    disasterMonitorTitle = "Pantauan Bencana",
    emergencySosCd = "SOS Darurat",
    sosHelpButton = "Bantuan SOS",
    depthLabel = "Kedalaman",
    peakPgaLabel = "PGA Puncak",
    tsunamiLabel = "Tsunami",
    civilDefenseSirenUpper = "SIRINE PERINGATAN DINI BENCANA",
    zoneEarlyWarningSubtitle = "Peringatan Dini & Notifikasi Zona",
    sirenActiveBadge = "SIRINE AKTIF",
    armedBadge = "SIAGA",
    sirenDescriptionForCity = { cityName ->
        "Mengirimkan notifikasi darurat & membunyikan sirine pertahanan sipil saat gempa bumi, potensi tsunami, badai ekstrem, atau udara berbahaya melanda $cityName."
    },
    disasterEarthquakeChip = "Gempa",
    disasterTsunamiChip = "Tsunami",
    disasterStormChip = "Badai",
    disasterAirHazardChip = "Udara Buruk",
    testSirenCd = "Uji Sirine",
    backgroundAlertServiceUpper = "LAYANAN PEMANTAUAN LATAR",
    scanNowButton = "Pindai",
    scanningLabel = "Memindai...",
    silenceDisasterSirenButton = "Matikan Sirine Bencana",
    silenceDisasterSirenCd = "Matikan Sirine Bencana",
    accelerometerSeismicUpper = "SENSOR GETARAN AKSELEROMETER",
    accelUnavailable = "Sensor Akselerometer Tidak Tersedia",
    accelArmed = "Aktif · Detektor Pola Getaran Siap",
    accelPaused = "Pemantauan Sensor Dijeda",
    liveLinearVibrationLabel = "Getaran Linear Langsung",
    detectionsLabel = "Deteksi",
    accelMonitorsDesc = "Memantau pola getaran osilasi beruntun (>=2,4 m/dt² dengan pembalikan sumbu) untuk memicu sirine instan.",
    pWaveLeadUpper = "GELOMBANG-P",
    sWaveShockUpper = "GETARAN-S",
    tsunamiBuoyUpper = "PELAMPUNG TSUNAMI",
    volcanicArrayUpper = "PANTAUAN VULKANIK",
    primaryWaveElapsed = "Waktu gelombang primer",
    statusLabel = "Status",
    decayingLabel = "Mereda",
    feltStrongIndoors = "Terasa kuat di dalam ruang",
    structuralLabel = "Struktur",
    safeLabel = "Aman",
    secUnit = "dtk",
    mSurgeUnit = "m pasang",
    coastalMarineAdvisory = "Peringatan gelombang pesisir",
    baysideLabel = "Pesisir",
    restrictedLabel = "Terbatas",
    normalLabel = "Normal",
    tiltmeterLabel = "Kemiringan",
    recentSeismicActivity = "Aktivitas Gempa Terkini",
    past48HoursLabel = "48 Jam Terakhir",
    emergencySafetyProtocol = "Protokol Keselamatan Darurat",
    closeSosCd = "Tutup SOS",
    immediateShakeResponse = "Tindakan Cepat Saat Guncangan",
    sosChecklistBody = "1. Merunduk, berlindung di bawah meja kokoh, dan bertahan.\n2. Jauhi kaca jendela serta rak yang tidak terikat.\n3. Hindari wilayah pesisir pantai saat Peringatan Tsunami.",
    fireAmbulanceLabel = "Damkar / Ambulans",
    policeRescueLabel = "Polisi / SAR",
    acknowledgeChecklistButton = "Pahami Panduan Keselamatan",
    bmkgShakemapTab = "Peta Guncangan BMKG",
    radar2dMapTab = "Peta Radar 2D",
    refreshBmkgSeismicCd = "Perbarui Data Gempa BMKG",
    autoUpdatingPrefix = "Pembaruan Otomatis",
    dragIslandsHint = "Geser peta · Ketuk pin M",

    settingsTitle = "Setelan",
    appLanguageCardTitle = "Bahasa Aplikasi",
    appLanguageCardSubtitle = "Ganti seluruh tampilan aplikasi ke Bahasa Indonesia penuh atau Inggris",
    languageEnglishOption = "English",
    languageIndonesianOption = "Bahasa Indonesia",
    gpsPrivacySyncTitle = "Privasi GPS & Sinkronisasi Langsung",
    gpsPrivacyShieldTitle = "Perisai Privasi Lokasi GPS",
    gpsPrivacyShieldSubtitle = "Mengunci telemetri ke zona kabupaten/kota (~9-11 km); koordinat persis dibuang",
    powerAwareSyncTitle = "Sinkronisasi Latar Hemat Daya",
    realTimeTelemetryStream = "Aliran Telemetri Waktu Nyata",
    adaptiveWorkManagerPrefix = "WorkManager Adaptif",
    encryptionHardeningTitle = "Enkripsi & Keamanan Aplikasi",
    secSettingsAndGps = "Setelan & Koordinat GPS",
    secRoomDbCache = "Basis Data Luring Lokal",
    secDomainAllowlist = "Daftar Domain Resmi BMKG & Cuaca",
    secCoarseGpsGuard = "Penjaga Zona GPS Kasar",
    secBinaryIntegrity = "Integritas Biner & Runtime",
    secEncryptedPrefs = "Preferensi Terenkripsi · AES-256",
    secAesEncrypted = "Terenkripsi AES",
    secKeystoreEncrypted = "Terenkripsi AES-256 KeyStore",
    secStandard = "Standar",
    secHttpsOnlyBlocked = { count -> "Khusus HTTPS · $count Diblokir" },
    secCoarseEnforced = "Aktif (~11 km · Tanpa Titik Persis)",
    secR8Hardened = "Terlindungi R8 · Aman",
    emergencyAlertsTitle = "Peringatan Darurat",
    seismicPWaveAlertTitle = "Peringatan Gelombang-P Gempa",
    seismicPWaveAlertSubtitle = "Bunyikan peringatan untuk gempa M 4,5+",
    typhoonFlashWatchTitle = "Siaga Badai & Gelombang",
    typhoonFlashWatchSubtitle = "Peringatan penurunan tekanan udara dan badai",
    aqiSmogInversionTitle = "Peringatan Asap & Polusi Udara",
    aqiSmogInversionSubtitle = "Beri tahu saat PM2.5 melebihi 100 µg/m³",
    unitsTelemetryTitle = "Satuan & Standar Telemetri",
    temperatureUnitTitle = "Satuan Suhu",
    currentScalePrefix = "Skala aktif",
    celsiusName = "Celcius",
    fahrenheitName = "Fahrenheit",
    tempMetricOption = "°C Metrik",
    tempImperialOption = "°F Imperial",
    windVelocityTitle = "Kecepatan Angin",
    pressureScaleTitle = "Skala Tekanan Udara",
    displayParallaxTitle = "Tampilan & Paralaks",
    gyroParallaxTitle = "Paralaks Giroskop",
    gyroParallaxSubtitle = "Miringkan ponsel untuk menggerakkan sudut pandang",
    glassRefractionTitle = "Kedalaman Efek Kaca",
    glassRefractionSubtitle = "Buram akselerasi perangkat keras",
    glassRefractionValue = "20px Jernih",
    offlineDisasterCacheTitle = "Penyimpanan Luring Bencana",
    cacheActiveSize = "128 MB Aktif",
    faultLineVectorsLabel = "Garis patahan & vektor topografi",
    emergencyShelterMarkersLabel = "Penanda lokasi evakuasi darurat",
    offlineReadyLabel = "Siap Luring",
    refreshOfflineCacheButton = "Perbarui Penyimpanan Luring Wilayah",
    syncingRegionalCache = "Menyimpan Data Wilayah...",
)

fun veridiaStringsFor(language: AppLanguage): VeridiaStrings = when (language) {
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.INDONESIAN -> IndonesianStrings
}

val LocalVeridiaStrings = compositionLocalOf { EnglishStrings }

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

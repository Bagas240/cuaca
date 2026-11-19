package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.model.CityLocation
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherConditionKind
import com.example.ui.components.HomeWeatherBackdrop
import com.example.ui.components.VeridiaCard
import com.example.ui.components.conditionIcon
import com.example.ui.components.conditionTint
import com.example.ui.components.formatPressureValue
import com.example.ui.components.formatTempString
import com.example.ui.components.formatTempValue
import com.example.ui.components.formatWindValue
import java.util.Locale
import kotlin.math.roundToInt
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.PastelAccent
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelDangerBg
import com.example.ui.theme.PastelDangerBorder
import com.example.ui.theme.PastelDangerText
import com.example.ui.theme.PastelMint
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelSubtle
import com.example.ui.theme.PastelSunset
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelTextMain
import com.example.ui.theme.PastelTextSub
import com.example.ui.theme.SecondaryGreenBright
import com.example.ui.theme.SecondaryGreenDark
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.DashboardMetricCard
import com.example.ui.viewmodel.VeridiaUiState

@Composable
fun HomeDashboardScreen(
    state: VeridiaUiState,
    gyroTilt: Offset,
    onRefreshNow: () -> Unit,
    onOpenCityModal: () -> Unit,
    onCloseCityModal: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectCity: (CityLocation) -> Unit,
    onPermissionResult: (Boolean) -> Unit,
    onSyncPermissionState: () -> Unit,
    onFetchCurrentGpsWeather: () -> Unit,
    onToggleMetricCard: (DashboardMetricCard) -> Unit,
    onOpenDisasterMonitor: () -> Unit,
    onOpenAirQuality: () -> Unit,
    onOpenDetailedWeather: () -> Unit,
    onOpenRadar: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasAutoPrompted by rememberSaveable { mutableStateOf(false) }

    // Enforce coarse-only permission so exact pinpoint GPS is never requested or exposed
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onPermissionResult(granted)
    }

    fun requestLocationPermissions() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onSyncPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (!state.hasLocationPermission && !hasAutoPrompted) {
            hasAutoPrompted = true
            requestLocationPermissions()
        }
    }

    val weather = state.weather
    val tempUnit = state.settings.temperatureUnit
    val windUnit = state.settings.windUnit
    val pressureUnit = state.settings.pressureUnit
    val (windVal, windUnitLabel) = formatWindValue(weather.windSpeedKmh, windUnit)
    val (gustVal, _) = formatWindValue(weather.windGustKmh, windUnit)
    val (pressureVal, pressureUnitLabel) = formatPressureValue(weather.pressureHpa, pressureUnit)
    val todayMin = weather.dailyForecast.firstOrNull()?.minTempC ?: (weather.currentTempC - 4)
    val todayMax = weather.dailyForecast.firstOrNull()?.maxTempC ?: (weather.currentTempC + 3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelSky),
        contentAlignment = Alignment.TopCenter,
    ) {
        // Real-time Photographic Sky Backdrop & Dynamic Weather Particle Layer
        HomeWeatherBackdrop(
            conditionKind = weather.conditionKind,
            windSpeedKmh = weather.windSpeedKmh,
            precipChancePercent = weather.precipChancePercent,
            gyroTilt = gyroTilt,
            stageHeight = 540.dp,
        )

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Foreground App Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Location Selector Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onOpenCityModal)
                            .testTag("location_selector_pill"),
                        shape = RoundedCornerShape(50),
                        color = PastelSurface,
                        border = BorderStroke(1.dp, PastelBorder),
                        shadowElevation = 2.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (state.isGpsActive) Icons.Rounded.MyLocation else Icons.Rounded.NearMe,
                                contentDescription = stringResource(R.string.cd_select_city),
                                tint = PastelPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.selectedCity.name,
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                                color = PastelTextMain,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Rounded.ExpandMore,
                                contentDescription = null,
                                tint = PastelTextSub,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    // Header Actions: Live Refresh, Radar, Search
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable(enabled = !state.isSyncing, onClick = onRefreshNow)
                                .testTag("home_refresh_button"),
                            shape = CircleShape,
                            color = PastelSurface,
                            border = BorderStroke(1.dp, PastelBorder),
                            shadowElevation = 2.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (state.isSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = PastelPrimary,
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = "Refresh Live Telemetry",
                                        tint = PastelPrimary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onOpenRadar)
                                .testTag("home_radar_button"),
                            shape = CircleShape,
                            color = PastelSurface,
                            border = BorderStroke(1.dp, PastelBorder),
                            shadowElevation = 2.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Radar,
                                    contentDescription = stringResource(R.string.cd_open_radar),
                                    tint = PastelPrimary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onOpenCityModal)
                                .testTag("search_locations_button"),
                            shape = CircleShape,
                            color = PastelSurface,
                            border = BorderStroke(1.dp, PastelBorder),
                            shadowElevation = 2.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = stringResource(R.string.cd_search_locations),
                                    tint = PastelTextMain,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Hero Weather Focus Readout
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = formatTempValue(weather.currentTempC, tempUnit).toString(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 60.sp,
                                shadow = Shadow(
                                    color = Color(0x80000000),
                                    offset = Offset(0f, 3f),
                                    blurRadius = 16f,
                                ),
                            ),
                            color = Color.White,
                        )
                        Text(
                            text = "°",
                            style = MaterialTheme.typography.displayMedium.copy(
                                shadow = Shadow(
                                    color = Color(0x80000000),
                                    offset = Offset(0f, 2f),
                                    blurRadius = 12f,
                                ),
                            ),
                            color = Color.White.copy(alpha = 0.92f),
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }

                    Text(
                        text = "Feels like ${formatTempString(weather.feelsLikeC, tempUnit)} · ${weather.windDirectionLabel} ${windVal} $windUnitLabel",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            shadow = Shadow(
                                color = Color(0x66000000),
                                offset = Offset(0f, 1f),
                                blurRadius = 8f,
                            ),
                        ),
                        color = Color.White.copy(alpha = 0.95f),
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PastelSurface,
                            border = BorderStroke(1.dp, PastelBorder),
                            shadowElevation = 2.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = conditionIcon(weather.conditionKind),
                                    contentDescription = null,
                                    tint = PastelPrimary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = weather.conditionLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PastelTextMain,
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PastelSurface,
                            border = BorderStroke(1.dp, PastelBorder),
                            shadowElevation = 2.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WaterDrop,
                                    contentDescription = null,
                                    tint = PastelPrimary,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = weather.precipStatusLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = PastelTextSub,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // BMKG / Live Auto-Sync or Offline Room Cache Attribution Chip
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (state.isOfflineMode) Color(0xCC451A03) else Color(0x990F172A),
                        border = BorderStroke(
                            1.dp,
                            if (state.isOfflineMode) Color(0xFFFCD34D).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.22f),
                        ),
                        modifier = Modifier.testTag("weather_source_badge"),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = if (state.isOfflineMode) Icons.Rounded.CloudOff else Icons.Rounded.Public,
                                contentDescription = if (state.isOfflineMode) "Offline Room Cache" else "Live Telemetry Source",
                                tint = if (state.isOfflineMode) Color(0xFFFCD34D) else Color(0xFF7DD3FC),
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Source: ${weather.dataSourceLabel} · ${state.lastUpdatedLabel}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                            )
                        }
                    }
                }
            }

            // Hyper-Local GPS Coordinates & Location Permission Card
            item {
                HyperLocalGpsCard(
                    hasLocationPermission = state.hasLocationPermission,
                    locationPermissionDenied = state.locationPermissionDenied,
                    isLocatingGps = state.isLocatingGps,
                    isGpsActive = state.isGpsActive,
                    isLiveSynced = weather.isLiveSynced,
                    dataSourceLabel = weather.dataSourceLabel,
                    coordinatesLabel = state.gpsCoordinatesLabel,
                    errorMessage = state.gpsErrorMessage,
                    cityName = state.selectedCity.name,
                    onRequestPermission = ::requestLocationPermissions,
                    onRefreshGps = onFetchCurrentGpsWeather,
                    onOpenAppSettings = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Active Seismic Notice Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenDisasterMonitor)
                        .testTag("seismic_notice_banner"),
                    shape = RoundedCornerShape(18.dp),
                    color = PastelDangerBg,
                    border = BorderStroke(1.dp, PastelDangerBorder),
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DangerCrimson.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sensors,
                                contentDescription = null,
                                tint = PastelDangerText,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = state.seismic.bannerTitle.uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PastelDangerText,
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = DangerCrimson.copy(alpha = 0.12f),
                                ) {
                                    Text(
                                        text = state.seismic.bannerMagnitude,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PastelDangerText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Text(
                                text = state.seismic.bannerSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextMain,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenDisasterMonitor)
                                .testTag("seismic_details_button"),
                            shape = RoundedCornerShape(12.dp),
                            color = PastelSurface,
                            border = BorderStroke(1.dp, PastelDangerBorder),
                        ) {
                            Text(
                                text = "Details",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = PastelDangerText,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            )
                        }
                    }
                }
            }

            // Interactive 2x2 Current Conditions Cards (Temperature, Humidity, Wind Speed, Air Quality)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Interactive Card 1: Temperature
                        val tempExpanded = state.expandedMetricCard == DashboardMetricCard.TEMPERATURE
                        VeridiaCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_temperature_tile"),
                            borderColor = if (tempExpanded) PastelPrimary.copy(alpha = 0.5f) else PastelBorder,
                            onClick = { onToggleMetricCard(DashboardMetricCard.TEMPERATURE) },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Thermostat,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "TEMPERATURE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PastelTextSub,
                                    )
                                }
                                Icon(
                                    imageVector = if (tempExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = "Toggle Temperature Details",
                                    tint = PastelTextSub,
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = formatTempString(weather.currentTempC, tempUnit),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = PastelTextMain,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Feels ${formatTempString(weather.feelsLikeC, tempUnit)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelTextSub,
                                    modifier = Modifier.padding(bottom = 5.dp),
                                )
                            }
                            Text(
                                text = "H ${formatTempString(todayMax, tempUnit)} · L ${formatTempString(todayMin, tempUnit)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextSub,
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { ((weather.currentTempC + 10) / 50f).coerceIn(0.15f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = WarningAmber,
                                trackColor = PastelSubtle,
                                strokeCap = StrokeCap.Round,
                            )

                            AnimatedVisibility(
                                visible = tempExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut(),
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    HorizontalDivider(color = PastelBorder)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "UV ${String.format(Locale.US, "%.1f", weather.uvIndex)} (${weather.uvLevelLabel})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PastelTextMain,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable(onClick = onOpenDetailedWeather)
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "Full Atmosphere",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PastelPrimary,
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            tint = PastelPrimary,
                                            modifier = Modifier.size(12.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // Interactive Card 2: Humidity
                        val humidityExpanded = state.expandedMetricCard == DashboardMetricCard.HUMIDITY
                        VeridiaCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_humidity_tile"),
                            borderColor = if (humidityExpanded) PastelPrimary.copy(alpha = 0.5f) else PastelBorder,
                            onClick = { onToggleMetricCard(DashboardMetricCard.HUMIDITY) },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.WaterDrop,
                                        contentDescription = null,
                                        tint = PastelPrimary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "HUMIDITY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PastelTextSub,
                                    )
                                }
                                Icon(
                                    imageVector = if (humidityExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = "Toggle Humidity Details",
                                    tint = PastelTextSub,
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${weather.humidityPercent}%",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = PastelTextMain,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        weather.humidityPercent >= 75 -> "Humid"
                                        weather.humidityPercent >= 40 -> "Balanced"
                                        else -> "Dry"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelPrimary,
                                    modifier = Modifier.padding(bottom = 5.dp),
                                )
                            }
                            val dewPointDisplay = when (tempUnit) {
                                TemperatureUnit.CELSIUS -> "${String.format(Locale.US, "%.1f", weather.dewPointC)}°C"
                                TemperatureUnit.FAHRENHEIT -> "${((weather.dewPointC * 9.0 / 5.0) + 32.0).roundToInt()}°F"
                            }
                            Text(
                                text = "Dew point $dewPointDisplay",
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextSub,
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { (weather.humidityPercent / 100f).coerceIn(0.1f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = PastelPrimary,
                                trackColor = PastelSky,
                                strokeCap = StrokeCap.Round,
                            )

                            AnimatedVisibility(
                                visible = humidityExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut(),
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    HorizontalDivider(color = PastelBorder)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Precip chance: ${weather.precipChancePercent}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PastelTextMain,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable(onClick = onOpenDetailedWeather)
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "Solar & Moisture",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PastelPrimary,
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            tint = PastelPrimary,
                                            modifier = Modifier.size(12.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Interactive Card 3: Wind Speed
                        val windExpanded = state.expandedMetricCard == DashboardMetricCard.WIND_SPEED
                        VeridiaCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_wind_tile"),
                            borderColor = if (windExpanded) PastelPrimary.copy(alpha = 0.5f) else PastelBorder,
                            onClick = { onToggleMetricCard(DashboardMetricCard.WIND_SPEED) },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.NearMe,
                                        contentDescription = null,
                                        tint = PastelPrimary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "WIND SPEED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PastelPrimary,
                                    )
                                }
                                Icon(
                                    imageVector = if (windExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = "Toggle Wind Details",
                                    tint = PastelTextSub,
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = windVal,
                                    style = MaterialTheme.typography.displaySmall,
                                    color = PastelTextMain,
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = windUnitLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PastelTextSub,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                            }
                            Text(
                                text = "${weather.windDirectionLabel} · $pressureVal $pressureUnitLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextSub,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { (weather.windSpeedKmh / 45f).coerceIn(0.15f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = PastelPrimary,
                                trackColor = PastelSky,
                                strokeCap = StrokeCap.Round,
                            )

                            AnimatedVisibility(
                                visible = windExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut(),
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    HorizontalDivider(color = PastelBorder)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Gusts $gustVal $windUnitLabel · ${weather.windBearingDeg}°",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PastelTextMain,
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable(onClick = onOpenRadar)
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "Wind Radar",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PastelPrimary,
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            tint = PastelPrimary,
                                            modifier = Modifier.size(12.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // Interactive Card 4: Air Quality (Pastel Mint)
                        VeridiaCard(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_aqi_tile"),
                            containerColor = PastelMint,
                            borderColor = SecondaryGreenBright.copy(alpha = 0.35f),
                            onClick = onOpenAirQuality,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Air,
                                        contentDescription = null,
                                        tint = SecondaryGreenDark,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "AIR QUALITY",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SecondaryGreenDark,
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = SecondaryGreenDark,
                                    modifier = Modifier.size(14.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = state.airQuality.homeCardAqi.toString(),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = PastelTextMain,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = state.airQuality.regionalCategory,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecondaryGreenDark,
                                    modifier = Modifier.padding(bottom = 5.dp),
                                )
                            }
                            Text(
                                text = "PM2.5: ${String.format(Locale.US, "%.1f", state.airQuality.homeCardPm25)} µg/m³",
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextMain,
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { (state.airQuality.homeCardAqi / 150f).coerceIn(0.1f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = PastelAccent,
                                trackColor = Color.White,
                                strokeCap = StrokeCap.Round,
                            )
                        }
                    }
                }
            }

            // Hourly Forecast Horizontal Widget
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = PastelTextSub,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HOURLY FORECAST",
                                style = MaterialTheme.typography.labelSmall,
                                color = PastelTextSub,
                            )
                        }
                        Text(
                            text = "Next 24 Hours",
                            style = MaterialTheme.typography.labelSmall,
                            color = PastelTextSub,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PastelBorder.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        weather.hourlyForecast.forEach { hour ->
                            val bg = when {
                                hour.isCurrent -> PastelSky.copy(alpha = 0.7f)
                                hour.isSunset -> WarningAmber.copy(alpha = 0.12f)
                                else -> Color.Transparent
                            }
                            Column(
                                modifier = Modifier
                                    .width(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = hour.timeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (hour.isCurrent || hour.isSunset) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = if (hour.isSunset) PastelSunset else PastelTextSub,
                                )
                                Icon(
                                    imageVector = conditionIcon(hour.condition),
                                    contentDescription = null,
                                    tint = conditionTint(hour.condition),
                                    modifier = Modifier.size(22.dp),
                                )
                                Text(
                                    text = formatTempString(hour.tempC, tempUnit),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = if (hour.isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = PastelTextMain,
                                )
                            }
                        }
                    }
                }
            }

            // 7-Day Summary Widget
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                tint = PastelTextSub,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "7-DAY FORECAST",
                                style = MaterialTheme.typography.labelSmall,
                                color = PastelTextSub,
                            )
                        }
                        Text(
                            text = "Full Details",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = PastelPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onOpenDetailedWeather)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("home_full_details_link"),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PastelBorder.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        weather.dailyForecast.take(3).forEach { day ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = day.dayLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (day.dayLabel == "Today") FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = if (day.dayLabel == "Today") PastelTextMain else PastelTextSub,
                                    modifier = Modifier.width(56.dp),
                                )
                                Row(
                                    modifier = Modifier.width(72.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = conditionIcon(day.condition),
                                        contentDescription = null,
                                        tint = conditionTint(day.condition),
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${day.precipPercent}%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (day.precipPercent >= 60) FontWeight.Bold else FontWeight.Medium,
                                        ),
                                        color = if (day.precipPercent >= 60) PastelPrimary else PastelTextSub,
                                    )
                                }
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = formatTempString(day.minTempC, tempUnit),
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceMonoFamily),
                                        color = PastelTextSub,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(86.dp)
                                            .height(6.dp)
                                            .clip(CircleShape)
                                            .background(PastelSubtle),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.65f)
                                                .height(6.dp)
                                                .align(Alignment.Center)
                                                .clip(CircleShape)
                                                .background(PastelPrimary),
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatTempString(day.maxTempC, tempUnit),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = SpaceMonoFamily,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = PastelTextMain,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Locations Modal Bottom Sheet
        if (state.isCityModalOpen) {
            LocationsModalSheet(
                searchQuery = state.searchQuery,
                cities = state.searchResults,
                selectedCityId = state.selectedCity.id,
                hasLocationPermission = state.hasLocationPermission,
                isLocatingGps = state.isLocatingGps,
                isGpsActive = state.isGpsActive,
                gpsCoordinatesLabel = state.gpsCoordinatesLabel,
                onUseCurrentGps = {
                    if (state.hasLocationPermission) {
                        onFetchCurrentGpsWeather()
                    } else {
                        onCloseCityModal()
                        requestLocationPermissions()
                    }
                },
                onSearchQueryChange = onSearchQueryChange,
                onSelectCity = onSelectCity,
                onDismiss = onCloseCityModal,
            )
        }
    }
}

@Composable
private fun HyperLocalGpsCard(
    hasLocationPermission: Boolean,
    locationPermissionDenied: Boolean,
    isLocatingGps: Boolean,
    isGpsActive: Boolean,
    isLiveSynced: Boolean,
    dataSourceLabel: String,
    coordinatesLabel: String?,
    errorMessage: String?,
    cityName: String,
    onRequestPermission: () -> Unit,
    onRefreshGps: () -> Unit,
    onOpenAppSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VeridiaCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gps_status_card"),
        containerColor = PastelSurface.copy(alpha = 0.95f),
        borderColor = if (isGpsActive) PastelPrimary.copy(alpha = 0.3f) else PastelBorder,
    ) {
        if (!hasLocationPermission) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PastelPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GpsFixed,
                        contentDescription = null,
                        tint = PastelPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.gps_prompt_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = PastelTextMain,
                    )
                    Text(
                        text = if (locationPermissionDenied) {
                            stringResource(R.string.gps_denied_body)
                        } else {
                            stringResource(R.string.gps_prompt_body)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = PastelTextSub,
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = PastelPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gps_enable_button"),
                ) {
                    Text(
                        text = if (locationPermissionDenied) "Retry GPS Access" else stringResource(R.string.gps_enable_cta),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                if (locationPermissionDenied) {
                    OutlinedButton(
                        onClick = onOpenAppSettings,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PastelBorder),
                        modifier = Modifier.testTag("gps_open_settings_button"),
                    ) {
                        Text(
                            text = stringResource(R.string.gps_open_settings),
                            color = PastelTextMain,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGpsActive) PastelAccent.copy(alpha = 0.14f)
                                else PastelPrimary.copy(alpha = 0.12f),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isLocatingGps) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = PastelPrimary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = null,
                                tint = if (isGpsActive) SecondaryGreenDark else PastelPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when {
                                    isLocatingGps -> "Acquiring coarse zone..."
                                    isGpsActive -> "Protected Zone GPS · $cityName"
                                    else -> "Viewing $cityName"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PastelTextMain,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Icon(
                                imageVector = Icons.Rounded.Shield,
                                contentDescription = "GPS Privacy Protected",
                                tint = SecondaryGreenDark,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                        Text(
                            text = when {
                                errorMessage != null -> errorMessage
                                isGpsActive && coordinatesLabel != null ->
                                    "$coordinatesLabel · ${if (isLiveSynced) dataSourceLabel else "Syncing..."}"
                                else -> "Privacy-masked (~9 km) · $dataSourceLabel"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (errorMessage != null) PastelDangerText else PastelTextSub,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isLocatingGps, onClick = onRefreshGps)
                        .testTag("gps_refresh_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = PastelSky,
                    border = BorderStroke(1.dp, PastelPrimary.copy(alpha = 0.25f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh_gps),
                            tint = PastelPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GPS",
                            style = MaterialTheme.typography.labelSmall,
                            color = PastelPrimary,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationsModalSheet(
    searchQuery: String,
    cities: List<CityLocation>,
    selectedCityId: String,
    hasLocationPermission: Boolean,
    isLocatingGps: Boolean,
    isGpsActive: Boolean,
    gpsCoordinatesLabel: String?,
    onUseCurrentGps: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectCity: (CityLocation) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PastelSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Locations",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PastelTextMain,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_city_modal_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Cities",
                        tint = PastelTextSub,
                    )
                }
            }

            // Use Current GPS Location Row
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !isLocatingGps, onClick = onUseCurrentGps)
                    .testTag("gps_modal_option"),
                shape = RoundedCornerShape(16.dp),
                color = if (isGpsActive) PastelMint else PastelSky.copy(alpha = 0.6f),
                border = BorderStroke(
                    1.dp,
                    if (isGpsActive) PastelAccent.copy(alpha = 0.5f) else PastelPrimary.copy(alpha = 0.3f),
                ),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = null,
                            tint = if (isGpsActive) SecondaryGreenDark else PastelPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Use Privacy-Protected Zone",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PastelTextMain,
                            )
                            Text(
                                text = when {
                                    isLocatingGps -> "Acquiring masked zone..."
                                    isGpsActive && gpsCoordinatesLabel != null -> gpsCoordinatesLabel
                                    hasLocationPermission -> "Masked ~9 km grid · exact GPS never exposed"
                                    else -> "Tap to allow approximate location"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isGpsActive) SecondaryGreenDark else PastelPrimary,
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isGpsActive) Icons.Rounded.CheckCircle else Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = if (isGpsActive) SecondaryGreenDark else PastelPrimary,
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search city or airport...",
                        style = MaterialTheme.typography.bodySmall,
                        color = PastelTextSub,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = PastelTextSub,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PastelSubtle,
                    unfocusedContainerColor = PastelSubtle,
                    focusedBorderColor = PastelPrimary,
                    unfocusedBorderColor = PastelBorder,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("city_search_input"),
            )

            cities.forEach { city ->
                val isSelected = !isGpsActive && city.id == selectedCityId
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectCity(city) }
                        .testTag("city_option_${city.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) PastelSky.copy(alpha = 0.65f) else PastelSurface,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) PastelPrimary.copy(alpha = 0.35f) else PastelBorder,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "${city.name}, ${city.country}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                ),
                                color = PastelTextMain,
                            )
                            Text(
                                text = if (isSelected) {
                                    "Current Selection · ${city.summaryTempC}°C"
                                } else {
                                    "${city.summaryCondition} · ${city.summaryTempC}°C"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) PastelPrimary else PastelTextSub,
                            )
                        }
                        Icon(
                            imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = if (isSelected) PastelPrimary else PastelTextSub,
                        )
                    }
                }
            }
        }
    }
}

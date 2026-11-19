package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.ActiveDisasterAlertOverlay
import com.example.ui.components.VeridiaBottomDock
import com.example.ui.components.VeridiaRoute
import com.example.ui.components.rememberGyroParallax
import com.example.ui.navigation.LocalNavStateController
import com.example.ui.navigation.ProvideNavDestinationState
import com.example.ui.navigation.rememberNavStateController
import com.example.ui.screens.AirQualityMapScreen
import com.example.ui.screens.DetailedWeatherScreen
import com.example.ui.screens.DisasterMonitorScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WeatherRadarScreen
import com.example.ui.theme.VeridiaTheme
import com.example.ui.viewmodel.VeridiaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeridiaTheme {
                VeridiaApp()
            }
        }
    }
}

@Composable
fun VeridiaApp(
    viewModel: VeridiaViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navStateController = rememberNavStateController(navController)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: VeridiaRoute.HOME
    val gyroTilt by rememberGyroParallax(enabled = state.settings.gyroParallaxEnabled)

    var lastTopRoute by remember { mutableStateOf(currentRoute) }
    val homeWeatherSpringScale = remember { Animatable(1f) }

    LaunchedEffect(currentRoute) {
        val isHomeWeatherToggle = (lastTopRoute == VeridiaRoute.HOME && currentRoute == VeridiaRoute.WEATHER) ||
            (lastTopRoute == VeridiaRoute.WEATHER && currentRoute == VeridiaRoute.HOME)
        lastTopRoute = currentRoute
        if (isHomeWeatherToggle) {
            homeWeatherSpringScale.snapTo(0.975f)
            homeWeatherSpringScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }

    var requestedNotif by rememberSaveable { mutableStateOf(false) }
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !requestedNotif) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestedNotif = true
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val iosSpringSlide = spring<IntOffset>(
        dampingRatio = 0.72f,
        stiffness = Spring.StiffnessMediumLow,
    )
    val iosSpringScale = spring<Float>(
        dampingRatio = 0.68f,
        stiffness = Spring.StiffnessMediumLow,
    )

    CompositionLocalProvider(LocalNavStateController provides navStateController) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = VeridiaRoute.HOME,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = homeWeatherSpringScale.value
                        scaleY = homeWeatherSpringScale.value
                    },
                enterTransition = {
                    val fromIdx = VeridiaRoute.indexOf(initialState.destination.route ?: VeridiaRoute.HOME)
                    val toIdx = VeridiaRoute.indexOf(targetState.destination.route ?: VeridiaRoute.HOME)
                    val forward = toIdx >= fromIdx
                    slideInHorizontally(
                        animationSpec = iosSpringSlide,
                        initialOffsetX = { fullWidth -> if (forward) (fullWidth * 0.28f).toInt() else -(fullWidth * 0.28f).toInt() },
                    ) + scaleIn(
                        initialScale = 0.94f,
                        animationSpec = iosSpringScale,
                    ) + fadeIn(animationSpec = tween(durationMillis = 220))
                },
                exitTransition = {
                    val fromIdx = VeridiaRoute.indexOf(initialState.destination.route ?: VeridiaRoute.HOME)
                    val toIdx = VeridiaRoute.indexOf(targetState.destination.route ?: VeridiaRoute.HOME)
                    val forward = toIdx >= fromIdx
                    slideOutHorizontally(
                        animationSpec = iosSpringSlide,
                        targetOffsetX = { fullWidth -> if (forward) -(fullWidth * 0.16f).toInt() else (fullWidth * 0.16f).toInt() },
                    ) + scaleOut(
                        targetScale = 0.96f,
                        animationSpec = iosSpringScale,
                    ) + fadeOut(animationSpec = tween(durationMillis = 190))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        animationSpec = iosSpringSlide,
                        initialOffsetX = { fullWidth -> -(fullWidth * 0.26f).toInt() },
                    ) + scaleIn(
                        initialScale = 0.95f,
                        animationSpec = iosSpringScale,
                    ) + fadeIn(animationSpec = tween(durationMillis = 220))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        animationSpec = iosSpringSlide,
                        targetOffsetX = { fullWidth -> (fullWidth * 0.28f).toInt() },
                    ) + scaleOut(
                        targetScale = 0.94f,
                        animationSpec = iosSpringScale,
                    ) + fadeOut(animationSpec = tween(durationMillis = 190))
                },
            ) {
                composable(VeridiaRoute.HOME) {
                    ProvideNavDestinationState(VeridiaRoute.HOME, navStateController) {
                        HomeDashboardScreen(
                            state = state,
                            gyroTilt = gyroTilt,
                            onRefreshNow = viewModel::refreshLiveTelemetryNow,
                            onOpenCityModal = viewModel::openCityModal,
                            onCloseCityModal = viewModel::closeCityModal,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onSelectCity = viewModel::selectCity,
                            onPermissionResult = viewModel::onLocationPermissionResult,
                            onSyncPermissionState = viewModel::syncLocationPermissionState,
                            onFetchCurrentGpsWeather = viewModel::fetchWeatherForCurrentGps,
                            onToggleMetricCard = viewModel::toggleMetricCard,
                            onOpenDisasterMonitor = { navigateTopLevel(VeridiaRoute.ALERTS) },
                            onOpenAirQuality = { navigateTopLevel(VeridiaRoute.AIR) },
                            onOpenDetailedWeather = { navigateTopLevel(VeridiaRoute.WEATHER) },
                            onOpenRadar = { navController.navigate(VeridiaRoute.RADAR) },
                        )
                    }
                }
                composable(VeridiaRoute.WEATHER) {
                    ProvideNavDestinationState(VeridiaRoute.WEATHER, navStateController) {
                        DetailedWeatherScreen(
                            state = state,
                            onBack = { navController.popBackStack() },
                            onSelectStratum = viewModel::selectStratum,
                            onSelectWeatherMapLayer = viewModel::selectWeatherMapLayer,
                            onRefreshNow = viewModel::refreshLiveTelemetryNow,
                            onOpenRadar = { navController.navigate(VeridiaRoute.RADAR) },
                        )
                    }
                }
                composable(VeridiaRoute.AIR) {
                    ProvideNavDestinationState(VeridiaRoute.AIR, navStateController) {
                        AirQualityMapScreen(
                            state = state,
                            gyroTilt = gyroTilt,
                            onSelectAqiLayer = viewModel::selectAqiLayer,
                            onSelectAqiSector = viewModel::selectAqiSector,
                            onRefreshNow = viewModel::refreshLiveTelemetryNow,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable(VeridiaRoute.ALERTS) {
                    ProvideNavDestinationState(VeridiaRoute.ALERTS, navStateController) {
                        DisasterMonitorScreen(
                            state = state,
                            gyroTilt = gyroTilt,
                            onRefreshNow = viewModel::refreshLiveTelemetryNow,
                            onBack = { navController.popBackStack() },
                            onOpenSosModal = viewModel::openSosModal,
                            onCloseSosModal = viewModel::closeSosModal,
                            onTriggerSampleAlert = viewModel::triggerSampleDisasterAlert,
                            onStopSiren = viewModel::stopDisasterSiren,
                            onToggleSeismicSensor = viewModel::setSeismicSensorMonitoring,
                        )
                    }
                }
                composable(VeridiaRoute.RADAR) {
                    ProvideNavDestinationState(VeridiaRoute.RADAR, navStateController) {
                        WeatherRadarScreen(
                            state = state,
                            gyroTilt = gyroTilt,
                            onBack = { navController.popBackStack() },
                            onSelectRadarLayer = viewModel::selectRadarLayer,
                            onPlaybackOffsetChange = viewModel::setRadarPlaybackOffset,
                            onRefreshNow = viewModel::refreshLiveTelemetryNow,
                        )
                    }
                }
                composable(VeridiaRoute.SETTINGS) {
                    ProvideNavDestinationState(VeridiaRoute.SETTINGS, navStateController) {
                        SettingsScreen(
                            state = state,
                            onBack = { navController.popBackStack() },
                            onSeismicAlertChange = viewModel::setSeismicPWaveAlert,
                            onTyphoonWatchChange = viewModel::setTyphoonFlashWatch,
                            onAqiAlertChange = viewModel::setAqiSmogInversion,
                            onTempUnitChange = viewModel::setTemperatureUnit,
                            onWindUnitChange = viewModel::setWindUnit,
                            onPressureUnitChange = viewModel::setPressureUnit,
                            onGyroParallaxChange = viewModel::setGyroParallaxEnabled,
                            onGpsPrivacyModeChange = viewModel::setGpsPrivacyMode,
                            onBackgroundSyncChange = viewModel::setBackgroundSyncEnabled,
                            onRefreshCache = viewModel::refreshOfflineCache,
                        )
                    }
                }
            }

            ActiveDisasterAlertOverlay(
                alert = state.activeDisasterAlert,
                isSirenPlaying = state.isSirenPlaying,
                onSilenceSiren = viewModel::stopDisasterSiren,
                onDismiss = viewModel::dismissActiveDisasterAlert,
                onOpenDisasterMonitor = { navigateTopLevel(VeridiaRoute.ALERTS) },
                modifier = Modifier.align(Alignment.TopCenter),
            )

            VeridiaBottomDock(
                currentRoute = currentRoute,
                onNavigate = ::navigateTopLevel,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

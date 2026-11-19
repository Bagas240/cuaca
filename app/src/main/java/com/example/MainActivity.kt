package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.VeridiaBottomDock
import com.example.ui.components.VeridiaRoute
import com.example.ui.components.rememberGyroParallax
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
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: VeridiaRoute.HOME
    val gyroTilt by rememberGyroParallax(enabled = state.settings.gyroParallaxEnabled)

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = VeridiaRoute.HOME,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(VeridiaRoute.HOME) {
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
            composable(VeridiaRoute.WEATHER) {
                DetailedWeatherScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onSelectStratum = viewModel::selectStratum,
                    onSelectWeatherMapLayer = viewModel::selectWeatherMapLayer,
                    onRefreshNow = viewModel::refreshLiveTelemetryNow,
                    onOpenRadar = { navController.navigate(VeridiaRoute.RADAR) },
                )
            }
            composable(VeridiaRoute.AIR) {
                AirQualityMapScreen(
                    state = state,
                    gyroTilt = gyroTilt,
                    onSelectAqiLayer = viewModel::selectAqiLayer,
                    onSelectAqiSector = viewModel::selectAqiSector,
                    onRefreshNow = viewModel::refreshLiveTelemetryNow,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(VeridiaRoute.ALERTS) {
                DisasterMonitorScreen(
                    state = state,
                    gyroTilt = gyroTilt,
                    onRefreshNow = viewModel::refreshLiveTelemetryNow,
                    onBack = { navController.popBackStack() },
                    onOpenSosModal = viewModel::openSosModal,
                    onCloseSosModal = viewModel::closeSosModal,
                )
            }
            composable(VeridiaRoute.RADAR) {
                WeatherRadarScreen(
                    state = state,
                    gyroTilt = gyroTilt,
                    onBack = { navController.popBackStack() },
                    onSelectRadarLayer = viewModel::selectRadarLayer,
                    onPlaybackOffsetChange = viewModel::setRadarPlaybackOffset,
                    onRefreshNow = viewModel::refreshLiveTelemetryNow,
                )
            }
            composable(VeridiaRoute.SETTINGS) {
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
                    onRefreshCache = viewModel::refreshOfflineCache,
                )
            }
        }

        VeridiaBottomDock(
            currentRoute = currentRoute,
            onNavigate = ::navigateTopLevel,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

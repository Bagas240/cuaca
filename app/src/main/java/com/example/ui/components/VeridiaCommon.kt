package com.example.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.CrisisAlert
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PressureUnit
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherConditionKind
import com.example.data.model.WindUnit
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelDangerText
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.PastelSubtle
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelTextMain
import com.example.ui.theme.PastelTextSub
import com.example.ui.theme.WarningAmber
import java.util.Locale
import kotlin.math.roundToInt

object VeridiaRoute {
    const val HOME = "home_dashboard"
    const val WEATHER = "detailed_weather"
    const val AIR = "air_quality"
    const val ALERTS = "disaster_monitor"
    const val RADAR = "weather_radar"
    const val SETTINGS = "settings"
}

@Composable
fun VeridiaCard(
    modifier: Modifier = Modifier,
    containerColor: Color = PastelSurface.copy(alpha = 0.92f),
    borderColor: Color = PastelBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color(0x14141E2D),
                spotColor = Color(0x14141E2D),
            )
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}

@Composable
fun VeridiaBackHeader(
    title: String,
    subtitle: String? = null,
    onBackClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("header_back_button")
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onBackClick),
                shape = RoundedCornerShape(12.dp),
                color = PastelSurface,
                border = BorderStroke(1.dp, PastelBorder),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = PastelTextMain,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = PastelTextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = PastelTextSub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

private data class DockTab(
    val route: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val activeTint: Color,
    val hasBadge: Boolean = false,
    val testTag: String,
)

@Composable
fun VeridiaBottomDock(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf(
        DockTab(
            route = VeridiaRoute.HOME,
            label = stringResource(R.string.nav_home),
            activeIcon = Icons.Rounded.Home,
            inactiveIcon = Icons.Outlined.Home,
            activeTint = PastelPrimary,
            testTag = "dock_tab_home",
        ),
        DockTab(
            route = VeridiaRoute.WEATHER,
            label = stringResource(R.string.nav_weather),
            activeIcon = Icons.Rounded.Cloud,
            inactiveIcon = Icons.Outlined.Cloud,
            activeTint = PastelPrimary,
            testTag = "dock_tab_weather",
        ),
        DockTab(
            route = VeridiaRoute.AIR,
            label = stringResource(R.string.nav_air),
            activeIcon = Icons.Rounded.Air,
            inactiveIcon = Icons.Outlined.Air,
            activeTint = PastelPrimary,
            testTag = "dock_tab_air",
        ),
        DockTab(
            route = VeridiaRoute.ALERTS,
            label = stringResource(R.string.nav_alerts),
            activeIcon = Icons.Rounded.CrisisAlert,
            inactiveIcon = Icons.Outlined.CrisisAlert,
            activeTint = DangerCrimson,
            hasBadge = true,
            testTag = "dock_tab_alerts",
        ),
        DockTab(
            route = VeridiaRoute.SETTINGS,
            label = stringResource(R.string.nav_settings),
            activeIcon = Icons.Rounded.Settings,
            inactiveIcon = Icons.Outlined.Settings,
            activeTint = PastelPrimary,
            testTag = "dock_tab_settings",
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = Color(0x1E14233C),
                    spotColor = Color(0x1E14233C),
                ),
            shape = RoundedCornerShape(50),
            color = PastelSurface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, PastelBorder),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    val isSelected = currentRoute == tab.route ||
                        (currentRoute == VeridiaRoute.RADAR && tab.route == VeridiaRoute.WEATHER)
                    val tint = if (isSelected) tab.activeTint else PastelTextSub
                    val pillBg = if (isSelected) tab.activeTint.copy(alpha = 0.12f) else Color.Transparent

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigate(tab.route) }
                            .padding(vertical = 4.dp)
                            .testTag(tab.testTag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(pillBg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier.size(18.dp),
                            )
                            if (tab.hasBadge && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-3).dp, y = 3.dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(PastelDangerText)
                                        .border(1.5.dp, PastelSurface, CircleShape),
                                )
                            }
                        }
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = tint,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

fun conditionIcon(kind: WeatherConditionKind): ImageVector = when (kind) {
    WeatherConditionKind.RAIN -> Icons.Rounded.WaterDrop
    WeatherConditionKind.CLOUDY -> Icons.Rounded.Cloud
    WeatherConditionKind.PARTLY_CLOUDY -> Icons.Rounded.Cloud
    WeatherConditionKind.SUNNY -> Icons.Rounded.WbSunny
    WeatherConditionKind.THUNDERSTORM -> Icons.Rounded.Thunderstorm
    WeatherConditionKind.SUNSET -> Icons.Rounded.WbTwilight
    WeatherConditionKind.FOG -> Icons.Rounded.Cloud
}

fun conditionTint(kind: WeatherConditionKind): Color = when (kind) {
    WeatherConditionKind.RAIN -> PastelPrimary
    WeatherConditionKind.CLOUDY -> PastelTextSub
    WeatherConditionKind.PARTLY_CLOUDY -> PastelPrimary
    WeatherConditionKind.SUNNY -> WarningAmber
    WeatherConditionKind.THUNDERSTORM -> WarningAmber
    WeatherConditionKind.SUNSET -> WarningAmber
    WeatherConditionKind.FOG -> PastelTextSub
}

fun formatTempValue(tempC: Int, unit: TemperatureUnit): Int = when (unit) {
    TemperatureUnit.CELSIUS -> tempC
    TemperatureUnit.FAHRENHEIT -> ((tempC * 9.0 / 5.0) + 32.0).roundToInt()
}

fun formatTempString(tempC: Int, unit: TemperatureUnit, includeUnit: Boolean = false): String {
    val value = formatTempValue(tempC, unit)
    return if (includeUnit) {
        "$value°${if (unit == TemperatureUnit.CELSIUS) "C" else "F"}"
    } else {
        "$value°"
    }
}

fun formatWindValue(kmh: Int, unit: WindUnit): Pair<String, String> = when (unit) {
    WindUnit.KMH -> "$kmh" to "km/h"
    WindUnit.MPH -> "${(kmh * 0.621371).roundToInt()}" to "mph"
    WindUnit.KNOTS -> "${(kmh * 0.539957).roundToInt()}" to "kn"
}

fun formatPressureValue(hpa: Int, unit: PressureUnit): Pair<String, String> = when (unit) {
    PressureUnit.HPA -> String.format(Locale.US, "%,d", hpa) to "hPa"
    PressureUnit.INHG -> String.format(Locale.US, "%.2f", hpa * 0.02953) to "inHg"
}

@Composable
fun rememberGyroParallax(enabled: Boolean): State<Offset> {
    val context = LocalContext.current
    val tiltState = remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(enabled) {
        if (!enabled) {
            tiltState.value = Offset.Zero
            return@DisposableEffect onDispose {}
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorManager == null || accelerometer == null) {
            return@DisposableEffect onDispose {}
        }
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = (event.values.getOrNull(0) ?: 0f).coerceIn(-4f, 4f) / 4f
                val y = ((event.values.getOrNull(1) ?: 0f) - 4.5f).coerceIn(-4f, 4f) / 4f
                tiltState.value = Offset(x, y)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
    return tiltState
}

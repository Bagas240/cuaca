package com.example.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.alerts.DisasterAlertEvent
import com.example.data.model.PressureUnit
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherConditionKind
import com.example.data.model.WindUnit
import com.example.ui.i18n.VeridiaLocalizer
import com.example.ui.i18n.veridiaStrings
import com.example.ui.navigation.LocalNavStateController
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.veridiaColors
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object VeridiaRoute {
    const val HOME = "home_dashboard"
    const val WEATHER = "detailed_weather"
    const val AIR = "air_quality"
    const val ALERTS = "disaster_monitor"
    const val RADAR = "weather_radar"
    const val SETTINGS = "settings"

    val topLevelOrder = listOf(HOME, WEATHER, AIR, ALERTS, SETTINGS)

    fun indexOf(route: String): Int {
        val mapped = if (route == RADAR) WEATHER else route
        return topLevelOrder.indexOf(mapped).coerceAtLeast(0)
    }
}

@Composable
fun VeridiaCard(
    modifier: Modifier = Modifier,
    containerColor: Color = veridiaColors.surface.copy(alpha = 0.94f),
    borderColor: Color = veridiaColors.border,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "card_press_scale",
    )

    val isDark = veridiaColors.isDark
    val shadowTint = if (isDark) Color(0x40000000) else Color(0x14141E2D)

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = shadowTint,
                spotColor = shadowTint,
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = androidx.compose.material3.ripple(),
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            ),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                )
                .padding(16.dp),
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
                color = veridiaColors.surface,
                border = BorderStroke(1.dp, veridiaColors.border),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val strings = veridiaStrings
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = strings.cdBack,
                        tint = veridiaColors.textMain,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = veridiaColors.textMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = veridiaColors.textSub,
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
    val colors = veridiaColors
    val strings = veridiaStrings
    val tabs = listOf(
        DockTab(
            route = VeridiaRoute.HOME,
            label = strings.navHome,
            activeTint = colors.primary,
            testTag = "dock_tab_home",
        ),
        DockTab(
            route = VeridiaRoute.WEATHER,
            label = strings.navWeather,
            activeTint = colors.primary,
            testTag = "dock_tab_weather",
        ),
        DockTab(
            route = VeridiaRoute.AIR,
            label = strings.navAir,
            activeTint = colors.primary,
            testTag = "dock_tab_air",
        ),
        DockTab(
            route = VeridiaRoute.ALERTS,
            label = strings.navAlerts,
            activeTint = DangerCrimson,
            hasBadge = true,
            testTag = "dock_tab_alerts",
        ),
        DockTab(
            route = VeridiaRoute.SETTINGS,
            label = strings.navSettings,
            activeTint = colors.primary,
            testTag = "dock_tab_settings",
        ),
    )

    val selectedIdx = tabs.indexOfFirst { tab ->
        currentRoute == tab.route ||
            (currentRoute == VeridiaRoute.RADAR && tab.route == VeridiaRoute.WEATHER)
    }.coerceAtLeast(0)

    var previousIdx by remember { mutableIntStateOf(selectedIdx) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    // iOS-style bouncy spring physics feedback using updateTransition + Animatable impulse
    val dockTransition = updateTransition(
        targetState = selectedIdx,
        label = "dock_spring_transition",
    )
    val capsuleBounce = remember { Animatable(1f) }
    val activeIconKick = remember { Animatable(1f) }

    LaunchedEffect(selectedIdx) {
        if (selectedIdx != previousIdx) {
            val isHomeWeatherToggle = (previousIdx == 0 && selectedIdx == 1) ||
                (previousIdx == 1 && selectedIdx == 0)
            previousIdx = selectedIdx
            capsuleBounce.snapTo(if (isHomeWeatherToggle) 0.86f else 0.92f)
            activeIconKick.snapTo(if (isHomeWeatherToggle) 0.78f else 0.88f)
            capsuleBounce.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.45f,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
    }

    LaunchedEffect(selectedIdx) {
        if (activeIconKick.value < 0.99f) {
            activeIconKick.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 372.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = Color(0x2414233C),
                    spotColor = Color(0x2414233C),
                )
                .pointerInput(selectedIdx) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragAccumulator = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            dragAccumulator += dragAmount
                            val stepThreshold = size.width / (tabs.size * 1.15f)
                            if (dragAccumulator > stepThreshold && selectedIdx < tabs.lastIndex) {
                                dragAccumulator = 0f
                                onNavigate(tabs[selectedIdx + 1].route)
                            } else if (dragAccumulator < -stepThreshold && selectedIdx > 0) {
                                dragAccumulator = 0f
                                onNavigate(tabs[selectedIdx - 1].route)
                            }
                        },
                    )
                },
            shape = RoundedCornerShape(50),
            color = colors.surface.copy(alpha = 0.96f),
            border = BorderStroke(1.dp, colors.border),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 5.dp),
            ) {
                val tabWidth = maxWidth / tabs.size
                val indicatorOffset by dockTransition.animateDp(
                    transitionSpec = {
                        spring(
                            dampingRatio = 0.56f,
                            stiffness = 340f,
                        )
                    },
                    label = "dock_pill_offset",
                ) { idx -> tabWidth * idx }

                val indicatorColor by dockTransition.animateColor(
                    transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
                    label = "dock_pill_color",
                ) { idx -> tabs[idx].activeTint.copy(alpha = 0.14f) }

                // Sliding iOS-style bouncy capsule behind active tab
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .height(48.dp)
                        .graphicsLayer {
                            // Stretch horizontally while compressing vertically during spring impulse
                            val bounceDelta = 1f - capsuleBounce.value
                            scaleX = 1f + (bounceDelta * 1.15f)
                            scaleY = capsuleBounce.value
                        }
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(indicatorColor),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tabs.forEachIndexed { idx, tab ->
                        val isSelected = idx == selectedIdx
                        val itemTransition = updateTransition(
                            targetState = isSelected,
                            label = "dock_item_transition_$idx",
                        )
                        val tint by itemTransition.animateColor(
                            transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
                            label = "tab_tint_$idx",
                        ) { selected -> if (selected) tab.activeTint else colors.textSub }

                        val iconScale by itemTransition.animateFloat(
                            transitionSpec = {
                                spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                )
                            },
                            label = "tab_scale_$idx",
                        ) { selected -> if (selected) 1.16f else 1.0f }

                        val iconLiftY by itemTransition.animateDp(
                            transitionSpec = {
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                )
                            },
                            label = "tab_lift_$idx",
                        ) { selected -> if (selected) (-1.5).dp else 0.dp }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { onNavigate(tab.route) }
                                .padding(vertical = 4.dp)
                                .testTag(tab.testTag),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .offset(y = iconLiftY)
                                    .graphicsLayer {
                                        val effectiveScale = if (isSelected) iconScale * activeIconKick.value else iconScale
                                        scaleX = effectiveScale
                                        scaleY = effectiveScale
                                    }
                                    .semantics {
                                        stateDescription = if (isSelected) "animating" else "idle"
                                    }
                                    .testTag("dock_icon_${tab.route}"),
                                contentAlignment = Alignment.Center,
                            ) {
                                when (tab.route) {
                                    VeridiaRoute.HOME -> DockAnimatedHomeIcon(isSelected = isSelected, tint = tint)
                                    VeridiaRoute.WEATHER -> DockAnimatedWeatherIcon(isSelected = isSelected, tint = tint)
                                    VeridiaRoute.AIR -> DockAnimatedAirIcon(isSelected = isSelected, tint = tint)
                                    VeridiaRoute.ALERTS -> DockAnimatedAlertsRadarIcon(isSelected = isSelected, tint = tint)
                                    else -> DockAnimatedSettingsGearIcon(isSelected = isSelected, tint = tint)
                                }
                                if (tab.hasBadge && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 1.dp, y = (-1).dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(colors.dangerText)
                                            .border(1.5.dp, colors.surface, CircleShape),
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
}

private const val DOCK_LOW_POWER_FPS = 30 // Quantizes redraws to 30fps so 90/120Hz displays skip redundant GPU passes

/**
 * Battery-optimized animation driver for bottom navigation icons:
 * - Zero active animators when [isSelected] is false (unselected tabs stay completely static/idle).
 * - Automatically unmounts animation when lifecycle leaves STARTED (background/screen off).
 * - Checks [PowerManager.isPowerSaveMode] and plays only a single selection cycle in Battery Saver.
 * - Quantizes active phase updates to ~30 fps via [derivedStateOf] to reduce GPU invalidations on 90/120Hz panels.
 */
@Composable
private fun rememberBatteryOptimizedDockPhase(
    isSelected: Boolean,
    cycleDurationMs: Int,
    pingPong: Boolean = false,
): Float {
    if (!isSelected) {
        return 0f
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLifecycleActive by remember(lifecycleOwner) {
        mutableStateOf(
            lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        )
    }
    var isPowerSaveMode by remember(context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        mutableStateOf(pm?.isPowerSaveMode == true)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, _ ->
            isLifecycleActive = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            isPowerSaveMode = pm?.isPowerSaveMode == true
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val navStateController = LocalNavStateController.current
    val isAppForeground = navStateController?.navState?.isAppForeground ?: true

    if (!isLifecycleActive || !isAppForeground) {
        return 0f
    }

    if (isPowerSaveMode) {
        // Under OS Battery Saver, run a single crisp cycle on tap then settle at 0f
        val singleBurst = remember { Animatable(0f) }
        LaunchedEffect(isSelected, cycleDurationMs) {
            singleBurst.snapTo(0f)
            singleBurst.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = cycleDurationMs, easing = LinearEasing),
            )
            singleBurst.snapTo(0f)
        }
        val raw = singleBurst.value
        return if (pingPong) (0.5f - 0.5f * cos(raw * 2.0 * PI)).toFloat() else raw
    }

    val transition = rememberInfiniteTransition(label = "dock_selected_only_anim")
    val rawPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDurationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "dock_selected_phase",
    )

    val frameSteps = ((cycleDurationMs / 1000f) * DOCK_LOW_POWER_FPS).roundToInt().coerceAtLeast(16)
    val quantizedPhase by remember(frameSteps, pingPong) {
        derivedStateOf {
            val snapped = (rawPhase * frameSteps).roundToInt() / frameSteps.toFloat()
            if (pingPong) {
                (0.5f - 0.5f * cos(snapped * 2.0 * PI)).toFloat()
            } else {
                snapped
            }
        }
    }
    return quantizedPhase
}

@Composable
private fun DockAnimatedHomeIcon(isSelected: Boolean, tint: Color) {
    val wave = rememberBatteryOptimizedDockPhase(
        isSelected = isSelected,
        cycleDurationMs = 1500,
        pingPong = true,
    )
    val pulse = if (isSelected) 1f + (0.08f * wave) else 1f

    Icon(
        imageVector = if (isSelected) Icons.Rounded.Home else Icons.Outlined.Home,
        contentDescription = veridiaStrings.navHome,
        tint = tint,
        modifier = Modifier
            .size(19.dp)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
    )
}

@Composable
private fun DockAnimatedWeatherIcon(isSelected: Boolean, tint: Color) {
    val drift = rememberBatteryOptimizedDockPhase(
        isSelected = isSelected,
        cycleDurationMs = 2400,
        pingPong = false,
    )
    val bobWave = if (isSelected) sin(drift * 2.0 * PI).toFloat() else 0f

    Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        if (isSelected) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val speedAlpha = 0.42f
                val c1x = ((drift * w * 1.4f) % (w * 1.3f)) - w * 0.15f
                drawCircle(color = tint.copy(alpha = speedAlpha), radius = 3.2.dp.toPx(), center = Offset(c1x, h * 0.28f))
                drawCircle(color = tint.copy(alpha = speedAlpha), radius = 2.4.dp.toPx(), center = Offset(c1x + 3.dp.toPx(), h * 0.32f))
                val c2x = (((drift + 0.52f) % 1f) * w * 1.4f) - w * 0.18f
                drawCircle(color = tint.copy(alpha = speedAlpha * 0.85f), radius = 2.6.dp.toPx(), center = Offset(c2x, h * 0.72f))
            }
        }
        Icon(
            imageVector = Icons.Rounded.Cloud,
            contentDescription = veridiaStrings.navWeather,
            tint = tint,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer {
                    translationX = bobWave * 2.1f
                    translationY = -bobWave * 0.8f
                },
        )
    }
}

@Composable
private fun DockAnimatedAirIcon(isSelected: Boolean, tint: Color) {
    val flow = rememberBatteryOptimizedDockPhase(
        isSelected = isSelected,
        cycleDurationMs = 1350,
        pingPong = false,
    )
    val wavePath = remember { Path() }

    Canvas(modifier = Modifier.size(21.dp)) {
        val w = size.width
        val h = size.height
        val stroke = (if (isSelected) 1.9f else 1.5f).dp.toPx()

        for (i in 0..2) {
            val y = h * (0.25f + i * 0.25f)
            val phase = if (isSelected) (flow + i * 0.24f) % 1f else 0.5f
            val shift = if (isSelected) (phase - 0.5f) * w * 0.34f else 0f
            val alpha = if (isSelected) {
                (0.45f + 0.55f * sin(phase * PI).toFloat()).coerceIn(0.35f, 1f)
            } else {
                0.78f
            }
            wavePath.rewind()
            wavePath.moveTo(w * 0.10f + shift * 0.35f, y)
            wavePath.cubicTo(
                w * 0.38f + shift * 0.35f, y - h * 0.11f,
                w * 0.62f + shift * 0.35f, y + h * 0.11f,
                w * 0.88f + shift * 0.35f, y - (if (i == 0) h * 0.08f else 0f),
            )
            drawPath(
                path = wavePath,
                color = tint.copy(alpha = alpha),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
private fun DockAnimatedAlertsRadarIcon(isSelected: Boolean, tint: Color) {
    val sweep = rememberBatteryOptimizedDockPhase(
        isSelected = isSelected,
        cycleDurationMs = 1250,
        pingPong = false,
    )

    Canvas(modifier = Modifier.size(22.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension * 0.48f

        if (isSelected) {
            val ring1 = (0.15f + sweep * 0.85f).coerceIn(0.15f, 1f)
            val ring2 = ((sweep + 0.5f) % 1f).coerceIn(0.15f, 1f)
            drawCircle(
                color = tint.copy(alpha = (1f - ring1) * 0.90f),
                radius = maxR * ring1,
                center = center,
                style = Stroke(width = 1.6.dp.toPx()),
            )
            drawCircle(
                color = tint.copy(alpha = (1f - ring2) * 0.75f),
                radius = maxR * ring2,
                center = center,
                style = Stroke(width = 1.3.dp.toPx()),
            )
        } else {
            // Static radar target outline when unselected (zero frame overhead)
            drawCircle(
                color = tint.copy(alpha = 0.45f),
                radius = maxR * 0.80f,
                center = center,
                style = Stroke(width = 1.3.dp.toPx()),
            )
        }

        drawCircle(
            color = tint,
            radius = if (isSelected) 3.4.dp.toPx() else 2.8.dp.toPx(),
            center = center,
        )
    }
}

@Composable
private fun DockAnimatedSettingsGearIcon(isSelected: Boolean, tint: Color) {
    val spinPhase = rememberBatteryOptimizedDockPhase(
        isSelected = isSelected,
        cycleDurationMs = 3200,
        pingPong = false,
    )
    val kickRotation by animateFloatAsState(
        targetValue = if (isSelected) 120f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "gear_kick",
    )

    Icon(
        imageVector = if (isSelected) Icons.Rounded.Settings else Icons.Outlined.Settings,
        contentDescription = veridiaStrings.navSettings,
        tint = tint,
        modifier = Modifier
            .size(19.dp)
            .graphicsLayer {
                rotationZ = (if (isSelected) spinPhase * 360f else 0f) + kickRotation
            },
    )
}

@Composable
fun ActiveDisasterAlertOverlay(
    alert: DisasterAlertEvent?,
    isSirenPlaying: Boolean,
    onSilenceSiren: () -> Unit,
    onDismiss: () -> Unit,
    onOpenDisasterMonitor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = alert != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier,
    ) {
        if (alert != null) {
            val colors = veridiaColors
            val strings = veridiaStrings
            val badgeText = VeridiaLocalizer.disasterKindBadge(alert.kind, strings)
            val sirenProfileText = VeridiaLocalizer.disasterKindSirenProfile(alert.kind, strings)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clickable(onClick = onOpenDisasterMonitor)
                    .testTag("active_disaster_alert_overlay"),
                shape = RoundedCornerShape(20.dp),
                color = colors.dangerBg,
                border = BorderStroke(1.5.dp, DangerCrimson.copy(alpha = 0.65f)),
                shadowElevation = 10.dp,
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DangerCrimson),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$badgeText · ${alert.title}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = colors.dangerText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = alert.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMain,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.dismissAlert,
                                tint = colors.dangerText,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = alert.guidance,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.dangerText,
                    )

                    if (isSirenPlaying) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onSilenceSiren)
                                .testTag("silence_siren_button"),
                            shape = RoundedCornerShape(10.dp),
                            color = DangerCrimson,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${strings.silenceSirenPrefix} ($sirenProfileText)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                )
                            }
                        }
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

@Composable
fun conditionTint(kind: WeatherConditionKind): Color = when (kind) {
    WeatherConditionKind.RAIN -> veridiaColors.primary
    WeatherConditionKind.CLOUDY -> veridiaColors.textSub
    WeatherConditionKind.PARTLY_CLOUDY -> veridiaColors.primary
    WeatherConditionKind.SUNNY -> veridiaColors.warning
    WeatherConditionKind.THUNDERSTORM -> veridiaColors.warning
    WeatherConditionKind.SUNSET -> veridiaColors.warning
    WeatherConditionKind.FOG -> veridiaColors.textSub
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val tiltState = remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(enabled, lifecycleOwner) {
        if (!enabled) {
            tiltState.value = Offset.Zero
            return@DisposableEffect onDispose {}
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorManager == null || accelerometer == null) {
            return@DisposableEffect onDispose {}
        }
        var registered = false
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = (event.values.getOrNull(0) ?: 0f).coerceIn(-4f, 4f) / 4f
                val y = ((event.values.getOrNull(1) ?: 0f) - 4.5f).coerceIn(-4f, 4f) / 4f
                tiltState.value = Offset(x, y)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        fun updateSensorRegistration() {
            val active = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            if (active && !registered) {
                sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
                registered = true
            } else if (!active && registered) {
                sensorManager.unregisterListener(listener)
                registered = false
                tiltState.value = Offset.Zero
            }
        }
        val observer = LifecycleEventObserver { _, _ -> updateSensorRegistration() }
        lifecycleOwner.lifecycle.addObserver(observer)
        updateSensorRegistration()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (registered) {
                sensorManager.unregisterListener(listener)
            }
        }
    }
    return tiltState
}

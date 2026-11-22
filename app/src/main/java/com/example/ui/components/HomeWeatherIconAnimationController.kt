package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.navigation.rememberIsDestinationAnimating
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Coordinates Compose AnimatedVectorDrawable playback and continuous motion parameters
 * (gentle swaying wind indicators, pulsating air-quality glow, and floating weather icons)
 * for the Veridia home screen.
 */
@Stable
class HomeWeatherIconAnimationController(
    initialWindSpeedKmh: Int = 12,
    initialAqiScore: Int = 42,
) {
    var windSpeedKmh by mutableIntStateOf(initialWindSpeedKmh)
        internal set

    var aqiScore by mutableIntStateOf(initialAqiScore)
        internal set

    var isPlaying by mutableStateOf(true)
        private set

    var isNavigationVisible by mutableStateOf(true)
        private set

    val shouldAnimate: Boolean
        get() = isPlaying && isNavigationVisible

    var weatherAvdAtEnd by mutableStateOf(false)
        internal set

    var windAvdAtEnd by mutableStateOf(false)
        internal set

    var windSwayAngleDeg by mutableFloatStateOf(0f)
        internal set

    var windSwayOffsetPx by mutableFloatStateOf(0f)
        internal set

    var telemetryImpulseScale by mutableFloatStateOf(1f)
        internal set

    var telemetryImpulseCount by mutableIntStateOf(0)
        private set

    fun play() {
        isPlaying = true
    }

    fun pause() {
        isPlaying = false
    }

    fun togglePlayback() {
        isPlaying = !isPlaying
    }

    internal fun updateNavigationVisible(visible: Boolean) {
        if (isNavigationVisible != visible) {
            isNavigationVisible = visible
        }
    }

    fun triggerTelemetryImpulse() {
        telemetryImpulseCount += 1
        weatherAvdAtEnd = !weatherAvdAtEnd
        windAvdAtEnd = !windAvdAtEnd
    }
}

val LocalHomeWeatherIconController = compositionLocalOf<HomeWeatherIconAnimationController?> { null }

@Composable
fun rememberHomeWeatherIconAnimationController(
    windSpeedKmh: Int,
    aqiScore: Int,
): HomeWeatherIconAnimationController {
    val controller = remember {
        HomeWeatherIconAnimationController(
            initialWindSpeedKmh = windSpeedKmh,
            initialAqiScore = aqiScore,
        )
    }
    controller.windSpeedKmh = windSpeedKmh
    controller.aqiScore = aqiScore

    val windCycleMs = when {
        windSpeedKmh >= 40 -> 1150
        windSpeedKmh >= 20 -> 1500
        windSpeedKmh >= 6 -> 1850
        else -> 2300
    }
    val swayAmplitudeDeg = when {
        windSpeedKmh <= 1 -> 1.5f
        windSpeedKmh >= 40 -> 8.5f
        windSpeedKmh >= 20 -> 6.2f
        else -> 4.2f
    }
    val swayDriftPx = when {
        windSpeedKmh <= 1 -> 0.6f
        windSpeedKmh >= 40 -> 3.2f
        windSpeedKmh >= 20 -> 2.4f
        else -> 1.6f
    }

    val isNavVisible = rememberIsDestinationAnimating()
    controller.updateNavigationVisible(isNavVisible)

    // Battery-optimized quantized wave driver (30fps / 20fps in Battery Saver, auto-paused offscreen)
    val swayPhase by rememberQuantizedCyclePhase(
        enabled = controller.shouldAnimate,
        cycleDurationMs = windCycleMs * 2,
        pingPong = true,
        idleValue = 0.5f,
        label = "ctrl_wind_sway",
    )

    if (controller.shouldAnimate) {
        val swayWave = (swayPhase * 2f) - 1f
        controller.windSwayAngleDeg = swayWave * swayAmplitudeDeg
        controller.windSwayOffsetPx = swayWave * swayDriftPx
    } else {
        controller.windSwayAngleDeg = 0f
        controller.windSwayOffsetPx = 0f
    }

    // Consolidated coroutine cadence for Compose AnimatedVectorDrawables
    LaunchedEffect(controller.shouldAnimate, windCycleMs) {
        if (!controller.shouldAnimate) return@LaunchedEffect
        var elapsedMs = 0L
        var nextWeatherMs = 1800L
        var nextWindMs = windCycleMs.toLong().coerceAtLeast(1100L)
        while (isActive) {
            val nextWakeMs = minOf(nextWeatherMs, nextWindMs)
            val sleepMs = (nextWakeMs - elapsedMs).coerceAtLeast(150L)
            delay(sleepMs)
            elapsedMs += sleepMs
            if (elapsedMs >= nextWeatherMs) {
                controller.weatherAvdAtEnd = !controller.weatherAvdAtEnd
                nextWeatherMs += 1800L
            }
            if (elapsedMs >= nextWindMs) {
                controller.windAvdAtEnd = !controller.windAvdAtEnd
                nextWindMs += windCycleMs.toLong().coerceAtLeast(1100L)
            }
        }
    }

    // Spring-physics impulse whenever live telemetry refreshes or user triggers an impulse
    val impulseScale = remember { Animatable(1f) }
    LaunchedEffect(controller.telemetryImpulseCount) {
        if (controller.telemetryImpulseCount > 0) {
            impulseScale.snapTo(0.88f)
            impulseScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
    }
    controller.telemetryImpulseScale = impulseScale.value

    return controller
}

/**
 * Compose AnimatedVectorDrawable weather badge driven by [HomeWeatherIconAnimationController].
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun AnimatedHomeWeatherAvdIcon(
    controller: HomeWeatherIconAnimationController?,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = com.example.ui.theme.veridiaColors.primary,
) {
    val avd = AnimatedImageVector.animatedVectorResource(R.drawable.avd_weather_sun_cloud)
    val atEnd = controller?.weatherAvdAtEnd ?: false
    val impulse = controller?.telemetryImpulseScale ?: 1f

    Image(
        painter = rememberAnimatedVectorPainter(avd, atEnd),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = impulse
                scaleY = impulse
            }
            .testTag("avd_weather_icon"),
    )
}

/**
 * Compose AnimatedVectorDrawable wind icon with gentle swaying motion driven by
 * [HomeWeatherIconAnimationController].
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun AnimatedWindSwayAvdIcon(
    controller: HomeWeatherIconAnimationController?,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = com.example.ui.theme.veridiaColors.primary,
) {
    val activeController = controller ?: LocalHomeWeatherIconController.current
    val avd = AnimatedImageVector.animatedVectorResource(R.drawable.avd_wind_sway)
    val atEnd = activeController?.windAvdAtEnd ?: false
    val swayDeg = activeController?.windSwayAngleDeg ?: 0f
    val driftPx = activeController?.windSwayOffsetPx ?: 0f
    val impulse = activeController?.telemetryImpulseScale ?: 1f

    Image(
        painter = rememberAnimatedVectorPainter(avd, atEnd),
        contentDescription = null,
        colorFilter = ColorFilter.tint(tint),
        contentScale = ContentScale.Fit,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationZ = swayDeg
                translationX = driftPx
                scaleX = impulse
                scaleY = impulse
            }
            .testTag("avd_wind_sway_icon"),
    )
}

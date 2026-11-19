package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.navigation.rememberIsDestinationAnimating
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.PastelAccent
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.SecondaryGreenDark
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

    var aqiAvdAtEnd by mutableStateOf(false)
        internal set

    var windSwayAngleDeg by mutableFloatStateOf(0f)
        internal set

    var windSwayOffsetPx by mutableFloatStateOf(0f)
        internal set

    var aqiGlowPulse by mutableFloatStateOf(0.5f)
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
        aqiAvdAtEnd = !aqiAvdAtEnd
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
    val aqiCycleMs = when {
        aqiScore >= 101 -> 1050
        aqiScore >= 51 -> 1350
        else -> 1650
    }

    val isNavVisible = rememberIsDestinationAnimating()
    controller.updateNavigationVisible(isNavVisible)

    // Only mount infinite animation frames while playing AND navigation destination is visible
    if (controller.shouldAnimate) {
        val infiniteTransition = rememberInfiniteTransition(label = "home_weather_icon_ctrl")
        val swayWave by infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = windCycleMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "ctrl_wind_sway_wave",
        )
        val aqiGlowWave by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = aqiCycleMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "ctrl_aqi_glow_wave",
        )
        controller.windSwayAngleDeg = swayWave * swayAmplitudeDeg
        controller.windSwayOffsetPx = swayWave * swayDriftPx
        controller.aqiGlowPulse = aqiGlowWave
    } else {
        controller.windSwayAngleDeg = 0f
        controller.windSwayOffsetPx = 0f
    }

    // Drive Compose AnimatedVectorDrawable start <-> end transitions on a calibrated cadence
    LaunchedEffect(controller.shouldAnimate, windCycleMs, aqiCycleMs) {
        if (!controller.shouldAnimate) return@LaunchedEffect
        val weatherJob = launch {
            while (isActive) {
                delay(1800L)
                controller.weatherAvdAtEnd = !controller.weatherAvdAtEnd
            }
        }
        val windJob = launch {
            while (isActive) {
                delay(windCycleMs.toLong().coerceAtLeast(1100L))
                controller.windAvdAtEnd = !controller.windAvdAtEnd
            }
        }
        val aqiJob = launch {
            while (isActive) {
                delay(aqiCycleMs.toLong().coerceAtLeast(1000L))
                controller.aqiAvdAtEnd = !controller.aqiAvdAtEnd
            }
        }
        weatherJob.join()
        windJob.join()
        aqiJob.join()
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

fun aqiGlowColorFor(aqiScore: Int): Color = when {
    aqiScore >= 101 -> DangerCrimson
    aqiScore >= 51 -> WarningAmber
    else -> PastelAccent
}

/**
 * Applies a controller-driven pulsating radial glow behind an Air Quality reading container.
 */
fun Modifier.aqiPulsatingGlow(
    glowPulse: Float,
    glowColor: Color,
): Modifier = this.drawBehind {
    val clamped = glowPulse.coerceIn(0f, 1f)
    val radius = size.maxDimension * (0.68f + clamped * 0.24f)
    val alpha = 0.08f + clamped * 0.14f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = alpha),
                glowColor.copy(alpha = alpha * 0.35f),
                Color.Transparent,
            ),
            center = Offset(size.width * 0.78f, size.height * 0.32f),
            radius = radius,
        ),
        radius = radius,
        center = Offset(size.width * 0.78f, size.height * 0.32f),
    )
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
    tint: Color = PastelPrimary,
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
    tint: Color = PastelPrimary,
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

/**
 * Compose AnimatedVectorDrawable Air Quality orb combined with a pulsating radial glow halo
 * driven by [HomeWeatherIconAnimationController].
 */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun PulsatingAqiGlowAvdBadge(
    aqiScore: Int,
    controller: HomeWeatherIconAnimationController?,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    coreTint: Color = SecondaryGreenDark,
) {
    val activeController = controller ?: LocalHomeWeatherIconController.current
    val avd = AnimatedImageVector.animatedVectorResource(R.drawable.avd_aqi_pulse)
    val atEnd = activeController?.aqiAvdAtEnd ?: false
    val glowPulse = activeController?.aqiGlowPulse ?: 0.5f
    val impulse = activeController?.telemetryImpulseScale ?: 1f
    val glowColor = aqiGlowColorFor(aqiScore)

    Box(
        modifier = modifier
            .size(size)
            .testTag("avd_aqi_pulse_badge"),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val baseR = this.size.minDimension * 0.48f
            val haloR = baseR * (0.95f + glowPulse * 0.55f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.42f - glowPulse * 0.14f),
                        glowColor.copy(alpha = 0.18f),
                        Color.Transparent,
                    ),
                    center = center,
                    radius = haloR,
                ),
                radius = haloR,
                center = center,
            )
        }
        Image(
            painter = rememberAnimatedVectorPainter(avd, atEnd),
            contentDescription = null,
            colorFilter = ColorFilter.tint(coreTint),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size * 0.86f)
                .graphicsLayer {
                    val pulseScale = (0.94f + glowPulse * 0.14f) * impulse
                    scaleX = pulseScale
                    scaleY = pulseScale
                },
        )
    }
}

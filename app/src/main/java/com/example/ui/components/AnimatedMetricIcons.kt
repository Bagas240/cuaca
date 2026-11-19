package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.WeatherConditionKind
import com.example.ui.navigation.rememberIsDestinationAnimating
import com.example.ui.theme.PastelAccent
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.SecondaryGreenDark
import com.example.ui.theme.WarningAmber
import kotlin.math.PI
import kotlin.math.sin

/**
 * Animated thermometer icon with warm thermal shimmer pulse.
 */
@Composable
fun AnimatedThermometerIcon(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = WarningAmber,
) {
    val isAnimating = rememberIsDestinationAnimating()
    val pulse: Float
    val tilt: Float
    if (isAnimating) {
        val transition = rememberInfiniteTransition(label = "thermo_anim")
        val animatedPulse by transition.animateFloat(
            initialValue = 0.92f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "thermo_pulse",
        )
        val animatedTilt by transition.animateFloat(
            initialValue = -4f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "thermo_tilt",
        )
        pulse = animatedPulse
        tilt = animatedTilt
    } else {
        pulse = 1f
        tilt = 0f
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = tint.copy(alpha = 0.22f),
                radius = (this.size.minDimension * 0.48f) * pulse,
            )
        }
        Icon(
            imageVector = Icons.Rounded.Thermostat,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                    rotationZ = tilt
                },
        )
    }
}

/**
 * Animated humidity icon with a smooth dripping water droplet falling below the drop.
 */
@Composable
fun AnimatedHumidityDropletIcon(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = PastelPrimary,
) {
    val isAnimating = rememberIsDestinationAnimating()
    val dropProgress: Float
    if (isAnimating) {
        val transition = rememberInfiniteTransition(label = "humidity_drip")
        val animatedDrop by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "drop_progress",
        )
        dropProgress = animatedDrop
    } else {
        dropProgress = 0f
    }

    val mainScale = 1f - (sin(dropProgress * PI).toFloat() * 0.08f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.WaterDrop,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(size * 0.86f)
                .graphicsLayer {
                    scaleX = mainScale
                    scaleY = mainScale
                    translationY = -1.dp.toPx()
                },
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            if (dropProgress < 0.78f) {
                val p = (dropProgress / 0.78f)
                val y = h * (0.62f + p * 0.34f)
                val alpha = (1f - p * 0.6f).coerceIn(0f, 1f)
                drawCircle(
                    color = tint.copy(alpha = alpha),
                    radius = (1.6.dp.toPx()) * (1f - p * 0.3f),
                    center = Offset(w * 0.5f, y),
                )
            } else {
                val ripple = ((dropProgress - 0.78f) / 0.22f).coerceIn(0f, 1f)
                drawCircle(
                    color = tint.copy(alpha = (1f - ripple) * 0.55f),
                    radius = (2.0.dp.toPx()) + ripple * 3.5.dp.toPx(),
                    center = Offset(w * 0.5f, h * 0.92f),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
        }
    }
}

/**
 * Animated blowing air waves icon for Air Quality telemetry with a subtle pulsating glow halo.
 */
@Composable
fun AnimatedAirQualityWindIcon(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = SecondaryGreenDark,
) {
    val controller = LocalHomeWeatherIconController.current
    val isAnimating = rememberIsDestinationAnimating() && (controller?.isPlaying ?: true)
    val flow: Float
    if (isAnimating) {
        val transition = rememberInfiniteTransition(label = "aqi_air_flow")
        val animatedFlow by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1900, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "aqi_flow_phase",
        )
        flow = animatedFlow
    } else {
        flow = 0.5f
    }
    val glowPulse = controller?.aqiGlowPulse ?: ((sin(flow * 2f * PI).toFloat() + 1f) * 0.5f)
    val impulse = controller?.telemetryImpulseScale ?: 1f

    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = impulse
                scaleY = impulse
            },
    ) {
        val w = this.size.width
        val h = this.size.height
        val stroke = 1.7.dp.toPx()
        val glowRadius = this.size.minDimension * (0.45f + glowPulse * 0.25f)

        drawCircle(
            color = tint.copy(alpha = 0.08f + glowPulse * 0.14f),
            radius = glowRadius,
            center = Offset(w * 0.5f, h * 0.5f),
        )

        for (lineIdx in 0..2) {
            val yBase = h * (0.26f + lineIdx * 0.24f)
            val phase = (flow + lineIdx * 0.26f) % 1f
            val shift = (phase - 0.5f) * w * 0.35f
            val alpha = (sin(phase * PI).toFloat() * 0.85f + 0.15f).coerceIn(0.2f, 1f)

            val path = Path().apply {
                moveTo(w * 0.08f + shift * 0.35f, yBase)
                cubicTo(
                    w * 0.36f + shift * 0.35f, yBase - h * 0.10f,
                    w * 0.62f + shift * 0.35f, yBase + h * 0.10f,
                    w * 0.86f + shift * 0.35f, yBase - (if (lineIdx == 0) h * 0.08f else 0f),
                )
            }
            drawPath(
                path = path,
                color = tint.copy(alpha = alpha),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            // Curling gust tail on top and bottom streams
            if (lineIdx != 1) {
                val curlY = if (lineIdx == 0) yBase - h * 0.12f else yBase + h * 0.11f
                drawCircle(
                    color = tint.copy(alpha = alpha * 0.8f),
                    radius = stroke * 0.7f,
                    center = Offset((w * 0.88f + shift * 0.35f).coerceIn(0f, w), curlY),
                )
            }
        }
    }
}

/**
 * Gentle animated condition icon for hourly forecast slots and weather headers.
 */
@Composable
fun AnimatedWeatherConditionIcon(
    kind: WeatherConditionKind,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    tint: Color = conditionTint(kind),
) {
    val controller = LocalHomeWeatherIconController.current
    val isAnimating = rememberIsDestinationAnimating() && (controller?.isPlaying ?: true)
    val phase: Float
    if (isAnimating) {
        val transition = rememberInfiniteTransition(label = "hourly_weather_icon")
        val animatedPhase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "weather_icon_phase",
        )
        phase = animatedPhase
    } else {
        phase = 0f
    }

    val bobY = when (kind) {
        WeatherConditionKind.CLOUDY,
        WeatherConditionKind.PARTLY_CLOUDY,
        WeatherConditionKind.FOG -> sin(phase) * 1.4f
        WeatherConditionKind.RAIN,
        WeatherConditionKind.THUNDERSTORM -> (sin(phase * 2f) * 1.3f)
        else -> 0f
    }
    val impulse = controller?.telemetryImpulseScale ?: 1f
    val pulseScale = (when (kind) {
        WeatherConditionKind.SUNNY,
        WeatherConditionKind.SUNSET -> 1f + 0.07f * sin(phase)
        WeatherConditionKind.THUNDERSTORM -> 1f + 0.06f * sin(phase * 2f)
        else -> 1f
    }) * impulse
    val rot = if (kind == WeatherConditionKind.SUNNY) sin(phase) * 8f else 0f

    Icon(
        imageVector = conditionIcon(kind),
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                translationY = bobY
                scaleX = pulseScale
                scaleY = pulseScale
                rotationZ = rot
            },
    )
}

/**
 * Breathing green/blue dot indicating active real-time telemetry streaming.
 */
@Composable
fun LiveTelemetryPulseDot(
    color: Color = PastelAccent,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier,
) {
    val isAnimating = rememberIsDestinationAnimating()
    val scale: Float
    val haloAlpha: Float
    if (isAnimating) {
        val transition = rememberInfiniteTransition(label = "live_dot")
        val animatedScale by transition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.65f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "live_dot_scale",
        )
        val animatedHaloAlpha by transition.animateFloat(
            initialValue = 0.55f,
            targetValue = 0.10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "live_dot_alpha",
        )
        scale = animatedScale
        haloAlpha = animatedHaloAlpha
    } else {
        scale = 1f
        haloAlpha = 0.35f
    }

    Box(
        modifier = modifier.size(size * 2f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(color.copy(alpha = haloAlpha)),
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color),
        )
    }
}

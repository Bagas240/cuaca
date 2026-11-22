package com.example.ui.components

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
import androidx.compose.runtime.remember
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
import com.example.ui.theme.veridiaColors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Animated thermometer icon with warm thermal shimmer pulse (30fps / 20fps quantized).
 */
@Composable
fun AnimatedThermometerIcon(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    tint: Color = veridiaColors.warning,
) {
    val wave by rememberQuantizedCyclePhase(
        enabled = true,
        cycleDurationMs = 3200,
        pingPong = true,
        idleValue = 0.4f,
        label = "thermo_anim",
    )
    val pulse = 0.92f + wave * 0.20f
    val tilt = -4f + wave * 8f

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
    tint: Color = veridiaColors.primary,
) {
    val dropProgress by rememberQuantizedCyclePhase(
        enabled = true,
        cycleDurationMs = 1800,
        pingPong = false,
        idleValue = 0f,
        label = "humidity_drip",
    )
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
 * Animated blowing air waves icon for Air Quality telemetry.
 */
@Composable
fun AnimatedAirQualityWindIcon(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = veridiaColors.greenText,
) {
    val controller = LocalHomeWeatherIconController.current
    val flow by rememberQuantizedCyclePhase(
        enabled = controller?.isPlaying ?: true,
        cycleDurationMs = 1900,
        pingPong = false,
        idleValue = 0.5f,
        label = "aqi_air_flow",
    )
    val impulse = controller?.telemetryImpulseScale ?: 1f
    val windPath = remember { Path() }

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

        for (lineIdx in 0..2) {
            val yBase = h * (0.26f + lineIdx * 0.24f)
            val phase = (flow + lineIdx * 0.26f) % 1f
            val shift = (phase - 0.5f) * w * 0.35f
            val alpha = (sin(phase * PI).toFloat() * 0.85f + 0.15f).coerceIn(0.2f, 1f)

            windPath.rewind()
            windPath.moveTo(w * 0.08f + shift * 0.35f, yBase)
            windPath.cubicTo(
                w * 0.36f + shift * 0.35f, yBase - h * 0.10f,
                w * 0.62f + shift * 0.35f, yBase + h * 0.10f,
                w * 0.86f + shift * 0.35f, yBase - (if (lineIdx == 0) h * 0.08f else 0f),
            )
            drawPath(
                path = windPath,
                color = tint.copy(alpha = alpha),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
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
    val normPhase by rememberQuantizedCyclePhase(
        enabled = controller?.isPlaying ?: true,
        cycleDurationMs = 3000,
        pingPong = false,
        idleValue = 0f,
        label = "hourly_weather_icon",
    )
    val phase = normPhase * (2f * PI).toFloat()

    val bobY = when (kind) {
        WeatherConditionKind.CLOUDY,
        WeatherConditionKind.PARTLY_CLOUDY,
        WeatherConditionKind.FOG -> sin(phase) * 1.4f
        WeatherConditionKind.RAIN,
        WeatherConditionKind.THUNDERSTORM -> sin(phase * 2f) * 1.3f
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
    color: Color = veridiaColors.accent,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier,
) {
    val wave by rememberQuantizedCyclePhase(
        enabled = true,
        cycleDurationMs = 2400,
        pingPong = true,
        idleValue = 0.25f,
        label = "live_dot",
    )
    val scale = 0.85f + wave * 0.80f
    val haloAlpha = 0.55f - wave * 0.45f

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

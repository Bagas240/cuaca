package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.WindUnit
import com.example.ui.navigation.rememberIsDestinationAnimating
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelTextMain
import com.example.ui.theme.PastelTextSub
import com.example.ui.theme.WarningAmber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class WindIndicatorStyle {
    COMPACT_PILL,
    DETAILED_DIAL,
}

fun windConditionSummary(speedKmh: Int): String = when {
    speedKmh < 2 -> "Calm"
    speedKmh < 12 -> "Light Breeze"
    speedKmh < 25 -> "Gentle Breeze"
    speedKmh < 39 -> "Moderate Breeze"
    speedKmh < 55 -> "Fresh Breeze"
    else -> "Strong Wind"
}

fun normalizeWindBearing(bearingDeg: Int): Float {
    val mod = bearingDeg % 360
    return if (mod < 0) (mod + 360).toFloat() else mod.toFloat()
}

fun shortestAngleDelta(fromDeg: Float, toDeg: Float): Float {
    return ((toDeg - fromDeg + 540f) % 360f) - 180f
}

fun windIntensityColor(speedKmh: Int): Color = when {
    speedKmh >= 50 -> DangerCrimson
    speedKmh >= 30 -> WarningAmber
    else -> PastelPrimary
}

fun formatWindConditionsLabel(
    speedKmh: Int,
    unit: WindUnit,
    directionLabel: String,
    bearingDeg: Int,
): String {
    val (value, unitLabel) = formatWindValue(speedKmh, unit)
    val summary = windConditionSummary(speedKmh)
    val cleanBearing = normalizeWindBearing(bearingDeg).toInt()
    return "$value $unitLabel · $directionLabel (${cleanBearing}°) · $summary"
}

/**
 * Animated directional compass arrow whose bearing smoothly rotates along the shortest arc
 * and pulses/oscillates along the wind vector proportionally to current wind speed.
 */
@Composable
fun AnimatedWindArrowIcon(
    windBearingDeg: Int,
    windSpeedKmh: Int,
    modifier: Modifier = Modifier,
    dialSize: Dp = 36.dp,
    arrowColor: Color = windIntensityColor(windSpeedKmh),
    showCompassRing: Boolean = true,
) {
    val targetBearing = normalizeWindBearing(windBearingDeg)
    var continuousBearing by remember { mutableFloatStateOf(targetBearing) }
    val delta = shortestAngleDelta(continuousBearing, targetBearing)
    val desiredBearing = continuousBearing + delta

    SideEffect {
        continuousBearing = desiredBearing
    }

    val animatedBearing by animateFloatAsState(
        targetValue = desiredBearing,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "wind_bearing_rotation",
    )

    val pulseDurationMs = when {
        windSpeedKmh >= 40 -> 850
        windSpeedKmh >= 20 -> 1250
        windSpeedKmh >= 6 -> 1750
        else -> 2400
    }
    val gustAmplitudePx = when {
        windSpeedKmh <= 1 -> 0f
        windSpeedKmh >= 40 -> 3.2f
        windSpeedKmh >= 20 -> 2.4f
        else -> 1.5f
    }

    val homeController = LocalHomeWeatherIconController.current
    val isAnimating = rememberIsDestinationAnimating() && (homeController?.isPlaying ?: true)
    val breezeWave: Float
    val haloAlpha: Float
    if (isAnimating) {
        val infiniteTransition = rememberInfiniteTransition(label = "wind_breeze_transition")
        val animatedBreeze by infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = pulseDurationMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "wind_breeze_wave",
        )
        val animatedHalo by infiniteTransition.animateFloat(
            initialValue = 0.14f,
            targetValue = 0.34f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = pulseDurationMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "wind_halo_alpha",
        )
        breezeWave = animatedBreeze
        haloAlpha = animatedHalo
    } else {
        breezeWave = 0f
        haloAlpha = 0.22f
    }

    val radians = (animatedBearing - 90f) * (PI / 180.0)
    val controllerSwayDeg = homeController?.windSwayAngleDeg ?: 0f
    val controllerDriftPx = homeController?.windSwayOffsetPx ?: 0f
    val impulseScale = homeController?.telemetryImpulseScale ?: 1f

    val shiftX = (cos(radians) * breezeWave * gustAmplitudePx).toFloat() + (controllerDriftPx * 0.45f)
    val shiftY = (sin(radians) * breezeWave * gustAmplitudePx).toFloat()
    val swayTilt = (breezeWave * (if (windSpeedKmh <= 1) 0f else 5.2f)) + (controllerSwayDeg * 0.55f)

    Box(
        modifier = modifier
            .size(dialSize)
            .graphicsLayer {
                scaleX = impulseScale
                scaleY = impulseScale
            },
        contentAlignment = Alignment.Center,
    ) {
        if (showCompassRing) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawCircle(
                    color = PastelSky.copy(alpha = 0.72f),
                    radius = radius - 1.dp.toPx(),
                    center = center,
                )
                drawCircle(
                    color = arrowColor.copy(alpha = haloAlpha),
                    radius = radius - 1.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx()),
                )

                // Cardinal tick marks (North highlighted)
                for (deg in listOf(0, 90, 180, 270)) {
                    val rad = (deg - 90) * (PI / 180.0)
                    val innerR = radius - (if (deg == 0) 5.dp.toPx() else 3.5.dp.toPx())
                    val outerR = radius - 1.5.dp.toPx()
                    val start = Offset(
                        x = center.x + (cos(rad) * innerR).toFloat(),
                        y = center.y + (sin(rad) * innerR).toFloat(),
                    )
                    val end = Offset(
                        x = center.x + (cos(rad) * outerR).toFloat(),
                        y = center.y + (sin(rad) * outerR).toFloat(),
                    )
                    drawLine(
                        color = if (deg == 0) arrowColor else PastelTextSub.copy(alpha = 0.35f),
                        start = start,
                        end = end,
                        strokeWidth = if (deg == 0) 2.dp.toPx() else 1.2.dp.toPx(),
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.Rounded.Navigation,
            contentDescription = null,
            tint = arrowColor,
            modifier = Modifier
                .size(dialSize * 0.52f)
                .graphicsLayer {
                    rotationZ = animatedBearing + swayTilt
                    translationX = shiftX
                    translationY = shiftY
                },
        )
    }
}

/**
 * Reusable wind speed and direction indicator component combining an animated directional
 * arrow icon with a formatted text label describing current wind conditions.
 */
@Composable
fun WindSpeedDirectionIndicator(
    windSpeedKmh: Int,
    windBearingDeg: Int,
    windDirectionLabel: String,
    windUnit: WindUnit,
    modifier: Modifier = Modifier,
    style: WindIndicatorStyle = WindIndicatorStyle.COMPACT_PILL,
    useLargeReadout: Boolean = false,
) {
    val (speedValue, unitLabel) = formatWindValue(windSpeedKmh, windUnit)
    val conditionText = windConditionSummary(windSpeedKmh)
    val cleanBearing = normalizeWindBearing(windBearingDeg).toInt()
    val accentColor = windIntensityColor(windSpeedKmh)
    val accessibilityLabel = stringResource(
        R.string.cd_wind_indicator,
        speedValue,
        unitLabel,
        windDirectionLabel,
        cleanBearing,
        conditionText,
    )

    when (style) {
        WindIndicatorStyle.COMPACT_PILL -> {
            Surface(
                modifier = modifier
                    .semantics(mergeDescendants = true) {
                        contentDescription = accessibilityLabel
                    }
                    .testTag("wind_indicator_pill"),
                shape = RoundedCornerShape(12.dp),
                color = PastelSurface,
                border = BorderStroke(1.dp, PastelBorder),
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedWindArrowIcon(
                        windBearingDeg = windBearingDeg,
                        windSpeedKmh = windSpeedKmh,
                        dialSize = 24.dp,
                        arrowColor = accentColor,
                        showCompassRing = true,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$speedValue $unitLabel · $windDirectionLabel · $conditionText",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = PastelTextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        WindIndicatorStyle.DETAILED_DIAL -> {
            Row(
                modifier = modifier
                    .semantics(mergeDescendants = true) {
                        contentDescription = accessibilityLabel
                    }
                    .testTag("wind_indicator_dial"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AnimatedWindArrowIcon(
                    windBearingDeg = windBearingDeg,
                    windSpeedKmh = windSpeedKmh,
                    dialSize = if (useLargeReadout) 46.dp else 40.dp,
                    arrowColor = accentColor,
                    showCompassRing = true,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = speedValue,
                            style = if (useLargeReadout) {
                                MaterialTheme.typography.displayMedium
                            } else {
                                MaterialTheme.typography.displaySmall
                            },
                            color = PastelTextMain,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unitLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = PastelTextSub,
                            modifier = Modifier.padding(bottom = if (useLargeReadout) 6.dp else 4.dp),
                        )
                    }
                    Text(
                        text = "$windDirectionLabel (${cleanBearing}°) · $conditionText",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = PastelTextSub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

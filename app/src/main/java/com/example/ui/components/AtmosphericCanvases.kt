package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.VeridiaLocalizer
import com.example.ui.i18n.veridiaStrings
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.veridiaColors

const val HOME_SKY_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida/ALJJyrBflIwXeKtgNlLCoxiar9VKrIzwk4NjTyfrY-IK3ea1NuAKt1NKY0JYm8MctLP9KyOJOmbOvg7CL3gi6EhZyGriELfsnEN8QsSoO1b01NkGDaIQwOSsHvfH9Zwza5I75RstpC9CP1J-jxfTyvjndlcTmewIKFBdsp4UoV_zZecGIsjZlqHOQnKrP3dPJe37teFyuH23IoRkqjX5a9H9yGjErBcBUUvvNaegkkzjOaPBPFNMcbHejyyWQg"

@Composable
fun SunCycleCard(
    locationName: String,
    sunriseTime: String,
    sunsetTime: String,
    daylightDuration: String,
    solarProgress: Float,
    modifier: Modifier = Modifier,
) {
    val colors = veridiaColors
    val strings = veridiaStrings
    VeridiaCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sun_cycle_card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.WbTwilight,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = strings.sunCycleTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSub,
                    )
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textMain,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.subtle,
                border = BorderStroke(1.dp, colors.border),
            ) {
                Text(
                    text = VeridiaLocalizer.daylightDuration(daylightDuration, strings),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.sunset,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        SolarArcCanvas(
            sunriseTime = sunriseTime,
            sunsetTime = sunsetTime,
            solarProgress = solarProgress,
            showInlineLabels = false,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Sunrise pill with vector icons
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .testTag("sun_cycle_sunrise"),
                shape = RoundedCornerShape(14.dp),
                color = colors.warning.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.28f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.warning.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = strings.sunriseLabel,
                            tint = colors.warning,
                            modifier = Modifier.size(16.dp),
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = colors.sunset,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(11.dp)
                                .padding(1.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = strings.sunriseLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSub,
                        )
                        Text(
                            text = sunriseTime,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = SpaceMonoFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colors.textMain,
                        )
                    }
                }
            }

            // Sunset pill with vector icons
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .testTag("sun_cycle_sunset"),
                shape = RoundedCornerShape(14.dp),
                color = colors.primary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.24f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NightsStay,
                            contentDescription = strings.sunsetLabel,
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = colors.sunset,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(11.dp)
                                .padding(1.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = strings.sunsetLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSub,
                        )
                        Text(
                            text = sunsetTime,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = SpaceMonoFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colors.textMain,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SolarArcCanvas(
    sunriseTime: String,
    sunsetTime: String,
    solarProgress: Float,
    modifier: Modifier = Modifier,
    showInlineLabels: Boolean = true,
) {
    val colors = veridiaColors
    val strings = veridiaStrings
    val animatedProgress by animateFloatAsState(
        targetValue = solarProgress.coerceIn(0.04f, 0.96f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "solar_progress_anim",
    )
    val orbPulsePhase by rememberQuantizedCyclePhase(
        enabled = true,
        cycleDurationMs = 3600,
        pingPong = true,
        idleValue = 0.35f,
        label = "sun_orb_pulse",
    )
    var tapCount by remember { mutableIntStateOf(0) }
    val tapBurst = remember { Animatable(0f) }
    LaunchedEffect(tapCount) {
        if (tapCount > 0) {
            tapBurst.snapTo(1f)
            tapBurst.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing),
            )
        }
    }
    val orbScale = 0.90f + orbPulsePhase * 0.34f + tapBurst.value * 0.55f
    val arcPath = remember { Path() }
    val horizonDash = remember { PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (showInlineLabels) 104.dp else 82.dp)
            .semantics {
                contentDescription = "Solar arc trajectory, tap to pulse sun orb"
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
            ) { tapCount += 1 }
            .testTag("solar_arc_interactive_canvas"),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val horizonY = h * if (showInlineLabels) 0.76f else 0.84f
            val leftX = w * 0.08f
            val rightX = w * 0.92f
            val peakY = h * 0.14f

            // Dashed horizon line
            drawLine(
                color = colors.border,
                start = Offset(w * 0.05f, horizonY),
                end = Offset(w * 0.95f, horizonY),
                strokeWidth = 2.5f,
                pathEffect = horizonDash,
            )

            // Solar arc quadratic curve (reusing hoisted Path)
            arcPath.rewind()
            arcPath.moveTo(leftX, horizonY)
            arcPath.quadraticTo(w * 0.5f, peakY, rightX, horizonY)
            drawPath(
                path = arcPath,
                color = colors.warning.copy(alpha = 0.45f),
                style = Stroke(width = 4.5f, cap = StrokeCap.Round),
            )

            // Sunrise & sunset anchor dots on horizon
            drawCircle(color = colors.warning.copy(alpha = 0.65f), radius = 4.5f, center = Offset(leftX, horizonY))
            drawCircle(color = colors.primary.copy(alpha = 0.65f), radius = 4.5f, center = Offset(rightX, horizonY))

            // Current sun position along quadratic Bezier
            val t = animatedProgress
            val oneMinusT = 1f - t
            val ctrlX = w * 0.5f
            val sunX = (oneMinusT * oneMinusT * leftX) + (2f * oneMinusT * t * ctrlX) + (t * t * rightX)
            val sunY = (oneMinusT * oneMinusT * horizonY) + (2f * oneMinusT * t * peakY) + (t * t * horizonY)

            if (tapBurst.value > 0.02f) {
                drawCircle(
                    color = colors.warning.copy(alpha = tapBurst.value * 0.42f),
                    radius = 18f + (1f - tapBurst.value) * 28f,
                    center = Offset(sunX, sunY),
                    style = Stroke(width = 2.5f),
                )
            }
            drawCircle(
                color = colors.warning.copy(alpha = 0.24f + tapBurst.value * 0.16f),
                radius = 18f * orbScale,
                center = Offset(sunX, sunY),
            )
            drawCircle(
                color = colors.warning,
                radius = 9f + tapBurst.value * 2.5f,
                center = Offset(sunX, sunY),
            )
        }

        if (showInlineLabels) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 8.dp),
            ) {
                Text(
                    text = strings.sunriseLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSub,
                )
                Text(
                    text = sunriseTime,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textMain,
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = strings.sunsetLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSub,
                )
                Text(
                    text = sunsetTime,
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textMain,
                )
            }
        }
    }
}

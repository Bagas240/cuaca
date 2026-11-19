package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.WeatherConditionKind
import com.example.ui.theme.PastelSky
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeWeatherBackdrop(
    conditionKind: WeatherConditionKind,
    windSpeedKmh: Int,
    precipChancePercent: Int,
    gyroTilt: Offset,
    stageHeight: Dp = 540.dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val skyPalette = when (conditionKind) {
        WeatherConditionKind.SUNNY -> listOf(Color(0xFF1565C0), Color(0xFF4BA3F2), Color(0xFFBBE1FA))
        WeatherConditionKind.PARTLY_CLOUDY -> listOf(Color(0xFF215B9E), Color(0xFF639AD1), Color(0xFFC2DCF2))
        WeatherConditionKind.CLOUDY -> listOf(Color(0xFF2B3B4E), Color(0xFF53677E), Color(0xFF98AABF))
        WeatherConditionKind.FOG -> listOf(Color(0xFF586A7D), Color(0xFF8C9EAE), Color(0xFFD5DFE8))
        WeatherConditionKind.RAIN -> listOf(Color(0xFF19273A), Color(0xFF334963), Color(0xFF6B819A))
        WeatherConditionKind.THUNDERSTORM -> listOf(Color(0xFF0C1424), Color(0xFF1D2C42), Color(0xFF42546C))
        WeatherConditionKind.SUNSET -> listOf(Color(0xFF2B2D5F), Color(0xFF9C4E73), Color(0xFFF39C6B))
    }

    val photoAlpha = when (conditionKind) {
        WeatherConditionKind.SUNNY -> 0.45f
        WeatherConditionKind.PARTLY_CLOUDY -> 0.72f
        WeatherConditionKind.CLOUDY -> 0.88f
        WeatherConditionKind.FOG -> 0.44f
        WeatherConditionKind.RAIN -> 0.84f
        WeatherConditionKind.THUNDERSTORM -> 0.78f
        WeatherConditionKind.SUNSET -> 0.62f
    }

    val photoColorFilter = when (conditionKind) {
        WeatherConditionKind.RAIN, WeatherConditionKind.THUNDERSTORM -> {
            val matrix = ColorMatrix().apply { setToSaturation(0.65f) }
            ColorFilter.colorMatrix(matrix)
        }
        WeatherConditionKind.FOG -> {
            val matrix = ColorMatrix().apply { setToSaturation(0.38f) }
            ColorFilter.colorMatrix(matrix)
        }
        else -> null
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(stageHeight)
            .background(PastelSky)
            .testTag("weather_backdrop_stage"),
    ) {
        // 1. Atmospheric Sky Gradient Base
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to skyPalette[0],
                        0.48f to skyPalette[1],
                        0.82f to skyPalette[2],
                        1.0f to PastelSky,
                    ),
                ),
        )

        // 2. Real Photographic Sky Backdrop with subtle gyro parallax
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(HOME_SKY_IMAGE_URL)
                .crossfade(true)
                .build(),
            contentDescription = "Atmospheric sky backdrop",
            contentScale = ContentScale.Crop,
            alpha = photoAlpha,
            colorFilter = photoColorFilter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.08f
                    scaleY = 1.08f
                    translationX = (gyroTilt.x * 14f).coerceIn(-18f, 18f)
                    translationY = (gyroTilt.y * 10f).coerceIn(-14f, 14f)
                },
        )

        // 3. Condition Color Grading Veil
        val atmosphericOverlay = when (conditionKind) {
            WeatherConditionKind.SUNNY -> Brush.verticalGradient(
                0.0f to Color(0x400284C7),
                0.45f to Color(0x1838BDF8),
                1.0f to Color.Transparent,
            )
            WeatherConditionKind.FOG -> Brush.verticalGradient(
                0.0f to Color(0x5AE2E8F0),
                0.55f to Color(0x88EDF2F7),
                1.0f to Color(0xAAF8FAFC),
            )
            WeatherConditionKind.THUNDERSTORM -> Brush.verticalGradient(
                0.0f to Color(0x88070B14),
                0.55f to Color(0x501E293B),
                1.0f to Color.Transparent,
            )
            WeatherConditionKind.RAIN -> Brush.verticalGradient(
                0.0f to Color(0x550F172A),
                0.55f to Color(0x281E293B),
                1.0f to Color.Transparent,
            )
            WeatherConditionKind.SUNSET -> Brush.verticalGradient(
                0.0f to Color(0x50312E81),
                0.52f to Color(0x58EA580C),
                1.0f to Color.Transparent,
            )
            else -> Brush.verticalGradient(
                0.0f to Color(0x2E0F172A),
                0.50f to Color.Transparent,
                1.0f to Color.Transparent,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(atmosphericOverlay),
        )

        // 4. Per-Condition Organic Weather Animation Canvas (Clouds, Sun, Fog Waves, Rain, Lightning)
        WeatherParticlesLayer(
            conditionKind = conditionKind,
            windSpeedKmh = windSpeedKmh,
            precipChancePercent = precipChancePercent,
            parallaxOffset = Offset(
                x = gyroTilt.x * 12f,
                y = gyroTilt.y * 8f,
            ),
            modifier = Modifier.fillMaxSize(),
        )

        // 5. Smooth bottom transition into PastelSky for card legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.68f to Color.Transparent,
                        0.89f to PastelSky.copy(alpha = 0.86f),
                        1.0f to PastelSky,
                    ),
                ),
        )
    }
}

@Composable
fun WeatherParticlesLayer(
    conditionKind: WeatherConditionKind,
    windSpeedKmh: Int,
    precipChancePercent: Int,
    parallaxOffset: Offset = Offset.Zero,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_particles")
    val rainPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (windSpeedKmh >= 24) 680 else 940,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rain_fall",
    )
    val cloudDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (26000 - (windSpeedKmh * 220)).coerceIn(12000, 26000),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "cloud_drift",
    )
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "solar_pulse",
    )
    val lightningCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "lightning_cycle",
    )

    val lightningFlash = if (conditionKind == WeatherConditionKind.THUNDERSTORM) {
        when {
            lightningCycle in 0.70f..0.73f -> 0.72f
            lightningCycle in 0.75f..0.78f -> 0.42f
            else -> 0f
        }
    } else 0f

    Canvas(
        modifier = modifier.testTag("weather_particles_layer"),
    ) {
        val w = size.width
        val h = size.height
        val windNorm = (windSpeedKmh / 45f).coerceIn(0.06f, 1.0f)

        // A. SUN / PARTLY_CLOUDY / SUNSET: Crisp Solar Disc, Corona, Volumetric Rays & Motes
        if (conditionKind == WeatherConditionKind.SUNNY ||
            conditionKind == WeatherConditionKind.PARTLY_CLOUDY ||
            conditionKind == WeatherConditionKind.SUNSET
        ) {
            val isSunset = conditionKind == WeatherConditionKind.SUNSET
            val sunCenter = Offset(
                x = if (isSunset) w * 0.50f else (w * 0.78f - parallaxOffset.x * 0.7f).coerceIn(w * 0.25f, w * 0.90f),
                y = if (isSunset) h * 0.42f else (h * 0.16f + parallaxOffset.y * 0.5f).coerceIn(h * 0.08f, h * 0.34f),
            )
            val coronaColor = if (isSunset) Color(0xFFFB923C) else Color(0xFFFDE047)
            val coreColor = if (isSunset) Color(0xFFFFEDD5) else Color(0xFFFFFBEB)
            val glowAlpha = when (conditionKind) {
                WeatherConditionKind.SUNNY -> 0.58f
                WeatherConditionKind.SUNSET -> 0.52f
                else -> 0.34f
            }
            val breathe = 1f + 0.05f * sin(pulsePhase)
            val outerRadius = w * 0.46f * breathe

            // Outer volumetric corona (strictly fades to Transparent inside circle radius)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to coreColor.copy(alpha = glowAlpha),
                    0.22f to coronaColor.copy(alpha = glowAlpha * 0.65f),
                    0.58f to coronaColor.copy(alpha = glowAlpha * 0.18f),
                    1.0f to Color.Transparent,
                    center = sunCenter,
                    radius = outerRadius,
                ),
                center = sunCenter,
                radius = outerRadius,
            )

            // Crisp luminous solar disc core
            val discRadius = (w * if (isSunset) 0.085f else 0.062f)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color.White.copy(alpha = glowAlpha + 0.32f),
                    0.65f to coreColor.copy(alpha = glowAlpha + 0.15f),
                    1.0f to Color.Transparent,
                    center = sunCenter,
                    radius = discRadius * 1.45f,
                ),
                center = sunCenter,
                radius = discRadius * 1.45f,
            )

            // Rotating subtle sunbeam shafts
            val beamBaseAngle = if (isSunset) 210f else 132f
            val beamCount = if (conditionKind == WeatherConditionKind.SUNNY) 5 else 3
            for (b in 0 until beamCount) {
                val deg = beamBaseAngle + b * 16f + sin(pulsePhase + b) * 3f
                val rad = deg * (PI.toFloat() / 180f)
                val spread = 0.075f + (b % 2) * 0.025f
                val shaftLen = h * 0.72f
                val p1 = Offset(
                    sunCenter.x + cos(rad - spread) * shaftLen,
                    sunCenter.y + sin(rad - spread) * shaftLen,
                )
                val p2 = Offset(
                    sunCenter.x + cos(rad + spread) * shaftLen,
                    sunCenter.y + sin(rad + spread) * shaftLen,
                )
                val shaftPath = Path().apply {
                    moveTo(sunCenter.x, sunCenter.y)
                    lineTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    close()
                }
                val pulse = 0.72f + 0.28f * sin(pulsePhase + b * 1.1f)
                drawPath(
                    path = shaftPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            coreColor.copy(alpha = glowAlpha * 0.28f * pulse),
                            Color.Transparent,
                        ),
                        start = sunCenter,
                        end = Offset((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f),
                    ),
                )
            }

            // Floating sunlit bokeh motes
            val moteCount = if (conditionKind == WeatherConditionKind.SUNNY) 24 else 14
            for (i in 0 until moteCount) {
                val seedX = (i * 0.6180339f) % 1f
                val seedY = (i * 0.3819660f) % 0.72f
                val mx = (w * seedX + sin(pulsePhase + i * 0.9f) * 18f - parallaxOffset.x * 0.3f + w) % w
                val my = h * (0.06f + seedY) + cos(pulsePhase * 0.8f + i) * 10f
                val radius = 2.0f + (i % 3) * 1.4f
                val alpha = 0.24f + 0.26f * (0.5f + 0.5f * sin(pulsePhase + i * 1.3f))
                drawCircle(
                    color = coreColor.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(mx, my),
                )
            }
        }

        // B. VOLUMINOUS MULTI-LOBE CUMULUS & STORM CLOUDS (Unclipped radial circles — zero ovals/boxes!)
        when (conditionKind) {
            WeatherConditionKind.SUNNY -> {
                // Sparse high wispy sunlit clouds
                drawSoftCumulusCluster(
                    centerX = ((0.18f + cloudDrift * 1.0f) % 1.35f - 0.17f) * w,
                    centerY = h * 0.18f,
                    scale = w * 0.22f,
                    topTint = Color.White.copy(alpha = 0.38f),
                    baseTint = Color(0xFFE0F2FE).copy(alpha = 0.22f),
                )
                drawSoftCumulusCluster(
                    centerX = ((0.72f + cloudDrift * 0.7f) % 1.35f - 0.17f) * w,
                    centerY = h * 0.29f,
                    scale = w * 0.18f,
                    topTint = Color.White.copy(alpha = 0.30f),
                    baseTint = Color(0xFFE0F2FE).copy(alpha = 0.18f),
                )
            }
            WeatherConditionKind.PARTLY_CLOUDY -> {
                drawSoftCumulusCluster(
                    centerX = ((0.15f + cloudDrift * 1.1f) % 1.40f - 0.20f) * w,
                    centerY = h * 0.16f,
                    scale = w * 0.28f,
                    topTint = Color.White.copy(alpha = 0.54f),
                    baseTint = Color(0xFFCBD5E1).copy(alpha = 0.32f),
                )
                drawSoftCumulusCluster(
                    centerX = ((0.64f + cloudDrift * 0.85f) % 1.40f - 0.20f) * w,
                    centerY = h * 0.26f,
                    scale = w * 0.30f,
                    topTint = Color.White.copy(alpha = 0.48f),
                    baseTint = Color(0xFFCBD5E1).copy(alpha = 0.28f),
                )
                drawSoftCumulusCluster(
                    centerX = ((0.92f + cloudDrift * 1.3f) % 1.40f - 0.20f) * w,
                    centerY = h * 0.39f,
                    scale = w * 0.24f,
                    topTint = Color.White.copy(alpha = 0.36f),
                    baseTint = Color(0xFFDBEAFE).copy(alpha = 0.20f),
                )
            }
            WeatherConditionKind.CLOUDY -> {
                drawSoftCumulusCluster(
                    centerX = ((0.10f + cloudDrift * 0.9f) % 1.45f - 0.22f) * w,
                    centerY = h * 0.13f,
                    scale = w * 0.34f,
                    topTint = Color(0xFFF1F5F9).copy(alpha = 0.56f),
                    baseTint = Color(0xFF64748B).copy(alpha = 0.42f),
                )
                drawSoftCumulusCluster(
                    centerX = ((0.55f + cloudDrift * 1.15f) % 1.45f - 0.22f) * w,
                    centerY = h * 0.22f,
                    scale = w * 0.36f,
                    topTint = Color(0xFFE2E8F0).copy(alpha = 0.52f),
                    baseTint = Color(0xFF64748B).copy(alpha = 0.38f),
                )
                drawSoftCumulusCluster(
                    centerX = ((0.88f + cloudDrift * 1.4f) % 1.45f - 0.22f) * w,
                    centerY = h * 0.34f,
                    scale = w * 0.30f,
                    topTint = Color(0xFFE2E8F0).copy(alpha = 0.42f),
                    baseTint = Color(0xFF94A3B8).copy(alpha = 0.30f),
                )
            }
            WeatherConditionKind.RAIN, WeatherConditionKind.THUNDERSTORM -> {
                val isStorm = conditionKind == WeatherConditionKind.THUNDERSTORM
                val flashBoost = if (isStorm) lightningFlash * 0.45f else 0f
                val topShade = if (isStorm) Color(0xFF475569).copy(alpha = 0.62f + flashBoost * 0.3f) else Color(0xFF64748B).copy(alpha = 0.54f)
                val baseShade = if (isStorm) Color(0xFF0F172A).copy(alpha = 0.68f) else Color(0xFF334155).copy(alpha = 0.52f)
                drawSoftCumulusCluster(
                    centerX = ((0.12f + cloudDrift * 1.3f) % 1.45f - 0.22f) * w,
                    centerY = h * 0.11f,
                    scale = w * 0.38f,
                    topTint = topShade,
                    baseTint = baseShade,
                )
                drawSoftCumulusCluster(
                    centerX = ((0.58f + cloudDrift * 1.6f) % 1.45f - 0.22f) * w,
                    centerY = h * 0.17f,
                    scale = w * 0.40f,
                    topTint = topShade,
                    baseTint = baseShade,
                )
                drawSoftCumulusCluster(
                    centerX = ((0.85f + cloudDrift * 1.9f) % 1.45f - 0.22f) * w,
                    centerY = h * 0.26f,
                    scale = w * 0.32f,
                    topTint = topShade.copy(alpha = 0.44f),
                    baseTint = baseShade.copy(alpha = 0.38f),
                )
            }
            WeatherConditionKind.SUNSET -> {
                drawSoftCumulusCluster(
                    centerX = ((0.22f + cloudDrift * 0.85f) % 1.40f - 0.20f) * w,
                    centerY = h * 0.22f,
                    scale = w * 0.26f,
                    topTint = Color(0xFFFED7AA).copy(alpha = 0.44f),
                    baseTint = Color(0xFF9D174D).copy(alpha = 0.28f),
                )
                drawSoftCumulusCluster(
                    centerX = ((0.76f + cloudDrift * 1.1f) % 1.40f - 0.20f) * w,
                    centerY = h * 0.31f,
                    scale = w * 0.28f,
                    topTint = Color(0xFFFDBA74).copy(alpha = 0.40f),
                    baseTint = Color(0xFF701A75).copy(alpha = 0.25f),
                )
            }
            WeatherConditionKind.FOG -> Unit
        }

        // C. FOG: Undulating Smooth Cubic-Bezier Fog Waves + Drifting Mist Particles
        if (conditionKind == WeatherConditionKind.FOG) {
            for (layer in 0..3) {
                val baseY = h * (0.18f + layer * 0.14f)
                val amp = h * (0.025f + layer * 0.006f)
                val phaseShift = cloudDrift * 2f * PI.toFloat() * (1f + layer * 0.35f) + layer * 1.3f
                val fogPath = Path().apply {
                    moveTo(0f, baseY + sin(phaseShift) * amp)
                    cubicTo(
                        w * 0.30f, baseY - cos(phaseShift + 0.9f) * amp,
                        w * 0.68f, baseY + sin(phaseShift + 1.8f) * amp,
                        w, baseY - sin(phaseShift + 2.7f) * amp,
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = fogPath,
                    brush = Brush.verticalGradient(
                        0.0f to Color(0xFFF8FAFC).copy(alpha = 0.32f - layer * 0.04f),
                        0.55f to Color(0xFFE2E8F0).copy(alpha = 0.24f),
                        1.0f to Color.Transparent,
                        startY = baseY - amp,
                        endY = baseY + h * 0.28f,
                    ),
                )
            }
            for (i in 0 until 34) {
                val speed = (0.45f + (i % 4) * 0.22f) * (0.6f + windNorm)
                val seedX = (i * 0.6180339f) % 1f
                val seedY = (i * 0.2931f) % 0.72f
                val x = ((seedX + cloudDrift * speed * 2.4f) % 1f) * w
                val y = h * (0.10f + seedY) + sin(pulsePhase + i) * 7f
                drawCircle(
                    color = Color(0xFFF8FAFC).copy(alpha = 0.28f),
                    radius = 3.2f + (i % 4) * 1.5f,
                    center = Offset(x, y),
                )
            }
        }

        // D. THUNDERSTORM LIGHTNING: Synchronized Cloud Glow + Forked Lightning Bolt
        if (conditionKind == WeatherConditionKind.THUNDERSTORM && lightningFlash > 0f) {
            val boltOrigin = Offset(w * 0.62f, h * 0.08f)
            // Cloud interior burst glow
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color(0xFFE0F2FE).copy(alpha = lightningFlash * 0.85f),
                    0.45f to Color(0xFF93C5FD).copy(alpha = lightningFlash * 0.35f),
                    1.0f to Color.Transparent,
                    center = boltOrigin,
                    radius = w * 0.45f,
                ),
                center = boltOrigin,
                radius = w * 0.45f,
            )

            // Main forked bolt path
            val mainBolt = Path().apply {
                moveTo(boltOrigin.x, boltOrigin.y)
                lineTo(boltOrigin.x - w * 0.06f, h * 0.19f)
                lineTo(boltOrigin.x + w * 0.03f, h * 0.24f)
                lineTo(boltOrigin.x - w * 0.09f, h * 0.39f)
                lineTo(boltOrigin.x - w * 0.02f, h * 0.44f)
                lineTo(boltOrigin.x - w * 0.11f, h * 0.60f)
            }
            val forkBranch = Path().apply {
                moveTo(boltOrigin.x + w * 0.03f, h * 0.24f)
                lineTo(boltOrigin.x + w * 0.12f, h * 0.34f)
                lineTo(boltOrigin.x + w * 0.08f, h * 0.45f)
            }
            drawPath(
                path = mainBolt,
                color = Color(0xFF7DD3FC).copy(alpha = lightningFlash * 0.55f),
                style = Stroke(width = 11f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                path = mainBolt,
                color = Color.White.copy(alpha = lightningFlash),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                path = forkBranch,
                color = Color(0xFFBAE6FD).copy(alpha = lightningFlash * 0.85f),
                style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }

        // E. RAIN & THUNDERSTORM: Multi-Depth Wind-Slanted Rain Streaks + Surface Splash Mist
        val isRaining = conditionKind == WeatherConditionKind.RAIN ||
            conditionKind == WeatherConditionKind.THUNDERSTORM
        if (isRaining) {
            val intensity = when {
                conditionKind == WeatherConditionKind.THUNDERSTORM -> 1.50f
                precipChancePercent >= 70 -> 1.25f
                else -> 0.95f
            }
            val dropCount = (64 * intensity).toInt().coerceIn(48, 96)
            val windSlantPx = (windSpeedKmh * 1.75f).coerceIn(10f, 58f)

            for (i in 0 until dropCount) {
                val depth = i % 3
                val seedX = (i * 0.6180339f) % 1.22f - 0.10f
                val seedY = (i * 0.3819660f) % 1f
                val speedScale = 0.76f + depth * 0.45f
                val progress = (rainPhase * speedScale + seedY) % 1f

                val startX = w * seedX - progress * windSlantPx * (0.95f + depth * 0.35f) -
                    parallaxOffset.x * (0.15f + depth * 0.22f)
                val startY = -52f + progress * (h * 0.92f)
                val streakLen = (22f + depth * 20f) * intensity
                val endX = startX - windSlantPx * (0.28f + depth * 0.08f)
                val endY = startY + streakLen

                val dropAlpha = when (depth) {
                    0 -> 0.24f
                    1 -> 0.45f
                    else -> 0.72f
                }
                val strokeWidth = when (depth) {
                    0 -> 1.2f
                    1 -> 1.8f
                    else -> 2.6f
                }

                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFFE0F2FE).copy(alpha = dropAlpha),
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                    ),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }

            // Raindrop rebound mist sparkles along lower atmospheric shelf
            for (s in 0..18) {
                val sx = ((s * 0.731f + cloudDrift * 0.6f) % 1f) * w
                val sy = h * (0.64f + (s % 5) * 0.045f)
                val sparkleProgress = (rainPhase * 1.5f + s * 0.19f) % 1f
                val sparkleAlpha = sin(sparkleProgress * PI.toFloat()).coerceIn(0f, 1f) * 0.34f
                drawCircle(
                    color = Color(0xFFE0F2FE).copy(alpha = sparkleAlpha),
                    radius = 1.9f + (s % 2) * 1.1f,
                    center = Offset(sx, sy),
                )
            }
        }
    }
}

/**
 * Draws a natural, puffy multi-lobe cumulus cluster composed of overlapping circular radial gradients
 * that strictly fade to Color.Transparent inside their own radii — zero boxy or oval clipping edges.
 */
private fun DrawScope.drawSoftCumulusCluster(
    centerX: Float,
    centerY: Float,
    scale: Float,
    topTint: Color,
    baseTint: Color,
) {
    val puffs = listOf(
        Triple(-0.55f, 0.14f, 0.52f) to baseTint,
        Triple(0.58f, 0.16f, 0.48f) to baseTint,
        Triple(0.0f, 0.12f, 0.66f) to baseTint,
        Triple(-0.28f, -0.06f, 0.56f) to topTint,
        Triple(0.26f, -0.04f, 0.52f) to topTint,
        Triple(0.0f, -0.18f, 0.62f) to topTint,
    )
    puffs.forEach { (geom, tint) ->
        val (dxFactor, dyFactor, rFactor) = geom
        val c = Offset(centerX + dxFactor * scale, centerY + dyFactor * scale)
        val r = rFactor * scale
        drawCircle(
            brush = Brush.radialGradient(
                0.0f to tint,
                0.52f to tint.copy(alpha = tint.alpha * 0.58f),
                1.0f to Color.Transparent,
                center = c,
                radius = r,
            ),
            center = c,
            radius = r,
        )
    }
}

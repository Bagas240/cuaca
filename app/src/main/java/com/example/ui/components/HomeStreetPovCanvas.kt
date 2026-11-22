package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.SolarThemePhase
import com.example.data.model.WeatherConditionKind
import com.example.ui.theme.veridiaColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun HomeWeatherBackdrop(
    conditionKind: WeatherConditionKind,
    windSpeedKmh: Int,
    precipChancePercent: Int,
    gyroTilt: Offset,
    stageHeight: Dp = 540.dp,
    interactionImpulseCount: Int = 0,
    onInteract: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = veridiaColors

    val skyPalette = when (colors.phase) {
        SolarThemePhase.SUNRISE_SUNSET -> when (conditionKind) {
            WeatherConditionKind.THUNDERSTORM -> listOf(Color(0xFF282238), Color(0xFF6B5344), Color(0xFFD8B86A))
            else -> listOf(colors.backdropTop, colors.backdropMid, colors.backdropBottom)
        }
        SolarThemePhase.AFTERNOON_DUSK -> when (conditionKind) {
            WeatherConditionKind.THUNDERSTORM -> listOf(Color(0xFF0E1B33), Color(0xFF243B61), Color(0xFF597CA8))
            else -> listOf(colors.backdropTop, colors.backdropMid, colors.backdropBottom)
        }
        SolarThemePhase.NIGHT -> listOf(colors.backdropTop, colors.backdropMid, colors.backdropBottom)
        SolarThemePhase.DAY -> when (conditionKind) {
            WeatherConditionKind.SUNNY -> listOf(Color(0xFF1967C2), Color(0xFF4CA3EC), Color(0xFFE7DFB2))
            WeatherConditionKind.PARTLY_CLOUDY -> listOf(Color(0xFF215B9E), Color(0xFF609CD4), Color(0xFFE4DDB4))
            WeatherConditionKind.CLOUDY -> listOf(Color(0xFF2D3F54), Color(0xFF566B82), Color(0xFFAEC0CF))
            WeatherConditionKind.FOG -> listOf(Color(0xFF586A7D), Color(0xFF8C9EAE), Color(0xFFDDE3DE))
            WeatherConditionKind.RAIN -> listOf(Color(0xFF18273A), Color(0xFF334963), Color(0xFF6E8399))
            WeatherConditionKind.THUNDERSTORM -> listOf(Color(0xFF0C1424), Color(0xFF1D2C42), Color(0xFF42546C))
            WeatherConditionKind.SUNSET -> listOf(Color(0xFF364578), Color(0xFFD49B4B), Color(0xFFFCE69C))
        }
    }

    val photoAlpha = when (colors.phase) {
        SolarThemePhase.NIGHT -> when (conditionKind) {
            WeatherConditionKind.SUNNY, WeatherConditionKind.PARTLY_CLOUDY -> 0.16f
            else -> 0.24f
        }
        SolarThemePhase.AFTERNOON_DUSK -> 0.56f
        SolarThemePhase.SUNRISE_SUNSET -> 0.42f
        SolarThemePhase.DAY -> when (conditionKind) {
            WeatherConditionKind.SUNNY -> 0.42f
            WeatherConditionKind.PARTLY_CLOUDY -> 0.68f
            WeatherConditionKind.CLOUDY -> 0.84f
            WeatherConditionKind.FOG -> 0.40f
            WeatherConditionKind.RAIN -> 0.80f
            WeatherConditionKind.THUNDERSTORM -> 0.72f
            WeatherConditionKind.SUNSET -> 0.52f
        }
    }

    val photoColorFilter = remember(conditionKind, colors.phase) {
        when {
            colors.phase == SolarThemePhase.NIGHT -> {
                val matrix = ColorMatrix().apply {
                    setToSaturation(0.35f)
                    timesAssign(
                        ColorMatrix(
                            floatArrayOf(
                                0.45f, 0f, 0f, 0f, 0f,
                                0f, 0.55f, 0f, 0f, 0f,
                                0f, 0f, 0.78f, 0f, 0f,
                                0f, 0f, 0f, 1f, 0f,
                            ),
                        ),
                    )
                }
                ColorFilter.colorMatrix(matrix)
            }
            conditionKind == WeatherConditionKind.RAIN || conditionKind == WeatherConditionKind.THUNDERSTORM -> {
                val matrix = ColorMatrix().apply { setToSaturation(0.62f) }
                ColorFilter.colorMatrix(matrix)
            }
            conditionKind == WeatherConditionKind.FOG -> {
                val matrix = ColorMatrix().apply { setToSaturation(0.36f) }
                ColorFilter.colorMatrix(matrix)
            }
            else -> null
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val baseModifier = modifier
        .fillMaxWidth()
        .height(stageHeight)
        .background(colors.sky)
        .testTag("weather_backdrop_stage")
        .then(
            if (onInteract != null) {
                Modifier
                    .semantics {
                        contentDescription = "Interactive sky animation, tap to trigger weather gust or pulse"
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onInteract,
                    )
            } else {
                Modifier
            }
        )

    BoxWithConstraints(modifier = baseModifier) {
        // 1. Atmospheric Sky Gradient Base
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to skyPalette[0],
                        0.48f to skyPalette[1],
                        0.82f to skyPalette[2],
                        1.0f to colors.sky,
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
        val isNight = colors.phase == SolarThemePhase.NIGHT
        val atmosphericOverlay = remember(conditionKind, colors.phase) {
            when (conditionKind) {
                WeatherConditionKind.SUNNY -> Brush.verticalGradient(
                    0.0f to if (isNight) Color(0x55060B18) else Color(0x300284C7),
                    0.45f to if (isNight) Color(0x330F1D3A) else Color(0x1438BDF8),
                    1.0f to Color.Transparent,
                )
                WeatherConditionKind.FOG -> Brush.verticalGradient(
                    0.0f to if (isNight) Color(0x66162238) else Color(0x5AE2E8F0),
                    0.55f to if (isNight) Color(0x881E293B) else Color(0x88EDF2F7),
                    1.0f to if (isNight) Color(0xAA0F172A) else Color(0xAAF1F5F9),
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
                    0.0f to Color(0x40312E81),
                    0.52f to Color(0x48D97706),
                    1.0f to Color.Transparent,
                )
                else -> Brush.verticalGradient(
                    0.0f to if (isNight) Color(0x55060B18) else Color(0x220F172A),
                    0.50f to Color.Transparent,
                    1.0f to Color.Transparent,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(atmosphericOverlay),
        )

        // 4. Per-Condition Realistic Atmospheric Animation Canvas (Multi-Layer Baked Clouds, Sun/Stars, Fog, Rain, Lightning)
        WeatherParticlesLayer(
            conditionKind = conditionKind,
            windSpeedKmh = windSpeedKmh,
            precipChancePercent = precipChancePercent,
            parallaxOffset = Offset(
                x = gyroTilt.x * 12f,
                y = gyroTilt.y * 8f,
            ),
            interactionImpulseCount = interactionImpulseCount,
            modifier = Modifier.fillMaxSize(),
        )

        // 5. Smooth bottom transition into active theme sky background for card legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.68f to Color.Transparent,
                        0.89f to colors.sky.copy(alpha = 0.88f),
                        1.0f to colors.sky,
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
    interactionImpulseCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    val colors = veridiaColors
    val isNight = colors.phase == SolarThemePhase.NIGHT
    val isDawnOrSunset = colors.phase == SolarThemePhase.SUNRISE_SUNSET
    val isAfternoon = colors.phase == SolarThemePhase.AFTERNOON_DUSK

    val needsRainPhase = conditionKind == WeatherConditionKind.RAIN ||
        conditionKind == WeatherConditionKind.THUNDERSTORM
    val needsLightning = conditionKind == WeatherConditionKind.THUNDERSTORM
    val cloudCycleMs = (28000 - (windSpeedKmh * 220)).coerceIn(13000, 28000)
    val rainCycleMs = if (windSpeedKmh >= 24) 680 else 940

    // Quantized 30fps / 20fps battery-aware animation drivers (only active cycles run!)
    val cloudDrift by rememberQuantizedCyclePhase(
        enabled = true,
        cycleDurationMs = cloudCycleMs,
        label = "cloud_drift",
    )
    val pulseRaw by rememberQuantizedCyclePhase(
        enabled = true,
        cycleDurationMs = 4800,
        label = "solar_pulse",
    )
    val pulsePhase = pulseRaw * (2f * PI).toFloat()

    val rainPhase by rememberQuantizedCyclePhase(
        enabled = needsRainPhase,
        cycleDurationMs = rainCycleMs,
        label = "rain_fall",
    )
    val lightningCycle by rememberQuantizedCyclePhase(
        enabled = needsLightning,
        cycleDurationMs = 5400,
        label = "lightning_cycle",
    )

    // Interactive tap impulse wave (1f -> 0f over 950ms)
    val tapImpulseAnim = remember { Animatable(0f) }
    LaunchedEffect(interactionImpulseCount) {
        if (interactionImpulseCount > 0) {
            tapImpulseAnim.snapTo(1f)
            tapImpulseAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            )
        }
    }
    val tapImpulse = tapImpulseAnim.value
    val cloudGustBoost = tapImpulse * 0.045f

    val ambientLightning = if (needsLightning) {
        when {
            lightningCycle in 0.70f..0.73f -> 0.72f
            lightningCycle in 0.75f..0.78f -> 0.42f
            else -> 0f
        }
    } else 0f
    val lightningFlash = if (needsLightning) {
        max(ambientLightning, if (tapImpulse > 0.35f) tapImpulse * 0.92f else 0f)
    } else 0f

    // Pre-baked realistic volumetric cloud sprites cached once per condition & solar theme phase!
    val cloudPalette = remember(conditionKind, colors.phase) {
        resolveRealisticCloudPalette(conditionKind, colors.phase)
    }
    val cloudSprites = remember(cloudPalette) {
        CloudSpriteCache(
            cumulusFront = bakeRealisticCumulusSprite(
                width = 440,
                height = 200,
                variant = 0,
                topHighlight = cloudPalette.topHighlight,
                midTone = cloudPalette.midTone,
                baseShade = cloudPalette.baseShade,
            ),
            cumulusMid = bakeRealisticCumulusSprite(
                width = 420,
                height = 190,
                variant = 1,
                topHighlight = cloudPalette.topHighlight,
                midTone = cloudPalette.midTone,
                baseShade = cloudPalette.baseShade,
            ),
            cumulusBroad = bakeRealisticCumulusSprite(
                width = 480,
                height = 175,
                variant = 2,
                topHighlight = cloudPalette.topHighlight.copy(alpha = cloudPalette.topHighlight.alpha * 0.92f),
                midTone = cloudPalette.midTone,
                baseShade = cloudPalette.baseShade,
            ),
            cirrusWisp = bakeCirrusWispSprite(
                width = 360,
                height = 88,
                wispColor = cloudPalette.cirrusTint,
            ),
        )
    }

    // Hoisted reusable Path objects to avoid per-frame allocations inside Canvas
    val reusableBeamPath = remember { Path() }
    val reusableFogPath = remember { Path() }
    val reusableBoltPath = remember { Path() }
    val reusableForkPath = remember { Path() }

    Canvas(
        modifier = modifier.testTag("weather_particles_layer"),
    ) {
        val w = size.width
        val h = size.height
        if (w <= 1f || h <= 1f) return@Canvas
        val windNorm = (windSpeedKmh / 45f).coerceIn(0.06f, 1.0f)

        // A0. NIGHT TWINKLING STARFIELD (when Night phase & sky isn't heavy storm/fog)
        if (isNight && conditionKind != WeatherConditionKind.THUNDERSTORM && conditionKind != WeatherConditionKind.FOG) {
            val starCount = if (conditionKind == WeatherConditionKind.SUNNY || conditionKind == WeatherConditionKind.PARTLY_CLOUDY) 28 else 14
            for (i in 0 until starCount) {
                val sx = ((i * 0.6180339f + 0.07f) % 1f) * w
                val sy = ((i * 0.3819660f) % 0.56f) * h + h * 0.04f
                val twinkle = 0.25f + 0.65f * (0.5f + 0.5f * sin(pulsePhase * 1.4f + i * 1.7f))
                val boost = (twinkle + tapImpulse * 0.35f).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(0xFFE2EEFF).copy(alpha = boost * 0.72f),
                    radius = (1.3f + (i % 3) * 0.75f) * (1f + tapImpulse * 0.25f),
                    center = Offset(sx - parallaxOffset.x * 0.15f, sy - parallaxOffset.y * 0.12f),
                )
            }
        }

        // A. SUN / PARTLY_CLOUDY / SUNSET: Crisp Solar/Lunar Disc, Corona, Volumetric Rays & Motes
        if (!isNight && (conditionKind == WeatherConditionKind.SUNNY ||
                conditionKind == WeatherConditionKind.PARTLY_CLOUDY ||
                conditionKind == WeatherConditionKind.SUNSET ||
                isDawnOrSunset)
        ) {
            val isSunset = conditionKind == WeatherConditionKind.SUNSET || isDawnOrSunset
            val sunCenter = Offset(
                x = if (isSunset) w * 0.50f else (w * 0.78f - parallaxOffset.x * 0.7f).coerceIn(w * 0.25f, w * 0.90f),
                y = if (isSunset) h * 0.40f else (h * 0.16f + parallaxOffset.y * 0.5f).coerceIn(h * 0.08f, h * 0.34f),
            )
            val coronaColor = when {
                isSunset -> Color(0xFFFCD34D)
                isAfternoon -> Color(0xFFFDE68A)
                else -> Color(0xFFFDE047)
            }
            val coreColor = if (isSunset) Color(0xFFFFFBEB) else Color(0xFFFFFBE6)
            val baseGlowAlpha = when (conditionKind) {
                WeatherConditionKind.SUNNY -> 0.56f
                WeatherConditionKind.SUNSET -> 0.54f
                WeatherConditionKind.PARTLY_CLOUDY -> 0.36f
                else -> 0.24f
            }
            val glowAlpha = (baseGlowAlpha + tapImpulse * 0.24f).coerceIn(0f, 0.88f)
            val breathe = 1f + 0.05f * sin(pulsePhase) + tapImpulse * 0.16f
            val outerRadius = w * 0.46f * breathe

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

            val discRadius = (w * if (isSunset) 0.082f else 0.062f) * (1f + tapImpulse * 0.10f)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to Color.White.copy(alpha = (glowAlpha + 0.32f).coerceAtMost(1f)),
                    0.65f to coreColor.copy(alpha = (glowAlpha + 0.14f).coerceAtMost(0.95f)),
                    1.0f to Color.Transparent,
                    center = sunCenter,
                    radius = discRadius * 1.45f,
                ),
                center = sunCenter,
                radius = discRadius * 1.45f,
            )

            // Rotating sunbeam shafts using hoisted reusable Path
            val beamBaseAngle = if (isSunset) 210f else 132f
            val beamCount = if (conditionKind == WeatherConditionKind.SUNNY) 5 else 3
            for (b in 0 until beamCount) {
                val deg = beamBaseAngle + b * 16f + sin(pulsePhase + b) * 3f
                val rad = deg * (PI.toFloat() / 180f)
                val spread = 0.075f + (b % 2) * 0.025f
                val shaftLen = h * (0.70f + tapImpulse * 0.14f)
                val p1 = Offset(
                    sunCenter.x + cos(rad - spread) * shaftLen,
                    sunCenter.y + sin(rad - spread) * shaftLen,
                )
                val p2 = Offset(
                    sunCenter.x + cos(rad + spread) * shaftLen,
                    sunCenter.y + sin(rad + spread) * shaftLen,
                )
                reusableBeamPath.rewind()
                reusableBeamPath.moveTo(sunCenter.x, sunCenter.y)
                reusableBeamPath.lineTo(p1.x, p1.y)
                reusableBeamPath.lineTo(p2.x, p2.y)
                reusableBeamPath.close()

                val pulse = 0.72f + 0.28f * sin(pulsePhase + b * 1.1f)
                drawPath(
                    path = reusableBeamPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            coreColor.copy(alpha = (glowAlpha * 0.26f * pulse).coerceAtMost(0.45f)),
                            Color.Transparent,
                        ),
                        start = sunCenter,
                        end = Offset((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f),
                    ),
                )
            }

            // Floating sunlit bokeh motes
            val moteCount = if (conditionKind == WeatherConditionKind.SUNNY) 22 else 12
            for (i in 0 until moteCount) {
                val seedX = (i * 0.6180339f) % 1f
                val seedY = (i * 0.3819660f) % 0.72f
                val mx = (w * seedX + sin(pulsePhase + i * 0.9f) * 18f - parallaxOffset.x * 0.3f + w) % w
                val my = h * (0.06f + seedY) + cos(pulsePhase * 0.8f + i) * 10f
                val radius = 2.0f + (i % 3) * 1.3f
                val alpha = 0.22f + 0.24f * (0.5f + 0.5f * sin(pulsePhase + i * 1.3f))
                drawCircle(
                    color = coreColor.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(mx, my),
                )
            }
        }

        // B. REALISTIC MULTI-LAYER VOLUMINOUS CLOUDS (Pre-baked ImageBitmap GPU blits — zero per-frame shader churn!)
        if (conditionKind != WeatherConditionKind.FOG) {
            val effectiveDrift = (cloudDrift + cloudGustBoost) % 1f
            val layers = rememberCloudLayersForCondition(conditionKind)

            layers.forEach { layer ->
                val sprite = when (layer.spriteKind) {
                    CloudSpriteKind.CIRRUS -> cloudSprites.cirrusWisp
                    CloudSpriteKind.CUMULUS_FRONT -> cloudSprites.cumulusFront
                    CloudSpriteKind.CUMULUS_MID -> cloudSprites.cumulusMid
                    CloudSpriteKind.CUMULUS_BROAD -> cloudSprites.cumulusBroad
                }
                val dstW = (w * layer.widthRatio).roundToInt().coerceAtLeast(32)
                val aspect = sprite.height.toFloat() / sprite.width.toFloat()
                val dstH = (dstW * aspect).roundToInt().coerceAtLeast(24)

                val travelSpan = w + dstW * 1.35f
                val wrappedX = (((layer.initialPhaseX + effectiveDrift * layer.speedFactor) % 1f) * travelSpan) - dstW * 1.15f +
                    parallaxOffset.x * layer.parallaxScale
                val yOffset = (h * layer.yRatio) +
                    sin(pulsePhase * 0.55f + layer.bobPhase) * 4.5f +
                    parallaxOffset.y * (layer.parallaxScale * 0.55f)

                val stormHighlightAlpha = if (conditionKind == WeatherConditionKind.THUNDERSTORM && lightningFlash > 0f) {
                    (layer.alpha + lightningFlash * 0.28f).coerceIn(0f, 1f)
                } else {
                    layer.alpha
                }

                drawImage(
                    image = sprite,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(sprite.width, sprite.height),
                    dstOffset = IntOffset(wrappedX.roundToInt(), yOffset.roundToInt()),
                    dstSize = IntSize(dstW, dstH),
                    alpha = stormHighlightAlpha,
                )
            }
        }

        // C. FOG: Undulating Smooth Cubic-Bezier Fog Waves + Drifting Mist Particles
        if (conditionKind == WeatherConditionKind.FOG) {
            val mistLight = if (isNight) Color(0xFF94A3B8) else Color(0xFFF8FAFC)
            val mistMid = if (isNight) Color(0xFF64748B) else Color(0xFFE2E8F0)
            for (layer in 0..3) {
                val baseY = h * (0.18f + layer * 0.14f)
                val amp = h * (0.025f + layer * 0.006f) * (1f + tapImpulse * 0.4f)
                val phaseShift = (cloudDrift + cloudGustBoost) * 2f * PI.toFloat() * (1f + layer * 0.35f) + layer * 1.3f
                reusableFogPath.rewind()
                reusableFogPath.moveTo(0f, baseY + sin(phaseShift) * amp)
                reusableFogPath.cubicTo(
                    w * 0.30f, baseY - cos(phaseShift + 0.9f) * amp,
                    w * 0.68f, baseY + sin(phaseShift + 1.8f) * amp,
                    w, baseY - sin(phaseShift + 2.7f) * amp,
                )
                reusableFogPath.lineTo(w, h)
                reusableFogPath.lineTo(0f, h)
                reusableFogPath.close()
                drawPath(
                    path = reusableFogPath,
                    brush = Brush.verticalGradient(
                        0.0f to mistLight.copy(alpha = 0.30f - layer * 0.04f),
                        0.55f to mistMid.copy(alpha = 0.22f),
                        1.0f to Color.Transparent,
                        startY = baseY - amp,
                        endY = baseY + h * 0.28f,
                    ),
                )
            }
            for (i in 0 until 28) {
                val speed = (0.45f + (i % 4) * 0.22f) * (0.6f + windNorm)
                val seedX = (i * 0.6180339f) % 1f
                val seedY = (i * 0.2931f) % 0.72f
                val x = ((seedX + (cloudDrift + cloudGustBoost) * speed * 2.4f) % 1f) * w
                val y = h * (0.10f + seedY) + sin(pulsePhase + i) * 7f
                drawCircle(
                    color = mistLight.copy(alpha = 0.26f),
                    radius = 3.2f + (i % 4) * 1.5f,
                    center = Offset(x, y),
                )
            }
        }

        // D. THUNDERSTORM LIGHTNING: Synchronized Cloud Glow + Forked Lightning Bolt
        if (conditionKind == WeatherConditionKind.THUNDERSTORM && lightningFlash > 0f) {
            val boltOrigin = Offset(w * 0.60f, h * 0.09f)
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

            reusableBoltPath.rewind()
            reusableBoltPath.moveTo(boltOrigin.x, boltOrigin.y)
            reusableBoltPath.lineTo(boltOrigin.x - w * 0.06f, h * 0.19f)
            reusableBoltPath.lineTo(boltOrigin.x + w * 0.03f, h * 0.24f)
            reusableBoltPath.lineTo(boltOrigin.x - w * 0.09f, h * 0.39f)
            reusableBoltPath.lineTo(boltOrigin.x - w * 0.02f, h * 0.44f)
            reusableBoltPath.lineTo(boltOrigin.x - w * 0.11f, h * 0.60f)

            reusableForkPath.rewind()
            reusableForkPath.moveTo(boltOrigin.x + w * 0.03f, h * 0.24f)
            reusableForkPath.lineTo(boltOrigin.x + w * 0.12f, h * 0.34f)
            reusableForkPath.lineTo(boltOrigin.x + w * 0.08f, h * 0.45f)

            drawPath(
                path = reusableBoltPath,
                color = Color(0xFF7DD3FC).copy(alpha = lightningFlash * 0.55f),
                style = Stroke(width = 11f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                path = reusableBoltPath,
                color = Color.White.copy(alpha = lightningFlash),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                path = reusableForkPath,
                color = Color(0xFFBAE6FD).copy(alpha = lightningFlash * 0.85f),
                style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }

        // E. RAIN & THUNDERSTORM: Multi-Depth Wind-Slanted Rain Streaks (zero per-drop Brush allocation!)
        val isRaining = conditionKind == WeatherConditionKind.RAIN ||
            conditionKind == WeatherConditionKind.THUNDERSTORM
        if (isRaining) {
            val intensity = when {
                conditionKind == WeatherConditionKind.THUNDERSTORM -> 1.45f
                precipChancePercent >= 70 -> 1.25f
                else -> 0.95f
            } * (1f + tapImpulse * 0.28f)
            val dropCount = (60 * intensity).toInt().coerceIn(48, 96)
            val windSlantPx = ((windSpeedKmh * 1.75f) + tapImpulse * 20f).coerceIn(10f, 68f)
            val rainColor = Color(0xFFE0F2FE)

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
                    0 -> 0.22f
                    1 -> 0.42f
                    else -> 0.68f
                }
                val strokeWidth = when (depth) {
                    0 -> 1.2f
                    1 -> 1.8f
                    else -> 2.5f
                }

                // Soft tail + crisp leading tip without creating 90+ LinearGradient objects per frame
                drawLine(
                    color = rainColor.copy(alpha = dropAlpha * 0.42f),
                    start = Offset(startX, startY),
                    end = Offset((startX + endX) * 0.5f, (startY + endY) * 0.5f),
                    strokeWidth = strokeWidth * 0.75f,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = rainColor.copy(alpha = dropAlpha),
                    start = Offset((startX + endX) * 0.5f, (startY + endY) * 0.5f),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round,
                )
            }

            // Raindrop rebound mist sparkles along lower atmospheric shelf
            for (s in 0..16) {
                val sx = ((s * 0.731f + cloudDrift * 0.6f) % 1f) * w
                val sy = h * (0.64f + (s % 5) * 0.045f)
                val sparkleProgress = (rainPhase * 1.5f + s * 0.19f) % 1f
                val sparkleAlpha = sin(sparkleProgress * PI.toFloat()).coerceIn(0f, 1f) * (0.34f + tapImpulse * 0.25f)
                drawCircle(
                    color = rainColor.copy(alpha = sparkleAlpha.coerceAtMost(0.85f)),
                    radius = 1.9f + (s % 2) * 1.1f,
                    center = Offset(sx, sy),
                )
            }
        }
    }
}

private enum class CloudSpriteKind {
    CIRRUS,
    CUMULUS_FRONT,
    CUMULUS_MID,
    CUMULUS_BROAD,
}

private data class CloudLayerSpec(
    val spriteKind: CloudSpriteKind,
    val initialPhaseX: Float,
    val yRatio: Float,
    val widthRatio: Float,
    val speedFactor: Float,
    val parallaxScale: Float,
    val alpha: Float,
    val bobPhase: Float,
)

private data class CloudColorPalette(
    val topHighlight: Color,
    val midTone: Color,
    val baseShade: Color,
    val cirrusTint: Color,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CloudColorPalette) return false
        return topHighlight.toArgb() == other.topHighlight.toArgb() &&
            midTone.toArgb() == other.midTone.toArgb() &&
            baseShade.toArgb() == other.baseShade.toArgb() &&
            cirrusTint.toArgb() == other.cirrusTint.toArgb()
    }

    override fun hashCode(): Int {
        var result = topHighlight.toArgb()
        result = 31 * result + midTone.toArgb()
        result = 31 * result + baseShade.toArgb()
        result = 31 * result + cirrusTint.toArgb()
        return result
    }
}

private class CloudSpriteCache(
    val cumulusFront: ImageBitmap,
    val cumulusMid: ImageBitmap,
    val cumulusBroad: ImageBitmap,
    val cirrusWisp: ImageBitmap,
)

private fun resolveRealisticCloudPalette(
    conditionKind: WeatherConditionKind,
    phase: SolarThemePhase,
): CloudColorPalette {
    return when {
        phase == SolarThemePhase.NIGHT -> when (conditionKind) {
            WeatherConditionKind.THUNDERSTORM -> CloudColorPalette(
                topHighlight = Color(0xFF475569).copy(alpha = 0.72f),
                midTone = Color(0xFF1E293B).copy(alpha = 0.80f),
                baseShade = Color(0xFF090F1C).copy(alpha = 0.88f),
                cirrusTint = Color(0xFF64748B).copy(alpha = 0.25f),
            )
            WeatherConditionKind.RAIN -> CloudColorPalette(
                topHighlight = Color(0xFF64748B).copy(alpha = 0.65f),
                midTone = Color(0xFF334155).copy(alpha = 0.75f),
                baseShade = Color(0xFF0F172A).copy(alpha = 0.84f),
                cirrusTint = Color(0xFF94A3B8).copy(alpha = 0.28f),
            )
            else -> CloudColorPalette(
                topHighlight = Color(0xFF94A3B8).copy(alpha = 0.56f),
                midTone = Color(0xFF475569).copy(alpha = 0.64f),
                baseShade = Color(0xFF1E293B).copy(alpha = 0.74f),
                cirrusTint = Color(0xFFCBD5E1).copy(alpha = 0.34f),
            )
        }
        phase == SolarThemePhase.SUNRISE_SUNSET || conditionKind == WeatherConditionKind.SUNSET -> CloudColorPalette(
            topHighlight = Color(0xFFFFFBEB).copy(alpha = 0.90f),
            midTone = Color(0xFFFDE68A).copy(alpha = 0.78f),
            baseShade = Color(0xFF927156).copy(alpha = 0.62f),
            cirrusTint = Color(0xFFFEF3C7).copy(alpha = 0.58f),
        )
        conditionKind == WeatherConditionKind.THUNDERSTORM -> CloudColorPalette(
            topHighlight = Color(0xFF94A3B8).copy(alpha = 0.78f),
            midTone = Color(0xFF475569).copy(alpha = 0.85f),
            baseShade = Color(0xFF1E293B).copy(alpha = 0.92f),
            cirrusTint = Color(0xFF94A3B8).copy(alpha = 0.32f),
        )
        conditionKind == WeatherConditionKind.RAIN -> CloudColorPalette(
            topHighlight = Color(0xFFCBD5E1).copy(alpha = 0.80f),
            midTone = Color(0xFF64748B).copy(alpha = 0.82f),
            baseShade = Color(0xFF334155).copy(alpha = 0.86f),
            cirrusTint = Color(0xFFCBD5E1).copy(alpha = 0.38f),
        )
        phase == SolarThemePhase.AFTERNOON_DUSK -> CloudColorPalette(
            topHighlight = Color(0xFFF0F7FF).copy(alpha = 0.88f),
            midTone = Color(0xFFBFD7F5).copy(alpha = 0.76f),
            baseShade = Color(0xFF6487B5).copy(alpha = 0.65f),
            cirrusTint = Color(0xFFE0EEFF).copy(alpha = 0.54f),
        )
        else -> CloudColorPalette(
            topHighlight = Color(0xFFFFFDF4).copy(alpha = 0.92f),
            midTone = Color(0xFFE6ECF5).copy(alpha = 0.80f),
            baseShade = Color(0xFF9FB0C4).copy(alpha = 0.62f),
            cirrusTint = Color(0xFFFFFCEB).copy(alpha = 0.56f),
        )
    }
}

private fun rememberCloudLayersForCondition(conditionKind: WeatherConditionKind): List<CloudLayerSpec> {
    return when (conditionKind) {
        WeatherConditionKind.SUNNY -> listOf(
            CloudLayerSpec(CloudSpriteKind.CIRRUS, 0.08f, 0.06f, 0.52f, 0.45f, 0.25f, 0.56f, 0.2f),
            CloudLayerSpec(CloudSpriteKind.CIRRUS, 0.60f, 0.14f, 0.44f, 0.55f, 0.30f, 0.44f, 1.4f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_MID, 0.14f, 0.16f, 0.46f, 0.85f, 0.45f, 0.72f, 0.7f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_FRONT, 0.68f, 0.24f, 0.42f, 1.05f, 0.65f, 0.64f, 2.1f),
        )
        WeatherConditionKind.PARTLY_CLOUDY -> listOf(
            CloudLayerSpec(CloudSpriteKind.CIRRUS, 0.05f, 0.05f, 0.56f, 0.42f, 0.22f, 0.60f, 0.1f),
            CloudLayerSpec(CloudSpriteKind.CIRRUS, 0.54f, 0.12f, 0.48f, 0.50f, 0.28f, 0.48f, 1.1f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_BROAD, 0.10f, 0.12f, 0.66f, 0.78f, 0.42f, 0.86f, 0.6f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_FRONT, 0.48f, 0.20f, 0.58f, 1.02f, 0.65f, 0.90f, 1.8f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_MID, 0.82f, 0.30f, 0.52f, 1.24f, 0.82f, 0.78f, 2.9f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_FRONT, 0.28f, 0.36f, 0.46f, 1.35f, 0.95f, 0.66f, 3.6f),
        )
        WeatherConditionKind.CLOUDY -> listOf(
            CloudLayerSpec(CloudSpriteKind.CIRRUS, 0.04f, 0.03f, 0.62f, 0.40f, 0.20f, 0.55f, 0.2f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_BROAD, 0.02f, 0.06f, 0.76f, 0.68f, 0.35f, 0.90f, 0.5f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_MID, 0.42f, 0.11f, 0.72f, 0.88f, 0.52f, 0.92f, 1.3f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_FRONT, 0.76f, 0.18f, 0.68f, 1.08f, 0.68f, 0.94f, 2.2f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_BROAD, 0.22f, 0.25f, 0.70f, 1.22f, 0.82f, 0.86f, 3.1f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_MID, 0.62f, 0.33f, 0.60f, 1.38f, 0.96f, 0.74f, 4.0f),
        )
        WeatherConditionKind.RAIN, WeatherConditionKind.THUNDERSTORM -> listOf(
            CloudLayerSpec(CloudSpriteKind.CUMULUS_BROAD, 0.00f, 0.02f, 0.82f, 0.95f, 0.35f, 0.94f, 0.3f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_FRONT, 0.38f, 0.07f, 0.78f, 1.20f, 0.55f, 0.96f, 1.2f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_MID, 0.74f, 0.12f, 0.74f, 1.42f, 0.75f, 0.92f, 2.1f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_BROAD, 0.18f, 0.18f, 0.76f, 1.56f, 0.88f, 0.88f, 3.0f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_FRONT, 0.56f, 0.24f, 0.68f, 1.72f, 1.00f, 0.80f, 3.8f),
        )
        WeatherConditionKind.SUNSET -> listOf(
            CloudLayerSpec(CloudSpriteKind.CIRRUS, 0.10f, 0.07f, 0.58f, 0.45f, 0.25f, 0.68f, 0.4f),
            CloudLayerSpec(CloudSpriteKind.CIRRUS, 0.58f, 0.15f, 0.50f, 0.58f, 0.32f, 0.56f, 1.7f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_MID, 0.16f, 0.18f, 0.54f, 0.82f, 0.50f, 0.82f, 0.9f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_BROAD, 0.62f, 0.26f, 0.60f, 1.04f, 0.72f, 0.78f, 2.3f),
            CloudLayerSpec(CloudSpriteKind.CUMULUS_FRONT, 0.88f, 0.34f, 0.46f, 1.18f, 0.88f, 0.66f, 3.2f),
        )
        WeatherConditionKind.FOG -> emptyList()
    }
}

private data class PuffNode(
    val xNorm: Float,
    val yNorm: Float,
    val rNorm: Float,
    val layer: Int, // 0 = shaded flat base, 1 = mid body, 2 = sunlit upper crest
)

/**
 * Renders a realistic, asymmetric, multi-layered cumulus / stratocumulus cloud sprite
 * with a flat shaded convective base, turbulent mid-body domes, and luminous upper-rim highlights
 * using 28 overlapping radial-falloff puffs (zero cartoon outlines, zero hard clipping edges).
 */
private fun createOffscreenImageBitmap(width: Int, height: Int): ImageBitmap {
    return Bitmap.createBitmap(
        width.coerceAtLeast(1),
        height.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888,
    ).apply {
        setHasAlpha(true)
    }.asImageBitmap()
}

private fun bakeRealisticCumulusSprite(
    width: Int,
    height: Int,
    variant: Int,
    topHighlight: Color,
    midTone: Color,
    baseShade: Color,
): ImageBitmap {
    val bitmap = createOffscreenImageBitmap(width, height)
    val canvas = ComposeCanvas(bitmap)
    val scope = CanvasDrawScope()

    val puffs = when (variant) {
        0 -> listOf(
            // Shaded flat convective base shelf
            PuffNode(0.18f, 0.67f, 0.16f, 0),
            PuffNode(0.30f, 0.68f, 0.19f, 0),
            PuffNode(0.43f, 0.69f, 0.21f, 0),
            PuffNode(0.57f, 0.68f, 0.20f, 0),
            PuffNode(0.71f, 0.68f, 0.18f, 0),
            PuffNode(0.83f, 0.66f, 0.15f, 0),
            // Turbulent mid-body cloud mass
            PuffNode(0.22f, 0.56f, 0.18f, 1),
            PuffNode(0.35f, 0.50f, 0.22f, 1),
            PuffNode(0.48f, 0.46f, 0.24f, 1),
            PuffNode(0.61f, 0.51f, 0.21f, 1),
            PuffNode(0.75f, 0.56f, 0.18f, 1),
            PuffNode(0.29f, 0.43f, 0.16f, 1),
            PuffNode(0.55f, 0.39f, 0.19f, 1),
            PuffNode(0.68f, 0.44f, 0.16f, 1),
            // Sunlit upper-crest cauliflower turrets
            PuffNode(0.44f, 0.31f, 0.19f, 2),
            PuffNode(0.52f, 0.28f, 0.18f, 2),
            PuffNode(0.37f, 0.36f, 0.16f, 2),
            PuffNode(0.62f, 0.36f, 0.16f, 2),
            PuffNode(0.26f, 0.47f, 0.14f, 2),
            PuffNode(0.74f, 0.48f, 0.14f, 2),
            PuffNode(0.48f, 0.23f, 0.13f, 2),
            PuffNode(0.58f, 0.31f, 0.13f, 2),
            // Soft outer wisps
            PuffNode(0.14f, 0.60f, 0.12f, 1),
            PuffNode(0.86f, 0.58f, 0.12f, 1),
            PuffNode(0.41f, 0.48f, 0.17f, 2),
            PuffNode(0.56f, 0.48f, 0.16f, 2),
        )
        1 -> listOf(
            // Asymmetric right-leaning stratocumulus bank
            PuffNode(0.16f, 0.66f, 0.14f, 0),
            PuffNode(0.28f, 0.68f, 0.18f, 0),
            PuffNode(0.42f, 0.68f, 0.20f, 0),
            PuffNode(0.56f, 0.67f, 0.21f, 0),
            PuffNode(0.70f, 0.66f, 0.19f, 0),
            PuffNode(0.83f, 0.65f, 0.15f, 0),
            PuffNode(0.23f, 0.55f, 0.16f, 1),
            PuffNode(0.36f, 0.53f, 0.19f, 1),
            PuffNode(0.50f, 0.48f, 0.22f, 1),
            PuffNode(0.64f, 0.44f, 0.23f, 1),
            PuffNode(0.76f, 0.52f, 0.18f, 1),
            PuffNode(0.58f, 0.36f, 0.18f, 1),
            PuffNode(0.43f, 0.42f, 0.17f, 1),
            PuffNode(0.63f, 0.29f, 0.18f, 2),
            PuffNode(0.70f, 0.34f, 0.16f, 2),
            PuffNode(0.54f, 0.33f, 0.16f, 2),
            PuffNode(0.44f, 0.38f, 0.15f, 2),
            PuffNode(0.32f, 0.47f, 0.14f, 2),
            PuffNode(0.78f, 0.45f, 0.13f, 2),
            PuffNode(0.66f, 0.24f, 0.12f, 2),
            PuffNode(0.58f, 0.46f, 0.16f, 2),
            PuffNode(0.20f, 0.58f, 0.12f, 1),
            PuffNode(0.87f, 0.57f, 0.11f, 1),
        )
        else -> listOf(
            // Broad layered stratus-cumulus deck
            PuffNode(0.14f, 0.64f, 0.14f, 0),
            PuffNode(0.26f, 0.66f, 0.18f, 0),
            PuffNode(0.39f, 0.66f, 0.20f, 0),
            PuffNode(0.52f, 0.66f, 0.21f, 0),
            PuffNode(0.66f, 0.66f, 0.20f, 0),
            PuffNode(0.79f, 0.65f, 0.18f, 0),
            PuffNode(0.88f, 0.63f, 0.13f, 0),
            PuffNode(0.20f, 0.54f, 0.16f, 1),
            PuffNode(0.33f, 0.48f, 0.20f, 1),
            PuffNode(0.46f, 0.49f, 0.21f, 1),
            PuffNode(0.60f, 0.47f, 0.21f, 1),
            PuffNode(0.73f, 0.51f, 0.19f, 1),
            PuffNode(0.84f, 0.55f, 0.14f, 1),
            PuffNode(0.32f, 0.37f, 0.16f, 2),
            PuffNode(0.42f, 0.35f, 0.17f, 2),
            PuffNode(0.53f, 0.38f, 0.16f, 2),
            PuffNode(0.64f, 0.36f, 0.17f, 2),
            PuffNode(0.74f, 0.42f, 0.15f, 2),
            PuffNode(0.24f, 0.45f, 0.14f, 2),
            PuffNode(0.37f, 0.31f, 0.12f, 2),
            PuffNode(0.66f, 0.31f, 0.12f, 2),
            PuffNode(0.49f, 0.46f, 0.15f, 2),
        )
    }

    val canvasSize = androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())
    scope.draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = canvasSize,
    ) {
        val w = size.width
        val h = size.height
        val minDim = size.minDimension

        // Draw back-to-front by layer (base shade -> mid body -> sunlit upper highlights)
        for (layerIdx in 0..2) {
            val baseColor = when (layerIdx) {
                0 -> baseShade
                1 -> midTone
                else -> topHighlight
            }
            puffs.filter { it.layer == layerIdx }.forEach { puff ->
                val cx = (puff.xNorm * w).coerceIn(w * 0.12f, w * 0.88f)
                val cy = (puff.yNorm * h).coerceIn(h * 0.18f, h * 0.80f)
                // Clamp radius strictly inside sprite bounds so zero rectangular edge clipping ever occurs!
                val maxR = minOf(cx, w - cx, cy, h - cy)
                val r = (puff.rNorm * minDim * 1.48f).coerceAtMost(maxR).coerceAtLeast(4f)
                val c = Offset(cx, cy)
                drawCircle(
                    brush = Brush.radialGradient(
                        0.0f to baseColor,
                        0.38f to baseColor.copy(alpha = baseColor.alpha * 0.72f),
                        0.74f to baseColor.copy(alpha = baseColor.alpha * 0.24f),
                        1.0f to Color.Transparent,
                        center = c,
                        radius = r,
                    ),
                    center = c,
                    radius = r,
                )
            }
        }
    }
    return bitmap
}

/**
 * Renders delicate high-altitude cirrus vapor wisps into a reusable [ImageBitmap].
 */
private fun bakeCirrusWispSprite(
    width: Int,
    height: Int,
    wispColor: Color,
): ImageBitmap {
    val bitmap = createOffscreenImageBitmap(width, height)
    val canvas = ComposeCanvas(bitmap)
    val scope = CanvasDrawScope()

    val canvasSize = androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat())
    scope.draw(
        density = Density(1f),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = canvasSize,
    ) {
        val w = size.width
        val h = size.height
        for (i in 0..18) {
            val t = i / 18f
            val envelope = sin(t * PI).toFloat().coerceIn(0f, 1f)
            val cx = w * (0.14f + t * 0.72f)
            val cy = h * (0.54f - sin(t * PI * 0.85).toFloat() * 0.14f + (i % 2) * 0.05f)
            val maxR = minOf(cx, w - cx, cy, h - cy)
            val r = (h * (0.18f + envelope * 0.22f)).coerceAtMost(maxR).coerceAtLeast(3f)
            val alpha = wispColor.alpha * (0.35f + 0.65f * envelope)
            val center = Offset(cx, cy)
            drawCircle(
                brush = Brush.radialGradient(
                    0.0f to wispColor.copy(alpha = alpha),
                    0.45f to wispColor.copy(alpha = alpha * 0.45f),
                    1.0f to Color.Transparent,
                    center = center,
                    radius = r,
                ),
                center = center,
                radius = r,
            )
        }
    }
    return bitmap
}

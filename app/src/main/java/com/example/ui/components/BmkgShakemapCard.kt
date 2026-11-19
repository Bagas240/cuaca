package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.location.LocationTracker
import com.example.data.model.SeismicEventItem
import com.example.data.model.SeismicSeverity
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.PastelAccent
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelDangerText
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.PastelSunset
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelTextMain
import com.example.ui.theme.PastelTextSub
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.WarningAmber
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun BmkgShakemapCard(
    shakemapUrl: String?,
    epicenterLat: Double,
    epicenterLon: Double,
    magnitudeLabel: String,
    locationLabel: String,
    recentEvents: List<SeismicEventItem>,
    lastUpdatedLabel: String,
    isSyncing: Boolean,
    dataSourceLabel: String,
    onRefreshNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageLoader = rememberMapImageLoader()
    var showInteractive2DMap by remember(shakemapUrl) {
        mutableStateOf(shakemapUrl.isNullOrBlank())
    }
    var zoomLevel by remember { mutableIntStateOf(5) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedEventIndex by remember(recentEvents) { mutableIntStateOf(0) }
    var manualBustNonce by remember { mutableIntStateOf(0) }

    val activeEvent = recentEvents.getOrNull(selectedEventIndex)
    val activeLat = activeEvent?.latitude ?: epicenterLat
    val activeLon = activeEvent?.longitude ?: epicenterLon
    val activeMagLabel = activeEvent?.let { "M ${String.format(Locale.US, "%.1f", it.magnitude)}" } ?: magnitudeLabel
    val activeLocation = activeEvent?.location ?: locationLabel
    val activeShakemapUrl = activeEvent?.shakemapUrl ?: shakemapUrl

    LaunchedEffect(activeLat, activeLon) {
        panOffset = Offset.Zero
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Toggle between Official BMKG Shakemap JPG & Interactive 2D Earthquake Radar Map
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val shakemapSelected = !showInteractive2DMap && !activeShakemapUrl.isNullOrBlank()
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !activeShakemapUrl.isNullOrBlank()) { showInteractive2DMap = false }
                    .testTag("bmkg_shakemap_tab"),
                shape = RoundedCornerShape(12.dp),
                color = if (shakemapSelected) DangerCrimson else PastelSurface,
                border = BorderStroke(1.dp, if (shakemapSelected) DangerCrimson else PastelBorder),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Public,
                        contentDescription = null,
                        tint = if (shakemapSelected) Color.White else PastelTextSub,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BMKG Shakemap",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (shakemapSelected) Color.White else PastelTextSub,
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showInteractive2DMap = true }
                    .testTag("bmkg_2d_map_tab"),
                shape = RoundedCornerShape(12.dp),
                color = if (showInteractive2DMap) PastelPrimary else PastelSurface,
                border = BorderStroke(1.dp, if (showInteractive2DMap) PastelPrimary else PastelBorder),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Map,
                        contentDescription = null,
                        tint = if (showInteractive2DMap) Color.White else PastelTextMain,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "2D Radar Map",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (showInteractive2DMap) Color.White else PastelTextMain,
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(enabled = !isSyncing) {
                        manualBustNonce += 1
                        onRefreshNow()
                    }
                    .testTag("bmkg_refresh_button"),
                shape = CircleShape,
                color = PastelSurface,
                border = BorderStroke(1.dp, PastelBorder),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color = PastelPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh BMKG Seismic Feed",
                            tint = PastelPrimary,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }
        }

        // Map Viewport Container
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .testTag("bmkg_earthquake_map_container"),
            shape = RoundedCornerShape(22.dp),
            color = PastelSurface,
            border = BorderStroke(1.dp, PastelBorder),
            shadowElevation = 2.dp,
        ) {
            if (!showInteractive2DMap && !activeShakemapUrl.isNullOrBlank()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    val cacheBustedUrl = remember(activeShakemapUrl, manualBustNonce) {
                        if (manualBustNonce == 0) activeShakemapUrl else "$activeShakemapUrl?t=$manualBustNonce"
                    }
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(cacheBustedUrl)
                            .memoryCachePolicy(CachePolicy.WRITE_ONLY)
                            .diskCachePolicy(CachePolicy.WRITE_ONLY)
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = "Official BMKG Shakemap for $activeLocation",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF8FAFC)),
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PastelPrimary)
                            }
                        },
                        error = {
                            Epicenter2DRadarMap(
                                centerLat = activeLat,
                                centerLon = activeLon,
                                activeLocation = activeLocation,
                                activeMagLabel = activeMagLabel,
                                activeDepthLabel = activeEvent?.let { "${it.depthKm} km" } ?: "10 km",
                                activeMmiLabel = activeEvent?.shindoLabel ?: "MMI",
                                recentEvents = recentEvents,
                                selectedEventIndex = selectedEventIndex,
                                zoomLevel = zoomLevel,
                                panOffset = panOffset,
                                onSelectEvent = { idx ->
                                    selectedEventIndex = idx
                                    panOffset = Offset.Zero
                                },
                                onPan = { delta ->
                                    panOffset = Offset(
                                        (panOffset.x + delta.x).coerceIn(-1200f, 1200f),
                                        (panOffset.y + delta.y).coerceIn(-1200f, 1200f),
                                    )
                                },
                                onZoomIn = { zoomLevel = (zoomLevel + 1).coerceAtMost(11) },
                                onZoomOut = { zoomLevel = (zoomLevel - 1).coerceAtLeast(4) },
                                onReset = {
                                    zoomLevel = 5
                                    panOffset = Offset.Zero
                                },
                            )
                        },
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = PastelSurface.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, PastelBorder),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(PastelAccent),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-Updating · $lastUpdatedLabel",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = PastelTextMain,
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = PastelSurface.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, PastelBorder),
                    ) {
                        Text(
                            text = "Source: $dataSourceLabel",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = PastelDangerText,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        )
                    }
                }
            } else {
                Epicenter2DRadarMap(
                    centerLat = activeLat,
                    centerLon = activeLon,
                    activeLocation = activeLocation,
                    activeMagLabel = activeMagLabel,
                    activeDepthLabel = activeEvent?.let { "${it.depthKm} km" } ?: "10 km",
                    activeMmiLabel = activeEvent?.shindoLabel ?: "MMI",
                    recentEvents = recentEvents,
                    selectedEventIndex = selectedEventIndex,
                    zoomLevel = zoomLevel,
                    panOffset = panOffset,
                    onSelectEvent = { idx ->
                        selectedEventIndex = idx
                        panOffset = Offset.Zero
                    },
                    onPan = { delta ->
                        panOffset = Offset(
                            (panOffset.x + delta.x).coerceIn(-1200f, 1200f),
                            (panOffset.y + delta.y).coerceIn(-1200f, 1200f),
                        )
                    },
                    onZoomIn = { zoomLevel = (zoomLevel + 1).coerceAtMost(11) },
                    onZoomOut = { zoomLevel = (zoomLevel - 1).coerceAtLeast(4) },
                    onReset = {
                        zoomLevel = 5
                        panOffset = Offset.Zero
                    },
                )
            }
        }
    }
}

@Composable
private fun Epicenter2DRadarMap(
    centerLat: Double,
    centerLon: Double,
    activeLocation: String,
    activeMagLabel: String,
    activeDepthLabel: String,
    activeMmiLabel: String,
    recentEvents: List<SeismicEventItem>,
    selectedEventIndex: Int,
    zoomLevel: Int,
    panOffset: Offset,
    onSelectEvent: (Int) -> Unit,
    onPan: (Offset) -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "seismic_2d_radar")
    val pWavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "p_wave_2d",
    )
    val sWavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2900, easing = LinearEasing), RepeatMode.Restart),
        label = "s_wave_2d",
    )
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "seismic_radar_sweep",
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(zoomLevel) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onPan(dragAmount)
                }
            }
            .testTag("earthquake_2d_radar_map"),
    ) {
        val viewW = constraints.maxWidth.toFloat()
        val viewH = constraints.maxHeight.toFloat()
        val (centerWX, centerWY) = SlippyMapMath.latLonToWorldPx(centerLat, centerLon, zoomLevel)
        val originX = centerWX - viewW / 2.0 - panOffset.x
        val originY = centerWY - viewH / 2.0 - panOffset.y

        // 1. 2D Geographic Base Map (Coastlines + Live Esri/OSM/CartoDB Tiles)
        Slippy2DMapBackground(
            centerLat = centerLat,
            centerLon = centerLon,
            zoom = zoomLevel,
            panOffsetPx = panOffset,
        )

        // 2. Animated Seismic Radar Sweep + P-Wave & S-Wave Concentric Pulses at Epicenter
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sx = (centerWX - originX).toFloat()
            val sy = (centerWY - originY).toFloat()
            val epi = Offset(sx, sy)
            val radarRadius = 112f

            // Subtle radar sweep cone around epicenter
            rotate(degrees = sweepAngle, pivot = epi) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to Color.Transparent,
                        0.80f to Color.Transparent,
                        0.96f to DangerCrimson.copy(alpha = 0.22f),
                        1.0f to DangerCrimson.copy(alpha = 0.36f),
                        center = epi,
                    ),
                    startAngle = -72f,
                    sweepAngle = 72f,
                    useCenter = true,
                    topLeft = Offset(epi.x - radarRadius, epi.y - radarRadius),
                    size = Size(radarRadius * 2f, radarRadius * 2f),
                )
            }

            // Static MMI felt boundary ring
            drawCircle(
                color = WarningAmber.copy(alpha = 0.12f),
                radius = radarRadius,
                center = epi,
            )
            drawCircle(
                color = WarningAmber.copy(alpha = 0.65f),
                radius = radarRadius,
                center = epi,
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)),
            )

            // Expanding S-Wave & P-Wave pulses
            drawCircle(
                color = WarningAmber.copy(alpha = (1f - sWavePhase) * 0.85f),
                radius = 14f + sWavePhase * radarRadius,
                center = epi,
                style = Stroke(width = 3f),
            )
            drawCircle(
                color = DangerCrimson.copy(alpha = (1f - pWavePhase) * 0.95f),
                radius = 10f + pWavePhase * (radarRadius * 0.8f),
                center = epi,
                style = Stroke(width = 3.2f),
            )
        }

        // 3. Clickable Earthquake Markers Across Indonesia / Region
        recentEvents.forEachIndexed { idx, ev ->
            val (wx, wy) = SlippyMapMath.latLonToWorldPx(ev.latitude, ev.longitude, zoomLevel)
            val sx = (wx - originX).roundToInt()
            val sy = (wy - originY).roundToInt()
            if (sx in -50..(viewW.toInt() + 50) && sy in -30..(viewH.toInt() + 30)) {
                val isSelected = idx == selectedEventIndex
                val pillColor = when (ev.severity) {
                    SeismicSeverity.HIGH -> DangerCrimson
                    SeismicSeverity.MODERATE -> WarningAmber
                    SeismicSeverity.LOW -> PastelPrimary
                }
                Surface(
                    modifier = Modifier
                        .offset { IntOffset(sx - 30, sy - 14) }
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectEvent(idx) }
                        .testTag("eq_map_pin_$idx"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) pillColor else PastelSurface.copy(alpha = 0.95f),
                    border = BorderStroke(if (isSelected) 2.dp else 1.5.dp, pillColor),
                    shadowElevation = if (isSelected) 6.dp else 2.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else pillColor),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "M${String.format(Locale.US, "%.1f", ev.magnitude)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = SpaceMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            ),
                            color = if (isSelected) Color.White else PastelTextMain,
                        )
                    }
                }
            }
        }

        // Top-left Pan Hint Pill
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = RoundedCornerShape(10.dp),
            color = PastelSurface.copy(alpha = 0.93f),
            border = BorderStroke(1.dp, PastelBorder),
        ) {
            Text(
                text = "Drag islands · Tap M pin",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                color = PastelTextMain,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            )
        }

        // Top-right Map Zoom & Recenter Controls
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(onClick = onZoomIn),
                shape = CircleShape,
                color = PastelSurface.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, PastelBorder),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Add, contentDescription = "Zoom In", tint = PastelTextMain, modifier = Modifier.size(15.dp))
                }
            }
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(onClick = onZoomOut),
                shape = CircleShape,
                color = PastelSurface.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, PastelBorder),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Remove, contentDescription = "Zoom Out", tint = PastelTextMain, modifier = Modifier.size(15.dp))
                }
            }
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(onClick = onReset),
                shape = CircleShape,
                color = PastelSurface.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, PastelBorder),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = "Recenter Epicenter", tint = PastelPrimary, modifier = Modifier.size(15.dp))
                }
            }
        }

        // Bottom Active Earthquake Inspector
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(14.dp),
            color = PastelSurface.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, DangerCrimson.copy(alpha = 0.4f)),
            shadowElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$activeMagLabel · $activeLocation",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PastelDangerText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Depth $activeDepthLabel · $activeMmiLabel · ${LocationTracker.formatCoordLabel(centerLat, centerLon)} · Source: BMKG",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = PastelTextSub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

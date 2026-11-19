package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.LocationTracker
import com.example.data.model.AqiMapLayer
import com.example.data.model.RegionalAqiSector
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PastelAccent
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelTextMain
import com.example.ui.theme.PastelTextSub
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.WarningAmber
import java.util.Locale
import kotlin.math.roundToInt

private fun normalizedAqiIntensity(sector: RegionalAqiSector, layer: AqiMapLayer): Float = when (layer) {
    AqiMapLayer.AQI -> (sector.aqi / 180f).coerceIn(0.08f, 1f)
    AqiMapLayer.PM25 -> (sector.pm25.toFloat() / 65f).coerceIn(0.08f, 1f)
    AqiMapLayer.PM10 -> (sector.pm10.toFloat() / 110f).coerceIn(0.08f, 1f)
    AqiMapLayer.NO2 -> (sector.no2Ppb.toFloat() / 75f).coerceIn(0.08f, 1f)
}

private fun aqiHeatmapColor(t: Float): Color {
    val clamped = t.coerceIn(0f, 1f)
    return when {
        clamped < 0.30f -> lerp(PastelAccent, Color(0xFF84CC16), clamped / 0.30f)
        clamped < 0.58f -> lerp(Color(0xFF84CC16), WarningAmber, (clamped - 0.30f) / 0.28f)
        clamped < 0.82f -> lerp(WarningAmber, DangerCrimson, (clamped - 0.58f) / 0.24f)
        else -> lerp(DangerCrimson, NeonPurple, (clamped - 0.82f) / 0.18f)
    }
}

private fun shortSectorCode(id: String): String = when (id.lowercase()) {
    "c" -> "CTR"
    "nw" -> "NW"
    "n" -> "N"
    "ne" -> "NE"
    "w" -> "W"
    "e" -> "E"
    "sw" -> "SW"
    "s" -> "S"
    "se" -> "SE"
    else -> id.uppercase().take(3)
}

@Composable
fun RegionalAqi2DMap(
    cityName: String,
    centerLat: Double,
    centerLon: Double,
    sectors: List<RegionalAqiSector>,
    activeLayer: AqiMapLayer,
    selectedSectorId: String,
    lastUpdatedLabel: String,
    isSyncing: Boolean,
    onSelectLayer: (AqiMapLayer) -> Unit,
    onSelectSector: (String) -> Unit,
    onRefreshNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var zoomLevel by remember { mutableIntStateOf(10) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(centerLat, centerLon) {
        panOffset = Offset.Zero
    }

    val selectedSector = sectors.firstOrNull { it.id == selectedSectorId }
        ?: sectors.firstOrNull { it.id == "c" }
        ?: sectors.firstOrNull()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Pollutant Layer Selector Pills + Manual Live Refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    AqiMapLayer.AQI to "US AQI",
                    AqiMapLayer.PM25 to "PM2.5",
                    AqiMapLayer.PM10 to "PM10",
                    AqiMapLayer.NO2 to "NO₂",
                ).forEach { (layer, label) ->
                    val isSelected = activeLayer == layer
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectLayer(layer) }
                            .testTag("aqi_layer_${layer.name.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PastelPrimary else PastelSurface,
                        border = BorderStroke(1.dp, if (isSelected) PastelPrimary else PastelBorder),
                        shadowElevation = if (isSelected) 3.dp else 1.dp,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.White else PastelTextMain,
                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier
                    .size(34.dp)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(enabled = !isSyncing, onClick = onRefreshNow)
                    .testTag("aqi_refresh_button"),
                shape = CircleShape,
                color = PastelSurface,
                border = BorderStroke(1.dp, PastelBorder),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = PastelPrimary,
                        )
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh AQI Map", tint = PastelPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Interactive 2D Geographic Map Viewport (Base Coastlines + Esri/OSM Tiles + Numbered AQI Heatmap)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFCBE2F6))
                .pointerInput(zoomLevel) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffset = Offset(
                            (panOffset.x + dragAmount.x).coerceIn(-1200f, 1200f),
                            (panOffset.y + dragAmount.y).coerceIn(-1200f, 1200f),
                        )
                    }
                }
                .testTag("air_quality_2d_map"),
        ) {
            val viewW = constraints.maxWidth.toFloat()
            val viewH = constraints.maxHeight.toFloat()
            val (centerWX, centerWY) = SlippyMapMath.latLonToWorldPx(centerLat, centerLon, zoomLevel)
            val originX = centerWX - viewW / 2.0 - panOffset.x
            val originY = centerWY - viewH / 2.0 - panOffset.y

            // 1. Real 2D Geographic Map (Mercator Archipelago Coastlines + Live Esri/OSM/CartoDB Tiles)
            Slippy2DMapBackground(
                centerLat = centerLat,
                centerLon = centerLon,
                zoom = zoomLevel,
                panOffsetPx = panOffset,
            )

            // 2. Smooth 2D AQI Heatmap Plumes projected at real lat/lon positions
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radiusScale = when {
                    zoomLevel >= 12 -> 175f
                    zoomLevel == 11 -> 135f
                    zoomLevel == 10 -> 102f
                    else -> 72f
                }
                sectors.forEach { sector ->
                    val (wx, wy) = SlippyMapMath.latLonToWorldPx(sector.latitude, sector.longitude, zoomLevel)
                    val sx = (wx - originX).toFloat()
                    val sy = (wy - originY).toFloat()
                    val intensity = normalizedAqiIntensity(sector, activeLayer)
                    val heatColor = aqiHeatmapColor(intensity)
                    val radius = radiusScale * (0.85f + intensity * 0.45f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                heatColor.copy(alpha = 0.52f),
                                heatColor.copy(alpha = 0.20f),
                                Color.Transparent,
                            ),
                            center = Offset(sx, sy),
                            radius = radius,
                        ),
                        center = Offset(sx, sy),
                        radius = radius,
                    )
                }
            }

            // 3. Numbered Interactive AQI Station Markers at real geographic positions
            sectors.forEach { sector ->
                val (wx, wy) = SlippyMapMath.latLonToWorldPx(sector.latitude, sector.longitude, zoomLevel)
                val sx = (wx - originX).roundToInt()
                val sy = (wy - originY).roundToInt()
                if (sx in -50..(viewW.toInt() + 50) && sy in -30..(viewH.toInt() + 30)) {
                    val intensity = normalizedAqiIntensity(sector, activeLayer)
                    val badgeColor = aqiHeatmapColor(intensity)
                    val isSelected = sector.id == selectedSector?.id
                    val valueText = when (activeLayer) {
                        AqiMapLayer.AQI -> "${sector.aqi}"
                        AqiMapLayer.PM25 -> String.format(Locale.US, "%.0f", sector.pm25)
                        AqiMapLayer.PM10 -> String.format(Locale.US, "%.0f", sector.pm10)
                        AqiMapLayer.NO2 -> String.format(Locale.US, "%.0f", sector.no2Ppb)
                    }
                    val metricPrefix = shortSectorCode(sector.id)

                    Surface(
                        modifier = Modifier
                            .offset { IntOffset(sx - 34, sy - 14) }
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectSector(sector.id) }
                            .testTag("aqi_map_pin_${sector.id}"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PastelTextMain else PastelSurface.copy(alpha = 0.95f),
                        border = BorderStroke(if (isSelected) 2.dp else 1.5.dp, badgeColor),
                        shadowElevation = if (isSelected) 6.dp else 3.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$metricPrefix $valueText",
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

            // Top-left Map + Live Status Label
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(12.dp),
                color = PastelSurface.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, PastelBorder),
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Map,
                        contentDescription = null,
                        tint = PastelPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$cityName AQI · $lastUpdatedLabel",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = PastelTextMain,
                    )
                }
            }

            // Top-right Zoom & Recenter Controls
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable { zoomLevel = (zoomLevel + 1).coerceAtMost(13) }
                        .testTag("aqi_map_zoom_in"),
                    shape = CircleShape,
                    color = PastelSurface.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, PastelBorder),
                    shadowElevation = 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Add, contentDescription = "Zoom In", tint = PastelTextMain, modifier = Modifier.size(16.dp))
                    }
                }
                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable { zoomLevel = (zoomLevel - 1).coerceAtLeast(5) }
                        .testTag("aqi_map_zoom_out"),
                    shape = CircleShape,
                    color = PastelSurface.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, PastelBorder),
                    shadowElevation = 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Zoom Out", tint = PastelTextMain, modifier = Modifier.size(16.dp))
                    }
                }
                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable {
                            zoomLevel = 10
                            panOffset = Offset.Zero
                            onSelectSector("c")
                        }
                        .testTag("aqi_map_reset_view"),
                    shape = CircleShape,
                    color = PastelSurface.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, PastelBorder),
                    shadowElevation = 2.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = "Recenter Map", tint = PastelPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Bottom Selected Station Inspector Bar
            if (selectedSector != null) {
                val metricValue = when (activeLayer) {
                    AqiMapLayer.AQI -> "AQI ${selectedSector.aqi}"
                    AqiMapLayer.PM25 -> "${String.format(Locale.US, "%.1f", selectedSector.pm25)} µg/m³"
                    AqiMapLayer.PM10 -> "${String.format(Locale.US, "%.1f", selectedSector.pm10)} µg/m³"
                    AqiMapLayer.NO2 -> "${String.format(Locale.US, "%.1f", selectedSector.no2Ppb)} ppb"
                }
                val accentColor = aqiHeatmapColor(normalizedAqiIntensity(selectedSector, activeLayer))

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(10.dp)
                        .testTag("aqi_sector_inspector"),
                    shape = RoundedCornerShape(14.dp),
                    color = PastelSurface.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                    shadowElevation = 3.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = selectedSector.name,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PastelTextMain,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${LocationTracker.formatCoordLabel(selectedSector.latitude, selectedSector.longitude)} · ${selectedSector.statusLabel}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = PastelTextSub,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = accentColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)),
                        ) {
                            Text(
                                text = metricValue,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = SpaceMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = PastelTextMain,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }

        // Quick Sector Selection Chips with numbers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            sectors.forEach { sector ->
                val isSelected = sector.id == selectedSector?.id
                val chipColor = aqiHeatmapColor(normalizedAqiIntensity(sector, activeLayer))
                val chipNum = when (activeLayer) {
                    AqiMapLayer.AQI -> "AQI ${sector.aqi}"
                    AqiMapLayer.PM25 -> "${String.format(Locale.US, "%.0f", sector.pm25)} µg"
                    AqiMapLayer.PM10 -> "${String.format(Locale.US, "%.0f", sector.pm10)} µg"
                    AqiMapLayer.NO2 -> "${String.format(Locale.US, "%.0f", sector.no2Ppb)} ppb"
                }
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectSector(sector.id) }
                        .testTag("aqi_sector_chip_${sector.id}"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) chipColor.copy(alpha = 0.16f) else PastelSurface,
                    border = BorderStroke(1.dp, if (isSelected) chipColor else PastelBorder),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(chipColor),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${sector.directionLabel} · $chipNum",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isSelected) PastelTextMain else PastelTextSub,
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.SatelliteAlt
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RadarLayer
import com.example.data.model.WeatherMapLayer
import com.example.ui.components.BmkgInteractiveWeatherMap
import com.example.ui.components.VeridiaBackHeader
import com.example.ui.components.VeridiaCard
import com.example.ui.components.formatTempString
import com.example.ui.components.formatWindValue
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.veridiaColors
import com.example.ui.viewmodel.VeridiaUiState
import kotlin.math.roundToInt

@Composable
fun WeatherRadarScreen(
    state: VeridiaUiState,
    gyroTilt: Offset,
    onBack: () -> Unit,
    onSelectRadarLayer: (RadarLayer) -> Unit,
    onPlaybackOffsetChange: (Int) -> Unit,
    onRefreshNow: () -> Unit,
) {
    val colors = veridiaColors
    val radar = state.radar
    val tempUnit = state.settings.temperatureUnit
    val (cellSpeedVal, speedUnitLabel) = formatWindValue(radar.cellSpeedKmh, state.settings.windUnit)
    val mappedWeatherLayer = when (state.selectedRadarLayer) {
        RadarLayer.RAIN_DOPPLER -> WeatherMapLayer.RAIN_RADAR
        RadarLayer.WIND_STREAM -> WeatherMapLayer.WIND_VECTOR
        RadarLayer.SATELLITE -> WeatherMapLayer.CLOUDS_TEMP
    }

    val playbackStatusLabel = when {
        state.radarPlaybackOffsetMinutes == 0 -> "Live (Now)"
        state.radarPlaybackOffsetMinutes < 0 -> "${state.radarPlaybackOffsetMinutes} min"
        else -> "+${state.radarPlaybackOffsetMinutes} min"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.sky),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                VeridiaBackHeader(
                    title = "Weather Radar",
                    subtitle = "Doppler Precipitation & Satellite Array",
                    onBackClick = onBack,
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border),
                            shadowElevation = 2.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live Sweep",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMain,
                                )
                            }
                        }
                    },
                )
            }

            // Dedicated Interactive Open-Source Map (osmdroid) with Real-Time BMKG Precipitation Overlay
            item {
                BmkgInteractiveWeatherMap(
                    centerLat = state.selectedCity.latitude,
                    centerLon = state.selectedCity.longitude,
                    stations = state.weatherMapStations,
                    rainViewerTilePrefix = state.rainViewerTilePrefix,
                    activeLayer = mappedWeatherLayer,
                    tempUnit = tempUnit,
                    lastUpdatedLabel = state.lastUpdatedLabel,
                    isSyncing = state.isSyncing,
                    onSelectLayer = { mapLayer ->
                        onSelectRadarLayer(
                            when (mapLayer) {
                                WeatherMapLayer.RAIN_RADAR -> RadarLayer.RAIN_DOPPLER
                                WeatherMapLayer.WIND_VECTOR -> RadarLayer.WIND_STREAM
                                WeatherMapLayer.CLOUDS_TEMP -> RadarLayer.SATELLITE
                            },
                        )
                    },
                    onRefreshNow = onRefreshNow,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Radar Filter & Layer Toggle Tabs
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border),
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        RadarLayerTab(
                            label = "Rain Doppler",
                            selected = state.selectedRadarLayer == RadarLayer.RAIN_DOPPLER,
                            onClick = { onSelectRadarLayer(RadarLayer.RAIN_DOPPLER) },
                            modifier = Modifier.weight(1f).testTag("radar_tab_rain"),
                        )
                        RadarLayerTab(
                            label = "Wind Stream",
                            selected = state.selectedRadarLayer == RadarLayer.WIND_STREAM,
                            onClick = { onSelectRadarLayer(RadarLayer.WIND_STREAM) },
                            modifier = Modifier.weight(1f).testTag("radar_tab_wind"),
                        )
                        RadarLayerTab(
                            label = "Satellite",
                            selected = state.selectedRadarLayer == RadarLayer.SATELLITE,
                            onClick = { onSelectRadarLayer(RadarLayer.SATELLITE) },
                            modifier = Modifier.weight(1f).testTag("radar_tab_satellite"),
                        )
                    }
                }
            }

            // Active Precipitation Cell Summary Card
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE PRECIPITATION CELL",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSub,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = radar.echoCoreDbz.toString(),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = colors.textMain,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "dBZ Rain Core",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.textSub,
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                            Text(
                                text = radar.summaryDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSub,
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = colors.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.25f)),
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(Icons.Rounded.WaterDrop, null, tint = colors.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(radar.intensityBadge, style = MaterialTheme.typography.labelSmall, color = colors.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadarSummaryStat(
                            label = "Cell Speed",
                            value = "$cellSpeedVal $speedUnitLabel",
                            valueColor = colors.textMain,
                            modifier = Modifier.weight(1f),
                        )
                        Box(modifier = Modifier.width(1.dp).height(26.dp).background(colors.border))
                        RadarSummaryStat(
                            label = "Echo Top",
                            value = String.format("%,d m", radar.echoTopMeters),
                            valueColor = colors.textMain,
                            modifier = Modifier.weight(1f),
                        )
                        Box(modifier = Modifier.width(1.dp).height(26.dp).background(colors.border))
                        RadarSummaryStat(
                            label = "Accumulation",
                            value = "${radar.accumulationMmH} mm/h",
                            valueColor = colors.primary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // 4 Consolidated Radar Metric Cards
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadarMetricCard(
                            title = "SURFACE RAIN",
                            value = String.format("%.1f", radar.surfaceRainMmH),
                            unit = "mm/h",
                            unitColor = colors.textSub,
                            subtitle = "Moderate steady rain",
                            footerLeft = "Next 60m",
                            footerRight = "${radar.next60mMm} mm",
                            footerRightColor = colors.textMain,
                            icon = Icons.Rounded.WaterDrop,
                            iconTint = colors.primary,
                            modifier = Modifier.weight(1f),
                        )
                        RadarMetricCard(
                            title = "CELL VECTOR",
                            value = radar.cellVectorLabel,
                            unit = "",
                            unitColor = colors.textSub,
                            subtitle = radar.cellDestination,
                            footerLeft = "Velocity",
                            footerRight = "$cellSpeedVal $speedUnitLabel",
                            footerRightColor = colors.textMain,
                            icon = Icons.Rounded.NearMe,
                            iconTint = colors.accent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        RadarMetricCard(
                            title = "REFLECTIVITY",
                            value = radar.echoCoreDbz.toString(),
                            unit = "dBZ",
                            unitColor = colors.sunset,
                            subtitle = "Doppler band echo",
                            footerLeft = "Hail Risk",
                            footerRight = radar.hailRiskLabel,
                            footerRightColor = colors.greenText,
                            icon = Icons.Rounded.Radar,
                            iconTint = colors.warning,
                            modifier = Modifier.weight(1f),
                        )
                        RadarMetricCard(
                            title = "IR CLOUD TOP",
                            value = "${radar.irCloudTopC}°",
                            unit = "C",
                            unitColor = colors.textSub,
                            subtitle = "Deep convective deck",
                            footerLeft = "Satellite",
                            footerRight = radar.satelliteName,
                            footerRightColor = colors.textMain,
                            icon = Icons.Rounded.SatelliteAlt,
                            iconTint = colors.primary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Timeline Scrubber Controller
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Schedule, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RADAR PLAYBACK TRACK",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSub,
                            )
                        }
                        Text(
                            text = playbackStatusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary,
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = state.radarPlaybackOffsetMinutes.toFloat(),
                        onValueChange = { onPlaybackOffsetChange(it.roundToInt()) },
                        valueRange = -60f..30f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary,
                            inactiveTrackColor = colors.border,
                        ),
                        modifier = Modifier.testTag("radar_playback_slider"),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("-60 min", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceMonoFamily), color = colors.textSub)
                        Text("-30 min", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceMonoFamily), color = colors.textSub)
                        Text(
                            "Now",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = SpaceMonoFamily,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colors.textMain,
                        )
                        Text("+30 min", style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceMonoFamily), color = colors.textSub)
                    }
                }
            }
        }
    }
}

@Composable
private fun RadarLayerTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = veridiaColors
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.primary else Color.Transparent,
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (selected) colors.onPrimary else colors.textSub,
            )
        }
    }
}

@Composable
private fun RadarSummaryStat(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = veridiaColors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
        )
    }
}

@Composable
private fun RadarMetricCard(
    title: String,
    value: String,
    unit: String,
    unitColor: Color,
    subtitle: String,
    footerLeft: String,
    footerRight: String,
    footerRightColor: Color,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    val colors = veridiaColors
    VeridiaCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.displayMedium, color = colors.textMain)
            if (unit.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = unitColor,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
        }
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSub)
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = colors.border)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(footerLeft, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
            Text(footerRight, style = MaterialTheme.typography.labelSmall, color = footerRightColor)
        }
    }
}

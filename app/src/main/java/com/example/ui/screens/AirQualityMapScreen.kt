package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.Co2
import androidx.compose.material.icons.rounded.Domain
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Masks
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SensorWindow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AqiMapLayer
import com.example.data.model.AqiSeverity
import com.example.ui.components.RegionalAqi2DMap
import com.example.ui.components.VeridiaBackHeader
import com.example.ui.components.VeridiaCard
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PastelAccent
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelSubtle
import com.example.ui.theme.PastelSunset
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelTextMain
import com.example.ui.theme.PastelTextSub
import com.example.ui.theme.SecondaryGreenDark
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.VeridiaUiState
import java.util.Locale

@Composable
fun AirQualityMapScreen(
    state: VeridiaUiState,
    gyroTilt: Offset,
    onSelectAqiLayer: (AqiMapLayer) -> Unit,
    onSelectAqiSector: (String) -> Unit,
    onRefreshNow: () -> Unit,
    onBack: () -> Unit,
) {
    val aqi = state.airQuality

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelSky),
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
                    title = "Air Quality & Smog",
                    subtitle = "${state.selectedCity.name} · Aerosol Sensor Array",
                    onBackClick = onBack,
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PastelSurface,
                            border = BorderStroke(1.dp, PastelBorder),
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
                                        .background(PastelAccent),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${aqi.activeSensorsCount} Sensors Live",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelTextMain,
                                )
                            }
                        }
                    },
                )
            }

            // Interactive 2D Regional AQI Geographic Map
            item {
                RegionalAqi2DMap(
                    cityName = state.selectedCity.name,
                    centerLat = state.selectedCity.latitude,
                    centerLon = state.selectedCity.longitude,
                    sectors = aqi.regionalGrid,
                    activeLayer = state.selectedAqiLayer,
                    selectedSectorId = state.selectedAqiSectorId,
                    lastUpdatedLabel = state.lastUpdatedLabel,
                    isSyncing = state.isSyncing,
                    onSelectLayer = onSelectAqiLayer,
                    onSelectSector = onSelectAqiSector,
                    onRefreshNow = onRefreshNow,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Regional AQI Hero Card
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
                                text = "REGIONAL AIR QUALITY INDEX",
                                style = MaterialTheme.typography.labelSmall,
                                color = PastelTextSub,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = aqi.regionalAqi.toString(),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = PastelSunset,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = aqi.regionalCategory,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PastelSunset,
                                    modifier = Modifier.padding(bottom = 10.dp),
                                )
                            }
                            Text(
                                text = aqi.summaryDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextSub,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Public, null, tint = PastelPrimary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Source: ${aqi.dataSourceLabel} · Esri / OSM Map",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = PastelPrimary,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = WarningAmber.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.35f)),
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(Icons.Rounded.Masks, null, tint = PastelSunset, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("PM2.5", style = MaterialTheme.typography.labelSmall, color = PastelSunset)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Multi-Stop Spectrum Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                    ) {
                        Box(modifier = Modifier.weight(1f).height(6.dp).background(PastelAccent))
                        Box(modifier = Modifier.weight(1f).height(6.dp).background(WarningAmber))
                        Box(modifier = Modifier.weight(1f).height(6.dp).background(DangerCrimson))
                        Box(modifier = Modifier.weight(1f).height(6.dp).background(NeonPurple))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("0 Good", style = MaterialTheme.typography.labelSmall, color = SecondaryGreenDark)
                        Text("50 Moderate", style = MaterialTheme.typography.labelSmall, color = PastelSunset)
                        Text("150 Poor", style = MaterialTheme.typography.labelSmall, color = DangerCrimson)
                        Text("300+ Alert", style = MaterialTheme.typography.labelSmall, color = NeonPurple)
                    }
                }
            }

            // 4-Gas Pollutant Metric Breakdown
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
                        PollutantMetricCard(
                            title = "PM2.5 PARTICLES",
                            value = String.format(Locale.US, "%.1f", aqi.pm25),
                            unit = "µg/m³",
                            subtitle = "2.4x WHO Threshold",
                            subtitleColor = PastelSunset,
                            icon = Icons.Rounded.Grain,
                            iconTint = WarningAmber,
                            progress = (aqi.pm25.toFloat() / 60f).coerceIn(0.1f, 1f),
                            modifier = Modifier.weight(1f),
                        )
                        PollutantMetricCard(
                            title = "PM10 COARSE",
                            value = String.format(Locale.US, "%.1f", aqi.pm10),
                            unit = "µg/m³",
                            subtitle = "Within Safety Band",
                            subtitleColor = SecondaryGreenDark,
                            icon = Icons.Rounded.BlurOn,
                            iconTint = PastelAccent,
                            progress = (aqi.pm10.toFloat() / 110f).coerceIn(0.1f, 1f),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PollutantMetricCard(
                            title = "CARBON MONOXIDE",
                            value = aqi.coPpb.toString(),
                            unit = "ppb",
                            subtitle = "Normal Urban Base",
                            subtitleColor = PastelTextSub,
                            icon = Icons.Rounded.Co2,
                            iconTint = PastelPrimary,
                            progress = (aqi.coPpb.toFloat() / 1200f).coerceIn(0.1f, 1f),
                            modifier = Modifier.weight(1f),
                        )
                        PollutantMetricCard(
                            title = "NITROGEN DIOXIDE",
                            value = String.format(Locale.US, "%.1f", aqi.no2Ppb),
                            unit = "ppb",
                            subtitle = "Traffic Corridor",
                            subtitleColor = DangerCrimson,
                            icon = Icons.Rounded.Factory,
                            iconTint = DangerCrimson,
                            progress = (aqi.no2Ppb.toFloat() / 75f).coerceIn(0.1f, 1f),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Health & Activity Protocols
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MedicalServices, null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Health & Activity Protocols",
                            style = MaterialTheme.typography.headlineSmall,
                            color = PastelTextMain,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HealthProtocolTile(
                            icon = Icons.Rounded.Masks,
                            iconTint = WarningAmber,
                            title = "Wear Mask",
                            subtitle = "N95 for sensitive lungs",
                            modifier = Modifier.weight(1f),
                        )
                        HealthProtocolTile(
                            icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                            iconTint = DangerCrimson,
                            title = "Limit Cardio",
                            subtitle = "Reduce outdoor training",
                            modifier = Modifier.weight(1f),
                        )
                        HealthProtocolTile(
                            icon = Icons.Rounded.SensorWindow,
                            iconTint = PastelPrimary,
                            title = "Close Vents",
                            subtitle = "Engage HEPA purifiers",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // District Comparison Matrix
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
                            Icon(Icons.Rounded.Domain, null, tint = PastelPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "District Comparison",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PastelTextMain,
                            )
                        }
                        Text(
                            text = "${aqi.districts.size} Key Sectors",
                            style = MaterialTheme.typography.labelSmall,
                            color = PastelTextSub,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PastelBorder)

                    aqi.districts.forEachIndexed { idx, district ->
                        val (dotColor, valueColor) = when (district.severity) {
                            AqiSeverity.EXCELLENT, AqiSeverity.GOOD -> PastelAccent to SecondaryGreenDark
                            AqiSeverity.MODERATE -> WarningAmber to PastelSunset
                            AqiSeverity.UNHEALTHY -> DangerCrimson to DangerCrimson
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(dotColor),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = district.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PastelTextMain,
                                    )
                                    Text(
                                        text = district.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PastelTextSub,
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "AQI ${district.aqi}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = valueColor,
                                )
                                Text(
                                    text = district.statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = valueColor,
                                )
                            }
                        }
                        if (idx < aqi.districts.lastIndex) {
                            HorizontalDivider(color = PastelBorder.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PollutantMetricCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    subtitleColor: Color,
    icon: ImageVector,
    iconTint: Color,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    VeridiaCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = PastelTextSub)
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.displayMedium, color = PastelTextMain)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = PastelTextSub,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = subtitleColor)
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape),
            color = iconTint,
            trackColor = PastelBorder,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun HealthProtocolTile(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = PastelSubtle,
        border = BorderStroke(1.dp, PastelBorder),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PastelTextMain,
                textAlign = TextAlign.Center,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 13.sp),
                color = PastelTextSub,
                textAlign = TextAlign.Center,
            )
        }
    }
}

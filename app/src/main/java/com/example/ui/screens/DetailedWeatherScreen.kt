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
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AtmosphereStratum
import com.example.data.model.WeatherConditionKind
import com.example.data.model.WeatherMapLayer
import com.example.ui.components.BmkgInteractiveWeatherMap
import com.example.ui.components.SolarArcCanvas
import com.example.ui.components.VeridiaBackHeader
import com.example.ui.components.VeridiaCard
import com.example.ui.components.conditionIcon
import com.example.ui.components.conditionTint
import com.example.ui.components.formatPressureValue
import com.example.ui.components.formatTempString
import com.example.ui.components.formatWindValue
import com.example.ui.theme.PastelAccent
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelSubtle
import com.example.ui.theme.PastelSurface
import com.example.ui.theme.PastelTextMain
import com.example.ui.theme.PastelTextSub
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.VeridiaUiState

@Composable
fun DetailedWeatherScreen(
    state: VeridiaUiState,
    onBack: () -> Unit,
    onSelectStratum: (AtmosphereStratum) -> Unit,
    onSelectWeatherMapLayer: (WeatherMapLayer) -> Unit,
    onRefreshNow: () -> Unit,
    onOpenRadar: () -> Unit,
) {
    val weather = state.weather
    val tempUnit = state.settings.temperatureUnit
    val (windVal, windUnitLabel) = formatWindValue(weather.windSpeedKmh, state.settings.windUnit)
    val (gustVal, _) = formatWindValue(weather.windGustKmh, state.settings.windUnit)
    val (pressureVal, pressureUnitLabel) = formatPressureValue(weather.pressureHpa, state.settings.pressureUnit)

    val altitudeBadge = when (state.selectedStratum) {
        AtmosphereStratum.SURFACE -> "Surface · 42m"
        AtmosphereStratum.MID_CLOUD -> "Mid-Cloud · 4,200m"
        AtmosphereStratum.UPPER_STRATUM -> "Stratosphere · 12,000m"
    }
    val strataRangeLabel = when (state.selectedStratum) {
        AtmosphereStratum.SURFACE -> "0 - 1,200m Ground Level"
        AtmosphereStratum.MID_CLOUD -> "1,200 - 8,000m Troposphere"
        AtmosphereStratum.UPPER_STRATUM -> "8,000 - 15,000m Stratum"
    }

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
                    title = "Weather & Atmosphere",
                    subtitle = "${state.selectedCity.name} · ${weather.dataSourceLabel} · ${state.lastUpdatedLabel}",
                    onBackClick = onBack,
                    trailingContent = {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenRadar)
                                .testTag("detailed_weather_radar_button"),
                            shape = RoundedCornerShape(12.dp),
                            color = PastelSurface,
                            border = BorderStroke(1.dp, PastelBorder),
                            shadowElevation = 2.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Radar,
                                    contentDescription = "Open Radar",
                                    tint = PastelPrimary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Radar",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = PastelTextMain,
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
                    activeLayer = state.selectedWeatherMapLayer,
                    tempUnit = tempUnit,
                    lastUpdatedLabel = state.lastUpdatedLabel,
                    isSyncing = state.isSyncing,
                    onSelectLayer = onSelectWeatherMapLayer,
                    onRefreshNow = onRefreshNow,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Atmospheric Strata Segmented Bar
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = PastelSurface,
                    border = BorderStroke(1.dp, PastelBorder),
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        StratumSegmentButton(
                            label = "Surface",
                            selected = state.selectedStratum == AtmosphereStratum.SURFACE,
                            onClick = { onSelectStratum(AtmosphereStratum.SURFACE) },
                            modifier = Modifier.weight(1f).testTag("stratum_btn_surface"),
                        )
                        StratumSegmentButton(
                            label = "Mid-Cloud",
                            selected = state.selectedStratum == AtmosphereStratum.MID_CLOUD,
                            onClick = { onSelectStratum(AtmosphereStratum.MID_CLOUD) },
                            modifier = Modifier.weight(1f).testTag("stratum_btn_mid"),
                        )
                        StratumSegmentButton(
                            label = "Upper Stratum",
                            selected = state.selectedStratum == AtmosphereStratum.UPPER_STRATUM,
                            onClick = { onSelectStratum(AtmosphereStratum.UPPER_STRATUM) },
                            modifier = Modifier.weight(1f).testTag("stratum_btn_high"),
                        )
                    }
                }
            }

            // 3D Atmospheric Column Visualizer Card
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
                            Icon(
                                imageVector = Icons.Rounded.Air,
                                contentDescription = null,
                                tint = PastelPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ATMOSPHERE STRATUM",
                                style = MaterialTheme.typography.labelSmall,
                                color = PastelTextSub,
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PastelSubtle,
                            border = BorderStroke(1.dp, PastelBorder),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Layers,
                                    contentDescription = null,
                                    tint = PastelPrimary,
                                    modifier = Modifier.size(12.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = altitudeBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelTextMain,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strataRangeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = PastelTextSub,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StratumLayerRow(
                            title = "Upper Stratosphere",
                            meta = "12,000m · ${formatTempString(-52, tempUnit, true)}",
                            dotColor = PastelPrimary,
                            isActive = state.selectedStratum == AtmosphereStratum.UPPER_STRATUM,
                        )
                        StratumLayerRow(
                            title = "Mid-Troposphere Rain Clouds",
                            meta = "4,200m · ${formatTempString(-8, tempUnit, true)}",
                            dotColor = PastelAccent,
                            isActive = state.selectedStratum == AtmosphereStratum.MID_CLOUD,
                        )
                        StratumLayerRow(
                            title = "Ground Boundary & Precipitation",
                            meta = "42m · ${formatTempString(weather.currentTempC, tempUnit, true)}",
                            dotColor = PastelPrimary,
                            isActive = state.selectedStratum == AtmosphereStratum.SURFACE,
                        )
                    }
                }
            }

            // 2x2 Detailed Telemetry Cards
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
                        // Wind Card
                        VeridiaCard(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("WIND", style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
                                Icon(Icons.Rounded.NearMe, null, tint = PastelPrimary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(windVal, style = MaterialTheme.typography.displayMedium, color = PastelTextMain)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = windUnitLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelTextSub,
                                    modifier = Modifier.padding(bottom = 6.dp),
                                )
                            }
                            Text(
                                text = "${weather.windDirectionLabel} · Bearing ${weather.windBearingDeg}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextSub,
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = PastelBorder)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Gusts", style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
                                Text(
                                    "$gustVal $windUnitLabel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelTextMain,
                                )
                            }
                        }

                        // UV Index Card
                        VeridiaCard(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("UV INDEX", style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
                                Icon(Icons.Rounded.WbSunny, null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%.1f", weather.uvIndex),
                                    style = MaterialTheme.typography.displayMedium,
                                    color = PastelTextMain,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = weather.uvLevelLabel.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarningAmber,
                                    modifier = Modifier.padding(bottom = 6.dp),
                                )
                            }
                            Text(
                                text = weather.uvPeakWindow,
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextSub,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (weather.uvIndex / 10.0).toFloat().coerceIn(0.1f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = WarningAmber,
                                trackColor = PastelBorder,
                                strokeCap = StrokeCap.Round,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Humidity Card
                        VeridiaCard(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("HUMIDITY", style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
                                Icon(Icons.Rounded.WaterDrop, null, tint = PastelPrimary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "${weather.humidityPercent}%",
                                style = MaterialTheme.typography.displayMedium,
                                color = PastelTextMain,
                            )
                            Text(
                                text = "Dew point is ${String.format("%.1f", weather.dewPointC)}°C right now",
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextSub,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (weather.humidityPercent / 100f).coerceIn(0.1f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = PastelPrimary,
                                trackColor = PastelBorder,
                                strokeCap = StrokeCap.Round,
                            )
                        }

                        // Pressure Card
                        VeridiaCard(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("PRESSURE", style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
                                Icon(Icons.Rounded.Compress, null, tint = PastelAccent, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(pressureVal, style = MaterialTheme.typography.displayMedium, color = PastelTextMain)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = pressureUnitLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelTextSub,
                                    modifier = Modifier.padding(bottom = 6.dp),
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                    contentDescription = null,
                                    tint = PastelAccent,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = weather.pressureTrendLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PastelAccent,
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = PastelBorder)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("Reference", style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
                                Text("1,013.25 Std", style = MaterialTheme.typography.labelSmall, color = PastelTextMain)
                            }
                        }
                    }
                }
            }

            // Sunrise & Sunset Celestial Tracker
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
                            Icon(Icons.Rounded.WbTwilight, null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SOLAR PATH & DAYLIGHT",
                                style = MaterialTheme.typography.labelSmall,
                                color = PastelTextSub,
                            )
                        }
                        Text(
                            text = weather.daylightDuration,
                            style = MaterialTheme.typography.labelSmall,
                            color = PastelTextSub,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    SolarArcCanvas(
                        sunriseTime = weather.sunriseTime,
                        sunsetTime = weather.sunsetTime,
                        solarProgress = weather.solarProgress,
                    )
                }
            }

            // 7-Day Extended Forecast
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
                            Icon(Icons.Rounded.CalendarToday, null, tint = PastelPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "7-Day Forecast",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PastelTextMain,
                            )
                        }
                        Text(
                            text = "Next 168 Hours",
                            style = MaterialTheme.typography.labelSmall,
                            color = PastelTextSub,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PastelBorder)

                    weather.dailyForecast.forEachIndexed { idx, day ->
                        val barColor = when (day.condition) {
                            WeatherConditionKind.SUNNY -> if (day.dayLabel == "Sun") PastelAccent else WarningAmber
                            WeatherConditionKind.THUNDERSTORM -> WarningAmber
                            WeatherConditionKind.RAIN -> PastelPrimary
                            else -> PastelBorder
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = day.dayLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Medium,
                                ),
                                color = if (idx == 0) PastelTextMain else PastelTextSub,
                                modifier = Modifier.width(58.dp),
                            )
                            Row(
                                modifier = Modifier.width(72.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = conditionIcon(day.condition),
                                    contentDescription = null,
                                    tint = conditionTint(day.condition),
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${day.precipPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = conditionTint(day.condition),
                                )
                            }
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = formatTempString(day.minTempC, tempUnit),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelTextSub,
                                    modifier = Modifier.width(30.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(PastelSubtle),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.55f)
                                            .height(6.dp)
                                            .align(Alignment.Center)
                                            .clip(CircleShape)
                                            .background(barColor),
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatTempString(day.maxTempC, tempUnit),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PastelTextMain,
                                )
                            }
                        }
                        if (idx < weather.dailyForecast.lastIndex) {
                            HorizontalDivider(color = PastelBorder.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StratumSegmentButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) PastelPrimary else Color.Transparent,
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
                color = if (selected) Color.White else PastelTextSub,
            )
        }
    }
}

@Composable
private fun StratumLayerRow(
    title: String,
    meta: String,
    dotColor: Color,
    isActive: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) PastelSky.copy(alpha = 0.65f) else PastelSubtle,
        border = BorderStroke(
            1.dp,
            if (isActive) PastelPrimary.copy(alpha = 0.35f) else PastelBorder,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                    color = PastelTextMain,
                )
            }
            Text(
                text = meta,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) PastelPrimary else PastelTextSub,
            )
        }
    }
}

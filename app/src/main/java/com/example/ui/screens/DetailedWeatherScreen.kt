package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AtmosphereStratum
import com.example.data.model.DailyForecastItem
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherConditionKind
import com.example.data.model.WeatherMapLayer
import com.example.data.model.WindUnit
import com.example.ui.components.AnimatedAirQualityWindIcon
import com.example.ui.components.AnimatedHumidityDropletIcon
import com.example.ui.components.AnimatedWeatherConditionIcon
import com.example.ui.components.BmkgInteractiveWeatherMap
import com.example.ui.components.HourlyForecast24hCard
import com.example.ui.components.LiveTelemetryPulseDot
import com.example.ui.components.SunCycleCard
import com.example.ui.components.VeridiaBackHeader
import com.example.ui.components.VeridiaCard
import com.example.ui.components.WeatherParticlesLayer
import com.example.ui.components.WindIndicatorStyle
import com.example.ui.components.WindSpeedDirectionIndicator
import com.example.ui.components.conditionTint
import com.example.ui.components.formatPressureValue
import com.example.ui.components.formatTempString
import com.example.ui.components.formatWindValue
import com.example.ui.i18n.VeridiaLocalizer
import com.example.ui.i18n.VeridiaStrings
import com.example.ui.i18n.veridiaStrings
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.veridiaColors
import com.example.ui.viewmodel.VeridiaUiState
import java.util.Locale

@Composable
fun DetailedWeatherScreen(
    state: VeridiaUiState,
    onBack: () -> Unit,
    onSelectStratum: (AtmosphereStratum) -> Unit,
    onSelectWeatherMapLayer: (WeatherMapLayer) -> Unit,
    onRefreshNow: () -> Unit,
    onOpenRadar: () -> Unit,
) {
    val colors = veridiaColors
    val strings = veridiaStrings
    val weather = state.weather
    val tempUnit = state.settings.temperatureUnit
    val windUnit = state.settings.windUnit
    val (_, windUnitLabel) = formatWindValue(weather.windSpeedKmh, windUnit)
    val (gustVal, _) = formatWindValue(weather.windGustKmh, windUnit)
    val (pressureVal, pressureUnitLabel) = formatPressureValue(weather.pressureHpa, state.settings.pressureUnit)
    var expandedDayIndex by rememberSaveable { mutableIntStateOf(1) }

    val altitudeBadge = when (state.selectedStratum) {
        AtmosphereStratum.SURFACE -> strings.altitudeSurface
        AtmosphereStratum.MID_CLOUD -> strings.altitudeMidCloud
        AtmosphereStratum.UPPER_STRATUM -> strings.altitudeUpper
    }
    val strataRangeLabel = when (state.selectedStratum) {
        AtmosphereStratum.SURFACE -> strings.strataRangeSurface
        AtmosphereStratum.MID_CLOUD -> strings.strataRangeMidCloud
        AtmosphereStratum.UPPER_STRATUM -> strings.strataRangeUpper
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
                    title = strings.weatherAtmosphereTitle,
                    subtitle = "${state.selectedCity.name} · ${VeridiaLocalizer.dataSourceLabel(weather.dataSourceLabel, strings)} · ${VeridiaLocalizer.timestampLabel(state.lastUpdatedLabel, strings)}",
                    onBackClick = onBack,
                    trailingContent = {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenRadar)
                                .testTag("detailed_weather_radar_button"),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border),
                            shadowElevation = 2.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Radar,
                                    contentDescription = strings.openRadarCd,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = strings.radarButton,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = colors.textMain,
                                )
                            }
                        }
                    },
                )
            }

            // Live Animated Atmospheric Header Card
            item {
                val skyColors = when (weather.conditionKind) {
                    WeatherConditionKind.SUNNY -> listOf(colors.backdropTop, colors.backdropMid)
                    WeatherConditionKind.THUNDERSTORM -> listOf(Color(0xFF141E30), Color(0xFF2B3E5A))
                    WeatherConditionKind.RAIN -> listOf(Color(0xFF223349), Color(0xFF45607D))
                    WeatherConditionKind.SUNSET -> listOf(Color(0xFF3E356B), Color(0xFFD97757))
                    else -> listOf(colors.backdropTop, colors.backdropMid)
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(142.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("weather_live_sky_header"),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, colors.border),
                    shadowElevation = 4.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(skyColors)),
                    ) {
                        WeatherParticlesLayer(
                            conditionKind = weather.conditionKind,
                            windSpeedKmh = weather.windSpeedKmh,
                            precipChancePercent = weather.precipChancePercent,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LiveTelemetryPulseDot(color = Color(0xFF4ADE80), size = 6.dp)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = strings.realTimeAtmosphereBadge,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White.copy(alpha = 0.88f),
                                    )
                                }
                                Text(
                                    text = formatTempString(weather.currentTempC, tempUnit),
                                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 42.sp),
                                    color = Color.White,
                                )
                                Text(
                                    text = "${VeridiaLocalizer.conditionLabel(weather.conditionLabel, weather.conditionKind, strings)} · ${strings.heroFeelsWord} ${formatTempString(weather.feelsLikeC, tempUnit)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.92f),
                                )
                            }
                            AnimatedWeatherConditionIcon(
                                kind = weather.conditionKind,
                                size = 54.dp,
                                tint = Color.White,
                            )
                        }
                    }
                }
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
                        StratumSegmentButton(
                            label = strings.surfaceTab,
                            selected = state.selectedStratum == AtmosphereStratum.SURFACE,
                            onClick = { onSelectStratum(AtmosphereStratum.SURFACE) },
                            modifier = Modifier.weight(1f).testTag("stratum_btn_surface"),
                        )
                        StratumSegmentButton(
                            label = strings.midCloudTab,
                            selected = state.selectedStratum == AtmosphereStratum.MID_CLOUD,
                            onClick = { onSelectStratum(AtmosphereStratum.MID_CLOUD) },
                            modifier = Modifier.weight(1f).testTag("stratum_btn_mid"),
                        )
                        StratumSegmentButton(
                            label = strings.upperStratumTab,
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
                            AnimatedAirQualityWindIcon(
                                size = 18.dp,
                                tint = colors.primary,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.atmosphereStratumUpper,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSub,
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.subtle,
                            border = BorderStroke(1.dp, colors.border),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Layers,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(12.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = altitudeBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMain,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    AnimatedContent(
                        targetState = strataRangeLabel,
                        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                        label = "stratum_range_anim",
                    ) { rangeLabel ->
                        Text(
                            text = rangeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSub,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StratumLayerRow(
                            title = strings.upperStratosphereTitle,
                            meta = "12,000m · ${formatTempString(-52, tempUnit, true)}",
                            dotColor = colors.primary,
                            isActive = state.selectedStratum == AtmosphereStratum.UPPER_STRATUM,
                        )
                        StratumLayerRow(
                            title = strings.midTroposphereTitle,
                            meta = "4,200m · ${formatTempString(-8, tempUnit, true)}",
                            dotColor = colors.accent,
                            isActive = state.selectedStratum == AtmosphereStratum.MID_CLOUD,
                        )
                        StratumLayerRow(
                            title = strings.groundBoundaryTitle,
                            meta = "42m · ${formatTempString(weather.currentTempC, tempUnit, true)}",
                            dotColor = colors.primary,
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
                                Text(strings.windUpper, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
                                Icon(Icons.Rounded.NearMe, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            WindSpeedDirectionIndicator(
                                windSpeedKmh = weather.windSpeedKmh,
                                windBearingDeg = weather.windBearingDeg,
                                windDirectionLabel = weather.windDirectionLabel,
                                windUnit = windUnit,
                                style = WindIndicatorStyle.DETAILED_DIAL,
                                useLargeReadout = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = colors.border)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(strings.gustsLabel, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
                                Text(
                                    "$gustVal $windUnitLabel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMain,
                                )
                            }
                        }

                        // UV Index Card
                        VeridiaCard(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(strings.uvIndexUpper, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
                                Icon(Icons.Rounded.WbSunny, null, tint = colors.warning, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%.1f", weather.uvIndex),
                                    style = MaterialTheme.typography.displayMedium,
                                    color = colors.textMain,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = VeridiaLocalizer.uvLevel(weather.uvLevelLabel, strings).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.warning,
                                    modifier = Modifier.padding(bottom = 6.dp),
                                )
                            }
                            Text(
                                text = strings.uvPeakWindowText,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSub,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val animatedUvProgress by animateFloatAsState(
                                targetValue = (weather.uvIndex / 10.0).toFloat().coerceIn(0.1f, 1f),
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "detail_uv_progress",
                            )
                            LinearProgressIndicator(
                                progress = { animatedUvProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = colors.warning,
                                trackColor = colors.border,
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
                                Text(strings.humidityUpper, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
                                AnimatedHumidityDropletIcon(size = 18.dp, tint = colors.primary)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "${weather.humidityPercent}%",
                                style = MaterialTheme.typography.displayMedium,
                                color = colors.textMain,
                            )
                            Text(
                                text = strings.dewPointSentence(String.format("%.1f", weather.dewPointC)),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSub,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val animatedHumidityDetail by animateFloatAsState(
                                targetValue = (weather.humidityPercent / 100f).coerceIn(0.1f, 1f),
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "detail_humidity_progress",
                            )
                            LinearProgressIndicator(
                                progress = { animatedHumidityDetail },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = colors.primary,
                                trackColor = colors.border,
                                strokeCap = StrokeCap.Round,
                            )
                        }

                        // Pressure Card
                        VeridiaCard(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(strings.pressureUpper, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
                                Icon(Icons.Rounded.Compress, null, tint = colors.accent, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(pressureVal, style = MaterialTheme.typography.displayMedium, color = colors.textMain)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = pressureUnitLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSub,
                                    modifier = Modifier.padding(bottom = 6.dp),
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = VeridiaLocalizer.pressureTrend(weather.pressureTrendLabel, strings),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.accent,
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = colors.border)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(strings.referenceLabel, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
                                Text(strings.referenceStdValue, style = MaterialTheme.typography.labelSmall, color = colors.textMain)
                            }
                        }
                    }
                }
            }

            // 24-Hour Horizontal Scrollable Hourly Forecast
            item {
                HourlyForecast24hCard(
                    hourlyItems = weather.next24hForecast.ifEmpty { weather.hourlyForecast },
                    tempUnit = tempUnit,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Sun Cycle Tracker Card
            item {
                SunCycleCard(
                    locationName = state.selectedCity.name,
                    sunriseTime = weather.sunriseTime,
                    sunsetTime = weather.sunsetTime,
                    daylightDuration = weather.daylightDuration,
                    solarProgress = weather.solarProgress,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Detailed Expandable Multi-Day Forecast (Today, Tomorrow, Day After & Week)
            item {
                val weekMin = weather.dailyForecast.minOfOrNull { it.minTempC } ?: 20
                val weekMax = (weather.dailyForecast.maxOfOrNull { it.maxTempC } ?: 35)
                    .coerceAtLeast(weekMin + 1)

                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("detailed_multiday_forecast_card"),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.CalendarToday, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.forecast7DayTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.textMain,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.subtle,
                            border = BorderStroke(1.dp, colors.border),
                        ) {
                            Text(
                                text = strings.next168HoursLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.tapDayForDetailsHint,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = colors.textSub,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        weather.dailyForecast.forEachIndexed { idx, day ->
                            DetailedDailyForecastCardRow(
                                idx = idx,
                                day = day,
                                isExpanded = expandedDayIndex == idx,
                                weekMin = weekMin,
                                weekMax = weekMax,
                                tempUnit = tempUnit,
                                windUnit = windUnit,
                                strings = strings,
                                onToggle = {
                                    expandedDayIndex = if (expandedDayIndex == idx) -1 else idx
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailedDailyForecastCardRow(
    idx: Int,
    day: DailyForecastItem,
    isExpanded: Boolean,
    weekMin: Int,
    weekMax: Int,
    tempUnit: TemperatureUnit,
    windUnit: WindUnit,
    strings: VeridiaStrings,
    onToggle: () -> Unit,
) {
    val colors = veridiaColors
    val barColor = when (day.condition) {
        WeatherConditionKind.SUNNY -> colors.warning
        WeatherConditionKind.THUNDERSTORM -> colors.warning
        WeatherConditionKind.RAIN -> colors.primary
        else -> colors.accent
    }
    val span = (weekMax - weekMin).coerceAtLeast(1).toFloat()
    val endFraction = ((day.maxTempC - weekMin) / span).coerceIn(0.35f, 1f)
    val relLabel = VeridiaLocalizer.dayRelativeLabel(idx, day.dayLabel, strings)
    val localizedDate = VeridiaLocalizer.dateLabel(day.dateLabel, strings)
    val condText = VeridiaLocalizer.conditionLabel(
        day.conditionSummary.ifBlank { day.condition.name.lowercase().replace('_', ' ') },
        day.condition,
        strings,
    )
    val (windMaxVal, windUnitLabel) = formatWindValue(day.windMaxKmh, windUnit)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .testTag("daily_forecast_card_$idx"),
        shape = RoundedCornerShape(14.dp),
        color = if (isExpanded) colors.sky.copy(alpha = 0.55f) else colors.subtle.copy(alpha = 0.7f),
        border = BorderStroke(
            1.dp,
            if (isExpanded) colors.primary.copy(alpha = 0.4f) else colors.border,
        ),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.width(86.dp)) {
                    Text(
                        text = relLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (idx <= 1 || isExpanded) FontWeight.Bold else FontWeight.SemiBold,
                        ),
                        color = if (idx <= 1 || isExpanded) colors.textMain else colors.textSub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (localizedDate.isNotBlank()) {
                        Text(
                            text = localizedDate,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = colors.textSub,
                        )
                    }
                }

                Row(
                    modifier = Modifier.width(68.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedWeatherConditionIcon(
                        kind = day.condition,
                        size = 20.dp,
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${day.precipPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (day.precipPercent >= 50) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = conditionTint(day.condition),
                    )
                }

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatTempString(day.minTempC, tempUnit),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = SpaceMonoFamily),
                        color = colors.textSub,
                        modifier = Modifier.width(32.dp),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(colors.surface),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(endFraction)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(barColor),
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatTempString(day.maxTempC, tempUnit),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceMonoFamily,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.textMain,
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = colors.textSub,
                    modifier = Modifier.size(16.dp),
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                ) {
                    HorizontalDivider(color = colors.border.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DailyDetailMetricCell(
                            label = strings.detailConditionLabel,
                            value = condText,
                            modifier = Modifier.weight(1f),
                        )
                        DailyDetailMetricCell(
                            label = strings.detailRainfallLabel,
                            value = "${String.format(Locale.US, "%.1f", day.precipMm)} mm (${day.precipPercent}%)",
                            modifier = Modifier.weight(1f),
                        )
                        DailyDetailMetricCell(
                            label = strings.detailWindMaxLabel,
                            value = "$windMaxVal $windUnitLabel",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DailyDetailMetricCell(
                            label = strings.detailHumidityLabel,
                            value = "${day.humidityPercent}%",
                            modifier = Modifier.weight(1f),
                        )
                        DailyDetailMetricCell(
                            label = strings.detailUvPeakLabel,
                            value = String.format(Locale.US, "%.1f", day.uvIndexMax),
                            modifier = Modifier.weight(1f),
                        )
                        DailyDetailMetricCell(
                            label = strings.detailTempRangeLabel,
                            value = "${formatTempString(day.minTempC, tempUnit)} – ${formatTempString(day.maxTempC, tempUnit)}",
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyDetailMetricCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = veridiaColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.surface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, colors.border.copy(alpha = 0.7f)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = colors.textSub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.textMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
    val colors = veridiaColors
    val pillColor by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "stratum_pill_color",
    )
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = pillColor,
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
private fun StratumLayerRow(
    title: String,
    meta: String,
    dotColor: Color,
    isActive: Boolean,
) {
    val colors = veridiaColors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) colors.sky.copy(alpha = 0.65f) else colors.subtle,
        border = BorderStroke(
            1.dp,
            if (isActive) colors.primary.copy(alpha = 0.35f) else colors.border,
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
                    color = colors.textMain,
                )
            }
            Text(
                text = meta,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) colors.primary else colors.textSub,
            )
        }
    }
}

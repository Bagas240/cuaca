package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyForecastItem
import com.example.data.model.TemperatureUnit
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelPrimary
import com.example.ui.theme.PastelSky
import com.example.ui.theme.PastelSunset
import com.example.ui.theme.PastelTextMain
import com.example.ui.theme.PastelTextSub
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.WarningAmber

/**
 * Horizontal scrollable 24-hour forecast view using LazyRow to display
 * time slots, animated weather condition icons, rain chances, and temperatures.
 */
@Composable
fun HourlyForecast24hCard(
    hourlyItems: List<HourlyForecastItem>,
    tempUnit: TemperatureUnit,
    modifier: Modifier = Modifier,
) {
    VeridiaCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hourly_forecast_24h_card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = PastelPrimary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "24-HOUR FORECAST",
                    style = MaterialTheme.typography.labelSmall,
                    color = PastelTextSub,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                LiveTelemetryPulseDot(color = PastelPrimary, size = 6.dp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Next 24 Hours",
                    style = MaterialTheme.typography.labelSmall,
                    color = PastelPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = PastelBorder.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hourly_forecast_24h_list"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
        ) {
            items(
                items = hourlyItems.take(25),
                key = { "${it.timeLabel}_${it.isCurrent}" },
            ) { hour ->
                val bg = when {
                    hour.isCurrent -> PastelSky.copy(alpha = 0.9f)
                    hour.isSunset -> WarningAmber.copy(alpha = 0.14f)
                    else -> Color.Transparent
                }
                val border = when {
                    hour.isCurrent -> BorderStroke(1.dp, PastelPrimary.copy(alpha = 0.35f))
                    hour.isSunset -> BorderStroke(1.dp, WarningAmber.copy(alpha = 0.35f))
                    else -> null
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = bg,
                    border = border,
                ) {
                    Column(
                        modifier = Modifier
                            .width(66.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = hour.timeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = if (hour.isCurrent) MaterialTheme.typography.labelSmall.fontFamily else SpaceMonoFamily,
                                fontWeight = if (hour.isCurrent || hour.isSunset) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = when {
                                hour.isCurrent -> PastelPrimary
                                hour.isSunset -> PastelSunset
                                else -> PastelTextSub
                            },
                            maxLines = 1,
                        )
                        AnimatedWeatherConditionIcon(
                            kind = hour.condition,
                            size = 24.dp,
                        )
                        if (hour.precipPercent >= 25) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WaterDrop,
                                    contentDescription = null,
                                    tint = PastelPrimary,
                                    modifier = Modifier.size(9.dp),
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${hour.precipPercent}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = PastelPrimary,
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Text(
                            text = formatTempString(hour.tempC, tempUnit),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 16.sp,
                                fontWeight = if (hour.isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                            ),
                            color = PastelTextMain,
                        )
                    }
                }
            }
        }
    }
}

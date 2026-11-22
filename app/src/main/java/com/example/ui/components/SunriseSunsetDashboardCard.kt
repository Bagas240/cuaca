package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.VeridiaLocalizer
import com.example.ui.i18n.veridiaStrings
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.veridiaColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SunriseSunsetDashboardCard(
    locationName: String,
    coordinatesLabel: String?,
    isGpsActive: Boolean,
    sunriseTime: String,
    sunsetTime: String,
    solarNoonTime: String,
    daylightDuration: String,
    solarProgress: Float,
    todayDateLabel: String,
    nextSolarEventLabel: String,
    moonPhaseName: String = "Waxing Gibbous",
    moonIlluminationPercent: Int = 68,
    moonPhaseFraction: Float = 0.35f,
    modifier: Modifier = Modifier,
) {
    val colors = veridiaColors
    val strings = veridiaStrings

    VeridiaCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sunrise_sunset_dashboard_card"),
    ) {
        // Header row: title + today's date + daylight length
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    colors.warning.copy(alpha = 0.24f),
                                    colors.sky,
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WbTwilight,
                        contentDescription = strings.sunriseSunsetCd,
                        tint = colors.sunset,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = strings.sunriseSunsetTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.textSub,
                    )
                    Text(
                        text = if (todayDateLabel.isBlank()) {
                            strings.todayLabel
                        } else {
                            VeridiaLocalizer.solarDateLabel(todayDateLabel, strings)
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.textMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("sunrise_sunset_date"),
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colors.warning.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.32f)),
            ) {
                Text(
                    text = VeridiaLocalizer.daylightDuration(daylightDuration, strings),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.sunset,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Location & coarse/masked coordinates pill
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sunrise_sunset_location"),
            shape = RoundedCornerShape(12.dp),
            color = colors.subtle,
            border = BorderStroke(1.dp, colors.border),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = if (isGpsActive) Icons.Rounded.MyLocation else Icons.Rounded.Public,
                        contentDescription = if (isGpsActive) strings.currentGpsZoneCd else strings.selectedCityCd,
                        tint = colors.primary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (!coordinatesLabel.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGpsActive) "GPS · $coordinatesLabel" else coordinatesLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceMonoFamily,
                            fontSize = 10.sp,
                        ),
                        color = colors.textSub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Animated horizon arc showing current position of the sun
        SolarArcCanvas(
            sunriseTime = sunriseTime,
            sunsetTime = sunsetTime,
            solarProgress = solarProgress,
            showInlineLabels = false,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Sunrise & Sunset time blocks
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .testTag("sunrise_sunset_sunrise_tile"),
                shape = RoundedCornerShape(14.dp),
                color = colors.warning.copy(alpha = 0.11f),
                border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.30f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.warning.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = strings.sunriseTimeCd,
                            tint = colors.warning,
                            modifier = Modifier.size(17.dp),
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
                    Spacer(modifier = Modifier.width(10.dp))
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
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colors.textMain,
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .testTag("sunrise_sunset_sunset_tile"),
                shape = RoundedCornerShape(14.dp),
                color = colors.primary.copy(alpha = 0.09f),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.26f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NightsStay,
                            contentDescription = strings.sunsetTimeCd,
                            tint = colors.primary,
                            modifier = Modifier.size(17.dp),
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
                    Spacer(modifier = Modifier.width(10.dp))
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
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colors.textMain,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lunar Phase row computed from current date
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sunrise_sunset_moon_phase"),
            shape = RoundedCornerShape(12.dp),
            color = colors.primary.copy(alpha = 0.07f),
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.20f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoonPhaseCanvas(
                        phaseFraction = moonPhaseFraction,
                        litColor = colors.warning,
                        shadowColor = colors.textSub.copy(alpha = 0.25f),
                        rimColor = colors.primary.copy(alpha = 0.55f),
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.moonPhaseTitle,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 0.7.sp,
                            ),
                            color = colors.textSub,
                        )
                        Text(
                            text = VeridiaLocalizer.moonPhaseName(moonPhaseName, strings),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = colors.textMain,
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border),
                ) {
                    Text(
                        text = "$moonIlluminationPercent% ${strings.litSuffix}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = SpaceMonoFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Solar Noon + Next Event countdown footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.subtle)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("sunrise_sunset_solar_noon"),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = strings.solarNoonLabel,
                    tint = colors.sunset,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${strings.solarNoonLabel} · $solarNoonTime",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceMonoFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.textMain,
                )
            }

            if (nextSolarEventLabel.isNotBlank()) {
                Text(
                    text = VeridiaLocalizer.nextSolarEventLabel(nextSolarEventLabel, strings),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                    ),
                    color = colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("sunrise_sunset_next_event"),
                )
            }
        }
    }
}

@Composable
fun MoonPhaseCanvas(
    phaseFraction: Float,
    litColor: Color,
    shadowColor: Color,
    rimColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Base unlit lunar disc
        drawCircle(
            color = shadowColor,
            radius = r,
            center = center,
        )

        val f = phaseFraction.coerceIn(0f, 1f)
        val k = cos(2.0 * Math.PI * f).toFloat() // +1 new, 0 quarter, -1 full
        val waxing = f <= 0.5f

        val litPath = Path()
        val rect = Rect(center.x - r, center.y - r, center.x + r, center.y + r)
        if (waxing) {
            // Right semi-circle lit limb (top -> bottom clockwise)
            litPath.arcTo(rect, startAngleDegrees = -90f, sweepAngleDegrees = 180f, forceMoveTo = true)
            // Terminator from bottom to top
            val steps = 24
            for (i in steps downTo 0) {
                val theta = -Math.PI / 2.0 + (Math.PI * i / steps)
                val x = center.x + (r * k * cos(theta).toFloat())
                val y = center.y + (r * sin(theta).toFloat())
                litPath.lineTo(x, y)
            }
            litPath.close()
        } else {
            // Left semi-circle lit limb (top -> bottom counter-clockwise)
            litPath.arcTo(rect, startAngleDegrees = -90f, sweepAngleDegrees = -180f, forceMoveTo = true)
            val steps = 24
            for (i in steps downTo 0) {
                val theta = -Math.PI / 2.0 + (Math.PI * i / steps)
                val x = center.x - (r * k * cos(theta).toFloat())
                val y = center.y + (r * sin(theta).toFloat())
                litPath.lineTo(x, y)
            }
            litPath.close()
        }

        if (abs(k) < 0.985f || f in 0.46f..0.54f) {
            drawPath(path = litPath, color = litColor)
        }

        drawCircle(
            color = rimColor,
            radius = r - 0.5.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

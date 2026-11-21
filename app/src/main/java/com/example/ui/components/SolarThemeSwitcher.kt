package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.NightsStay
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeMode
import com.example.data.model.SolarThemePhase
import com.example.ui.theme.veridiaColors

fun solarPhaseDisplayLabel(phase: SolarThemePhase): String = when (phase) {
    SolarThemePhase.SUNRISE_SUNSET -> "Sunrise/Sunset · Rose-White"
    SolarThemePhase.DAY -> "Day · Bright & Warm"
    SolarThemePhase.AFTERNOON_DUSK -> "Afternoon · Shaded Dusk"
    SolarThemePhase.NIGHT -> "Night · Dark & Cool"
}

@Composable
fun SolarThemeSwitcherCard(
    themeMode: AppThemeMode,
    activePhase: SolarThemePhase,
    sunriseTime: String,
    sunsetTime: String,
    onSelectThemeMode: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = veridiaColors
    val phaseIcon = when (activePhase) {
        SolarThemePhase.SUNRISE_SUNSET -> Icons.Rounded.WbTwilight
        SolarThemePhase.DAY -> Icons.Rounded.WbSunny
        SolarThemePhase.AFTERNOON_DUSK -> Icons.Rounded.WbTwilight
        SolarThemePhase.NIGHT -> Icons.Rounded.NightsStay
    }
    val phaseTint = when (activePhase) {
        SolarThemePhase.SUNRISE_SUNSET -> colors.primary
        SolarThemePhase.DAY -> colors.warning
        SolarThemePhase.AFTERNOON_DUSK -> colors.sunset
        SolarThemePhase.NIGHT -> colors.primary
    }

    VeridiaCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("solar_theme_switcher_card"),
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
                Icon(
                    imageVector = phaseIcon,
                    contentDescription = "Active solar theme phase",
                    tint = phaseTint,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ADAPTIVE SOLAR THEME",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 0.7.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.textSub,
                    )
                    Text(
                        text = solarPhaseDisplayLabel(activePhase),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("solar_theme_active_phase"),
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.subtle,
                border = BorderStroke(1.dp, colors.border),
            ) {
                Text(
                    text = if (themeMode == AppThemeMode.AUTO_SOLAR) {
                        "Auto · $sunriseTime–$sunsetTime"
                    } else {
                        "Manual"
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = colors.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colors.subtle,
            border = BorderStroke(1.dp, colors.border),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ThemeModePill(
                    label = "Auto",
                    icon = Icons.Rounded.AutoAwesome,
                    selected = themeMode == AppThemeMode.AUTO_SOLAR,
                    onClick = { onSelectThemeMode(AppThemeMode.AUTO_SOLAR) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_switch_auto"),
                )
                ThemeModePill(
                    label = "Dawn",
                    icon = Icons.Rounded.WbTwilight,
                    selected = themeMode == AppThemeMode.SUNRISE_SUNSET,
                    onClick = { onSelectThemeMode(AppThemeMode.SUNRISE_SUNSET) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_switch_sunrise_sunset"),
                )
                ThemeModePill(
                    label = "Day",
                    icon = Icons.Rounded.WbSunny,
                    selected = themeMode == AppThemeMode.DAY,
                    onClick = { onSelectThemeMode(AppThemeMode.DAY) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_switch_day"),
                )
                ThemeModePill(
                    label = "Dusk",
                    icon = Icons.Rounded.WbTwilight,
                    selected = themeMode == AppThemeMode.AFTERNOON_DUSK,
                    onClick = { onSelectThemeMode(AppThemeMode.AFTERNOON_DUSK) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_switch_afternoon_dusk"),
                )
                ThemeModePill(
                    label = "Night",
                    icon = Icons.Rounded.DarkMode,
                    selected = themeMode == AppThemeMode.NIGHT,
                    onClick = { onSelectThemeMode(AppThemeMode.NIGHT) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_switch_night"),
                )
            }
        }
    }
}

@Composable
private fun ThemeModePill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = veridiaColors
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) colors.surface else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)) else null,
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = "$label theme mode",
                    tint = if (selected) colors.primary else colors.textSub,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (selected) colors.primary else colors.textSub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

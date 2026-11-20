package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Volcano
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alerts.DisasterKind
import com.example.data.model.SeismicSeverity
import com.example.ui.components.BmkgShakemapCard
import com.example.ui.components.LiveTelemetryPulseDot
import com.example.ui.components.VeridiaBackHeader
import com.example.ui.components.VeridiaCard
import com.example.ui.navigation.rememberIsDestinationAnimating
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.PastelAccent
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelDangerBg
import com.example.ui.theme.PastelDangerBorder
import com.example.ui.theme.PastelDangerText
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
fun DisasterMonitorScreen(
    state: VeridiaUiState,
    gyroTilt: Offset,
    onRefreshNow: () -> Unit,
    onBack: () -> Unit,
    onOpenSosModal: () -> Unit,
    onCloseSosModal: () -> Unit,
    onTriggerSampleAlert: (DisasterKind) -> Unit = {},
    onStopSiren: () -> Unit = {},
    onToggleSeismicSensor: (Boolean) -> Unit = {},
    onCheckBackgroundAlertsNow: () -> Unit = {},
) {
    val seismic = state.seismic
    val isAnimating = rememberIsDestinationAnimating()
    val sensorScale: Float
    if (isAnimating) {
        val pulseTransition = rememberInfiniteTransition(label = "seismic_pulse")
        val animatedScale by pulseTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "sensor_scale",
        )
        sensorScale = animatedScale
    } else {
        sensorScale = 1f
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
                    title = "Disaster Monitor",
                    subtitle = "Source: ${seismic.dataSourceLabel}",
                    onBackClick = onBack,
                    trailingContent = {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onOpenSosModal)
                                .testTag("sos_help_button"),
                            shape = RoundedCornerShape(12.dp),
                            color = DangerCrimson,
                            shadowElevation = 2.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Emergency,
                                    contentDescription = "Emergency SOS",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "SOS Help",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Color.White,
                                )
                            }
                        }
                    },
                )
            }

            // Official BMKG Shakemap & 2D Geographic Epicenter Radar Map
            item {
                BmkgShakemapCard(
                    shakemapUrl = seismic.shakemapUrl,
                    epicenterLat = seismic.epicenterLat,
                    epicenterLon = seismic.epicenterLon,
                    magnitudeLabel = seismic.bannerMagnitude,
                    locationLabel = seismic.recentEvents.firstOrNull()?.location ?: "Indonesia",
                    recentEvents = seismic.recentEvents,
                    lastUpdatedLabel = state.lastUpdatedLabel,
                    isSyncing = state.isSyncing,
                    dataSourceLabel = seismic.dataSourceLabel,
                    onRefreshNow = onRefreshNow,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Active Seismic Early Warning Card
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    borderColor = PastelDangerBorder,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PastelDangerBg,
                            border = BorderStroke(1.dp, PastelDangerBorder),
                            modifier = Modifier
                                .size(42.dp)
                                .graphicsLayer {
                                    scaleX = sensorScale
                                    scaleY = sensorScale
                                },
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Sensors, null, tint = PastelDangerText, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = seismic.warningHeader.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PastelDangerText,
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PastelDangerBg,
                                    border = BorderStroke(1.dp, PastelDangerBorder),
                                ) {
                                    Text(
                                        text = seismic.shindoBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PastelDangerText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = seismic.activeEventTitle,
                                style = MaterialTheme.typography.displayMedium.copy(fontSize = 26.sp, lineHeight = 32.sp),
                                color = PastelTextMain,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = seismic.activeEventDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextSub,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = PastelBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SeismicStatPill(
                            label = "Depth",
                            value = "${String.format(Locale.US, "%.1f", seismic.depthKm)} km",
                            valueColor = PastelTextMain,
                            modifier = Modifier.weight(1f),
                        )
                        SeismicStatPill(
                            label = "Peak PGA",
                            value = "${seismic.peakPgaGal} gal",
                            valueColor = PastelDangerText,
                            modifier = Modifier.weight(1f),
                        )
                        SeismicStatPill(
                            label = "Tsunami",
                            value = seismic.tsunamiStatus,
                            valueColor = PastelSunset,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Early Warning Civil-Defense Notification & Disaster Siren Controls
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("disaster_siren_panel"),
                    borderColor = if (state.isSirenPlaying) DangerCrimson.copy(alpha = 0.6f) else PastelBorder,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = DangerCrimson,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "CIVIL-DEFENSE DISASTER SIREN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelTextSub,
                                )
                                Text(
                                    text = "Zone Early Warning & Notification",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PastelTextMain,
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiveTelemetryPulseDot(
                                color = if (state.isSirenPlaying) DangerCrimson else PastelAccent,
                                size = 6.dp,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.isSirenPlaying) "SIREN ACTIVE" else "ARMED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (state.isSirenPlaying) DangerCrimson else SecondaryGreenDark,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Automatically dispatches disaster-specific notifications & a sweeping civil-defense siren (non-ambulance) when earthquakes, tsunami advisories, extreme storms, or hazardous air strike ${state.selectedCity.name}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PastelTextSub,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DisasterSirenChip(
                            label = "Earthquake",
                            icon = Icons.Rounded.Vibration,
                            accent = DangerCrimson,
                            onClick = { onTriggerSampleAlert(DisasterKind.EARTHQUAKE) },
                            modifier = Modifier.weight(1f).testTag("test_siren_earthquake"),
                        )
                        DisasterSirenChip(
                            label = "Tsunami",
                            icon = Icons.Rounded.Waves,
                            accent = PastelPrimary,
                            onClick = { onTriggerSampleAlert(DisasterKind.TSUNAMI) },
                            modifier = Modifier.weight(1f).testTag("test_siren_tsunami"),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DisasterSirenChip(
                            label = "Storm",
                            icon = Icons.Rounded.Thunderstorm,
                            accent = WarningAmber,
                            onClick = { onTriggerSampleAlert(DisasterKind.SEVERE_STORM) },
                            modifier = Modifier.weight(1f).testTag("test_siren_storm"),
                        )
                        DisasterSirenChip(
                            label = "Air Hazard",
                            icon = Icons.Rounded.Air,
                            accent = PastelSunset,
                            onClick = { onTriggerSampleAlert(DisasterKind.HAZARDOUS_AIR) },
                            modifier = Modifier.weight(1f).testTag("test_siren_air"),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PastelBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = "BACKGROUND ALERT SERVICE",
                                style = MaterialTheme.typography.labelSmall,
                                color = PastelTextSub,
                            )
                            Text(
                                text = state.backgroundAlertCheckStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = PastelTextMain,
                                modifier = Modifier.testTag("workmanager_alert_status"),
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onCheckBackgroundAlertsNow)
                                .testTag("check_alerts_now_button"),
                            shape = RoundedCornerShape(10.dp),
                            color = PastelSubtle,
                            border = BorderStroke(1.dp, PastelBorder),
                        ) {
                            Text(
                                text = "Scan Now",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PastelPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            )
                        }
                    }

                    if (state.isSirenPlaying) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onStopSiren,
                            colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("disaster_stop_siren_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeOff,
                                contentDescription = "Silence Disaster Siren",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Silence Civil-Defense Siren", color = Color.White)
                        }
                    }
                }
            }

            // On-Device Accelerometer Seismic Vibration Pattern Detector Card
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("accelerometer_seismic_card"),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Rounded.Vibration,
                                contentDescription = null,
                                tint = PastelPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "ACCELEROMETER SEISMIC SENSOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PastelTextSub,
                                )
                                Text(
                                    text = if (!state.seismicSensorAvailable) {
                                        "Hardware Accelerometer Unavailable"
                                    } else if (state.seismicSensorMonitoringEnabled) {
                                        "Active · Pattern Detector Armed"
                                    } else {
                                        "Sensor Monitoring Paused"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PastelTextMain,
                                )
                            }
                        }
                        Switch(
                            checked = state.seismicSensorMonitoringEnabled,
                            onCheckedChange = onToggleSeismicSensor,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PastelPrimary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = PastelBorder,
                            ),
                            modifier = Modifier.testTag("switch_accelerometer_seismic"),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PastelBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Live Linear Vibration: ${String.format(Locale.US, "%.2f", state.liveAccelerometerMs2)} m/s²",
                            style = MaterialTheme.typography.bodySmall,
                            color = PastelTextMain,
                        )
                        Text(
                            text = "Detections: ${state.seismicPatternDetectedCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.seismicPatternDetectedCount > 0) DangerCrimson else PastelTextSub,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (state.liveAccelerometerMs2 / 6.0f).coerceIn(0.03f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (state.liveAccelerometerMs2 >= 2.4f) DangerCrimson else PastelPrimary,
                        trackColor = PastelSubtle,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Monitors sustained oscillatory tremor patterns (≥2.4 m/s² with axis reversals) to trigger instant siren alerts.",
                        style = MaterialTheme.typography.labelSmall,
                        color = PastelTextSub,
                    )
                }
            }

            // 4 Consolidated Hazard Metrics
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
                        HazardMetricCard(
                            title = "P-WAVE LEAD",
                            value = String.format(Locale.US, "%04.1f", seismic.pWaveLeadSec),
                            unit = "sec",
                            unitColor = PastelTextSub,
                            subtitle = "Primary wave elapsed",
                            footerLeft = "Status",
                            footerRight = "Decaying",
                            footerRightColor = SecondaryGreenDark,
                            icon = Icons.Rounded.Timer,
                            iconTint = DangerCrimson,
                            modifier = Modifier.weight(1f),
                        )
                        HazardMetricCard(
                            title = "S-WAVE SHOCK",
                            value = String.format(Locale.US, "%.1f", seismic.sWaveJma),
                            unit = "JMA",
                            unitColor = PastelSunset,
                            subtitle = "Felt strong indoors",
                            footerLeft = "Structural",
                            footerRight = "Safe",
                            footerRightColor = SecondaryGreenDark,
                            icon = Icons.Rounded.Vibration,
                            iconTint = WarningAmber,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HazardMetricCard(
                            title = "TSUNAMI BUOY",
                            value = String.format(Locale.US, "%.1f", seismic.tsunamiSurgeMeters),
                            unit = "m surge",
                            unitColor = PastelTextSub,
                            subtitle = "Coastal marine advisory",
                            footerLeft = "Bayside",
                            footerRight = "Restricted",
                            footerRightColor = PastelSunset,
                            icon = Icons.Rounded.Waves,
                            iconTint = PastelPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        HazardMetricCard(
                            title = "VOLCANIC ARRAY",
                            value = seismic.volcanicLevel,
                            unit = "Normal",
                            unitColor = SecondaryGreenDark,
                            subtitle = seismic.volcanicNote,
                            footerLeft = "Tiltmeter",
                            footerRight = "${seismic.volcanicTiltUrad} µrad",
                            footerRightColor = PastelTextMain,
                            icon = Icons.Rounded.Volcano,
                            iconTint = PastelAccent,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Recent Regional Events List
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
                            Icon(Icons.Rounded.History, null, tint = DangerCrimson, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Recent Seismic Activity",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PastelTextMain,
                            )
                        }
                        Text(
                            text = "Past 48 Hours",
                            style = MaterialTheme.typography.labelSmall,
                            color = PastelTextSub,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = PastelBorder)

                    seismic.recentEvents.forEachIndexed { idx, event ->
                        val (dotColor, valueColor) = when (event.severity) {
                            SeismicSeverity.HIGH -> DangerCrimson to PastelDangerText
                            SeismicSeverity.MODERATE -> WarningAmber to PastelSunset
                            SeismicSeverity.LOW -> PastelAccent to SecondaryGreenDark
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
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(dotColor),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = event.location,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PastelTextMain,
                                    )
                                    Text(
                                        text = "Depth ${event.depthKm}km · ${event.timeAgoLabel}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PastelTextSub,
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "M ${String.format(Locale.US, "%.1f", event.magnitude)}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = valueColor,
                                )
                                Text(
                                    text = event.shindoLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = valueColor,
                                )
                            }
                        }
                        if (idx < seismic.recentEvents.lastIndex) {
                            HorizontalDivider(color = PastelBorder.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }

        if (state.isSosModalOpen) {
            SosEmergencyModalSheet(onDismiss = onCloseSosModal)
        }
    }
}

@Composable
private fun SeismicStatPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PastelSubtle,
        border = BorderStroke(1.dp, PastelBorder),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = valueColor,
            )
        }
    }
}

@Composable
private fun HazardMetricCard(
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
    VeridiaCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.displayMedium, color = PastelTextMain)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = unitColor,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = PastelTextSub)
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = PastelBorder)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(footerLeft, style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
            Text(footerRight, style = MaterialTheme.typography.labelSmall, color = footerRightColor)
        }
    }
}

@Composable
private fun DisasterSirenChip(
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = accent.copy(alpha = 0.09f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.30f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = PastelTextMain,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.VolumeUp,
                contentDescription = "Test Siren",
                tint = accent,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SosEmergencyModalSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PastelSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Shield, null, tint = DangerCrimson, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Emergency Safety Protocol",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PastelTextMain,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, "Close SOS", tint = PastelTextSub)
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PastelDangerBg,
                border = BorderStroke(1.dp, PastelDangerBorder),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Immediate Shake Response",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = PastelDangerText,
                    )
                    Text(
                        text = "1. Drop, Cover under sturdy furniture, and Hold On.\n" +
                            "2. Keep away from glass windows and unsecured shelving.\n" +
                            "3. Avoid coastal bayside zones during Tsunami Advisory.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PastelTextMain,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = PastelSubtle,
                    border = BorderStroke(1.dp, PastelBorder),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Fire / Ambulance", style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
                        Text("119", style = MaterialTheme.typography.headlineMedium, color = PastelDangerText)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = PastelSubtle,
                    border = BorderStroke(1.dp, PastelBorder),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Police / Rescue", style = MaterialTheme.typography.labelSmall, color = PastelTextSub)
                        Text("110", style = MaterialTheme.typography.headlineMedium, color = PastelPrimary)
                    }
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sos_acknowledge_button"),
            ) {
                Text("Acknowledge Safety Checklist", color = Color.White)
            }
        }
    }
}

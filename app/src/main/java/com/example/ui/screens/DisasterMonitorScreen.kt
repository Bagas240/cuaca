package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.ui.components.rememberQuantizedCyclePhase
import com.example.ui.i18n.VeridiaLocalizer
import com.example.ui.i18n.veridiaStrings
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.veridiaColors
import com.example.ui.viewmodel.VeridiaUiState
import kotlinx.coroutines.launch
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
    val colors = veridiaColors
    val strings = veridiaStrings
    val seismic = state.seismic
    val scope = rememberCoroutineScope()
    val sensorWave by rememberQuantizedCyclePhase(
        cycleDurationMs = 1300,
        pingPong = true,
        idleValue = 0.35f,
        label = "seismic_pulse",
    )
    val sensorTapImpulse = remember { Animatable(1f) }
    val sensorScale = (0.94f + sensorWave * 0.18f) * sensorTapImpulse.value

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
                    title = strings.disasterMonitorTitle,
                    subtitle = "${strings.sourcePrefix}: ${VeridiaLocalizer.dataSourceLabel(seismic.dataSourceLabel, strings)}",
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
                                    contentDescription = strings.emergencySosCd,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = strings.sosHelpButton,
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
                    borderColor = colors.dangerBorder,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.dangerBg,
                            border = BorderStroke(1.dp, colors.dangerBorder),
                            modifier = Modifier
                                .size(42.dp)
                                .graphicsLayer {
                                    scaleX = sensorScale
                                    scaleY = sensorScale
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    scope.launch {
                                        sensorTapImpulse.snapTo(0.80f)
                                        sensorTapImpulse.animateTo(
                                            targetValue = 1f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium,
                                            ),
                                        )
                                    }
                                }
                                .testTag("seismic_sensor_pulse_badge"),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Sensors, contentDescription = "Pulse seismic telemetry sensor", tint = colors.dangerText, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = VeridiaLocalizer.seismicWarningHeader(seismic.warningHeader, strings).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.dangerText,
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.dangerBg,
                                    border = BorderStroke(1.dp, colors.dangerBorder),
                                ) {
                                    Text(
                                        text = seismic.shindoBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.dangerText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = VeridiaLocalizer.seismicEventTitle(seismic.activeEventTitle, strings),
                                style = MaterialTheme.typography.displayMedium.copy(fontSize = 26.sp, lineHeight = 32.sp),
                                color = colors.textMain,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = VeridiaLocalizer.seismicDescription(seismic.activeEventDescription, strings),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSub,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SeismicStatPill(
                            label = strings.depthLabel,
                            value = "${String.format(Locale.US, "%.1f", seismic.depthKm)} km",
                            valueColor = colors.textMain,
                            modifier = Modifier.weight(1f),
                        )
                        SeismicStatPill(
                            label = strings.peakPgaLabel,
                            value = "${seismic.peakPgaGal} gal",
                            valueColor = colors.dangerText,
                            modifier = Modifier.weight(1f),
                        )
                        SeismicStatPill(
                            label = strings.tsunamiLabel,
                            value = VeridiaLocalizer.tsunamiStatus(seismic.tsunamiStatus, strings),
                            valueColor = colors.sunset,
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
                    borderColor = if (state.isSirenPlaying) DangerCrimson.copy(alpha = 0.6f) else colors.border,
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
                                    text = strings.civilDefenseSirenUpper,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSub,
                                )
                                Text(
                                    text = strings.zoneEarlyWarningSubtitle,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textMain,
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LiveTelemetryPulseDot(
                                color = if (state.isSirenPlaying) DangerCrimson else colors.accent,
                                size = 6.dp,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (state.isSirenPlaying) strings.sirenActiveBadge else strings.armedBadge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (state.isSirenPlaying) DangerCrimson else colors.greenText,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.sirenDescriptionForCity(state.selectedCity.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSub,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        DisasterSirenChip(
                            label = strings.disasterEarthquakeChip,
                            icon = Icons.Rounded.Vibration,
                            accent = DangerCrimson,
                            onClick = { onTriggerSampleAlert(DisasterKind.EARTHQUAKE) },
                            modifier = Modifier.weight(1f).testTag("test_siren_earthquake"),
                        )
                        DisasterSirenChip(
                            label = strings.disasterTsunamiChip,
                            icon = Icons.Rounded.Waves,
                            accent = colors.primary,
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
                            label = strings.disasterStormChip,
                            icon = Icons.Rounded.Thunderstorm,
                            accent = colors.warning,
                            onClick = { onTriggerSampleAlert(DisasterKind.SEVERE_STORM) },
                            modifier = Modifier.weight(1f).testTag("test_siren_storm"),
                        )
                        DisasterSirenChip(
                            label = strings.disasterAirHazardChip,
                            icon = Icons.Rounded.Air,
                            accent = colors.sunset,
                            onClick = { onTriggerSampleAlert(DisasterKind.HAZARDOUS_AIR) },
                            modifier = Modifier.weight(1f).testTag("test_siren_air"),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = strings.backgroundAlertServiceUpper,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSub,
                            )
                            Text(
                                text = VeridiaLocalizer.alertScanStatus(state.backgroundAlertCheckStatus, strings),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMain,
                                modifier = Modifier.testTag("workmanager_alert_status"),
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    enabled = !state.isCheckingBackgroundAlerts,
                                    onClick = onCheckBackgroundAlertsNow,
                                )
                                .testTag("check_alerts_now_button"),
                            shape = RoundedCornerShape(10.dp),
                            color = colors.subtle,
                            border = BorderStroke(1.dp, colors.border),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (state.isCheckingBackgroundAlerts) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(13.dp),
                                        strokeWidth = 2.dp,
                                        color = colors.primary,
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (state.isCheckingBackgroundAlerts) strings.scanningLabel else strings.scanNowButton,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.primary,
                                )
                            }
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
                                contentDescription = strings.silenceDisasterSirenCd,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.silenceDisasterSirenButton, color = Color.White)
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
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = strings.accelerometerSeismicUpper,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSub,
                                )
                                Text(
                                    text = if (!state.seismicSensorAvailable) {
                                        strings.accelUnavailable
                                    } else if (state.seismicSensorMonitoringEnabled) {
                                        strings.accelArmed
                                    } else {
                                        strings.accelPaused
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textMain,
                                )
                            }
                        }
                        Switch(
                            checked = state.seismicSensorMonitoringEnabled,
                            onCheckedChange = onToggleSeismicSensor,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = colors.primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = colors.border,
                            ),
                            modifier = Modifier.testTag("switch_accelerometer_seismic"),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${strings.liveLinearVibrationLabel}: ${String.format(Locale.US, "%.2f", state.liveAccelerometerMs2)} m/s²",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMain,
                        )
                        Text(
                            text = "${strings.detectionsLabel}: ${state.seismicPatternDetectedCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.seismicPatternDetectedCount > 0) DangerCrimson else colors.textSub,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (state.liveAccelerometerMs2 / 6.0f).coerceIn(0.03f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = if (state.liveAccelerometerMs2 >= 2.4f) DangerCrimson else colors.primary,
                        trackColor = colors.subtle,
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = strings.accelMonitorsDesc,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSub,
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
                            title = strings.pWaveLeadUpper,
                            value = String.format(Locale.US, "%04.1f", seismic.pWaveLeadSec),
                            unit = strings.secUnit,
                            unitColor = colors.textSub,
                            subtitle = strings.primaryWaveElapsed,
                            footerLeft = strings.statusLabel,
                            footerRight = strings.decayingLabel,
                            footerRightColor = colors.greenText,
                            icon = Icons.Rounded.Timer,
                            iconTint = DangerCrimson,
                            modifier = Modifier.weight(1f),
                        )
                        HazardMetricCard(
                            title = strings.sWaveShockUpper,
                            value = String.format(Locale.US, "%.1f", seismic.sWaveJma),
                            unit = "JMA",
                            unitColor = colors.sunset,
                            subtitle = strings.feltStrongIndoors,
                            footerLeft = strings.structuralLabel,
                            footerRight = strings.safeLabel,
                            footerRightColor = colors.greenText,
                            icon = Icons.Rounded.Vibration,
                            iconTint = colors.warning,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HazardMetricCard(
                            title = strings.tsunamiBuoyUpper,
                            value = String.format(Locale.US, "%.1f", seismic.tsunamiSurgeMeters),
                            unit = strings.mSurgeUnit,
                            unitColor = colors.textSub,
                            subtitle = strings.coastalMarineAdvisory,
                            footerLeft = strings.baysideLabel,
                            footerRight = strings.restrictedLabel,
                            footerRightColor = colors.sunset,
                            icon = Icons.Rounded.Waves,
                            iconTint = colors.primary,
                            modifier = Modifier.weight(1f),
                        )
                        HazardMetricCard(
                            title = strings.volcanicArrayUpper,
                            value = seismic.volcanicLevel,
                            unit = strings.normalLabel,
                            unitColor = colors.greenText,
                            subtitle = VeridiaLocalizer.volcanicNote(seismic.volcanicNote, strings),
                            footerLeft = strings.tiltmeterLabel,
                            footerRight = "${seismic.volcanicTiltUrad} µrad",
                            footerRightColor = colors.textMain,
                            icon = Icons.Rounded.Volcano,
                            iconTint = colors.accent,
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
                                text = strings.recentSeismicActivity,
                                style = MaterialTheme.typography.headlineSmall,
                                color = colors.textMain,
                            )
                        }
                        Text(
                            text = strings.past48HoursLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSub,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)

                    seismic.recentEvents.forEachIndexed { idx, event ->
                        val (dotColor, valueColor) = when (event.severity) {
                            SeismicSeverity.HIGH -> DangerCrimson to colors.dangerText
                            SeismicSeverity.MODERATE -> colors.warning to colors.sunset
                            SeismicSeverity.LOW -> colors.accent to colors.greenText
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
                                        color = colors.textMain,
                                    )
                                    Text(
                                        text = VeridiaLocalizer.seismicBannerSubtitle("Depth ${event.depthKm}km · ${event.timeAgoLabel}", strings).replace("Depth ", "${strings.depthLabel} "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSub,
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
                            HorizontalDivider(color = colors.border.copy(alpha = 0.6f))
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
    val colors = veridiaColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.subtle,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
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
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = unitColor,
                modifier = Modifier.padding(bottom = 6.dp),
            )
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

@Composable
private fun DisasterSirenChip(
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = veridiaColors
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
                color = colors.textMain,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.VolumeUp,
                contentDescription = veridiaStrings.testSirenCd,
                tint = accent,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SosEmergencyModalSheet(onDismiss: () -> Unit) {
    val colors = veridiaColors
    val strings = veridiaStrings
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
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
                        text = strings.emergencySafetyProtocol,
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textMain,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, strings.closeSosCd, tint = colors.textSub)
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.dangerBg,
                border = BorderStroke(1.dp, colors.dangerBorder),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = strings.immediateShakeResponse,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.dangerText,
                    )
                    Text(
                        text = strings.sosChecklistBody,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMain,
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
                    color = colors.subtle,
                    border = BorderStroke(1.dp, colors.border),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(strings.fireAmbulanceLabel, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
                        Text("119", style = MaterialTheme.typography.headlineMedium, color = colors.dangerText)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = colors.subtle,
                    border = BorderStroke(1.dp, colors.border),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(strings.policeRescueLabel, style = MaterialTheme.typography.labelSmall, color = colors.textSub)
                        Text("110", style = MaterialTheme.typography.headlineMedium, color = colors.primary)
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
                Text(strings.acknowledgeChecklistButton, color = Color.White)
            }
        }
    }
}

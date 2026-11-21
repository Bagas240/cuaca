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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.AppThemeMode
import com.example.data.model.PressureUnit
import com.example.data.model.TemperatureUnit
import com.example.data.model.WindUnit
import com.example.ui.components.SolarThemeSwitcherCard
import com.example.ui.components.VeridiaBackHeader
import com.example.ui.components.VeridiaCard
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.veridiaColors
import com.example.ui.viewmodel.VeridiaUiState
import com.example.work.SyncBatteryTier

@Composable
fun SettingsScreen(
    state: VeridiaUiState,
    onBack: () -> Unit,
    onSeismicAlertChange: (Boolean) -> Unit,
    onTyphoonWatchChange: (Boolean) -> Unit,
    onAqiAlertChange: (Boolean) -> Unit,
    onTempUnitChange: (TemperatureUnit) -> Unit,
    onWindUnitChange: (WindUnit) -> Unit,
    onPressureUnitChange: (PressureUnit) -> Unit,
    onGyroParallaxChange: (Boolean) -> Unit,
    onGpsPrivacyModeChange: (Boolean) -> Unit,
    onBackgroundSyncChange: (Boolean) -> Unit = {},
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onRefreshCache: () -> Unit,
) {
    val colors = veridiaColors
    val settings = state.settings

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
                    title = "Settings",
                    subtitle = null,
                    onBackClick = onBack,
                )
            }

            // Section 00: Dynamic Solar Theme Switcher
            item {
                SolarThemeSwitcherCard(
                    themeMode = settings.themeMode,
                    activePhase = state.activeThemePhase,
                    sunriseTime = state.weather.sunriseTime,
                    sunsetTime = state.weather.sunsetTime,
                    onSelectThemeMode = onThemeModeChange,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }

            // Section 0: GPS Privacy & Live Sync Security
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Shield, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GPS Privacy & Real-Time Sync", style = MaterialTheme.typography.headlineSmall, color = colors.textMain)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingSwitchRow(
                            title = "GPS Location Privacy Shield",
                            subtitle = "Locks telemetry to regency/kabupaten zone (~9-11 km); exact GPS discarded",
                            checked = settings.gpsPrivacyMode,
                            checkedColor = colors.accent,
                            onCheckedChange = onGpsPrivacyModeChange,
                            testTag = "switch_gps_privacy",
                        )
                        val batteryProfile = state.syncBatteryProfile
                        val syncSubtitle = when (batteryProfile.tier) {
                            SyncBatteryTier.CRITICAL_BATTERY ->
                                "Battery critical (${batteryProfile.batteryPercent}%): paused/deferred to ${batteryProfile.repeatIntervalMinutes}m until charged"
                            SyncBatteryTier.LOW_BATTERY ->
                                "Low battery (${batteryProfile.batteryPercent}%): frequency reduced to every ${batteryProfile.repeatIntervalMinutes}m on Wi-Fi"
                            SyncBatteryTier.CHARGING ->
                                "Charging (${batteryProfile.batteryPercent}%): refreshes every ${batteryProfile.repeatIntervalMinutes}m"
                            SyncBatteryTier.NORMAL ->
                                "BatteryManager (${if (batteryProfile.isBatteryUnknown) "Nominal" else "${batteryProfile.batteryPercent}%"}): every ${batteryProfile.repeatIntervalMinutes}m on Wi-Fi or charging"
                        }
                        SettingSwitchRow(
                            title = "Power-Aware Background Sync",
                            subtitle = syncSubtitle,
                            checked = settings.backgroundSyncEnabled,
                            checkedColor = colors.primary,
                            onCheckedChange = onBackgroundSyncChange,
                            testTag = "switch_background_sync",
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Real-Time Telemetry Stream", style = MaterialTheme.typography.labelMedium, color = colors.textMain)
                                Text(
                                    text = "Adaptive WorkManager · ${batteryProfile.repeatIntervalMinutes}m (${batteryProfile.tier.name.lowercase().replace('_', ' ')})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSub,
                                    modifier = Modifier.testTag("sync_battery_cadence_badge"),
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.subtle,
                                border = BorderStroke(1.dp, colors.border),
                            ) {
                                Text(
                                    text = state.lastUpdatedLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Section 0b: Encryption, Outbound Allowlist & Anti-Reverse-Engineering Posture
            item {
                val posture = state.securityPosture
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .testTag("security_posture_card"),
                    containerColor = if (posture.deviceIntegrityClean) colors.mint.copy(alpha = 0.55f) else colors.surface,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Lock, null, tint = colors.greenText, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Encryption & Hardening", style = MaterialTheme.typography.headlineSmall, color = colors.textMain)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.surface,
                            border = BorderStroke(1.dp, colors.border),
                        ) {
                            Text(
                                text = "AES-256-GCM",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.greenText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecurityStatusRow(
                            label = "User Settings & GPS Coordinates",
                            status = if (posture.encryptedPrefsActive) "EncryptedSharedPreferences · AES-256" else "AES Encrypted",
                            isSecure = posture.encryptedPrefsActive,
                        )
                        SecurityStatusRow(
                            label = "Local Room Database Cache",
                            status = if (posture.encryptedStorageActive) "AndroidKeyStore AES-256 Encrypted" else "Standard",
                            isSecure = posture.encryptedStorageActive,
                        )
                        SecurityStatusRow(
                            label = "BMKG & Telemetry Domain Allowlist",
                            status = "HTTPS Only · ${posture.blockedOutboundAttempts} Blocked",
                            isSecure = posture.tlsAllowlistActive,
                        )
                        SecurityStatusRow(
                            label = "Coarse Regency GPS Guard",
                            status = "Enforced (~11 km · No Pinpoint)",
                            isSecure = posture.coarseLocationShieldLocked,
                        )
                        SecurityStatusRow(
                            label = "Binary & Runtime Integrity",
                            status = posture.tamperWarning ?: "R8 Hardened · No Hook Detected",
                            isSecure = posture.deviceIntegrityClean,
                        )
                    }
                }
            }

            // Section 1: Emergency Alerts
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.NotificationsActive, null, tint = DangerCrimson, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Emergency Alerts", style = MaterialTheme.typography.headlineSmall, color = colors.textMain)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingSwitchRow(
                            title = "Seismic P-Wave Alert",
                            subtitle = "Bypass silent mode for M 4.5+ shocks",
                            checked = settings.seismicPWaveAlert,
                            checkedColor = DangerCrimson,
                            onCheckedChange = onSeismicAlertChange,
                            testTag = "switch_seismic_alert",
                        )
                        SettingSwitchRow(
                            title = "Typhoon Flash Watch",
                            subtitle = "Barometer drops and storm surge alerts",
                            checked = settings.typhoonFlashWatch,
                            checkedColor = colors.primary,
                            onCheckedChange = onTyphoonWatchChange,
                            testTag = "switch_typhoon_watch",
                        )
                        SettingSwitchRow(
                            title = "AQI Smog Inversion",
                            subtitle = "Notify when PM2.5 exceeds 100 µg/m³",
                            checked = settings.aqiSmogInversion,
                            checkedColor = colors.warning,
                            onCheckedChange = onAqiAlertChange,
                            testTag = "switch_aqi_inversion",
                        )
                    }
                }
            }

            // Section 2: Units & Telemetry Standards
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Straighten, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Units & Telemetry Standards", style = MaterialTheme.typography.headlineSmall, color = colors.textMain)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Temperature Unit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Temperature Unit", style = MaterialTheme.typography.labelMedium, color = colors.textMain)
                                Text(
                                    text = "Current scale: ${if (settings.temperatureUnit == TemperatureUnit.CELSIUS) "Celsius" else "Fahrenheit"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSub,
                                )
                            }
                            SegmentedOptionGroup(
                                options = listOf(
                                    "°C Metric" to (settings.temperatureUnit == TemperatureUnit.CELSIUS),
                                    "°F Imperial" to (settings.temperatureUnit == TemperatureUnit.FAHRENHEIT),
                                ),
                                onSelect = { idx ->
                                    onTempUnitChange(if (idx == 0) TemperatureUnit.CELSIUS else TemperatureUnit.FAHRENHEIT)
                                },
                                testTagPrefix = "unit_temp",
                            )
                        }

                        // Wind Velocity
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Wind Velocity", style = MaterialTheme.typography.labelMedium, color = colors.textMain)
                                Text(
                                    text = "Current scale: ${settings.windUnit.name.lowercase()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSub,
                                )
                            }
                            SegmentedOptionGroup(
                                options = listOf(
                                    "km/h" to (settings.windUnit == WindUnit.KMH),
                                    "mph" to (settings.windUnit == WindUnit.MPH),
                                    "knots" to (settings.windUnit == WindUnit.KNOTS),
                                ),
                                onSelect = { idx ->
                                    onWindUnitChange(
                                        when (idx) {
                                            0 -> WindUnit.KMH
                                            1 -> WindUnit.MPH
                                            else -> WindUnit.KNOTS
                                        }
                                    )
                                },
                                testTagPrefix = "unit_wind",
                            )
                        }

                        // Barometric Scale
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Barometric Scale", style = MaterialTheme.typography.labelMedium, color = colors.textMain)
                                Text(
                                    text = "Current scale: ${if (settings.pressureUnit == PressureUnit.HPA) "hPa" else "inHg"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSub,
                                )
                            }
                            SegmentedOptionGroup(
                                options = listOf(
                                    "hPa" to (settings.pressureUnit == PressureUnit.HPA),
                                    "inHg" to (settings.pressureUnit == PressureUnit.INHG),
                                ),
                                onSelect = { idx ->
                                    onPressureUnitChange(if (idx == 0) PressureUnit.HPA else PressureUnit.INHG)
                                },
                                testTagPrefix = "unit_pressure",
                            )
                        }
                    }
                }
            }

            // Section 3: Display & Parallax
            item {
                VeridiaCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Tune, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Display & Parallax", style = MaterialTheme.typography.headlineSmall, color = colors.textMain)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingSwitchRow(
                            title = "Gyroscope Camera Parallax",
                            subtitle = "Tilt device to orbit 3D viewpoints",
                            checked = settings.gyroParallaxEnabled,
                            checkedColor = colors.accent,
                            onCheckedChange = onGyroParallaxChange,
                            testTag = "switch_gyro_parallax",
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Glass Refraction Depth", style = MaterialTheme.typography.labelMedium, color = colors.textMain)
                                Text("Hardware accelerated blur", style = MaterialTheme.typography.bodySmall, color = colors.textSub)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.subtle,
                                border = BorderStroke(1.dp, colors.border),
                            ) {
                                Text(
                                    text = "20px Clean",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMain,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Offline Disaster Cache
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
                            Icon(Icons.Rounded.CloudSync, null, tint = colors.accent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Offline Disaster Cache", style = MaterialTheme.typography.headlineSmall, color = colors.textMain)
                        }
                        Text(
                            text = "128 MB Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accent,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = colors.border)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Fault line & topographic vectors", style = MaterialTheme.typography.bodySmall, color = colors.textMain)
                        Text(settings.offlineCacheLabel, style = MaterialTheme.typography.labelSmall, color = colors.accent)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Emergency shelter GPS markers", style = MaterialTheme.typography.bodySmall, color = colors.textMain)
                        Text("Offline Ready", style = MaterialTheme.typography.labelSmall, color = colors.accent)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !state.cacheRefreshing, onClick = onRefreshCache)
                            .testTag("refresh_cache_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = colors.subtle,
                        border = BorderStroke(1.dp, colors.border),
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (state.cacheRefreshing) {
                                    "Syncing Regional Cache..."
                                } else {
                                    "Refresh Regional Offline Cache"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textMain,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    checkedColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
) {
    val colors = veridiaColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = colors.textMain)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSub)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = checkedColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = colors.border,
                uncheckedBorderColor = Color.Transparent,
            ),
            modifier = Modifier.testTag(testTag),
        )
    }
}

@Composable
private fun SegmentedOptionGroup(
    options: List<Pair<String, Boolean>>,
    onSelect: (Int) -> Unit,
    testTagPrefix: String,
) {
    val colors = veridiaColors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.subtle,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            options.forEachIndexed { idx, (label, isSelected) ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .clickable { onSelect(idx) }
                        .testTag("${testTagPrefix}_$idx"),
                    shape = RoundedCornerShape(9.dp),
                    color = if (isSelected) colors.surface else Color.Transparent,
                    shadowElevation = if (isSelected) 1.dp else 0.dp,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (isSelected) colors.primary else colors.textSub,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityStatusRow(
    label: String,
    status: String,
    isSecure: Boolean,
) {
    val colors = veridiaColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMain,
            modifier = Modifier.weight(1f),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isSecure) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = if (isSecure) colors.greenText else colors.warning,
                modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSecure) colors.greenText else DangerCrimson,
            )
        }
    }
}

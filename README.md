<div align="center">

# Veridia

**Atmospheric Weather Intelligence, Regional Air Quality, Live Doppler Radar & Natural Disaster Early Warning for Android**

[![Android Build APK](https://img.shields.io/badge/Android-SDK%2024%E2%80%9336-3DDC84?logo=android&logoColor=white)](./app/build.gradle.kts)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x%20%2B%20Compose%20M3-7F52FF?logo=kotlin&logoColor=white)](./gradle/libs.versions.toml)
[![BMKG Open Data](https://img.shields.io/badge/Data%20Source-BMKG%20Official%20API-0055A4)](https://data.bmkg.go.id/)
[![WorkManager EWS](https://img.shields.io/badge/Background%20EWS-WorkManager-4285F4)](./app/src/main/java/com/example/work/WeatherAlertWorker.kt)
[![Encryption](https://img.shields.io/badge/Storage-AES--256--GCM-1E8E3E)](./app/src/main/java/com/example/data/security/TelemetryCrypto.kt)

</div>

---

## Overview

**Veridia** is a privacy-hardened Android weather and disaster early-warning application built with **Kotlin** and **Jetpack Compose (Material Design 3)**. It combines hyperlocal Indonesian meteorological and geophysical feeds from **BMKG (Badan Meteorologi, Klimatologi, dan Geofisika)** with global telemetry from **Open-Meteo**, **Copernicus CAMS**, **USGS**, and **RainViewer**.

Veridia continuously evaluates live atmospheric, particulate, and seismic feeds in both foreground and background (**WorkManager**) to dispatch urgent local push notifications, civil-defense acoustic sirens, and offline-cached emergency guidance without exposing exact user GPS coordinates.

---

## Table of Contents

- [Key Features](#key-features)
- [Data Sources, API Credits & BMKG Attribution](#data-sources-api-credits--bmkg-attribution)
- [Architecture & Project Structure](#architecture--project-structure)
- [Background Alert Service (WorkManager)](#background-alert-service-workmanager)
- [Privacy & Security Architecture](#privacy--security-architecture)
- [Tech Stack](#tech-stack)
- [Getting Started & Local Build](#getting-started--local-build)
- [GitHub Actions CI/CD (Automated APK Builds)](#github-actions-cicd-automated-apk-builds)
- [Testing](#testing)
- [Permissions](#permissions)
- [Disclaimer](#disclaimer)

---

## Key Features

### 1. Atmospheric Home Dashboard
- **Real-time telemetry**: Current temperature, feels-like temperature, relative humidity, barometric pressure, UV index, and wind speed/gusts/direction (`WindSpeedDirectionIndicator`).
- **Dynamic visual canvases & AVDs**: Interactive street-POV atmospheric canvas (`HomeStreetPovCanvas`), solar arc cycle (`SunCycleCard`), and animated vector drawables (`avd_weather_sun_cloud`, `avd_wind_sway`, `avd_aqi_pulse`) synchronized with navigation lifecycle state (`NavStateController`).
- **24-Hour & 7-Day Outlooks**: Scrollable hourly temperature/precipitation slots and weekly daily summaries.
- **City Search & Protected GPS**: Preset Indonesian and international cities, Open-Meteo geocoding search, and coarse-zone GPS lookup.

### 2. Detailed Weather & Interactive Station Map
- **Atmospheric Strata View**: Switch between Surface, Mid-Cloud, and Upper Stratum telemetry layers.
- **BMKG 12-Station Indonesia Map**: Interactive map plotting live conditions, precipitation, wind vectors, and temperatures across 12 major BMKG administrative stations (Jakarta, Bandung, Semarang, Yogyakarta, Surabaya, Denpasar, Medan, Palembang, Makassar, Balikpapan, Manado, Jayapura).

### 3. Regional Air Quality Monitor (9-Node Grid)
- **Copernicus CAMS Spatial Grid**: Evaluates a 3x3 regional sector grid (`NW`, `N`, `NE`, `W`, `Center`, `E`, `SW`, `S`, `SE`) around the selected city.
- **Pollutant Breakdown**: US AQI, `PM2.5`, `PM10`, `CO`, and `NO2` metrics with severity classifications and district highlights.

### 4. Live Doppler Weather Radar
- **RainViewer Integration**: Live precipitation radar tile overlay with timeline playback scrubbing (`-60m` to `+30m`), storm cell vectors, echo core dBZ, and satellite cloud top indicators.

### 5. Natural Disaster Monitor & Civil-Defense EWS
- **BMKG TEWS Earthquake & Tsunami Monitoring**: Displays latest felt earthquakes, M 5.0+ activity, MMI/Shindo intensity, depth, tsunami potential (`Tidak Berpotensi` / `Waspada Tsunami`), and official BMKG **Shakemap** imagery.
- **On-Device Accelerometer Seismic Detector**: `SeismicAccelerometerDetector` monitors real-time accelerometer oscillation patterns (`>= 2.4 m/s²` with axis reversals) to detect sustained seismic shaking while ignoring single taps.
- **Civil-Defense Acoustic Siren**: `DisasterSirenPlayer` synthesizes smooth PCM civil-defense warning sweeps (`AudioTrack` on `USAGE_ALARM`) paired with tactile vibration patterns.

---

## Data Sources, API Credits & BMKG Attribution

> ### Official Credit — BMKG (Badan Meteorologi, Klimatologi, dan Geofisika)
>
> Veridia relies on official meteorological and geophysical open data published by **BMKG (Badan Meteorologi, Klimatologi, dan Geofisika Republik Indonesia)**.
>
> - **Weather Forecast API**: `https://api.bmkg.go.id/publik/prakiraan-cuaca?adm4={kode_adm4}`
> - **Earthquake & Tsunami Early Warning (TEWS) API**:
>   - `https://data.bmkg.go.id/DataMKG/TEWS/autogempa.json` (Latest automatic earthquake)
>   - `https://data.bmkg.go.id/DataMKG/TEWS/gempadirasakan.json` (Felt earthquakes)
>   - `https://data.bmkg.go.id/DataMKG/TEWS/gempaterkini.json` (Recent M 5.0+ earthquakes)
>   - `https://data.bmkg.go.id/DataMKG/TEWS/{Shakemap}` (Official BMKG Shakemap images)
>
> #### BMKG API Key & Access Policy
> - **No Private API Key Required**: BMKG's public Open Data endpoints (`api.bmkg.go.id` and `data.bmkg.go.id`) are provided openly by BMKG without requiring an API key, client secret, or bearer token.
> - **Mandatory Attribution**: In accordance with BMKG's open-data usage guidelines, **BMKG** is explicitly credited both in this repository and inside the Veridia user interface (`Source: BMKG (api.bmkg.go.id)` and `Source: BMKG TEWS (data.bmkg.go.id)`).
> - **Fair Use & Official Integrity**: All meteorological forecasts, ADM4 regional mappings, earthquake parameters, tsunami advisories, and Shakemap graphics remain the intellectual property and official public service of **BMKG Indonesia**. Do not remove BMKG source labels or misrepresent unofficial estimates as official BMKG advisories.
> - **Optional Custom Proxy / Environment Keys**: If you fork this project to route requests through a private BMKG gateway or authenticated proxy, place custom variables in a local `.env` file (excluded from Git; see `.env.example`), which is wired via the **Secrets Gradle Plugin**.

### Additional Public Data & Map Providers

| Provider | Endpoint / Service | Purpose | API Key Required? |
|---|---|---|---|
| **BMKG Indonesia** | `api.bmkg.go.id` / `data.bmkg.go.id` | Indonesia ADM4 weather forecasts, TEWS earthquakes, tsunami status & Shakemaps | **No** (Open Public Data with Attribution) |
| **Open-Meteo** | `api.open-meteo.com` | Global weather fallback, 24h/7d forecasts, UV & barometric pressure | **No** (Non-commercial open access) |
| **Open-Meteo / Copernicus CAMS** | `air-quality-api.open-meteo.com` | 9-node regional AQI, PM2.5, PM10, CO, NO2 | **No** |
| **Open-Meteo Geocoding** | `geocoding-api.open-meteo.com` | Global city coordinate lookup | **No** |
| **USGS Earthquake Hazards** | `earthquake.usgs.gov` | GeoJSON seismic fallback feed when BMKG TEWS is unreachable | **No** |
| **RainViewer** | `api.rainviewer.com` / `tilecache.rainviewer.com` | Live Doppler precipitation radar tiles | **No** |
| **CARTO / Esri / OpenStreetMap** | Raster map tiles | Base map rendering across Weather, AQI, Radar, and Shakemap views | **No** |

---

## Architecture & Project Structure

Veridia follows an **MVVM + Repository** architecture with unidirectional state flow (`StateFlow<VeridiaUiState>`), encrypted Room persistence, and lifecycle-aware Compose UI:

```text
app/src/main/java/com/example/
├── MainActivity.kt                        # Edge-to-edge host, NavHost, notification deep-linking, runtime permissions
├── alerts/
│   ├── DisasterNotificationManager.kt     # Urgent alert evaluator, persistent deduplication & high-priority push notifications
│   ├── DisasterSirenPlayer.kt             # PCM AudioTrack civil-defense siren & haptic waveforms
│   └── SeismicAccelerometerDetector.kt    # Oscillatory tremor detection from hardware accelerometer
├── data/
│   ├── local/                             # Room database (VeridiaDatabase, TelemetryCacheDao, Entities)
│   ├── location/                          # Fused/System LocationTracker & coarse coordinate masking
│   ├── model/                             # Immutable domain models (Weather, AQI, Seismic, Radar, UserSettings)
│   ├── network/                           # Retrofit & Moshi DTOs for BMKG, Open-Meteo, USGS, RainViewer
│   ├── preferences/                       # EncryptedSharedPreferences (SettingsDataStore, SecurePreferencesProvider)
│   ├── repository/                        # VeridiaRepository (concurrent API orchestration & encrypted Room caching)
│   └── security/                          # SecurityGuard domain allowlist, tamper checks & AES-256-GCM TelemetryCrypto
├── ui/
│   ├── components/                        # Reusable M3 cards, 2D maps, RainViewer/BMKG overlays, AVD controllers
│   ├── navigation/                        # NavStateController (pauses animations off-screen / backgrounded)
│   ├── screens/                           # HomeDashboard, DetailedWeather, AirQualityMap, WeatherRadar, DisasterMonitor, Settings
│   ├── theme/                             # Material 3 ColorScheme, Typography & custom font family
│   └── viewmodel/                         # VeridiaViewModel (telemetry polling, sensors, WorkManager sync)
└── work/
    ├── WeatherAlertWorker.kt              # Dedicated periodic/on-demand worker for urgent weather & seismic push alerts
    ├── WeatherSyncWorker.kt               # Power-aware periodic cache sync & hazard evaluation
    └── WeatherSyncScheduler.kt            # WorkManager constraint & battery-tier scheduler
```

---

## Background Alert Service (WorkManager)

Veridia runs reliable, battery-conscious background monitoring via **AndroidX WorkManager**:

1. **Three Complementary Unique Work Channels (`WeatherSyncScheduler`)**:
   - `veridia_weather_alerts_periodic` (`WeatherAlertWorker`): Runs every **15 minutes** (scales down to **60m** on low battery `<= 20%` and **180m** on critical battery `<= 10%`) under `NetworkType.CONNECTED` + `requiresBatteryNotLow = true` so urgent alerts are delivered over both Wi-Fi and cellular networks.
   - `veridia_weather_sync_wifi` (`WeatherSyncWorker`): Unmetered Wi-Fi sync (`NetworkType.UNMETERED` + `requiresBatteryNotLow = true`).
   - `veridia_weather_sync_charging` (`WeatherSyncWorker`): Charging sync (`NetworkType.CONNECTED` + `requiresCharging = true`).
   - `veridia_weather_alerts_now` (`OneTimeWorkRequest`): On-demand expedited alert scan triggered from **Disaster Monitor → Scan Now**.

2. **Urgent Event Thresholds (`DisasterNotificationManager`)**:
   - **Tsunami Warning**: Triggered when BMKG `Potensi` indicates `"Waspada"` or `"Berpotensi"`.
   - **Earthquake Alert**: Triggered for `M 4.5+` events within `380 km` of the user's zone or any regional `M 5.5+` event.
   - **Severe Storm & Gale Alert**: Triggered for active thunderstorms, sustained wind `>= 48 km/h`, damaging gusts `>= 65 km/h`, deep tropical trough downpours (`precip >= 85%` and `pressure <= 1004 hPa`), or imminent severe storm cells within the next 3 hours.
   - **Hazardous Air Emergency**: Triggered when `PM2.5 >= 100.0 µg/m³` or regional `AQI >= 151`.

3. **Persistent Deduplication & Deep-Linking**:
   - Alert signatures and timestamps are persisted across background worker invocations (with a 6-hour cooldown per identical signature) so users are never spammed by repeated polls while still receiving distinct or recurring urgent alerts.
   - Notifications use distinct per-hazard IDs on a high-importance channel (`veridia_disaster_ews_v1`) and deep-link directly to the **Disaster Monitor** screen when tapped.

---

## Privacy & Security Architecture

- **Coarse Location Privacy Shield (`LocationMaskingUtility`)**: Exact GPS coordinates are rounded (`~9–11 km` regency/kabupaten precision) before any API request or local storage write.
- **AES-256-GCM Encryption at Rest (`TelemetryCrypto` & `SecurePreferencesProvider`)**:
  - User preferences and masked GPS coordinates are stored in Jetpack `EncryptedSharedPreferences` (`AES256_SIV` keys + `AES256_GCM` values).
  - Cached weather, air quality, and seismic JSON payloads in Room (`VeridiaDatabase`) are encrypted using AndroidKeyStore-backed `AES-256-GCM`.
- **Outbound Network Allowlist (`SecurityGuard` / `NetworkSecurityInterceptor`)**: All OkHttp traffic is restricted to HTTPS connections on an explicit allowlist of verified meteorological and geophysical hosts (`api.bmkg.go.id`, `data.bmkg.go.id`, `api.open-meteo.com`, `air-quality-api.open-meteo.com`, `geocoding-api.open-meteo.com`, `earthquake.usgs.gov`, `api.rainviewer.com`, `tilecache.rainviewer.com`).
- **Release Hardening (`proguard-rules.pro`)**: R8 code shrinking, resource shrinking, and verbose `Log` stripping enabled for release builds.

---

## Tech Stack

| Layer | Technologies |
|---|---|
| **Language & UI** | Kotlin, Jetpack Compose, Material Design 3, Compose Animation Graphics (AVD) |
| **Navigation & State** | Navigation Compose, Lifecycle `StateFlow`, `collectAsStateWithLifecycle` |
| **Concurrency & Background** | Kotlin Coroutines, `WorkManager` (`work-runtime-ktx`) |
| **Networking & Serialization** | Retrofit 2, OkHttp 4, Moshi (KSP codegen) |
| **Persistence & Security** | Room (KSP), Jetpack Security Crypto (`EncryptedSharedPreferences`), AndroidKeyStore |
| **Maps & Location** | OSMDroid, Coil, Google Play Services Location |
| **Testing** | JUnit 4, Robolectric (SDK 36), Compose UI Test, `androidx.work:work-testing` |

---

## Getting Started & Local Build

### Prerequisites
- **JDK 21** (Eclipse Temurin recommended)
- **Android SDK 36** (`compileSdk = 36`, Build-Tools `36.0.0`)
- **Gradle 9.3.1** (included via `./gradlew`)

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Bagas240/cuaca.git
   cd cuaca
   ```

2. **Environment Configuration**:
   `.env.example` is already provided for the Secrets Gradle Plugin. No API keys are required out of the box because BMKG, Open-Meteo, USGS, and RainViewer endpoints are publicly accessible. If you have custom overrides, copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

3. **Prepare the debug keystore** (if building from a fresh clone where `debug.keystore` is gitignored):
   ```bash
   base64 -d debug.keystore.base64 > debug.keystore
   ```

4. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   Output APK path:
   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

5. **Build a Signed Release APK** (optional):
   Provide your upload keystore (`keyAlias = "upload"`):
   ```bash
   export KEYSTORE_PATH="/path/to/my-upload-key.jks"
   export STORE_PASSWORD="your_store_password"
   export KEY_PASSWORD="your_key_password"
   ./gradlew assembleRelease
   ```
   Output APK path:
   ```text
   app/build/outputs/apk/release/app-release.apk
   ```

---

## GitHub Actions CI/CD (Automated APK Builds)

This repository includes a pre-configured workflow at [`.github/workflows/build-apk.yml`](./.github/workflows/build-apk.yml).

### Triggers
- Every `push` or `pull_request` targeting `main` or `master`
- Every version tag push matching `v*` (e.g., `v1.0.0`) — automatically creates a **GitHub Release** with the APK attached
- Manual runs anytime via **GitHub → Actions → Build Android APK → Run workflow**

### Downloading Built APKs
1. Open the **Actions** tab in your GitHub repository.
2. Select the latest **Build Veridia APK** run.
3. Download **`veridia-debug-apk`** from the **Artifacts** section.

### Optional: Enabling Signed Release APKs on GitHub
Add the following **Repository Secrets** (*Settings → Secrets and variables → Actions*):
- `KEYSTORE_BASE64`: Base64-encoded `.jks` keystore file containing key alias `upload`
- `STORE_PASSWORD`: Keystore password
- `KEY_PASSWORD`: Key password for alias `upload`

When all three secrets are present, the workflow automatically builds and uploads **`veridia-release-apk`**.

---

## Testing

Veridia uses local JVM unit and **Robolectric** tests covering telemetry formatting, GPS privacy masking, AES-256-GCM encryption, Room offline caching, accelerometer seismic pattern recognition, Compose UI navigation/animations, WorkManager constraints, and urgent push notification deduplication:

```bash
./gradlew :app:testDebugUnitTest
```

---

## Permissions

Declared in [`AndroidManifest.xml`](./app/src/main/AndroidManifest.xml) following least-privilege principles:

| Permission | Purpose |
|---|---|
| `INTERNET` & `ACCESS_NETWORK_STATE` | Fetch live BMKG, Open-Meteo, USGS, and RainViewer telemetry |
| `ACCESS_COARSE_LOCATION` | Approximate regional location (~9–11 km masked zone; exact GPS is never requested) |
| `POST_NOTIFICATIONS` | Display urgent weather, earthquake, tsunami, and hazardous air push notifications (Android 13+ runtime prompt) |
| `VIBRATE` | Tactile alert waveforms for emergency notifications and civil-defense sirens |

---

## Disclaimer

Weather, air quality, and seismic/tsunami early warning information in Veridia is provided for **informational and situational awareness purposes**. Always follow official instructions issued by **BMKG (Badan Meteorologi, Klimatologi, dan Geofisika)**, **BNPB**, and local emergency authorities during natural disasters.

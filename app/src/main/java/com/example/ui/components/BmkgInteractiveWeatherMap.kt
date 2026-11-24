package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.location.LocationMaskingUtility
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherMapLayer
import com.example.data.model.WeatherMapStation
import com.example.ui.i18n.VeridiaLocalizer
import com.example.ui.i18n.veridiaStrings
import com.example.ui.theme.SpaceMonoFamily
import com.example.ui.theme.veridiaColors
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.unit.Dp

fun configureOsmdroidRuntime(context: Context) {
    OsmdroidRuntimeConfig.ensureConfigured(context)
}

private object OsmdroidRuntimeConfig {
    @Volatile
    private var configured = false

    fun ensureConfigured(context: Context) {
        if (configured) return
        synchronized(this) {
            if (configured) return
            val appCtx = context.applicationContext
            val prefs = appCtx.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE)
            val config = Configuration.getInstance()
            config.load(appCtx, prefs)
            config.userAgentValue = appCtx.packageName.ifBlank { "com.aistudio.veridia.wxqmpk" }
            val baseDir = File(appCtx.cacheDir, "osmdroid").apply { mkdirs() }
            val tileDir = File(baseDir, "tiles").apply { mkdirs() }
            config.osmdroidBasePath = baseDir
            config.osmdroidTileCache = tileDir
            configured = true
        }
    }
}

private class RainViewerRadarTileSource(private val prefix: String) : OnlineTileSourceBase(
    "RainViewer_${prefix.hashCode()}",
    0,
    14,
    256,
    ".png",
    arrayOf(prefix),
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "$prefix/256/$zoom/$x/$y/2/1_1.png"
    }
}

private class BmkgPrecipitationOverlay(
    private val density: Float,
    private val onSelectStation: (String) -> Unit,
) : Overlay() {

    var stations: List<WeatherMapStation> = emptyList()
    var activeLayer: WeatherMapLayer = WeatherMapLayer.RAIN_RADAR
    var selectedStationId: String = ""
    var tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS

    private val screenPoint = Point()
    private val hitBoxes = mutableListOf<Pair<RectF, String>>()

    private val plumePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    private val vectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(37, 99, 235)
        strokeWidth = 2.6f * density
        strokeCap = Paint.Cap.ROUND
    }
    private val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val pillDotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pillTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10.5f * density
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val projection = mapView.projection ?: return
        val zoom = mapView.zoomLevelDouble
        val zoomScale = (zoom / 6.0).toFloat().coerceIn(0.65f, 1.9f)

        hitBoxes.clear()

        // Pass 1: Draw BMKG real-time precipitation plumes / cloud halos / wind vectors
        stations.forEach { st ->
            projection.toPixels(GeoPoint(st.latitude, st.longitude), screenPoint)
            val sx = screenPoint.x.toFloat()
            val sy = screenPoint.y.toFloat()
            if (sx < -220f || sx > mapView.width + 220f || sy < -220f || sy > mapView.height + 220f) {
                return@forEach
            }

            when (activeLayer) {
                WeatherMapLayer.RAIN_RADAR -> {
                    val hasPrecip = st.precipMm > 0.1 || st.precipChancePercent >= 40
                    if (hasPrecip) {
                        val colorRgb = precipColorArgb(st.precipMm, st.precipChancePercent)
                        val intensityFactor = ((st.precipMm / 12.0).toFloat() + (st.precipChancePercent / 100f) * 0.6f)
                            .coerceIn(0.35f, 1.45f)
                        val radiusPx = (34f + intensityFactor * 28f) * density * zoomScale

                        plumePaint.shader = RadialGradient(
                            sx,
                            sy,
                            radiusPx,
                            intArrayOf(
                                withAlpha(colorRgb, 138),
                                withAlpha(colorRgb, 58),
                                android.graphics.Color.TRANSPARENT,
                            ),
                            floatArrayOf(0f, 0.55f, 1f),
                            Shader.TileMode.CLAMP,
                        )
                        canvas.drawCircle(sx, sy, radiusPx, plumePaint)
                        plumePaint.shader = null

                        if (st.precipMm >= 2.0 || st.precipChancePercent >= 65) {
                            ringPaint.color = withAlpha(colorRgb, 140)
                            canvas.drawCircle(sx, sy, radiusPx * 0.55f, ringPaint)
                        }
                    }
                }
                WeatherMapLayer.CLOUDS_TEMP -> {
                    val radiusPx = (28f + st.cloudCoverPercent * 0.25f) * density * zoomScale
                    val cloudColor = android.graphics.Color.rgb(71, 85, 105)
                    plumePaint.shader = RadialGradient(
                        sx,
                        sy,
                        radiusPx,
                        intArrayOf(
                            withAlpha(cloudColor, (st.cloudCoverPercent * 1.1f).toInt().coerceIn(30, 130)),
                            withAlpha(cloudColor, 32),
                            android.graphics.Color.TRANSPARENT,
                        ),
                        floatArrayOf(0f, 0.6f, 1f),
                        Shader.TileMode.CLAMP,
                    )
                    canvas.drawCircle(sx, sy, radiusPx, plumePaint)
                    plumePaint.shader = null
                }
                WeatherMapLayer.WIND_VECTOR -> {
                    val rad = Math.toRadians(st.windBearingDeg.toDouble())
                    val lenPx = ((18f + st.windSpeedKmh * 0.75f) * density * zoomScale.coerceAtMost(1.3f))
                    val tipX = sx + (sin(rad) * lenPx).toFloat()
                    val tipY = sy - (cos(rad) * lenPx).toFloat()
                    canvas.drawLine(sx, sy, tipX, tipY, vectorPaint)
                    canvas.drawCircle(tipX, tipY, 3.5f * density, vectorPaint)
                }
            }
        }

        // Pass 2: Draw interactive BMKG station badges on top of plumes
        stations.forEach { st ->
            projection.toPixels(GeoPoint(st.latitude, st.longitude), screenPoint)
            val sx = screenPoint.x.toFloat()
            val sy = screenPoint.y.toFloat()
            if (sx < -80f || sx > mapView.width + 80f || sy < -40f || sy > mapView.height + 40f) {
                return@forEach
            }

            val isSelected = st.id == selectedStationId
            val accentColor = precipColorArgb(st.precipMm, st.precipChancePercent)
            val code = st.name.take(3).uppercase(Locale.US)
            val label = when (activeLayer) {
                WeatherMapLayer.RAIN_RADAR -> {
                    if (st.precipMm >= 0.1) {
                        "$code ${String.format(Locale.US, "%.1f", st.precipMm)}mm"
                    } else {
                        "$code ${st.precipChancePercent}%"
                    }
                }
                WeatherMapLayer.CLOUDS_TEMP -> "$code ${formatTempString(st.tempC, tempUnit)}"
                WeatherMapLayer.WIND_VECTOR -> "$code ${st.windSpeedKmh}km/h"
            }

            val textW = pillTextPaint.measureText(label)
            val padH = 8f * density
            val dotRadius = 3.5f * density
            val gap = 5f * density
            val pillW = padH * 2 + dotRadius * 2 + gap + textW
            val pillH = 22f * density

            val rect = RectF(
                sx - pillW / 2f,
                sy - pillH / 2f,
                sx + pillW / 2f,
                sy + pillH / 2f,
            )

            pillBgPaint.color = if (isSelected) {
                android.graphics.Color.rgb(15, 23, 42)
            } else {
                android.graphics.Color.argb(244, 255, 255, 255)
            }
            pillBorderPaint.color = accentColor
            pillBorderPaint.strokeWidth = (if (isSelected) 2f else 1.4f) * density

            val corner = 9f * density
            canvas.drawRoundRect(rect, corner, corner, pillBgPaint)
            canvas.drawRoundRect(rect, corner, corner, pillBorderPaint)

            pillDotPaint.color = accentColor
            val dotCx = rect.left + padH + dotRadius
            val dotCy = rect.centerY()
            canvas.drawCircle(dotCx, dotCy, dotRadius, pillDotPaint)

            pillTextPaint.color = if (isSelected) {
                android.graphics.Color.WHITE
            } else {
                android.graphics.Color.rgb(15, 23, 42)
            }
            val fm = pillTextPaint.fontMetrics
            val textY = rect.centerY() - (fm.ascent + fm.descent) / 2f
            canvas.drawText(label, dotCx + dotRadius + gap, textY, pillTextPaint)

            // Expand hit target to at least 44dp for easy tapping
            val minHitHalf = 22f * density
            val hitRect = RectF(
                minOf(rect.left, sx - minHitHalf),
                minOf(rect.top, sy - minHitHalf),
                maxOf(rect.right, sx + minHitHalf),
                maxOf(rect.bottom, sy + minHitHalf),
            )
            hitBoxes.add(hitRect to st.id)
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
        val x = e.x
        val y = e.y
        for (i in hitBoxes.indices.reversed()) {
            val (rect, stationId) = hitBoxes[i]
            if (rect.contains(x, y)) {
                onSelectStation(stationId)
                mapView.invalidate()
                return true
            }
        }
        return false
    }

    private fun precipColorArgb(precipMm: Double, precipChance: Int): Int = when {
        precipMm >= 7.0 -> android.graphics.Color.rgb(220, 38, 38)   // Heavy storm crimson
        precipMm >= 2.5 -> android.graphics.Color.rgb(245, 158, 11)  // Moderate/strong amber
        precipMm >= 0.4 || precipChance >= 55 -> android.graphics.Color.rgb(2, 132, 199) // Rain cobalt
        else -> android.graphics.Color.rgb(56, 189, 248)             // Light mist sky
    }

    private fun withAlpha(rgbColor: Int, alpha: Int): Int {
        return android.graphics.Color.argb(
            alpha.coerceIn(0, 255),
            android.graphics.Color.red(rgbColor),
            android.graphics.Color.green(rgbColor),
            android.graphics.Color.blue(rgbColor),
        )
    }
}

/**
 * Dedicated scrollable, pinch-zoomable interactive weather map built with the open-source
 * osmdroid (OpenStreetMap) library, overlaying real-time BMKG precipitation intensity plumes,
 * station readouts, and optional RainViewer Doppler radar tiles.
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun BmkgInteractiveWeatherMap(
    centerLat: Double,
    centerLon: Double,
    stations: List<WeatherMapStation>,
    rainViewerTilePrefix: String?,
    activeLayer: WeatherMapLayer,
    tempUnit: TemperatureUnit,
    lastUpdatedLabel: String,
    isSyncing: Boolean,
    onSelectLayer: (WeatherMapLayer) -> Unit,
    onRefreshNow: () -> Unit,
    modifier: Modifier = Modifier,
    mapHeight: Dp = 340.dp,
    onExpandRadar: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = context.resources.displayMetrics.density
    val colors = veridiaColors
    val strings = veridiaStrings

    configureOsmdroidRuntime(context)

    var selectedStationId by remember {
        mutableStateOf(stations.firstOrNull()?.id ?: "")
    }
    LaunchedEffect(stations) {
        if (stations.isNotEmpty() && stations.none { it.id == selectedStationId }) {
            selectedStationId = stations.first().id
        }
    }
    var currentZoom by remember { mutableDoubleStateOf(5.8) }
    var programmaticTargetZoom by remember { mutableStateOf<Double?>(null) }

    val selectedStation = stations.firstOrNull { it.id == selectedStationId } ?: stations.firstOrNull()

    val precipitationOverlay = remember(density) {
        BmkgPrecipitationOverlay(density = density) { clickedId ->
            selectedStationId = clickedId
        }
    }

    var activeRadarTilesOverlay by remember { mutableStateOf<TilesOverlay?>(null) }
    var activeRadarPrefix by remember { mutableStateOf<String?>(null) }

    val maskedCenter = remember(centerLat, centerLon) {
        LocationMaskingUtility.maskCoordinates(centerLat, centerLon, enabled = true)
    }
    val mapCenterLat = maskedCenter.latitude
    val mapCenterLon = maskedCenter.longitude

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isHorizontalMapRepetitionEnabled = true
            isVerticalMapRepetitionEnabled = false
            minZoomLevel = 3.5
            maxZoomLevel = 15.0
            controller.setZoom(5.8)
            controller.setCenter(GeoPoint(mapCenterLat, mapCenterLon))

            // Disallow parent LazyColumn scroll interception while panning or zooming the map
            setOnTouchListener { v, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_POINTER_DOWN,
                    MotionEvent.ACTION_MOVE,
                    -> v.parent?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL,
                    -> v.parent?.requestDisallowInterceptTouchEvent(false)
                }
                false
            }

            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean = false
                override fun onZoom(event: ZoomEvent?): Boolean {
                    val zoom = event?.zoomLevel ?: return false
                    val target = programmaticTargetZoom
                    if (target != null) {
                        if (kotlin.math.abs(zoom - target) <= 0.15) {
                            programmaticTargetZoom = null
                            currentZoom = target
                        }
                        return false
                    }
                    currentZoom = zoom
                    return false
                }
            })
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> runCatching { mapView.onResume() }
                Lifecycle.Event.ON_PAUSE -> runCatching { mapView.onPause() }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            runCatching { mapView.onDetach() }
        }
    }

    // Apply dark-mode tile filter when Night theme is active so base OSM tiles harmonize
    LaunchedEffect(colors.isDark) {
        mapView.overlayManager.tilesOverlay.setColorFilter(
            if (colors.isDark) TilesOverlay.INVERT_COLORS else null,
        )
        mapView.invalidate()
    }

    // Recenter when user selects a different city (using privacy-masked coordinates)
    LaunchedEffect(mapCenterLat, mapCenterLon) {
        programmaticTargetZoom = 6.2
        currentZoom = 6.2
        mapView.controller.animateTo(GeoPoint(mapCenterLat, mapCenterLon), 6.2, 420L)
    }

    // Keep BMKG precipitation overlay & optional RainViewer tile layer synchronized
    LaunchedEffect(stations, activeLayer, selectedStationId, tempUnit, rainViewerTilePrefix) {
        precipitationOverlay.stations = stations
        precipitationOverlay.activeLayer = activeLayer
        precipitationOverlay.selectedStationId = selectedStationId
        precipitationOverlay.tempUnit = tempUnit

        val showRadarTiles = activeLayer == WeatherMapLayer.RAIN_RADAR && !rainViewerTilePrefix.isNullOrBlank()
        if (showRadarTiles && rainViewerTilePrefix != activeRadarPrefix) {
            activeRadarTilesOverlay?.let { mapView.overlays.remove(it) }
            val provider = MapTileProviderBasic(context, RainViewerRadarTileSource(rainViewerTilePrefix!!))
            val radarOverlay = TilesOverlay(provider, context).apply {
                loadingBackgroundColor = android.graphics.Color.TRANSPARENT
                loadingLineColor = android.graphics.Color.TRANSPARENT
            }
            activeRadarTilesOverlay = radarOverlay
            activeRadarPrefix = rainViewerTilePrefix
            mapView.overlays.add(0, radarOverlay)
        } else if (!showRadarTiles && activeRadarTilesOverlay != null) {
            mapView.overlays.remove(activeRadarTilesOverlay)
            activeRadarTilesOverlay = null
            activeRadarPrefix = null
        }

        if (!mapView.overlays.contains(precipitationOverlay)) {
            mapView.overlays.add(precipitationOverlay)
        }
        mapView.invalidate()
    }

    val minZoom = 3.5
    val maxZoom = 15.0
    val canZoomIn = currentZoom < maxZoom - 0.1
    val canZoomOut = currentZoom > minZoom + 0.1

    fun applyMapZoom(targetZoom: Double) {
        val clamped = targetZoom.coerceIn(minZoom, maxZoom)
        programmaticTargetZoom = clamped
        currentZoom = clamped
        runCatching {
            mapView.controller.animateTo(mapView.mapCenter, clamped, 220L)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Layer Tabs + Manual Live Refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    WeatherMapLayer.RAIN_RADAR to strings.mapLayerRainRadar,
                    WeatherMapLayer.CLOUDS_TEMP to strings.mapLayerCloudsTemp,
                    WeatherMapLayer.WIND_VECTOR to strings.mapLayerWindVector,
                ).forEach { (layer, label) ->
                    val selected = activeLayer == layer
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelectLayer(layer) }
                            .testTag("weather_map_layer_${layer.name.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) colors.primary else colors.surface,
                        border = BorderStroke(1.dp, if (selected) colors.primary else colors.border),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selected) colors.onPrimary else colors.textMain,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onRefreshNow,
                enabled = !isSyncing,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("weather_map_refresh"),
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = colors.primary,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = strings.refreshPrecipitationMapCd,
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }

        // Interactive OpenStreetMap (osmdroid) Viewport + BMKG Precipitation Overlay
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(mapHeight)
                .testTag("bmkg_osmdroid_weather_map"),
            shape = RoundedCornerShape(22.dp),
            color = if (colors.isDark) Color(0xFF0F1E33) else Color(0xFFCBE2F6),
            border = BorderStroke(1.dp, colors.border),
            shadowElevation = 2.dp,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                )

                // Top-left Live BMKG + RainViewer Status & Optional Full Radar Link
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 10.dp, top = 10.dp, end = 64.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surface.copy(alpha = 0.94f),
                        border = BorderStroke(1.dp, colors.border),
                        shadowElevation = 2.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Map, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.osmPrecipPrefix} · ${VeridiaLocalizer.timestampLabel(lastUpdatedLabel, strings)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = colors.textMain,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }

                    if (onExpandRadar != null) {
                        Surface(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onExpandRadar)
                                .testTag("weather_map_expand_radar"),
                            shape = RoundedCornerShape(12.dp),
                            color = colors.primary,
                            shadowElevation = 2.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.OpenInFull,
                                    contentDescription = strings.openFullRadarCd,
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(12.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.radarButton,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = colors.onPrimary,
                                )
                            }
                        }
                    }
                }

                // Right-side Map Zoom In/Out & Recenter Overlay Controls (Row prevents overlap with bottom presets on compact map heights)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    IconButton(
                        onClick = {
                            programmaticTargetZoom = 5.8
                            currentZoom = 5.8
                            runCatching {
                                mapView.controller.animateTo(GeoPoint(mapCenterLat, mapCenterLon), 5.8, 360L)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("osmdroid_recenter"),
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            color = colors.surface.copy(alpha = 0.95f),
                            border = BorderStroke(1.dp, colors.border),
                            shadowElevation = 3.dp,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.MyLocation,
                                    contentDescription = strings.recenterMapCd,
                                    tint = colors.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, colors.border),
                        shadowElevation = 4.dp,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            IconButton(
                                onClick = { applyMapZoom(currentZoom + 1.0) },
                                enabled = canZoomIn,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("osmdroid_zoom_in"),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = strings.zoomInMapCd,
                                    tint = if (canZoomIn) colors.textMain else colors.textSub.copy(alpha = 0.35f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.width(28.dp),
                                color = colors.border,
                            )

                            Text(
                                text = "Z${currentZoom.roundToInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = SpaceMonoFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = colors.primary,
                                modifier = Modifier
                                    .padding(vertical = 3.dp)
                                    .testTag("osmdroid_zoom_level_badge"),
                            )

                            HorizontalDivider(
                                modifier = Modifier.width(28.dp),
                                color = colors.border,
                            )

                            IconButton(
                                onClick = { applyMapZoom(currentZoom - 1.0) },
                                enabled = canZoomOut,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("osmdroid_zoom_out"),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Remove,
                                    contentDescription = strings.zoomOutMapCd,
                                    tint = if (canZoomOut) colors.textMain else colors.textSub.copy(alpha = 0.35f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }

                // Bottom Overlay: Quick Zoom Presets + Precipitation Scale Legend + Selected BMKG Station Card
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (activeLayer == WeatherMapLayer.RAIN_RADAR) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.surface.copy(alpha = 0.92f),
                                border = BorderStroke(1.dp, colors.border),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    LegendDotLabel(color = Color(0xFF38BDF8), text = strings.legendLight)
                                    LegendDotLabel(color = Color(0xFF0284C7), text = "0.5+ mm")
                                    LegendDotLabel(color = Color(0xFFF59E0B), text = "2.5+ mm")
                                    LegendDotLabel(color = Color(0xFFDC2626), text = "7.0+ mm/h")
                                }
                            }
                        }

                        // Quick scale zoom presets for regional storm systems vs local rainfall tracking
                        listOf(
                            Triple(strings.presetRegion, 5.0, "osmdroid_zoom_preset_regional"),
                            Triple(strings.presetMetro, 8.0, "osmdroid_zoom_preset_metro"),
                            Triple(strings.presetLocal, 11.0, "osmdroid_zoom_preset_local"),
                        ).forEach { (presetLabel, presetZoom, tag) ->
                            val isNearPreset = kotlin.math.abs(currentZoom - presetZoom) <= 0.8
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { applyMapZoom(presetZoom) }
                                    .testTag(tag),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isNearPreset) colors.primary else colors.surface.copy(alpha = 0.92f),
                                border = BorderStroke(1.dp, if (isNearPreset) colors.primary else colors.border),
                            ) {
                                Text(
                                    text = presetLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = if (isNearPreset) colors.onPrimary else colors.textMain,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                )
                            }
                        }
                    }

                    if (selectedStation != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bmkg_station_inspector"),
                            shape = RoundedCornerShape(14.dp),
                            color = colors.surface.copy(alpha = 0.96f),
                            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.38f)),
                            shadowElevation = 3.dp,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.WaterDrop,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${selectedStation.name} · ${VeridiaLocalizer.conditionLabel(selectedStation.conditionLabel, selectedStation.conditionKind, strings)}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textMain,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = "${strings.stationPrecipLabel} ${String.format(Locale.US, "%.1f", selectedStation.precipMm)} mm/h (${selectedStation.precipChancePercent}%) · ${strings.stationWindLabel} ${selectedStation.windSpeedKmh} km/h · BMKG & OSM",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = colors.textSub,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = formatTempString(selectedStation.tempC, tempUnit),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = SpaceMonoFamily,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = colors.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDotLabel(color: Color, text: String) {
    val colors = veridiaColors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
            color = colors.textMain,
        )
    }
}

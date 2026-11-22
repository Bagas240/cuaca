package com.example.ui.components

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.example.ui.theme.veridiaColors
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.tan

object SlippyMapMath {
    const val TILE_SIZE_PX = 256.0

    fun latLonToWorldPx(lat: Double, lon: Double, zoom: Int): Pair<Double, Double> {
        val scale = TILE_SIZE_PX * 2.0.pow(zoom.toDouble())
        val x = ((lon + 180.0) / 360.0) * scale
        val clampedLat = lat.coerceIn(-85.0511, 85.0511)
        val latRad = Math.toRadians(clampedLat)
        val y = (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) * 0.5 * scale
        return x to y
    }

    fun tileUrl(zoom: Int, tileX: Int, tileY: Int): String {
        val maxTile = 1 shl zoom
        val wrappedX = ((tileX % maxTile) + maxTile) % maxTile
        return "https://basemaps.cartocdn.com/rastertiles/voyager/$zoom/$wrappedX/$tileY.png"
    }

    fun multiProviderTileUrls(zoom: Int, tileX: Int, tileY: Int): List<String> {
        val maxTile = 1 shl zoom
        val wrappedX = ((tileX % maxTile) + maxTile) % maxTile
        return listOf(
            "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/$zoom/$tileY/$wrappedX",
            "https://tile.openstreetmap.org/$zoom/$wrappedX/$tileY.png",
            "https://basemaps.cartocdn.com/rastertiles/voyager/$zoom/$wrappedX/$tileY.png",
        )
    }

    fun rainViewerTileUrl(prefix: String, zoom: Int, tileX: Int, tileY: Int): String {
        val maxTile = 1 shl zoom
        val wrappedX = ((tileX % maxTile) + maxTile) % maxTile
        return "$prefix/256/$zoom/$wrappedX/$tileY/2/1_1.png"
    }
}

private object MapTileLoaderSingleton {
    @Volatile
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        return instance ?: synchronized(this) {
            instance ?: buildLoader(context.applicationContext).also { instance = it }
        }
    }

    private fun buildLoader(appContext: Context): ImageLoader {
        val okHttp = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "VeridiaWeatherMonitor/1.0 (Android; BMKG Weather & Seismic)")
                    .build()
                chain.proceed(req)
            }
            .build()
        return ImageLoader.Builder(appContext)
            .okHttpClient(okHttp)
            .memoryCache {
                MemoryCache.Builder(appContext)
                    .maxSizePercent(0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(appContext.cacheDir.resolve("veridia_map_tiles"))
                    .maxSizeBytes(32L * 1024L * 1024L)
                    .build()
            }
            .crossfade(false)
            .build()
    }
}

@Composable
fun rememberMapImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember(context) { MapTileLoaderSingleton.get(context) }
}

private data class IslandPolygon(
    val name: String,
    val labelLat: Double,
    val labelLon: Double,
    val coords: List<Pair<Double, Double>>,
)

private val archipelagoCoastlines: List<IslandPolygon> = listOf(
    // Sumatra
    IslandPolygon(
        name = "Sumatra",
        labelLat = -0.6,
        labelLon = 101.4,
        coords = listOf(
            5.55 to 95.32, 4.90 to 97.90, 3.60 to 99.00, 1.80 to 101.60,
            -0.50 to 103.60, -2.50 to 105.90, -5.85 to 105.90, -5.90 to 104.50,
            -4.10 to 102.20, -1.50 to 100.40, 1.60 to 98.70, 4.20 to 96.10,
        ),
    ),
    // Java
    IslandPolygon(
        name = "Jawa",
        labelLat = -7.25,
        labelLon = 109.9,
        coords = listOf(
            -5.95 to 105.90, -6.10 to 107.00, -6.30 to 108.40, -6.95 to 110.45,
            -6.75 to 111.50, -7.20 to 112.80, -7.80 to 114.40, -8.70 to 114.30,
            -8.20 to 111.60, -7.75 to 109.00, -7.50 to 106.50, -6.80 to 105.40,
        ),
    ),
    // Bali & Nusa Tenggara
    IslandPolygon(
        name = "Bali",
        labelLat = -8.38,
        labelLon = 115.15,
        coords = listOf(-8.08 to 114.45, -8.18 to 115.65, -8.82 to 115.52, -8.65 to 114.45),
    ),
    IslandPolygon(
        name = "Nusa Tenggara",
        labelLat = -8.65,
        labelLon = 117.8,
        coords = listOf(-8.35 to 116.05, -8.35 to 117.45, -8.85 to 117.45, -8.88 to 116.05),
    ),
    IslandPolygon(
        name = "Flores",
        labelLat = -8.62,
        labelLon = 121.3,
        coords = listOf(-8.30 to 119.90, -8.32 to 122.90, -8.85 to 122.70, -8.82 to 119.95),
    ),
    // Kalimantan (Borneo)
    IslandPolygon(
        name = "Kalimantan",
        labelLat = 0.4,
        labelLon = 114.2,
        coords = listOf(
            6.90 to 116.80, 5.10 to 119.10, 1.50 to 118.90, -1.30 to 116.90,
            -3.90 to 116.10, -3.40 to 113.20, -2.90 to 110.20, 1.50 to 109.00,
            2.10 to 111.30, 4.90 to 114.90,
        ),
    ),
    // Sulawesi
    IslandPolygon(
        name = "Sulawesi",
        labelLat = -1.9,
        labelLon = 120.7,
        coords = listOf(
            1.70 to 125.10, 0.50 to 123.50, 0.90 to 120.70, -1.60 to 119.30,
            -3.50 to 118.90, -5.60 to 119.50, -5.20 to 120.40, -3.20 to 120.40,
            -4.60 to 122.90, -2.60 to 122.20, -0.70 to 123.20, -0.40 to 121.40,
            1.20 to 124.80,
        ),
    ),
    // Halmahera
    IslandPolygon(
        name = "Maluku",
        labelLat = 0.6,
        labelLon = 127.8,
        coords = listOf(2.10 to 127.90, 1.20 to 128.70, -0.70 to 128.20, -0.20 to 127.40),
    ),
    // Papua (Western New Guinea)
    IslandPolygon(
        name = "Papua",
        labelLat = -4.0,
        labelLon = 137.8,
        coords = listOf(
            -0.80 to 131.00, -1.50 to 134.10, -2.60 to 141.00, -8.30 to 141.00,
            -8.20 to 137.80, -4.50 to 134.80, -2.40 to 132.10,
        ),
    ),
    // Malay Peninsula
    IslandPolygon(
        name = "Malaya",
        labelLat = 4.2,
        labelLon = 102.0,
        coords = listOf(
            6.50 to 100.10, 6.20 to 102.30, 1.35 to 104.20, 1.30 to 103.50,
            3.10 to 101.20, 5.40 to 100.20,
        ),
    ),
    // Japan (Honshu + Kyushu + Hokkaido) for preset Tokyo/Kyoto/Sapporo
    IslandPolygon(
        name = "Honshu",
        labelLat = 36.2,
        labelLon = 138.2,
        coords = listOf(
            41.30 to 140.40, 40.50 to 141.60, 36.20 to 140.60, 35.10 to 139.80,
            34.40 to 136.80, 33.50 to 135.70, 34.00 to 131.00, 35.60 to 133.40,
            37.80 to 139.00,
        ),
    ),
    IslandPolygon(
        name = "Hokkaido",
        labelLat = 43.3,
        labelLon = 142.8,
        coords = listOf(45.40 to 141.90, 43.90 to 145.10, 42.00 to 143.30, 41.80 to 140.10, 43.80 to 141.30),
    ),
)

@Composable
fun Slippy2DMapBackground(
    centerLat: Double,
    centerLon: Double,
    zoom: Int,
    panOffsetPx: Offset,
    overlayTileUrlPrefix: String? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val imageLoader = rememberMapImageLoader()
    val tileDp = with(density) { SlippyMapMath.TILE_SIZE_PX.toFloat().toDp() }
    val isDark = veridiaColors.isDark
    val oceanColor = if (isDark) Color(0xFF0B1626) else Color(0xFFCBE2F6)
    val gridColor = if (isDark) Color(0xFF1A2C47) else Color(0xFFB8D4EC)
    val landColor = if (isDark) Color(0xFF1D2E46) else Color(0xFFEAE6D9)
    val coastlineStroke = if (isDark) Color(0xFF47638A) else Color(0xFF7D9DB8)
    val islandPath = remember { Path() }
    val labelPaint = remember(isDark) {
        Paint().apply {
            color = if (isDark) android.graphics.Color.argb(195, 203, 213, 225) else android.graphics.Color.argb(175, 51, 65, 85)
            textSize = 24f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
    val tileDarkFilter = remember(isDark) {
        if (!isDark) {
            null
        } else {
            val matrix = ColorMatrix(
                floatArrayOf(
                    0.68f, 0f, 0f, 0f, 0f,
                    0f, 0.74f, 0f, 0f, 0f,
                    0f, 0f, 0.88f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )
            ColorFilter.colorMatrix(matrix)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(oceanColor),
    ) {
        val viewportW = constraints.maxWidth.toDouble()
        val viewportH = constraints.maxHeight.toDouble()
        val (centerWorldX, centerWorldY) = SlippyMapMath.latLonToWorldPx(centerLat, centerLon, zoom)

        val viewOriginX = centerWorldX - viewportW / 2.0 - panOffsetPx.x
        val viewOriginY = centerWorldY - viewportH / 2.0 - panOffsetPx.y

        val tileSize = SlippyMapMath.TILE_SIZE_PX
        val maxTileIdx = (1 shl zoom) - 1
        val minTx = floor(viewOriginX / tileSize).toInt()
        val maxTx = floor((viewOriginX + viewportW) / tileSize).toInt()
        val minTy = floor(viewOriginY / tileSize).toInt().coerceIn(0, maxTileIdx)
        val maxTy = floor((viewOriginY + viewportH) / tileSize).toInt().coerceIn(0, maxTileIdx)

        // 1. Instant Mercator Archipelago & Coastline Base Layer (always visible even before/without tiles)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Subtle ocean lat/lon grid
            val step = 72f
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f)
                y += step
            }

            archipelagoCoastlines.forEach { island ->
                islandPath.rewind()
                var anyVisible = false
                island.coords.forEachIndexed { idx, (lat, lon) ->
                    val (wx, wy) = SlippyMapMath.latLonToWorldPx(lat, lon, zoom)
                    val sx = (wx - viewOriginX).toFloat()
                    val sy = (wy - viewOriginY).toFloat()
                    if (sx in -200f..(size.width + 200f) && sy in -200f..(size.height + 200f)) {
                        anyVisible = true
                    }
                    if (idx == 0) islandPath.moveTo(sx, sy) else islandPath.lineTo(sx, sy)
                }
                islandPath.close()
                if (anyVisible) {
                    drawPath(path = islandPath, color = landColor)
                    drawPath(path = islandPath, color = coastlineStroke, style = Stroke(width = 2f))
                    val (lx, ly) = SlippyMapMath.latLonToWorldPx(island.labelLat, island.labelLon, zoom)
                    val slx = (lx - viewOriginX).toFloat()
                    val sly = (ly - viewOriginY).toFloat()
                    if (slx in 20f..(size.width - 20f) && sly in 20f..(size.height - 20f)) {
                        drawContext.canvas.nativeCanvas.drawText(island.name, slx, sly, labelPaint)
                    }
                }
            }
        }

        // 2. Multi-Provider Live 2D Map Tiles (Esri Street -> OSM -> CartoDB automatic fallback)
        for (tx in minTx..maxTx) {
            for (ty in minTy..maxTy) {
                val screenX = (tx * tileSize - viewOriginX).roundToInt()
                val screenY = (ty * tileSize - viewOriginY).roundToInt()
                val candidates = remember(zoom, tx, ty) {
                    SlippyMapMath.multiProviderTileUrls(zoom, tx, ty)
                }
                var providerIndex by remember(zoom, tx, ty) { mutableIntStateOf(0) }
                val activeUrl = candidates.getOrNull(providerIndex)

                if (activeUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(activeUrl)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        colorFilter = tileDarkFilter,
                        onError = {
                            if (providerIndex < candidates.lastIndex) {
                                providerIndex += 1
                            }
                        },
                        modifier = Modifier
                            .offset { IntOffset(screenX, screenY) }
                            .size(tileDp),
                    )
                }

                // Optional Live Weather Radar Tile Overlay (RainViewer)
                if (!overlayTileUrlPrefix.isNullOrBlank()) {
                    val radarUrl = SlippyMapMath.rainViewerTileUrl(overlayTileUrlPrefix, zoom, tx, ty)
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(radarUrl)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        alpha = 0.72f,
                        modifier = Modifier
                            .offset { IntOffset(screenX, screenY) }
                            .size(tileDp),
                    )
                }
            }
        }
    }
}

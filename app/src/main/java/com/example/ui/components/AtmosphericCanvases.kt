package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PastelBorder
import com.example.ui.theme.PastelTextMain
import com.example.ui.theme.PastelTextSub
import com.example.ui.theme.WarningAmber

const val HOME_SKY_IMAGE_URL =
    "https://lh3.googleusercontent.com/aida/ALJJyrBflIwXeKtgNlLCoxiar9VKrIzwk4NjTyfrY-IK3ea1NuAKt1NKY0JYm8MctLP9KyOJOmbOvg7CL3gi6EhZyGriELfsnEN8QsSoO1b01NkGDaIQwOSsHvfH9Zwza5I75RstpC9CP1J-jxfTyvjndlcTmewIKFBdsp4UoV_zZecGIsjZlqHOQnKrP3dPJe37teFyuH23IoRkqjX5a9H9yGjErBcBUUvvNaegkkzjOaPBPFNMcbHejyyWQg"

@Composable
fun SolarArcCanvas(
    sunriseTime: String,
    sunsetTime: String,
    solarProgress: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val horizonY = h * 0.76f
            val leftX = w * 0.08f
            val rightX = w * 0.92f
            val peakY = h * 0.14f

            // Dashed horizon line
            drawLine(
                color = PastelBorder,
                start = Offset(w * 0.05f, horizonY),
                end = Offset(w * 0.95f, horizonY),
                strokeWidth = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
            )

            // Solar arc quadratic curve
            val arcPath = Path().apply {
                moveTo(leftX, horizonY)
                quadraticTo(w * 0.5f, peakY, rightX, horizonY)
            }
            drawPath(
                path = arcPath,
                color = WarningAmber.copy(alpha = 0.45f),
                style = Stroke(width = 4f, cap = StrokeCap.Round),
            )

            // Current sun position along quadratic Bezier
            val t = solarProgress.coerceIn(0.05f, 0.95f)
            val oneMinusT = 1f - t
            val ctrlX = w * 0.5f
            val sunX = (oneMinusT * oneMinusT * leftX) + (2f * oneMinusT * t * ctrlX) + (t * t * rightX)
            val sunY = (oneMinusT * oneMinusT * horizonY) + (2f * oneMinusT * t * peakY) + (t * t * horizonY)

            drawCircle(
                color = WarningAmber.copy(alpha = 0.25f),
                radius = 18f,
                center = Offset(sunX, sunY),
            )
            drawCircle(
                color = WarningAmber,
                radius = 9f,
                center = Offset(sunX, sunY),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 8.dp),
        ) {
            Text(
                text = "Sunrise",
                style = MaterialTheme.typography.labelSmall,
                color = PastelTextSub,
            )
            Text(
                text = sunriseTime,
                style = MaterialTheme.typography.headlineSmall,
                color = PastelTextMain,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = "Sunset",
                style = MaterialTheme.typography.labelSmall,
                color = PastelTextSub,
            )
            Text(
                text = sunsetTime,
                style = MaterialTheme.typography.headlineSmall,
                color = PastelTextMain,
            )
        }
    }
}

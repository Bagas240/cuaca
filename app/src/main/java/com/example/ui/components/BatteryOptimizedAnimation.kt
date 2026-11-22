package com.example.ui.components

import android.content.Context
import android.os.PowerManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.navigation.rememberIsDestinationAnimating
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

private const val STANDARD_ANIMATION_FPS = 30
private const val BATTERY_SAVER_ANIMATION_FPS = 20

/**
 * Observes OS Battery Saver mode only on lifecycle transitions rather than querying
 * [PowerManager] on every animation frame.
 */
@Composable
fun rememberIsBatterySaverActive(): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var powerSave by remember(context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        mutableStateOf(pm?.isPowerSaveMode == true)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, _ ->
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerSave = pm?.isPowerSaveMode == true
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return powerSave
}

/**
 * Battery-optimized continuous animation driver:
 * - Unmounts infinite frame callbacks whenever the navigation destination is hidden or backgrounded.
 * - Quantizes redraw invalidations to ~30 fps (~20 fps in Battery Saver) via [derivedStateOf] so
 *   90Hz/120Hz OLED displays keep smooth visual motion while eliminating redundant GPU passes.
 */
@Composable
fun rememberQuantizedCyclePhase(
    enabled: Boolean = true,
    cycleDurationMs: Int = 2000,
    pingPong: Boolean = false,
    idleValue: Float = 0f,
    label: String = "quantized_cycle",
): State<Float> {
    val isDestinationAnimating = rememberIsDestinationAnimating()
    val isPowerSave = rememberIsBatterySaverActive()
    val shouldRun = enabled && isDestinationAnimating

    if (!shouldRun) {
        return remember(idleValue) { mutableStateOf(idleValue) }
    }

    val targetFps = if (isPowerSave) BATTERY_SAVER_ANIMATION_FPS else STANDARD_ANIMATION_FPS
    val transition = rememberInfiniteTransition(label = label)
    val rawPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = cycleDurationMs.coerceAtLeast(300),
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "${label}_raw",
    )

    val steps = ((cycleDurationMs / 1000f) * targetFps).roundToInt().coerceAtLeast(12)
    return remember(steps, pingPong) {
        derivedStateOf {
            val snapped = (rawPhase * steps).roundToInt() / steps.toFloat()
            if (pingPong) {
                (0.5f - 0.5f * cos(snapped * 2.0 * PI)).toFloat()
            } else {
                snapped
            }
        }
    }
}

package com.example.alerts

import kotlin.math.sqrt

data class SeismicSampleResult(
    val linearMagnitudeMs2: Float,
    val peakMagnitudeMs2: Float,
    val vibrationPeakCount: Int,
    val patternRecognized: Boolean,
)

/**
 * Analyzes high-rate device accelerometer streams to distinguish sustained oscillatory
 * seismic tremors (P/S-wave vibrations) from static tilt or single accidental bumps/drops.
 *
 * Uses a gravity-isolating high-pass filter plus sliding-window peak cadence and axis
 * direction reversal checks.
 */
class SeismicAccelerometerDetector(
    private val tremorThresholdMs2: Float = DEFAULT_TREMOR_THRESHOLD_MS2,
    private val requiredOscillationPeaks: Int = REQUIRED_OSCILLATION_PEAKS,
    private val requiredAxisReversals: Int = REQUIRED_AXIS_REVERSALS,
    private val windowDurationNanos: Long = WINDOW_DURATION_NANOS,
    private val minPeakSpacingNanos: Long = MIN_PEAK_SPACING_NANOS,
    private val minPatternSpanNanos: Long = MIN_PATTERN_SPAN_NANOS,
    private val triggerCooldownNanos: Long = TRIGGER_COOLDOWN_NANOS,
) {

    companion object {
        const val GRAVITY_ALPHA = 0.8f
        const val DEFAULT_TREMOR_THRESHOLD_MS2 = 2.4f
        const val REQUIRED_OSCILLATION_PEAKS = 4
        const val REQUIRED_AXIS_REVERSALS = 2
        const val WINDOW_DURATION_NANOS = 3_500_000_000L
        const val MIN_PEAK_SPACING_NANOS = 70_000_000L
        const val MIN_PATTERN_SPAN_NANOS = 400_000_000L
        const val TRIGGER_COOLDOWN_NANOS = 15_000_000_000L
    }

    private data class TremorPeak(
        val timestampNanos: Long,
        val magnitudeMs2: Float,
        val dominantAxisSign: Int,
    )

    private var gravityInitialized = false
    private val gravity = FloatArray(3)
    private val recentPeaks = ArrayDeque<TremorPeak>()
    private var lastTriggerNanos: Long = Long.MIN_VALUE

    @Synchronized
    fun onAccelerometerReading(
        rawX: Float,
        rawY: Float,
        rawZ: Float,
        timestampNanos: Long,
    ): SeismicSampleResult {
        if (!gravityInitialized) {
            gravity[0] = rawX
            gravity[1] = rawY
            gravity[2] = rawZ
            gravityInitialized = true
            return SeismicSampleResult(
                linearMagnitudeMs2 = 0f,
                peakMagnitudeMs2 = 0f,
                vibrationPeakCount = 0,
                patternRecognized = false,
            )
        }

        // Isolate dynamic linear acceleration by filtering out Earth gravity
        gravity[0] = GRAVITY_ALPHA * gravity[0] + (1f - GRAVITY_ALPHA) * rawX
        gravity[1] = GRAVITY_ALPHA * gravity[1] + (1f - GRAVITY_ALPHA) * rawY
        gravity[2] = GRAVITY_ALPHA * gravity[2] + (1f - GRAVITY_ALPHA) * rawZ

        val linX = rawX - gravity[0]
        val linY = rawY - gravity[1]
        val linZ = rawZ - gravity[2]
        val linearMagnitude = sqrt(linX * linX + linY * linY + linZ * linZ)

        // Evict peaks older than the analysis window
        while (recentPeaks.isNotEmpty() && timestampNanos - recentPeaks.first().timestampNanos > windowDurationNanos) {
            recentPeaks.removeFirst()
        }

        val lastPeak = recentPeaks.lastOrNull()
        val spacedEnough = lastPeak == null || (timestampNanos - lastPeak.timestampNanos) >= minPeakSpacingNanos

        if (linearMagnitude >= tremorThresholdMs2 && spacedEnough) {
            val dominantSign = dominantAxisSign(linX, linY, linZ)
            recentPeaks.addLast(
                TremorPeak(
                    timestampNanos = timestampNanos,
                    magnitudeMs2 = linearMagnitude,
                    dominantAxisSign = dominantSign,
                )
            )
        }

        val peakCount = recentPeaks.size
        val peakMag = recentPeaks.maxOfOrNull { it.magnitudeMs2 } ?: linearMagnitude
        val axisReversals = countAxisReversals()
        val patternSpan = if (peakCount >= 2) {
            recentPeaks.last().timestampNanos - recentPeaks.first().timestampNanos
        } else {
            0L
        }

        val cooldownElapsed = lastTriggerNanos == Long.MIN_VALUE ||
            (timestampNanos - lastTriggerNanos) >= triggerCooldownNanos

        val recognized = cooldownElapsed &&
            peakCount >= requiredOscillationPeaks &&
            axisReversals >= requiredAxisReversals &&
            patternSpan >= minPatternSpanNanos

        if (recognized) {
            lastTriggerNanos = timestampNanos
            recentPeaks.clear()
        }

        return SeismicSampleResult(
            linearMagnitudeMs2 = linearMagnitude,
            peakMagnitudeMs2 = peakMag,
            vibrationPeakCount = peakCount,
            patternRecognized = recognized,
        )
    }

    private fun dominantAxisSign(x: Float, y: Float, z: Float): Int {
        val ax = kotlin.math.abs(x)
        val ay = kotlin.math.abs(y)
        val az = kotlin.math.abs(z)
        return when {
            ax >= ay && ax >= az -> if (x >= 0f) 1 else -1
            ay >= ax && ay >= az -> if (y >= 0f) 2 else -2
            else -> if (z >= 0f) 3 else -3
        }
    }

    private fun countAxisReversals(): Int {
        if (recentPeaks.size < 2) return 0
        var reversals = 0
        var prevSign = recentPeaks.first().dominantAxisSign
        for (i in 1 until recentPeaks.size) {
            val currentSign = recentPeaks[i].dominantAxisSign
            if (currentSign != prevSign) {
                reversals += 1
                prevSign = currentSign
            }
        }
        return reversals
    }

    @Synchronized
    fun reset() {
        gravityInitialized = false
        gravity[0] = 0f
        gravity[1] = 0f
        gravity[2] = 0f
        recentPeaks.clear()
        lastTriggerNanos = Long.MIN_VALUE
    }
}

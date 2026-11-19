package com.example.alerts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class DisasterKind(
    val title: String,
    val badge: String,
    val sirenProfileLabel: String,
) {
    EARTHQUAKE(
        title = "Earthquake Alert",
        badge = "SEISMIC EWS",
        sirenProfileLabel = "Civil-Defense Seismic Wail",
    ),
    TSUNAMI(
        title = "Tsunami Warning",
        badge = "TSUNAMI SIREN",
        sirenProfileLabel = "Coastal Civil-Defense Siren",
    ),
    SEVERE_STORM(
        title = "Extreme Storm Alert",
        badge = "STORM WATCH",
        sirenProfileLabel = "Severe Weather Horn",
    ),
    HAZARDOUS_AIR(
        title = "Hazardous Air Alert",
        badge = "AIR EMERGENCY",
        sirenProfileLabel = "Atmospheric Alert Tone",
    ),
}

/**
 * Synthesizes a natural-disaster civil-defense warning siren (smooth low-to-high-to-low wail
 * with harmonic resonance, unlike a rapid two-tone ambulance siren) using AudioTrack PCM.
 */
class DisasterSirenPlayer(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playJob: Job? = null
    private var activeTrack: AudioTrack? = null

    fun playSiren(
        kind: DisasterKind,
        cycles: Int = 2,
        onFinished: () -> Unit = {},
    ) {
        stopSiren()
        triggerDisasterVibration()

        playJob = scope.launch {
            runCatching {
                val sampleRate = 22050
                val cycleSec = when (kind) {
                    DisasterKind.TSUNAMI -> 3.6
                    DisasterKind.EARTHQUAKE -> 3.2
                    DisasterKind.SEVERE_STORM -> 2.8
                    DisasterKind.HAZARDOUS_AIR -> 2.4
                }
                val samplesPerCycle = (sampleRate * cycleSec).toInt()
                val totalSamples = samplesPerCycle * cycles.coerceIn(1, 3)

                val minFreq = when (kind) {
                    DisasterKind.TSUNAMI -> 320.0
                    DisasterKind.EARTHQUAKE -> 360.0
                    DisasterKind.SEVERE_STORM -> 300.0
                    DisasterKind.HAZARDOUS_AIR -> 340.0
                }
                val maxFreq = when (kind) {
                    DisasterKind.TSUNAMI -> 740.0
                    DisasterKind.EARTHQUAKE -> 690.0
                    DisasterKind.SEVERE_STORM -> 560.0
                    DisasterKind.HAZARDOUS_AIR -> 520.0
                }

                val pcm = ShortArray(totalSamples)
                var phase = 0.0
                val fadeSamples = (sampleRate * 0.12).toInt()

                for (i in 0 until totalSamples) {
                    val cyclePos = (i % samplesPerCycle).toDouble() / samplesPerCycle.toDouble()
                    // Smooth civil-defense siren rise-and-fall curve (not a rapid ambulance two-tone)
                    val sweep = 0.5 - 0.5 * cos(2.0 * PI * cyclePos)
                    val freq = minFreq + (maxFreq - minFreq) * sweep
                    phase += 2.0 * PI * freq / sampleRate

                    val fundamental = sin(phase)
                    val harmonic2 = 0.28 * sin(2.0 * phase)
                    val harmonic3 = 0.12 * sin(3.0 * phase)
                    val subtleTremolo = 0.90 + 0.10 * sin(2.0 * PI * 3.5 * i / sampleRate)

                    val env = when {
                        i < fadeSamples -> i.toDouble() / fadeSamples
                        i > totalSamples - fadeSamples -> (totalSamples - i).toDouble() / fadeSamples
                        else -> 1.0
                    }

                    val sample = ((fundamental + harmonic2 + harmonic3) / 1.4) * subtleTremolo * env
                    pcm[i] = (sample * Short.MAX_VALUE * 0.72).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        .toShort()
                }

                val minBuffer = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                ).coerceAtLeast(4096)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuffer)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                activeTrack = track
                track.play()

                var offset = 0
                val chunkSize = 2048
                while (isActive && offset < pcm.size) {
                    val toWrite = minOf(chunkSize, pcm.size - offset)
                    val written = track.write(pcm, offset, toWrite)
                    if (written <= 0) break
                    offset += written
                }

                runCatching {
                    track.stop()
                    track.release()
                }
                if (activeTrack === track) activeTrack = null
            }
            onFinished()
        }
    }

    fun stopSiren() {
        playJob?.cancel()
        playJob = null
        activeTrack?.let { track ->
            runCatching {
                track.pause()
                track.flush()
                track.stop()
                track.release()
            }
        }
        activeTrack = null
        cancelVibration()
    }

    private fun triggerDisasterVibration() {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                mgr?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return
            if (!vibrator.hasVibrator()) return
            val timings = longArrayOf(0, 450, 200, 450, 200, 700)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        }
    }

    private fun cancelVibration() {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                mgr?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.cancel()
        }
    }
}

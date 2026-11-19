package com.example.data.location

import com.example.data.model.CityLocation
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round

data class MaskedCoordinates(
    val latitude: Double,
    val longitude: Double,
    val precisionDecimals: Int,
    val isMasked: Boolean,
)

/**
 * Privacy-preserving location masking utility.
 * Rounds precise GPS coordinates to lower decimal precision (default: 1 decimal place, ~11.1 km)
 * before sending location coordinates to external weather, air-quality, or mapping APIs.
 */
object LocationMaskingUtility {

    const val DEFAULT_PRECISION_DECIMALS = 1

    fun maskCoordinates(
        latitude: Double,
        longitude: Double,
        precisionDecimals: Int = DEFAULT_PRECISION_DECIMALS,
        enabled: Boolean = true,
    ): MaskedCoordinates {
        val safeLat = latitude.coerceIn(-85.0, 85.0)
        val safeLon = longitude.coerceIn(-180.0, 180.0)
        if (!enabled) {
            return MaskedCoordinates(
                latitude = safeLat,
                longitude = safeLon,
                precisionDecimals = precisionDecimals,
                isMasked = false,
            )
        }

        val clampedDecimals = precisionDecimals.coerceIn(0, 2)
        val factor = 10.0.pow(clampedDecimals)
        val roundedLat = normalizeZero(round(safeLat * factor) / factor).coerceIn(-85.0, 85.0)
        val roundedLon = normalizeZero(round(safeLon * factor) / factor).coerceIn(-180.0, 180.0)

        return MaskedCoordinates(
            latitude = roundedLat,
            longitude = roundedLon,
            precisionDecimals = clampedDecimals,
            isMasked = true,
        )
    }

    fun maskCityForApi(
        city: CityLocation,
        enabled: Boolean = true,
        precisionDecimals: Int = DEFAULT_PRECISION_DECIMALS,
    ): CityLocation {
        val masked = maskCoordinates(
            latitude = city.latitude,
            longitude = city.longitude,
            precisionDecimals = precisionDecimals,
            enabled = enabled,
        )
        return city.copy(
            latitude = masked.latitude,
            longitude = masked.longitude,
        )
    }

    fun formatMaskedZoneLabel(
        latitude: Double,
        longitude: Double,
        precisionDecimals: Int = DEFAULT_PRECISION_DECIMALS,
    ): String {
        val masked = maskCoordinates(
            latitude = latitude,
            longitude = longitude,
            precisionDecimals = precisionDecimals,
            enabled = true,
        )
        val latHem = if (masked.latitude >= 0) "N" else "S"
        val lonHem = if (masked.longitude >= 0) "E" else "W"
        val pattern = "%.${masked.precisionDecimals}f°%s, %.${masked.precisionDecimals}f°%s · Masked Zone"
        return "~" + String.format(
            Locale.US,
            pattern,
            abs(masked.latitude),
            latHem,
            abs(masked.longitude),
            lonHem,
        )
    }

    private fun normalizeZero(value: Double): Double = if (value == 0.0) 0.0 else value
}

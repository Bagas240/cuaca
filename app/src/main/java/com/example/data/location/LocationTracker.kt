package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class ResolvedGpsLocation(
    val latitude: Double,
    val longitude: Double,
    val localityName: String,
    val countryName: String,
)

class LocationTracker(private val context: Context) {

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentCoordinates(privacyMasking: Boolean = true): Result<ResolvedGpsLocation> =
        withContext(Dispatchers.IO) {
            if (!hasLocationPermission()) {
                return@withContext Result.failure(SecurityException("Location permission not granted"))
            }

            val location = getFusedCurrentLocation()
                ?: getFusedLastLocation()
                ?: getFrameworkLastKnownLocation()
                ?: return@withContext Result.failure(
                    IllegalStateException("Unable to acquire coarse location. Ensure device location is turned on.")
                )

            val (effLat, effLon) = if (privacyMasking) {
                obfuscateForPrivacy(location.latitude, location.longitude)
            } else {
                location.latitude to location.longitude
            }
            val (locality, country) = reverseGeocode(effLat, effLon)
            Result.success(
                ResolvedGpsLocation(
                    latitude = effLat,
                    longitude = effLon,
                    localityName = locality,
                    countryName = country,
                )
            )
        }

    @SuppressLint("MissingPermission")
    private suspend fun getFusedCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        // Always prefer coarse / balanced accuracy so exact street-level GPS is never requested
        val priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
        val request = CurrentLocationRequest.Builder()
            .setPriority(priority)
            .setMaxUpdateAgeMillis(60_000L)
            .setDurationMillis(6_000L)
            .build()
        val cts = CancellationTokenSource()
        cont.invokeOnCancellation { cts.cancel() }

        runCatching {
            fusedClient.getCurrentLocation(request, cts.token)
                .addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }.onFailure {
            if (cont.isActive) cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getFusedLastLocation(): Location? = suspendCancellableCoroutine { cont ->
        runCatching {
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    if (cont.isActive) cont.resume(location)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }.onFailure {
            if (cont.isActive) cont.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getFrameworkLastKnownLocation(): Location? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
        return providers
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    private suspend fun reverseGeocode(lat: Double, lon: Double): Pair<String, String> {
        if (!Geocoder.isPresent()) {
            return "Current GPS" to formatCoordLabel(lat, lon)
        }
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                runCatching {
                    geocoder.getFromLocation(lat, lon, 1) { addresses ->
                        if (cont.isActive) cont.resume(addresses.firstOrNull())
                    }
                }.onFailure {
                    if (cont.isActive) cont.resume(null)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            runCatching { geocoder.getFromLocation(lat, lon, 1)?.firstOrNull() }.getOrNull()
        }

        val locality = address?.locality
            ?: address?.subAdminArea
            ?: address?.adminArea
            ?: "Current GPS"
        val country = address?.countryName ?: formatCoordLabel(lat, lon)
        return locality to country
    }

    companion object {
        fun obfuscateForPrivacy(lat: Double, lon: Double): Pair<Double, Double> {
            val masked = LocationMaskingUtility.maskCoordinates(
                latitude = lat,
                longitude = lon,
                precisionDecimals = LocationMaskingUtility.DEFAULT_PRECISION_DECIMALS,
                enabled = true,
            )
            return masked.latitude to masked.longitude
        }

        fun formatPrivateZoneLabel(lat: Double, lon: Double): String {
            return LocationMaskingUtility.formatMaskedZoneLabel(
                latitude = lat,
                longitude = lon,
                precisionDecimals = LocationMaskingUtility.DEFAULT_PRECISION_DECIMALS,
            )
        }

        fun formatCoordLabel(lat: Double, lon: Double): String {
            val latHem = if (lat >= 0) "N" else "S"
            val lonHem = if (lon >= 0) "E" else "W"
            return String.format(
                Locale.US,
                "%.2f°%s, %.2f°%s",
                kotlin.math.abs(lat),
                latHem,
                kotlin.math.abs(lon),
                lonHem,
            )
        }
    }
}

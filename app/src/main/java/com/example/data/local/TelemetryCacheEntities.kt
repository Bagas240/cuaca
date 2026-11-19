package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val cityId: String,
    val cityName: String,
    val maskedLat: Double,
    val maskedLon: Double,
    val currentTempC: Int,
    val conditionLabel: String,
    val dataSourceLabel: String,
    val weatherJson: String,
    val airQualityJson: String? = null,
    val stationsJson: String? = null,
    val fetchedAtMillis: Long,
)

@Entity(tableName = "cached_seismic")
data class CachedSeismicEntity(
    @PrimaryKey val cacheKey: String = LATEST_KEY,
    val bannerMagnitude: String,
    val activeEventTitle: String,
    val tsunamiStatus: String,
    val epicenterLat: Double,
    val epicenterLon: Double,
    val seismicJson: String,
    val fetchedAtMillis: Long,
) {
    companion object {
        const val LATEST_KEY = "latest_seismic"
    }
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryCacheDao {

    @Query("SELECT * FROM cached_weather WHERE cityId = :cityId LIMIT 1")
    fun observeWeatherForCity(cityId: String): Flow<CachedWeatherEntity?>

    @Query("SELECT * FROM cached_weather ORDER BY fetchedAtMillis DESC LIMIT 1")
    fun observeLatestWeather(): Flow<CachedWeatherEntity?>

    @Query("SELECT * FROM cached_seismic WHERE cacheKey = :cacheKey LIMIT 1")
    fun observeLatestSeismic(cacheKey: String = CachedSeismicEntity.LATEST_KEY): Flow<CachedSeismicEntity?>

    @Query("SELECT * FROM cached_weather WHERE cityId = :cityId LIMIT 1")
    suspend fun getWeatherForCity(cityId: String): CachedWeatherEntity?

    @Query("SELECT * FROM cached_weather ORDER BY fetchedAtMillis DESC LIMIT 1")
    suspend fun getLatestWeather(): CachedWeatherEntity?

    @Query("SELECT * FROM cached_seismic WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getLatestSeismic(cacheKey: String = CachedSeismicEntity.LATEST_KEY): CachedSeismicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeather(entity: CachedWeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSeismic(entity: CachedSeismicEntity)

    @Query("DELETE FROM cached_weather")
    suspend fun clearWeatherCache()

    @Query("DELETE FROM cached_seismic")
    suspend fun clearSeismicCache()
}

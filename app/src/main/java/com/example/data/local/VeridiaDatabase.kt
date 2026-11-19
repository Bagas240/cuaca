package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CachedWeatherEntity::class, CachedSeismicEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class VeridiaDatabase : RoomDatabase() {

    abstract fun telemetryCacheDao(): TelemetryCacheDao

    companion object {
        @Volatile
        private var INSTANCE: VeridiaDatabase? = null

        fun getInstance(context: Context): VeridiaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VeridiaDatabase::class.java,
                    "veridia_telemetry_cache.db",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

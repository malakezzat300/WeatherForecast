package com.malakezzat.weatherforecast.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.malakezzat.weatherforecast.model.Alert

@Database(entities = [WeatherDB::class, Alert::class, FavoriteDB::class], version = 10)
abstract class AppDatabase : RoomDatabase() {
    abstract val weatherDAO: WeatherDao
    abstract val favoriteDao: FavoriteDao
    abstract val alertDAO: AlertDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weather_database"
                ).fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
        }
    }
}
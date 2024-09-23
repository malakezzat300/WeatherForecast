package com.malakezzat.weatherforecast.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.malakezzat.weatherforecast.database.home.AlertDao
import com.malakezzat.weatherforecast.database.home.WeatherDB
import com.malakezzat.weatherforecast.database.home.WeatherDao
import com.malakezzat.weatherforecast.model.Alert
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.WeatherResponse

@Database(entities = [WeatherDB::class, Alert::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract val weatherDAO: WeatherDao
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
package com.malakezzat.weatherforecast.database.home

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.WeatherResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_response")
    fun getStoredWeather(): Flow<List<WeatherDB>>

    @Query("SELECT * FROM weather_response WHERE id = :weatherId")
    suspend fun findByWeatherId(weatherId: Int): WeatherDB?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherResponse(weatherDB: WeatherDB)

    @Delete
    suspend fun deleteWeatherResponse(weatherDB: WeatherDB)

}

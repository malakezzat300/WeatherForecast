package com.malakezzat.weatherforecast.model

import com.malakezzat.weatherforecast.database.home.WeatherDB
import kotlinx.coroutines.flow.Flow


interface WeatherRepository {
    suspend fun getWeatherOverNetwork(lat: Double,
                                      lon: Double,
                                      units: String = " ",
                                      lang: String = "en") : WeatherResponse

    suspend fun getForecastOverNetwork(lat: Double,
                                      lon: Double,
                                      cnt : Int = 7,
                                      units: String = " ",
                                      lang: String = "en") : ForecastResponse

    suspend fun insertWeather(weatherDB: WeatherDB)
    suspend fun deleteWeather(weatherDB: WeatherDB)
    suspend fun getAllStoredWeather(): Flow<List<WeatherDB>>
    suspend fun findWeatherById(weatherId: Int): WeatherDB?


    suspend fun getAllAlerts(): Flow<List<Alert>>
    suspend fun insertAlert(alert: Alert)
    suspend fun deleteAlert(alert: Alert)
}
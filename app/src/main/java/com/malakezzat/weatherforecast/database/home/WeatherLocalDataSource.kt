package com.malakezzat.weatherforecast.database.home

import com.malakezzat.weatherforecast.model.Alert

import kotlinx.coroutines.flow.Flow

interface WeatherLocalDataSource {

    suspend fun getStoredWeather(): Flow<List<WeatherDB>>
    suspend fun findWeatherById(weatherId: Int): WeatherDB?
    suspend fun insertWeather(weatherDB: WeatherDB)
    suspend fun deleteWeather(weatherDB: WeatherDB)

    suspend fun getAllAlerts(): Flow<List<Alert>>
    suspend fun insertAlert(alert: Alert)
    suspend fun deleteAlert(alert: Alert)
}


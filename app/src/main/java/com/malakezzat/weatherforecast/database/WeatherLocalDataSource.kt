package com.malakezzat.weatherforecast.database

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
    suspend fun deleteAlertById(alertId: String)

    suspend fun getFavoriteData(): Flow<List<FavoriteDB>>
    suspend fun insertFavorite(favoriteDB: FavoriteDB)
    suspend fun deleteFavorite(favoriteDB: FavoriteDB)
    suspend fun deleteFavoriteById(favoriteId: String)
}


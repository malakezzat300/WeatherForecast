package com.malakezzat.weatherforecast.model

import com.malakezzat.weatherforecast.ApiState
import com.malakezzat.weatherforecast.database.FavoriteDB
import com.malakezzat.weatherforecast.database.WeatherDB
import kotlinx.coroutines.flow.Flow


interface WeatherRepository {
    suspend fun getWeatherOverNetwork(lat: Double,
                                      lon: Double,
                                      units: String = " ",
                                      lang: String = "en") : ApiState<WeatherResponse>

    suspend fun getForecastOverNetwork(lat: Double,
                                      lon: Double,
                                      cnt : Int = 7,
                                      units: String = " ",
                                      lang: String = "en") : ApiState<ForecastResponse>

    suspend fun insertWeather(weatherDB: WeatherDB)
    suspend fun deleteWeather(weatherDB: WeatherDB)
    suspend fun getAllStoredWeather(): Flow<List<WeatherDB>>
    suspend fun findWeatherById(weatherId: Int): WeatherDB?
    suspend fun deleteWeatherById(weatherId: Int)


    suspend fun getAllAlerts(): Flow<List<Alert>>
    suspend fun insertAlert(alert: Alert)
    suspend fun deleteAlert(alert: Alert)
    suspend fun deleteAlertById(alertId: String)

    suspend fun getFavoriteData(): Flow<List<FavoriteDB>>
    suspend fun insertFavorite(favoriteDB: FavoriteDB)
    suspend fun deleteFavorite(favoriteDB: FavoriteDB)
    suspend fun deleteFavoriteById(favoriteId: String)
}
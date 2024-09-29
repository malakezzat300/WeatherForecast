package com.malakezzat.weatherforecast.model

import com.malakezzat.weatherforecast.misc.ApiState
import com.malakezzat.weatherforecast.database.FavoriteDB
import com.malakezzat.weatherforecast.database.IWeatherLocalDataSource
import com.malakezzat.weatherforecast.database.WeatherDB
import com.malakezzat.weatherforecast.database.WeatherLocalDataSource
import com.malakezzat.weatherforecast.network.IWeatherRemoteDataSource
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow

class WeatherRepositoryImpl(
    private var weatherRemoteDataSource: IWeatherRemoteDataSource,
    private var weatherLocalDataSource: IWeatherLocalDataSource
) : WeatherRepository , IWeatherRepository {

    override suspend fun getWeatherOverNetwork(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): ApiState<WeatherResponse> {
        return try {
            val weatherResponse = weatherRemoteDataSource.getWeatherOverNetwork(lat, lon, units, lang)
            ApiState.Success(weatherResponse)
        } catch (e: Exception) {
            ApiState.Failure(e)
        }
    }

    override suspend fun getForecastOverNetwork(
        lat: Double,
        lon: Double,
        cnt: Int,
        units: String,
        lang: String
    ): ApiState<ForecastResponse> {
        return try {
            val forecastResponse = weatherRemoteDataSource.getForecastOverNetwork(lat, lon, cnt, units, lang)
            ApiState.Success(forecastResponse)
        } catch (e: Exception) {
            ApiState.Failure(e)
        }
    }


    override suspend fun getAllStoredWeather(): Flow<List<WeatherDB>> {
        return weatherLocalDataSource.getStoredWeather()
    }

    override suspend fun findWeatherById(weatherId: Int): WeatherDB? {
        return weatherLocalDataSource.findWeatherById(weatherId)
    }

    override suspend fun deleteWeatherById(weatherId: Int) {
        weatherLocalDataSource.deleteWeatherById(weatherId)
    }

    override suspend fun insertWeather(weatherDB: WeatherDB) {
        weatherLocalDataSource.insertWeather(weatherDB)
    }

    override suspend fun deleteWeather(weatherDB: WeatherDB) {
        weatherLocalDataSource.deleteWeather(weatherDB)
    }


    override suspend fun getAllAlerts(): Flow<List<Alert>> {
        return weatherLocalDataSource.getAllAlerts()
    }

    override suspend fun insertAlert(alert: Alert) {
        weatherLocalDataSource.insertAlert(alert)
    }

    override suspend fun deleteAlert(alert: Alert) {
        weatherLocalDataSource.deleteAlert(alert)
    }

    override suspend fun deleteAlertById(alertId: String) {
        weatherLocalDataSource.deleteAlertById(alertId)
    }

    override suspend fun getFavoriteData(): Flow<List<FavoriteDB>> {
        return weatherLocalDataSource.getFavoriteData()
    }

    override suspend fun insertFavorite(favoriteDB: FavoriteDB) {
        weatherLocalDataSource.insertFavorite(favoriteDB)
    }

    override suspend fun deleteFavorite(favoriteDB: FavoriteDB) {
        weatherLocalDataSource.deleteFavorite(favoriteDB)
    }

    override suspend fun deleteFavoriteById(favoriteId: String) {
        weatherLocalDataSource.deleteFavoriteById(favoriteId)
    }

}
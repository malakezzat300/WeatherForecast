package com.malakezzat.weatherforecast.model

import com.malakezzat.weatherforecast.database.FavoriteDB
import com.malakezzat.weatherforecast.database.IWeatherLocalDataSource
import com.malakezzat.weatherforecast.database.WeatherDB
import com.malakezzat.weatherforecast.misc.ApiState
import com.malakezzat.weatherforecast.network.IWeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeWeatherRepository(
    private var weatherRemoteDataSource: IWeatherRemoteDataSource,
    private var weatherLocalDataSource: IWeatherLocalDataSource
)  : IWeatherRepository{

    private val alerts = mutableListOf<Alert>()

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
            val forecastResponse = weatherRemoteDataSource.getForecastOverNetwork(lat, lon,cnt,units,lang)
            ApiState.Success(forecastResponse)
        } catch (e: Exception) {
            ApiState.Failure(e)
        }
    }

    override suspend fun getAllStoredWeather(): Flow<List<WeatherDB>> {
        TODO("Not yet implemented")
    }

    override suspend fun findWeatherById(weatherId: Int): WeatherDB? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeatherById(weatherId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun insertWeather(weatherDB: WeatherDB) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeather(weatherDB: WeatherDB) {
        TODO("Not yet implemented")
    }

    override suspend fun insertAlert(alert: Alert) {
        weatherLocalDataSource.insertAlert(alert)
    }

    override suspend fun deleteAlert(alert: Alert) {
        weatherLocalDataSource.deleteAlert(alert)
    }

    override suspend fun getAllAlerts(): Flow<List<Alert>> {
        return weatherLocalDataSource.getAllAlerts()

    }

    override suspend fun deleteAlertById(alertId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getFavoriteData(): Flow<List<FavoriteDB>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertFavorite(favoriteDB: FavoriteDB) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFavorite(favoriteDB: FavoriteDB) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFavoriteById(favoriteId: String) {
        TODO("Not yet implemented")
    }


}
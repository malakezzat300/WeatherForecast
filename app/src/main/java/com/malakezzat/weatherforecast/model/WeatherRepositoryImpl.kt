package com.malakezzat.weatherforecast.model

import com.malakezzat.weatherforecast.database.WeatherDB
import com.malakezzat.weatherforecast.database.WeatherLocalDataSource
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSource
import kotlinx.coroutines.flow.Flow

class WeatherRepositoryImpl(
    private var weatherRemoteDataSource: WeatherRemoteDataSource,
    private var weatherLocalDataSource: WeatherLocalDataSource
) : WeatherRepository {

    override suspend fun getWeatherOverNetwork(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): WeatherResponse {
        val weatherResponse = weatherRemoteDataSource.getWeatherOverNetwork(lat, lon, units, lang)
        return weatherResponse
    }

    override suspend fun getForecastOverNetwork(
        lat: Double,
        lon: Double,
        cnt: Int,
        units: String,
        lang: String
    ): ForecastResponse {
        val forecastResponse = weatherRemoteDataSource.getForecastOverNetwork(lat, lon, cnt, units, lang)
        return forecastResponse
    }


    override suspend fun getAllStoredWeather(): Flow<List<WeatherDB>> {
        return weatherLocalDataSource.getStoredWeather()
    }

    override suspend fun findWeatherById(weatherId: Int): WeatherDB? {
        return weatherLocalDataSource.findWeatherById(weatherId)
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

}
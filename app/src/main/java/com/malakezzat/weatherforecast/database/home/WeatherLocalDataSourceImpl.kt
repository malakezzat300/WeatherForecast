package com.malakezzat.weatherforecast.database.home

import com.malakezzat.weatherforecast.database.AppDatabase
import com.malakezzat.weatherforecast.model.Alert
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.WeatherResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow

class WeatherLocalDataSourceImpl(
    private val appDatabase: AppDatabase
) : WeatherLocalDataSource {

    private var weatherDao: WeatherDao = appDatabase.weatherDAO
    private var alertDao: AlertDao = appDatabase.alertDAO

    override suspend fun getStoredWeather(): Flow<List<WeatherDB>> {
        return weatherDao.getStoredWeather()
    }

    override suspend fun insertWeather(weatherDB: WeatherDB) {
        weatherDao.insertWeatherResponse(weatherDB)
    }

    override suspend fun deleteWeather(weatherDB: WeatherDB) {
        weatherDao.deleteWeatherResponse(weatherDB)
    }

    override suspend fun findWeatherById(weatherId: Int): WeatherDB? {
        return weatherDao.findByWeatherId(weatherId)
    }

    override suspend fun getAllAlerts(): Flow<List<Alert>> {
        return alertDao.getAllAlerts()
    }

    override suspend fun insertAlert(alert: Alert) {
        alertDao.insertAlert(alert)
    }

    override suspend fun deleteAlert(alert: Alert) {
        alertDao.deleteAlert(alert)
    }

    override suspend fun deleteAlertById(alertId: String) {
        alertDao.deleteAlertById(alertId)
    }
}
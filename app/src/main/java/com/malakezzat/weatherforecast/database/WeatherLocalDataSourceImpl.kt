package com.malakezzat.weatherforecast.database

import com.malakezzat.weatherforecast.model.Alert
import kotlinx.coroutines.flow.Flow

class WeatherLocalDataSourceImpl(
    private val appDatabase: AppDatabase
) : WeatherLocalDataSource, IWeatherLocalDataSource {

    private var weatherDao: WeatherDao = appDatabase.weatherDAO
    private var alertDao: AlertDao = appDatabase.alertDAO
    private var favoriteDao: FavoriteDao = appDatabase.favoriteDao

    override suspend fun getStoredWeather(): Flow<List<WeatherDB>> {
        return weatherDao.getStoredWeather()
    }

    override suspend fun insertWeather(weatherDB: WeatherDB) {
        weatherDao.insertWeatherResponse(weatherDB)
    }

    override suspend fun deleteWeather(weatherDB: WeatherDB) {
        weatherDao.deleteWeatherResponse(weatherDB)
    }

    override suspend fun deleteWeatherById(weatherId: Int) {
        weatherDao.deleteWeatherById(weatherId)
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

    override suspend fun getFavoriteData(): Flow<List<FavoriteDB>> {
        return favoriteDao.getFavoriteData()
    }

    override suspend fun insertFavorite(favoriteDB: FavoriteDB) {
        val existingFavorite = favoriteDao.getFavoriteById(favoriteDB.id)
        if (existingFavorite == null) {
            favoriteDao.insertFavorite(favoriteDB)
        }
    }

    override suspend fun deleteFavorite(favoriteDB: FavoriteDB) {
        favoriteDao.deleteFavorite(favoriteDB)
    }

    override suspend fun deleteFavoriteById(favoriteId: String) {
        favoriteDao.deleteFavoriteById(favoriteId)
    }
}
package com.malakezzat.weatherforecast.database

import com.malakezzat.weatherforecast.model.Alert
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow

class FakeLocalDataSource(
    private var favoriteDBList: MutableList<FavoriteDB>? = mutableListOf()
): IWeatherLocalDataSource{
    override suspend fun getStoredWeather(): Flow<List<WeatherDB>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertWeather(weatherDB: WeatherDB) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeather(weatherDB: WeatherDB) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeatherById(weatherId: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun findWeatherById(weatherId: Int): WeatherDB? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllAlerts(): Flow<List<Alert>> {
        TODO("Not yet implemented")
    }

    override suspend fun insertAlert(alert: Alert) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAlert(alert: Alert) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAlertById(alertId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getFavoriteData(): Flow<List<FavoriteDB>> {
        return flow {
            emit(favoriteDBList ?: listOf())
        }
    }

    override suspend fun insertFavorite(favoriteDB: FavoriteDB) {
        if (favoriteDBList == null) {
            favoriteDBList = mutableListOf()
        }
        val existingFavorite = favoriteDBList?.find { it.id == favoriteDB.id }
        if (existingFavorite == null) {
            favoriteDBList?.add(favoriteDB)
        }
    }

    override suspend fun deleteFavorite(favoriteDB: FavoriteDB) {
        if (favoriteDBList == null) {
            favoriteDBList = mutableListOf()
        }
        val existingFavorite = favoriteDBList?.find { it.id == favoriteDB.id }
        if (existingFavorite != null) {
            favoriteDBList?.remove(favoriteDB)
        }
    }

    override suspend fun deleteFavoriteById(favoriteId: String) {
        TODO("Not yet implemented")
    }
}
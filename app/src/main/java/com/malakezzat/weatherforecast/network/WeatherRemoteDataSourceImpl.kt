package com.malakezzat.weatherforecast

import com.malakezzat.weatherforecast.model.WeatherResponse


class WeatherRemoteDataSourceImpl private constructor() : WeatherRemoteDataSource {

    private val weatherService : WeatherService by lazy {
        RetrofitHelper.getInstance().create(WeatherService::class.java)
    }

    override suspend fun getWeatherOverNetwork(lat: Double,lon: Double,units: String,lang: String)
                    : WeatherResponse {
        val response = weatherService.getWeather(lat=lat,lon=lon)
        return response
    }

    companion object {
        @Volatile
        private var instance: WeatherRemoteDataSourceImpl? = null

        fun getInstance(): WeatherRemoteDataSourceImpl {
            return instance ?: synchronized(this) {
                val tempInstance = instance ?: WeatherRemoteDataSourceImpl().also { instance = it }
                tempInstance
            }
        }
    }




}
package com.malakezzat.weatherforecast.network

import com.malakezzat.weatherforecast.RetrofitHelper
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.WeatherResponse


class WeatherRemoteDataSourceImpl private constructor() : WeatherRemoteDataSource,
    IWeatherRemoteDataSource {

    private val weatherService : WeatherService by lazy {
        RetrofitHelper.getInstance().create(WeatherService::class.java)
    }

    override suspend fun getWeatherOverNetwork(lat: Double,lon: Double,units: String,lang: String)
                    : WeatherResponse {
        val response = weatherService.getWeather(lat=lat,lon=lon,units = units,lang = lang)
        return response
    }

    override suspend fun getForecastOverNetwork(
        lat: Double,
        lon: Double,
        cnt: Int,
        units: String,
        lang: String
    ): ForecastResponse {
        val response = weatherService.getForecast(lat=lat,lon=lon, cnt = cnt, units = units,lang = lang)
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
package com.malakezzat.mvvmdemo.model

import com.malakezzat.weatherforecast.WeatherRemoteDataSource
import com.malakezzat.weatherforecast.model.WeatherResponse

class WeatherRepositoryImpl(
    private var weatherRemoteDataSource: WeatherRemoteDataSource
) : WeatherRepository {

    override suspend fun getWeatherOverNetwork(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): WeatherResponse {
       return weatherRemoteDataSource.getWeatherOverNetwork(lat,lon,units,lang)
    }
}
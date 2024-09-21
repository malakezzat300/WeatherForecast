package com.malakezzat.weatherforecast.model

import android.util.Log
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSource

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

    override suspend fun getForecastOverNetwork(
        lat: Double,
        lon: Double,
        cnt : Int,
        units: String,
        lang: String
    ): ForecastResponse {
        return weatherRemoteDataSource.getForecastOverNetwork(lat,lon,cnt,units,lang)
    }
}
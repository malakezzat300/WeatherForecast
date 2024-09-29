package com.malakezzat.weatherforecast.network

import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.WeatherResponse

interface IWeatherRemoteDataSource {
    suspend fun getWeatherOverNetwork(lat: Double, lon: Double, units: String, lang: String)
            : WeatherResponse

    suspend fun getForecastOverNetwork(
        lat: Double,
        lon: Double,
        cnt: Int,
        units: String,
        lang: String
    ): ForecastResponse
}
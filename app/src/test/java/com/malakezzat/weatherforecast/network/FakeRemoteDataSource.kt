package com.malakezzat.weatherforecast.network

import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.WeatherResponse

class FakeRemoteDataSource : IWeatherRemoteDataSource {
    override suspend fun getWeatherOverNetwork(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): WeatherResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getForecastOverNetwork(
        lat: Double,
        lon: Double,
        cnt: Int,
        units: String,
        lang: String
    ): ForecastResponse {
        TODO("Not yet implemented")
    }
}
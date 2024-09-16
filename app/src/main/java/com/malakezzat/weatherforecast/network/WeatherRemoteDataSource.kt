package com.malakezzat.weatherforecast

import com.malakezzat.weatherforecast.model.WeatherResponse


interface WeatherRemoteDataSource {
    suspend fun getWeatherOverNetwork(lat: Double,lon: Double,units: String = " ",lang: String = "en") : WeatherResponse
}
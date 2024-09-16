package com.malakezzat.mvvmdemo.model

import com.malakezzat.weatherforecast.model.WeatherResponse


interface WeatherRepository {
    suspend fun getWeatherOverNetwork(lat: Double,
                                      lon: Double,
                                      units: String = " ",
                                      lang: String = "en") : WeatherResponse

}
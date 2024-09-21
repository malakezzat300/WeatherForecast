package com.malakezzat.weatherforecast.model


interface WeatherRepository {
    suspend fun getWeatherOverNetwork(lat: Double,
                                      lon: Double,
                                      units: String = " ",
                                      lang: String = "en") : WeatherResponse

    suspend fun getForecastOverNetwork(lat: Double,
                                      lon: Double,
                                      cnt : Int = 7,
                                      units: String = " ",
                                      lang: String = "en") : ForecastResponse
}
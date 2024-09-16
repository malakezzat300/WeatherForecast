package com.malakezzat.weatherforecast


import com.malakezzat.weatherforecast.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    private val api : String
        get() = "f7635973f14207acd96142d69c3a867c"

    @GET("weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "",
        @Query("lang") lang: String = "en",
        @Query("appid") apiKey: String = api
    ): WeatherResponse

}
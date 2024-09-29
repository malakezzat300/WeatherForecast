package com.malakezzat.weatherforecast.network

import com.malakezzat.weatherforecast.database.FavoriteDB
import com.malakezzat.weatherforecast.model.City
import com.malakezzat.weatherforecast.model.Clouds
import com.malakezzat.weatherforecast.model.Coord
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.Main
import com.malakezzat.weatherforecast.model.Sys
import com.malakezzat.weatherforecast.model.Weather
import com.malakezzat.weatherforecast.model.WeatherResponse
import com.malakezzat.weatherforecast.model.Wind

class FakeRemoteDataSource : IWeatherRemoteDataSource {

    override suspend fun getWeatherOverNetwork(
        lat: Double,
        lon: Double,
        units: String,
        lang: String
    ): WeatherResponse {
        return WeatherResponse(1,
                Coord(1.23,1.23),
                listOf(
                    Weather( 802,
                    "Clouds",
                    "clear sky",
                    "03n")
                ),
                "base",
                Main(298.05,
                    298.74,
                    298.05,
                    298.05,
                    1012,
                    82,
                    1012,
                    1012,
                    "123"),
                100,
                Wind(5.56,277,5.1),
                Clouds(33),
                123456789,
                Sys(123456,456123,"eg",123456,123456),
                0,
                "0",
                200)

    }

    override suspend fun getForecastOverNetwork(
        lat: Double,
        lon: Double,
        cnt: Int,
        units: String,
        lang: String
    ): ForecastResponse {
        return ForecastResponse(2,
            "200",
            210,
            40,
            listOf(ListF(123456,
                Main(298.05,
                    298.74,
                    298.05,
                    298.05,
                    1012,
                    82,
                    1012,
                    1012,
                    "123"),
                listOf(
                    Weather( 802,
                        "Clouds",
                        "clear sky",
                        "03n")
                ),
                Clouds(15),
                Wind(5.56,277,5.1),
                123456,
                5.5,
                Sys(123456,456123,"eg",123456,123456),
                "45678912")),
                City(1,"london",
                    Coord(1.2,1.2),
                    "UK",
                    123456,
                    0,
                    45564545,
                    64),
                4554
        )
    }
}
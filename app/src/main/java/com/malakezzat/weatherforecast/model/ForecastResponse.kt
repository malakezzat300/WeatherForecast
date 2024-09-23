package com.malakezzat.weatherforecast.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

data class ForecastResponse(
    val id: Long,
    val cod: String,
    val message: Long,
    val cnt: Long,
    val list: List<ListF>,
    val city: City,
    val weatherResponseId: Int
)

data class ListF(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Long,
    val pop: Double,
    val sys: Sys,
    val dt_txt: String,
)

data class City(
    val id: Long,
    val name: String,
    val coord: Coord,
    val country: String,
    val population: Long,
    val timezone: Long,
    val sunrise: Long,
    val sunset: Long,
)

data class DayWeather(
    var day: String,
    val temp: String,
    val icon: String,
    val description: String
)
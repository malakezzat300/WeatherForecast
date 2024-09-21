package com.malakezzat.weatherforecast.model

data class ForecastResponse(
    val cod: String,
    val message: Long,
    val cnt: Long,
    val list: List<ListF>,
    val city: City,
)

data class ListF(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    val visibility: Long,
    val pop: Long,
    val sys: Sys,
    val dt_Txt: String,
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


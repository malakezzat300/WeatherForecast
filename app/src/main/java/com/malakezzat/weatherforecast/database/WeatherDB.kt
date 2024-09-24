package com.malakezzat.weatherforecast.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.malakezzat.weatherforecast.model.DayWeather
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.WeatherResponse

@Entity(tableName = "weather_response")
data class WeatherDB (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val description: String,
    val icon: String,
    var temp: Double,
    val pressure: Int,
    val humidity: Int,
    val visibility: Int,
    val wind: Double,
    val clouds: Int,
    val dt: Long,
    val country: String?,
    val sunrise: Long,
    val sunset: Long,
    val name: String,
    val isHome : Boolean,

    //temp
    val dt1: Long,
    val icon1: String,
    var tempV21 : String,
    val dt2: Long,
    val icon2: String,
    var tempV22 : String,
    val dt3: Long,
    val icon3: String,
    var tempV23 : String,
    val dt4: Long,
    val icon4: String,
    var tempV24 : String,
    val dt5: Long,
    val icon5: String,
    var tempV25 : String,
    val dt6: Long,
    val icon6: String,
    var tempV26 : String,
    val dt7: Long,
    val icon7: String,
    var tempV27 : String,

    //days
    var dayD1: String,
    val tempD1: String,
    val iconD1: String,
    val descriptionD1: String,
    var dayD2: String,
    val tempD2: String,
    val iconD2: String,
    val descriptionD2: String,
    var dayD3: String,
    val tempD3: String,
    val iconD3: String,
    val descriptionD3: String,
    var dayD4: String,
    val tempD4: String,
    val iconD4: String,
    val descriptionD4: String,
    var dayD5: String,
    val tempD5: String,
    val iconD5: String,
    val descriptionD5: String

){
    companion object {
        fun mapWeatherDB(weatherResponse: WeatherResponse, tempList: List<ListF>, dayList: List<DayWeather>, isHome: Boolean): WeatherDB {
            return WeatherDB(
                id = if (isHome) 1 else 0,
                description = weatherResponse.weather[0].description,
                icon = weatherResponse.weather[0].icon,
                temp = weatherResponse.main.temp,
                pressure = weatherResponse.main.pressure,
                humidity = weatherResponse.main.humidity,
                visibility = weatherResponse.visibility,
                wind = weatherResponse.wind.speed,
                clouds = weatherResponse.clouds.all,
                dt = weatherResponse.dt,
                country = weatherResponse.sys.country,
                sunrise = weatherResponse.sys.sunrise,
                sunset = weatherResponse.sys.sunset,
                name = weatherResponse.name,
                isHome = isHome,
                // Temperature data
                dt1 = tempList[0].dt,
                icon1 = tempList[0].weather[0].icon,
                tempV21 = tempList[0].main.tempV2,
                dt2 = tempList[1].dt,
                icon2 = tempList[1].weather[0].icon,
                tempV22 = tempList[1].main.tempV2,
                dt3 = tempList[2].dt,
                icon3 = tempList[2].weather[0].icon,
                tempV23 = tempList[2].main.tempV2,
                dt4 = tempList[3].dt,
                icon4 = tempList[3].weather[0].icon,
                tempV24 = tempList[3].main.tempV2,
                dt5 = tempList[4].dt,
                icon5 = tempList[4].weather[0].icon,
                tempV25 = tempList[4].main.tempV2,
                dt6 = tempList[5].dt,
                icon6 = tempList[5].weather[0].icon,
                tempV26 = tempList[5].main.tempV2,
                dt7 = tempList[6].dt,
                icon7 = tempList[6].weather[0].icon,
                tempV27 = tempList[6].main.tempV2,
                // Daily data
                dayD1 = dayList[0].day,
                tempD1 = dayList[0].temp,
                iconD1 = dayList[0].icon,
                descriptionD1 = dayList[0].description,
                dayD2 = dayList[1].day,
                tempD2 = dayList[1].temp,
                iconD2 = dayList[1].icon,
                descriptionD2 = dayList[1].description,
                dayD3 = dayList[2].day,
                tempD3 = dayList[2].temp,
                iconD3 = dayList[2].icon,
                descriptionD3 = dayList[2].description,
                dayD4 = dayList[3].day,
                tempD4 = dayList[3].temp,
                iconD4 = dayList[3].icon,
                descriptionD4 = dayList[3].description,
                dayD5 = dayList[4].day,
                tempD5 = dayList[4].temp,
                iconD5 = dayList[4].icon,
                descriptionD5 = dayList[4].description,
            )
        }
    }
}
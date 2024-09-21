package com.malakezzat.weatherforecast

import androidx.recyclerview.widget.DiffUtil
import com.malakezzat.weatherforecast.model.DayWeather
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.WeatherResponse

class ForecastDiffUtil : DiffUtil.ItemCallback<ListF>() {
    override fun areItemsTheSame(oldItem: ListF, newItem: ListF): Boolean {
        return oldItem.main == newItem.main
    }
    override fun areContentsTheSame(oldItem: ListF, newItem: ListF): Boolean {
        return oldItem == newItem
    }

}

class ForecastDiffUtilDays : DiffUtil.ItemCallback<DayWeather>() {
    override fun areItemsTheSame(oldItem: DayWeather, newItem: DayWeather): Boolean {
        return oldItem.temp == newItem.temp
    }

    override fun areContentsTheSame(oldItem: DayWeather, newItem: DayWeather): Boolean {
        return oldItem == newItem
    }
}

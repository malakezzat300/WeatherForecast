package com.malakezzat.weatherforecast

import androidx.recyclerview.widget.DiffUtil
import com.malakezzat.weatherforecast.model.DayTemperature
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

class ForecastDiffUtilDays : DiffUtil.ItemCallback<DayTemperature>() {
    override fun areItemsTheSame(oldItem: DayTemperature, newItem: DayTemperature): Boolean {
        return oldItem.date == newItem.date
    }

    override fun areContentsTheSame(oldItem: DayTemperature, newItem: DayTemperature): Boolean {
        return oldItem == newItem
    }
}

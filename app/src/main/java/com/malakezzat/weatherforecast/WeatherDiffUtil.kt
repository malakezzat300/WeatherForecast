package com.malakezzat.weatherforecast

import androidx.recyclerview.widget.DiffUtil
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
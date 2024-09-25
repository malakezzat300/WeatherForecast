package com.malakezzat.weatherforecast

import androidx.recyclerview.widget.DiffUtil
import com.malakezzat.weatherforecast.database.FavoriteDB
import com.malakezzat.weatherforecast.model.Alert
import com.malakezzat.weatherforecast.model.DayWeather
import com.malakezzat.weatherforecast.model.TempWeather

class ForecastDiffUtil : DiffUtil.ItemCallback<TempWeather>() {
    override fun areItemsTheSame(oldItem: TempWeather, newItem: TempWeather): Boolean {
        return oldItem.day == newItem.day
    }
    override fun areContentsTheSame(oldItem: TempWeather, newItem: TempWeather): Boolean {
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

class AlertDiffUtil : DiffUtil.ItemCallback<Alert>() {
    override fun areItemsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Alert, newItem: Alert): Boolean {
        return oldItem == newItem
    }
}

class FavoriteDiffUtil : DiffUtil.ItemCallback<FavoriteDB>() {
    override fun areItemsTheSame(oldItem: FavoriteDB, newItem: FavoriteDB): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FavoriteDB, newItem: FavoriteDB): Boolean {
        return oldItem == newItem
    }
}

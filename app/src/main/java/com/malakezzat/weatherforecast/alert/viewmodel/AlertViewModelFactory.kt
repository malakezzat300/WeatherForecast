package com.malakezzat.weatherforecast.alert.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.malakezzat.weatherforecast.home.viewmodel.HomeViewModel
import com.malakezzat.weatherforecast.model.IWeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepository

class AlertViewModelFactory(private val weatherRepository: IWeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertViewModel::class.java)) {
            return AlertViewModel(weatherRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
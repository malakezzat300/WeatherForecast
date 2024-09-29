package com.malakezzat.weatherforecast.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.malakezzat.weatherforecast.model.IWeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepository

class HomeViewModelFactory(private val weatherRepository: IWeatherRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(weatherRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
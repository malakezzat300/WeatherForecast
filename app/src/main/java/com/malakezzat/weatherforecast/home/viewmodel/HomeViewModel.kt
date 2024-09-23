package com.malakezzat.weatherforecast.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malakezzat.weatherforecast.database.home.WeatherDB
import com.malakezzat.weatherforecast.model.DayWeather
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(val weatherRepository: WeatherRepository) : ViewModel() {

    private val _currentWeather = MutableLiveData<WeatherResponse>()
    val currentWeather: LiveData<WeatherResponse> get() = _currentWeather

    private val _currentForecast = MutableLiveData<ForecastResponse>()
    val currentForecast: LiveData<ForecastResponse> get() = _currentForecast

    private val _currentForecastDays = MutableLiveData<ForecastResponse>()
    val currentForecastDays : LiveData<ForecastResponse> get() = _currentForecastDays

    fun fetchWeatherData(lat: Double, lon: Double,units : String = "",lang: String = "") {
        viewModelScope.launch {
            try {
                getWeatherData(lat, lon,units,lang)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to fetch weather data: ${e.message}")
            }
        }
    }

    fun fetchForecastData(lat: Double, lon: Double,units : String = "",lang: String = "") {
        viewModelScope.launch {
            try {
                getForecastData(lat, lon,units,lang)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to fetch forecast data: ${e.message}")
            }
        }
    }

    fun fetchForecastDataDays(lat: Double,lon: Double,cnt : Int,units : String = "",lang: String = "") {
        viewModelScope.launch {
            try {
                getForecastDataDays(lat,lon,cnt,units,lang)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to fetch forecast data: ${e.message}")
            }
        }
    }

    private suspend fun getWeatherData(lat : Double, lon : Double,units: String,lang :String) {
        val weatherData = weatherRepository.getWeatherOverNetwork(lat = lat, lon = lon, units = units, lang = lang)
        _currentWeather.postValue(weatherData)
    }


    private suspend fun getForecastData(lat : Double, lon : Double,units: String,lang :String) {
        val forecastData = weatherRepository.getForecastOverNetwork(lat = lat, lon = lon, units = units, lang = lang)
        _currentForecast.postValue(forecastData)
    }

    private suspend fun getForecastDataDays(lat : Double, lon : Double,cnt : Int,units: String,lang :String) {
        val forecastData = weatherRepository.getForecastOverNetwork(lat = lat, lon = lon, cnt = cnt, units = units, lang = lang)
        _currentForecastDays.postValue(forecastData)
    }


    fun storeWeatherData(weatherResponse: WeatherResponse, tempList: List<ListF>, dayList: List<DayWeather>, isHome: Boolean) {
        viewModelScope.launch {
            val weatherDB = WeatherDB.mapWeatherDB(weatherResponse,tempList,dayList,isHome)
            weatherRepository.insertWeather(weatherDB)
        }
    }

}
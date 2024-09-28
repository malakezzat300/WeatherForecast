package com.malakezzat.weatherforecast.home.viewmodel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.malakezzat.weatherforecast.misc.ApiState
import com.malakezzat.weatherforecast.database.WeatherDB
import com.malakezzat.weatherforecast.model.DayWeather
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(val weatherRepository: WeatherRepository) : ViewModel() {

    private val _currentWeather = MutableStateFlow<ApiState<WeatherResponse>>(ApiState.Loading)
    val currentWeather: StateFlow<ApiState<WeatherResponse>> get() = _currentWeather

    private val _currentForecast = MutableStateFlow<ApiState<ForecastResponse>>(ApiState.Loading)
    val currentForecast: StateFlow<ApiState<ForecastResponse>> get() = _currentForecast

    private val _currentForecastDays = MutableStateFlow<ApiState<ForecastResponse>>(ApiState.Loading)
    val currentForecastDays : StateFlow<ApiState<ForecastResponse>> get() = _currentForecastDays

    val combinedData = MediatorLiveData<Triple<WeatherResponse?, ForecastResponse?, ForecastResponse?>>()

    init {
        val weatherLiveData = currentWeather.asLiveData()
        val forecastLiveData = currentForecast.asLiveData()
        val forecastDaysLiveData = currentForecastDays.asLiveData()

        combinedData.addSource(weatherLiveData) { weatherState ->
            val weatherResponse = if (weatherState is ApiState.Success) weatherState.data else null
            combinedData.value = Triple(weatherResponse, currentForecast.valueOrNull(), currentForecastDays.valueOrNull())
        }

        combinedData.addSource(forecastLiveData) { forecastState ->
            val forecastResponse = if (forecastState is ApiState.Success) forecastState.data else null
            combinedData.value = Triple(currentWeather.valueOrNull(), forecastResponse, currentForecastDays.valueOrNull())
        }

        combinedData.addSource(forecastDaysLiveData) { forecastDaysState ->
            val forecastDaysResponse = if (forecastDaysState is ApiState.Success) forecastDaysState.data else null
            combinedData.value = Triple(currentWeather.valueOrNull(), currentForecast.valueOrNull(), forecastDaysResponse)
        }
    }

    private fun <T> StateFlow<ApiState<T>>.valueOrNull(): T? {
        return (this.value as? ApiState.Success)?.data
    }

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
        val weatherData =
            weatherRepository.getWeatherOverNetwork(lat = lat, lon = lon, units = units, lang = lang)
        _currentWeather.emit(weatherData)
    }


    private suspend fun getForecastData(lat : Double, lon : Double,units: String,lang :String) {
        val forecastData =
            weatherRepository.getForecastOverNetwork(lat = lat, lon = lon, units = units, lang = lang)
        _currentForecast.emit(forecastData)
    }

    private suspend fun getForecastDataDays(lat : Double, lon : Double,cnt : Int,units: String,lang :String) {
        val forecastData =
            weatherRepository.getForecastOverNetwork(lat = lat, lon = lon, cnt = cnt, units = units, lang = lang)
        _currentForecastDays.emit(forecastData)
    }


    fun storeWeatherData(weatherResponse: WeatherResponse, tempList: List<ListF>, dayList: List<DayWeather>, isHome: Int) {
        viewModelScope.launch {
            val weatherDB = WeatherDB.mapWeatherDB(weatherResponse,tempList,dayList,isHome)
            weatherRepository.insertWeather(weatherDB)
        }
    }

    suspend fun getStoredWeatherData(): WeatherDB? {
        return weatherRepository.findWeatherById(1)
    }


}
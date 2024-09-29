package com.malakezzat.weatherforecast.alert.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malakezzat.weatherforecast.misc.ApiState
import com.malakezzat.weatherforecast.model.Alert
import com.malakezzat.weatherforecast.model.IWeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class AlertViewModel(private val weatherRepository: IWeatherRepository) : ViewModel() {

    private val _alertList = MutableStateFlow<ApiState<List<Alert>>>(ApiState.Loading)
    val alertList: StateFlow<ApiState<List<Alert>>> get() = _alertList

    fun fetchAlertData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getAlertData()
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Failed to fetch Alert data: ${e.message}")
            }
        }
    }

    init {
        fetchAlertData()
    }

    private suspend fun getAlertData() {
        viewModelScope.launch {
            weatherRepository.getAllAlerts()
                .onStart { _alertList.value = ApiState.Loading
            }.catch { e ->
                _alertList.value = ApiState.Failure(e)
            }.collect { alertList ->
                _alertList.value = ApiState.Success(alertList)
            }
        }
    }

    fun removeAlert(alert: Alert) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.deleteAlert(alert)
                getAlertData()
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Failed to remove Alert: ${e.message}")
            }
        }
    }

    fun addAlert(alert: Alert) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                weatherRepository.insertAlert(alert)
                getAlertData()
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Failed to add Alert: ${e.message}")
            }
        }
    }
}
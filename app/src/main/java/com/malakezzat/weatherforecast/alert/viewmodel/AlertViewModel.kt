package com.malakezzat.weatherforecast.alert.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malakezzat.weatherforecast.model.Alert
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AlertViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {

    private val _alertList = MutableSharedFlow<List<Alert>>()
    val alertList: SharedFlow<List<Alert>> get() = _alertList

    fun fetchAlertData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getAlertData()
            } catch (e: Exception) {
                Log.e("AlertViewModel", "Failed to fetch Alert data: ${e.message}")
            }
        }
    }

    private suspend fun getAlertData() {
        weatherRepository.getAllAlerts().collect { alertList ->
            _alertList.emit(alertList)
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
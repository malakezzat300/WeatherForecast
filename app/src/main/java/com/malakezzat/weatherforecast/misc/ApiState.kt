package com.malakezzat.weatherforecast.misc

sealed class ApiState<out T> {
    object Loading : ApiState<Nothing>()
    data class Success<out T>(val data: T) : ApiState<T>()
    data class Failure(val exception: Throwable) : ApiState<Nothing>()
}
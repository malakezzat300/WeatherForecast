package com.malakezzat.weatherforecast.connection

interface ReceiverInterface {
    fun loadFromNetwork()
    fun loadFromDataBase()
}
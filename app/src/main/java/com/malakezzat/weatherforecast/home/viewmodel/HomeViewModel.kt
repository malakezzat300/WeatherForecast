package com.malakezzat.weatherforecast.home.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malakezzat.mvvmdemo.model.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(val weatherRepository: WeatherRepository) : ViewModel() {

//    companion object{
//        private const val TAG: String = "HomeViewModel"
//    }

//    private val _productList = MutableLiveData<List<Product>>()
//    val onlineProduct : LiveData<List<Product>> = _productList
//
//    init {
//        getAllProducts()
//    }

//    fun insertProduct(productDB: ProductDB){
//        viewModelScope.launch(Dispatchers.IO) {
//            _iRepo.insertProduct(productDB)
//            getAllProducts()
//        }
//    }
//
//    fun deleteProduct(productDB: ProductDB){
//        viewModelScope.launch(Dispatchers.IO) {
//            _iRepo.deleteProduct(productDB)
//            getAllProducts()
//        }
//    }
//
//    private fun getWeatherOverNetwork(){
//        viewModelScope.launch(Dispatchers.IO) {
//            val product = weatherRepository.getWeatherOverNetwork(lat = 1.2, lon = 1.2)
//            withContext(Dispatchers.Main){
//                Log.i(TAG, "getAllProducts: $product")
//                _productList.postValue(product)
//            }
//        }
//    }

}
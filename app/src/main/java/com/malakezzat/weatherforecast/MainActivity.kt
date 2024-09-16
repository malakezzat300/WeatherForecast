package com.malakezzat.weatherforecast

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.malakezzat.weatherforecast.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        binding.lifecycleOwner = this




//        val weatherRemoteDataSource = WeatherRemoteDataSourceImpl.getInstance()
//
//        CoroutineScope(Dispatchers.Default).launch {
//            val weatherResponse = weatherRemoteDataSource.getWeatherOverNetwork(lat = 27.1803844,lon = 31.1851445)
//
//            Log.i("MainActivity", "weatherResponse: $weatherResponse")
//            Log.i("MainActivity", "weather: ${weatherResponse.weather[0]}")
//            Log.i("MainActivity", "timezone: ${weatherResponse.timezone}")
//            Log.i("MainActivity", "name: ${weatherResponse.name}")
//
//        }

    }
}
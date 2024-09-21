package com.malakezzat.weatherforecast.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepositoryImpl
import com.malakezzat.weatherforecast.InitActivity
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSourceImpl
import com.malakezzat.weatherforecast.databinding.FragmentHomeBinding
import com.malakezzat.weatherforecast.home.viewmodel.HomeViewModel
import com.malakezzat.weatherforecast.home.viewmodel.HomeViewModelFactory
import com.malakezzat.weatherforecast.model.DayWeather
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.ListF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {

    private val TAG : String = "HomeFragment"
    private lateinit var viewModel: HomeViewModel
    private lateinit var factory: HomeViewModelFactory
    private lateinit var repository : WeatherRepository
    private lateinit var binding : FragmentHomeBinding
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : Editor
    private lateinit var fusedClient : FusedLocationProviderClient
    private lateinit var units : String
    private lateinit var lang : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = WeatherRepositoryImpl(WeatherRemoteDataSourceImpl.getInstance())

        factory = HomeViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()


        if(sharedPreferences.getBoolean(getString(R.string.gps_pref),false)){
            getFreshLocation()
        } else if(sharedPreferences.getBoolean(getString(R.string.map_pref),false)){

        }

        if(sharedPreferences.getBoolean(getString(R.string.celsius_pref),false)){
            units = "metric"
        } else if(sharedPreferences.getBoolean(getString(R.string.fahrenheit_pref),false)){
            units = "imperial"
        } else {
            units = "standard"
        }

        if(sharedPreferences.getBoolean(getString(R.string.arabic_pref),false)){
            lang = "ar"
        } else {
            lang = "en"
        }

        var lat = sharedPreferences.getString(getString(R.string.lat),"0.0") ?: "0.0"
        var lon = sharedPreferences.getString(getString(R.string.lon),"0.0") ?: "0.0"

        Log.i(TAG, "onViewCreated: lat: $lat")
        Log.i(TAG, "onViewCreated: lon: $lon")

        viewModel.fetchWeatherData(lat.toDouble(), lon.toDouble(),units,lang)
        viewModel.currentWeather.observe(viewLifecycleOwner, Observer { weatherResponse ->
            Log.i(TAG, "onViewCreated: ${weatherResponse.dt}")
            binding.weatherResponse = weatherResponse
            binding.date = dateConverter(weatherResponse.dt)
            setIcon(weatherResponse.weather[0].icon)
            binding.sunset = dateConverterForSun(weatherResponse.sys.sunset)
            binding.sunrise = dateConverterForSun(weatherResponse.sys.sunrise)
            binding.windSpeed = getFormattedWindSpeed(weatherResponse.wind.speed)
            binding.temp = setFormattedTemperature(weatherResponse.main.temp)
        })

        viewModel.fetchForecastData(lat.toDouble(), lon.toDouble(),units,lang)
        viewModel.currentForecast.observe(viewLifecycleOwner, Observer { forecastResponse ->
            val recyclerAdapter = TempAdapter(requireContext())

            recyclerAdapter.submitList(refactorTemperatureList(forecastResponse.list).toMutableList())
            binding.tempRecyclerView.apply {
                adapter = recyclerAdapter
                layoutManager = LinearLayoutManager(context).apply {
                    orientation = RecyclerView.HORIZONTAL
                }
            }
        })

        viewModel.fetchForecastDataDays(lat.toDouble(), lon.toDouble(),40,units,lang)
        viewModel.currentForecastDays.observe(viewLifecycleOwner, Observer { forecastResponse ->
            val recyclerAdapter = DayAdapter(requireContext())
            Log.i(TAG, "onViewCreated: $forecastResponse")

            recyclerAdapter.submitList(filterUniqueDaysWithMinMax(forecastResponse.list).toMutableList())
            binding.daysRecyclerView.apply {
                adapter = recyclerAdapter
                layoutManager = LinearLayoutManager(context).apply {
                    orientation = RecyclerView.VERTICAL
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            if(sharedPreferences.getBoolean(getString(R.string.gps_pref),false)){
                getFreshLocation()
            } else if(sharedPreferences.getBoolean(getString(R.string.gps_pref),false)) {
                viewModel.fetchForecastData(lat.toDouble(), lon.toDouble(),units,lang)
                viewModel.fetchForecastData(lat.toDouble(), lon.toDouble(),units,lang)
                viewModel.fetchForecastDataDays(lat.toDouble(), lon.toDouble(),40,units,lang)
            }
            binding.swipeRefresh.isRefreshing = false
        }


    }

    @SuppressLint("MissingPermission")
    fun getFreshLocation() {
        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 300000)
            .setMinUpdateIntervalMillis(300000)
            .setMaxUpdateDelayMillis(600000)
            .build()

        fusedClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)

                    if (locationResult.locations.isNotEmpty()) {
                        val location : Location? = locationResult.lastLocation
                        Log.i(InitActivity.TAG, "Location updated: ${location?.latitude}, ${location?.longitude}")

                        val lat : String = location?.latitude.toString()
                        val lon : String = location?.longitude.toString()
                        editor.putString(requireContext().getString(R.string.lat_pref), lat)
                        editor.putString(requireContext().getString(R.string.lon_pref), lon)
                        editor.apply()

                        // Fetch updated weather data
                        viewModel.fetchWeatherData(lat.toDouble(), lon.toDouble(),units,lang)
                        viewModel.fetchForecastData(lat.toDouble(), lon.toDouble(),units,lang)
                        viewModel.fetchForecastDataDays(lat.toDouble(), lon.toDouble(),40,units,lang)
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    fun dateConverter(dt : Long) : String{
        val date = Date(dt * 1000)
        val sdf = SimpleDateFormat("EEE, dd MMM - hh:mm a")
        sdf.setTimeZone(TimeZone.getDefault())
        val formattedDate: String = sdf.format(date)
        return formattedDate
    }

    fun dateConverterForSun(dt : Long) : String{
        val date = Date(dt * 1000)
        val sdf = SimpleDateFormat("hh:mm a")
        sdf.setTimeZone(TimeZone.getDefault())
        val formattedDate: String = sdf.format(date)
        return formattedDate
    }


    fun setFormattedTemperature(temperature: Double?) : String {
        var result = ""
        if(sharedPreferences.getBoolean(getString(R.string.celsius_pref),false)){
            result = String.format("%.1f " + getString(R.string.celsius), temperature)
        } else if(sharedPreferences.getBoolean(getString(R.string.fahrenheit_pref),false)){
            result = String.format("%.1f " + getString(R.string.fahrenheit), temperature)
        } else {
            result = String.format("%.1f " + getString(R.string.kelvin), temperature)
        }
         return result
    }

    fun setFormattedTemperature(minTemperature: Double?,maxTemperature: Double?) : String {
        var result = ""
        if(sharedPreferences.getBoolean(getString(R.string.celsius_pref),false)){
            result = String.format("%.1f / %.1f " + getString(R.string.celsius), maxTemperature , minTemperature)
        } else if(sharedPreferences.getBoolean(getString(R.string.fahrenheit_pref),false)){
            result = String.format("%.1f / %.1f " + getString(R.string.fahrenheit), maxTemperature , minTemperature)
        } else {
            result = String.format("%.1f / %.1f " + getString(R.string.kelvin), maxTemperature , minTemperature)
        }
        return result
    }

    fun refactorTemperatureList(list: List<ListF>): List<ListF> {
        return list.map { item ->
            item.apply {
                main.tempV2 = setFormattedTemperature(main.temp)
            }
        }
    }


    fun setIcon(icon : String){
        Glide
            .with(requireContext())
            .load("https://openweathermap.org/img/wn/${icon}@2x.png")
            .centerCrop()
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(binding.stateImage)
    }

    fun filterUniqueDaysWithMinMax(listF: List<ListF>): List<DayWeather> {
        val today = getCurrentDate()

        val groupedByDay = listF.groupBy { item -> item.dt_txt.substring(0, 10) }

        return groupedByDay
            .filterKeys { it > today }
            .map { (date, dayList) ->
                val day = convertToDayOfWeek(dayList.first().dt_txt)

                val minTemp = dayList.minOf { it.main.temp_min }
                val maxTemp = dayList.maxOf { it.main.temp_max }

                val temp = setFormattedTemperature(minTemp,maxTemp)

                val entryAtNoon = dayList.find { it.dt_txt.contains("09:00:00") }
                val representativeWeather = entryAtNoon?.weather?.firstOrNull()

                val icon = representativeWeather?.icon ?: dayList.first().weather.firstOrNull()?.icon ?: ""
                val description = representativeWeather?.description ?: dayList.first().weather.firstOrNull()?.description ?: ""

                DayWeather(day, temp, icon, description)
            }
    }

    fun convertToDayOfWeek(dateString: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date: Date = sdf.parse(dateString) ?: Date()

        val outputSdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return outputSdf.format(date)
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun getFormattedWindSpeed(metersPerSec: Double): String {
        if(sharedPreferences.getBoolean(getString(R.string.mile_hour_pref),false)){
            val formattedSpeed = String.format("%.3f", metersPerSec * 2.23694)
            return "$formattedSpeed ${getString(R.string.mile_hour)}"
        } else {
            return "$metersPerSec ${getString(R.string.meter_sec)}"
        }

    }

}
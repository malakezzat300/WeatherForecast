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
    private lateinit var viewModelFactory: HomeViewModelFactory
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : Editor
    private lateinit var fusedClient : FusedLocationProviderClient



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
        binding.lifecycleOwner = this

        repository = WeatherRepositoryImpl(WeatherRemoteDataSourceImpl.getInstance())

        factory = HomeViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        var lat = sharedPreferences.getString(getString(R.string.lat),"0.0") ?: "0.0"
        var lon = sharedPreferences.getString(getString(R.string.lon),"0.0") ?: "0.0"

        Log.i(TAG, "onViewCreated: lat: $lat")
        Log.i(TAG, "onViewCreated: lon: $lon")

        getFreshLocation()

        viewModel.fetchWeatherData(lat.toDouble(), lon.toDouble())
        viewModel.currentWeather.observe(viewLifecycleOwner, Observer { weatherResponse ->
            Log.i(TAG, "onViewCreated: ${weatherResponse.dt}")
            binding.weatherResponse = weatherResponse
            binding.date = dateConverter(weatherResponse.dt)
            setIcon(weatherResponse.weather[0].icon)
            binding.sunset = dateConverterForSun(weatherResponse.sys.sunset)
            binding.sunrise = dateConverterForSun(weatherResponse.sys.sunrise)
        })

        viewModel.fetchForecastData(lat.toDouble(), lon.toDouble())
        viewModel.currentForecast.observe(viewLifecycleOwner, Observer { forecastResponse ->
            val recyclerAdapter = TempAdapter(requireContext())
            recyclerAdapter.submitList(forecastResponse.list.toMutableList())
            binding.tempRecyclerView.apply {
                adapter = recyclerAdapter
                layoutManager = LinearLayoutManager(context).apply {
                    orientation = RecyclerView.HORIZONTAL
                }
            }
        })

        viewModel.fetchForecastDataDays(lat.toDouble(), lon.toDouble(),40)
        viewModel.currentForecastDays.observe(viewLifecycleOwner, Observer { forecastResponse ->
            val recyclerAdapter = DayAdapter(requireContext())


            recyclerAdapter.submitList(filterUniqueDaysWithMinMax(forecastResponse.list).toMutableList())
            binding.daysRecyclerView.apply {
                adapter = recyclerAdapter
                layoutManager = LinearLayoutManager(context).apply {
                    orientation = RecyclerView.VERTICAL
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            getFreshLocation()
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
                        editor.putString(requireContext().getString(R.string.lat), lat)
                        editor.putString(requireContext().getString(R.string.lon), lon)
                        editor.apply()

                        // Fetch updated weather data
                        viewModel.fetchWeatherData(lat.toDouble(), lon.toDouble())
                        viewModel.fetchForecastData(lat.toDouble(), lon.toDouble())
                        viewModel.fetchForecastDataDays(lat.toDouble(), lon.toDouble(),40)
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

    companion object{
        @BindingAdapter("formattedTemperature")
        @JvmStatic
        fun setFormattedTemperature(textView: TextView, temperature: Double) {
            textView.text = String.format("%.1f K", temperature)

            //textView.text = String.format("%.1f °C", temperature)
            //textView.text = String.format("%.1f °F", temperature)
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

                val temp = String.format("%.1f / %.1f K", maxTemp, minTemp)

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
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

}
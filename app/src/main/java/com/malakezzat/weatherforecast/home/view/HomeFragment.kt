package com.malakezzat.weatherforecast.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.malakezzat.weatherforecast.misc.ApiState
import com.malakezzat.weatherforecast.connection.ConnectionBroadcastReceiver
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepositoryImpl
import com.malakezzat.weatherforecast.init.InitActivity
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.connection.ReceiverInterface
import com.malakezzat.weatherforecast.database.AppDatabase
import com.malakezzat.weatherforecast.database.WeatherLocalDataSourceImpl
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSourceImpl
import com.malakezzat.weatherforecast.databinding.FragmentHomeBinding
import com.malakezzat.weatherforecast.home.viewmodel.HomeViewModel
import com.malakezzat.weatherforecast.home.viewmodel.HomeViewModelFactory
import com.malakezzat.weatherforecast.model.DayWeather
import com.malakezzat.weatherforecast.model.IWeatherRepository
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.TempWeather
import com.malakezzat.weatherforecast.model.WeatherResponse
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() , ReceiverInterface {

    private val TAG: String = "HomeFragment"
    private lateinit var viewModel: HomeViewModel
    private lateinit var factory: HomeViewModelFactory
    private lateinit var repository: IWeatherRepository
    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var units: String
    private lateinit var lang: String
    private lateinit var weatherResponseStore: WeatherResponse
    private lateinit var tempListStore: List<ListF>
    private lateinit var dayListStore: List<DayWeather>
    private val isHome = true
    private lateinit var connectionBroadcastReceiver: ConnectionBroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = WeatherRepositoryImpl(
            WeatherRemoteDataSourceImpl.getInstance(), WeatherLocalDataSourceImpl(
                AppDatabase.getInstance(requireContext())
            )
        )

        factory = HomeViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.my_preference),
            Context.MODE_PRIVATE
        )
        editor = sharedPreferences.edit()


        if (sharedPreferences.getBoolean(getString(R.string.gps_pref), false)) {
            getFreshLocation()
        } else if (sharedPreferences.getBoolean(getString(R.string.map_pref), false)) {

        }

        if (sharedPreferences.getBoolean(getString(R.string.celsius_pref), false)) {
            units = "metric"
        } else if (sharedPreferences.getBoolean(getString(R.string.fahrenheit_pref), false)) {
            units = "imperial"
        } else {
            units = "standard"
        }

        if (sharedPreferences.getBoolean(getString(R.string.arabic_pref), false)) {
            lang = "ar"
        } else {
            lang = "en"
        }

        var lat = sharedPreferences.getString(getString(R.string.lat), "0.0") ?: "0.0"
        var lon = sharedPreferences.getString(getString(R.string.lon), "0.0") ?: "0.0"

        Log.i(TAG, "onViewCreated: lat: $lat")
        Log.i(TAG, "onViewCreated: lon: $lon")

        viewModel.fetchWeatherData(lat.toDouble(), lon.toDouble(), units, lang)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentWeather.collect { weatherState ->
                    when (weatherState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            binding.progressBarDetails.visibility = View.GONE
                            val weatherResponse = weatherState.data
                            Log.i(TAG, "Weather data: ${weatherResponse.dt}")
                            weatherResponseStore = weatherResponse
                            binding.weatherResponse = weatherResponse
                            binding.date = dateConverter(weatherResponse.dt)
                            setIcon(weatherResponse.weather[0].icon)
                            binding.sunset = dateConverterForSun(weatherResponse.sys.sunset)
                            binding.sunrise = dateConverterForSun(weatherResponse.sys.sunrise)
                            binding.windSpeed = getFormattedWindSpeed(weatherResponse.wind.speed)
                            binding.temp = setFormattedTemperature(weatherResponse.main.temp)
                        }

                        is ApiState.Failure -> {
                            binding.progressBarDetails.visibility = View.GONE
                        }
                    }
                }
            }
        }

        viewModel.fetchForecastData(lat.toDouble(), lon.toDouble(), units, lang)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentForecast.collect { forecastState ->
                    when (forecastState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            binding.progressBarDetails.visibility = View.GONE
                            val forecastResponse = forecastState.data
                            val recyclerAdapter = TempAdapter(requireContext())
                            tempListStore = refactorTemperatureList(forecastResponse.list)
                            recyclerAdapter.submitList(convertListFToTempWeatherList(tempListStore).toMutableList())
                            binding.tempRecyclerView.apply {
                                adapter = recyclerAdapter
                                layoutManager = LinearLayoutManager(requireContext()).apply {
                                    orientation = RecyclerView.HORIZONTAL
                                }
                            }
                        }

                        is ApiState.Failure -> {
                            binding.progressBarDetails.visibility = View.GONE
                        }
                    }
                }
            }
        }

        viewModel.fetchForecastDataDays(lat.toDouble(), lon.toDouble(), 40, units, lang)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentForecastDays.collect { forecastDaysState ->
                    when (forecastDaysState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                            val forecastResponse = forecastDaysState.data
                            val recyclerAdapter = DayAdapter(requireContext())
                            dayListStore = filterUniqueDaysWithMinMax(forecastResponse.list)
                            recyclerAdapter.submitList(dayListStore.toMutableList())
                            binding.daysRecyclerView.apply {
                                adapter = recyclerAdapter
                                layoutManager = LinearLayoutManager(requireContext()).apply {
                                    orientation = RecyclerView.VERTICAL
                                }
                            }
                        }

                        is ApiState.Failure -> {
                            binding.progressBarDetails.visibility = View.GONE
                        }
                    }
                }
            }
        }

        viewModel.combinedData.observe(viewLifecycleOwner) { (weatherResponse, forecastResponse, forecastDaysResponse) ->
            if (weatherResponse != null && forecastResponse != null && forecastDaysResponse != null) {
                storeHomeWeather()
            }
        }



        binding.swipeRefresh.setOnRefreshListener {
            if (sharedPreferences.getBoolean(getString(R.string.gps_pref), false)) {
                getFreshLocation()
            } else if (sharedPreferences.getBoolean(getString(R.string.gps_pref), false)) {
                viewModel.fetchForecastData(lat.toDouble(), lon.toDouble(), units, lang)
                viewModel.fetchForecastData(lat.toDouble(), lon.toDouble(), units, lang)
                viewModel.fetchForecastDataDays(lat.toDouble(), lon.toDouble(), 40, units, lang)
            }
            storeHomeWeather()
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
                        val location: Location? = locationResult.lastLocation
                        Log.i(
                            InitActivity.TAG,
                            "Location updated: ${location?.latitude}, ${location?.longitude}"
                        )

                        val lat: String = location?.latitude.toString()
                        val lon: String = location?.longitude.toString()
                        editor.putString("lat", lat)
                        editor.putString("lon", lon)
                        editor.apply()

                        viewModel.fetchWeatherData(lat.toDouble(), lon.toDouble(), units, lang)
                        viewModel.fetchForecastData(lat.toDouble(), lon.toDouble(), units, lang)
                        viewModel.fetchForecastDataDays(
                            lat.toDouble(),
                            lon.toDouble(),
                            40,
                            units,
                            lang
                        )
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    fun dateConverter(dt: Long): String {
        val date = Date(dt * 1000)
        val sdf = SimpleDateFormat("EEE, dd MMM - hh:mm a")
        sdf.setTimeZone(TimeZone.getDefault())
        val formattedDate: String = sdf.format(date)
        return formattedDate
    }

    fun dateConverterForSun(dt: Long): String {
        val date = Date(dt * 1000)
        val sdf = SimpleDateFormat("hh:mm a")
        sdf.setTimeZone(TimeZone.getDefault())
        val formattedDate: String = sdf.format(date)
        return formattedDate
    }


    fun setFormattedTemperature(temperature: Double?): String {
        var result = ""
        if (sharedPreferences.getBoolean(getString(R.string.celsius_pref), false)) {
            result = String.format("%.1f " + getString(R.string.celsius), temperature)
        } else if (sharedPreferences.getBoolean(getString(R.string.fahrenheit_pref), false)) {
            result = String.format("%.1f " + getString(R.string.fahrenheit), temperature)
        } else {
            result = String.format("%.1f " + getString(R.string.kelvin), temperature)
        }
        return result
    }

    fun setFormattedTemperature(minTemperature: Double?, maxTemperature: Double?): String {
        var result = ""
        if (sharedPreferences.getBoolean(getString(R.string.celsius_pref), false)) {
            result = String.format(
                "%.1f / %.1f " + getString(R.string.celsius),
                maxTemperature,
                minTemperature
            )
        } else if (sharedPreferences.getBoolean(getString(R.string.fahrenheit_pref), false)) {
            result = String.format(
                "%.1f / %.1f " + getString(R.string.fahrenheit),
                maxTemperature,
                minTemperature
            )
        } else {
            result = String.format(
                "%.1f / %.1f " + getString(R.string.kelvin),
                maxTemperature,
                minTemperature
            )
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


    fun setIcon(icon: String) {
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
        val firstDayKey = groupedByDay.keys.firstOrNull()

        return groupedByDay
            .filterKeys { it > today && it != firstDayKey  }
            .map { (date, dayList) ->
                val day = convertToDayOfWeek(dayList.first().dt_txt)

                val minTemp = dayList.minOf { it.main.temp_min }
                val maxTemp = dayList.maxOf { it.main.temp_max }

                val temp = setFormattedTemperature(minTemp, maxTemp)

                val entryAtNoon = dayList.find { it.dt_txt.contains("09:00:00") }
                val representativeWeather = entryAtNoon?.weather?.firstOrNull()

                val icon =
                    representativeWeather?.icon ?: dayList.first().weather.firstOrNull()?.icon ?: ""
                val description = representativeWeather?.description
                    ?: dayList.first().weather.firstOrNull()?.description ?: ""

                DayWeather(day, temp, icon, description)
            }
    }

    fun convertListFToTempWeatherList(listFList: List<ListF>): List<TempWeather> {
        return listFList.map { listF ->
            TempWeather(
                day = listF.dt,
                icon = listF.weather.firstOrNull()?.icon ?: "",
                tempV2 = listF.main.tempV2
            )
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
        if (sharedPreferences.getBoolean(getString(R.string.mile_hour_pref), false)) {
            val formattedSpeed = String.format("%.3f", metersPerSec * 2.23694)
            return "$formattedSpeed ${getString(R.string.mile_hour)}"
        } else {
            return "$metersPerSec ${getString(R.string.meter_sec)}"
        }

    }

    fun storeHomeWeather() {
        if(this::weatherResponseStore.isInitialized
        && this::tempListStore.isInitialized
        && this::dayListStore.isInitialized)
        {
            viewModel.storeWeatherData(weatherResponseStore, tempListStore, dayListStore, 1)
        }
    }

    override fun loadFromNetwork() {
        val lat = sharedPreferences.getString(getString(R.string.lat), "0.0")?.toDouble() ?: 0.0
        val lon = sharedPreferences.getString(getString(R.string.lon), "0.0")?.toDouble() ?: 0.0

        viewModel.fetchWeatherData(lat, lon, units, lang)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentWeather.collect { weatherState ->
                    when (weatherState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            binding.progressBarDetails.visibility = View.GONE
                            val weatherResponse = weatherState.data
                            Log.i(TAG, "Weather data fetched successfully.")
                            weatherResponseStore = weatherResponse
                            binding.weatherResponse = weatherResponse
                            binding.date = dateConverter(weatherResponse.dt)
                            setIcon(weatherResponse.weather[0].icon)
                            binding.sunset = dateConverterForSun(weatherResponse.sys.sunset)
                            binding.sunrise = dateConverterForSun(weatherResponse.sys.sunrise)
                            binding.windSpeed = getFormattedWindSpeed(weatherResponse.wind.speed)
                            binding.temp = setFormattedTemperature(weatherResponse.main.temp)
                        }

                        is ApiState.Failure -> {
                            binding.progressBarDetails.visibility = View.GONE
                        }
                    }
                }
            }
        }

        viewModel.fetchForecastData(lat, lon, units, lang)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.currentForecast.collect { forecastState ->
                    when (forecastState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }
                        is ApiState.Success -> {
                            binding.progressBarDetails.visibility = View.GONE
                            val forecastResponse = forecastState.data
                            val recyclerAdapter = TempAdapter(requireContext())
                            tempListStore = refactorTemperatureList(forecastResponse.list)
                            recyclerAdapter.submitList(convertListFToTempWeatherList(tempListStore).toMutableList())
                            binding.tempRecyclerView.adapter = recyclerAdapter
                        }
                        is ApiState.Failure -> {
                            binding.progressBarDetails.visibility = View.GONE
                        }
                    }
                }
            }
        }

        viewModel.fetchForecastDataDays(lat, lon, 40, units, lang)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentForecastDays.collect { forecastDaysState ->
                    when (forecastDaysState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            binding.progressBarDetails.visibility = View.GONE
                            val forecastResponse = forecastDaysState.data
                            val recyclerAdapter = DayAdapter(requireContext())
                            dayListStore = filterUniqueDaysWithMinMax(forecastResponse.list)
                            recyclerAdapter.submitList(dayListStore.toMutableList())
                            binding.daysRecyclerView.adapter = recyclerAdapter
                        }

                        is ApiState.Failure -> {
                            binding.progressBarDetails.visibility = View.GONE
                        }
                    }
                }
            }
        }

    }

    override fun loadFromDataBase() {
        Log.i(TAG, "loadFromDataBase: no internet")
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val weatherData = viewModel.getStoredWeatherData()
                if (weatherData != null) {
                    Log.i(TAG, "loadFromDataBase: Weather data loaded from database successfully.")
                    binding.cityName.text = weatherData.name
                    binding.date = dateConverter(weatherData.dt)
                    binding.weatherState.text = weatherData.description
                    setIcon(weatherData.icon)

                    val temperatureDataList = weatherData.getTemperatureData()
                    val recyclerAdapterTemp = TempAdapter(requireContext())
                    recyclerAdapterTemp.submitList(temperatureDataList.toMutableList())
                    binding.tempRecyclerView.adapter = recyclerAdapterTemp

                    val dailyDataList = weatherData.getDailyData()
                    val recyclerAdapterDays = DayAdapter(requireContext())
                    recyclerAdapterDays.submitList(dailyDataList.toMutableList())
                    binding.daysRecyclerView.apply {
                        adapter = recyclerAdapterDays
                        layoutManager = LinearLayoutManager(requireContext()).apply {
                            orientation = RecyclerView.VERTICAL
                        }
                    }

                    binding.cloudText.text =
                        weatherData.clouds.toString() + getString(R.string.percentage)
                    binding.humidityText.text =
                        weatherData.humidity.toString() + getString(R.string.percentage)
                    binding.pressureText.text =
                        weatherData.pressure.toString() + getString(R.string.hpa)
                    binding.sunset = dateConverterForSun(weatherData.sunset)
                    binding.sunrise = dateConverterForSun(weatherData.sunrise)
                    binding.windSpeed = getFormattedWindSpeed(weatherData.wind)
                    binding.temp = setFormattedTemperature(weatherData.temp)
                } else {
                    Log.e(TAG, "loadFromDataBase: No weather data found in database.")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        connectionBroadcastReceiver = ConnectionBroadcastReceiver(this)
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireContext().registerReceiver(connectionBroadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(connectionBroadcastReceiver)
    }


}
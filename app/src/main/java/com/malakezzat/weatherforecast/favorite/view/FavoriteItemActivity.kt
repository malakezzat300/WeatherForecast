package com.malakezzat.weatherforecast.favorite.view

import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.net.ConnectivityManager
import android.os.Bundle
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
import com.malakezzat.weatherforecast.misc.ApiState
import com.malakezzat.weatherforecast.connection.ConnectionBroadcastReceiver
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.connection.ReceiverInterface
import com.malakezzat.weatherforecast.database.AppDatabase
import com.malakezzat.weatherforecast.database.WeatherLocalDataSourceImpl
import com.malakezzat.weatherforecast.databinding.FragmentHomeBinding
import com.malakezzat.weatherforecast.favorite.viewmodel.FavoriteViewModel
import com.malakezzat.weatherforecast.favorite.viewmodel.FavoriteViewModelFactory
import com.malakezzat.weatherforecast.home.view.DayAdapter
import com.malakezzat.weatherforecast.home.view.TempAdapter
import com.malakezzat.weatherforecast.model.DayWeather
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.TempWeather
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepositoryImpl
import com.malakezzat.weatherforecast.model.WeatherResponse
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FavoriteItemActivity(val lat : Double,val lon : Double,val units : String,val lang :String,val id : String)
    : Fragment() , ReceiverInterface {

    private val TAG: String = "FavoriteItemActivity"
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var factory: FavoriteViewModelFactory
    private lateinit var repository: WeatherRepository
    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: Editor
    private lateinit var weatherResponseStore: WeatherResponse
    private lateinit var tempListStore: List<ListF>
    private lateinit var dayListStore: List<DayWeather>
    private val isHome = false
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

        factory = FavoriteViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory).get(FavoriteViewModel::class.java)

        sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.my_preference),
            Context.MODE_PRIVATE
        )
        editor = sharedPreferences.edit()

        viewModel.fetchWeatherData(lat = lat, lon = lon,units,lang)
        viewModel.fetchForecastData(lat = lat, lon = lon,units,lang)
        viewModel.fetchForecastDataDays(lat = lat, lon = lon,40,units,lang)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentWeather.collect { apiState ->
                    when (apiState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            val weatherResponse = apiState.data
                            binding.progressBarDetails.visibility = View.GONE
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentForecast.collect { apiState ->
                    when (apiState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            val forecastResponse = apiState.data
                            binding.progressBarDetails.visibility = View.GONE
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentForecastDays.collect { apiState ->
                    when (apiState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            val forecastResponse = apiState.data
                            binding.progressBarDetails.visibility = View.GONE
                            val recyclerAdapter = DayAdapter(requireContext())
                            Log.i(TAG, "Forecast days data received: $forecastResponse")
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
                storeHomeWeather(id.toInt())
            }
        }



        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchForecastData(lat, lon, units, lang)
            viewModel.fetchForecastData(lat, lon, units, lang)
            viewModel.fetchForecastDataDays(lat, lon, 40, units, lang)
            storeHomeWeather(id.toInt())
            binding.swipeRefresh.isRefreshing = false
        }




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

    fun storeHomeWeather(id : Int) {
        if(this::weatherResponseStore.isInitialized
            && this::tempListStore.isInitialized
            && this::dayListStore.isInitialized)
        {
            viewModel.storeFavoriteData(weatherResponseStore, tempListStore, dayListStore,id )
        }
    }

    override fun loadFromNetwork() {
        viewModel.fetchWeatherData(lat, lon, units, lang)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentWeather.collect { apiState ->
                    when (apiState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            val weatherResponse = apiState.data
                            binding.progressBarDetails.visibility = View.GONE
                            Log.i(TAG, "Weather data received: ${weatherResponse.dt}")
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentForecast.collect { apiState ->
                    when (apiState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            val forecastResponse = apiState.data
                            binding.progressBarDetails.visibility = View.GONE
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

        viewModel.fetchForecastDataDays(lat, lon, 40, units, lang)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentForecastDays.collect { apiState ->
                    when (apiState) {
                        is ApiState.Loading -> {
                            binding.progressBarDetails.visibility = View.VISIBLE
                        }

                        is ApiState.Success -> {
                            val forecastResponse = apiState.data
                            binding.progressBarDetails.visibility = View.GONE
                            val recyclerAdapter = DayAdapter(requireContext())
                            Log.i(TAG, "Forecast days data received: $forecastResponse")
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
    }

    override fun loadFromDataBase() {
        Log.i(TAG, "loadFromDataBase: no internet")
        lifecycleScope.launch {
            val weatherData = viewModel.findByWeatherId(id.toInt())
            Log.i("favoriteTest", "loadFromDataBase: ${id}")
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

                binding.cloudText.text = weatherData.clouds.toString() + getString(R.string.percentage)
                binding.humidityText.text = weatherData.humidity.toString() + getString(R.string.percentage)
                binding.pressureText.text = weatherData.pressure.toString() + getString(R.string.hpa)
                binding.sunset = dateConverterForSun(weatherData.sunset)
                binding.sunrise = dateConverterForSun(weatherData.sunrise)
                binding.windSpeed = getFormattedWindSpeed(weatherData.wind)
                binding.temp = setFormattedTemperature(weatherData.temp)
            } else {
                Log.e(TAG, "loadFromDataBase: No weather data found in database.")
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
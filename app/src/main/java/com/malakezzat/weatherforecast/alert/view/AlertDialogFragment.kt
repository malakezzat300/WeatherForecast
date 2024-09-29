package com.malakezzat.weatherforecast.alert.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.malakezzat.weatherforecast.misc.ApiState
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.alert.worker.AlertWorker
import com.malakezzat.weatherforecast.database.AppDatabase
import com.malakezzat.weatherforecast.database.WeatherLocalDataSourceImpl
import com.malakezzat.weatherforecast.databinding.DialogAlertBinding
import com.malakezzat.weatherforecast.home.viewmodel.HomeViewModel
import com.malakezzat.weatherforecast.home.viewmodel.HomeViewModelFactory
import com.malakezzat.weatherforecast.model.Alert
import com.malakezzat.weatherforecast.model.IWeatherRepository
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepositoryImpl
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSourceImpl
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class AlertDialogFragment : DialogFragment() {

    private var _binding: DialogAlertBinding? = null
    private val binding get() = _binding!!
    private var listener: AlertDialogListener? = null
    private val REQUEST_CODE_OVERLAY_PERMISSION = 1001
    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 1002
    private var fromCalendar = Calendar.getInstance()
    private var toCalendar = Calendar.getInstance()
    private lateinit var viewModel: HomeViewModel
    private lateinit var factory: HomeViewModelFactory
    private lateinit var repository: IWeatherRepository
    private lateinit var sharedPreferences: SharedPreferences

    interface AlertDialogListener {
        fun onDialogPositiveClick(alert: Alert)
    }

    companion object {
        fun newInstance(listener: AlertDialogListener): AlertDialogFragment {
            val fragment = AlertDialogFragment()
            fragment.listener = listener
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(requireContext())
            _binding = DialogAlertBinding.inflate(layoutInflater)

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

            val lat = sharedPreferences.getString(getString(R.string.lat), "0.0") ?: "0.0"
            val lon = sharedPreferences.getString(getString(R.string.lon), "0.0") ?: "0.0"
            val units = getUnits()
            val lang = getLanguage()

            viewModel.fetchForecastDataDays(lat.toDouble(), lon.toDouble(),40,units,lang)

            setDefaultDateTime()

            binding.fromButton.setOnClickListener {
                pickDateTime(fromCalendar, true) {
                    updateFromTimeAndDate()
                    toCalendar.timeInMillis = fromCalendar.timeInMillis + 3600000
                    updateToTimeAndDate()
                }
            }

            binding.toButton.setOnClickListener {
                pickDateTime(toCalendar, false) { updateToTimeAndDate() }
            }

            binding.saveButton.setOnClickListener {
                val alertType = if (binding.alarmRadioButton.isChecked) R.string.alarm else R.string.notification
                if (alertType == R.string.alarm) {
                    if (!Settings.canDrawOverlays(requireContext())) {
                        requestOverlayPermission()
                    } else {
                        saveAlert()
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestNotificationPermission()
                    } else {
                        saveAlert()
                    }
                }
            }

            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
        startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION_PERMISSION)
    }

    private fun saveAlert() {
        val forecastData = when (val forecastState = viewModel.currentForecastDays.value) {
            is ApiState.Success -> forecastState.data.list
            else -> listOf()
        }

        val message = checkWeather(fromCalendar, forecastData) ?: " "

        val newAlert = Alert(
            0,
            fromCalendar.timeInMillis,
            toCalendar.timeInMillis,
            fromCalendar.timeInMillis,
            toCalendar.timeInMillis,
            if (binding.alarmRadioButton.isChecked) R.string.alarm else R.string.notification,
            message,
            " ",
            Random.nextInt().toString()
        )

        newAlert.workId = scheduleNotification(requireContext(), fromCalendar.timeInMillis, newAlert)
        listener?.onDialogPositiveClick(newAlert)
        dismiss()
    }


    private fun setDefaultDateTime() {
        toCalendar.add(Calendar.HOUR_OF_DAY, 1)
        updateFromTimeAndDate()
        updateToTimeAndDate()
    }

    private fun updateFromTimeAndDate() {
        binding.fromTime.text = formatTime(fromCalendar)
        binding.fromDate.text = formatDate(fromCalendar)
    }

    private fun updateToTimeAndDate() {
        binding.toTime.text = formatTime(toCalendar)
        binding.toDate.text = formatDate(toCalendar)
    }

    private fun pickDateTime(calendar: Calendar, isFromDate: Boolean, onDateTimeSet: () -> Unit) {
        val currentDate = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                if (calendar.timeInMillis < currentDate.timeInMillis) {
                    calendar.timeInMillis = currentDate.timeInMillis
                }

                pickTime(calendar, isFromDate) {
                    onDateTimeSet()
                }
            },
            currentDate.get(Calendar.YEAR),
            currentDate.get(Calendar.MONTH),
            currentDate.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = if (isFromDate) {
            currentDate.timeInMillis
        } else {
            fromCalendar.timeInMillis
        }
        datePicker.show()
    }

    private fun pickTime(calendar: Calendar, isFromTime: Boolean, onDateTimeSet: () -> Unit) {
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                if (isFromTime) {
                    if (calendar.timeInMillis < System.currentTimeMillis()) {
                        calendar.timeInMillis = System.currentTimeMillis()
                        Toast.makeText(requireContext(),
                            getString(R.string.please_select_time_after_now), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (calendar.timeInMillis <= fromCalendar.timeInMillis) {
                        calendar.timeInMillis = fromCalendar.timeInMillis + 3600000
                        Toast.makeText(requireContext(),
                            getString(R.string.please_select_time_after_from_time), Toast.LENGTH_SHORT).show()
                    }
                }
                onDateTimeSet()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePicker.show()
    }


    private fun formatTime(calendar: Calendar): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun formatDate(calendar: Calendar): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    fun scheduleNotification(context: Context, alarmTime: Long,alert: Alert): String {
        val currentTime = System.currentTimeMillis()
        val delay = alarmTime - currentTime

        val data = Data.Builder()
            .putString(getString(R.string.id_worker),alert.deleteId)
            .putString(getString(R.string.message_worker), alert.message)
            .putInt(getString(R.string.type_worker), alert.type)
            .build()
        Log.i("deleteAfterWork", "scheduleNotification: id ${alert.deleteId}")
        val notificationWork = OneTimeWorkRequestBuilder<AlertWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(notificationWork)

        return notificationWork.id.toString()
    }


    fun checkWeather(fromCalendar: Calendar, list: List<ListF>): String? {
        val calendarTimeInSeconds = fromCalendar.timeInMillis / 1000
        var closestIndex = -1
        var smallestDifference = Long.MAX_VALUE

        for (i in list.indices) {
            val weatherTimeInSeconds = list[i].dt
            val difference = kotlin.math.abs(weatherTimeInSeconds - calendarTimeInSeconds)

            if (difference < smallestDifference) {
                smallestDifference = difference
                closestIndex = i
            }
        }

        return if (closestIndex != -1) {
            val temp = list[closestIndex].main.temp
            val description = list[closestIndex].weather[0].description
            "$description ${getString(R.string.and_the_temperature_is)} ${setFormattedTemperature(temp)}"
        } else null
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

    private fun getUnits() : String {
        return if (sharedPreferences.getBoolean(getString(R.string.celsius_pref), false)) {
            "metric"
        } else if (sharedPreferences.getBoolean(getString(R.string.fahrenheit_pref), false)) {
            "imperial"
        } else {
            "standard"
        }
    }

    private fun getLanguage() : String{
        return if(sharedPreferences.getBoolean(getString(R.string.arabic_pref),false)){
            "ar"
        } else {
            "en"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
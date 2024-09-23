package com.malakezzat.weatherforecast.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.DialogAlertBinding
import com.malakezzat.weatherforecast.model.Alert
import java.text.SimpleDateFormat
import java.util.*

class AlertDialogFragment : DialogFragment() {

    private var _binding: DialogAlertBinding? = null
    private val binding get() = _binding!!
    private lateinit var listener: AlertDialogListener
    private val REQUEST_CODE_OVERLAY_PERMISSION = 1001
    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 1002
    private var fromCalendar = Calendar.getInstance()
    private var toCalendar = Calendar.getInstance()

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
        val newAlert = Alert(
            0,
            fromCalendar.timeInMillis,
            toCalendar.timeInMillis,
            fromCalendar.timeInMillis,
            toCalendar.timeInMillis,
            if (binding.alarmRadioButton.isChecked) R.string.alarm else R.string.notification
        )
        listener.onDialogPositiveClick(newAlert)
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
                        Toast.makeText(requireContext(), "Please select Time after now", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    if (calendar.timeInMillis <= fromCalendar.timeInMillis) {
                        calendar.timeInMillis = fromCalendar.timeInMillis + 3600000 // Set "to" time to one hour later
                        Toast.makeText(requireContext(), "Please select Time after From Time", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
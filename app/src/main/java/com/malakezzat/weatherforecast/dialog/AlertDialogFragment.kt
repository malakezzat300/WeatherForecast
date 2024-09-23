package com.malakezzat.weatherforecast.dialog

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.alert.view.AlertFragment
import com.malakezzat.weatherforecast.databinding.DialogAlertBinding
import com.malakezzat.weatherforecast.model.Alert
import com.malakezzat.weatherforecast.model.Type

class AlertDialogFragment : DialogFragment() {

    private var _binding: DialogAlertBinding? = null
    private val binding get() = _binding!!
    private lateinit var listener: AlertDialogListener

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

            binding.saveButton.setOnClickListener {
                val alertType = if (binding.alarmRadioButton.isChecked) Type.ALERT else Type.NOTIFICATION
                val newAlert = Alert(0, "Title", "Description", "Start", "End", alertType)

                listener.onDialogPositiveClick(newAlert)
                dismiss()
            }

            builder.setView(binding.root)
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
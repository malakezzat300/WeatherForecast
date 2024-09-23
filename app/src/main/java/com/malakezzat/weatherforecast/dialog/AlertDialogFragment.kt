package com.malakezzat.weatherforecast.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.DialogAlertBinding
import com.malakezzat.weatherforecast.databinding.DialogLocationBinding

class AlertDialogFragment : DialogFragment() {

    private var _binding: DialogAlertBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : Editor

    interface LocationDialogListener {
        fun onDialogPositiveClick(data: Map<String,Boolean>)
    }

    private lateinit var listener: LocationDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)
            editor = sharedPreferences.edit()

            _binding = DialogAlertBinding.inflate(layoutInflater)
            listener = it as? LocationDialogListener
                ?: throw ClassCastException("$context must implement AlertDialogListener")

            binding.saveButton.setOnClickListener {
                val data = mapOf(
                    getString(R.string.alarm_pref) to binding.alarmRadioButton.isChecked,
                    getString(R.string.notification_pref) to binding.notificationRadioButton.isChecked,
                )
                editor.putBoolean(getString(R.string.first_run),false)
                editor.commit()
                listener.onDialogPositiveClick(data)
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
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
import com.malakezzat.weatherforecast.databinding.DialogLocationBinding

class AlertDialogFragment : DialogFragment() {

    private var _binding: DialogLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : Editor

    interface LocationDialogListener {
        fun onDialogPositiveClick(data: Map<String,Boolean>)
        fun onDialogCancel()
    }

    private lateinit var listener: LocationDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)
            editor = sharedPreferences.edit()

            _binding = DialogLocationBinding.inflate(layoutInflater)
            listener = it as? LocationDialogListener
                ?: throw ClassCastException("$context must implement LocationDialogListener")

            binding.gpsRadioButton.text = getString(R.string.gps)
            binding.gpsRadioButton.isChecked = true
            binding.mapRadioButton.text = getString(R.string.map)
            binding.notificationSwitch.isChecked = true

            binding.okButton.setOnClickListener {
                val data = mapOf(
                    getString(R.string.gps_pref) to binding.gpsRadioButton.isChecked,
                    getString(R.string.map_pref) to binding.mapRadioButton.isChecked,
                    getString(R.string.notification_pref) to binding.notificationSwitch.isChecked
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

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        requireActivity().finish()
        listener.onDialogCancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
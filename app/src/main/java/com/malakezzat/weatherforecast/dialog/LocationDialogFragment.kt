package com.malakezzat.weatherforecast.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.DialogLocationBinding

class LocationDialogFragment : DialogFragment() {

    private var _binding: DialogLocationBinding? = null
    private val binding get() = _binding!!

    interface LocationDialogListener {
        fun onDialogPositiveClick(data: String)
        fun onDialogCancel()
    }

    private lateinit var listener: LocationDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            _binding = DialogLocationBinding.inflate(layoutInflater)
            listener = it as? LocationDialogListener
                ?: throw ClassCastException("$context must implement LocationDialogListener")

            binding.gpsRadioButton.text = getString(R.string.gps)
            binding.gpsRadioButton.isChecked = true
            binding.mapRadioButton.text = getString(R.string.map)
            binding.notificationSwitch.isChecked = true

            binding.okButton.setOnClickListener {
                listener.onDialogPositiveClick("gps: ${binding.gpsRadioButton.isChecked} \n" +
                        "map: ${binding.mapRadioButton.isChecked} \n" +
                        "notification: ${binding.notificationSwitch.isChecked}" )
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

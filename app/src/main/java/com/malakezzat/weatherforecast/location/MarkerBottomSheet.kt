package com.malakezzat.weatherforecast.location

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.malakezzat.weatherforecast.InitActivity
import com.malakezzat.weatherforecast.MainActivity
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.BottomSheetMarkerBinding
import java.util.Locale


class MarkerBottomSheet (
    private val latitude: Double,
    private val longitude: Double
) : BottomSheetDialogFragment() {

    companion object{
        const val TAG = "MarkerBottomSheet"
    }
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : Editor

    lateinit var binding : BottomSheetMarkerBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.bottom_sheet_marker, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if(addresses?.size!! > 0) {
            val address = addresses?.get(0)
            val fullAddress = address?.getAddressLine(0) ?: " "
            binding.cityText.text = fullAddress
            if(address?.adminArea == "null"){
                address?.adminArea == ""
            }
            if(address?.countryName == "null"){
                address?.countryName == ""
            }
            binding.countryText.text = address?.adminArea + " - " + address?.countryName
            binding.saveButton.setOnClickListener {
                val lat : String = latitude.toString()
                val lon : String = longitude.toString()
                editor.putString(requireContext().getString(R.string.lat_pref), lat)
                editor.putString(requireContext().getString(R.string.lon_pref), lon)
                editor.apply()
                Log.i(TAG, "getFreshLocation: start MainActivity")
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                )
                startActivity(intent)
                requireActivity().finishAffinity()
                requireActivity().finish()
                dismiss()
            }
        }


    }
}
package com.malakezzat.weatherforecast.settings.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.malakezzat.weatherforecast.init.InitActivity.Companion.TAG
import com.malakezzat.weatherforecast.MainActivity
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.FragmentSettingsBinding
import com.malakezzat.weatherforecast.location.OsmMapFragment
import java.util.Locale

class SettingsFragment : Fragment() {

    lateinit var binding : FragmentSettingsBinding
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : Editor
    private val LOCATION_PERMISSION : Int = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_settings, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

//        editor.putBoolean(getString(R.string.gps),true)
//        editor.commit()

        //location group
        binding.gpsRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.gps_pref),false)
        binding.mapRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.map_pref),false)
        //language group
        binding.englishRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.english_pref),true)
        binding.arabicRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.arabic_pref),false)
        //temperature group
        binding.celsiusRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.celsius_pref),false)
        binding.kelvinRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.kelvin_pref),true)
        binding.fahrenheitRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.fahrenheit_pref),false)
        //wind speed group
        binding.meterRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.meter_sec_pref),true)
        binding.mileRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.mile_hour_pref),false)
        //notification group
        binding.enableRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.enable_pref),true)
        binding.disableRadioButton.isChecked = sharedPreferences.getBoolean(getString(R.string.disable_pref),false)

        binding.locationRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.gps_radio_button -> {
                    if(checkPermissions()){
                        if(isLocationEnabled()){
                            startMainActivity()
                        } else {
                            showEnableLocationDialog()
                        }
                    }else{
                        ActivityCompat.requestPermissions(requireActivity(),
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION),
                            LOCATION_PERMISSION)
                    }
                    editor.putBoolean(getString(R.string.map_pref),false)
                    editor.putBoolean(getString(R.string.gps_pref),true)
                    editor.commit()
                }
                R.id.map_radio_button -> {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .add(android.R.id.content, OsmMapFragment(true)).commit()
                    editor.putBoolean(getString(R.string.gps_pref),false)
                    editor.putBoolean(getString(R.string.map_pref),true)
                    editor.commit()
                }
            }
        }

        binding.languageRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.english_radio_button -> {
                    setLanguage("en")
                    editor.putBoolean(getString(R.string.arabic_pref),false)
                    editor.putBoolean(getString(R.string.english_pref),true)
                    editor.commit()
                }
                R.id.arabic_radio_button -> {
                    setLanguage("ar")
                    editor.putBoolean(getString(R.string.english_pref),false)
                    editor.putBoolean(getString(R.string.arabic_pref),true)
                    editor.commit()
                }
            }
        }

        binding.temperatureRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.celsius_radio_button -> {
                    editor.putBoolean(getString(R.string.celsius_pref),true)
                    editor.putBoolean(getString(R.string.kelvin_pref),false)
                    editor.putBoolean(getString(R.string.fahrenheit_pref),false)
                    editor.commit()
                }
                R.id.kelvin_radio_button -> {
                    editor.putBoolean(getString(R.string.celsius_pref),false)
                    editor.putBoolean(getString(R.string.kelvin_pref),true)
                    editor.putBoolean(getString(R.string.fahrenheit_pref),false)
                    editor.commit()
                }
                R.id.fahrenheit_radio_button -> {
                    editor.putBoolean(getString(R.string.celsius_pref),false)
                    editor.putBoolean(getString(R.string.kelvin_pref),false)
                    editor.putBoolean(getString(R.string.fahrenheit_pref),true)
                    editor.commit()
                }
            }
        }

        binding.windSpeedRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.meter_radio_button -> {
                    editor.putBoolean(getString(R.string.meter_sec_pref),true)
                    editor.putBoolean(getString(R.string.mile_hour_pref),false)
                    editor.commit()
                }
                R.id.mile_radio_button -> {
                    editor.putBoolean(getString(R.string.meter_sec_pref),false)
                    editor.putBoolean(getString(R.string.mile_hour_pref),true)
                    editor.commit()
                }
            }
        }

        binding.notificationRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.enable_radio_button -> {
                    editor.putBoolean(getString(R.string.disable_pref),false)
                    editor.putBoolean(getString(R.string.enable_pref),true)
                    editor.commit()
                    binding.enableRadioButton.isChecked = true
                }
                R.id.disable_radio_button -> {
                    editor.putBoolean(getString(R.string.disable_pref),true)
                    editor.putBoolean(getString(R.string.enable_pref),false)
                    editor.commit()
                    binding.disableRadioButton.isChecked = true
                }
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!isLocationEnabled()) {
                    showEnableLocationDialog()
                } else {
                    startMainActivity()
                }
            }
        }
    }

    fun showEnableLocationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Location Services Disabled")
            .setMessage("Please enable location services to use this feature.")
            .setPositiveButton("Open Settings") { _, _ ->
                Toast.makeText(requireContext(), "Enable Location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    fun isLocationEnabled() : Boolean{
        val locationManager : LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun startMainActivity() {
        Log.i(TAG, "getFreshLocation: start MainActivity")
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        )
        startActivity(intent)
        requireActivity().finishAffinity()
        requireActivity().finish()
    }

    private fun setLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        val layoutDirection = if (isRTL(languageCode)) {
            View.LAYOUT_DIRECTION_RTL
        } else {
            View.LAYOUT_DIRECTION_LTR
        }
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        requireActivity().window.decorView.layoutDirection = layoutDirection
        requireActivity().recreate()
    }

    private fun isRTL(languageCode: String): Boolean {
        return TextUtils.getLayoutDirectionFromLocale(Locale(languageCode)) == View.LAYOUT_DIRECTION_RTL
    }
}
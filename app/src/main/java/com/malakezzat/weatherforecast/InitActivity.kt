package com.malakezzat.weatherforecast

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.malakezzat.weatherforecast.databinding.ActivityInitBinding
import com.malakezzat.weatherforecast.dialog.LocationDialogFragment
import com.malakezzat.weatherforecast.location.OsmMapFragment



class InitActivity : AppCompatActivity(), LocationDialogFragment.LocationDialogListener {

    companion object {
        const val TAG = "InitActivity"
    }
    lateinit var binding: ActivityInitBinding
    private lateinit var fusedClient : FusedLocationProviderClient
    private val LOCATION_PERMISSION : Int = 1001
    private val REQUEST_CODE_NOTIFICATION_PERMISSION = 1002
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()



        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_init)
        binding.lifecycleOwner = this


        if(sharedPreferences.getBoolean(getString(R.string.first_run),true)) {
            showNoticeDialog()
        } else {
            if(sharedPreferences.getBoolean(getString(R.string.gps),false)){
                if (checkPermissions()) {
                    if (!isLocationEnabled()) {
                        showEnableLocationDialog()
                    } else {
                        startMainActivity()
                    }
                } else {
                    binding.permissionCard.visibility = View.VISIBLE
                }
            } else if(sharedPreferences.getBoolean(getString(R.string.map),false)){
                startMainActivity()
            }
        }

        binding.allowButton.setOnClickListener {
            Log.i(TAG, "onDialogPositiveClick: allowButton is pressed")
                ActivityCompat.requestPermissions(this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION)
        }

    }

    override fun onResume() {
        super.onResume()
        if(checkPermissions()) {
            if (isLocationEnabled()) {
                startMainActivity()
            }
        }
    }

    fun showNoticeDialog() {
        val dialog = LocationDialogFragment()
        dialog.show(supportFragmentManager, "LocationDialogFragment")
    }

    override fun onDialogPositiveClick(data: Map<String, Boolean>) {
        val gps = data[getString(R.string.gps)]
        val map = data[getString(R.string.map)]
        val notification = data[getString(R.string.notification)]

        editor.putBoolean(getString(R.string.gps_pref),gps ?: false)
        editor.putBoolean(getString(R.string.map_pref),map ?: false)
        editor.putBoolean(getString(R.string.notification_pref),notification ?: false)
        editor.commit()

        if(gps == true){
            if(checkPermissions()){
                if(isLocationEnabled()){
                    startMainActivity()
                } else {
                    showEnableLocationDialog()
                }
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION),
                    LOCATION_PERMISSION)
            }
        } else if(map == true){
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, OsmMapFragment()).commit()
        }
        if(notification == true){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission()
            }
        }
    }

    override fun onDialogCancel() {
        finish()
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
            } else {
                binding.permissionCard.visibility = View.VISIBLE
            }
        }
    }

    fun showEnableLocationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Location Services Disabled")
            .setMessage("Please enable location services to use this feature.")
            .setPositiveButton("Open Settings") { _, _ ->
                Toast.makeText(this, "Enable Location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    fun isLocationEnabled() : Boolean{
        val locationManager : LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun startMainActivity() {
        Log.i(TAG, "getFreshLocation: start MainActivity")
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NEW_TASK
        )
        startActivity(intent)
        finishAffinity()
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE_NOTIFICATION_PERMISSION)
    }

}
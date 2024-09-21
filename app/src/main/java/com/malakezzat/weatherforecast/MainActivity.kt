package com.malakezzat.weatherforecast

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.malakezzat.weatherforecast.alert.view.AlertFragment
import com.malakezzat.weatherforecast.databinding.ActivityMainBinding
import com.malakezzat.weatherforecast.favorite.view.FavoriteFragment
import com.malakezzat.weatherforecast.home.view.HomeFragment
import com.malakezzat.weatherforecast.settings.view.SettingsFragment
import java.util.Locale
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFragmentName by Delegates.notNull<Int>()
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)

        if(sharedPreferences.getBoolean(getString(R.string.arabic_pref),false)){
            setLocale("ar")
        } else {
            setLocale("en")
        }

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this


        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = " "

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.setHomeAsUpIndicator(R.drawable.ic_hamburger)
        toggle.syncState()


        if (savedInstanceState == null) {
            currentFragmentName = R.string.home
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        } else {
            currentFragmentName = savedInstanceState.getInt("currentFragmentName",R.string.home)
            binding.toolbarTitle.text = getString(currentFragmentName)
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment(), getString(R.string.home))
                    currentFragmentName = R.string.home
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment(), getString(R.string.settings))
                    currentFragmentName = R.string.settings
                }
                R.id.nav_alert -> {
                    replaceFragment(AlertFragment(), getString(R.string.alert))
                    currentFragmentName = R.string.alert
                }
                R.id.nav_favorite -> {
                    replaceFragment(FavoriteFragment(), getString(R.string.favorite))
                    currentFragmentName = R.string.favorite
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        binding.toolbarTitle.text = title
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentFragmentName",currentFragmentName)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    @Suppress("DEPRECATION")
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        resources.updateConfiguration(config, resources.displayMetrics)

    }

}

//        val weatherRemoteDataSource = WeatherRemoteDataSourceImpl.getInstance()
//
//        CoroutineScope(Dispatchers.Default).launch {
//            val weatherResponse = weatherRemoteDataSource.getWeatherOverNetwork(lat = 27.1803844,lon = 31.1851445)
//
//            Log.i("MainActivity", "weatherResponse: $weatherResponse")
//            Log.i("MainActivity", "weather: ${weatherResponse.weather[0]}")
//            Log.i("MainActivity", "timezone: ${weatherResponse.timezone}")
//            Log.i("MainActivity", "name: ${weatherResponse.name}")
//
//        }


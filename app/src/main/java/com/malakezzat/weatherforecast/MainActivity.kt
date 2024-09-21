package com.malakezzat.weatherforecast

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


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment(), "Home")
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment(), "Settings")
                }
                R.id.nav_alert -> {
                    replaceFragment(AlertFragment(), "Alert")
                }
                R.id.nav_favorite -> {
                    replaceFragment(FavoriteFragment(), "Favorite")
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



    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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


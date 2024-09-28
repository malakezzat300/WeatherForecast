package com.malakezzat.weatherforecast.favorite.view

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.malakezzat.weatherforecast.misc.ApiState
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.database.AppDatabase
import com.malakezzat.weatherforecast.database.WeatherLocalDataSourceImpl
import com.malakezzat.weatherforecast.databinding.FragmentFavoriteBinding
import com.malakezzat.weatherforecast.favorite.viewmodel.FavoriteViewModel
import com.malakezzat.weatherforecast.favorite.viewmodel.FavoriteViewModelFactory
import com.malakezzat.weatherforecast.model.DayWeather
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepositoryImpl
import com.malakezzat.weatherforecast.model.WeatherResponse
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() , FavoriteDialogFragment.FavoriteDialogListener {

    private val TAG: String = "FavoriteFragment"
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var factory: FavoriteViewModelFactory
    private lateinit var repository: WeatherRepository
    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var units: String
    private lateinit var lang: String
    private lateinit var weatherResponseStore: WeatherResponse
    private lateinit var tempListStore: List<ListF>
    private lateinit var dayListStore: List<DayWeather>
    private val isHome = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = WeatherRepositoryImpl(
            WeatherRemoteDataSourceImpl.getInstance(), WeatherLocalDataSourceImpl(
                AppDatabase.getInstance(requireContext())
            )
        )
        factory = FavoriteViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(FavoriteViewModel::class.java)
        sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.my_preference),
            Context.MODE_PRIVATE
        )

        viewModel.fetchFavoriteData()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favoriteList.collect { apiState ->
                    when (apiState) {
                        is ApiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is ApiState.Success -> {
                            val list = apiState.data
                            if (list.isNotEmpty()) {
                                binding.noFavoriteBackground.visibility = View.GONE
                            } else {
                                binding.noFavoriteBackground.visibility = View.VISIBLE
                            }
                            binding.progressBar.visibility = View.GONE

                            val recyclerAdapter = FavoriteAdapter(requireContext(),
                                { item ->
                                    viewModel.removeFavorite(item)
                                    viewModel.removeFavoriteById(item.deleteId.toInt())
                                },
                                { lat, lon, id ->
                                    handleLocationClick(lat, lon, id)
                                }
                            )
                            recyclerAdapter.submitList(list.toMutableList())
                            binding.favoriteRecyclerView.apply {
                                adapter = recyclerAdapter
                                layoutManager = LinearLayoutManager(requireContext())
                            }
                        }

                        is ApiState.Failure -> {
                            Toast.makeText(
                                requireContext(),
                                "Failed to load favorites: ${apiState.exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }


            binding.addFavButton.setOnClickListener {
                binding.progressBar.visibility = View.VISIBLE
                if (isNetworkAvailable()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(600)
                        showFavoriteDialog()
                        binding.progressBar.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_connect_to_internet_first_and_try_again_later),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        private fun showFavoriteDialog() {
            val dialog = FavoriteDialogFragment.newInstance(this)
            dialog.show(parentFragmentManager, "FavoriteDialogFragment")
        }

        override fun onDialogPositiveClick() {

        }

        private fun handleLocationClick(lat: Double, lon: Double, id: String) {
            units = getUnits()
            lang = getLanguage()


            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FavoriteItemActivity(lat, lon, units, lang, id))
                .addToBackStack(getString(R.string.favorite))
                .commit()
            Log.i("favoriteTest", "handleLocationClick: $id")
        }

        private fun getUnits(): String {
            return if (sharedPreferences.getBoolean(getString(R.string.celsius_pref), false)) {
                "metric"
            } else if (sharedPreferences.getBoolean(getString(R.string.fahrenheit_pref), false)) {
                "imperial"
            } else {
                "standard"
            }
        }

        private fun getLanguage(): String {
            return if (sharedPreferences.getBoolean(getString(R.string.arabic_pref), false)) {
                "ar"
            } else {
                "en"
            }
        }

        private fun isNetworkAvailable(): Boolean {
            val connectivityManager =
                requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            return activeNetwork != null && activeNetwork.isConnected
        }

}

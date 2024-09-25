package com.malakezzat.weatherforecast.favorite.view

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.database.AppDatabase
import com.malakezzat.weatherforecast.database.FavoriteDB
import com.malakezzat.weatherforecast.database.LocationItem
import com.malakezzat.weatherforecast.database.WeatherLocalDataSourceImpl
import com.malakezzat.weatherforecast.databinding.DialogFavoriteBinding
import com.malakezzat.weatherforecast.favorite.viewmodel.FavoriteViewModel
import com.malakezzat.weatherforecast.favorite.viewmodel.FavoriteViewModelFactory
import com.malakezzat.weatherforecast.location.OsmMapFragment
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepositoryImpl
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSourceImpl
import java.io.IOException
import kotlin.properties.Delegates
import kotlin.random.Random

class FavoriteDialogFragment : DialogFragment() {

    private var _binding: DialogFavoriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var editor : Editor
    private lateinit var locationList: List<LocationItem>
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var factory: FavoriteViewModelFactory
    private lateinit var repository: WeatherRepository
    var lat : Double = 0.0
    var lon : Double = 0.0
    lateinit var fullAddress : String

    companion object {
        fun newInstance(listener: FavoriteDialogListener): FavoriteDialogFragment {
            val fragment = FavoriteDialogFragment()
            fragment.listener = listener
            return fragment
        }
    }

    interface FavoriteDialogListener {
        fun onDialogPositiveClick()
    }

    private lateinit var listener: FavoriteDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(requireContext())

            repository = WeatherRepositoryImpl(
                WeatherRemoteDataSourceImpl.getInstance(), WeatherLocalDataSourceImpl(
                    AppDatabase.getInstance(requireContext())
                )
            )
            factory = FavoriteViewModelFactory(repository)
            viewModel = ViewModelProvider(this, factory).get(FavoriteViewModel::class.java)

            sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.my_preference), Context.MODE_PRIVATE)
            editor = sharedPreferences.edit()

            _binding = DialogFavoriteBinding.inflate(layoutInflater)

            locationList = loadLocationsFromAssets(requireContext())

            val locationNames = locationList.map { it.name }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, locationNames)
            binding.autoCompleteTextView.setAdapter(adapter)

            binding.autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
                val selectedLocationName = adapter.getItem(position)
                val selectedLocation = locationList.find { it.name == selectedLocationName }

                selectedLocation?.let {
                    lat = it.coord.lat
                    lon = it.coord.lon
                    fullAddress = it.name
                }
            }

            binding.addButton.setOnClickListener {
                addLocationToFavorite(lat,lon,fullAddress)
                listener.onDialogPositiveClick()
                dismiss()
            }

            binding.mapFavoriteButton.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, OsmMapFragment(false),"OsmMapFragment").commit()
                listener.onDialogPositiveClick()
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

    fun loadLocationsFromAssets(context: Context): List<LocationItem> {
        val json: String?
        try {
            val inputStream = context.assets.open("city_list.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return emptyList()
        }
        val listType = object : TypeToken<List<LocationItem>>() {}.type
        return Gson().fromJson(json, listType)
    }

    fun addLocationToFavorite(_lat : Double,_lon : Double,_fullAddress : String){
        repository = WeatherRepositoryImpl(
            WeatherRemoteDataSourceImpl.getInstance(), WeatherLocalDataSourceImpl(
                AppDatabase.getInstance(requireContext())
            )
        )
        factory = FavoriteViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(FavoriteViewModel::class.java)

        viewModel.addFavorite(
            FavoriteDB(0,_lon,_lat,_fullAddress,
            Random.nextInt().toString())
        )

        Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT).show()
        dismiss()
    }
}
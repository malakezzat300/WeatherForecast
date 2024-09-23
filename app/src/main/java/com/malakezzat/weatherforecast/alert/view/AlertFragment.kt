package com.malakezzat.weatherforecast.alert.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.alert.viewmodel.AlertViewModel
import com.malakezzat.weatherforecast.alert.viewmodel.AlertViewModelFactory
import com.malakezzat.weatherforecast.database.AppDatabase
import com.malakezzat.weatherforecast.database.home.WeatherLocalDataSourceImpl
import com.malakezzat.weatherforecast.databinding.FragmentAlertBinding
import com.malakezzat.weatherforecast.dialog.AlertDialogFragment
import com.malakezzat.weatherforecast.model.Alert
import com.malakezzat.weatherforecast.model.WeatherRepository
import com.malakezzat.weatherforecast.model.WeatherRepositoryImpl
import com.malakezzat.weatherforecast.network.WeatherRemoteDataSourceImpl
import kotlinx.coroutines.launch

class AlertFragment : Fragment(), AlertDialogFragment.AlertDialogListener {

    private lateinit var viewModel: AlertViewModel
    private lateinit var factory: AlertViewModelFactory
    private lateinit var repository: WeatherRepository
    private lateinit var binding: FragmentAlertBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alert, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = WeatherRepositoryImpl(
            WeatherRemoteDataSourceImpl.getInstance(),
            WeatherLocalDataSourceImpl(AppDatabase.getInstance(requireContext()))
        )

        factory = AlertViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(AlertViewModel::class.java)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.alertList.collect { list ->
                if(list.isNotEmpty()){
                    binding.noAlertBackground.visibility = View.GONE
                } else {
                    binding.noAlertBackground.visibility = View.VISIBLE
                }
                val recyclerAdapter = AlertAdapter(requireContext()) { item ->
                    viewModel.removeAlert(item)
                }
                recyclerAdapter.submitList(list.toMutableList())
                binding.alertRecyclerView.apply {
                    adapter = recyclerAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                }
            }
        }

        binding.addAlertButton.setOnClickListener {
            showAlertDialog()
        }

        viewModel.fetchAlertData()
    }

    private fun showAlertDialog() {
        val dialog = AlertDialogFragment.newInstance(this)
        dialog.show(parentFragmentManager, "AlertDialogFragment")
    }

    override fun onDialogPositiveClick(alert: Alert) {
        Toast.makeText(requireContext(), "Added Alert", Toast.LENGTH_SHORT).show()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.addAlert(alert)
        }
    }
}
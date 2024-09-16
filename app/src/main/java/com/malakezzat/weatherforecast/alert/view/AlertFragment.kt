package com.malakezzat.weatherforecast.alert.view

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.alert.viewmodel.AlertViewModel

class AlertFragment : Fragment() {

    companion object {
        fun newInstance() = AlertFragment()
    }

    private val viewModel: AlertViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_alert, container, false)
    }
}
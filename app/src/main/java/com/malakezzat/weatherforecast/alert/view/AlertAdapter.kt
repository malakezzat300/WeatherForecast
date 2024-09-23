package com.malakezzat.weatherforecast.alert.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.malakezzat.weatherforecast.AlertDiffUtilDays

import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.AlertItemBinding
import com.malakezzat.weatherforecast.model.Alert

class AlertAdapter(val context : Context,
                   private val onDelete: (Alert) -> Unit) : ListAdapter<Alert, AlertAdapter.ViewHolder>(
    AlertDiffUtilDays()
){

    lateinit var binding : AlertItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertAdapter.ViewHolder {
        val inflater : LayoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater, R.layout.alert_item,parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alertItem = getItem(position)


        binding.alertMenuButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, binding.alertMenuButton)
            popupMenu.inflate(R.menu.options_menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.delete_item -> {
                        onDelete(alertItem)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

    }

    class ViewHolder(val binding: AlertItemBinding) : RecyclerView.ViewHolder(binding.root)


}
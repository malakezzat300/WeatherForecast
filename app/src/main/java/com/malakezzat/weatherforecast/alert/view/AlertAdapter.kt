package com.malakezzat.weatherforecast.alert.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.malakezzat.weatherforecast.AlertDiffUtilDays

import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.AlertItemBinding
import com.malakezzat.weatherforecast.model.Alert
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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
        holder.binding.alert = alertItem
        holder.binding.fromTime = convertTimestampToTime(alertItem.fromTime)
        holder.binding.fromDate = convertTimestampToDate(alertItem.fromDate)
        holder.binding.toTime = convertTimestampToTime(alertItem.toTime)
        holder.binding.toDate = convertTimestampToDate(alertItem.toDate)


        holder.binding.alertMenuButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, holder.binding.alertMenuButton)
            popupMenu.inflate(R.menu.options_menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.delete_item -> {
                        showConfirmationDialog(view.context) {
                            cancelNotification(context,alertItem.workId)
                            onDelete(alertItem)
                            Toast.makeText(context,
                                context.getString(R.string.removed), Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    class ViewHolder(val binding: AlertItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun convertTimestampToTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun convertTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM,yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }


    private fun showConfirmationDialog(context: Context, onConfirm: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.confirmation))
            .setMessage(context.getString(R.string.are_you_sure_you_want_to_delete_this_item))
            .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                onConfirm()
            }
            .setNegativeButton(context.getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    fun cancelNotification(context: Context, workRequestId: String) {
        val workManager = WorkManager.getInstance(context)

        val id = UUID.fromString(workRequestId)

        workManager.cancelWorkById(id)
    }
}
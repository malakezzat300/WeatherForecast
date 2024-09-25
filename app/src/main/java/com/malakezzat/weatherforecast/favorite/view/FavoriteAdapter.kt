package com.malakezzat.weatherforecast.favorite.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.malakezzat.weatherforecast.FavoriteDiffUtil
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.database.FavoriteDB
import com.malakezzat.weatherforecast.databinding.AlertItemBinding
import com.malakezzat.weatherforecast.databinding.FavoriteItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FavoriteAdapter(val context : Context,
                       private val onDelete: (FavoriteDB) -> Unit) : ListAdapter<FavoriteDB, FavoriteAdapter.ViewHolder>(
    FavoriteDiffUtil()
){

    lateinit var binding : FavoriteItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteAdapter.ViewHolder {
        val inflater : LayoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater, R.layout.favorite_item,parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favoriteItem = getItem(position)

        holder.binding.addressText.text = favoriteItem.address

        holder.binding.favoriteMenuButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(view.context, holder.binding.favoriteMenuButton)
            popupMenu.inflate(R.menu.options_menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.delete_item -> {
                        showConfirmationDialog(view.context) {
                            onDelete(favoriteItem)
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

    class ViewHolder(val binding: FavoriteItemBinding) : RecyclerView.ViewHolder(binding.root)

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

}
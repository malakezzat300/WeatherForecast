package com.malakezzat.weatherforecast.home.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.malakezzat.weatherforecast.ForecastDiffUtil
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.TempHoursItemBinding
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.ListF
import com.malakezzat.weatherforecast.model.WeatherResponse
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class TempAdapter (val context : Context) : ListAdapter<ListF, TempAdapter.ViewHolder>(
    ForecastDiffUtil()
){

    lateinit var binding : TempHoursItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TempAdapter.ViewHolder {
        val inflater : LayoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater, R.layout.temp_hours_item,parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        binding.tempItem = getItem(position)
        binding.time = dateConverterForTemps(getItem(position).dt)
    }

    class ViewHolder(val binding: TempHoursItemBinding) : RecyclerView.ViewHolder(binding.root)

    companion object{

        @BindingAdapter("icon","myError")
        @JvmStatic
        fun setIcon(view: ImageView, icon :String?, myError : Drawable){
            Picasso.get().load("https://openweathermap.org/img/wn/${icon}@2x.png").error(myError).into(view)
        }

        @BindingAdapter("formattedTemperature")
        @JvmStatic
        fun setFormattedTemperature(textView: TextView, temperature: Double) {
            textView.text = String.format("%.1f K", temperature)

            //textView.text = String.format("%.1f °C", temperature)
            //textView.text = String.format("%.1f °F", temperature)
        }
    }

    fun dateConverterForTemps(dt : Long) : String{
        val date = Date(dt * 1000)
        val sdf = SimpleDateFormat("h a")
        sdf.setTimeZone(TimeZone.getDefault())
        val formattedDate: String = sdf.format(date)
        return formattedDate
    }

}
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
import com.malakezzat.weatherforecast.ForecastDiffUtil
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.DayTempItemBinding
import com.malakezzat.weatherforecast.databinding.TempHoursItemBinding
import com.malakezzat.weatherforecast.model.ListF
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class DayAdapter (val context : Context) : ListAdapter<ListF, DayAdapter.ViewHolder>(
    ForecastDiffUtil()
){

    lateinit var binding : DayTempItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayAdapter.ViewHolder {
        val inflater : LayoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater, R.layout.day_temp_item,parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        binding.dayItem  = getItem(position)
        binding.time = dateConverterForTemps(getItem(position).dt)
        binding.dayText.text = dateConverterForTemps(getItem(position).dt)
        if(position == 0){
            binding.dayText.text = "Tomorrow"
            binding.constraintCard.setBackgroundResource(R.drawable.button_background)
        }
    }

    class ViewHolder(val binding: DayTempItemBinding) : RecyclerView.ViewHolder(binding.root)

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
        val sdf = SimpleDateFormat("EEE")
        sdf.setTimeZone(TimeZone.getDefault())
        val formattedDate: String = sdf.format(date)

        return formattedDate
    }

}
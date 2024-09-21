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
import com.malakezzat.weatherforecast.ForecastDiffUtilDays
import com.malakezzat.weatherforecast.R
import com.malakezzat.weatherforecast.databinding.DayTempItemBinding
import com.malakezzat.weatherforecast.databinding.TempHoursItemBinding
import com.malakezzat.weatherforecast.model.DayWeather
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.ListF
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class DayAdapter (val context : Context) : ListAdapter<DayWeather, DayAdapter.ViewHolder>(
    ForecastDiffUtilDays()
){

    lateinit var binding : DayTempItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayAdapter.ViewHolder {
        val inflater : LayoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater, R.layout.day_temp_item,parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position == 0){
         getItem(position).day = "Tomorrow"
         binding.constraintCard.setBackgroundResource(R.drawable.button_background)
        }
        binding.dayItem = getItem(position)
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


//    fun getFiveDaysMinMaxTemps(forecastResponse: ForecastResponse): List<DayTemperature> {
//        val fiveDaysData = mutableListOf<DayTemperature>()
//        val uniqueDates = forecastResponse.list.map { it.dt_txt.substring(0, 10) }.distinct().take(5)
//
//        for (date in uniqueDates) {
//            val (minTemp, maxTemp) = getMinMaxTempForDay(forecastResponse.list, date)
//            fiveDaysData.add(DayTemperature(date, minTemp, maxTemp))
//        }
//
//        return fiveDaysData
//    }

}
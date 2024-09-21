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
import com.malakezzat.weatherforecast.model.DayTemperature
import com.malakezzat.weatherforecast.model.ForecastResponse
import com.malakezzat.weatherforecast.model.ListF
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
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


    fun filterSingleDayData(list : List<ListF>,date : String): List<ListF> {
        return list.filter { item ->
            item.dt_txt.contains(date)
        }
    }

    fun getMinMaxTempForDay(list : List<ListF>, date: String): Pair<Double, Double> {
        // Filter list for the given date
        val dayForecasts = list.filter { it.dt_txt.startsWith(date) }

        // Extract temperatures for the day
        val temps = dayForecasts.map { it.main.temp }

        // Get the minimum and maximum temperatures
        val minTemp = temps.minOrNull() ?: Double.NaN
        val maxTemp = temps.maxOrNull() ?: Double.NaN

        return Pair(minTemp, maxTemp)
    }

    fun getDayDate(days : Int): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd") // Define the pattern
        val calendar = Calendar.getInstance() // Get the current date
        calendar.add(Calendar.DAY_OF_YEAR, days) // Add one day
        return formatter.format(calendar.time) // Format and return the date
    }

    fun filterFiveDaysData(forecastResponse: ForecastResponse): List<ListF> {
        return forecastResponse.list.filter { item ->
            item.dt_txt.contains("00:00:00")
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
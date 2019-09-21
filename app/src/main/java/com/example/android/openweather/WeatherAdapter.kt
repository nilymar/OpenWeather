package com.example.android.openweather

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.openweather.WeatherAdapter.WeatherViewHolder
import java.util.*
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
class WeatherAdapter// this is the constructor for the Weather object adapter
    (private var mContext: Context?, mWeathers: ArrayList<ForecastWeather>) :
    RecyclerView.Adapter<WeatherViewHolder>() {
    private var weathers = ArrayList<ForecastWeather>()
    init {
        weathers = mWeathers
    }

    inner class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private var cardView: CardView? = null
        private var forecastLayout: ConstraintLayout? = null
        private var minTempField: TextView? = null
        private var maxTempField: TextView? = null
        private var conditionsField: TextView? = null
        private var humidityField: TextView? = null
        private var forecastDate: TextView? = null
        private var minTemp: TextView? = null
        private var maxTemp: TextView? = null
        private var conditions: TextView? = null
        private var humidity: TextView? = null
        private var conditionIcon: ImageView = itemView.findViewById(R.id.condition_icon)

        init {
            cardView = itemView.findViewById(R.id.card_view)
            forecastLayout = itemView.findViewById(R.id.forecast_layout)
            forecastDate = itemView.findViewById(R.id.date_of_forecast)
            minTemp = itemView.findViewById(R.id.min_temp_text)
            minTempField = itemView.findViewById(R.id.min_temp)
            maxTemp = itemView.findViewById(R.id.max_temp_text)
            maxTempField = itemView.findViewById(R.id.max_temp)
            conditions = itemView.findViewById(R.id.conditions_text)
            conditionsField = itemView.findViewById(R.id.conditions)
            humidity = itemView.findViewById(R.id.humidity_text)
            humidityField = itemView.findViewById(R.id.humidity)
            mContext = itemView.context
        }

        fun bindWeathers(currentWeather: ForecastWeather) {
            forecastLayout!!.visibility = View.VISIBLE
            if (currentWeather.isDay) {
                forecastLayout!!.setBackgroundResource(R.color.day_background)
                forecastDate!!.setTextColor(mContext!!.resources.getColor(R.color.text_color))
                minTemp!!.setTextColor(mContext!!.resources.getColor(R.color.text_color))
                minTempField!!.setTextColor(mContext!!.resources.getColor(R.color.text_color))
                maxTemp!!.setTextColor(mContext!!.resources.getColor(R.color.text_color))
                maxTempField!!.setTextColor(mContext!!.resources.getColor(R.color.text_color))
                conditions!!.setTextColor(mContext!!.resources.getColor(R.color.text_color))
                conditionsField!!.setTextColor(mContext!!.resources.getColor(R.color.text_color))
                humidity!!.setTextColor(mContext!!.resources.getColor(R.color.text_color))
                humidityField!!.setTextColor(mContext!!.resources.getColor(R.color.text_color))
            } else {
                forecastLayout!!.setBackgroundResource(R.color.night_background)
                forecastDate!!.setTextColor(mContext!!.resources.getColor(R.color.night_text))
                minTemp!!.setTextColor(mContext!!.resources.getColor(R.color.night_text))
                minTempField!!.setTextColor(mContext!!.resources.getColor(R.color.night_text))
                maxTemp!!.setTextColor(mContext!!.resources.getColor(R.color.night_text))
                maxTempField!!.setTextColor(mContext!!.resources.getColor(R.color.night_text))
                conditions!!.setTextColor(mContext!!.resources.getColor(R.color.night_text))
                conditionsField!!.setTextColor(mContext!!.resources.getColor(R.color.night_text))
                humidity!!.setTextColor(mContext!!.resources.getColor(R.color.night_text))
                humidityField!!.setTextColor(mContext!!.resources.getColor(R.color.night_text))
            }
            // putting the date and time on screen
            val updatedAtText = currentWeather.dateString + "  " + currentWeather.timeString
            forecastDate!!.text = updatedAtText
            val minT = java.lang.Float.parseFloat(currentWeather.minTemp)
            val minTint = minT.roundToInt() // to get a rounded temp number
            val tempMin = "$minTint \u2103" // adding celsius symbol to the temp
            minTemp!!.text = tempMin
            val maxT = java.lang.Float.parseFloat(currentWeather.maxTemp)
            val maxTint = maxT.roundToInt()// to get a rounded temp number
            val tempMax = "$maxTint \u2103" // adding celsius symbol to the temp
            maxTemp!!.text = tempMax
            val condition = currentWeather.conditionsDesc
            conditions!!.text = condition
            // replacing the ".0" with percentage symbol in humidity
            val humidityText = currentWeather.humidity + "%"
            humidity!!.text = humidityText
            val icon = currentWeather.icon
            // using glide to fetch the right icon from the internet, and put on screen
            val iconUrl = mContext!!.resources.getString(R.string.icon_url) + icon +
                    mContext!!.resources.getString(R.string.icon_url_end)
            if (!iconUrl.equals("")) {
                Glide.with(mContext!!)
                    .load(iconUrl)
                    .into(conditionIcon);
            }
        }

        override fun onClick(v: View) {
            // didn't put anything here
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return WeatherViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bindWeathers(weathers[position]) // binding the view to the right object
    }

    override fun getItemCount(): Int {
        return weathers.size
    }

}

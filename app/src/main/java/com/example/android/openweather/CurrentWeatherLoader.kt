package com.example.android.openweather

import android.content.Context
import androidx.loader.content.AsyncTaskLoader

// a loader for the current weather url
class CurrentWeatherLoader
/**
 * Constructs a new [CurrentWeatherLoader].
 * @param context of the activity
 * @param url     to load data from
 */
    (
    context: Context, // Query URL
    private val mUrl: String?
) : AsyncTaskLoader<List<ForecastWeather>>(context) {
    // the List variable for Weather objects
    private var weather: List<ForecastWeather>? = null

    override fun onStartLoading() {
        if (weather != null) {// if you have weather in the list - use that list
            // Use cached data
            deliverResult(weather)
        } else {
            forceLoad()
        }
    }

    // This is on a background thread.
    override fun loadInBackground(): List<ForecastWeather>? {
        if (mUrl == null) {
            return null
        }
        weather = CurrentQueryUtils.fetchWeathers(mUrl)
            return weather
    }

    override fun deliverResult(data: List<ForecastWeather>?) {
        weather = data
        super.deliverResult(data)
    }

}

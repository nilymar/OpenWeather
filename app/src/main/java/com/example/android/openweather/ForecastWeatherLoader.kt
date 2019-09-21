package com.example.android.openweather

import android.content.Context
import androidx.loader.content.AsyncTaskLoader

// a loader for the forecast weather data
class ForecastWeatherLoader
/**
 * Constructs a new [ForecastWeatherLoader].
 * @param context of the activity
 * @param url     to load data from
 */
    (
    context: Context, // Query URL
    private val mUrl: String?
) : AsyncTaskLoader<List<ForecastWeather>>(context) {
    // the List variable for Weather objects
    private var weathers: List<ForecastWeather>? = null

    override fun onStartLoading() {
        if (weathers != null) {// if you have weathers in the list - use that list
            // Use cached data
            deliverResult(weathers)
        } else {
            forceLoad()
        }
    }

    // This is on a background thread.
    override fun loadInBackground(): List<ForecastWeather>? {
        if (mUrl == null) {
            return null
        }
        weathers = ForecastQueryUtils.fetchWeathers(mUrl)
            return weathers
    }

    override fun deliverResult(data: List<ForecastWeather>?) {
        weathers = data
        super.deliverResult(data)
    }
}

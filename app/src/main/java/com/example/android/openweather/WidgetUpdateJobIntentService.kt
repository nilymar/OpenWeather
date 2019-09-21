package com.example.android.openweather

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.android.openweather.AppConstants.ACTION_UPDATE_WEATHER_WIDGET_ONLINE
import com.example.android.openweather.AppConstants.BASE_CURRENT_URL
import com.example.android.openweather.AppConstants.SHARED_PREFERENCES

/**
 * An [JobIntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread - for Build.VERSION_CODES.O and up.
 */
class WidgetUpdateJobIntentService : JobIntentService() {
    // shared pref file for specific widget
    lateinit var widgetSharedPref: String
    var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onHandleWork(intent: Intent) {
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        widgetSharedPref = SHARED_PREFERENCES + widgetId
        if (intent.action != null) {
            val action = intent.action
            if (ACTION_UPDATE_WEATHER_WIDGET_ONLINE == action) {
                handleActionUpdateWeatherWidget()
            }
        }
    }

    /**
     * Handle action ACTION_UPDATE_WEATHER_WIDGET_ONLINE
     */
    private fun handleActionUpdateWeatherWidget() {
        var weather: ForecastWeather? = null
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(
                this,
                WeatherWidgetProvider::class.java
            )
        )
        // get a reference to the ConnectivityManager to check state of network connectivity
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // get details on the currently active default data network
        val networkInfo = connMgr.activeNetworkInfo
        val isConnected = networkInfo != null && networkInfo.isConnected
        if (!isConnected) { // if there is no internet connection - don't show progress bar and set
            Log.i(LOG_TAG, "handleActionUpdateWeatherWidget activated - no Internet")
        } else {
            //weather = WidgetQueryUtils.fetchWeather(createURI()) // fetch the weather data
            weather = CurrentQueryUtils.fetchWeathers(createURI())?.get(0)
        }
        if (weather != null) {
            WeatherWidgetProvider.updateAppWidget(this, appWidgetManager, weather, widgetId)
        } else {
            Log.i(LOG_TAG, "Json was wrong, updating with empty weather")
            weather= ForecastWeather(
                "-", 0, "-", "-", "-", "-", "-",
                "-", "-" , "-", "-", true, "-",
                0, "", "")
            WeatherWidgetProvider.updateAppWidget(this, appWidgetManager, weather, widgetId)
        }
    }

    private fun createURI(): String {
        var queryCity = restorePreferences(getString(R.string.settings_city_key))
        if (queryCity!!.isEmpty())
            queryCity = resources.getString(R.string.settings_city_default)
        val baseUri = Uri.parse(BASE_CURRENT_URL)
        val uriBuilder = baseUri.buildUpon()
        uriBuilder.appendQueryParameter(getString(R.string.query_by_city), queryCity)
        uriBuilder.appendQueryParameter("appid", resources.getString(R.string.api_key))
        Log.i(LOG_TAG, uriBuilder.toString())
        return uriBuilder.toString()
    }

    // This method to restore the custom preferences data
    private fun restorePreferences(key: String): String? {
        val myPreferences = getSharedPreferences(widgetSharedPref, Context.MODE_PRIVATE)
        return if (myPreferences.contains(key))
            myPreferences.getString(key, "")
        else
            ""
    }

    companion object {
        val LOG_TAG: String = WidgetUpdateJobIntentService::class.java.name
        private const val RECEIVER = "receiver"
        private const val JOB_ID = 2


        // with this method we receive instructions to update the widget
        fun enqueueWork(
            context: Context, workerResultReceiver: ServiceResultReceiver, action: String,
            id: Int
        ) {
            Log.i(LOG_TAG, "enqueueWork activated. widgetId is $id")
            val intent = Intent(context, WidgetUpdateJobIntentService::class.java)
            intent.putExtra(RECEIVER, workerResultReceiver)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            intent.action = action
            enqueueWork(context, WidgetUpdateJobIntentService::class.java, JOB_ID, intent)
        }
    }
}

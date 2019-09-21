package com.example.android.openweather

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.example.android.openweather.AppConstants.ACTION_UPDATE_WEATHER_WIDGET_ONLINE
import java.io.File
import com.example.android.openweather.AppConstants.SHARED_PREFERENCES
import com.example.android.openweather.AppConstants.URI_SCHEME

/**
 * Implementation of App Widget functionality
 */
@Suppress("DEPRECATION")
class WeatherWideWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val mServiceResultReceiver = ServiceResultReceiver(Handler())
        for (i in appWidgetIds.indices) {
            val appWidgetId = appWidgetIds[i]
            Log.i(WeatherWidgetProvider.LOG_TAG, "onUpdate widget id is: $appWidgetId")
            WideWidgetUpdateJobIntentService.enqueueWork(
                context, mServiceResultReceiver,
                ACTION_UPDATE_WEATHER_WIDGET_ONLINE, appWidgetId
            )
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        val mgr = AppWidgetManager.getInstance(context)
        val defaultViews = RemoteViews(context.packageName, R.layout.widget_wide_view)
        val comp = ComponentName(context.packageName, WeatherWideWidgetProvider::class.java.name)
        val appWidgetIds = mgr.getAppWidgetIds(comp)
        val N = appWidgetIds.size
        for (i in 0 until N) {
            val idefault = Intent(context, MainActivity::class.java)
            // putting the name of provider and the id of the widget as extra to pass to main
            idefault.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, comp.toString())
            idefault.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i])
            val data = Uri.withAppendedPath(
                Uri.parse("$URI_SCHEME://widget/id"),
                appWidgetIds[i].toString()
            )
            idefault.data = data
            val defaultPendingIntent = PendingIntent.getActivity(
                context, 0, idefault,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            defaultViews.setOnClickPendingIntent(R.id.widget_background, defaultPendingIntent)
            mgr.updateAppWidget(comp, defaultViews)
        }
    }

    override fun onDisabled(context: Context) {
        Log.i(LOG_TAG, "onDisabled activated")
    }

    // handle updating widget when there are changes in size
    override fun onAppWidgetOptionsChanged(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int,
        newOptions: Bundle
    ) {
        val mServiceResultReceiver = ServiceResultReceiver(Handler())
        WideWidgetUpdateJobIntentService.enqueueWork(
            context, mServiceResultReceiver,
            ACTION_UPDATE_WEATHER_WIDGET_ONLINE, appWidgetId
        )
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    // when a widget is deleted from home screen - delete the sharedPreferences file with the widget data
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (i in appWidgetIds.indices) {
            val fileLocation = context.filesDir.parent + File.separator + "shared_prefs/"
            val fileName = SHARED_PREFERENCES + appWidgetIds[i] + ".xml"
            val filePath = fileLocation + fileName
            val file = File(filePath)
            if (file.exists()) {
                if (file.delete())
                    Log.i(LOG_TAG, "onDeleted deleted file $filePath")
                else
                    Log.i(LOG_TAG, "onDeleted didn't delete the file")
            }
        }
        super.onDeleted(context, appWidgetIds)
    }

    companion object {
        val LOG_TAG: String = WeatherWideWidgetProvider::class.java.name

        // updating an app widget, using different views for narrow and wide widgets
        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            weather: ForecastWeather, appWidgetId: Int
        ) {
            // Construct the RemoteViews object
            val views: RemoteViews
            // Creating the widget views, depending on its width
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            views = if (width < 200) {
                getSmallRemoteView(context, weather, appWidgetId) // for a narrow widget
            } else {
                getBigRemoteView(context, weather, appWidgetId) // for a wide widget
            }
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Creates and returns the RemoteViews to be displayed in the small mode widget
         * @param context The context
         * @param weather The current weather object with all its data
         * @return The RemoteViews for the small display mode widget
         */
        @SuppressLint("SimpleDateFormat")
        private fun getSmallRemoteView(context: Context, weather: ForecastWeather?, widgetId: Int): RemoteViews {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.weather_widget)
            // Set the click handler to open MainActivity and send the widget id and provider component name to the activity
            val intent = Intent(context, MainActivity::class.java)
            val comp = ComponentName(context.packageName, WeatherWideWidgetProvider::class.java.name)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, comp.toString())
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            // the following 4 lines make sure each widget installment will get its own intent with its own data
            val data = Uri.withAppendedPath(
                Uri.parse("$URI_SCHEME://widget/id"),
                widgetId.toString()
            )
            intent.data = data
            // the pendingIntent to open the activity
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            // initialize the values to put in the widget
            var time = "-"
            var widgetTemp = "-"
            val weatherIcon = R.drawable.sun_icon1
            var city = "-"
            var isDay = true
            // if no weather was fetched
            if (weather == null) {
                widgetTemp = "No data"
                Log.i(LOG_TAG, "weather is empty")
            } else if (weather.name == "-") {
                widgetTemp = "No data"
                Log.i(LOG_TAG, "json string was wrong")
            }else {
                city = weather.name
                time = weather.timeString
                val temp = weather.tempC
                val curTemp = java.lang.Float.parseFloat(temp)
                val currTemp = Math.round(curTemp) // to get a rounded temp number
                widgetTemp = "$currTemp \u2103"
                isDay = weather.isDay
            }
            // setting the strings in the right places in the views
            views.setTextViewText(R.id.current_city, city)
            views.setTextViewText(R.id.current_time, time)
            views.setTextViewText(R.id.current_temp, widgetTemp)
            val iconUrl = context.resources.getString(R.string.icon_url) + weather!!.icon +
                    context.resources.getString(R.string.icon_url_end)
            val awt: AppWidgetTarget = object : AppWidgetTarget(context.applicationContext,
                R.id.widget_image, views, widgetId) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    super.onResourceReady(resource, transition)
                }
            }
            // put the icon on screen using Glide
            val options = RequestOptions().
                override(300, 300).placeholder(weatherIcon).error(weatherIcon)
            Glide.with(context.applicationContext).asBitmap().load(iconUrl).apply(options).into(awt)
            if (isDay) { // if it is day - the text is black and background light blue
                views.setTextColor(R.id.current_city, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.current_time, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.current_temp, context.resources.getColor(R.color.text_color))
                views.setImageViewResource(R.id.widget_background, R.color.day_background)
            } else { // if it is night - the text is white and background night dark
                views.setTextColor(R.id.current_city, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.current_temp, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.current_time, context.resources.getColor(R.color.night_text))
                views.setImageViewResource(R.id.widget_background, R.color.night_background)
            }
            // clicking anywhere in the widget - will activate the pending intent
            views.setOnClickPendingIntent(R.id.widget_background, pendingIntent)
            return views
        }

        /**
         * Creates and returns the RemoteViews to be displayed in the big mode widget
         * @param context The context
         * @param weather The weather object with all its data
         * @return The RemoteViews for the small display mode widget
         */
        private fun getBigRemoteView(context: Context, weather: ForecastWeather?, widgetId: Int): RemoteViews {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_wide_view)
            // Set the click handler to open the MainActivity
            val intent = Intent(context, MainActivity::class.java)
            val comp = ComponentName(context.packageName, WeatherWideWidgetProvider::class.java.name)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, comp.toString())
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            val data = Uri.withAppendedPath(
                Uri.parse("$URI_SCHEME://widget/id"),
                widgetId.toString()
            )
            intent.data = data
            val pendingIntent = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            var time = ""
            var widgetTemp = "-"
            var conditions = "-"
            val weatherIcon = R.drawable.sun_icon1
            var city = "-"
            var humidity = "-"
            var wind = "-"
            var windDir = "-"
            var isDay = true
            if (weather == null) {
                widgetTemp = "No data"
                Log.i(LOG_TAG, "weather is empty")
            } else if (weather.name == "-") {
                widgetTemp = "No data"
                Log.i(LOG_TAG, "json string was wrong")
            }else {
                city = weather.name
                humidity = weather.humidity
                humidity += "%"
                // adding kph to the wind string
                wind = weather.windSpeed + " kph"
                windDir = weather.windDeg + "\u00B0"
                time = weather.timeString
                widgetTemp = weather.tempC
                val curTemp = java.lang.Float.parseFloat(widgetTemp)
                val currTemp = Math.round(curTemp) // to get a rounded temp number
                widgetTemp = "$currTemp \u2103" // adding celsius sign to the number
                conditions = weather.mainConditions
                isDay = weather.isDay
            }
            views.setTextViewText(R.id.current_city, city)
            views.setTextViewText(R.id.current_time, time)
            views.setTextViewText(R.id.current_temp, widgetTemp)
            views.setTextViewText(R.id.humidity, humidity)
            views.setTextViewText(R.id.wind, wind)
            views.setTextViewText(R.id.wind_dir, windDir)
            views.setTextViewText(R.id.conditions, conditions)
            val iconUrl = context.resources.getString(R.string.icon_url) + weather!!.icon +
                    context.resources.getString(R.string.icon_url_end)
            val awt: AppWidgetTarget = object : AppWidgetTarget(context.applicationContext,
                R.id.widget_image, views, widgetId) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    super.onResourceReady(resource, transition)
                }
            }
            //using Glide to put the icon on screen
            val options = RequestOptions().
                override(300, 300).placeholder(weatherIcon).error(weatherIcon)
            Glide.with(context.applicationContext).asBitmap().load(iconUrl).apply(options).into(awt)
            // setting the right background  and text color
            if (isDay){
                views.setImageViewResource(R.id.widget_background, R.color.day_background)
                views.setTextColor(R.id.current_city, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.current_time, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.current_time_title, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.current_temp, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.current_temp_title, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.humidity, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.humidity_title, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.wind, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.wind_title, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.wind_dir, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.wind_dir_title, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.conditions, context.resources.getColor(R.color.text_color))
                views.setTextColor(R.id.conditions_title, context.resources.getColor(R.color.text_color))
            } else {
                views.setImageViewResource(R.id.widget_background, R.color.night_background)
                views.setTextColor(R.id.current_city, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.current_time, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.current_time_title, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.current_temp, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.current_temp_title, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.humidity, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.humidity_title, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.wind, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.wind_title, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.wind_dir, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.wind_dir_title, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.conditions, context.resources.getColor(R.color.night_text))
                views.setTextColor(R.id.conditions_title, context.resources.getColor(R.color.night_text))
            }
            // clicking anywhere in the widget - will activate the pending intent
            views.setOnClickPendingIntent(R.id.widget_background, pendingIntent)
            return views
        }
    }
}
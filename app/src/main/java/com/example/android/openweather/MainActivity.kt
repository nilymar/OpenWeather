package com.example.android.openweather

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.android.openweather.AppConstants.BASE_CURRENT_URL
import com.example.android.openweather.AppConstants.BASE_FORECAST_URL
import com.example.android.openweather.AppConstants.SHARED_PREFERENCES
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() , LoaderManager.LoaderCallbacks<List<ForecastWeather>> {

  // what to do when a loader for fetching data from OpenWeatherApi is created
  override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<ForecastWeather>> {
      //creating the string urls for fetching forecast and current weather data
      var queryCity = restorePreferences(getString(R.string.settings_city_key))
      if (queryCity!!.isEmpty())
          queryCity = resources.getString(R.string.settings_city_default)
      var forecastDays = (restorePreferences(getString(R.string.settings_forecast_days_key))!!.toInt()*8).toString()
      if (forecastDays.isEmpty())
          forecastDays = (getString(R.string.settings_forecast_days_default).toInt()*8).toString()
      // this one is only for the forecast data
      val baseForecastUri = Uri.parse(BASE_FORECAST_URL)
      val baseCurrentUri = Uri.parse(BASE_CURRENT_URL)
      // build the forecast url integrating the settings
      forecastUriBuilder = baseForecastUri.buildUpon()
      forecastUriBuilder.appendQueryParameter(getString(R.string.query_by_city), queryCity)
      forecastUriBuilder.appendQueryParameter(
          getString(R.string.query_forecast_days),
          forecastDays
      )
      forecastUriBuilder.appendQueryParameter("appid", resources.getString(R.string.api_key))
      forecastUrlString = forecastUriBuilder.toString()
      // build the current weather url integrating the settings
      currentUriBuilder = baseCurrentUri.buildUpon()
      currentUriBuilder.appendQueryParameter(getString(R.string.query_by_city), queryCity)
      currentUriBuilder.appendQueryParameter("appid", resources.getString(R.string.api_key))
      currentUrlString = currentUriBuilder.toString()
      Log.i(LOG_TAG, forecastUrlString)
      Log.i(LOG_TAG, currentUrlString)
      // deciding which loader to use according to loader id
      if (id== FORECAST_WEATHER_LOADER_ID) return ForecastWeatherLoader(this, forecastUrlString)
      else return CurrentWeatherLoader(this, currentUrlString)
    }

    // what to do after the data was fetched
    override fun onLoadFinished(loader: Loader<List<ForecastWeather>>, loadedWeathers: List<ForecastWeather>?) {
        // if it is forecast weather data
        if(loader.id == FORECAST_WEATHER_LOADER_ID) {
            // Clear the adapter of previous weathers
            this.weathers.clear()
            listView.removeAllViews()
            // after loading is over - don't show the progress indicator
            progressBar.visibility = View.GONE
            // if there is a valid list of {@link Weather}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (loadedWeathers != null && loadedWeathers.isNotEmpty()) {
                this.weathers.addAll(loadedWeathers)
                mAdapter!!.notifyDataSetChanged()
                // set the city name as title before the recylerView list
                val forecastTitle = getString(R.string.forecast_title) + this.weathers[0].name +
                        ", " + this.weathers[0].country
                forecastCity.text = forecastTitle
            } else {
                emptyView.visibility = View.VISIBLE
                emptyView.setText(R.string.no_data_available)
            }
        }else {// if it is current weather data
            this.currentWeather.clear()
            if (loadedWeathers != null && loadedWeathers.isNotEmpty()) {
                this.currentWeather.addAll(loadedWeathers)
                showCurrentWeather()
            }
        }
    }

    // for the current weather data - this is the method to show it on screen
    private fun showCurrentWeather(){
        if (currentWeather.isNotEmpty()) {
            currentDateView.text = currentWeather[0].dateString
            currentTimeView.text = currentWeather[0].timeString
            val tempC = java.lang.Float.parseFloat(currentWeather[0].tempC)
            val tempCRound = tempC.roundToInt() // to get a rounded temp number
            val tempCString = "$tempCRound \u2103" // adding celsius symbol to the temp
            currentTempView.text = tempCString
            val wind = currentWeather[0].windSpeed + " kph"
            currentWindView.text = wind
            val windDeg = currentWeather[0].windDeg + "\u00B0"
            currentWindDirView.text = windDeg
            val humidityText = currentWeather[0].humidity + "%"
            currentHumidityView.text = humidityText
            currentConditionView.text = currentWeather[0].conditionsDesc
            // using glide to show the icon (from an internet url) on screen
            val iconUrl = resources.getString(R.string.icon_url) + currentWeather[0].icon +
                    resources.getString(R.string.icon_url_end)
            if (!iconUrl.equals("")) {
                Glide.with(this)
                    .load(iconUrl)
                    .into(currentIconView);
            }
            // set the background and text color to fit day or night
            val isDay = currentWeather[0].isDay
            if (isDay) {
                currentLayout.setBackgroundResource(R.color.light_blue)
                currentTitle.setTextColor(resources.getColor(R.color.colorPrimary))
                currentDateView.setTextColor(resources.getColor(R.color.colorPrimary))
                currentTimeView.setTextColor(resources.getColor(R.color.text_color))
                currentTempView.setTextColor(resources.getColor(R.color.text_color))
                currentWindView.setTextColor(resources.getColor(R.color.text_color))
                currentWindDirView.setTextColor(resources.getColor(R.color.text_color))
                currentHumidityView.setTextColor(resources.getColor(R.color.text_color))
                currentConditionView.setTextColor(resources.getColor(R.color.text_color))
                currentTimeTitle.setTextColor(resources.getColor(R.color.text_color))
                currentTempTitle.setTextColor(resources.getColor(R.color.text_color))
                currentWindTitle.setTextColor(resources.getColor(R.color.text_color))
                currentWindDirTitle.setTextColor(resources.getColor(R.color.text_color))
                currentHumidityTitle.setTextColor(resources.getColor(R.color.text_color))
                currentConditionTitle.setTextColor(resources.getColor(R.color.text_color))
            } else {
                currentLayout.setBackgroundResource(R.color.night_blue)
                currentTitle.setTextColor(resources.getColor(R.color.night_title))
                currentDateView.setTextColor(resources.getColor(R.color.night_title))
                currentTimeView.setTextColor(resources.getColor(R.color.night_text))
                currentTempView.setTextColor(resources.getColor(R.color.night_text))
                currentWindView.setTextColor(resources.getColor(R.color.night_text))
                currentWindDirView.setTextColor(resources.getColor(R.color.night_text))
                currentHumidityView.setTextColor(resources.getColor(R.color.night_text))
                currentConditionView.setTextColor(resources.getColor(R.color.night_text))
                currentTimeTitle.setTextColor(resources.getColor(R.color.night_text))
                currentTempTitle.setTextColor(resources.getColor(R.color.night_text))
                currentWindTitle.setTextColor(resources.getColor(R.color.night_text))
                currentWindDirTitle.setTextColor(resources.getColor(R.color.night_text))
                currentHumidityTitle.setTextColor(resources.getColor(R.color.night_text))
                currentConditionTitle.setTextColor(resources.getColor(R.color.night_text))
            }
            updateWeatherWidget()// with new current weather data - we update the widget as well
        }
    }

    override fun onLoaderReset(loader: Loader<List<ForecastWeather>>) {
        weathers.clear() // if the loader was restarted - clean the weather array
    }

    private lateinit var connMgr :ConnectivityManager
    // get details on the currently active default data network
    private var isConnected: Boolean = false
    private lateinit var currentLayout: RelativeLayout
    private lateinit var currentDateView: TextView
    private lateinit var currentTitle: TextView
    private lateinit var currentTimeView: TextView
    private lateinit var currentTimeTitle: TextView
    private lateinit var currentIconView: ImageView
    private lateinit var currentTempView: TextView
    private lateinit var currentTempTitle: TextView
    private lateinit var currentWindView: TextView
    private lateinit var currentWindTitle: TextView
    private lateinit var currentWindDirView: TextView
    private lateinit var currentWindDirTitle: TextView
    private lateinit var currentConditionView: TextView
    private lateinit var currentConditionTitle: TextView
    private lateinit var currentHumidityView: TextView
    private lateinit var currentHumidityTitle: TextView
    private lateinit var emptyView: TextView
    private lateinit var forecastCity: TextView
    var currentWeather: ArrayList<ForecastWeather> = ArrayList()
    private lateinit var listView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var mySwipeRefreshLayout: SwipeRefreshLayout
    private var loaderManager: LoaderManager? = null // loadManager to be used in this activity
    // Adapter for the forecast weather days  (i.e. items in the array)
    private var mAdapter: WeatherAdapter? = null
    var weathers: ArrayList<ForecastWeather> = ArrayList()
    lateinit var forecastUriBuilder: Uri.Builder
    lateinit var forecastUrlString: String
    lateinit var currentUriBuilder: Uri.Builder
    lateinit var currentUrlString: String
    private var widgetId = 0 // for the specific widget id
    private var widgetProvider = ""// which provider built the specific widget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // set the views values from the layout file
        currentLayout = findViewById(R.id.current_layout)
        currentTitle = findViewById(R.id.current_title)
        currentDateView = findViewById(R.id.current_date)
        currentTimeView = findViewById(R.id.current_time_text)
        currentTempView = findViewById(R.id.current_temp_text)
        currentIconView = findViewById(R.id.current_icon)
        currentWindView = findViewById(R.id.current_wind_text)
        currentWindDirView = findViewById(R.id.current_wind_dir_text)
        currentHumidityView = findViewById(R.id.current_humidity_text)
        currentConditionView = findViewById(R.id.current_conditions_text)
        currentTimeTitle = findViewById(R.id.current_time)
        currentTempTitle = findViewById(R.id.current_temp)
        currentWindTitle = findViewById(R.id.current_wind)
        currentWindDirTitle = findViewById(R.id.current_wind_dir)
        currentHumidityTitle = findViewById(R.id.current_humidity)
        currentConditionTitle = findViewById(R.id.current_conditions)
        emptyView = findViewById(R.id.empty_view)
        forecastCity = findViewById(R.id.forecast_city)
        listView = findViewById(R.id.list)
        progressBar = findViewById(R.id.loading_spinner)
        mySwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        widgetProvider = intent.getStringExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER)
        widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        // set a new adapter for the list
        mAdapter = WeatherAdapter(applicationContext, weathers)
        // vertical RecyclerView
        val layoutManager = LinearLayoutManager(this)
        listView.layoutManager = layoutManager
        listView.itemAnimator = DefaultItemAnimator()
        // set the adapter on the listView (this time - recyclerView)
        listView.adapter = mAdapter
        // get a reference to the LoaderManager, in order to interact with loaders
        loaderManager = LoaderManager.getInstance(this)
        mySwipeRefreshLayout.isRefreshing = false // set the status of swipe refreshing to false
        mySwipeRefreshLayout.setOnRefreshListener {
            // this create a new list of forecast dates when you refresh
            refreshData() // using the method to reload data from the internet
        }
        requestOperation() // requesting data from the weather api for the first time
    }

    // method for re-loading the data from the internet
    private fun refreshData() {
        loaderManager!!.destroyLoader(FORECAST_WEATHER_LOADER_ID) // so that the list will re-create
        weathers.clear() // clearing current list from the adapter
        listView.removeAllViews() // removing the items from the recyclerView
        requestOperation() // requesting data from the internet
        mySwipeRefreshLayout.isRefreshing =
            false // make sure the refresh spinner disappears when using with swipeRefresh
    }

    // this method checks if the internet is on - if so - starts the loader
    private fun requestOperation() {
        connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // get details on the currently active default data network
        val networkInfo = connMgr.activeNetworkInfo
        isConnected = networkInfo != null && networkInfo.isConnected
        if (!isConnected) { // if there is no internet connection - don't show progress bar and set
            // the no_internet_connection message
            progressBar.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            emptyView.setText(R.string.no_internet_connection)
        } else {
            emptyView.visibility = View.GONE
            loaderManager!!.initLoader(FORECAST_WEATHER_LOADER_ID, null, this)
            loaderManager!!.initLoader(CURRENT_WEATHER_LOADER_ID, null, this)
        }
    }

    // This method to restore the custom preferences data
    private fun restorePreferences(key: String): String? {
        val myPreferences =
            getSharedPreferences(SHARED_PREFERENCES + widgetId, Context.MODE_PRIVATE)
        return if (myPreferences.contains(key)) {
            myPreferences.getString(key, "")
        } else
            ""
    }

    // updating the widget with the loaded data
    private fun updateWeatherWidget() {
        if (currentWeather.isEmpty()) {
            Log.i(LOG_TAG, "updateWeatherWidget activated currentWeather null")
            return
        } else {
            Log.i(LOG_TAG, "updateWeatherWidget activated currentWeather not empty")
            val appWidgetManager = AppWidgetManager.getInstance(this)
            // update the relevant weather widget
            if (widgetProvider.contains("WeatherWideWidgetProvider")) {
                WeatherWideWidgetProvider.updateAppWidget(this, appWidgetManager, currentWeather[0],
                    widgetId)
            } else {
                WeatherWidgetProvider.updateAppWidget(this, appWidgetManager, currentWeather[0], widgetId)
            }
        }
    }

    // inflating the menu file
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    // setting the options for actions in the menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                // start SettingActivity file when user click on settings
                return if (widgetProvider.contains("WeatherWideWidgetProvider")) {
                    val settingsIntent = Intent(this, SettingActivityWide::class.java)
                    settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    settingsIntent.putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                        widgetProvider
                    )
                    settingsIntent.putExtra("from_main", 1)
                    startActivity(settingsIntent)
                    true
                } else {
                    val settingsIntent = Intent(this, SettingActivity::class.java)
                    settingsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    settingsIntent.putExtra(
                        AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                        widgetProvider
                    )
                    settingsIntent.putExtra("from_main", 1)
                    startActivity(settingsIntent)
                    true
                }
            }
            // inflate about window when the user click on about
            R.id.action_about -> showAbout()
        }
        return super.onOptionsItemSelected(item)
    }

    // creating the window with the about screen (credits) for the app (option in the navigation drawer menu)
    @SuppressLint("InflateParams")
    private fun showAbout() {
        // Inflate the about message contents
        val messageView = layoutInflater.inflate(R.layout.about_window, null, false)
        val textView: TextView = messageView.findViewById(R.id.about_credits)
        val defaultColor = textView.textColors.defaultColor
        textView.setTextColor(defaultColor)
        val builder = AlertDialog.Builder(this)
        builder.setView(messageView)
        builder.create()
        builder.show()
    }

    companion object {
        // Tag for the log messages
        val LOG_TAG: String = MainActivity::class.java.name
        // loaders ids
        private const val FORECAST_WEATHER_LOADER_ID = 1
        private const val CURRENT_WEATHER_LOADER_ID = 2
    }

}

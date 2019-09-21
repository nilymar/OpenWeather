package com.example.android.openweather

object AppConstants {

    const val GPS_REQUEST = 1001
    const val SHARED_PREFERENCES = "openweather"
    const val REQUEST_LOCATION_PERMISSION = 1
    const val ACTION_UPDATE_WEATHER_WIDGET_ONLINE =
        "com.example.android.openweather.action.update_weather_widget_online"
    const val URI_SCHEME = "every_widget"
    const val READ_TIMEOUT = 10000 /* milliseconds */
    const val CONNECT_TIMEOUT = 15000 /* milliseconds */
    const val REQUEST_METHOD = "GET"
    const val BASE_FORECAST_URL =
        "https://api.openweathermap.org/data/2.5/forecast?units=metric"
    const val BASE_CURRENT_URL =
        "https://api.openweathermap.org/data/2.5/weather?units=metric"
}
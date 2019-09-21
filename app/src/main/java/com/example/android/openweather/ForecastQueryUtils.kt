package com.example.android.openweather

import android.text.TextUtils
import android.util.Log
import com.example.android.openweather.AppConstants.CONNECT_TIMEOUT
import com.example.android.openweather.AppConstants.READ_TIMEOUT
import com.example.android.openweather.AppConstants.REQUEST_METHOD
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

// Helper methods related to requesting and receiving weather forecast from OpenWeatherMap API
object ForecastQueryUtils {
    // Tag for log messages
    private val LOG_TAG = ForecastQueryUtils::class.java.simpleName

    // Query the OpenWeatherMap API and return a list of {@link Weather} objects.
    fun fetchWeathers(requestUrl: String): List<ForecastWeather>? {
        // Create URL object
        val url = createUrl(requestUrl)
        // Perform HTTP request to the URL and receive a JSON response back
        var jsonResponse: String? = null
        try {
            jsonResponse = makeHttpRequest(url)
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e)
        }
        // Extract relevant fields from the JSON response and create a list of {@link Weather}s
        // Return the list of {@link Weather}s
        return extractFeatureFromJson(jsonResponse)
    }

    // Returns new URL object from the given string URL.
    private fun createUrl(stringUrl: String): URL? {
        var url: URL? = null
        try {
            url = URL(stringUrl)
        } catch (e: MalformedURLException) {
            Log.e(LOG_TAG, "Error with creating URL ", e)
        }
        return url
    }

    // Make an HTTP request to the given URL and return a String as the response.
    @Throws(IOException::class)
    private fun makeHttpRequest(url: URL?): String {
        var jsonResponse = ""
        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse
        }
        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.readTimeout = READ_TIMEOUT
            urlConnection.connectTimeout = CONNECT_TIMEOUT
            urlConnection.requestMethod = REQUEST_METHOD
            urlConnection.connect()
            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) { //HTTP_OK = 200
                inputStream = urlConnection.inputStream
                jsonResponse = readFromStream(inputStream)
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.responseCode)
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Problem retrieving the JSON results.", e)
        } finally {
            urlConnection?.disconnect()
            inputStream?.close()
        }
        return jsonResponse
    }

    // Convert the {@link InputStream} into a String which contains the
    // whole JSON response from the server.
    @Throws(IOException::class)
    private fun readFromStream(inputStream: InputStream?): String {
        val output = StringBuilder()
        if (inputStream != null) {
            val inputStreamReader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
            val reader = BufferedReader(inputStreamReader)
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line)
                line = reader.readLine()
            }
        }
        return output.toString()
    }

    // Return a list of {@link Weather} objects that has been built up from
    // parsing the given JSON response.
    private fun extractFeatureFromJson(weatherJSON: String?): List<ForecastWeather>? {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(weatherJSON)) {
            Log.e(LOG_TAG, "response is empty")
            return null
        }
        // Create an empty ArrayList that we can start adding weathers to
        val weathers = ArrayList<ForecastWeather>()
        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Create JSONObjects from the JSON response string
            val baseJsonObject = JSONObject(weatherJSON)
            val list = baseJsonObject.getJSONArray("list")
            val cityObject = baseJsonObject.getJSONObject("city")
            val city = cityObject.optString("name").trim { it <= ' ' }
            val timeZone = cityObject.getLong("timezone")
            val country = cityObject.getString("country")
            for (i in 0 until list.length()) {
                weathers.add(
                    extractWeather(
                        list.getJSONObject(i),
                        city,
                        country,
                        timeZone
                    )
                )
            }

        } catch (e: JSONException) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("ForecastQueryUtils", "Problem parsing the weather JSON results", e)
        }
        return weathers
    }

    private fun extractWeather(currentForecast: JSONObject, city: String, country: String, timeZone: Long):
            ForecastWeather {
        // extract dt - i.e. long number representing time and date
        val dt = currentForecast.getLong("dt")
        val main = currentForecast.getJSONObject("main")
        // extract average temp
        val tempC = main.optString("temp").trim { it <= ' ' }
        // extract min temp
        val minTemp = main.optString("temp_min").trim { it <= ' ' }
        // extract max temp
        val maxTemp = main.optString("temp_max").trim { it <= ' ' }
        // Extract the humidity
        val humidity = main.optString("humidity").trim { it <= ' ' }
        // Extract the conditions array
        val conditionArray = currentForecast.getJSONArray("weather")
        // from the array - take the first object
        val conditionObject = conditionArray.getJSONObject(0)
        // from the object - take the short condition description
        val mainConditions = conditionObject.optString("main").trim { it <= ' ' }
        // and the long condition description
        val conditionsDesc =
            conditionObject.optString("description").trim { it <= ' ' }
        // and the condition icon name
        val icon = conditionObject.optString("icon").trim { it <= ' ' }
        var isDay = true
        // discover if it is day or night from the icon name
        if (!icon.contains("d")) isDay = false
        // Extract wind conditions
        val windObject = currentForecast.getJSONObject("wind")
        val windSpeed = windObject.optString("speed").trim { it <= ' ' }
        val windDeg = windObject.optString("deg").trim { it <= ' ' }
        // create the right time and date string for the weather location
        val tz = TimeZone.getDefault();
        val now = Date();
        val offsetFromUtc = tz.getOffset(now.time)/1000;
        val timeString = SimpleDateFormat("HH:mm", Locale.ENGLISH).
            format(Date((dt + timeZone - offsetFromUtc)*1000))
        val dateString = SimpleDateFormat(
            "dd/MM/yyyy", Locale.ENGLISH).
            format(Date((dt + timeZone - offsetFromUtc) *1000))
        // Create a new {@link ForecastWeather} object
        return ForecastWeather(
            city, dt, tempC, minTemp, maxTemp, windSpeed, windDeg, mainConditions,
            conditionsDesc, humidity, icon, isDay, country, timeZone, timeString, dateString
        )
    }
}

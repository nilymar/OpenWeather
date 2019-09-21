package com.example.android.openweather

class ForecastWeather {
    // Get the name of the city of weather
    var name: String = ""
    /** current wind in kmh  */
    // Get the current wind speed
    var windSpeed: String = ""
    // Get the current wind degree
    var windDeg: String = ""
    // Get the date data - a Long number
    var dt: Long = 0
    // get the average/current temp
    var tempC: String = ""
    // Get the min temp
    var minTemp: String = ""
    // Get the max temp
    var maxTemp: String = ""
    // Get the weather conditions short description
    var mainConditions: String = ""
    // Get the weather conditions long description
    var conditionsDesc: String = ""
    // Get the humidity
    var humidity: String = ""
    // Get the icon name
    var icon: String = ""
    // indication if day or night
    var isDay: Boolean
    // country initials for locale
    var country: String = ""
    // city's timeZone - i.e. the distance in seconds from utc time zone
    var timeZone: Long
    // a string with the formatted time
    var timeString: String = ""
    // a string with the formatted date
    var dateString: String

    /**
     * Create a new ForecastWeather object
     * @param name
     *
     * @param dt
     *
     * @param tempC
     *
     * @param minTemp
     *
     * @param maxTemp
     *
     * @param windSpeed
     *
     * @param windDeg
     *
     * @param mainConditions
     *
     * @param conditionsDesc
     *
     * @param humidity
     *
     * @param icon
     *
     * @param isDay
     *
     * @param country
     *
     * @param timeZone
     *
     * @param timeString
     *
     * @param dateString
     */
    constructor(
        name: String, dt: Long, tempC: String, minTemp: String, maxTemp: String,
        windSpeed: String, windDeg: String, mainConditions: String, conditionsDesc: String, humidity: String,
        icon: String, isDay: Boolean, country: String, timeZone: Long, timeString: String, dateString: String) {
        this.name = name
        this.dt = dt
        this.tempC = tempC
        this.minTemp = minTemp
        this.maxTemp = maxTemp
        this.windSpeed = windSpeed
        this.windDeg = windDeg
        this.mainConditions = mainConditions
        this.conditionsDesc = conditionsDesc
        this.humidity = humidity
        this.icon = icon
        this.isDay = isDay
        this.country = country
        this.timeZone = timeZone
        this.timeString = timeString
        this.dateString = dateString
    }
}
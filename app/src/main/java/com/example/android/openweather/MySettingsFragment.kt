package com.example.android.openweather

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.example.android.openweather.AppConstants.GPS_REQUEST
import com.example.android.openweather.AppConstants.REQUEST_LOCATION_PERMISSION
import com.example.android.openweather.AppConstants.SHARED_PREFERENCES
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class MySettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener,
    EasyPermissions.PermissionCallbacks {
    private var gpsRequestStatus = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var wayLatitude = 0.0
    private var wayLongitude = 0.0
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    // to store the device location data
    private val place = arrayOf("")
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var switchPreference: SwitchPreference? = null
    private var autoCompletePreference: TextAutoCompletePreference? = null
    private var numberPickerPreference: NumberPickerPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val bundle = this.arguments
        if (bundle != null) {
            widgetId = bundle.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        switchPreference = findPreference(
            resources.getString(R.string.settings_switch_key)
        )
        if (switchPreference != null) {
            bindPreferenceSummaryToValue(switchPreference!!)
        }
        autoCompletePreference = findPreference(resources.getString(R.string.settings_city_key))
        if (autoCompletePreference != null) {
            bindPreferenceSummaryToValue(autoCompletePreference!!)
            autoCompletePreference!!.isSelectable = true
        }
        numberPickerPreference = findPreference(resources.getString(R.string.settings_forecast_days_key))
        if (numberPickerPreference != null) {
            bindPreferenceSummaryToValue(numberPickerPreference!!)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (preference is NumberPickerPreference) {
            val chosenNumber = newValue.toString()
            if (newValue.toString().isEmpty()) {
                val defaultValue = numberPickerPreference!!.defaultValue
                numberPickerPreference!!.setSummary(defaultValue)
                savePreferences(preference.getKey(), defaultValue.toString())
            } else {
                numberPickerPreference!!.summary = chosenNumber
                savePreferences(preference.getKey(), chosenNumber)
            }
        } else if (preference is SwitchPreference) {
            place[0] = ""
            if (!switchPreference!!.isChecked) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
                locationRequest = LocationRequest.create()
                locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest!!.interval = (10 * 1000).toLong() // 10 seconds
                locationRequest!!.fastestInterval = (5 * 1000).toLong() // 5 seconds
                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        if (locationResult == null) {
                            Log.i(FRAGMENT_TAG, "location result is null")
                            switchPreference!!.isChecked = false
                            autoCompletePreference!!.isEnabled = true
                            return
                        }
                        for (location in locationResult.locations) {
                            if (location != null) {
                                fetchLocationData(location)
                            }
                            if (fusedLocationClient != null) {
                                fusedLocationClient!!.removeLocationUpdates(locationCallback!!)
                            }
                        }
                    }
                }
                // check the gps status, also creates the dialog if gps not enables
                GpsUtils(this.requireContext()).turnGPSOn(object : GpsUtils.OnGpsListener {
                    override fun gpsStatus(isGPSEnable: Boolean) {
                        // GPS request is success/failure
                        gpsRequestStatus = isGPSEnable
                    }
                })

                if (!gpsRequestStatus) {
                    Toast.makeText(context, "Please turn on GPS", Toast.LENGTH_SHORT).show()
                    Log.i(FRAGMENT_TAG, "gpsRequestStatus is false")
                    switchPreference!!.isChecked = false
                    autoCompletePreference!!.isEnabled = true
                } else {
                    Log.i(FRAGMENT_TAG, "gpsRequestStatus is true")
                    getLocation()
                }
            } else
                autoCompletePreference!!.isEnabled = true
        } else if (preference is TextAutoCompletePreference) {
            val chosenCountry = newValue.toString()
            if (newValue.toString().isEmpty()) {
                val defaultValue = autoCompletePreference!!.defaultValue
                autoCompletePreference!!.setSummary(defaultValue)
                savePreferences(preference.getKey(), defaultValue)
            } else {
                autoCompletePreference!!.summary = chosenCountry
                savePreferences(preference.getKey(), chosenCountry)
            }
        }
        return true
    }

    private fun bindPreferenceSummaryToValue(preference: Preference) {
        preference.onPreferenceChangeListener = this
        if (preference is TextAutoCompletePreference) { // for the name
            val preferenceString = restorePreferences(preference.getKey())
            if (preferenceString == null || preferenceString.isEmpty()) {
                // when there is no saved data - put the default value
                onPreferenceChange(preference, autoCompletePreference!!.defaultValue)
            } else {
                onPreferenceChange(preference, preferenceString)
            }
        } else if (preference is SwitchPreference) {
            // when the setting screen opens - always set the switch preference to not checked
            switchPreference!!.isChecked = false
        } else if (preference is NumberPickerPreference) {
            val preferenceString = restorePreferences(preference.getKey())
            if (preferenceString == null || preferenceString.isEmpty()) {
                // when there is no saved data - put the default value
                onPreferenceChange(preference, numberPickerPreference!!.defaultValue)
            } else {
                onPreferenceChange(preference, preferenceString)
            }
        }
    }

    // only relevant to dialog preferences - not for the switch one
    override fun onDisplayPreferenceDialog(preference: Preference) {
        // check if dialog is already showing
        assert(fragmentManager != null)
        if (fragmentManager!!.findFragmentByTag(FRAGMENT_TAG) != null) {
            return
        }
        val f: DialogFragment?
        if (preference is NumberPickerPreference) {
            f = NumberPickerPreferenceDialogFragment.newInstance(preference.getKey(), widgetId)
        } else if (preference is TextAutoCompletePreference) {
            f = TextAutoCompletePreferenceDialogFragment.newInstance(preference.getKey(), widgetId)
        } else
            f = null
        if (f != null) {
            f.setTargetFragment(this, 0)
            f.show(fragmentManager!!, FRAGMENT_TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    // This method to store the custom preferences changes
    private fun savePreferences(key: String, value: String) {
        val myPreferences = this.activity!!.getSharedPreferences(
            SHARED_PREFERENCES + widgetId, Context.MODE_PRIVATE
        )
        val myEditor = myPreferences.edit()
        myEditor.putString(key, value)
        myEditor.apply()
    }

    // This method to restore the custom preferences data
    private fun restorePreferences(key: String): String? {
        val myPreferences = this.activity!!.getSharedPreferences(
            SHARED_PREFERENCES + widgetId, Context.MODE_PRIVATE
        )
        return if (myPreferences.contains(key))
            myPreferences.getString(key, "")
        else
            ""
    }

    //from here - what happen when the switchPreference is checked - i.e. - location is taken from device
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (!EasyPermissions.hasPermissions(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) && !EasyPermissions.hasPermissions(context!!, Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            EasyPermissions.requestPermissions(
                this, "Please grant the location permission",
                REQUEST_LOCATION_PERMISSION, *perms
            )
        } else {
            //  important!!! - when you switch the GPS on and off - stops getting location data unless you
            // add the following line - i.e - request location updates
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback!!, null)
            fusedLocationClient!!.lastLocation.addOnSuccessListener(activity!!) { location ->
                location?.let { fetchLocationData(it) } ?: fusedLocationClient!!.requestLocationUpdates(
                    locationRequest,
                    locationCallback!!, null
                )
            }
        }
    }

    private fun fetchLocationData(location: Location) {
        wayLatitude = location.latitude
        wayLongitude = location.longitude
        try {
            val geo = Geocoder(context, Locale.US)
            val addresses = geo.getFromLocation(wayLatitude, wayLongitude, 1)
            if (addresses.isEmpty()) {
                Log.i(FRAGMENT_TAG, "Waiting for Location")
            } else {
                val city = addresses[0].locality
                val country = addresses[0].countryName
                place[0] = "$city, $country"
                if (place[0] != "") {
                    // how to set value ->
                    autoCompletePreference!!.summary = place[0]
                    autoCompletePreference!!.isEnabled = false
                    savePreferences(resources.getString(R.string.settings_city_key), place[0])
                } else {
                    Toast.makeText(context, "Device location not found", Toast.LENGTH_SHORT).show()
                    autoCompletePreference!!.isEnabled = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // getFromLocation() may sometimes fail
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        // Some permissions have been granted
        Toast.makeText(context, "Location permission approved", Toast.LENGTH_SHORT).show()
        if (gpsRequestStatus)
            getLocation()
        else {
            switchPreference!!.isChecked = false
            Toast.makeText(context, "Location permission approved but GPS is off", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Some permissions have been denied
        Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        switchPreference!!.isChecked = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // goes back from the activity and do what needed when it is a request to enable device gps
            GPS_REQUEST -> when (resultCode) {
                Activity.RESULT_OK -> {
                    Log.i(FRAGMENT_TAG, "User agreed to make required location settings changes.")
                    gpsRequestStatus = true
                    getLocation()
                }
                Activity.RESULT_CANCELED -> {
                    Log.i(FRAGMENT_TAG, "User chose not to make required location settings changes.")
                    autoCompletePreference!!.isEnabled = true
                    switchPreference!!.isChecked = false
                }
            }
        }
    }

    companion object {
        private const val FRAGMENT_TAG = "Setting_fragment"
    }
}

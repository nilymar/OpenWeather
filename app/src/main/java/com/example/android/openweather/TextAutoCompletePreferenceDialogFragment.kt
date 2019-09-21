package com.example.android.openweather

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.preference.PreferenceDialogFragmentCompat
import com.example.android.openweather.AppConstants.SHARED_PREFERENCES
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.util.ArrayList

class TextAutoCompletePreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    private var mAutoCompleteTextView: AutoCompleteTextView? = null
    private var mValue: String? = null
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    // get the TextAutoCompletePreference instance
    private val textAutoCompletePreference: TextAutoCompletePreference
        get() = preference as TextAutoCompletePreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            widgetId = bundle.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        if (savedInstanceState == null) {
            // if it is first run after installation - get the default value
            mValue = textAutoCompletePreference.defaultValue
        } else
        // if not - there is a saved value
            mValue = savedInstanceState.getString(SAVE_STATE_VALUE)
    }

    // save the value
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SAVE_STATE_VALUE, mValue)
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        // finding the numberPicker view
        mAutoCompleteTextView = view.findViewById(R.id.auto_complete)
        mAutoCompleteTextView!!.setSelectAllOnFocus(true)
        // the places that will auto appear when typing similar strings
        var PLACES = arrayOfNulls<String>(0)
        var baseObj: JSONObject? = null
        val places = ArrayList<String>()
        var name: String
        var country: String
        val cities: JSONArray?
        try {
            baseObj = JSONObject(loadJSONFromAsset())
        } catch (e1: JSONException) {
            e1.printStackTrace()
        }

        if (baseObj != null) {
            cities = baseObj.getJSONArray("places")
            val length = cities.length()
            if (length>0) {
                for (i in 0 until length) {
                    val currentPlace = cities.getJSONObject(i)
                    name = currentPlace.getString("name")
                    country = currentPlace.getString("country")
                    places.add("$name, $country")
                }
            }


            PLACES = arrayOfNulls(places.size)
            var i = 0
            val count = places.size
            while (i < count) {
                PLACES[i] = places[i]
                i++
            }
        }
        if (PLACES.isNotEmpty()) {
            val adapter = ArrayAdapter(
                context!!,
                android.R.layout.simple_dropdown_item_1line, PLACES
            )
            mAutoCompleteTextView!!.setAdapter(adapter)
        }
        // throw an IllegalStateException if there is no NumberPicker view
        if (mAutoCompleteTextView == null) {
            throw IllegalStateException("Dialog view must contain an auto_complete with id")
        }
        // set the value for the AutoCompleteTextView - i.e. name of last place saved
        mValue = restorePreferences(resources.getString(R.string.settings_city_key))
        mAutoCompleteTextView!!.setText(mValue)
    }

    private fun loadJSONFromAsset(): String? {
        val json: String
        try {
            val `is` = context!!.assets.open("citylist.json")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    // This method to store the custom preferences changes
    private fun savePreferences(key: String, value: String) {
        val activity = this.activity
        val myPreferences: SharedPreferences
        if (activity != null) {
            myPreferences = activity.getSharedPreferences(
                SHARED_PREFERENCES + widgetId,
                Context.MODE_PRIVATE
            )
            val myEditor = myPreferences.edit()
            myEditor.putString(key, value)
            myEditor.apply()
        }
    }

    // This method to restore the custom preferences data
    private fun restorePreferences(key: String): String? {
        val activity = activity
        val myPreferences: SharedPreferences
        if (activity != null) {
            myPreferences = activity.getSharedPreferences(
                SHARED_PREFERENCES + widgetId,
                Context.MODE_PRIVATE
            )
            return if (myPreferences.contains(key))
                myPreferences.getString(key, "")
            else
                ""
        } else
            return ""
    }

    // what to do when the dialog is closed
    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            mAutoCompleteTextView!!.clearFocus()
            val value = mAutoCompleteTextView!!.text.toString()
            savePreferences(resources.getString(R.string.settings_city_key), value)
            textAutoCompletePreference.value = value
            textAutoCompletePreference.summary = value
            if (textAutoCompletePreference.callChangeListener(value)) {
                textAutoCompletePreference.value = value
            }
        }
    }

    companion object {
        // name of key for saving for state change (like rotating the screen)
        private const val SAVE_STATE_VALUE = "TextAutoCompletePreferenceDialogFragment.value"

        fun newInstance(key: String, id: Int): TextAutoCompletePreferenceDialogFragment {
            val fragment = TextAutoCompletePreferenceDialogFragment()
            val args = Bundle(1)
            args.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            fragment.arguments = args
            return fragment
        }
    }
}

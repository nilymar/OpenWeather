package com.example.android.openweather

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.preference.PreferenceDialogFragmentCompat
import com.example.android.openweather.AppConstants.SHARED_PREFERENCES

class NumberPickerPreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    private var mNumberPicker: NumberPicker? = null
    private var mValue: Int = 0
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    // get the NumberPickerPreference instance
    private val numberPickerPreference: NumberPickerPreference
        get() = preference as NumberPickerPreference

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
            mValue = numberPickerPreference.defaultValue
        } else {
            // if not - there is a saved value
            mValue = savedInstanceState.getInt(SAVE_STATE_VALUE)
        }
    }

    // save the value
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_STATE_VALUE, mValue)
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        // finding the numberPicker view
        mNumberPicker = view.findViewById(R.id.number_picker)
        // throw an IllegalStateException if there is no NumberPicker view
        if (mNumberPicker == null) {
            throw IllegalStateException("Dialog view must contain an NumberPicker with id")
        }
        // set the values for the NumberPicker view - min, max, default/value to show and if scrollable
        mNumberPicker!!.minValue = numberPickerPreference.minValue
        mNumberPicker!!.maxValue = numberPickerPreference.maxValue
        mValue = Integer.parseInt(restorePreferences(resources.getString(R.string.settings_forecast_days_key))!!)
        mNumberPicker!!.value = mValue
        mNumberPicker!!.wrapSelectorWheel = numberPickerPreference.selectorWheelValue
    }

    // This method to store the custom preferences changes
    private fun savePreferences(key: String, value: String) {
        val activity = this.activity
        val myPreferences: SharedPreferences
        if (activity != null) {
            myPreferences = activity.getSharedPreferences(SHARED_PREFERENCES + widgetId,
                Context.MODE_PRIVATE)
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
            myPreferences = activity.getSharedPreferences(SHARED_PREFERENCES + widgetId,
                Context.MODE_PRIVATE)
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
            mNumberPicker!!.clearFocus()
            val value = mNumberPicker!!.value.toString()
            savePreferences(resources.getString(R.string.settings_forecast_days_key), value)

            if (numberPickerPreference.callChangeListener(value)) {
                numberPickerPreference.value = Integer.parseInt(value)
            }
        }
    }

    companion object {
        private val SAVE_STATE_VALUE = "NumberPickerPreferenceDialogFragment.value"

        fun newInstance(key: String, id: Int): NumberPickerPreferenceDialogFragment {
            val fragment = NumberPickerPreferenceDialogFragment()
            val args = Bundle(1)
            args.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            args.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            fragment.arguments = args
            return fragment
        }
    }
}

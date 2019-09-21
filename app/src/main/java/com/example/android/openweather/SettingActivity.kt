package com.example.android.openweather

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.example.android.openweather.AppConstants.ACTION_UPDATE_WEATHER_WIDGET_ONLINE
import com.example.android.openweather.AppConstants.GPS_REQUEST

class SettingActivity : AppCompatActivity() {
    internal var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID // the id of the specific widget
    internal var comp = ""// the component name for the provider class
    internal var firstTime =
        0 // indicator for entry to settingActivity - i.e. is it first time (while installing) or not

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setResult(Activity.RESULT_CANCELED)
        val intent = intent
        val extras = intent.extras
        val actionBar = supportActionBar
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            comp = extras.getString(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, "")
            if (!extras.containsKey("from_main")) {
                assert(actionBar != null)
                actionBar!!.setHomeButtonEnabled(false) // disable the button
                actionBar.setDisplayHomeAsUpEnabled(false) // remove the left caret
                actionBar.setDisplayShowHomeEnabled(false) // remove the icon
            } else {
                // when the setting are accessed from main - i.e. not on installation
                firstTime = extras.getInt("from_main", 0)
                assert(actionBar != null)
                actionBar!!.setHomeButtonEnabled(true) // disable the button
                actionBar.setDisplayHomeAsUpEnabled(true) // remove the left caret
                actionBar.setDisplayShowHomeEnabled(true) // remove the icon
            }
        }
        // put the setting fragment in its frame
        val msf = MySettingsFragment()
        val bundle = Bundle()
        // make sure the fragment gets the right widget id and provider
        bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        bundle.putString(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, comp)
        msf.arguments = bundle
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, msf).commit()
    }

    override fun onBackPressed() {
        // i.e. not the first time the widget is accessed - so go back to main
        if (firstTime == 1) {
            // didn't come from the widget - so go back to where it is defined - i.e. mainActivity
            val upIntent = NavUtils.getParentActivityIntent(this)
            upIntent!!.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            upIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, comp)
            upIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            NavUtils.navigateUpTo(this, upIntent)
        } else {// if it is on widget installation - update the widget if setting was changed and close the app
            val mServiceResultReceiver = ServiceResultReceiver(Handler())
            WidgetUpdateJobIntentService.enqueueWork(
                this, mServiceResultReceiver,
                ACTION_UPDATE_WEATHER_WIDGET_ONLINE, appWidgetId
            )
            // need to sent the widget id as result intent
            val resultValue = Intent()
            // make sure the widget gets the right data
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, comp)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // what happens when you press the home(back) button in the optionMenu
        if (item.itemId == android.R.id.home) {
            val upIntent = NavUtils.getParentActivityIntent(this)
            upIntent!!.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            upIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, comp)
            NavUtils.navigateUpTo(this, upIntent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // if the GPS wasn't active and you try to fetch location from the device - a dialog opens
        if (requestCode == GPS_REQUEST) {
            // if the requestCode is GPS_REQUEST - go to the fragment onActivityResult
            val fragment = supportFragmentManager
                .findFragmentById(R.id.settings_container) as MySettingsFragment?
            // go the the setting fragment onActivityResult if there was a GPS request
            fragment!!.onActivityResult(requestCode, resultCode, data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}

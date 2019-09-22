# OpenWeather

This is an only-widget weather app using OpenWeatherMap API - https://openweathermap.org/api
*****************************************************************************************************************************************
YOU WILL NEED TO PUT YOUR OWN API KEY TO USE IT!!!!!!!!
*****************************************************************************************************************************************

this is written in kotlin, and is for android devices, api level 17 and up

It has custom dialog preferences for picking the number of days for forecast and the city of forecast

You can choose a city from the list (need to type the first 2 characters) or just type the full name of city. you can also fetch the
location from the device - using the switch prererence and allowing the app the use the gps/location

-----------------------------------------------------------------------------------------------------------------------------------------
Some helpful coding tips:
1. to create a widget only app you need to go the Run -> Edit configurations - > Launcheroptions -> 
Launch: choose Nothing. also, in the manifest make sure no activity is a launcher activity.
2. To make sure the setting screen will pop up on first on screen installation you need to add an intent-filter 
to the setting activity in the manifest file, with the action: "android.appwidget.action.APPWIDGET_CONFIGURE".
In the xml file for the widget provider info you will need to add "android:configure" and the full path for the 
setting activity name. Then in the setting activity "onCreate" you add an intent to get the widget id and in the 
same activity - you will need to update the widget on change in setting, and use an intent to send out the widght id. 
3. created custom preferences based on androidx.preference.DialogPreference (look at the example project 
here: https://github.com/nilymar/TestingAndroidxPreferences) - in java
-----------------------------------------------------------------------------------------------------------------------------------------

![ezgif com-resize (7)](https://user-images.githubusercontent.com/33417968/65372427-cde47b00-dc78-11e9-82f4-5ea3f4d94bd1.gif)



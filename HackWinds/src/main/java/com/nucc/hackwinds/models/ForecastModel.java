package com.nucc.hackwinds.models;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;

import com.nucc.hackwinds.types.Condition;
import com.nucc.hackwinds.types.Forecast;
import com.nucc.hackwinds.listeners.ForecastChangedListener;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.utilities.ServiceHandler;
import com.nucc.hackwinds.views.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class ForecastModel {

    // Member variables
    private Context mContext;
    private static ForecastModel mInstance;
    private String mRawData;
    private HashMap<String, String> mLocationURLs;
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangedListener;
    private ArrayList<ForecastChangedListener> mForecastChangedListeners;
    private String mCurrentURL;

    public ArrayList<Condition> conditions;
    public ArrayList<Forecast> forecasts;

    public static ForecastModel getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ForecastModel(context);
        }
        return mInstance;
    }

    private ForecastModel(Context context) {
        // Initialize the context
        mContext = context.getApplicationContext();

        // Initialize the forecast changed listener
        mForecastChangedListeners = new ArrayList<>();

        // Set up the url map
        mLocationURLs = new HashMap<>();
        String[] locations = mContext.getResources().getStringArray(R.array.mswForecastLocations);
        String[] urls = mContext.getResources().getStringArray(R.array.mswForecastURLs);
        for (int index = 0; index < locations.length; index++) {
            mLocationURLs.put(locations[index], urls[index]);
        }

        // Initialize the location URL from the user location settings
        changeLocation();

        // Set up the settings changed listeners
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        mPrefsChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (!key.equals(SettingsActivity.FORECAST_LOCATION_KEY)) {
                    return;
                }

                if (!conditions.isEmpty() || !forecasts.isEmpty()) {
                    conditions.clear();
                    forecasts.clear();
                }

                if (!mRawData.isEmpty()) {
                    mRawData = "";
                }

                // Change the location URL
                changeLocation();

                for (ForecastChangedListener listener : mForecastChangedListeners) {
                    if (listener != null) {
                        listener.forecastLocationChanged();
                    }
                }
            }
        };

        // Register the listener
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPrefsChangedListener);

        // Initialize the list of conditions
        conditions = new ArrayList<>();
        forecasts = new ArrayList<>();
    }

    public void addForecastChangedListener(ForecastChangedListener forecastListener) {
        mForecastChangedListeners.add(forecastListener);
    }

    private void changeLocation() {
        // Get the current location value from the shared preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String location = sharedPrefs.getString(SettingsActivity.FORECAST_LOCATION_KEY, "Narragansett Town Beach");

        // Change the current location url
        mCurrentURL = mLocationURLs.get(location);
    }

    public ArrayList<Condition> getConditionsForIndex(int index) {
        if (mRawData == null) {
            loadRawData();
        } else if (mRawData.isEmpty()) {
            loadRawData();
        }

        if (conditions.isEmpty()) {
            parseForecasts();
        }

        // Return the array of conditions
        ArrayList<Condition> dayConditions = new ArrayList(conditions.subList(index * 6, 6));
        return dayConditions;
    }

    public ArrayList<Forecast> getForecasts() {
        if (mRawData == null) {
            loadRawData();
        } else if (mRawData.isEmpty()) {
            loadRawData();
        }

        if (forecasts.isEmpty()) {
            parseForecasts();
        }

        // Return the list of forecasts
        return forecasts;
    }

    private void loadRawData() {
        ServiceHandler sh = new ServiceHandler();
        mRawData = sh.makeServiceCall(mCurrentURL, ServiceHandler.GET);
    }

    private boolean parseForecasts() {
        if (mRawData.isEmpty()) {
            return false;
        }

        try {
            // Make a json array from the response string
            JSONArray jsonArr = new JSONArray(mRawData);

            // The number of data points collected
            int conditionCount = 0;
            int forecastCount = 0;

            // The number of total points iterated
            int dataIndex = 0;

            // Iterate while the number of parsed is less than what the
            // user asked for
            while ((conditionCount < 30) || (forecastCount < 10)) {

                // Get the current json object
                JSONObject jsonObj = jsonArr.getJSONObject(dataIndex);
                dataIndex++;

                // Check the date to see if it is valid
                String date = formatDate(jsonObj.getLong("localTimestamp"));
                Boolean conditionCheck = checkConditionDate(date);
                Boolean forecastCheck = checkForecastDate(date);

                if (!conditionCheck && !forecastCheck) {
                    // Its in a time range we don't care about so continue
                    continue;
                }

                // Get the swell wind and chart dictionaries
                JSONObject swell = jsonObj.getJSONObject("swell");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject chart = jsonObj.getJSONObject("charts");

                if (conditionCheck && (conditionCount < 30)) {
                    // Make a new condition object
                    Condition thisCondition = new Condition();

                    // Set the date for the conditions
                    thisCondition.Date = date;

                    // Get the relevant values from the dicts into the models
                    // Start with the breaking wave sizes
                    thisCondition.MinBreakHeight = swell.getString("minBreakingHeight");
                    thisCondition.MaxBreakHeight = swell.getString("maxBreakingHeight");

                    // Get wind information
                    thisCondition.WindSpeed = wind.getString("speed");
                    thisCondition.WindDeg = wind.getString("direction");
                    thisCondition.WindDirection = wind.getString("compassDirection");

                    // Get swell information
                    thisCondition.SwellHeight = swell.getJSONObject("components").getJSONObject("primary").getString("height");
                    thisCondition.SwellPeriod = swell.getJSONObject("components").getJSONObject("primary").getString("period");
                    thisCondition.SwellDirection = swell.getJSONObject("components").getJSONObject("primary").getString("compassDirection");

                    // Get the chart URLs
                    thisCondition.SwellChartURL = chart.getString("swell");
                    thisCondition.WindChartURL = chart.getString("wind");
                    thisCondition.PeriodChartURL = chart.getString("period");

                    // Add the new condition object to the vector and iterate the number of parsed objects
                    conditions.add(thisCondition);
                    conditionCount++;
                }

                if (forecastCheck && (forecastCount < 10)) {
                    // Make a new forecast object
                    Forecast thisForecast = new Forecast();

                    // Set the date
                    thisForecast.Date = date;

                    // Get the minimum and maximum breaking heights
                    thisForecast.MinBreakHeight = swell.getString("minBreakingHeight");
                    thisForecast.MaxBreakHeight = swell.getString("maxBreakingHeight");

                    // Get the wind speed and direction
                    thisForecast.WindSpeed = wind.getString("speed");
                    thisForecast.WindDirection = wind.getString("compassDirection");

                    // Add the new forecast object to the list
                    forecasts.add(thisForecast);
                    forecastCount++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Return a pretty timestamp for headers
    private String formatDate(Long timestamp) {
        // Parse the timestamp and turn it into a stamp that
        // looks like 12:41
        Date date = new Date(timestamp * 1000);
        DateFormat format = new SimpleDateFormat("K a");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        String formatted = format.format(date);
        if (formatted.indexOf("0") > -1) {
            format = new SimpleDateFormat("HH a");
            format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            formatted = format.format(date);
        }
        return formatted;
    }

    // Check the time. If its not in the condittion range, return false to ignore it
    private boolean checkConditionDate(String dateString) {
        int amStamp = dateString.indexOf("AM");
        int hour0 = dateString.indexOf("0");
        int hour3 = dateString.indexOf("3");
        // If its midnight or 3 am we don't care about it. otherwise its fine
        if (((amStamp > -1) && (hour0 > -1)) || ((amStamp > -1) && (hour3 > -1))) {
            return false;
        }
        return true;
    }

    // Check the time. If its not in the forecast range return false to ignore it
    private boolean checkForecastDate(String dateString) {
        int amStamp = dateString.indexOf("AM");
        int pmStamp = dateString.indexOf("PM");
        int hour0 = dateString.indexOf("0");
        int hour3 = dateString.indexOf("3");
        int hour6 = dateString.indexOf("6");
        int hour9 = dateString.indexOf("9");
        int hour12 = dateString.indexOf("12");
        // If its midnight or 3 am we don't care about it. otherwise its fine
        if (((amStamp > -1) && (hour0 > -1)) ||
                ((amStamp > -1) && (hour3 > -1)) ||
                ((amStamp > -1) && (hour6 > -1)) ||
                ((pmStamp > -1) && (hour12 > -1)) ||
                ((pmStamp > -1) && (hour6 > -1)) ||
                ((pmStamp > -1) && (hour9 > -1))) {
            return false;
        }
        return true;
    }
}

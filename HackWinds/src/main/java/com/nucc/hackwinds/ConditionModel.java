package com.nucc.hackwinds;

import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.preference.PreferenceManager;
import android.util.Log;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class ConditionModel {
    // Constants
    final private String MSW_URL =
            "http://magicseaweed.com/api/nFSL2f845QOAf1Tuv7Pf5Pd9PXa5sVTS/forecast/?spot_id=1103&fields=localTimestamp,swell.*,wind.*";
    private String mCurrentURL;

    // Member variables
    private Context mContext;
    private static ConditionModel mInstance;
    private HashMap<String, String> mLocationURLs;
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangedListener;
    private ForecastChangedListener mForecastChangedListener;
    public ArrayList<Condition> conditions;

    public static ConditionModel getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ConditionModel(context);
        }
        return mInstance;
    }

    private ConditionModel(Context context) {
        // Initialize the context
        mContext = context;

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
                if (key != SettingsActivity.FORECAST_LOCATION_KEY) {
                    return;
                }

                if (!conditions.isEmpty()) {
                    conditions.clear();
                }

                changeLocation();

                if (mForecastChangedListener != null) {
                    mForecastChangedListener.forecastLocationChanged();
                }
            }
        };

        // Register the listener
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPrefsChangedListener);

        // Initialize the list of conditions
        conditions = new ArrayList<>();
    }

    public void setForecastChangedListener(ForecastChangedListener forecastListener) {
        mForecastChangedListener = forecastListener;
    }

    private void changeLocation() {
        // Get the current location value from the shared preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String location = sharedPrefs.getString(SettingsActivity.FORECAST_LOCATION_KEY, "Narragansett Town Beach");

        // Change the current location url
        mCurrentURL = mLocationURLs.get(location);
    }

    public ArrayList<Condition> getConditions(int numberOfConditions) {
        if (conditions.isEmpty()) {
            // get the raw condition data
            ServiceHandler sh = new ServiceHandler();
            String rawData = sh.makeServiceCall(MSW_URL, ServiceHandler.GET);

            // parse the raw condition data
            parseConditionData(numberOfConditions, rawData);
        }
        // Return the array of conditions
        return conditions;
    }

    private boolean parseConditionData(int numberOfConditions, String rawData) {
        if (rawData != null) {
            try {
                // Make a json array from the response string
                JSONArray jsonArr = new JSONArray(rawData);

                // i is the number of data points parsed
                int i = 0;

                // j is the number of total points iterated
                int j = 0;

                // Iterate while the number of parsed is less than what the
                // user asked for
                while (i < numberOfConditions) {

                    // Get the current json object
                    JSONObject jsonObj = jsonArr.getJSONObject(j);
                    j++;

                    // Check the date to see if it is valid
                    String date = formatDate(jsonObj.getLong("localTimestamp"));
                    if (checkDate(date) == false) {
                        // Its in a time range we don't care about so continue
                        continue;
                    }

                    // Its false, make a new condition object
                    Condition thisCondition = new Condition();

                    // Set the date for the conditions
                    thisCondition.Date = date;

                    // Get the values from the json object to fill the condition object
                    JSONObject swell = jsonObj.getJSONObject("swell");
                    JSONObject wind = jsonObj.getJSONObject("wind");
                    thisCondition.MinBreakHeight = swell.getString("minBreakingHeight");
                    thisCondition.MaxBreakHeight = swell.getString("maxBreakingHeight");
                    thisCondition.WindSpeed = wind.getString("speed");
                    thisCondition.WindDeg = wind.getString("direction");
                    thisCondition.WindDirection = wind.getString("compassDirection");
                    thisCondition.SwellHeight = swell.getJSONObject("components").getJSONObject("primary").getString("height");
                    thisCondition.SwellPeriod = swell.getJSONObject("components").getJSONObject("primary").getString("period");
                    thisCondition.SwellDirection = swell.getJSONObject("components").getJSONObject("primary").getString("compassDirection");

                    // Add the new condition object to the vector and iterate the number of parsed objects
                    conditions.add(thisCondition);
                    i++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("HackWinds", "Couldn't get any data from the msw url");
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

    // Check the time. If its irrelevant, skip the JSon Objects
    private boolean checkDate(String dateString) {
        int ampmStamp = dateString.indexOf("AM");
        int hour00 = dateString.indexOf("0");
        int hour03 = dateString.indexOf("3");
        // If its midnight or 3 am we don't care about it. otherwise its fine
        if (((ampmStamp > -1) && (hour00 > -1)) || ((ampmStamp > -1) && (hour03 > -1))) {
            return false;
        }
        return true;
    }
}

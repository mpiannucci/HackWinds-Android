package com.nucc.hackwinds;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TideModel {
    // Set the constants for the data tags and urls
    final private String WUNDER_URL = "http://api.wunderground.com/api/2e5424aab8c91757/tide/q/RI/Point_Judith.json";

    public static final String LOW_TIDE_TAG = "Low Tide";
    public static final String HIGH_TIDE_TAG = "High Tide";
    public static final String SUNRISE_TAG = "Sunrise";
    public static final String SUNSET_TAG = "Sunset";

    // Member variables
    private static TideModel mInstance;
    public ArrayList<Tide> tides;

    public static TideModel getInstance() {
        if (mInstance == null) {
            mInstance = new TideModel();
        }
        return mInstance;
    }

    private TideModel() {
        // Initialize tide array
        tides = new ArrayList<>();
    }

    public ArrayList<Tide> getTideData() {
        // Check if there is already data read in
        if (tides.isEmpty()) {
            // Get new data from Wunderground
            ServiceHandler sh = new ServiceHandler();
            String rawData = sh.makeServiceCall(WUNDER_URL, ServiceHandler.GET);

            // Parse the tide data
            parseTideData(rawData);
        }
        // Return the vector of tides
        return tides;
    }

    private boolean parseTideData(String rawData) {
        if (rawData != null) {
            try {
                // Get the tide summary json object from the current json object
                JSONObject jsonObj = new JSONObject(rawData);
                JSONArray tideSummary = jsonObj.getJSONObject("tide").getJSONArray("tideSummary");
                int datacount = 0;
                int objectcount = 0;
                while (datacount < 6) {

                    // Get the day and time
                    JSONObject tideJSONObject = tideSummary.getJSONObject(objectcount);
                    String hour = tideJSONObject.getJSONObject("date").getString("hour");
                    String min = tideJSONObject.getJSONObject("date").getString("min");
                    String type = tideJSONObject.getJSONObject("data").getString("type");
                    String height = tideJSONObject.getJSONObject("data").getString("height");

                    // Append the data to the current tide object adn increment the data count
                    if ((type.equals(HIGH_TIDE_TAG)) ||
                        (type.equals(LOW_TIDE_TAG))  ||
                        (type.equals(SUNRISE_TAG))   ||
                        (type.equals(SUNSET_TAG))) {

                        // Create a new tide object
                        Tide thisTide = new Tide();
                        thisTide.Time = hour + ":" + min;
                        thisTide.EventType = type;
                        thisTide.Height = height;

                        // Add the tide to the vector
                        tides.add(thisTide);

                        // Increment the data count
                        datacount++;
                    } else {
                        // Do nothing cuz these values aren't relevant
                    }
                    // Increase the object count
                    objectcount++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            Log.e("HackWinds", "Couldn't get any data from the Wunderground url");
            return false;
        }
    }
}

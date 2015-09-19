package com.nucc.hackwinds.models;

import android.text.format.DateFormat;
import android.content.Context;

import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.types.Tide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TideModel {
    // Member variables
    private static TideModel mInstance;
    public ArrayList<Tide> tides;
    private Context mContext;

    public static TideModel getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TideModel(context);
        }
        return mInstance;
    }

    private TideModel(Context context) {
        // Initialize tide array
        mContext = context;
        tides = new ArrayList<>();
    }

    public boolean fetchTideData() {
        synchronized (this) {
            if (tides.isEmpty()) {
                // Parse the tide data
                return parseTideData();
            } else {
                return true;
            }
        }
    }

    public ArrayList<Tide> getTideData() {
        // Return the vector of tides
        return tides;
    }

    private boolean parseTideData() {
        final String WUNDER_URL = "http://api.wunderground.com/api/2e5424aab8c91757/tide/q/RI/Point_Judith.json";

        String rawData;
        try {
            rawData = Ion.with(mContext).load(WUNDER_URL).asString().get();
        } catch (Exception e) {
            return false;
        }

        if (rawData == null) {
            return false;
        }

        try {
            // Get the tide summary json object from the current json object
            JSONObject jsonObj = new JSONObject(rawData);
            JSONArray tideSummary = jsonObj.getJSONObject("tide").getJSONArray("tideSummary");
            int dataCount = 0;
            int objectCount = 0;
            while (dataCount < 6) {

                // Get the day and time
                JSONObject tideJSONObject = tideSummary.getJSONObject(objectCount);
                String hour = tideJSONObject.getJSONObject("date").getString("hour");
                String min = tideJSONObject.getJSONObject("date").getString("min");
                String type = tideJSONObject.getJSONObject("data").getString("type");
                String height = tideJSONObject.getJSONObject("data").getString("height");
                String ampm = "";

                // Append the data to the current tide object adn increment the data count
                if (Tide.isValidEvent(type)) {

                    // Create a new tide object
                    Tide thisTide = new Tide();

                    if (!DateFormat.is24HourFormat(mContext)) {
                        // Get the correct am or pm stamp
                        if (Integer.parseInt(hour) < 12) {
                            ampm = "am";
                        } else {
                            ampm = "pm";
                        }

                        // Convert the hour to be in 12 hour format
                        int convertedHour = Integer.parseInt(hour) % 12;
                        if (convertedHour == 0) {
                            convertedHour = 12;
                        }
                        hour = String.valueOf(convertedHour);
                    }

                    // Set all of the tidal members
                    thisTide.time = hour + ":" + min + " " + ampm;
                    thisTide.eventType = type;
                    thisTide.height = height;

                    // Add the tide to the vector
                    tides.add(thisTide);

                    // Increment the data count
                    dataCount++;
                }
                // Increase the object count
                objectCount++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

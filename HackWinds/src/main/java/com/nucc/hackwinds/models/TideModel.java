package com.nucc.hackwinds.models;

import android.text.format.DateFormat;
import android.content.Context;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.listeners.TideChangedListener;
import com.nucc.hackwinds.types.Tide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class TideModel {
    // Member variables
    public ArrayList<Tide> tides;
    public ArrayList<Tide> otherEvents;
    public int dayCount;
    private Context mContext;

    private static TideModel mInstance;
    private ArrayList<TideChangedListener> mTideChangedListeners;

    private ArrayList<String> mDayIds;
    private ArrayList<Integer> mDayDataCounts;

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
        otherEvents = new ArrayList<>();
        mTideChangedListeners = new ArrayList<>();
    }

    public void addTideChangedListener(TideChangedListener listener) {
        mTideChangedListeners.add(listener);
    }

    public void fetchTideData() {
        synchronized (this) {
            final String WUNDER_URL = "http://api.wunderground.com/api/2e5424aab8c91757/tide/q/RI/Point_Judith.json";

            if (!tides.isEmpty()) {
                for (TideChangedListener listener : mTideChangedListeners) {
                    if (listener != null) {
                        listener.tideDataUpdated();
                    }
                }
                return;
            }

            Ion.with(mContext).load(WUNDER_URL).asString().setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    if (e != null) {
                        for (TideChangedListener listener : mTideChangedListeners) {
                            if (listener != null) {
                                listener.tideDataUpdateFailed();
                            }
                        }
                    }

                    Boolean successfulParse = parseTideData(result);
                    if (successfulParse) {
                        for (TideChangedListener listener : mTideChangedListeners) {
                            if (listener != null) {
                                listener.tideDataUpdated();
                            }
                        }
                    } else {
                        for (TideChangedListener listener : mTideChangedListeners) {
                            if (listener != null) {
                                listener.tideDataUpdateFailed();
                            }
                        }
                    }
                }
            });
        }
    }

    public ArrayList<Tide> getTideData() {
        // Return the vector of tides
        return tides;
    }

    private boolean parseTideData(String rawData) {
        if (rawData == null) {
            return false;
        }

        if (!tides.isEmpty()) {
            tides.clear();
        }

        try {
            // Get the tide summary json object from the current json object
            JSONObject jsonObj = new JSONObject(rawData);
            JSONArray tideSummary = jsonObj.getJSONObject("tide").getJSONArray("tideSummary");
            int currentDayDataCount = 0;
            dayCount = 0;
            String currentDay = "";
            for (int i = 0; i < tideSummary.length(); i++) {

                // Get the day and time
                JSONObject tideJSONObject = tideSummary.getJSONObject(i);
                long epoch = tideJSONObject.getJSONObject("date").getLong("epoch");
                String day = tideJSONObject.getJSONObject("date").getString("mday");
                String type = tideJSONObject.getJSONObject("data").getString("type");
                String height = tideJSONObject.getJSONObject("data").getString("height");

                // Just a day formatter
                SimpleDateFormat dayFormatter = new SimpleDateFormat("EEEE", Locale.US);

                // Append the data to the current tide object adn increment the data count
                if (Tide.isValidEvent(type)) {

                    // Create a new tide object
                    Tide thisTide = new Tide();

                    // Set all of the tidal members
                    thisTide.timestamp = new Date(epoch * 1000L);
                    thisTide.day = dayFormatter.format(thisTide.timestamp);
                    thisTide.eventType = type;
                    if (thisTide.isTidalEvent()) {
                        thisTide.height = height;
                        thisTide.heightValue = Float.valueOf(height.split(" ")[0]);
                    }

                    if (!currentDay.equals(thisTide.day)) {
                        dayCount++;
                        currentDay = thisTide.day;

                        Tide dayTide = new Tide();
                        dayTide.day = currentDay;
                        dayTide.eventType = Tide.DAY_TAG;
                        tides.add(dayTide);
                    }

                    // Add the tide to the vector
                    tides.add(thisTide);

                    if (!thisTide.isTidalEvent()) {
                        otherEvents.add(thisTide);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

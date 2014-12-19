package com.nucc.hackwinds;


import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TideModel {
    // Set the constants for the data tags and urls
    final private String WUNDER_URL = "http://api.wunderground.com/api/2e5424aab8c91757/tide/q/RI/Point_Judith.json";
    final private String[] DAYS = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    final private String LOW_TIDE_TAG = "Low Tide";
    final private String HIGH_TIDE_TAG = "High Tide";
    final private String SUNRISE_TAG = "Sunrise";
    final private String SUNSET_TAG = "Sunset";

    // Member variables
    private static TideModel mInstance = new TideModel();
    public ArrayList<Tide> tides;

    private int today;
    private int todayMonth;
    private int todayWeek;

    public static TideModel getInstance() {
        return mInstance;
    }

    private TideModel() {
    }

    private void getDate() {
        // Get the date and set the labels correctly
        Time now = new Time();
        now.setToNow();
        today = now.monthDay;
        todayMonth = now.month;
        todayWeek = now.weekDay;

        // Set the header text to the date
        for (int i = 0; i < 5; i++) {
            tides.add(new Tide(DAYS[(now.weekDay + i) % DAYS.length]));
        }
    }

    public ArrayList<Tide> getTideData() {
        // Check if there is already data read in
        if (tides.isEmpty()) {
            // Make a new list
            tides = new ArrayList<Tide>();

            // Create the objects
            getDate();

            // Get new data from wunderground
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
                int daycount = 0;
                int datacount = 0;

                for (int k = 0; k < tideSummary.length(); k++) {

                    // Get the day and time
                    String type = tideSummary.getJSONObject(k).getJSONObject("data").getString("type");
                    String month = tideSummary.getJSONObject(k).getJSONObject("date").getString("mon");
                    String day = tideSummary.getJSONObject(k).getJSONObject("date").getString("mday");
                    String hour = tideSummary.getJSONObject(k).getJSONObject("date").getString("hour");
                    String min = tideSummary.getJSONObject(k).getJSONObject("date").getString("min");

                    // Check the date
                    if (Integer.parseInt(day) != today) {
                        // Increment the day indices
                        // Check if there was enough data, if not increment the array
                        if (datacount < 5) {
                            if (datacount < 2) {
                                for (int l = 0; l < 5; l++) {
                                    // move each day up by one
                                    tides.get(l).day = DAYS[(todayWeek + l + 1) % DAYS.length];
                                }
                            }
                        } else {
                            // Increment the day count
                            daycount++;
                        }

                        // Check if its a new month
                        if (Integer.parseInt(month) != (todayMonth + 1)) {
                            // Its a new month so reset todays index and increment the month index
                            today = 1;
                            todayMonth++;
                        } else {
                            // Its not so increment the day count
                            today++;
                        }

                        // If there are more than four days, break
                        if (daycount > 4) {
                            break;
                        }
                        // Reset the data count
                        datacount = 0;
                    }

                    // Append the data to the current tide object adn increment the data count
                    if ((type.equals(HIGH_TIDE_TAG)) || (type.equals(LOW_TIDE_TAG)) || (type.equals(SUNRISE_TAG))
                            || (type.equals(SUNSET_TAG))) {
                        tides.get(daycount).addDataItem(type, hour + ":" + min, datacount);
                        datacount++;
                    } else {
                        // Do nothing cuz these values suck
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            Log.e("HackWinds", "Couldn't get any data from the wunderground url");
            return false;
        }
    }
}

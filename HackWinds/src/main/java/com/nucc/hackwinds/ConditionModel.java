package com.nucc.hackwinds;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ConditionModel {
    // Constants
    final private String MSW_URL =
            "http://magicseaweed.com/api/nFSL2f845QOAf1Tuv7Pf5Pd9PXa5sVTS/forecast/?spot_id=1103&fields=localTimestamp,swell.*,wind.*";

    // Member variables
    private static ConditionModel ourInstance = new ConditionModel();
    public ArrayList<Condition> conditions;

    public static ConditionModel getInstance() {
        return ourInstance;
    }

    private ConditionModel() {
        // Initialize the list of conditions
        conditions = new ArrayList<Condition>();
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
                        // Its in a timerange we dont care about so continue
                        continue;
                    }

                    // Get the vlaues from the json object to fill the condition object
                    JSONObject swell = jsonObj.getJSONObject("swell");
                    JSONObject wind = jsonObj.getJSONObject("wind");
                    String minBreak = swell.getString("minBreakingHeight");
                    String maxBreak = swell.getString("maxBreakingHeight");
                    String windSpeed = wind.getString("speed");
                    String windDeg = wind.getString("direction");
                    String windDir = wind.getString("compassDirection");
                    String swellHeight = swell.getJSONObject("components").getJSONObject("primary").getString("height");
                    String swellPeriod = swell.getJSONObject("components").getJSONObject("primary").getString("period");
                    String swellDir = swell.getJSONObject("components").getJSONObject("primary").getString("compassDirection");

                    // Add the new condition object to the vector and iterate the number of parsed objects
                    conditions.add(new Condition(date, minBreak, maxBreak, windSpeed, windDeg,
                            windDir, swellHeight, swellPeriod, swellDir));
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
        // If its midnight or 3 am we dont care about it. otherwise its fine
        if (((ampmStamp > -1) && (hour00 > -1)) || ((ampmStamp > -1) && (hour03 > -1))) {
            return false;
        }
        return true;
    }
}

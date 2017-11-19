package com.nucc.hackwinds.utilities;


import com.appspot.mpitester_13.station.model.ApiApiMessagesDataMessage;
import com.appspot.mpitester_13.station.model.ApiApiMessagesSwellMessage;

import java.text.DateFormat;
import java.util.Locale;

public class Extensions {

    private static final String[] COMPASS_DIRS = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};

    public static String getTimeString(ApiApiMessagesDataMessage data) {
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
        return dateFormat.format(data.getDate());
    }

    public static String getDateString(ApiApiMessagesDataMessage data) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
        return dateFormat.format(data.getDate());
    }

    public static String getSwellSummary(ApiApiMessagesSwellMessage swell) {
        return String.format(Locale.US, "%s %2.2f ft @ %2.1f", swell.getCompassDirection(), swell.getWaveHeight(), swell.getPeriod());
    }

    public static String getDetailedSwellSummary(ApiApiMessagesSwellMessage swell) {
        return String.format(Locale.US, "%2.2f ft @ %2.1f s %.0f" + (char) 0x00B0 + " %s", swell.getWaveHeight(), swell.getPeriod(), swell.getDirection(), swell.getCompassDirection());
    }

    public static String getCompassDirection(String direction) {
        // Set the direction to its letter value on a compass
        if (direction == null) {
            return "";
        } else if (!direction.equals("MM")) {
            int windIndex = Integer.valueOf(direction) / (360 / COMPASS_DIRS.length);
            if (windIndex >= COMPASS_DIRS.length) {
                // If its past NNW, force it to be north
                windIndex = 0;
            }
            return COMPASS_DIRS[windIndex];
        } else {
            // Its a null value
            return "NULL";
        }
    }

}

package com.nucc.hackwinds.types;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Buoy {

    static final String[] COMPASS_DIRS = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};

    public Date timestamp;

    // Wave data
    public Swell waveSummary;
    public ArrayList<Swell> swellComponents;

    // Meteorological data
    public double waterTemperature;

    // Charts
    public String directionalWaveSpectraPlotURL;
    public String waveEnergySpectraPlotURL;

    public Buoy() {

    }

    public String timeString() {
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
        return dateFormat.format(timestamp);
    }

    public String dateString() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
        return dateFormat.format(timestamp);
    }

    public String getWaveSummaryStatusText() {
        return String.format("%2.1f ft @ %2.0f s %s", waveSummary.waveHeight, waveSummary.period, waveSummary.compassDirection);
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

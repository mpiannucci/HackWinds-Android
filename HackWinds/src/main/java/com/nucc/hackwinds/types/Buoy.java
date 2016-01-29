package com.nucc.hackwinds.types;


public class Buoy {

    static final String[] COMPASS_DIRS = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};

    public String time;

    // Wave Heights
    public String significantWaveHeight;
    public String swellWaveHeight;
    public String windWaveHeight;

    // Periods
    public String dominantPeriod;
    public String swellPeriod;
    public String windWavePeriod;

    // Directions
    public String meanDirection;
    public String swellDirection;
    public String windWaveDirection;

    // Wave Steepness
    public String steepness;

    public String waterTemperature;

    public Buoy() {

    }

    public void interpolateDominantPeriod() {
        if (steepness == null) {
            return;
        }

        if (steepness.equals("SWELL") || steepness.equals("AVERAGE")) {
            dominantPeriod = swellPeriod;
        } else {
            dominantPeriod = windWavePeriod;
        }
    }

    public String getWaveSummaryStatusText() {
        return String.format("%s ft @ %s s %s", significantWaveHeight, dominantPeriod, meanDirection);
    }

    public String getPrimarySwellText() {
        if (steepness.equals("SWELL") || steepness.equals("AVERAGE")) {
            return String.format("%s ft @ %s s %s", swellWaveHeight, swellPeriod, swellDirection);
        } else {
            return String.format("%s ft @ %s s %s", windWaveHeight, windWavePeriod, windWaveDirection);
        }
    }

    public String getSecondarySwellText() {
        if (steepness.equals("SWELL") || steepness.equals("AVERAGE")) {
            return String.format("%s ft @ %s s %s", windWaveHeight, windWavePeriod, windWaveDirection);
        } else {
            if (swellPeriod.equals("MM")) {
                return "";
            } else {
                return String.format("%s ft @ %s s %s", swellWaveHeight, swellPeriod, swellDirection);
            }
        }
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

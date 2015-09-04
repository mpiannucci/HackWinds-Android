package com.nucc.hackwinds.types;

public class Tide {

    public static final String LOW_TIDE_TAG = "Low Tide";
    public static final String HIGH_TIDE_TAG = "High Tide";
    public static final String SUNRISE_TAG = "Sunrise";
    public static final String SUNSET_TAG = "Sunset";

    public String time;
    public String eventType;
    public String height;

    public Tide() {
    }

    public boolean isSunrise() {
        return eventType.equals(SUNRISE_TAG);
    }

    public boolean isSunset() {
        return eventType.equals(SUNSET_TAG);
    }

    public boolean isSolarEvent() {
        return isSunrise() || isSunset();
    }

    public boolean isHighTide() {
        return eventType.equals(HIGH_TIDE_TAG);
    }

    public boolean isLowTide() {
        return eventType.equals(LOW_TIDE_TAG);
    }

    public boolean isTidalEvent() {
        return isHighTide() || isLowTide();
    }

    public static boolean isValidEvent(String event) {
        return ( event.equals(SUNRISE_TAG) ||
                 event.equals(SUNSET_TAG) ||
                 event.equals(HIGH_TIDE_TAG) ||
                 event.equals(LOW_TIDE_TAG) );
    }
}

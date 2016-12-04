package com.nucc.hackwinds.types;

import java.util.Locale;

public class Forecast {

    public String date;
    public String time;
    public double minimumBreakingHeight;
    public double maximumBreakingHeight;
    public double windSpeed;
    public double windDirection;
    public String windCompassDirection;
    public Swell primarySwellComponent;
    public Swell secondarySwellComponent;
    public Swell tertiarySwellComponent;

    public Forecast() {

    }

    public String timeForTwentyFourHourFormat() {
        int hour = Integer.valueOf(time.substring(0, 2));
        String ampm = time.substring(3,time.length());

        if (hour == 12) {
            if (ampm.equals("AM")) {
                hour = 0;
            }
        } else if (ampm.equals("PM")) {
            hour += 12;
        }

        return String.format(Locale.US, "%d:00", hour);
    }

    public String timeStringNoZero() {
        if (time.startsWith("0")) {
            return time.substring(1);
        }
        return time;
    }

    public String getConditionSummary() {
        return String.format(Locale.US, "%d - %d ft, Wind %s %d mph", (int)minimumBreakingHeight, (int)maximumBreakingHeight, windCompassDirection, (int)windSpeed);
    }
}
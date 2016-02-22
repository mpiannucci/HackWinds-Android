package com.nucc.hackwinds.types;

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
}
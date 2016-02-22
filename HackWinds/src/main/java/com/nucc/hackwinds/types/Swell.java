package com.nucc.hackwinds.types;


import java.util.Locale;

public class Swell {
    public double waveHeight;
    public double period;
    public double direction;
    public String compassDirection;

    String getSwellSummary() {
        return String.format(Locale.US, "%s %2.2f ft @ %2.1f", compassDirection, waveHeight, period);
    }
}

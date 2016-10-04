package com.nucc.hackwinds.types;


import java.util.Locale;

public class Swell {
    public double waveHeight;
    public double period;
    public double direction;
    public String compassDirection;

    public String getSwellSummary() {
        return String.format(Locale.US, "%s %2.2f ft @ %2.1f", compassDirection, waveHeight, period);
    }

    public String getDetailedSwellSummary() {
        return String.format(Locale.US, "%2.2f ft @ %2.1f s %.0f" + (char) 0x00B0 + " %s", waveHeight, period, direction, compassDirection);
    }
}

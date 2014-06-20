package com.nucc.hackwinds;

public class Condition {

    public Condition(String date, String minbreak, String maxBreak, 
        String windSpeed, String windDeg, String windDir, String swellHeight,
        String swellPeriod, String swellDeg) {
        // Call the constructor
        super();
        this.date = date;
        this.minbreak = minbreak;
        this.maxBreak = maxBreak;
        this.windSpeed = windSpeed;
        this.windDeg = windDeg;
        this.windDir = windDir;
        this.swellHeight = swellHeight;
        this.swellPeriod = swellPeriod;
        this.swellDeg = swellDeg;
    }

    public String date;
    public String minbreak;
    public String maxBreak;
    public String windSpeed;
    public String windDeg;
    public String windDir;
    public String swellHeight;
    public String swellPeriod;
    public String swellDeg;
}
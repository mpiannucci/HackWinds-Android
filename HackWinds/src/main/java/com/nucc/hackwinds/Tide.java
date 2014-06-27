package com.nucc.hackwinds;

public class Tide {

    public Tide(String day, String lowTide1, String lowTide2, String highTide1,
                String highTide2, String sunset, String sunrise) {
        // Call the constructor
        super();
        this.day = day;
        this.lowTide1 = lowTide1;
        this.lowTide2 = lowTide2;
        this.highTide1 = highTide1;
        this.highTide2 = highTide2;
        this.sunset = sunset;
        this.sunrise = sunrise;
    }

    public String day;
    public String lowTide1;
    public String lowTide2;
    public String highTide1;
    public String highTide2;
    public String sunset;
    public String sunrise;
}

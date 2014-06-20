package com.nucc.hackwinds;

public class Forecast {

    public Forecast(String day, String overview, String detail) {
        // Call the constructor
        super();
        this.day = day;
        this.overview = overview;
        this.detail = detail;
    }

    public String day;
    public String overview;
    public String detail;
}
package com.nucc.hackwinds.types;


import java.util.ArrayList;
import java.util.Locale;

public class BuoyDataContainer {

    public String buoyID;
    public Buoy buoyData;
    public int updateInterval = 60;

    public BuoyDataContainer(String id) {
        buoyID = id;
    }

    public String createLatestWaveDataURL() {
        final String BASE_DATA_URL = "https://buoyfinder.appspot.com/api/latest/wave/charts/%s";
        return String.format(Locale.US, BASE_DATA_URL, buoyID);
    }

    public String createLatestSummaryURL() {
        final String BASE_LATEST_REPORT_URL = "https://buoyfinder.appspot.com/api/latest/%s";
        return String.format(Locale.US, BASE_LATEST_REPORT_URL, buoyID);
    }
}

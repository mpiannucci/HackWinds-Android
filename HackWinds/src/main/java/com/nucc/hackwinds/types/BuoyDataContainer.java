package com.nucc.hackwinds.types;


import java.util.ArrayList;
import java.util.Locale;

public class BuoyDataContainer {
    final static public int BUOY_DATA_POINTS = 20;

    public String buoyID;
    public ArrayList<Buoy> buoyData;

    public BuoyDataContainer(String id) {
        buoyID = id;
        buoyData = new ArrayList<>(BUOY_DATA_POINTS);
    }

    public String createDetailedWaveURL() {
        final String BASE_DATA_URL = "http://www.ndbc.noaa.gov/data/realtime2/%s%s";
        final String DETAIL_URL_SUFFIX = ".spec";
        return String.format(Locale.US, BASE_DATA_URL, buoyID, DETAIL_URL_SUFFIX);
    }

    public String createLatestReportOnlyURL() {
        final String BASE_LATEST_REPORT_URL = "http://www.ndbc.noaa.gov/get_observation_as_xml.php?station=%s";
        return String.format(Locale.US, BASE_LATEST_REPORT_URL, buoyID);
    }

    public String createSpectraPlotURL() {
        final String BASE_SPECTRA_PLOT_URL = "http://www.ndbc.noaa.gov/spec_plot.php?station=%s";
        return String.format(Locale.US, BASE_SPECTRA_PLOT_URL, buoyID);
    }
}

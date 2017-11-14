package com.nucc.hackwinds.types;


import com.appspot.mpitester_13.station.model.ApiApiMessagesDataMessage;

import java.util.ArrayList;
import java.util.Locale;

public class BuoyDataContainer {

    public String buoyID;
    public ApiApiMessagesDataMessage buoyData;
    public int updateInterval = 60;

    public BuoyDataContainer(String id) {
        buoyID = id;
    }

    public String createStationInfoURL() {
        final String BASE_STATION_INFO_URL = "https://mpitester-13.appspot.com/api/station/%@/info";
        return String.format(Locale.US, BASE_STATION_INFO_URL, buoyID);
    }

    public String createLatestWaveDataURL() {
        final String BASE_DATA_URL = "https://mpitester-13.appspot.com/api/station/%s/data/latest/spectra";
        return String.format(Locale.US, BASE_DATA_URL, buoyID);
    }

    public String createLatestSummaryURL() {
        final String BASE_LATEST_REPORT_URL = "https://mpitester-13.appspot.com/api/station/%s/data/latest";
        return String.format(Locale.US, BASE_LATEST_REPORT_URL, buoyID);
    }

    public String createWavePlotURL(String plotType) {
        final String BASE_WAVE_PLOT_URL = "https://mpitester-13.appspot.com/api/station/%s/plot/%s";
        return String.format(Locale.US, BASE_WAVE_PLOT_URL, buoyID, plotType);
    }
}

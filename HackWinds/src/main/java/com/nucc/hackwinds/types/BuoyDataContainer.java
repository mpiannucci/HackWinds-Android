package com.nucc.hackwinds.types;


import com.appspot.mpitester_13.station.model.ApiApiMessagesDataMessage;

import java.util.ArrayList;
import java.util.Locale;

public class BuoyDataContainer {

    public String buoyID;
    public ApiApiMessagesDataMessage buoyData;
    public int updateInterval = 60;
    public Boolean active = false;
    public Boolean statusFetched = false;

    public BuoyDataContainer(String id) {
        buoyID = id;
    }

}

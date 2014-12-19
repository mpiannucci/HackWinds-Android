package com.nucc.hackwinds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class BuoyModel {
    // Constants
    final private String BI_URL = "http://www.ndbc.noaa.gov/data/realtime2/44097.txt";
    final private String MTK_URL = "http://www.ndbc.noaa.gov/data/realtime2/44017.txt";
    final private int DATA_POINTS = 20;
    final private int DATA_HEADER_LENGTH = 38;
    final private int DATA_LINE_LEN = 19;
    final private int HOUR_OFFSET = 3;
    final private int MINUTE_OFFSET = 4;
    final private int WVHT_OFFSET = 8;
    final private int DPD_OFFSET = 9;
    final private int DIRECTION_OFFSET = 11;

    // Public constants
    final public int BI_LOCATION = 41;
    final public int MTK_LOCATION = 42;

    // Member variables
    private static BuoyModel ourInstance = new BuoyModel();
    public ArrayList<Buoy> blockIslandBuoyData;
    public ArrayList<Buoy> montaukBuoyData;

    private double hour_offset;

    public static BuoyModel getInstance() {
        return ourInstance;
    }

    private BuoyModel() {
        // Initialize the data arrays
        blockIslandBuoyData = new ArrayList<Buoy>();
        montaukBuoyData = new ArrayList<Buoy>();

        // Set the time offset variable so the times are correct
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        hour_offset = TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        if (mTimeZone.inDaylightTime(new Date())) {
            // If its daylight savings time make fix the gmt offset
            hour_offset++;
        }
    }

    public ArrayList<Buoy> getBuoyDataForLocation(int location) {
        if (location == BI_LOCATION) {
            if (blockIslandBuoyData.isEmpty()) {
                // Get the data
                ServiceHandler sh = new ServiceHandler();
                String rawData = sh.makeServiceCall(BI_URL, ServiceHandler.GET);

                // Parse the received data
                parseBuoyData(BI_LOCATION, rawData);
            }
            return blockIslandBuoyData;
        } else {
            if (montaukBuoyData.isEmpty()) {
                // Get the data
                ServiceHandler sh = new ServiceHandler();
                String rawData = sh.makeServiceCall(MTK_URL, ServiceHandler.GET);

                // Parse the received data
                parseBuoyData(MTK_LOCATION, rawData);
            }
            return montaukBuoyData;
        }
    }

    private boolean parseBuoyData(int location, String rawData) {
        // Split by the whitespace in the string
        String[] datas = rawData.split("\\s+");

        // Loop through the data and make new buoy objects to add to the list
        for(int i=DATA_HEADER_LENGTH; i<(DATA_HEADER_LENGTH+(DATA_LINE_LEN*DATA_POINTS)); i+=DATA_LINE_LEN) {
            // Create a new buoy object
            Buoy thisBuoy = new Buoy();

            // Set the time
            thisBuoy.time = String.format("%d:%s", (Integer.valueOf(datas[i + HOUR_OFFSET])+(int)hour_offset+12)%12, datas[i + MINUTE_OFFSET]);

            // Set the period and wind direction values
            thisBuoy.dpd = datas[i+DPD_OFFSET];
            thisBuoy.dir = datas[i+DIRECTION_OFFSET];

            // Convert and set the wave height
            String wv = datas[i+WVHT_OFFSET];
            double h = Double.valueOf(wv) * 3.28;
            thisBuoy.wvht = String.format("%4.2f", h);

            // Save the buoy object to the list
            if (location == BI_LOCATION) {
                blockIslandBuoyData.add(thisBuoy);
            } else {
                montaukBuoyData.add(thisBuoy);
            }
        }
        // Return that it was successful
        return true;
    }
}

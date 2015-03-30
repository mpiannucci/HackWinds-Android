package com.nucc.hackwinds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class BuoyModel {
    // Constants
    final private String BLOCK_ISLAND_URL = "http://www.ndbc.noaa.gov/data/realtime2/44097.txt";
    final private String MONTAUK_URL = "http://www.ndbc.noaa.gov/data/realtime2/44017.txt";
    final private int DATA_POINTS = 20;
    final private int DATA_HEADER_LENGTH = 38;
    final private int DATA_LINE_LEN = 19;
    final private int HOUR_OFFSET = 3;
    final private int MINUTE_OFFSET = 4;
    final private int WVHT_OFFSET = 8;
    final private int DPD_OFFSET = 9;
    final private int DIRECTION_OFFSET = 11;
    final private int TEMPERATURE_OFFSET = 14;

    // Public constants
    final static public int BLOCK_ISLAND_LOCATION = 41;
    final static public int MONTAUK_LOCATION = 42;

    // Member variables
    private static BuoyModel mInstance;
    public ArrayList<Buoy> blockIslandBuoyData;
    public ArrayList<Buoy> montaukBuoyData;

    private double hour_offset;

    public static BuoyModel getInstance() {
        if (mInstance == null) {
            mInstance = new BuoyModel();
        }
        return mInstance;
    }

    private BuoyModel() {
        // Initialize the data arrays
        blockIslandBuoyData = new ArrayList<>();
        montaukBuoyData = new ArrayList<>();

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
        if (location == BLOCK_ISLAND_LOCATION) {
            if (blockIslandBuoyData.isEmpty()) {
                // Get the data
                ServiceHandler sh = new ServiceHandler();
                String rawData = sh.makeServiceCall(BLOCK_ISLAND_URL, ServiceHandler.GET);

                // Parse the received data
                parseBuoyData(BLOCK_ISLAND_LOCATION, rawData);
            }
            return blockIslandBuoyData;
        } else {
            if (montaukBuoyData.isEmpty()) {
                // Get the data
                ServiceHandler sh = new ServiceHandler();
                String rawData = sh.makeServiceCall(MONTAUK_URL, ServiceHandler.GET);

                // Parse the received data
                parseBuoyData(MONTAUK_LOCATION, rawData);
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
            thisBuoy.Time = String.format("%d:%s", (Integer.valueOf(datas[i + HOUR_OFFSET])+(int)hour_offset+12)%12, datas[i + MINUTE_OFFSET]);

            // Set the period and wind direction values
            thisBuoy.DominantPeriod = datas[i+DPD_OFFSET];
            thisBuoy.Direction = datas[i+DIRECTION_OFFSET];

            // Convert and set the wave height
            String wv = datas[i+WVHT_OFFSET];
            double h = Double.valueOf(wv) * 3.28;
            thisBuoy.WaveHeight = String.format("%4.2f", h);

            // Convert the water temperature to fahrenheit and set it
            String waterTemp = datas[i + TEMPERATURE_OFFSET];
            if (location == BLOCK_ISLAND_LOCATION) {
                // The montauk buoy doesn't report this so only expect it for the BI buoy
                double rawTemp = Double.valueOf(waterTemp);
                double fahrenheitTemp = Math.floor(((rawTemp * (9 / 5) + 32) / 0.05) * 0.05);
                waterTemp = String.valueOf(fahrenheitTemp);
            }
            thisBuoy.WaterTemperature = waterTemp;

            // Save the buoy object to the list
            if (location == BLOCK_ISLAND_LOCATION) {
                blockIslandBuoyData.add(thisBuoy);
            } else {
                montaukBuoyData.add(thisBuoy);
            }
        }
        // Return that it was successful
        return true;
    }
}

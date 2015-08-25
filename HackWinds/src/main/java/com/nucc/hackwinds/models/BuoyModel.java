package com.nucc.hackwinds.models;

import android.content.Context;
import android.text.format.DateFormat;

import com.nucc.hackwinds.types.Buoy;
import com.nucc.hackwinds.types.BuoyDataContainer;
import com.nucc.hackwinds.utilities.ServiceHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class BuoyModel {
    // Constants
    final private String BLOCK_ISLAND_URL = "http://www.ndbc.noaa.gov/data/realtime2/44097.txt";
    final private String MONTAUK_URL = "http://www.ndbc.noaa.gov/data/realtime2/44017.txt";
    final private String NANTUCKET_URL = "http://www.ndbc.noaa.gov/data/realtime2/44008.txt";
    final private int DATA_HEADER_LENGTH = 38;
    final private int DATA_LINE_LEN = 19;
    final private int HOUR_OFFSET = 3;
    final private int MINUTE_OFFSET = 4;
    final private int WVHT_OFFSET = 8;
    final private int DPD_OFFSET = 9;
    final private int DIRECTION_OFFSET = 11;
    final private int TEMPERATURE_OFFSET = 14;

    // Public location types
    public enum Location {
        BLOCK_ISLAND,
        MONTAUK,
        NANTUCKET
    }

    // Member variables
    private static BuoyModel mInstance;
    public HashMap<Location, BuoyDataContainer> buoyDataContainers;

    private double mTimeOffset;
    private Context mContext;

    public static BuoyModel getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BuoyModel(context);
        }
        return mInstance;
    }

    private BuoyModel(Context context) {
        // Initialize the data arrays
        mContext = context;

        // Initialize buoy containers
        initBuoyContainers();

        // Set the time offset variable so the times are correct
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        mTimeOffset = TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS);
        if (mTimeZone.inDaylightTime(new Date())) {
            // If its daylight savings time make fix the gmt offset
            mTimeOffset++;
        }
    }

    private void initBuoyContainers() {
        // Initialize the buoy dictionary
        buoyDataContainers = new HashMap<>();

        // Block Island
        BuoyDataContainer biContainer = new BuoyDataContainer();
        biContainer.url = BLOCK_ISLAND_URL;
        buoyDataContainers.put(Location.BLOCK_ISLAND, biContainer);

        // Montauk
        BuoyDataContainer mtkContainer = new BuoyDataContainer();
        mtkContainer.url = MONTAUK_URL;
        buoyDataContainers.put(Location.MONTAUK, mtkContainer);

        // Nantucket
        BuoyDataContainer ackContainer = new BuoyDataContainer();
        ackContainer.url = NANTUCKET_URL;
        buoyDataContainers.put(Location.NANTUCKET, ackContainer);
    }

    public boolean fetchBuoyDataForLocation(Location location) {
        BuoyDataContainer dataContainer = buoyDataContainers.get(location);
        if (dataContainer.buoyData.isEmpty()) {
            // Get the data
            String[] rawData = retrieveBuoyData(dataContainer.url);

            // Parse the received data
            return parseBuoyData(location, rawData);
        } else {
            return true;
        }
    }

    public ArrayList<Buoy> getBuoyDataForLocation(Location location) {
        return buoyDataContainers.get(location).buoyData;
    }

    public ArrayList<String> getWaveHeightsForLocation(Location location) {
        return buoyDataContainers.get(location).waveHeights;
    }

    private String[] retrieveBuoyData(String url) {
        ServiceHandler sh = new ServiceHandler();
        String rawData = sh.makeServiceCall(url, ServiceHandler.GET);
        return rawData.split("\\s+");
    }

    private boolean parseBuoyData(Location location, String[] rawData) {
        // Loop through the data and make new buoy objects to add to the list
        int dataCount = 0;
        while (dataCount < BuoyDataContainer.BUOY_DATA_POINTS) {
            Buoy thisBuoy = getBuoyDataForIndexOfArray(dataCount, rawData);
            dataCount++;

            // Save the buoy object to the list
            buoyDataContainers.get(location).buoyData.add(thisBuoy);
            buoyDataContainers.get(location).waveHeights.add(thisBuoy.WaveHeight);
        }
        // Return that it was successful
        return true;
    }

    Buoy getBuoyDataForIndexOfArray(int index, String[] data) {
        int baseOffset = DATA_HEADER_LENGTH + (DATA_LINE_LEN * index);
        if (baseOffset >= (DATA_HEADER_LENGTH+(DATA_LINE_LEN*BuoyDataContainer.BUOY_DATA_POINTS))) {
            return null;
        }

        // Create a new buoy object
        Buoy thisBuoy = new Buoy();

        // Get the original hor timestamp from the buoy report
        int originalHour = (int) (Integer.valueOf(data[baseOffset + HOUR_OFFSET]) + mTimeOffset);
        int convertedHour = 0;

        // Get the daylight adjusted hour for the east coast and adjust for system 24 hours
        if (DateFormat.is24HourFormat(mContext)) {
            convertedHour = (originalHour + 24) % 24;
            if (convertedHour == 0) {
                if ((originalHour + mTimeOffset) > 0) {
                    convertedHour = 12;
                }
            }
        } else {
            convertedHour = (originalHour + 12) % 12;
            if (convertedHour == 0) {
                convertedHour = 12;
            }
        }

        // Set the time member
        String min = data[baseOffset + MINUTE_OFFSET];
        thisBuoy.Time = String.format("%d:%s", convertedHour, min);

        // Set the period and wind direction values
        thisBuoy.DominantPeriod = data[baseOffset+DPD_OFFSET];
        thisBuoy.Direction = data[baseOffset+DIRECTION_OFFSET];

        // Convert and set the wave height
        String wv = data[baseOffset+WVHT_OFFSET];
        double h = Double.valueOf(wv) * 3.28;
        thisBuoy.WaveHeight = String.format("%4.2f", h);

        // Convert the water temperature to fahrenheit and set it
        String waterTemp = data[baseOffset + TEMPERATURE_OFFSET];
        double rawTemp = Double.valueOf(waterTemp);
        double fahrenheitTemp = ((rawTemp * (9.0 / 5.0) + 32.0) / 0.05) * 0.05;
        waterTemp = String.format("%4.2f", fahrenheitTemp);
        thisBuoy.WaterTemperature = waterTemp;

        return thisBuoy;
    }
}

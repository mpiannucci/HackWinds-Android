package com.nucc.hackwinds.models;

import android.content.Context;
import android.text.format.DateFormat;

import com.nucc.hackwinds.types.Buoy;
import com.nucc.hackwinds.utilities.ServiceHandler;

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

    // Public location types
    public enum Location {
        BLOCK_ISLAND,
        MONTAUK
    }

    // Member variables
    private static BuoyModel mInstance;
    public ArrayList<Buoy> blockIslandBuoyData;
    public ArrayList<Buoy> montaukBuoyData;

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
        blockIslandBuoyData = new ArrayList<>();
        montaukBuoyData = new ArrayList<>();

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

    public boolean fetchBuoyDataForLocation(Location location) {
        if (location == Location.BLOCK_ISLAND) {
            if (blockIslandBuoyData.isEmpty()) {
                // Get the data
                ServiceHandler sh = new ServiceHandler();
                String rawData = sh.makeServiceCall(BLOCK_ISLAND_URL, ServiceHandler.GET);

                // Parse the received data
                return parseBuoyData(Location.BLOCK_ISLAND, rawData);
            } else {
                return true;
            }
        } else {
            if (montaukBuoyData.isEmpty()) {
                // Get the data
                ServiceHandler sh = new ServiceHandler();
                String rawData = sh.makeServiceCall(MONTAUK_URL, ServiceHandler.GET);

                // Parse the received data
                return parseBuoyData(Location.MONTAUK, rawData);
            } else {
                return true;
            }
        }
    }

    public ArrayList<Buoy> getBuoyDataForLocation(Location location) {
        if (location == Location.BLOCK_ISLAND) {
            return blockIslandBuoyData;
        } else {
            return montaukBuoyData;
        }
    }

    private boolean parseBuoyData(Location location, String rawData) {
        // Split by the whitespace in the string
        String[] datas = rawData.split("\\s+");

        // Loop through the data and make new buoy objects to add to the list
        for(int i=DATA_HEADER_LENGTH; i<(DATA_HEADER_LENGTH+(DATA_LINE_LEN*DATA_POINTS)); i+=DATA_LINE_LEN) {
            // Create a new buoy object
            Buoy thisBuoy = new Buoy();

            // Get the original hor timestamp from the buoy report
            int originalHour = (int) (Integer.valueOf(datas[i + HOUR_OFFSET]) + mTimeOffset);
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
            String min = datas[i + MINUTE_OFFSET];
            thisBuoy.Time = String.format("%d:%s", convertedHour, min);

            // Set the period and wind direction values
            thisBuoy.DominantPeriod = datas[i+DPD_OFFSET];
            thisBuoy.Direction = datas[i+DIRECTION_OFFSET];

            // Convert and set the wave height
            String wv = datas[i+WVHT_OFFSET];
            double h = Double.valueOf(wv) * 3.28;
            thisBuoy.WaveHeight = String.format("%4.2f", h);

            // Convert the water temperature to fahrenheit and set it
            String waterTemp = datas[i + TEMPERATURE_OFFSET];
            if (location == Location.BLOCK_ISLAND) {
                // The montauk buoy doesn't report this so only expect it for the BI buoy
                double rawTemp = Double.valueOf(waterTemp);
                double fahrenheitTemp = ((rawTemp * (9.0 / 5.0) + 32.0) / 0.05) * 0.05;
                waterTemp = String.format("%4.2f", fahrenheitTemp);
            }
            thisBuoy.WaterTemperature = waterTemp;

            // Save the buoy object to the list
            if (location == Location.BLOCK_ISLAND) {
                blockIslandBuoyData.add(thisBuoy);
            } else {
                montaukBuoyData.add(thisBuoy);
            }
        }
        // Return that it was successful
        return true;
    }
}

package com.nucc.hackwinds.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.BoringLayout;
import android.text.format.DateFormat;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.listeners.BuoyChangedListener;
import com.nucc.hackwinds.types.Buoy;
import com.nucc.hackwinds.types.BuoyDataContainer;
import com.nucc.hackwinds.views.SettingsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class BuoyModel {
    // Public location types
    final public static String BLOCK_ISLAND_LOCATION = "Block Island";
    final public static String MONTAUK_LOCATION = "Montauk";
    final public static String NANTUCKET_LOCATION = "Nantucket";

    // Public data modes
    final public static String SUMMARY_DATA_MODE = "Summary";
    final public static String SWELL_DATA_MODE = "Swell";
    final public static String WIND_DATA_MODE = "Wind Wave";

    // Member variables
    private static BuoyModel mInstance;
    private BuoyDataContainer mCurrentContainer;
    private HashMap<String, BuoyDataContainer> mBuoyDataContainers;
    private ArrayList<BuoyChangedListener> mBuoyChangedListeners;
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangedListener;
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

        // Initialize the listener array
        mBuoyChangedListeners = new ArrayList<>();

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

        // Set up the settings changed listeners
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mPrefsChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged( SharedPreferences prefs, String key ) {
                if ( !key.equals( SettingsActivity.BUOY_LOCATION_KEY ) ) {
                    return;
                }

                // Update the location from settings
                changeLocation();
            }
        };

        // Register the preference change listener
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPrefsChangedListener);
    }

    public void addBuoyChangedListener(BuoyChangedListener listener) {
        mBuoyChangedListeners.add(listener);
    }

    private void initBuoyContainers() {
        final int BLOCK_ISLAND_BUOY_ID = 44097;
        final int MONTAUK_BUOY_ID = 44017;
        final int NANTUCKET_BUOY_ID = 44008;

        // Initialize the buoy dictionary
        mBuoyDataContainers = new HashMap<>();

        // Block Island
        BuoyDataContainer biContainer = new BuoyDataContainer(BLOCK_ISLAND_BUOY_ID);
        mBuoyDataContainers.put(BLOCK_ISLAND_LOCATION, biContainer);

        // Montauk
        BuoyDataContainer mtkContainer = new BuoyDataContainer(MONTAUK_BUOY_ID);
        mBuoyDataContainers.put(MONTAUK_LOCATION, mtkContainer);

        // Nantucket
        BuoyDataContainer ackContainer = new BuoyDataContainer(NANTUCKET_BUOY_ID);
        mBuoyDataContainers.put(NANTUCKET_LOCATION, ackContainer);

        // Initialize to the default location
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String location = sharedPrefs.getString(SettingsActivity.BUOY_LOCATION_KEY, BLOCK_ISLAND_LOCATION);
        mCurrentContainer = mBuoyDataContainers.get(location);
    }

    public void changeLocation() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String location = sharedPrefs.getString(SettingsActivity.BUOY_LOCATION_KEY, BLOCK_ISLAND_LOCATION);
        mCurrentContainer = mBuoyDataContainers.get(location);

        // Fetch new data
        fetchBuoyData();
    }

    public void forceChangeLocation(String location) {
        mCurrentContainer = mBuoyDataContainers.get(location);
    }

    public void fetchBuoyData() {
        synchronized (this) {
            if (!mCurrentContainer.buoyData.isEmpty()) {
                // Send an update to the listeners cuz the data is already here
                for (BuoyChangedListener listener : mBuoyChangedListeners) {
                    if (listener != null) {
                        listener.buoyDataUpdated();
                    }
                }
                return;
            }

            Ion.with(mContext).load(mCurrentContainer.createDetailedWaveURL()).asString().setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    if (e != null) {
                        // Throw message saying failure to the children listeners
                        for (BuoyChangedListener listener : mBuoyChangedListeners) {
                            if (listener != null) {
                                listener.buoyDataUpdateFailed();
                            }
                        }
                        return;
                    }

                    Boolean successfulParse = parseBuoyData(result);
                    if (successfulParse) {
                        // Tell the children that there is new data!
                        for (BuoyChangedListener listener : mBuoyChangedListeners) {
                            if (listener != null) {
                                listener.buoyDataUpdated();
                            }
                        }
                    } else {
                        // Throw message saying failure to the children listeners
                        for (BuoyChangedListener listener : mBuoyChangedListeners) {
                            if (listener != null) {
                                listener.buoyDataUpdateFailed();
                            }
                        }
                    }
                }
            });

        }
    }

    public ArrayList<Buoy> getBuoyData() {
        return mCurrentContainer.buoyData;
    }

    public String getSpectraPlotURL() {
        return mCurrentContainer.createSpectraPlotURL();
    }

    private boolean parseBuoyData(String rawData) {
        String[] data = rawData.split("\\s+");
        if (data.length == 0) {
            return false;
        }

        // Constants for parsing buoy data
        final int DATA_HEADER_LENGTH = 30;
        final int DATA_LINE_LEN = 15;
        final int HOUR_OFFSET = 3;
        final int MINUTE_OFFSET = 4;
        final int SWELL_WAVE_HEIGHT_OFFSET = 6;
        final int SWELL_PERIOD_OFFSET = 7;
        final int WIND_WAVE_HEIGHT_OFFSET = 8;
        final int WIND_WAVE_PERIOD_OFFSET = 9;
        final int SWELL_DIRECTION_OFFSET = 10;
        final int WIND_WAVE_DIRECTION_OFFSET = 11;

        // Loop through the data and make new buoy objects to add to the list
        int dataCount = 0;
        while (dataCount < BuoyDataContainer.BUOY_DATA_POINTS) {
            Buoy buoy = new Buoy();

            int baseOffset = DATA_HEADER_LENGTH + (DATA_LINE_LEN * dataCount);
            if (baseOffset >= (DATA_HEADER_LENGTH + (DATA_LINE_LEN * BuoyDataContainer.BUOY_DATA_POINTS))) {
                return false;
            }

            if (baseOffset > data.length) {
                return false;
            }

            if (buoy.time == null) {
                // Get the original hor timestamp from the buoy report
                int originalHour = (int) (Integer.valueOf(data[baseOffset + HOUR_OFFSET]) + mTimeOffset);
                int convertedHour = getCorrectedHourValue(originalHour);

                // Set the time member
                String min = data[baseOffset + MINUTE_OFFSET];
                buoy.time = String.format(Locale.US, "%d:%s", convertedHour, min);
            }

            // Wave Height
            String swellWaveHeight = data[baseOffset + SWELL_WAVE_HEIGHT_OFFSET];
            String windWaveHeight = data[baseOffset + WIND_WAVE_HEIGHT_OFFSET];
            buoy.swellWaveHeight = String.format(Locale.US, "%4.2f", convertMeterToFoot(Double.valueOf(swellWaveHeight)));
            buoy.windWaveHeight = String.format(Locale.US, "%4.2f", convertMeterToFoot(Double.valueOf(windWaveHeight)));

            // Periods
            buoy.swellPeriod = data[baseOffset + SWELL_PERIOD_OFFSET];
            buoy.windWavePeriod = data[baseOffset + WIND_WAVE_PERIOD_OFFSET];

            // Directions
            buoy.swellDirection = data[baseOffset + SWELL_DIRECTION_OFFSET];
            buoy.windWaveDirection = data[baseOffset + WIND_WAVE_DIRECTION_OFFSET];

            // Increment the buoy data count
            dataCount++;

            // Save the buoy object to the list
            mCurrentContainer.buoyData.add(buoy);
        }

        // Return that it was successful
        return true;
    }

    // TODO: Parsing the latest buoy xml
//    private Boolean parseLatestBuoyData() {
//
//    }

    private int getCorrectedHourValue(int rawHour) {
        int convertedHour;

        // Get the daylight adjusted hour for the east coast and adjust for system 24 hours
        if (DateFormat.is24HourFormat(mContext)) {
            convertedHour = (rawHour + 24) % 24;
            if (convertedHour == 0) {
                if ((rawHour + mTimeOffset) > 0) {
                    convertedHour = 12;
                }
            }
        } else {
            convertedHour = (rawHour + 12) % 12;
            if (convertedHour == 0) {
                convertedHour = 12;
            }
        }

        return convertedHour;
    }

    private double convertMeterToFoot(double meterValue) {
        return meterValue * 3.28;
    }
}

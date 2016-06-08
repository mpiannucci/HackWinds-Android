package com.nucc.hackwinds.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.listeners.BuoyChangedListener;
import com.nucc.hackwinds.listeners.LatestBuoyFetchListener;
import com.nucc.hackwinds.types.Buoy;
import com.nucc.hackwinds.types.BuoyDataContainer;
import com.nucc.hackwinds.views.SettingsActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    final public static String NEWPORT_LOCATION = "Newport";

    // Public data modes
    final public static String SUMMARY_DATA_MODE = "Summary";
    final public static String SWELL_DATA_MODE = "Swell";
    final public static String WIND_DATA_MODE = "Wind Wave";

    // Member variables
    private static BuoyModel mInstance;
    private String mCurrentLocation;
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
        final String BLOCK_ISLAND_BUOY_ID = "44097";
        final String MONTAUK_BUOY_ID = "44017";
        final String NANTUCKET_BUOY_ID = "44008";
        final String NEWPORT_BUOY_ID = "nwpr1";

        // Initialize the buoy dictionary
        mBuoyDataContainers = new HashMap<>();

        // Block Island
        BuoyDataContainer biContainer = new BuoyDataContainer(BLOCK_ISLAND_BUOY_ID);
        biContainer.updateInterval = 30;
        mBuoyDataContainers.put(BLOCK_ISLAND_LOCATION, biContainer);

        // Montauk
        BuoyDataContainer mtkContainer = new BuoyDataContainer(MONTAUK_BUOY_ID);
        mBuoyDataContainers.put(MONTAUK_LOCATION, mtkContainer);

        // Nantucket
        BuoyDataContainer ackContainer = new BuoyDataContainer(NANTUCKET_BUOY_ID);
        mBuoyDataContainers.put(NANTUCKET_LOCATION, ackContainer);

        // Newport
        BuoyDataContainer nwpContainer = new BuoyDataContainer(NEWPORT_BUOY_ID);
        mBuoyDataContainers.put(NEWPORT_LOCATION, nwpContainer);

        // Initialize to the default location
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String location = sharedPrefs.getString(SettingsActivity.BUOY_LOCATION_KEY, MONTAUK_LOCATION);
        mCurrentContainer = mBuoyDataContainers.get(location);
        mCurrentLocation = location;
    }

    public void resetData() {
        mBuoyDataContainers.clear();
        initBuoyContainers();
    }

    public void changeLocation() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String location = sharedPrefs.getString(SettingsActivity.BUOY_LOCATION_KEY, MONTAUK_LOCATION);
        mCurrentContainer = mBuoyDataContainers.get(location);
        mCurrentLocation = location;

        // Fetch new data
        fetchBuoyData();
    }

    public void forceChangeLocation(String location) {
        mCurrentContainer = mBuoyDataContainers.get(location);
        mCurrentLocation = location;

        fetchBuoyData();
    }

    public void checkForUpdate() {
        if (mCurrentContainer.buoyData.size() < 1) {
            return;
        }

        if (mCurrentContainer.buoyData.get(0).timestamp == null) {
            return;
        }

        Date now = new Date();
        long rawTimeDiff = now.getTime() - mCurrentContainer.buoyData.get(0).timestamp.getTime();
        int minuteDiff = (int)TimeUnit.MILLISECONDS.toMinutes(rawTimeDiff);

        if (mCurrentContainer.updateInterval < minuteDiff) {
            resetData();
        }
    }

    public void fetchBuoyData() {
        synchronized (this) {
            checkForUpdate();

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

    public void fetchLatestBuoyReading() {
        synchronized (this) {
            checkForUpdate();

            if (!mCurrentContainer.buoyData.isEmpty()) {
                // Send an update to the listeners cuz the data is already here
                for (BuoyChangedListener listener : mBuoyChangedListeners) {
                    if (listener != null) {
                        listener.buoyDataUpdated();
                    }
                }
                return;
            }

            Ion.with(mContext).load(mCurrentContainer.createLatestReportOnlyURL()).asString().setCallback(new FutureCallback<String>() {
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

                    Buoy latestBuoy = parseLatestBuoyData(result);
                    if (latestBuoy != null) {
                        mCurrentContainer.buoyData.get(0).dominantPeriod = latestBuoy.dominantPeriod;
                        mCurrentContainer.buoyData.get(0).waterTemperature = latestBuoy.waterTemperature;

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

    public void fetchLatestBuoyReadingForLocation(String location, final LatestBuoyFetchListener listener) {
        synchronized (this) {
            // Change the location. Get the original first to change the location back.
            final String originalLocation = mCurrentLocation;
            forceChangeLocation(location);

            checkForUpdate();

            if (!mCurrentContainer.buoyData.isEmpty()) {
                // Send an update to the listeners cuz the data is already here
                if (getBuoyData().get(0).waterTemperature != null) {
                    listener.latestBuoyFetchSuccess(getBuoyData().get(0));
                    return;
                }
            }

            Ion.with(mContext).load(mCurrentContainer.createLatestReportOnlyURL()).asString().setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    if (e != null) {
                        // Throw message saying failure to the listener
                        listener.latestBuoyFetchFailed();
                        return;
                    }

                    Buoy latestBuoy = parseLatestBuoyData(result);
                    if (latestBuoy != null) {
                        // Tell the listener we have the new buoy!
                        listener.latestBuoyFetchSuccess(latestBuoy);
                    } else {
                        // Throw message saying failure to the listener
                        listener.latestBuoyFetchFailed();
                    }

                    // Change the location back to what it was before.
                    forceChangeLocation(originalLocation);
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
        final int YEAR_OFFSET = 0;
        final int MONTH_OFFSET = 1;
        final int DAY_OFFSET = 2;
        final int HOUR_OFFSET = 3;
        final int MINUTE_OFFSET = 4;
        final int SIGNIFICANT_WAVE_HEIGHT_OFFSET = 5;
        final int SWELL_WAVE_HEIGHT_OFFSET = 6;
        final int SWELL_PERIOD_OFFSET = 7;
        final int WIND_WAVE_HEIGHT_OFFSET = 8;
        final int WIND_WAVE_PERIOD_OFFSET = 9;
        final int SWELL_DIRECTION_OFFSET = 10;
        final int WIND_WAVE_DIRECTION_OFFSET = 11;
        final int WAVE_STEEPNESS_OFFSET = 12;
        final int MEAN_WAVE_DIRECTION = 14;

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

            // Time
            String yearVal = data[baseOffset + YEAR_OFFSET];
            String monthVal = data[baseOffset + MONTH_OFFSET];
            String dayVal = data[baseOffset + DAY_OFFSET];
            String hourVal = data[baseOffset + HOUR_OFFSET];
            String minuteVal = data[baseOffset + MINUTE_OFFSET];
            String fullDateString = String.format(Locale.US, "%s-%s-%s %s:%s UTC", dayVal, monthVal, yearVal, hourVal, minuteVal);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm ZZZ");
            try {
                buoy.timestamp = dateFormatter.parse(fullDateString);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }

            // Wave Height
            String significantWaveHeight = data[baseOffset + SIGNIFICANT_WAVE_HEIGHT_OFFSET];
            String swellWaveHeight = data[baseOffset + SWELL_WAVE_HEIGHT_OFFSET];
            String windWaveHeight = data[baseOffset + WIND_WAVE_HEIGHT_OFFSET];
            buoy.significantWaveHeight = String.format(Locale.US, "%4.2f", convertMeterToFoot(Double.valueOf(significantWaveHeight)));
            buoy.swellWaveHeight = String.format(Locale.US, "%4.2f", convertMeterToFoot(Double.valueOf(swellWaveHeight)));
            buoy.windWaveHeight = String.format(Locale.US, "%4.2f", convertMeterToFoot(Double.valueOf(windWaveHeight)));

            // Periods
            buoy.swellPeriod = data[baseOffset + SWELL_PERIOD_OFFSET];
            buoy.windWavePeriod = data[baseOffset + WIND_WAVE_PERIOD_OFFSET];

            // Steepness
            buoy.steepness = data[baseOffset + WAVE_STEEPNESS_OFFSET];

            // Directions
            buoy.swellDirection = data[baseOffset + SWELL_DIRECTION_OFFSET];
            buoy.windWaveDirection = data[baseOffset + WIND_WAVE_DIRECTION_OFFSET];
            buoy.meanDirection = Buoy.getCompassDirection(data[baseOffset + MEAN_WAVE_DIRECTION]);

            // Interpolate the Dominant Period
            buoy.interpolateDominantPeriodWithDirection();

            // Increment the buoy data count
            dataCount++;

            // Save the buoy object to the list
            mCurrentContainer.buoyData.add(buoy);
        }

        // Return that it was successful
        return true;
    }

    private Buoy parseLatestBuoyData(String rawData) {
        if (rawData == null) {
            return null;
        }

        String[] rawDataLines = rawData.split("\n");
        if (rawDataLines == null) {
            return null;
        } else if (rawDataLines.length < 6) {
            return null;
        }

        // New Buoy object
        Buoy latestBuoy = new Buoy();

        // Start with the time
        String rawDateTime = rawDataLines[4];
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HHmm ZZZ MM/dd/yy");
        try {
            latestBuoy.timestamp = dateFormatter.parse(rawDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        boolean swellPeriodParsed = false;
        boolean swellDirectionParsed = false;
        for (int i = 5; i < rawDataLines.length; i++) {
            String[] components = rawDataLines[i].split(":");
            if (components == null) {
                continue;
            } else if (components.length < 2) {
                continue;
            }

            String var = components[0];
            String val = components[1];
            if (var.equals("Water Temp")) {
                latestBuoy.waterTemperature = val.split("\\s")[1];
            } else if (var.equals("Seas")) {
                latestBuoy.significantWaveHeight = val.split("\\s")[1];
            } else if (var.equals("Peak Period")) {
                latestBuoy.dominantPeriod = val.split("\\s")[1];
            } else if (var.equals("Swell")) {
                latestBuoy.swellWaveHeight = val.split("\\s")[1];
                if (latestBuoy.swellWaveHeight.equals("0.0")) {
                    swellPeriodParsed = true;
                    swellDirectionParsed = true;
                }
            } else if (var.equals("Wind Wave")) {
                latestBuoy.windWaveHeight = val.split("\\s")[1];
            } else if (var.equals("Period")) {
                String periodVal = val.split("\\s")[1];
                if (!swellPeriodParsed) {
                    latestBuoy.swellPeriod = periodVal;
                    swellPeriodParsed = true;
                } else {
                    latestBuoy.windWavePeriod = periodVal;
                }
            } else if (var.equals("Direction")) {
                if (!swellDirectionParsed) {
                    latestBuoy.swellDirection = val.replace(" ", "");
                    swellDirectionParsed = true;
                } else {
                    latestBuoy.windWaveDirection = val.replace(" ", "");
                }
            }
        }

        // Find the wave direction
        latestBuoy.interpolateMeanWaveDirection();

        return latestBuoy;
    }

    private double convertMeterToFoot(double meterValue) {
        return meterValue * 3.28;
    }
}

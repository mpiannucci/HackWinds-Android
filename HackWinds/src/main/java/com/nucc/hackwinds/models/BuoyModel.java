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
        mCurrentContainer.buoyData = null;
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
        if (mCurrentContainer.buoyData == null) {
            return;
        }

        if (mCurrentContainer.buoyData.timestamp == null) {
            return;
        }

        Date now = new Date();
        long rawTimeDiff = now.getTime() - mCurrentContainer.buoyData.timestamp.getTime();
        int minuteDiff = (int)TimeUnit.MILLISECONDS.toMinutes(rawTimeDiff);

        if (mCurrentContainer.updateInterval < minuteDiff) {
            resetData();
        }
    }

    public void fetchBuoyData() {
        synchronized (this) {
            checkForUpdate();

            if (mCurrentContainer.buoyData != null) {
                // Send an update to the listeners cuz the data is already here
                for (BuoyChangedListener listener : mBuoyChangedListeners) {
                    if (listener != null) {
                        listener.buoyDataUpdated();
                    }
                }
                return;
            }

            Ion.with(mContext).load(mCurrentContainer.createLatestWaveDataURL()).asString().setCallback(new FutureCallback<String>() {
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

                    Buoy latestBuoy = parseBuoyData(result);
                    if (latestBuoy != null) {
                        mCurrentContainer.buoyData = latestBuoy;

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

            if (mCurrentContainer.buoyData != null) {
                // Send an update to the listeners cuz the data is already here
                for (BuoyChangedListener listener : mBuoyChangedListeners) {
                    if (listener != null) {
                        listener.buoyDataUpdated();
                    }
                }
                return;
            }

            Ion.with(mContext).load(mCurrentContainer.createLatestSummaryURL()).asString().setCallback(new FutureCallback<String>() {
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

                    Buoy latestBuoy = parseBuoyData(result);
                    if (latestBuoy != null) {
                        mCurrentContainer.buoyData = latestBuoy;

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

            Ion.with(mContext).load(mCurrentContainer.createLatestSummaryURL()).asString().setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    if (e != null) {
                        // Throw message saying failure to the listener
                        listener.latestBuoyFetchFailed();
                        return;
                    }

                    Buoy latestBuoy = parseBuoyData(result);
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

    public Buoy getBuoyData() {
        return mCurrentContainer.buoyData;
    }

    private Buoy parseBuoyData(String rawData) {
        String[] data = rawData.split("\\s+");
        if (data.length == 0) {
            return null;
        }

        // TODO: Parse the JSON data
        return null;
    }

    private double convertMeterToFoot(double meterValue) {
        return meterValue * 3.28;
    }
}

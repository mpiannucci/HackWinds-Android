package com.nucc.hackwinds.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.appspot.mpitester_13.station.Station;
import com.appspot.mpitester_13.station.model.ApiApiMessagesDataMessage;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.GsonBuilder;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.listeners.BuoyChangedListener;
import com.nucc.hackwinds.listeners.LatestBuoyFetchListener;
import com.nucc.hackwinds.tasks.Credentials;
import com.nucc.hackwinds.tasks.FetchBuoyLatestDataTask;
import com.nucc.hackwinds.tasks.FetchBuoySpectraDataTask;
import com.nucc.hackwinds.types.Buoy;
import com.nucc.hackwinds.types.BuoyDataContainer;
import com.nucc.hackwinds.types.Swell;
import com.nucc.hackwinds.views.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    final public static String LONG_ISLAND_LOCATION = "Long Island";
    final public static String NEWPORT_LOCATION = "Newport";
    final public static String TEXAS_TOWER_LOCATION = "Texas Tower";

    // Member variables
    private static BuoyModel mInstance;
    private Station mStationService;
    private String mCurrentLocation;
    private BuoyDataContainer mCurrentContainer;
    private HashMap<String, BuoyDataContainer> mBuoyDataContainers;
    private ArrayList<BuoyChangedListener> mBuoyChangedListeners;
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangedListener;
    private Context mContext;
    private Boolean refreshing = false;

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

        // Create the service
        Station.Builder serviceBuilder = new Station.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),null);
        mStationService = serviceBuilder.build();

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
        final String LONG_ISLAND_BUOY_ID = "44025";
        final String NEWPORT_BUOY_ID = "nwpr1";
        final String TEXAS_TOWER_BUOY_ID = "44066";

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

        // Long Island
        BuoyDataContainer liContainer = new BuoyDataContainer(LONG_ISLAND_BUOY_ID);
        mBuoyDataContainers.put(LONG_ISLAND_LOCATION, liContainer);

        // Newport
        BuoyDataContainer nwpContainer = new BuoyDataContainer(NEWPORT_BUOY_ID);
        mBuoyDataContainers.put(NEWPORT_LOCATION, nwpContainer);

        // Texas Tower
        BuoyDataContainer ttContainer = new BuoyDataContainer(TEXAS_TOWER_BUOY_ID);
        mBuoyDataContainers.put(TEXAS_TOWER_LOCATION, ttContainer);

        // Initialize to the default location
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String location = sharedPrefs.getString(SettingsActivity.BUOY_LOCATION_KEY, BLOCK_ISLAND_LOCATION);
        mCurrentContainer = mBuoyDataContainers.get(location);
        mCurrentLocation = location;
    }

    public void resetData() {
        mCurrentContainer.buoyData = null;
    }

    public void changeLocation() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String location = sharedPrefs.getString(SettingsActivity.BUOY_LOCATION_KEY, BLOCK_ISLAND_LOCATION);
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

        if (mCurrentContainer.buoyData.getDate() == null) {
            return;
        }

        Date now = new Date();
        long rawTimeDiff = now.getTime() - mCurrentContainer.buoyData.getDate().getValue();
        int minuteDiff = (int)TimeUnit.MILLISECONDS.toMinutes(rawTimeDiff);

        if (mCurrentContainer.updateInterval < minuteDiff) {
            resetData();
        }
    }

    public Boolean fetchBuoyActive() {
        try {
           return mStationService.info(mCurrentContainer.buoyID).setKey(Credentials.BUOYFINDER_API_KEY).execute().getActive();
        } catch (Exception e) {
            return false;
        }
    }

    public void fetchNewBuoyData() {
        resetData();
        fetchBuoyData();
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

            refreshing = true;
            for (BuoyChangedListener listener : mBuoyChangedListeners) {
                if (listener != null) {
                    listener.buoyRefreshStarted();
                }
            }

            FetchBuoySpectraDataTask buoyDataTask = new FetchBuoySpectraDataTask(new FetchBuoySpectraDataTask.BuoySpectraDataTaskListener() {
                @Override
                public void onFinished(ApiApiMessagesDataMessage data) {
                    refreshing = false;
                    if (data != null) {
                        mCurrentContainer.buoyData = data;

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
            buoyDataTask.execute(mCurrentContainer.buoyID);
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

            refreshing = true;
            for (BuoyChangedListener listener : mBuoyChangedListeners) {
                if (listener != null) {
                    listener.buoyRefreshStarted();
                }
            }

            FetchBuoyLatestDataTask latestBuoyDataTask = new FetchBuoyLatestDataTask(new FetchBuoyLatestDataTask.BuoyLatestDataTaskListener() {
                @Override
                public void onFinished(ApiApiMessagesDataMessage data) {
                    refreshing = false;
                    if (data != null) {
                        mCurrentContainer.buoyData = data;

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
            latestBuoyDataTask.execute(mCurrentContainer.buoyID);
        }
    }

    public void fetchLatestBuoyReadingForLocation(String location, final LatestBuoyFetchListener listener) {
        synchronized (this) {
            // Change the location. Get the original first to change the location back.
            BuoyDataContainer buoyDataContainer = mBuoyDataContainers.get(location);
            if (buoyDataContainer == null) {
                listener.latestBuoyFetchFailed();
                return;
            }

            FetchBuoyLatestDataTask latestDataTask = new FetchBuoyLatestDataTask(new FetchBuoyLatestDataTask.BuoyLatestDataTaskListener() {
                @Override
                public void onFinished(ApiApiMessagesDataMessage data) {
                    if (data != null) {
                        // Tell the listener we have the new buoy!
                        listener.latestBuoyFetchSuccess(data);
                    } else {
                        // Throw message saying failure to the listener
                        listener.latestBuoyFetchFailed();
                    }
                }
            });
            latestDataTask.execute(mBuoyDataContainers.get(location).buoyID);
        }
    }

    public Boolean isRefreshing() {
        return refreshing;
    }

    public ApiApiMessagesDataMessage getBuoyData() {
        return mCurrentContainer.buoyData;
    }

    private double convertCelsiusToFahrenheit(double celsiusValue) {
        return (celsiusValue * 1.8) + 32.0;
    }
}

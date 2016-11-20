package com.nucc.hackwinds.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.listeners.BuoyChangedListener;
import com.nucc.hackwinds.listeners.LatestBuoyFetchListener;
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
    final public static String NEWPORT_LOCATION = "Newport";
    final public static String TEXAS_TOWER_LOCATION = "Texas Tower";

    // Member variables
    private static BuoyModel mInstance;
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
                        refreshing = false;
                        return;
                    }

                    Buoy latestBuoy = parseBuoyData(result);
                    refreshing = false;
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

            refreshing = true;
            for (BuoyChangedListener listener : mBuoyChangedListeners) {
                if (listener != null) {
                    listener.buoyRefreshStarted();
                }
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
                        refreshing = false;
                        return;
                    }

                    Buoy latestBuoy = parseBuoyData(result);
                    refreshing = false;
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
            BuoyDataContainer buoyDataContainer = mBuoyDataContainers.get(location);
            if (buoyDataContainer == null) {
                listener.latestBuoyFetchFailed();
                return;
            }

            Ion.with(mContext).load(buoyDataContainer.createLatestSummaryURL()).asString().setCallback(new FutureCallback<String>() {
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
                }
            });
        }
    }

    public Boolean isRefreshing() {
        return refreshing;
    }

    public Buoy getBuoyData() {
        return mCurrentContainer.buoyData;
    }

    private Buoy parseBuoyData(String rawData) {
        if (rawData == null) {
            return null;
        }

        Buoy buoy = new Buoy();

        try {
            // Make a json array from the response string
            JSONObject jsonObj = new JSONObject(rawData);
            JSONObject rawBuoyObject = jsonObj.getJSONObject("BuoyData");

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                buoy.timestamp = dateFormatter.parse(rawBuoyObject.getString("Date"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            // Parse the WaveSummary
            JSONObject waveSummaryObject = rawBuoyObject.getJSONObject("WaveSummary");
            String units = waveSummaryObject.getString("Units");
            Swell waveSummary = new Swell();
            waveSummary.waveHeight = waveSummaryObject.getDouble("WaveHeight");
            if (units.equals("metric")) {
                waveSummary.waveHeight = convertMeterToFoot(waveSummary.waveHeight);
            }
            waveSummary.period = waveSummaryObject.getDouble("Period");
            waveSummary.direction = waveSummaryObject.getDouble("Direction");
            waveSummary.compassDirection = waveSummaryObject.getString("CompassDirection");
            buoy.waveSummary = waveSummary;

            // Parse the Swell Components
            JSONArray swellComponentsArray = rawBuoyObject.getJSONArray("SwellComponents");
            ArrayList<Swell> swellComponents = new ArrayList<>();
            for (int i = 0; i < swellComponentsArray.length(); i++) {
                JSONObject swellCompObject = swellComponentsArray.getJSONObject(i);
                Swell swellComponent = new Swell();
                swellComponent.waveHeight = swellCompObject.getDouble("WaveHeight");
                if (units.equals("metric")) {
                    swellComponent.waveHeight = convertMeterToFoot(swellComponent.waveHeight);
                }
                swellComponent.period = swellCompObject.getDouble("Period");
                swellComponent.direction = swellCompObject.getDouble("Direction");
                swellComponent.compassDirection = swellCompObject.getString("CompassDirection");
                swellComponents.add(swellComponent);
            }
            buoy.swellComponents = swellComponents;

            // Get the temperature
            buoy.waterTemperature = rawBuoyObject.getDouble("WaterTemperature");

            // Get the charts
            if (jsonObj.has("DirectionalSpectraPlot")) {
                buoy.directionalWaveSpectraPlotURL = jsonObj.getString("DirectionalSpectraPlot");
            }

            if (jsonObj.has("SpectraDistributionPlot")) {
                buoy.waveEnergySpectraPlotURL = jsonObj.getString("SpectraDistributionPlot");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return buoy;
    }

    private double convertMeterToFoot(double meterValue) {
        return meterValue * 3.28;
    }
}

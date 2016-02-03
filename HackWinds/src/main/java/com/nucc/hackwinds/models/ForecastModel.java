package com.nucc.hackwinds.models;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.Context;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.types.Condition;
import com.nucc.hackwinds.types.Forecast;
import com.nucc.hackwinds.listeners.ForecastChangedListener;
import com.nucc.hackwinds.types.ForecastDataContainer;
import com.nucc.hackwinds.views.SettingsActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class ForecastModel {
    final public static String TOWN_BEACH_LOCATION = "Narragansett Town Beach";
    final public static String POINT_JUDITH_LOCATION = "Point Judith Lighthouse";
    final public static String MATUNUCK_LOCATION = "Matunuck";
    final public static String SECOND_BEACH_LOCATION = "Second Beach";

    // Member variables
    private Context mContext;
    private static ForecastModel mInstance;
    private ArrayList<ForecastChangedListener> mForecastChangedListeners;
    private SharedPreferences.OnSharedPreferenceChangeListener mPrefsChangedListener;

    private HashMap<String, ForecastDataContainer> mForecastDataContainers;
    private ForecastDataContainer mCurrentContainer;

    public static ForecastModel getInstance( Context context ) {
        if ( mInstance == null ) {
            mInstance = new ForecastModel( context );
        }
        return mInstance;
    }

    private ForecastModel( Context context ) {
        // Initialize the context
        mContext = context.getApplicationContext();

        // Initialize the forecast changed listener
        mForecastChangedListeners = new ArrayList<>();

        // Set up the data containers
        initForecastContainers();

        // Set up the settings changed listeners
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( mContext );
        mPrefsChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged( SharedPreferences prefs, String key ) {
                if ( !key.equals( SettingsActivity.FORECAST_LOCATION_KEY ) ) {
                    return;
                }

                // Change the location URL
                changeLocation();
            }
        };

        // Register the listener
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPrefsChangedListener);
    }

    public void addForecastChangedListener( ForecastChangedListener forecastListener ) {
        mForecastChangedListeners.add(forecastListener);
    }

    private void initForecastContainers() {
        final int TOWN_BEACH_ID = 1103;
        final int POINT_JUDITH_ID = 376;
        final int MATUNUCK_ID = 377;
        final int SECOND_BEACH_ID = 846;

        mForecastDataContainers = new HashMap<>();

        // Town Beach
        ForecastDataContainer townBeachData = new ForecastDataContainer(TOWN_BEACH_ID);
        mForecastDataContainers.put(TOWN_BEACH_LOCATION, townBeachData);

        // Point Judith
        ForecastDataContainer pointJudithData = new ForecastDataContainer(POINT_JUDITH_ID);
        mForecastDataContainers.put(POINT_JUDITH_LOCATION, pointJudithData);

        // Matunuck
        ForecastDataContainer matunuckData = new ForecastDataContainer(MATUNUCK_ID);
        mForecastDataContainers.put(MATUNUCK_LOCATION, matunuckData);

        // Second Beach
        ForecastDataContainer secondBeachData = new ForecastDataContainer(SECOND_BEACH_ID);
        mForecastDataContainers.put(SECOND_BEACH_LOCATION, secondBeachData);

        // Initialize the current container from the settings
        changeLocation();
    }

    private void changeLocation() {
        // Get the current location value from the shared preferences
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( mContext );
        String location = sharedPrefs.getString( SettingsActivity.FORECAST_LOCATION_KEY, "Narragansett Town Beach" );

        // Change the current location url
        mCurrentContainer = mForecastDataContainers.get(location);

        // Update the data for the location
        fetchForecastData();
    }

    public void fetchForecastData() {
        synchronized (this) {
            if (!mCurrentContainer.conditions.isEmpty()) {
                for(ForecastChangedListener listener : mForecastChangedListeners) {
                    if (listener != null) {
                        listener.forecastDataUpdated();
                    }
                }
                return;
            }

            // Make the data URL
            final String BASE_DATA_URL = "http://magicseaweed.com/api/nFSL2f845QOAf1Tuv7Pf5Pd9PXa5sVTS/forecast/?spot_id=%d&fields=localTimestamp,swell.*,wind.*,charts.*";
            String dataURL = String.format(Locale.US, BASE_DATA_URL, mCurrentContainer.forecastID);
            Ion.with(mContext).load(dataURL).asString().setCallback(new FutureCallback<String>() {
                @Override
                public void onCompleted(Exception e, String result) {
                    if (e != null) {
                        for(ForecastChangedListener listener : mForecastChangedListeners) {
                            if (listener != null) {
                                listener.forecastDataUpdateFailed();
                            }
                        }
                        return;
                    }

                    Boolean successfulParse = parseForecasts(result);
                    if (successfulParse) {
                        for(ForecastChangedListener listener : mForecastChangedListeners) {
                            if (listener != null) {
                                listener.forecastDataUpdated();
                            }
                        }
                    } else {
                        for(ForecastChangedListener listener : mForecastChangedListeners) {
                            if (listener != null) {
                                listener.forecastDataUpdateFailed();
                            }
                        }
                    }
                }
            });
        }
    }

    public ArrayList<Condition> getConditionsForIndex( int index ) {
        // Return the array of conditions
        if (mCurrentContainer.conditions.size() == ForecastDataContainer.CONDITION_DATA_COUNT) {
            return new ArrayList<>(mCurrentContainer.conditions.subList(index * 6, (index * 6) + 6));
        } else {
            return null;
        }
    }

    public ArrayList<Forecast> getForecasts() {
        // Return the list of forecasts
        return mCurrentContainer.forecasts;
    }

    private boolean parseForecasts(String rawData) {
        // Get the raw data
        if (rawData == null) {
            return false;
        }

        try {
            // Make a json array from the response string
            JSONArray jsonArr = new JSONArray( rawData );

            // The number of data points collected
            int conditionCount = 0;
            int forecastCount = 0;

            // The number of total points iterated
            int dataIndex = 0;

            // Iterate while the number of parsed is less than what the
            // user asked for
            while ( ( ( conditionCount < ForecastDataContainer.CONDITION_DATA_COUNT ) ||
                    ( forecastCount < ForecastDataContainer.FORECAST_DATA_COUNT ) ) &&
                    ( dataIndex < jsonArr.length() ) ) {

                // Get the current json object
                JSONObject jsonObj = jsonArr.getJSONObject( dataIndex );
                dataIndex++;

                // Check the date to see if it is valid
                String date = formatDate( jsonObj.getLong( "localTimestamp" ) );
                Boolean conditionCheck = checkConditionDate( date );
                Boolean forecastCheck = checkForecastDate( date );

                if ( !conditionCheck && !forecastCheck ) {
                    // Its in a time range we don't care about so continue
                    continue;
                }

                // Get the swell wind and chart dictionaries
                JSONObject swell = jsonObj.getJSONObject( "swell" );
                JSONObject wind = jsonObj.getJSONObject( "wind" );
                JSONObject chart = jsonObj.getJSONObject( "charts" );

                if ( conditionCheck && ( conditionCount < ForecastDataContainer.CONDITION_DATA_COUNT ) ) {
                    // Make a new condition object
                    Condition thisCondition = new Condition();

                    // Set the date for the conditions
                    thisCondition.date = date;

                    // Get the relevant values from the dicts into the models
                    // Start with the breaking wave sizes
                    thisCondition.minBreakHeight = swell.getString( "minBreakingHeight" );
                    thisCondition.maxBreakHeight = swell.getString( "maxBreakingHeight" );

                    // Get wind information
                    thisCondition.windSpeed = wind.getString( "speed" );
                    thisCondition.windDeg = wind.getString( "direction" );
                    thisCondition.windDirection = wind.getString( "compassDirection" );

                    // Get swell information
                    thisCondition.swellHeight = swell.getJSONObject( "components" ).getJSONObject( "primary" ).getString( "height" );
                    thisCondition.swellPeriod = swell.getJSONObject( "components" ).getJSONObject( "primary" ).getString( "period" );
                    thisCondition.swellDirection =
                        swell.getJSONObject( "components" ).getJSONObject( "primary" ).getString( "compassDirection" );

                    // Get the chart URLs
                    thisCondition.swellChartURL = chart.getString( "swell" );
                    thisCondition.windChartURL = chart.getString( "wind" );
                    thisCondition.periodChartURL = chart.getString( "period" );

                    // Add the new condition object to the vector and iterate the number of parsed objects
                    mCurrentContainer.conditions.add(thisCondition);
                    conditionCount++;
                }

                if ( forecastCheck && ( forecastCount < ForecastDataContainer.FORECAST_DATA_COUNT ) ) {
                    // Make a new forecast object
                    Forecast thisForecast = new Forecast();

                    // Set the date
                    thisForecast.date = date;

                    // Get the minimum and maximum breaking heights
                    thisForecast.minBreakHeight = swell.getString( "minBreakingHeight" );
                    thisForecast.maxBreakHeight = swell.getString( "maxBreakingHeight" );

                    // Get the wind speed and direction
                    thisForecast.windSpeed = wind.getString( "speed" );
                    thisForecast.windDirection = wind.getString( "compassDirection" );

                    // Add the new forecast object to the list
                    mCurrentContainer.forecasts.add(thisForecast);
                    forecastCount++;
                }
            }
        } catch ( JSONException e ) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Return a pretty timestamp for headers
    private String formatDate( Long timestamp ) {
        // Parse the timestamp and turn it into a stamp that
        // looks like 12:41
        Date date = new Date( timestamp * 1000 );
        DateFormat format = new SimpleDateFormat( "K a" );
        format.setTimeZone( TimeZone.getTimeZone( "Etc/UTC" ) );
        String formatted = format.format( date );
        if ( formatted.indexOf( "0" ) > -1 ) {
            format = new SimpleDateFormat( "HH a" );
            format.setTimeZone( TimeZone.getTimeZone( "Etc/UTC" ) );
            formatted = format.format( date );
        }

        // Format the time for 24 hour clocks
        if ( android.text.format.DateFormat.is24HourFormat( mContext ) ) {
            int hour = 0;
            int pmIndex = formatted.indexOf( "PM" );
            int amIndex = formatted.indexOf( "AM" );
            if ( pmIndex > -1 ) {
                hour = Integer.parseInt( formatted.substring( 0, pmIndex - 1 ) );
                if ( hour != 12 ) {
                    hour += 12;
                }
            } else {
                hour = Integer.parseInt( formatted.substring( 0, amIndex - 1 ) );
                if ( hour == 12 ) {
                    hour = 0;
                }
            }
            formatted = String.valueOf( hour ) + ":00";
        }
        return formatted;
    }

    // Check the time. If its not in the condition range, return false to ignore it
    private boolean checkConditionDate( String dateString ) {
        int amStamp = dateString.indexOf( "AM" );
        int hour0 = dateString.indexOf( "0" );
        int hour00 = dateString.indexOf( "00:00" );
        int hour3 = dateString.indexOf( "3" );

        if ( android.text.format.DateFormat.is24HourFormat( mContext ) ) {
            // If its midnight or 3 am we don't care about it. otherwise its fine
            if ( ( hour00 > -1 ) || ( hour3 > -1 ) ) {
                return false;
            }
        } else {
            // If its midnight or 3 am we don't care about it. otherwise its fine
            if ( ( ( amStamp > -1 ) && ( hour0 > -1 ) ) || ( ( amStamp > -1 ) && ( hour3 > -1 ) ) ) {
                return false;
            }
        }
        return true;
    }

    // Check the time. If its not in the forecast range return false to ignore it
    private boolean checkForecastDate( String dateString ) {
        if ( android.text.format.DateFormat.is24HourFormat( mContext ) ) {
            int hour3 = dateString.indexOf( "15" );
            int hour9 = dateString.indexOf( "9" );
            if ( ( hour9 > -1 ) || ( hour3 > -1 ) ) {
                return true;
            }
        } else {
            int amStamp = dateString.indexOf( "AM" );
            int pmStamp = dateString.indexOf( "PM" );
            int hour3 = dateString.indexOf( "3" );
            int hour9 = dateString.indexOf( "9" );

            // We only care about 9 am and 3 pm
            if ( ( ( amStamp > -1 ) && ( hour9 > -1 ) ) || ( ( pmStamp > -1 ) && ( hour3 > -1 ) ) ) {
                return true;
            }
        }
        return false;
    }
}

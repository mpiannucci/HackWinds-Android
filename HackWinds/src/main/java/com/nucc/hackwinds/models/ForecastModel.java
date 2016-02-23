package com.nucc.hackwinds.models;

import android.content.Context;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.types.Forecast;
import com.nucc.hackwinds.listeners.ForecastChangedListener;
import com.nucc.hackwinds.types.ForecastDailySummary;
import com.nucc.hackwinds.types.Swell;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ForecastModel {

    // Public Member Variables
    public String locationName;
    public String waveModelName;
    public String waveModelRun;
    public String windModelName;
    public String windModelRun;
    public ArrayList<Forecast> forecasts;
    public ArrayList<ForecastDailySummary> dailyForecasts;
    public final int FORECAST_DATA_COUNT = 60;

    // Private Member variables
    private Context mContext;
    private static ForecastModel mInstance;
    private ArrayList<ForecastChangedListener> mForecastChangedListeners;
    private int dayCount;
    private int dayIndices[];

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

        // Initialize the data arrays
        forecasts = new ArrayList<>();
        dailyForecasts = new ArrayList<>();

        // Set up the day indices array indicating its empty
        dayIndices = new int[8];
        for (int i = 0; i < 8; i++) {
            dayIndices[i] = -1;
        }
        dayCount = 0;

        fetchForecastData();
    }

    public void addForecastChangedListener( ForecastChangedListener forecastListener ) {
        mForecastChangedListeners.add(forecastListener);
    }

    public void fetchForecastData() {
        synchronized (this) {
            if (!forecasts.isEmpty()) {
                for(ForecastChangedListener listener : mForecastChangedListeners) {
                    if (listener != null) {
                        listener.forecastDataUpdated();
                    }
                }
                return;
            }

            // Make the data URL
            final String dataURL = "https://rhodycast.appspot.com/forecast_as_json";
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

                        // Parse out the forecasts for the summaries
                        createDailyForecasts();

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

    public ArrayList<Forecast> getForecastsForDay( int day ) {
        // Return the array of conditions
        if (forecasts.size() == 0) {
            return null;
        }

        int startIndex = 0;
        int endIndex = 0;

        if (day < 8) {
            startIndex = dayIndices[day];
        }

        if (startIndex == -1) {
            return null;
        }

        if (day < 7) {
            endIndex = dayIndices[day+1];
            if (endIndex < 0) {
                endIndex = forecasts.size();
            }
        } else {
            endIndex = forecasts.size();
        }

        return new ArrayList<>(forecasts.subList(startIndex, endIndex));
    }

    public int getDayForecastStartingIndex(int day) {
        if (day < 8) {
            return dayIndices[day];
        } else {
            return 0;
        }
    }

    public int getDayCount() {
        return dayCount;
    }

    private boolean parseForecasts(String rawData) {
        // Get the raw data
        if (rawData == null) {
            return false;
        }

        if (!forecasts.isEmpty()) {
            forecasts.clear();
        }

        try {
            // Make a json array from the response string
            JSONObject jsonObj = new JSONObject( rawData );

            locationName = jsonObj.getString("LocationName");
            waveModelName = jsonObj.getJSONObject("WaveModel").getString("Description");
            waveModelRun = jsonObj.getJSONObject("WaveModel").getString("ModelRun");
            windModelName = jsonObj.getJSONObject("WindModel").getString("Description");
            windModelRun = jsonObj.getJSONObject("WindModel").getString("ModelRun");

            // Get alllllll of the forecast data!
            JSONArray forecastJsonAray = jsonObj.getJSONArray("ForecastData");
            dayCount = 0;
            int forecastOffset = 0;
            for (int i = 0; i < FORECAST_DATA_COUNT; i++) {
                Forecast newForecast = new Forecast();

                // Grab the next forecast object from the raw array
                JSONObject rawForecast = forecastJsonAray.getJSONObject(i);

                newForecast.date = rawForecast.getString("Date");
                newForecast.time = rawForecast.getString("Time");
                if ((i == 0) && (newForecast.time.equals("07 PM") || (newForecast.time.equals("08 PM")))) {
                    i += 1;
                    forecastOffset = 2;
                    continue;
                }

                newForecast.minimumBreakingHeight = rawForecast.getDouble("MinimumBreakingHeight");
                newForecast.maximumBreakingHeight = rawForecast.getDouble("MaximumBreakingHeight");
                newForecast.windSpeed = rawForecast.getDouble("WindSpeed");
                newForecast.windDirection = rawForecast.getDouble("WindDirection");
                newForecast.windCompassDirection = rawForecast.getString("WindCompassDirection");

                Swell primarySwell = new Swell();
                primarySwell.waveHeight = rawForecast.getJSONObject("PrimarySwellComponent").getDouble("WaveHeight");
                primarySwell.period = rawForecast.getJSONObject("PrimarySwellComponent").getDouble("Period");
                primarySwell.direction = rawForecast.getJSONObject("PrimarySwellComponent").getDouble("Direction");
                primarySwell.compassDirection = rawForecast.getJSONObject("PrimarySwellComponent").getString("CompassDirection");
                newForecast.primarySwellComponent = primarySwell;

                Swell secondarySwell = new Swell();
                secondarySwell.waveHeight = rawForecast.getJSONObject("SecondarySwellComponent").getDouble("WaveHeight");
                secondarySwell.period = rawForecast.getJSONObject("SecondarySwellComponent").getDouble("Period");
                secondarySwell.direction = rawForecast.getJSONObject("SecondarySwellComponent").getDouble("Direction");
                secondarySwell.compassDirection = rawForecast.getJSONObject("SecondarySwellComponent").getString("CompassDirection");
                newForecast.secondarySwellComponent = secondarySwell;

                Swell tertiarySwell = new Swell();
                tertiarySwell.waveHeight = rawForecast.getJSONObject("TertiarySwellComponent").getDouble("WaveHeight");
                tertiarySwell.period = rawForecast.getJSONObject("TertiarySwellComponent").getDouble("Period");
                tertiarySwell.direction = rawForecast.getJSONObject("TertiarySwellComponent").getDouble("Direction");
                tertiarySwell.compassDirection = rawForecast.getJSONObject("TertiarySwellComponent").getString("CompassDirection");
                newForecast.tertiarySwellComponent = tertiarySwell;

                if (newForecast.time.equals("01 AM") || newForecast.time.equals("02 AM")) {
                    dayIndices[dayCount] = i - forecastOffset;
                    dayCount++;
                } else if (forecasts.size() == 0) {
                    dayIndices[dayCount] = i - forecastOffset;
                    dayCount++;
                }

                forecasts.add(newForecast);
            }
        } catch ( JSONException e ) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void createDailyForecasts() {
        if (dailyForecasts.size() > 0) {
            dailyForecasts.clear();
        }

        for (int i = 0; i < dayCount; i++) {
            ForecastDailySummary newSummary = new ForecastDailySummary();

            ArrayList<Forecast> dailyForecastData = getForecastsForDay(i);

            if (dailyForecastData.size() < 8) {
                newSummary.morningMinimumWaveHeight = 0;
                newSummary.morningMaximumWaveHeight = 0;
                newSummary.morningWindSpeed = 0;
                newSummary.morningWindCompassDirection = "";
                newSummary.afternoonMinimumWaveHeight = 0;
                newSummary.afternoonMaximumWaveHeight = 0;
                newSummary.afternoonWindSpeed = 0;
                newSummary.afternoonWindCompassDirection = "";

                if (dailyForecasts.size() == 0) {
                    if (dailyForecastData.size() >= 6) {
                        newSummary.morningMinimumWaveHeight = (dailyForecastData.get(0).minimumBreakingHeight + dailyForecastData.get(1).minimumBreakingHeight) / 2;
                        newSummary.morningMaximumWaveHeight = (dailyForecastData.get(0).maximumBreakingHeight + dailyForecastData.get(1).maximumBreakingHeight) / 2;
                        newSummary.morningWindSpeed = dailyForecastData.get(1).windSpeed;
                        newSummary.morningWindCompassDirection = dailyForecastData.get(1).windCompassDirection;

                        newSummary.afternoonMinimumWaveHeight = (dailyForecastData.get(2).minimumBreakingHeight + dailyForecastData.get(3).minimumBreakingHeight) / 2;
                        newSummary.afternoonMaximumWaveHeight = (dailyForecastData.get(2).maximumBreakingHeight + dailyForecastData.get(3).maximumBreakingHeight) / 2;
                        newSummary.afternoonWindSpeed = dailyForecastData.get(3).windSpeed;
                        newSummary.afternoonWindCompassDirection = dailyForecastData.get(3).windCompassDirection;

                    } else if (dailyForecastData.size() >= 4) {
                        newSummary.afternoonMinimumWaveHeight = (dailyForecastData.get(1).minimumBreakingHeight + dailyForecastData.get(2).minimumBreakingHeight + dailyForecastData.get(3).minimumBreakingHeight) / 3;
                        newSummary.afternoonMaximumWaveHeight = (dailyForecastData.get(1).maximumBreakingHeight + dailyForecastData.get(3).maximumBreakingHeight + dailyForecastData.get(3).maximumBreakingHeight) / 3;
                        newSummary.afternoonWindSpeed = dailyForecastData.get(2).windSpeed;
                        newSummary.afternoonWindCompassDirection = dailyForecastData.get(2).windCompassDirection;
                    } else if (dailyForecastData.size() >= 2) {
                        newSummary.morningMinimumWaveHeight = dailyForecastData.get(1).minimumBreakingHeight;
                        newSummary.morningMaximumWaveHeight = dailyForecastData.get(1).maximumBreakingHeight;
                        newSummary.morningWindSpeed = dailyForecastData.get(1).windSpeed;
                        newSummary.morningWindCompassDirection = dailyForecastData.get(1).windCompassDirection;
                    }
                } else {
                    if (dailyForecastData.size() >= 4) {

                        newSummary.morningMinimumWaveHeight = (dailyForecastData.get(1).minimumBreakingHeight + dailyForecastData.get(2).minimumBreakingHeight + dailyForecastData.get(3).minimumBreakingHeight) / 3;
                        newSummary.morningMaximumWaveHeight = (dailyForecastData.get(1).maximumBreakingHeight + dailyForecastData.get(3).maximumBreakingHeight + dailyForecastData.get(3).maximumBreakingHeight) / 3;
                        newSummary.morningWindSpeed = dailyForecastData.get(2).windSpeed;
                        newSummary.morningWindCompassDirection = dailyForecastData.get(2).windCompassDirection;

                        if (dailyForecastData.size() >= 6) {
                            newSummary.afternoonMinimumWaveHeight = (dailyForecastData.get(4).minimumBreakingHeight + dailyForecastData.get(5).minimumBreakingHeight) / 2;
                            newSummary.afternoonMaximumWaveHeight = (dailyForecastData.get(4).maximumBreakingHeight + dailyForecastData.get(5).maximumBreakingHeight) / 2;
                            newSummary.afternoonWindSpeed = dailyForecastData.get(5).windSpeed;
                            newSummary.afternoonWindCompassDirection = dailyForecastData.get(5).windCompassDirection;
                        }
                    }
                }
            } else {
                newSummary.morningMinimumWaveHeight = (dailyForecastData.get(1).minimumBreakingHeight + dailyForecastData.get(2).minimumBreakingHeight + dailyForecastData.get(3).minimumBreakingHeight) / 3;
                newSummary.morningMaximumWaveHeight = (dailyForecastData.get(1).maximumBreakingHeight + dailyForecastData.get(3).maximumBreakingHeight + dailyForecastData.get(3).maximumBreakingHeight) / 3;
                newSummary.morningWindSpeed = dailyForecastData.get(2).windSpeed;
                newSummary.morningWindCompassDirection = dailyForecastData.get(2).windCompassDirection;

                newSummary.afternoonMinimumWaveHeight = (dailyForecastData.get(4).minimumBreakingHeight + dailyForecastData.get(5).minimumBreakingHeight + dailyForecastData.get(6).minimumBreakingHeight) / 3;
                newSummary.afternoonMaximumWaveHeight = (dailyForecastData.get(4).maximumBreakingHeight + dailyForecastData.get(5).maximumBreakingHeight + dailyForecastData.get(6).maximumBreakingHeight) / 3;
                newSummary.afternoonWindSpeed = dailyForecastData.get(5).windSpeed;
                newSummary.afternoonWindCompassDirection = dailyForecastData.get(5).windCompassDirection;
            }

            dailyForecasts.add(newSummary);
        }
    }
}

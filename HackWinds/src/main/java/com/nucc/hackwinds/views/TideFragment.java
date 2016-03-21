package com.nucc.hackwinds.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.TextView;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.listeners.LatestBuoyFetchListener;
import com.nucc.hackwinds.listeners.TideChangedListener;
import com.nucc.hackwinds.types.Buoy;
import com.nucc.hackwinds.types.Tide;
import com.nucc.hackwinds.models.BuoyModel;
import com.nucc.hackwinds.models.TideModel;
import com.nucc.hackwinds.utilities.ReachabilityHelper;


public class TideFragment extends Fragment implements TideChangedListener, LatestBuoyFetchListener {
    private TideModel mTideModel;
    private String mDefaultBuoyLocation;
    private String mWaterTemp;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferencesListener;

    final private int[] mTideTypeViews = new int[]{R.id.upcomingTideType1,
                                                   R.id.upcomingTideType2,
                                                   R.id.upcomingTideType3,
                                                   R.id.upcomingTideType4};
    final private int[] mTideTimeViews = new int[]{R.id.upcomingTideTime1,
                                                   R.id.upcomingTideTime2,
                                                   R.id.upcomingTideTime3,
                                                   R.id.upcomingTideTime4};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the menu options
        setHasOptionsMenu(true);

        mTideModel = TideModel.getInstance(getActivity());
        mTideModel.addTideChangedListener(this);

        reloadWaterTemperature();

        mSharedPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals(SettingsActivity.DEFAULT_BUOY_LOCATION_KEY)) {
                        reloadWaterTemperature();
                    }
                }
            };

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        sharedPrefs.registerOnSharedPreferenceChangeListener(mSharedPreferencesListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.tide_fragment, container, false);

        return V;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.tide_menu_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tide_schedule:
                startActivity(new Intent(getActivity(), BuoyHistoryActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        tideDataUpdated();
        latestBuoyFetchSuccess(null);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void tideDataUpdated() {
        if (mTideModel.tides.isEmpty()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int tideCount = 0;

                // Set the upcoming and sunrise/sunset values
                for (Tide thisTide : mTideModel.tides ) {
                    if (thisTide.isSunrise()) {
                        // Set the sunrise time that was found
                        TextView sunriseTime = (TextView)getActivity().findViewById(R.id.sunrise_time);
                        sunriseTime.setText(thisTide.time);
                    } else if (thisTide.isSunset()) {
                        // Set the sunset time that was read
                        TextView sunsetTime = (TextView)getActivity().findViewById(R.id.sunset_time);
                        sunsetTime.setText(thisTide.time);
                    } else if (thisTide.isTidalEvent()) {
                        // Yay we found a tide, now set the type (high or low) and the values
                        TextView typeView = (TextView)getActivity().findViewById(mTideTypeViews[tideCount]);
                        TextView timeView = (TextView)getActivity().findViewById(mTideTimeViews[tideCount]);
                        typeView.setText(thisTide.eventType);
                        timeView.setText(thisTide.height + " at " + thisTide.time);

                        // Also set the current status
                        if (tideCount == 0) {
                            TextView currentTextView = (TextView) getActivity().findViewById(R.id.currentTideStatus);
                            if (thisTide.isHighTide()) {
                                // Tide is incoming
                                currentTextView.setText("Incoming");
                                currentTextView.setTextColor(Color.parseColor("#009933"));
                            } else {
                                // Assume low time so outgoing
                                currentTextView.setText("Outgoing");
                                currentTextView.setTextColor(Color.parseColor("#D60000"));
                            }
                        }

                        tideCount++;
                    }
                }
            }
        });
    }

    @Override
    public void tideDataUpdateFailed() {
        // For now do nothing
    }

    @Override
    public void latestBuoyFetchSuccess(final Buoy latestBuoy) {

        if (mWaterTemp == null) {
            if (latestBuoy == null) {
                return;
            }

            if (latestBuoy.waterTemperature == null) {
                return;
            }

            if (latestBuoy.waterTemperature.isEmpty()) {
                return;
            }

            mWaterTemp = latestBuoy.waterTemperature;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update the water temperature from the latest buoy reading
                TextView buoyLocationTV = (TextView) getActivity().findViewById(R.id.water_temp_text);
                if (buoyLocationTV != null) {
                    buoyLocationTV.setText(mDefaultBuoyLocation);
                }

                TextView waterTemp = (TextView) getActivity().findViewById(R.id.water_temp_value);
                if (waterTemp != null) {
                    String waterTempValue = mWaterTemp + " " + getResources().getString(R.string.water_temp_holder);
                    waterTemp.setText(waterTempValue);
                }
            }
        });
    }

    @Override
    public void latestBuoyFetchFailed() {
        // For now do nothing
    }

    public void reloadWaterTemperature() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        mDefaultBuoyLocation = sharedPrefs.getString(SettingsActivity.DEFAULT_BUOY_LOCATION_KEY, BuoyModel.BLOCK_ISLAND_LOCATION);

        // Fetch the data from the models
        BuoyModel.getInstance(getActivity()).fetchLatestBuoyReadingForLocation(mDefaultBuoyLocation, this);
    }
}

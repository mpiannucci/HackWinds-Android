package com.nucc.hackwinds.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.listeners.LatestBuoyFetchListener;
import com.nucc.hackwinds.listeners.TideChangedListener;
import com.nucc.hackwinds.types.Buoy;
import com.nucc.hackwinds.types.Tide;
import com.nucc.hackwinds.models.BuoyModel;
import com.nucc.hackwinds.models.TideModel;


public class TideFragment extends Fragment implements TideChangedListener, LatestBuoyFetchListener {
    private TideModel mTideModel;
    private String mDefaultBuoyLocation;
    private String mWaterTemp;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferencesListener;

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
                if (mTideModel.otherEvents.size() < 2) {
                    return;
                }

                // For now there are always two events so no need for a list. Get all of the views!!
                TextView firstEventTypeText = (TextView) getActivity().findViewById(R.id.sun_first_event_type);
                if (firstEventTypeText == null) {
                    return;
                }
                TextView firstEventTimeText = (TextView) getActivity().findViewById(R.id.sun_first_event_time);
                if (firstEventTimeText == null) {
                    return;
                }
                ImageView firstEventIcon = (ImageView) getActivity().findViewById(R.id.sun_first_icon);
                if (firstEventIcon == null) {
                    return;
                }
                TextView secondEventTypeText = (TextView) getActivity().findViewById(R.id.sun_second_event_type);
                if (secondEventTypeText == null) {
                    return;
                }
                TextView secondEventTimeText = (TextView) getActivity().findViewById(R.id.sun_second_event_time);
                if (secondEventTimeText == null) {
                    return;
                }
                ImageView secondEventIcon = (ImageView) getActivity().findViewById(R.id.sun_second_icon);
                if (secondEventIcon == null) {
                    return;
                }

                Drawable sunriseDrawable = getResources().getDrawable(R.drawable.ic_brightness_high_white_36dp);
                Drawable sunsetDrawable = getResources().getDrawable(R.drawable.ic_brightness_low_white_36dp);

                // Fill the data!
                Tide firstEvent = mTideModel.otherEvents.get(0);
                firstEventTypeText.setText(firstEvent.eventType);
                firstEventTimeText.setText(firstEvent.getTimeString());
                if (firstEvent.isSunrise()) {
                    firstEventIcon.setImageDrawable(sunriseDrawable);
                } else {
                    firstEventIcon.setImageDrawable(sunsetDrawable);
                }

                Tide secondEvent = mTideModel.otherEvents.get(1);
                secondEventTypeText.setText(secondEvent.eventType);
                secondEventTimeText.setText(secondEvent.getTimeString());
                if (secondEvent.isSunrise()) {
                    secondEventIcon.setImageDrawable(sunriseDrawable);
                } else {
                    secondEventIcon.setImageDrawable(sunsetDrawable);
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
                TextView buoyLocationTV = (TextView) getActivity().findViewById(R.id.water_temp_location);
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
        mDefaultBuoyLocation = sharedPrefs.getString(SettingsActivity.DEFAULT_BUOY_LOCATION_KEY, BuoyModel.MONTAUK_LOCATION);

        // Fetch the data from the models
        BuoyModel.getInstance(getActivity()).fetchLatestBuoyReadingForLocation(mDefaultBuoyLocation, this);
    }
}

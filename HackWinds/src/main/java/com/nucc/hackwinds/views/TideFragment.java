package com.nucc.hackwinds.views;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.types.Tide;
import com.nucc.hackwinds.models.BuoyModel;
import com.nucc.hackwinds.models.TideModel;
import com.nucc.hackwinds.utilities.ReachabilityHelper;


public class TideFragment extends Fragment {
    private TideModel mTideModel;
    private BuoyModel mBuoyModel;

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

        if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            mTideModel = TideModel.getInstance(getActivity());
            mBuoyModel = BuoyModel.getInstance(getActivity());

            // deploy the Wunderground async task
            new FetchTideDataTask().execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.tide_fragment, container, false);
        return V;
    }

    // Set the view to reflect the current values received
    public void updateView() {
        int tideCount = 0;

        // Set the upcoming and sunrise/sunset values
        for (Tide thisTide : mTideModel.tides ) {
            if (thisTide.isSunrise()) {
                // Set the sunrise time that was found
                TextView sunriseTime = (TextView)getActivity().findViewById(R.id.sunriseTime);
                sunriseTime.setText(thisTide.Time);
            } else if (thisTide.isSunset()) {
                // Set the sunset time that was read
                TextView sunsetTime = (TextView)getActivity().findViewById(R.id.sunsetTime);
                sunsetTime.setText(thisTide.Time);
            } else if (thisTide.isTidalEvent()) {
                // TODO: THERES A BUG HERE.. Yay we found a tide, now set the type (high or low) and the values
                TextView typeView = (TextView)getActivity().findViewById(mTideTypeViews[tideCount]);
                TextView timeView = (TextView)getActivity().findViewById(mTideTimeViews[tideCount]);
                typeView.setText(thisTide.EventType);
                timeView.setText(thisTide.Height + " at " + thisTide.Time);

                // Also set the current status
                if (tideCount == 0) {
                    TextView currentTextView = (TextView) getActivity().findViewById(R.id.currentTideStatus);
                    if (thisTide.isHighTide()) {
                        // Tide is incoming
                        currentTextView.setText("Incoming");
                        currentTextView.setTextColor(Color.GREEN);
                    } else {
                        // Assume low time so outgoing
                        currentTextView.setText("Outgoing");
                        currentTextView.setTextColor(Color.RED);
                    }
                }

                tideCount++;
            }
        }
        // Update the water temperature from the latest buoy reading
        TextView biWaterTemp = (TextView) getActivity().findViewById(R.id.waterTempValue);
        if (mBuoyModel.getBuoyDataForLocation(BuoyModel.Location.BLOCK_ISLAND).size() > 0) {
            String waterTempValue = mBuoyModel.getBuoyDataForLocation(BuoyModel.Location.BLOCK_ISLAND).get(0).WaterTemperature + " " + getResources().getString(R.string.water_temp_holder);
            biWaterTemp.setText(waterTempValue);
        }
    }

    public class FetchTideDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Get the values using the model and parse the data
            mTideModel.fetchTideData();
            mBuoyModel.fetchBuoyDataForLocation(BuoyModel.Location.BLOCK_ISLAND);

            // Return
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Set the tide adapter to the list
            updateView();
        }
    }
}

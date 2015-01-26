package com.nucc.hackwinds;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TideFragment extends Fragment {
    private TideModel mTideModel;

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

        if (isOnline()) {
            mTideModel = TideModel.getInstance();

            // deploy the Wunderground async task
            new BackgroundWunderAsyncTask().execute();
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

        // Set the current tide status

        // Set the upcoming and sunrise/sunset values
        for (Tide thisTide : mTideModel.tides ) {
            if (thisTide.EventType.equals(Tide.HIGH_TIDE_TAG) ||
                thisTide.EventType.equals(Tide.LOW_TIDE_TAG)) {
                // Yay we found a tide, now set the type (high or low) and the values
                TextView typeView = (TextView)getActivity().findViewById(mTideTypeViews[tideCount]);
                TextView timeView = (TextView)getActivity().findViewById(mTideTimeViews[tideCount]);
                typeView.setText(thisTide.EventType);
                timeView.setText(thisTide.Height + " at " + thisTide.Time);

                // Also set the current status
                if (tideCount == 0) {
                    TextView currentTextView = (TextView) getActivity().findViewById(R.id.currentTideStatus);
                    if (thisTide.EventType.equals(Tide.HIGH_TIDE_TAG)) {
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
            } else if (thisTide.EventType.equals(Tide.SUNRISE_TAG)) {
                // Set the sunrise time that was found
                TextView sunriseTime = (TextView)getActivity().findViewById(R.id.sunriseTime);
                sunriseTime.setText(thisTide.Time);
            } else if (thisTide.EventType.equals(Tide.SUNSET_TAG)) {
                // Set the sunset time that was read
                TextView sunsetTime = (TextView)getActivity().findViewById(R.id.sunsetTime);
                sunsetTime.setText(thisTide.Time);
            } else {
                // Its not relevant and shouldn't have slipped by
            }
        }
    }

    public class BackgroundWunderAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Get the values using the model and parse the data
            mTideModel.getTideData();

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

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}

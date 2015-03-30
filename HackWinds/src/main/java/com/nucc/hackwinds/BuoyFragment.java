package com.nucc.hackwinds;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import info.hoang8f.android.segmented.SegmentedGroup;


public class BuoyFragment extends ListFragment {

    // Member variables
    BuoyModel mBuoyModel;
    BuoyArrayAdapter mBuoyArrayAdapter;

    int mLocation = BuoyModel.BLOCK_ISLAND_LOCATION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            // Get the buoy model
            mBuoyModel = BuoyModel.getInstance();

            // Get the BI location to initialize
            new BackgroundBuoyAsyncTask().execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.buoy_fragment, container, false);

        // Set the segment control to block island
        RadioButton biButton = (RadioButton) V.findViewById(R.id.biButton);
        biButton.setChecked(true);

        // Set the tint of the segment control
        SegmentedGroup locationGroup = (SegmentedGroup) V.findViewById(R.id.segmentedBuoy);
        locationGroup.setTintColor(getResources().getColor(R.color.hackwinds_blue));

        // Set the listener for the segment group radio change
        locationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.biButton) {
                    // Switch to block island view so get that data
                    mLocation = BuoyModel.BLOCK_ISLAND_LOCATION;
                } else {
                    // Switch to Montauk buoy view
                    mLocation = BuoyModel.MONTAUK_LOCATION;
                }
                if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
                    new BackgroundBuoyAsyncTask().execute();
                }
            }
        });
        return V;
    }

    public class BackgroundBuoyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Get the values using the model and parse the data
            mBuoyModel.getBuoyDataForLocation(mLocation);

            // Return
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Set the tide adapter to the list
            if (mLocation == BuoyModel.BLOCK_ISLAND_LOCATION) {
                mBuoyArrayAdapter = new BuoyArrayAdapter(getActivity(), mBuoyModel.blockIslandBuoyData);
            } else {
                mBuoyArrayAdapter = new BuoyArrayAdapter(getActivity(), mBuoyModel.montaukBuoyData);
            }
            setListAdapter(mBuoyArrayAdapter);
        }

    }
}

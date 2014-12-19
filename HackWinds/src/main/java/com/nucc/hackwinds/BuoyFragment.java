package com.nucc.hackwinds;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.GregorianCalendar;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

import info.hoang8f.android.segmented.SegmentedGroup;


public class BuoyFragment extends ListFragment {
    // Public constants
    final public int BI_LOCATION = 41;
    final public int MTK_LOCATION = 42;

    // Member variables
    BuoyModel mBuoyModel;
    BuoyArrayAdapter mBuoyArrayAdapter;

    int mLocation = BI_LOCATION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the buoy model
        mBuoyModel = BuoyModel.getInstance();

        // Get the BI location to initialize
        new BackgroundBuoyAsyncTask().execute();
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
                    mLocation = BI_LOCATION;
                } else {
                    // Switch to Montauk buoy view
                    mLocation = MTK_LOCATION;
                }
                new BackgroundBuoyAsyncTask().execute();
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
            if (mLocation == BI_LOCATION) {
                mBuoyArrayAdapter = new BuoyArrayAdapter(getActivity(), mBuoyModel.blockIslandBuoyData);
            } else {
                mBuoyArrayAdapter = new BuoyArrayAdapter(getActivity(), mBuoyModel.montaukBuoyData);
            }
            setListAdapter(mBuoyArrayAdapter);
        }

    }
}

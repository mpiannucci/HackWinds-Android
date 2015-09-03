package com.nucc.hackwinds.views;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.BuoyArrayAdapter;
import com.nucc.hackwinds.listeners.BuoyChangedListener;
import com.nucc.hackwinds.models.BuoyModel;
import com.nucc.hackwinds.utilities.ReachabilityHelper;

import info.hoang8f.android.segmented.SegmentedGroup;


public class BuoyFragment extends ListFragment {

    // Member variables
    private BuoyModel mBuoyModel;
    private BuoyArrayAdapter mBuoyArrayAdapter;
    private String mLocation = BuoyModel.BLOCK_ISLAND_LOCATION;
    private BuoyChangedListener mBuoyChangedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            // Get the buoy model
            mBuoyModel = BuoyModel.getInstance(getActivity());

            // Set up the buoy listener
            mBuoyChangedListener = new BuoyChangedListener() {
                @Override
                public void buoyLocationChanged() {
                    new FetchBuoyDataTask().execute();
                }
            };
            mBuoyModel.addBuoyChangedListener(mBuoyChangedListener);

            // Get the BI location to initialize
            new FetchBuoyDataTask().execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.buoy_fragment, container, false);

        // Set the segment control to block island
        RadioButton biButton = (RadioButton) V.findViewById(R.id.buoy_summary_mode_segment_button);
        biButton.setChecked(true);

        // Set the tint of the segment control
        SegmentedGroup locationGroup = (SegmentedGroup) V.findViewById(R.id.segmentedBuoyGroup);
        locationGroup.setTintColor(getResources().getColor(R.color.hackwinds_blue));

        // Set the listener for the segment group radio change
        locationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton clickedButton = (RadioButton) getActivity().findViewById(radioGroup.getCheckedRadioButtonId());
                mBuoyArrayAdapter.setDataMode(String.valueOf(clickedButton.getText()));
            }
        });
        return V;
    }

    public class FetchBuoyDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Get the values using the model and parse the data
            mBuoyModel.fetchBuoyData();

            // Return
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (mBuoyArrayAdapter == null) {
                mBuoyArrayAdapter = new BuoyArrayAdapter(getActivity(), mBuoyModel.getBuoyData(), BuoyModel.SUMMARY_DATA_MODE);
                setListAdapter(mBuoyArrayAdapter);
            } else {
                mBuoyArrayAdapter.setBuoyData(mBuoyModel.getBuoyData());
            }
        }

    }
}

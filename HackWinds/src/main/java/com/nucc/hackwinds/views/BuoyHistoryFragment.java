package com.nucc.hackwinds.views;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.BuoyHistoryArrayAdapter;
import com.nucc.hackwinds.listeners.BuoyChangedListener;
import com.nucc.hackwinds.models.BuoyModel;
import com.nucc.hackwinds.utilities.ReachabilityHelper;

import info.hoang8f.android.segmented.SegmentedGroup;


public class BuoyHistoryFragment extends ListFragment {

    // Member variables
    private BuoyModel mBuoyModel;
    private BuoyHistoryArrayAdapter mBuoyArrayAdapter;
    private BuoyChangedListener mBuoyChangedListener;
    private boolean mLastFetch = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the menu options
        setHasOptionsMenu(true);

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
        View V = inflater.inflate(R.layout.buoy_history_fragment, container, false);

        // Set the segment control to block island
        RadioButton summaryButton = (RadioButton) V.findViewById(R.id.buoy_summary_mode_segment_button);
        summaryButton.setChecked(true);

        // Set the tint of the segment control
        SegmentedGroup locationGroup = (SegmentedGroup) V.findViewById(R.id.buoy_segmented_group);
        locationGroup.setTintColor(ContextCompat.getColor(getActivity(), R.color.hackwinds_blue));

        // Set the listener for the segment group radio change
        locationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton clickedButton = (RadioButton) getActivity().findViewById(radioGroup.getCheckedRadioButtonId());
                if (mBuoyArrayAdapter != null) {
                    mBuoyArrayAdapter.setDataMode(String.valueOf(clickedButton.getText()));
                }
            }
        });
        return V;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.buoy_menu_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_buoy_plots:
                startActivity(new Intent(getActivity(), AdditionalBuoyPlotsActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public class FetchBuoyDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Get the values using the model and parse the data
            mLastFetch = mBuoyModel.fetchBuoyData();

            // Return
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (mBuoyArrayAdapter == null) {
                if (mLastFetch) {
                    mBuoyArrayAdapter = new BuoyHistoryArrayAdapter(getActivity(), mBuoyModel.getBuoyData(), BuoyModel.SUMMARY_DATA_MODE);
                    setListAdapter(mBuoyArrayAdapter);
                }
            } else {
                if (mLastFetch) {
                    mBuoyArrayAdapter.setBuoyData(mBuoyModel.getBuoyData());
                } else {
                    mBuoyArrayAdapter.clear();
                }
            }
        }

    }
}

package com.nucc.hackwinds.views;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.BuoyHistoryArrayAdapter;
import com.nucc.hackwinds.listeners.BuoyChangedListener;
import com.nucc.hackwinds.models.BuoyModel;


public class BuoyHistoryFragment extends ListFragment implements BuoyChangedListener {

    public String dataMode = BuoyModel.SUMMARY_DATA_MODE;

    // Member variables
    private BuoyModel mBuoyModel;
    private BuoyHistoryArrayAdapter mBuoyArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBuoyModel = BuoyModel.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.buoy_history_fragment, container, false);

        buoyDataUpdated();

        return V;
    }

    @Override
    public void buoyDataUpdated() {
        if (mBuoyModel.getBuoyData().isEmpty()) {
            return;
        }

        // Update the data in the list adapter
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBuoyArrayAdapter == null) {
                    mBuoyArrayAdapter = new BuoyHistoryArrayAdapter(getActivity(), mBuoyModel.getBuoyData(), dataMode);
                    setListAdapter(mBuoyArrayAdapter);
                } else {
                    mBuoyArrayAdapter.setBuoyData(mBuoyModel.getBuoyData());
                }
            }
        });
    }

    @Override
    public void buoyDataUpdateFailed() {
        // No data for the buoy so clear out the adapter
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBuoyArrayAdapter.clear();
            }
        });
    }
}

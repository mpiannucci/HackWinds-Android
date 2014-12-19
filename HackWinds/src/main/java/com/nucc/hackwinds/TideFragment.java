package com.nucc.hackwinds;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class TideFragment extends ListFragment {
    TideModel mTideModel;
    TideArrayAdapter mTideArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTideModel = TideModel.getInstance();

        // deploy the wunderground async task
        new BackgroundWunderAsyncTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.tide_fragment, container, false);
        return V;
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

            // If there arent enough values, remove it from the list
            if (mTideModel.tides.get(mTideModel.tides.size() - 1).dType[0] == null) {
                mTideModel.tides.remove(mTideModel.tides.size() - 1);
            }

            // Set the tide adapter to the list
            mTideArrayAdapter = new TideArrayAdapter(getActivity(), mTideModel.tides);
            setListAdapter(mTideArrayAdapter);
        }

    }
}

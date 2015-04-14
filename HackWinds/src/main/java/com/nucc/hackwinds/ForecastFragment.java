package com.nucc.hackwinds;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


public class ForecastFragment extends ListFragment {

    // Declare member variables
    private ForecastModel mForecastModel;
    private ForecastArrayAdapter mForecastArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            // Initialize forecast model
            mForecastModel = ForecastModel.getInstance(getActivity());

            // Get the forecast data
            new BackgroundForecastAsyncTask().execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.forecast_fragment, container, false);

        return V;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Responding to clicks
        Intent intent = new Intent(getActivity(), DetailedForecastActivity.class);
        startActivity(intent);
    }

    class BackgroundForecastAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {

            // Forecast data
            mForecastModel.getForecasts();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Set the forecast adapter to the ListView
            mForecastArrayAdapter = new ForecastArrayAdapter(getActivity(), mForecastModel.forecasts);
            setListAdapter(mForecastArrayAdapter);
        }
    }
}

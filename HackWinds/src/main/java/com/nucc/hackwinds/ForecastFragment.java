package com.nucc.hackwinds;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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

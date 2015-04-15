package com.nucc.hackwinds;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


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
            new FetchForecastTask().execute();
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

        // Pass the index of the day
        intent.putExtra("dayIndex", position);

        // Pass the name of the day so we can set the toolbar
        TextView dayView = (TextView) v.findViewById(R.id.forecastHeader);
        intent.putExtra("dayName", (String)dayView.getText());

        // Start the detailed forecast intent
        startActivity(intent);
    }

    class FetchForecastTask extends AsyncTask<Void, Void, Void> {
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

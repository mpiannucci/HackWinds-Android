package com.nucc.hackwinds.views;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.ForecastArrayAdapter;
import com.nucc.hackwinds.listeners.ForecastChangedListener;
import com.nucc.hackwinds.models.ForecastModel;
import com.nucc.hackwinds.utilities.ReachabilityHelper;


public class ForecastFragment extends ListFragment {

    // Declare member variables
    private ForecastModel mForecastModel;
    private ForecastArrayAdapter mForecastArrayAdapter;
    private ForecastChangedListener mForecastChangedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the menu
        setHasOptionsMenu(true);

        if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            // Initialize forecast model
            mForecastModel = ForecastModel.getInstance(getActivity());

            // Set the forecast location changed listener
            mForecastChangedListener = new ForecastChangedListener() {
                @Override
                public void forecastLocationChanged() {
                    // When the forecast changes reload the condition data
                    new FetchForecastTask().execute();
                }
            };
            mForecastModel.addForecastChangedListener(mForecastChangedListener);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecast_menu_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_model_forecast:
                // TODO: Launch Model activity
                // startActivity(new Intent(getActivity(), AlternateCameraActivity.class));
                //break;
            default:
                return super.onOptionsItemSelected(item);
        }
        //return true;
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
            mForecastModel.fetchForecastData();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Set the forecast adapter to the ListView
            if (mForecastArrayAdapter == null) {
                mForecastArrayAdapter = new ForecastArrayAdapter(getActivity(), mForecastModel.getForecasts());
                setListAdapter(mForecastArrayAdapter);
            } else {
                mForecastArrayAdapter.setForecastData(mForecastModel.getForecasts());
            }
        }
    }
}

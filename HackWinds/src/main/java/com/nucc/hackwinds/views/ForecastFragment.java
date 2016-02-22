package com.nucc.hackwinds.views;

import android.content.Intent;
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


public class ForecastFragment extends ListFragment implements ForecastChangedListener {

    // Declare member variables
    private ForecastModel mForecastModel;
    private ForecastArrayAdapter mForecastArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the menu
        setHasOptionsMenu(true);

        if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            // Initialize forecast model
            mForecastModel = ForecastModel.getInstance(getActivity());

            // Set the forecast updated listener
            mForecastModel.addForecastChangedListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.forecast_fragment, container, false);

        return V;
    }

    @Override
    public void onResume() {
        super.onResume();

        forecastDataUpdated();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.forecast_menu_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_model_forecast:
                // Launch Model activity
                startActivity(new Intent(getActivity(), WaveWatchChartActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Responding to clicks
        Intent intent = new Intent(getActivity(), DetailedForecastActivity.class);

        // Pass the index of the day
        intent.putExtra("dayIndex", position);

        // Pass the name of the day so we can set the toolbar
        TextView dayView = (TextView) v.findViewById(R.id.forecast_header);
        intent.putExtra("dayName", (String)dayView.getText());

        // Start the detailed forecast intent
        startActivity(intent);
    }

    @Override
    public void forecastDataUpdated() {
        if (mForecastModel.dailyForecasts.isEmpty()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Set the forecast adapter to the ListView
                if (mForecastArrayAdapter == null) {
                    mForecastArrayAdapter = new ForecastArrayAdapter(getActivity(), mForecastModel.dailyForecasts);
                    setListAdapter(mForecastArrayAdapter);
                } else {
                    mForecastArrayAdapter.setForecastData(mForecastModel.dailyForecasts);
                }
            }
        });
    }

    @Override
    public void forecastDataUpdateFailed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mForecastArrayAdapter != null) {
                    mForecastArrayAdapter.clear();
                }
            }
        });
    }
}

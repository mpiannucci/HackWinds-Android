package com.nucc.hackwinds.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.mpitester_13.station.model.ApiApiMessagesDataMessage;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.listeners.BuoyChangedListener;
import com.nucc.hackwinds.models.BuoyModel;
import com.nucc.hackwinds.utilities.Extensions;

import java.util.Locale;

public class BuoyFragment extends Fragment implements BuoyChangedListener, SwipeRefreshLayout.OnRefreshListener{

    private BuoyModel mBuoyModel;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the menu options
        setHasOptionsMenu(true);

        mBuoyModel = BuoyModel.getInstance(getActivity());
        mBuoyModel.addBuoyChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.buoy_fragment, container, false);

        mRefreshLayout = (SwipeRefreshLayout) V.findViewById(R.id.buoy_refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.hackwinds_blue), getResources().getColor(R.color.accent_blue));

        return V;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.buoy_menu_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mBuoyModel.isRefreshing()) {
            if (mRefreshLayout != null) {
                mRefreshLayout.setRefreshing(true);
            }
        }

        buoyDataUpdated();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void buoyDataUpdated() {
        final ApiApiMessagesDataMessage data = mBuoyModel.getBuoyData();

        if (data == null) {
            buoyDataUpdateFailed();
            return;
        }

        if (data.getWaveSummary() == null) {
            buoyDataUpdateFailed();
            return;
        }

        if (data.getSwellComponents() == null) {
            buoyDataUpdateFailed();
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRefreshLayout != null) {
                    mRefreshLayout.setRefreshing(false);
                }

                TextView currentBuoyStatus = (TextView) getActivity().findViewById(R.id.buoy_current_reading);
                if (currentBuoyStatus != null) {
                    currentBuoyStatus.setText(Extensions.getSwellSummary(data.getWaveSummary()));
                }

                TextView currentPrimaryStatus = (TextView) getActivity().findViewById(R.id.buoy_primary_reading);
                if (currentPrimaryStatus != null) {
                    if (data.getSwellComponents().size() > 0) {
                        currentPrimaryStatus.setText(Extensions.getDetailedSwellSummary(data.getSwellComponents().get(0)));
                    } else {
                        currentPrimaryStatus.setText("No primary swell");
                    }
                }

                TextView currentSecondaryStatus = (TextView) getActivity().findViewById(R.id.buoy_secondary_reading);
                if (currentSecondaryStatus != null) {
                    if (data.getSwellComponents().size() > 1) {
                        currentSecondaryStatus.setText(Extensions.getDetailedSwellSummary(data.getSwellComponents().get(1)));
                    } else {
                        currentSecondaryStatus.setText("No secondary swell");
                    }
                }

                TextView latestBuoyReadingTime = (TextView) getActivity().findViewById(R.id.buoy_time_reading);
                if (latestBuoyReadingTime != null) {
                    String buoyReport = String.format(Locale.US, "Buoy reported at %s %s", Extensions.getTimeString(data), Extensions.getDateString(data));
                    latestBuoyReadingTime.setText(buoyReport);
                }

                ImageView directionalSpectraPlot = (ImageView) getActivity().findViewById(R.id.directional_spectra_plot);
                if (directionalSpectraPlot != null) {
                    Ion.with(getActivity()).load(data.getDirectionSpectraPlot()).intoImageView(directionalSpectraPlot);
                }

                ImageView energyDistributionPlot = (ImageView) getActivity().findViewById(R.id.energy_distribution_plot);
                if (energyDistributionPlot != null) {
                    Ion.with(getActivity()).load(data.getEnergySpectraPlot()).intoImageView(energyDistributionPlot);
                }
            }
        });
    }

    @Override
    public void buoyDataUpdateFailed() {
        if (mBuoyModel.isRefreshing()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRefreshLayout != null) {
                    mRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    @Override
    public void buoyRefreshStarted() {
        if (mRefreshLayout != null) {
            mRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    public void onRefresh() {
        mBuoyModel.fetchNewBuoyData();
    }
}

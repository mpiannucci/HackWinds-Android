package com.nucc.hackwinds.views;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Image;
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

import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.listeners.BuoyChangedListener;
import com.nucc.hackwinds.models.BuoyModel;
import com.nucc.hackwinds.types.Buoy;

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
        final Buoy buoy = mBuoyModel.getBuoyData();

        if (buoy == null) {
            buoyDataUpdateFailed();
            return;
        }

        if (buoy.waveSummary == null) {
            buoyDataUpdateFailed();
            return;
        }

        if (buoy.swellComponents == null) {
            buoyDataUpdateFailed();
            return;
        }

        if (buoy.swellComponents.size() < 2) {
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
                    currentBuoyStatus.setText(buoy.getWaveSummaryStatusText());
                }

                TextView currentPrimaryStatus = (TextView) getActivity().findViewById(R.id.buoy_primary_reading);
                if (currentPrimaryStatus != null) {
                    currentPrimaryStatus.setText(buoy.swellComponents.get(0).getDetailedSwellSummary());
                }

                TextView currentSecondaryStatus = (TextView) getActivity().findViewById(R.id.buoy_secondary_reading);
                if (currentSecondaryStatus != null) {
                    currentSecondaryStatus.setText(buoy.swellComponents.get(1).getDetailedSwellSummary());
                }

                TextView latestBuoyReadingTime = (TextView) getActivity().findViewById(R.id.buoy_time_reading);
                if (latestBuoyReadingTime != null) {
                    String buoyReport = String.format(Locale.US, "Buoy reported at %s %s", buoy.timeString(), buoy.dateString());
                    latestBuoyReadingTime.setText(buoyReport);
                }

                ImageView directionalSpectraPlot = (ImageView) getActivity().findViewById(R.id.directional_spectra_plot);
                if (directionalSpectraPlot != null) {
                    directionalSpectraPlot.setImageBitmap(BitmapFactory.decodeByteArray(buoy.directionalWaveSpectraBase64, 0, buoy.directionalWaveSpectraBase64.length));
                }

                ImageView energyDistributionPlot = (ImageView) getActivity().findViewById(R.id.energy_distribution_plot);
                if (energyDistributionPlot != null) {
                    energyDistributionPlot.setImageBitmap(BitmapFactory.decodeByteArray(buoy.waveEnergySpectraBase64, 0, buoy.waveEnergySpectraBase64.length));
                }

                final String waveHeightSource = "https://dl.dropboxusercontent.com/s/uplzpw44vva91a1/test.png";
                ImageView waveHeightEstimationImage = (ImageView) getActivity().findViewById(R.id.wave_height_estimation_image);
                if (waveHeightEstimationImage != null) {
                    Ion.with(getActivity()).load(waveHeightSource).intoImageView(waveHeightEstimationImage);
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

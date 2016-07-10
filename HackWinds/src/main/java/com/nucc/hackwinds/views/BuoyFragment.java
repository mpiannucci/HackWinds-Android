package com.nucc.hackwinds.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class BuoyFragment extends Fragment implements BuoyChangedListener{

    private BuoyModel mBuoyModel;

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

        return V;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.buoy_menu_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_buoy_history:
                startActivity(new Intent(getActivity(), BuoyHistoryActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        buoyDataUpdated();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void buoyDataUpdated() {
        if (mBuoyModel.getBuoyData().isEmpty()) {
            return;
        }

        final Buoy buoy = mBuoyModel.getBuoyData().get(0);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView currentBuoyStatus = (TextView)getActivity().findViewById(R.id.buoy_current_reading);
                if (currentBuoyStatus != null) {
                    currentBuoyStatus.setText(buoy.getWaveSummaryStatusText());
                }

                TextView currentPrimaryStatus = (TextView)getActivity().findViewById(R.id.buoy_primary_reading);
                if (currentPrimaryStatus != null) {
                    currentPrimaryStatus.setText(buoy.getPrimarySwellText());
                }

                TextView currentSecondaryStatus = (TextView)getActivity().findViewById(R.id.buoy_secondary_reading);
                if (currentSecondaryStatus != null) {
                    currentSecondaryStatus.setText(buoy.getSecondarySwellText());
                }

                TextView latestBuoyReadingTime = (TextView)getActivity().findViewById(R.id.buoy_time_reading);
                if (latestBuoyReadingTime != null) {
                    String buoyReport = String.format(Locale.US, "Buoy reported at %s %s", buoy.timeString(), buoy.dateString());
                    latestBuoyReadingTime.setText(buoyReport);
                }

                ImageView latestWaveSpectraImage = (ImageView)getActivity().findViewById(R.id.latest_buoy_spectra_plot);
                if (latestWaveSpectraImage != null) {
                    Ion.with(getActivity()).load(mBuoyModel.getSpectraPlotURL()).intoImageView(latestWaveSpectraImage);
                }
            }
        });
    }

    @Override
    public void buoyDataUpdateFailed() {

    }
}

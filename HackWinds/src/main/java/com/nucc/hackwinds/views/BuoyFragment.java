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
        final Buoy buoy = mBuoyModel.getBuoyData();

        if (buoy == null) {
            return;
        }

        if (buoy.waveSummary == null) {
            return;
        }

        if (buoy.swellComponents == null) {
            return;
        }

        if (buoy.swellComponents.size() < 2) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView currentBuoyStatus = (TextView)getActivity().findViewById(R.id.buoy_current_reading);
                if (currentBuoyStatus != null) {
                    currentBuoyStatus.setText(buoy.getWaveSummaryStatusText());
                }

                TextView currentPrimaryStatus = (TextView)getActivity().findViewById(R.id.buoy_primary_reading);
                if (currentPrimaryStatus != null) {
                    currentPrimaryStatus.setText(buoy.swellComponents.get(0).getDetailedSwellSummary());
                }

                TextView currentSecondaryStatus = (TextView)getActivity().findViewById(R.id.buoy_secondary_reading);
                if (currentSecondaryStatus != null) {
                    currentSecondaryStatus.setText(buoy.swellComponents.get(1).getDetailedSwellSummary());
                }

                TextView latestBuoyReadingTime = (TextView)getActivity().findViewById(R.id.buoy_time_reading);
                if (latestBuoyReadingTime != null) {
                    String buoyReport = String.format(Locale.US, "Buoy reported at %s %s", buoy.timeString(), buoy.dateString());
                    latestBuoyReadingTime.setText(buoyReport);
                }
            }
        });
    }

    @Override
    public void buoyDataUpdateFailed() {

    }
}

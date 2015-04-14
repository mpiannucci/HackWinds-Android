package com.nucc.hackwinds;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import info.hoang8f.android.segmented.SegmentedGroup;


public class DetailedForecastFragment extends Fragment {
    // TODO: Rename and change types of parameters
    private int mDayIndex;
    private String mDayName;

    public DetailedForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.detailed_forecast_fragment, container, false);

        // Show the swell chart when the fragment is launched
        RadioButton swellButton = (RadioButton) V.findViewById(R.id.swellSegmentButton);
        swellButton.setChecked(true);

        // Get the Segmented widget
        SegmentedGroup chartTypeGroup = (SegmentedGroup) V.findViewById(R.id.segmentedChart);

        // Set the tint color of the segmented group
        chartTypeGroup.setTintColor(getResources().getColor(R.color.hackwinds_blue));

        return V;
    }

}

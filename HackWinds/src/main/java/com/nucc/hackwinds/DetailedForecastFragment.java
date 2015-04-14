package com.nucc.hackwinds;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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
        return inflater.inflate(R.layout.detailed_forecast_fragment, container, false);
    }

}

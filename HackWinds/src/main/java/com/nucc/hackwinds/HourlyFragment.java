package com.nucc.hackwinds;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.Condition;
import com.nucc.hackwinds.ConditionArrayAdapter;
import com.nucc.hackwinds.currentFragment.*;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import android.os.AsyncTask;

public class hourlyFragment extends ListFragment {

    ArrayList<Condition> conditionValues;
    ConditionArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // public Condition(String date, String minbreak, String maxBreak, 
        //     String windSpeed, String windDeg, String windDir, String swellHeight,
        //     String swellPeriod, String swellDeg) 
        conditionValues = new ArrayList<Condition>();
        new BackgroundMSWAsyncTask().execute(20);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.forecast_fragment, container, false);
        return V;
    }
}
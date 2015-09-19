package com.nucc.hackwinds.views;


import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;

import com.nucc.hackwinds.R;

import info.hoang8f.android.segmented.SegmentedGroup;

public class WaveWatchChartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_watch_chart);

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the segmented control view
        SegmentedGroup chartGroup = (SegmentedGroup) findViewById(R.id.wavewatch_segment_group);
        chartGroup.setTintColor(ContextCompat.getColor(getApplicationContext(), R.color.hackwinds_blue));
        RadioButton wavesButton = (RadioButton) findViewById(R.id.wavewatch_waves_mode_segment_button);
        wavesButton.setSelected(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

}

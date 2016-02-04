package com.nucc.hackwinds.views;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.BuoyModel;

public class BuoyHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buoy_history);

        // Get the day name to set the toolbar title
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String buoyLocation = sharedPrefs.getString(SettingsActivity.BUOY_LOCATION_KEY, BuoyModel.MONTAUK_LOCATION);

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(buoyLocation);
        setSupportActionBar(toolbar);

        // Create a new DetailedForecast Widget with the day index as a bundle
        BuoyHistoryFragment buoyHistoryFragment = new BuoyHistoryFragment();

        // Load the preference fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, buoyHistoryFragment).commit();
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

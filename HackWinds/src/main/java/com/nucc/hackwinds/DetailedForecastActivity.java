package com.nucc.hackwinds;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


public class DetailedForecastActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_forecast);

        // Get the day name to set the toolbar title
        String dayName = getIntent().getExtras().getString("dayName");

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(dayName);
        setSupportActionBar(toolbar);

        // Create a new DetailedForecast Widget with the day index as a bundle
        Bundle bundle = new Bundle();
        bundle.putInt("dayIndex", getIntent().getExtras().getInt("dayIndex"));
        DetailedForecastFragment detailedFragment = new DetailedForecastFragment();
        detailedFragment.setArguments(bundle);

        // Load the preference fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, detailedFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detailed_forecast, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

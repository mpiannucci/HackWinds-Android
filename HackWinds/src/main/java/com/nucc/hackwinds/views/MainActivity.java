package com.nucc.hackwinds.views;

import java.util.ArrayList;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.content.Intent;
import android.widget.Spinner;

import com.astuetz.PagerSlidingTabStrip;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.LocationArrayAdapter;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends ActionBarActivity {
    Toolbar toolbar;
    PagerSlidingTabStrip tabs;
    ViewPager pager;

    private MainPagerAdapter mAdapter;
    private SystemBarTintManager mTintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the toolbar and set it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Get the location spinner
        Spinner locationSpinner = (Spinner) findViewById(R.id.navigation_spinner);
        ArrayList<String> locations = new ArrayList<>();
        locations.add("Narragansett Town Beach");
        locations.add("Point Judith");
        locations.add("Block Island");
        LocationArrayAdapter locationAdapter = new LocationArrayAdapter(this, locations);
        locationSpinner.setAdapter(locationAdapter);

        // Create the system tint manager with this context
        mTintManager = new SystemBarTintManager(this);

        // Enable status bar tint
        mTintManager.setStatusBarTintEnabled(true);

        // Create and set the new pager adapter
        pager = (ViewPager) findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mAdapter = new MainPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(mAdapter);
        tabs.setViewPager(pager);

        // Set the background color of the tab strip, status bar, and action bar
        tabs.setBackgroundColor(getResources().getColor(R.color.hackwinds_blue));
        mTintManager.setStatusBarTintColor(getResources().getColor(R.color.hackwinds_blue));
        toolbar.setBackgroundColor(getResources().getColor(R.color.hackwinds_blue));

        // Set the toolbar Icon
        toolbar.setNavigationIcon(R.drawable.ic_launcher);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_alt_cameras:
                startActivity(new Intent(this, AlternateCameraActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public class MainPagerAdapter extends FragmentPagerAdapter {

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // get the title for each of the tabs
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.action_live).toUpperCase(l);
                case 1:
                    return getString(R.string.action_forecast).toUpperCase(l);
                case 2:
                    return getString(R.string.action_buoy).toUpperCase(l);
                case 3:
                    return getString(R.string.action_tide).toUpperCase(l);
            }
            return null;
        }

        @Override
        public int getCount() {
            // We have 4 pages
            return 4;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    // First tab was clicked, return Live fragment
                    return new CurrentFragment();
                case 1:
                    // Second is the Forecast fragment
                    return new ForecastFragment();
                case 2:
                    // Then the Buoy Fragment
                    return new BuoyFragment();
                case 3:
                    // Lastly the Tide Fragment
                    return new TideFragment();
            }
            return null;
        }
    }
}
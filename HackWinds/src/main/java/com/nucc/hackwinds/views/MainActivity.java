package com.nucc.hackwinds.views;

import java.util.ArrayList;
import java.util.Locale;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.astuetz.PagerSlidingTabStrip;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.LocationArrayAdapter;
import com.nucc.hackwinds.models.BuoyModel;
import com.nucc.hackwinds.models.ForecastModel;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private PagerSlidingTabStrip mSlidingTabStrip;
    private ViewPager mViewPager;
    private MainPagerAdapter mAdapter;
    private SystemBarTintManager mTintManager;
    private Spinner mLocationSpinner;
    private LocationArrayAdapter mLocationAdapter;
    private ArrayList<String> mForecastLocations;
    private ArrayList<String> mBuoyLocations;
    private ArrayList<String> mTideLocation;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPrefsChangedListener;
    private boolean initialPageLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the toolbar and set it as the actionbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // SharedPreference setup, always set the buoys to start at block island
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String defaultBuoyLocation = sharedPrefs.getString(SettingsActivity.DEFAULT_BUOY_LOCATION_KEY, BuoyModel.MONTAUK_LOCATION);
        sharedPrefs.edit().putString(SettingsActivity.BUOY_LOCATION_KEY, defaultBuoyLocation).apply();

        // Set up the spinner locations
        initLocationArrays();
        mLocationAdapter = new LocationArrayAdapter(this, mForecastLocations);
        mLocationSpinner = (Spinner) findViewById(R.id.navigation_spinner);
        mLocationSpinner.setAdapter(mLocationAdapter);
        mLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (initialPageLoad) {
                    initialPageLoad = false;
                    return;
                }

                String prefKey;
                final String currentPageTitle = String.valueOf(mAdapter.getPageTitle(mViewPager.getCurrentItem()));
                if (currentPageTitle.equals("FORECAST") || currentPageTitle.equals("LIVE")) {
                    prefKey = SettingsActivity.FORECAST_LOCATION_KEY;
                } else if (currentPageTitle.equals("BUOYS")) {
                    prefKey = SettingsActivity.BUOY_LOCATION_KEY;
                } else if (currentPageTitle.equals("TIDE")) {
                    prefKey = SettingsActivity.TIDE_LOCATION_KEY;
                } else {
                    return;
                }
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPrefs.edit().putString(prefKey, mLocationAdapter.getItem(position)).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Create the system tint manager with this context
        mTintManager = new SystemBarTintManager(this);

        // Enable status bar tint
        mTintManager.setStatusBarTintEnabled(true);

        // Create and set the new pager adapter
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mSlidingTabStrip.setViewPager(mViewPager);

        mSlidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Do nothing
            }

            @Override
            public void onPageSelected(int position) {
                String title = String.valueOf(mAdapter.getPageTitle(position));

                if (title.equals("LIVE") || title.equals("FORECAST")) {
                    mLocationAdapter.changeLocations(mForecastLocations);
                    mLocationAdapter.notifyDataSetChanged();
                    mLocationSpinner.setSelection(0);
                } else if (title.equals("BUOYS")) {
                    mLocationAdapter.changeLocations(mBuoyLocations);
                    mLocationAdapter.notifyDataSetChanged();
                    setSpinnerBuoyLocation();
                } else if (title.equals("TIDE")) {
                    mLocationAdapter.changeLocations(mTideLocation);
                    mLocationAdapter.notifyDataSetChanged();
                    mLocationSpinner.setSelection(0);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do Nothing
            }
        });

        // Set the background color of the tab strip, status bar, and action bar
        mSlidingTabStrip.setBackgroundColor(getResources().getColor(R.color.hackwinds_blue));
        mTintManager.setStatusBarTintColor(getResources().getColor(R.color.hackwinds_blue));
        mToolbar.setBackgroundColor(getResources().getColor(R.color.hackwinds_blue));

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
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void initLocationArrays() {
        mForecastLocations = new ArrayList<>();
        mForecastLocations.add("Narragansett");

        mBuoyLocations = new ArrayList<>();
        mBuoyLocations.add("Block Island");
        mBuoyLocations.add("Montauk");
        mBuoyLocations.add("Nantucket");

        mTideLocation = new ArrayList<>();
        mTideLocation.add("Point Judith Harbor");
    }

    public void setSpinnerBuoyLocation() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String buoyLocation = sharedPrefs.getString(SettingsActivity.BUOY_LOCATION_KEY, BuoyModel.MONTAUK_LOCATION);
        int index = 0;
        for (int i = 0; i < mLocationSpinner.getCount(); i++) {
            if (mLocationSpinner.getItemAtPosition(i).equals(buoyLocation)){
                index = i;
                break;
            }
        }

        // Set the spinner to the correct index
        mLocationSpinner.setSelection(index);
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
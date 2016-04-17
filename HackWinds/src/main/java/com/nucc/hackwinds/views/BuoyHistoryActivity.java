package com.nucc.hackwinds.views;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.BuoyModel;

import java.util.Locale;

public class BuoyHistoryActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mSlidingTabStrip;
    private BuoyPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buoy_history);

        // Get the day name to set the toolbar title
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String buoyLocation = sharedPrefs.getString(SettingsActivity.BUOY_LOCATION_KEY, BuoyModel.MONTAUK_LOCATION);

        // Set up the toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(buoyLocation);
        setSupportActionBar(mToolbar);

        // Create and set the new pager adapter
        mViewPager = (ViewPager) findViewById(R.id.buoy_history_pager);
        mSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.buoy_mode_tabs);
        mAdapter = new BuoyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mSlidingTabStrip.setViewPager(mViewPager);
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

    public class BuoyPagerAdapter extends FragmentPagerAdapter {
        public BuoyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // get the title for each of the tabs
            Locale l = Locale.US;
            switch (position) {
                case 0:
                    return getString(R.string.buoy_summary_mode_segment_title).toUpperCase(l);
                case 1:
                    return getString(R.string.buoy_swell_mode_segment_title).toUpperCase(l);
                case 2:
                    return getString(R.string.buoy_wind_mode_segment_title).toUpperCase(l);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // We have 3 buoy data modes
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    // Summary data mode
                    BuoyHistoryFragment summaryBuoyFrag = new BuoyHistoryFragment();
                    summaryBuoyFrag.dataMode = BuoyModel.SUMMARY_DATA_MODE;
                    return summaryBuoyFrag;
                case 1:
                    // Swell data mode
                    BuoyHistoryFragment swellBuoyFrag = new BuoyHistoryFragment();
                    swellBuoyFrag.dataMode = BuoyModel.SWELL_DATA_MODE;
                    return swellBuoyFrag;
                case 2:
                    // Wind data mode
                    BuoyHistoryFragment windBuoyFrag = new BuoyHistoryFragment();
                    windBuoyFrag.dataMode = BuoyModel.WIND_DATA_MODE;
                    return windBuoyFrag;
            }
            return null;
        }
    }
}

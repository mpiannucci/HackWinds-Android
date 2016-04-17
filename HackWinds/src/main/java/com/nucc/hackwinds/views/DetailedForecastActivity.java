package com.nucc.hackwinds.views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.ConditionArrayAdapter;
import com.nucc.hackwinds.models.BuoyModel;
import com.nucc.hackwinds.models.ForecastModel;
import com.nucc.hackwinds.types.Forecast;

import java.util.ArrayList;
import java.util.Locale;


public class DetailedForecastActivity extends AppCompatActivity {

    // UI objects
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mSlidingTabStrip;
    private BuoyHistoryActivity.BuoyPagerAdapter mAdapter;

    // Forecast values
    private ForecastModel mForecastModel;
    private ArrayList<Forecast> mDayConditions;
    private ConditionArrayAdapter mConditionArrayAdapter;

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

        // TODO: All the view pager stuff
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

    public class DetailedForecastPagerAdapter extends FragmentPagerAdapter {

        public DetailedForecastPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // get the title for each of the tabs
            Locale l = Locale.US;
            switch (position) {
                case 0:
                    return getString(R.string.wavewatch_wave_height_title).toUpperCase(l);
                case 1:
                    return getString(R.string.wavewatch_swell_title).toUpperCase(l);
                case 2:
                    return getString(R.string.wavewatch_wind_title).toUpperCase(l);
                case 3:
                    return getString(R.string.wavewatch_period_title).toUpperCase(l);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // We have 3 buoy data modes
            return 4;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    // Wave Height data mode
                    return null;
                case 1:
                    // Swell data mode
                    return null;
                case 2:
                    // Wind data mode
                    return null;
                case 3:
                    // Period mode
                    return null;
                default:
                    return null;
            }
        }
    }
}

package com.nucc.hackwinds.views;


import android.os.Bundle;
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

import java.util.Locale;

public class WaveWatchChartActivity extends AppCompatActivity {

    // UI objects
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mSlidingTabStrip;
    private AtlanticForecastChartPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_watch_chart);

        // Set up the toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Create and set the new pager adapter
        mViewPager = (ViewPager) findViewById(R.id.chart_type_pager);
        mSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.chart_mode_tabs);
        mAdapter = new AtlanticForecastChartPagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(1);
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

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    public class AtlanticForecastChartPagerAdapter extends FragmentPagerAdapter {

        public AtlanticForecastChartPagerAdapter(FragmentManager fm) {
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
                    return getString(R.string.wavewatch_wind_title).toUpperCase(l);
                case 2:
                    return getString(R.string.wavewatch_period_title).toUpperCase(l);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // We have 3 chart data modes
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    // Wave Height data mode
                    AtlanticForecastChartFragment waveChartFragment = new AtlanticForecastChartFragment();
                    waveChartFragment.setChartType(AtlanticForecastChartFragment.WaveWatchChartType.WAVES);
                    return waveChartFragment;
                case 1:
                    // Wind data mode
                    AtlanticForecastChartFragment windChartFragment = new AtlanticForecastChartFragment();
                    windChartFragment.setChartType(AtlanticForecastChartFragment.WaveWatchChartType.WIND);
                    return windChartFragment;
                case 2:
                    // Period data mode
                    AtlanticForecastChartFragment periodChartFragment = new AtlanticForecastChartFragment();
                    periodChartFragment.setChartType(AtlanticForecastChartFragment.WaveWatchChartType.PERIOD);
                    return periodChartFragment;
                default:
                    return null;
            }
        }
    }
}

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

import android.view.View;
import android.widget.ListView;
import com.astuetz.PagerSlidingTabStrip;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.ConditionArrayAdapter;
import com.nucc.hackwinds.models.ForecastModel;
import com.nucc.hackwinds.types.Forecast;
import com.nucc.hackwinds.utilities.WrappableViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class DetailedForecastActivity extends AppCompatActivity {

    // UI objects
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private PagerSlidingTabStrip mSlidingTabStrip;
    private DetailedForecastPagerAdapter mAdapter;
    private View mDetailedForecastListHeaderView;

    // Forecast values
    private ForecastModel mForecastModel;
    private ArrayList<Forecast> mDayConditions;
    private ConditionArrayAdapter mConditionArrayAdapter;
    private int dayIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_forecast);

        // Get the day name to set the toolbar title
        String dayName = getIntent().getExtras().getString("dayName");

        // Get the day index
        dayIndex = getIntent().getExtras().getInt("dayIndex");

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(dayName);
        setSupportActionBar(toolbar);

        // Create and set the new pager adapter
        mDetailedForecastListHeaderView = getLayoutInflater().inflate(R.layout.detailed_forecast_chart_layout, null);
        mViewPager = (WrappableViewPager) mDetailedForecastListHeaderView.findViewById(R.id.chart_type_pager);
        mSlidingTabStrip = (PagerSlidingTabStrip) mDetailedForecastListHeaderView.findViewById(R.id.chart_mode_tabs);
        mAdapter = new DetailedForecastPagerAdapter(getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(mAdapter);
        mSlidingTabStrip.setViewPager(mViewPager);

        // Get the forecast model and set up the condition list
        mForecastModel = ForecastModel.getInstance(this);
        mDayConditions = mForecastModel.getForecastsForDay(dayIndex);
        mConditionArrayAdapter = new ConditionArrayAdapter(this, mDayConditions);
        ListView conditionList = (ListView) findViewById(R.id.detailed_condition_list);
        conditionList.addHeaderView(mDetailedForecastListHeaderView);
        conditionList.setAdapter(mConditionArrayAdapter);
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
                    DetailedForecastChartFragment waveChartFragment = new DetailedForecastChartFragment();
                    waveChartFragment.setChartType(DetailedForecastChartFragment.ForecastChartType.WAVES);
                    waveChartFragment.setDayIndex(dayIndex);
                    return waveChartFragment;
                case 1:
                    // Wind data mode
                    DetailedForecastChartFragment windChartFragment = new DetailedForecastChartFragment();
                    windChartFragment.setChartType(DetailedForecastChartFragment.ForecastChartType.WIND);
                    windChartFragment.setDayIndex(dayIndex);
                    return windChartFragment;
                case 2:
                    // Period data mode
                    DetailedForecastChartFragment periodChartFragment = new DetailedForecastChartFragment();
                    periodChartFragment.setChartType(DetailedForecastChartFragment.ForecastChartType.PERIOD);
                    periodChartFragment.setDayIndex(dayIndex);
                    return periodChartFragment;
                default:
                    return null;
            }
        }
    }
}

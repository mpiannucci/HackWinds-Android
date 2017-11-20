package com.nucc.hackwinds.views;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
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
import com.nucc.hackwinds.models.CameraModel;
import com.nucc.hackwinds.models.ForecastModel;
import com.nucc.hackwinds.models.TideModel;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class MainActivity extends AppCompatActivity {

    static final int LIVE_PAGE_INDEX = 0;
    static final int FORECAST_PAGE_INDEX = 1;
    static final int BUOY_PAGE_INDEX = 2;
    static final int TIDE_PAGE_INDEX = 3;

    private Toolbar mToolbar;
    private PagerSlidingTabStrip mSlidingTabStrip;
    private ViewPager mViewPager;
    private MainPagerAdapter mAdapter;
    private SystemBarTintManager mTintManager;
    private ArrayList<String> mForecastLocations;
    private ArrayList<String> mBuoyLocations;
    private ArrayList<String> mTideLocations;
    private ToolbarClickListener mToolbarClickListener;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPrefsChangedListener;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        // Get the toolbar and set it as the actionbar
        mToolbar = ( Toolbar ) findViewById( R.id.toolbar );
        setSupportActionBar( mToolbar );
        getSupportActionBar().setDisplayShowTitleEnabled( false );
        mToolbar.setTitle("Rhode Island");

        // SharedPreference setup, always set the buoys to start at block island
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
        String defaultBuoyLocation = sharedPrefs.getString( SettingsActivity.DEFAULT_BUOY_LOCATION_KEY,
                                     BuoyModel.BLOCK_ISLAND_LOCATION );
        sharedPrefs.edit().putString( SettingsActivity.BUOY_LOCATION_KEY, defaultBuoyLocation ).apply();

        // Set up the spinner locations
        initLocationArrays();

        // Create the system tint manager with this context
        mTintManager = new SystemBarTintManager( this );

        // Enable status bar tint
        mTintManager.setStatusBarTintEnabled( true );

        // Create and set the new pager adapter
        mViewPager = ( ViewPager ) findViewById( R.id.pager );
        mSlidingTabStrip = ( PagerSlidingTabStrip ) findViewById( R.id.tabs );
        mAdapter = new MainPagerAdapter( getSupportFragmentManager() );
        mViewPager.setAdapter( mAdapter );
        mSlidingTabStrip.setViewPager( mViewPager );
        mToolbarClickListener = new ToolbarClickListener();

        mSlidingTabStrip.setOnPageChangeListener( new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels ) {
                // Do nothing
            }

            @Override
            public void onPageSelected( int position ) {
                switch (position) {
                    case LIVE_PAGE_INDEX:
                    case FORECAST_PAGE_INDEX:
                        mToolbar.setTitle(mForecastLocations.get(0));
                        break;
                    case BUOY_PAGE_INDEX:
                        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences( getApplicationContext() );
                        String buoyLocation = sharedPrefs.getString( SettingsActivity.BUOY_LOCATION_KEY,
                                BuoyModel.BLOCK_ISLAND_LOCATION );
                        mToolbar.setTitle(buoyLocation);
                        break;
                    case TIDE_PAGE_INDEX:
                        mToolbar.setTitle(mTideLocations.get(0));
                }
            }

            @Override
            public void onPageScrollStateChanged( int state ) {
                // Do Nothing
            }
        } );

        // Set the background color of the tab strip, status bar, and action bar
        mSlidingTabStrip.setBackgroundColor( getResources().getColor( R.color.hackwinds_blue ) );
        mTintManager.setStatusBarTintColor( getResources().getColor( R.color.hackwinds_blue ) );
        mToolbar.setBackgroundColor( getResources().getColor( R.color.hackwinds_blue ) );

        mToolbar.setOnClickListener(mToolbarClickListener);

        mSharedPrefsChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if (mViewPager.getCurrentItem() == 2 && s.equals(SettingsActivity.BUOY_LOCATION_KEY)) {
                    mToolbar.setTitle(sharedPreferences.getString(SettingsActivity.BUOY_LOCATION_KEY,BuoyModel.BLOCK_ISLAND_LOCATION));
                }
            }
        };
        sharedPrefs.registerOnSharedPreferenceChangeListener(mSharedPrefsChangedListener);
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch ( item.getItemId() ) {
            case R.id.action_settings:
                startActivity( new Intent( this, SettingsActivity.class ) );
                break;
            default:
                return super.onOptionsItemSelected( item );
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        ForecastModel.getInstance( this ).fetchForecastData();
        BuoyModel.getInstance( this ).fetchBuoyData();
        TideModel.getInstance( this ).fetchTideData();
        CameraModel.getInstance( this ).fetchCameraURLs();
    }

    public void initLocationArrays() {
        mForecastLocations = new ArrayList<>();
        mForecastLocations.add( "Rhode Island" );

        mBuoyLocations = new ArrayList<>();
        mBuoyLocations.add(BuoyModel.BLOCK_ISLAND_LOCATION);
        mBuoyLocations.add(BuoyModel.MONTAUK_LOCATION);
        mBuoyLocations.add(BuoyModel.NANTUCKET_LOCATION);
        mBuoyLocations.add(BuoyModel.LONG_ISLAND_LOCATION);
        mBuoyLocations.add(BuoyModel.TEXAS_TOWER_LOCATION);

        mTideLocations = new ArrayList<>();
        mTideLocations.add( "Point Judith Harbor" );
    }

    public class MainPagerAdapter extends FragmentPagerAdapter {

        public MainPagerAdapter( FragmentManager fm ) {
            super( fm );
        }

        @Override
        public CharSequence getPageTitle( int position ) {
            // get the title for each of the tabs
            Locale l = Locale.getDefault();
            switch ( position ) {
                case LIVE_PAGE_INDEX:
                    return getString( R.string.action_live ).toUpperCase( l );
                case FORECAST_PAGE_INDEX:
                    return getString( R.string.action_forecast ).toUpperCase( l );
                case BUOY_PAGE_INDEX:
                    return getString( R.string.action_buoy ).toUpperCase( l );
                case TIDE_PAGE_INDEX:
                    return getString( R.string.action_tide ).toUpperCase( l );
            }
            return null;
        }

        @Override
        public int getCount() {
            // We have 4 pages
            return 4;
        }

        @Override
        public Fragment getItem( int position ) {
            switch ( position ) {
                case LIVE_PAGE_INDEX:
                    // First tab was clicked, return Live fragment
                    return new CurrentFragment();
                case FORECAST_PAGE_INDEX:
                    // Second is the Forecast fragment
                    return new ForecastFragment();
                case BUOY_PAGE_INDEX:
                    // Then the Buoy Fragment
                    return new BuoyFragment();
                case TIDE_PAGE_INDEX:
                    // Lastly the Tide Fragment
                    return new TideFragment();
            }
            return null;
        }
    }

    public class ToolbarClickListener implements View.OnClickListener {

        public ToolbarClickListener () {
        }

        @Override
        public void onClick(View v) {
            int currentPageIndex = mViewPager.getCurrentItem();
            switch(currentPageIndex) {
                case LIVE_PAGE_INDEX:
                case FORECAST_PAGE_INDEX:
                    showModelInfoDialog();
                    break;
                case BUOY_PAGE_INDEX:
                    showBuoyLocationPickerDialog();
                    break;
                default:
                    break;
            }
        }

        private void showModelInfoDialog() {
            ModelInfoBottomSheetFragment modelInfoBottomSheetFragment = new ModelInfoBottomSheetFragment();
            modelInfoBottomSheetFragment.show(getSupportFragmentManager(), "Model Info");
        }

        private void showBuoyLocationPickerDialog() {
            BuoyPickerBottomSheetDialogFragment buoyPickerBottomSheetDialogFragment = new BuoyPickerBottomSheetDialogFragment();
            buoyPickerBottomSheetDialogFragment.show(getSupportFragmentManager(), "Buoy Picker");
        }

    }
}
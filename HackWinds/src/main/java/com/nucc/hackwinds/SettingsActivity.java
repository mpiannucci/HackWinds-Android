package com.nucc.hackwinds;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class SettingsActivity extends ActionBarActivity {

    public static final String FORECAST_LOCATION_KEY = "forecastLocation";
    public static final String RATE_APP_KEY = "rateApp";
    public static final String CONTACT_DEV_KEY = "contactDeveloper";
    public static final String SHOW_DISCLAIMER_KEY = "aboutDisclaimer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Load the preference fragment
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new HackWindsPreferenceFragment()).commit();
    }

    public static class HackWindsPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Initialize the settings view and set it to the default values on first launch
            addPreferencesFromResource(R.xml.main_settings);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.main_settings, false);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            LinearLayout view = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);

            // Get the shared preferences
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            // Set the summary of the location preference to be the current location
            Preference forecastLocationPref = findPreference(FORECAST_LOCATION_KEY);
            forecastLocationPref.setSummary(sharedPrefs.getString(FORECAST_LOCATION_KEY, getResources().getStringArray(R.array.mswForecastLocations)[0]));

            // Callbacks to the preference clicks
            // First is the call to send the user Google Play to rate the app
            Preference googlePlayPref = findPreference(RATE_APP_KEY);
            googlePlayPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
                    Intent goToGooglePlay = new Intent(Intent.ACTION_VIEW, uri);
                    goToGooglePlay.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToGooglePlay);
                    } catch (ActivityNotFoundException e) {
                        // If there's no market take them to the website
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
                    }
                    return false;
                }
            });

            // Next we want to send an email intent so they can contact me
            Preference contactPref = findPreference(CONTACT_DEV_KEY);
            contactPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto","rhodysurf13@gmail.com", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "HackWinds for Android");
                    startActivity(Intent.createChooser(emailIntent, "Email the developer"));
                    return false;
                }
            });

            // Last we want to show a quick dialog with the apps disclaimer
            Preference disclaimerPref = findPreference(SHOW_DISCLAIMER_KEY);
            disclaimerPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Disclaimer")
                            .setMessage(R.string.discalimer_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(R.drawable.ic_launcher)
                            .show();
                    return false;
                }
            });

            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
            // Set up a listener whenever a key changes
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            // Unregister the listener whenever a key changes
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(FORECAST_LOCATION_KEY)) {
                // If the location was changed set the summary to the new location
                Preference forecastLocationPref = findPreference(key);
                forecastLocationPref.setSummary(sharedPreferences.getString(key, getResources().getStringArray(R.array.mswForecastLocations)[0]));
            }
        }
    }
}

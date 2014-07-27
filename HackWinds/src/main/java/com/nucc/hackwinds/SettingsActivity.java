package com.nucc.hackwinds;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsActivity extends Activity {
    //private OnSharedPreferenceChangeListener listener;


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        /*
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Use instance field for listener
        // It will not be gc'd as long as this instance is kept referenced
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
          public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

          }
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);
        */
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

		@Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            Preference myPref = findPreference("pref_key_disclaimer");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                    alertBuilder.setTitle(R.string.pref_disclaimer_title);
                    alertBuilder.setMessage("I do not own nor claim to own the camera images or the " +
                            "forecast data. The camera images are courtesy of Warm Winds Surf Shop and warmwinds.com. " +
                            "The swell information displayed is courtesy of MagicSeaweed, Swellinfo, Wunderground, " +
                            "and NOAA.");
                    alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertBuilder.setIcon(R.drawable.ic_launcher);
                    AlertDialog disclaimerAlert = alertBuilder.create();
                    disclaimerAlert.show();
                    return true;
                }
            });
        }
    }
}
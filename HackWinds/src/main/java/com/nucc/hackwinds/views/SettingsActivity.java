package com.nucc.hackwinds.views;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.CameraModel;


public class SettingsActivity extends AppCompatActivity {

    public static final String FORECAST_LOCATION_KEY = "forecastLocation";
    public static final String DEFAULT_BUOY_LOCATION_KEY = "defaultBuoyLocation";
    public static final String BUOY_LOCATION_KEY = "buoyLocation";
    public static final String TIDE_LOCATION_KEY = "tideLocation";
    public static final String SHOW_PREMIUM_CONTENT_KEY = "showPremiumContent";
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
            final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

            // Set the summary of the buoy preference to be the current location
            Preference defaultBuoyLocationPref = findPreference(DEFAULT_BUOY_LOCATION_KEY);
            defaultBuoyLocationPref.setSummary(sharedPrefs.getString(DEFAULT_BUOY_LOCATION_KEY, getResources().getStringArray(R.array.buoyLocations)[1]));

            // Callbacks to the preference clicks
            final Preference showPremiumPref = findPreference(SHOW_PREMIUM_CONTENT_KEY);
            Boolean premiumContentEnabled = sharedPrefs.getBoolean(SHOW_PREMIUM_CONTENT_KEY, false);
            if (premiumContentEnabled) {
                showPremiumPref.setSummary(R.string.pref_premium_content_summary_enabled);
            } else {
                showPremiumPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        View editView = LayoutInflater.from(getActivity()).inflate(R.layout.premium_code_dialog, null);
                        final EditText codeInput = (EditText) editView.findViewById(R.id.activation_code_input);
                        android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(getActivity(), R.style.Theme_AlertDialog);
                        alertBuilder.setMessage("Enter the access code to activate premium content");
                        alertBuilder.setView(editView);
                        alertBuilder.setPositiveButton("Activate", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Check if the code matches. If you are looking at this, its your lucky day cuz you now know the code lol
                                String input = codeInput.getText().toString();
                                if (input.equals("109485")) {
                                    sharedPrefs.edit().putBoolean(SHOW_PREMIUM_CONTENT_KEY, true).apply();
                                }
                            }
                        });
                        alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Do nothing for a cancel
                            }
                        });

                        alertBuilder.show();

                        return false;
                    }
                });
            }

            // Add the call to send the user Google Play to rate the app
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
                    new AlertDialog.Builder(getActivity(), R.style.Theme_AlertDialog)
                            .setTitle("Disclaimer")
                            .setMessage(R.string.discalimer_message)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(R.mipmap.ic_launcher)
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
            if (key.equals(DEFAULT_BUOY_LOCATION_KEY)) {
                Preference defaultBuoyLocationPref = findPreference(key);
                defaultBuoyLocationPref.setSummary(sharedPreferences.getString(key, getResources().getStringArray(R.array.buoyLocations)[1]));
            } else if (key.equals(SHOW_PREMIUM_CONTENT_KEY)) {
                Preference showPremiumPref = findPreference(key);
                if (sharedPreferences.getBoolean(key, false)) {
                    showPremiumPref.setSummary(R.string.pref_premium_content_summary_enabled);
                    showPremiumPref.setOnPreferenceClickListener(null);
                    CameraModel cameraModel = CameraModel.getInstance(getActivity());
                    cameraModel.reset();
                    cameraModel.forceFetchCameraURLs();
                }
            }
        }
    }
}

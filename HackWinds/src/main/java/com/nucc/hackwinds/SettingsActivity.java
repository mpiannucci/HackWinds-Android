package com.nucc.hackwinds;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction().replace(R.id.content_frame, new HackWindsPreferenceFragment()).commit();
    }

    public static class HackWindsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main_settings);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.main_settings, false);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            LinearLayout view = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);

            // Callbacks to the preference clicks
            // First is the call to send the user Google Play to rate the app
            Preference googlePlayPref = findPreference("rateApp");
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
            Preference contactPref = findPreference("contactDeveloper");
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
            Preference disclaimerPref = findPreference("aboutDisclaimer");
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


    }
}

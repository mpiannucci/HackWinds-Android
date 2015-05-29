package com.nucc.hackwinds.views;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.nucc.hackwinds.R;

import java.util.TooManyListenersException;

public class AlternateCameraActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alternate_cameras);

        // Set up the toolbar
        resetToolbarTitle();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Load the preference fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new AlternateCameraListFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    // If there's fragments in the back stack, switch the fragment
                    // instead of the parent activity
                    fragmentManager.popBackStackImmediate();
                    return true;
                }
        }

        return super.onOptionsItemSelected(item);
    }

    public void setToolbarTitle(String title) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
    }

    public void resetToolbarTitle() {
        setToolbarTitle("Alternate Cameras");
    }

}

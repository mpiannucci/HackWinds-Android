package com.nucc.hackwinds;

import android.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

public class StillActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.still_activity);

    }
}

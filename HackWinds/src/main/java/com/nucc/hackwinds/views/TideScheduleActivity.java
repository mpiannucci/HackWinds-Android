package com.nucc.hackwinds.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.TideScheduleArrayAdapter;
import com.nucc.hackwinds.models.TideModel;
import com.nucc.hackwinds.types.Tide;

import java.util.ArrayList;

/**
 * Created by matthew on 4/21/16.
 */
public class TideScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tide_schedule);

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up the tide list
        ArrayList<Tide> tides = TideModel.getInstance(this).tides;
        TideScheduleArrayAdapter tideAdapter = new TideScheduleArrayAdapter(this, tides);
        ListView tideScheduleList = (ListView) findViewById(R.id.tide_schedule_list);
        tideScheduleList.setAdapter(tideAdapter);
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
}

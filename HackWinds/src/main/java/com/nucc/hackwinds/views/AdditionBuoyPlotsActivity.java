package com.nucc.hackwinds.views;


import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.BuoyModel;

public class AdditionBuoyPlotsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_buoy_plots);

        // Set up the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Additional Plots");
        setSupportActionBar(toolbar);

        // Get the plot URLs from the model
        String spectralDensityPlotURL = BuoyModel.getInstance(getApplicationContext()).getSpectraPlotURL();

        // Load the images for the plots
        ImageView spectralDensityImage = (ImageView) findViewById(R.id.sprectral_density_plot);
        Ion.with(this).load(spectralDensityPlotURL).intoImageView(spectralDensityImage);
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

package com.nucc.hackwinds;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.GregorianCalendar;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * Created by matthew on 10/30/14.
 */
public class BuoyFragment extends ListFragment {

    // URLs
    private String BI_URL = "http://www.ndbc.noaa.gov/data/realtime2/44097.txt";
    private String MTK_URL = "http://www.ndbc.noaa.gov/data/realtime2/44017.txt";

    // Data constants
    private int DATA_POINTS = 20;
    private int DATA_HEADER_LENGTH = 38;
    private int DATA_LINE_LEN = 19;
    private int HOUR_OFFSET = 3;
    private int MINUTE_OFFSET = 4;
    private int WVHT_OFFSET = 8;
    private int DPD_OFFSET = 9;
    private int DIRECTION_OFFSET = 11;
    private int BI_LOCATION = 41;
    private int MTK_LOCATION = 42;

    private double hour_offset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.buoy_fragment, container, false);

        // Initialize it to Block Island
        getData(BI_LOCATION);

        // Set the segment control to block island
        RadioButton biButton = (RadioButton) V.findViewById(R.id.biButton);
        biButton.setChecked(true);

        // Set the tint of the segment control
        SegmentedGroup locationGroup = (SegmentedGroup) V.findViewById(R.id.segmentedBuoy);
        locationGroup.setTintColor(getResources().getColor(R.color.jblue));

        // Set the time offset vaiable so the times are correct
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();
        int mGMTOffset = mTimeZone.getRawOffset();
        hour_offset = TimeUnit.HOURS.convert(mGMTOffset, TimeUnit.MILLISECONDS);

        // Set the listener for the segment group radio change
        locationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.biButton) {
                    // Switch to block island view so get that data
                    getData(BI_LOCATION);
                } else {
                    // Switch to Montauk view and get that data
                    getData(MTK_LOCATION);
                }
            }
        });

        return V;
    }

    public void getData(int location) {
        String URL;
        if (location == BI_LOCATION) {
            URL = BI_URL;
        } else {
            URL = MTK_URL;
        }

        Ion.with(getActivity()).load(URL).asString().setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String result) {
                // download done...
                // Parse the data that was received
                ArrayList<Buoy> buoys = parseData(result);

                // Set the list adapter
                BuoyArrayAdapter buoyAdapter = new BuoyArrayAdapter(getActivity(), buoys);
                setListAdapter(buoyAdapter);
            }
        });
    }

    public ArrayList<Buoy> parseData(String rawData) {
        // Split by the whitespace in the string
        String[] datas = rawData.split("\\s+");

        // Make an array for the buoy data
        ArrayList<Buoy> buoyDatas = new ArrayList<Buoy>();

        // Loop through the data and make new buoy objects to add to the list
        for(int i=DATA_HEADER_LENGTH; i<(DATA_HEADER_LENGTH+(DATA_LINE_LEN*DATA_POINTS)); i+=DATA_LINE_LEN) {
            // Create a new buoy object
            Buoy thisBuoy = new Buoy();

            // Set the time
            thisBuoy.time = String.format("%d:%s", (Integer.valueOf(datas[i + HOUR_OFFSET])+(int)hour_offset+12)%12, datas[i + MINUTE_OFFSET]);

            // Set the period and wind direction values
            thisBuoy.dpd = datas[i+DPD_OFFSET];
            thisBuoy.dir = datas[i+DIRECTION_OFFSET];

            // Convert and set the wave height
            String wv = datas[i+WVHT_OFFSET];
            double h = Double.valueOf(wv) * 3.28;
            thisBuoy.wvht = String.format("%4.2f", h);

            // Save the buoy object to the list
            buoyDatas.add(thisBuoy);
        }

        // Return the list of buoy objects
        return buoyDatas;
    }

}

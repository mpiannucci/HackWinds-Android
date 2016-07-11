package com.nucc.hackwinds.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.listeners.LatestBuoyFetchListener;
import com.nucc.hackwinds.listeners.TideChangedListener;
import com.nucc.hackwinds.types.Buoy;
import com.nucc.hackwinds.types.Tide;
import com.nucc.hackwinds.models.BuoyModel;
import com.nucc.hackwinds.models.TideModel;

import java.util.ArrayList;
import java.util.Calendar;


public class TideFragment extends Fragment implements TideChangedListener, LatestBuoyFetchListener {
    private TideModel mTideModel;
    private String mBuoyLocation = BuoyModel.NEWPORT_LOCATION;
    private String mWaterTemp;
    private boolean mBuoyFailed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the menu options
        setHasOptionsMenu(true);

        mTideModel = TideModel.getInstance(getActivity());
        mTideModel.addTideChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.tide_fragment, container, false);

        // Setup the tide chart
        LineChart tideChart = (LineChart) V.findViewById(R.id.tide_chart);
        tideChart.setDrawBorders(false);
        tideChart.setDescription("");
        tideChart.setPinchZoom(false);
        tideChart.setDoubleTapToZoomEnabled(false);
        tideChart.setDrawMarkerViews(false);
        tideChart.setTouchEnabled(false);
        tideChart.setViewPortOffsets(0f, 0f, 0f, 0f);

        // X Axis
        XAxis xAxis = tideChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);

        // Y Axis
        YAxis leftYAxis = tideChart.getAxisLeft();
        YAxis rightYAxis = tideChart.getAxisRight();
        leftYAxis.setDrawGridLines(false);
        rightYAxis.setDrawGridLines(false);
        leftYAxis.setDrawAxisLine(false);
        rightYAxis.setDrawAxisLine(false);
        leftYAxis.setDrawLabels(false);
        rightYAxis.setDrawLabels(false);
        leftYAxis.setDrawZeroLine(false);
        rightYAxis.setDrawZeroLine(false);

        // Legend
        tideChart.getLegend().setEnabled(false);

        return V;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.tide_menu_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_tide_schedule:
                startActivity(new Intent(getActivity(), TideScheduleActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        tideDataUpdated();
        reloadWaterTemperature();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void tideDataUpdated() {
        if (mTideModel.tides.isEmpty()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateTideChart();
                updateOtherEventCard();
            }
        });
    }

    @Override
    public void tideDataUpdateFailed() {
        // For now do nothing
    }

    @Override
    public void latestBuoyFetchSuccess(final Buoy latestBuoy) {

        if (latestBuoy == null) {
            return;
        }

        if (mWaterTemp == null) {

            if (latestBuoy.waterTemperature == null) {
                return;
            }

            if (latestBuoy.waterTemperature.isEmpty()) {
                return;
            }

            mWaterTemp = latestBuoy.waterTemperature;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateWaterTempCard();
            }
        });
    }

    @Override
    public void latestBuoyFetchFailed() {
        if (!mBuoyFailed) {
            mBuoyFailed = true;
            reloadWaterTemperature();
        }
    }

    public void reloadWaterTemperature() {
        if (mBuoyFailed) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            mBuoyLocation = sharedPrefs.getString(SettingsActivity.DEFAULT_BUOY_LOCATION_KEY, BuoyModel.MONTAUK_LOCATION);
        }
        // Fetch the data from the models
        BuoyModel.getInstance(getActivity()).fetchLatestBuoyReadingForLocation(mBuoyLocation, this);
    }

    public void updateTideChart() {
        if (mTideModel == null) {
            return;
        } else if (mTideModel.tides == null) {
            return;
        }

        if (mTideModel.tides.size() < 5) {
            return;
        }

        LineChart tideChart = (LineChart) getActivity().findViewById(R.id.tide_chart);
        if (tideChart == null) {
            return;
        }

        tideChart.clear();
        tideChart.getXAxis().removeAllLimitLines();

        // Get some colors
        int backgroundGrey = getResources().getColor(R.color.cardview_light_background);
        int hackWindsBlue = getResources().getColor(R.color.hackwinds_blue);
        int alternateBlue = getResources().getColor(R.color.accent_blue);
        int holoOrange = getResources().getColor(android.R.color.holo_orange_dark);

        // Some variables for introspection
        float min = 0;
        float max = 0;
        int firstIndex = 0;
        boolean highFirst = false;
        int prevIndex = 0;
        int tideCount = 0;
        int index = 0;

        Calendar c = Calendar.getInstance();
        long currentHourSeconds = ((c.getTimeInMillis()/1000) / 3600) * 3600;

        ArrayList<Entry> dataEntries = new ArrayList<>();

        // Create the first entry to hold its spot
        Entry firstEntry = new Entry(0, -1);
        dataEntries.add(firstEntry);

        while (tideCount < 5) {
            if (index > mTideModel.tides.size()) {
                return;
            }

            Tide thisTide = mTideModel.tides.get(index);
            index++;

            if (thisTide == null) {
                continue;
            }

            if (!thisTide.isTidalEvent()) {
                continue;
            }

            int xIndex = 0;
            if (tideCount == 0) {
                long interval = currentHourSeconds - (thisTide.timestamp.getTime()/1000);
                firstIndex = (int)(Math.abs(interval / (60*60)));
                xIndex = firstIndex;

                if (thisTide.isHighTide()) {
                    highFirst = true;
                } else {
                    highFirst = false;
                }
            } else {
                xIndex = prevIndex + 6;
            }

            float value = thisTide.heightValue;
            if (value < 0) {
                value = 0.01f;
            }

            if (tideCount < 2) {
                if (thisTide.isHighTide()) {
                    max = value;
                } else {
                    min = value;
                }
            }

            if (xIndex != 0) {
                Entry thisEntry = new Entry(value, xIndex);
                dataEntries.add(thisEntry);
            } else {
                firstEntry.setVal(value);
            }

            if (xIndex < 24 || tideCount < 4) {
                LimitLine tideLimit = new LimitLine(xIndex, thisTide.getTimeString());
                tideLimit.setTextSize(16);
                tideLimit.setLineWidth(2);
                if (xIndex > 16) {
                    tideLimit.setLineColor(hackWindsBlue);
                    tideLimit.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
                } else {
                    tideLimit.setLineColor(backgroundGrey);
                    tideLimit.setTextColor(backgroundGrey);
                    tideLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                }
                tideChart.getXAxis().addLimitLine(tideLimit);
            }
            prevIndex = xIndex;
            tideCount++;
        }

        float amplitude = max - min;
        if (firstIndex != 0) {
            if (firstIndex == 6) {
                LimitLine tideLimit = new LimitLine(0);
                tideLimit.setLineColor(backgroundGrey);
                tideLimit.setTextColor(backgroundGrey);
                tideLimit.setTextSize(16);
                tideLimit.setLineWidth(7);
                tideLimit.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
                firstEntry.setXIndex(0);
                if (highFirst) {
                    firstEntry.setVal(min);
                    tideLimit.setLabel("Low Tide");
                } else {
                    firstEntry.setVal(max);
                    tideLimit.setLabel("High Tide");
                }
                tideChart.getXAxis().addLimitLine(tideLimit);
            } else {
                if (highFirst) {
                    float approxMax = (float) (max - (amplitude * ((float)(firstIndex + 1) / 6.0)));
                    if (approxMax < 0) {
                        approxMax = 0.01f;
                    }
                    firstEntry.setVal(approxMax);
                } else {
                    float approxMin = (float)((amplitude * (((float)firstIndex + 1) / 6.0)) + min);
                    if (approxMin < 0) {
                        approxMin = 0.01f;
                    }
                    firstEntry.setVal(approxMin);
                }
            }
        } else {
            firstEntry.setXIndex(0);
        }

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < prevIndex; i++) {
            xVals.add("");
        }

        LineDataSet dataSet = new LineDataSet(dataEntries, "Tide Heights");
        dataSet.setDrawCircles(false);
        dataSet.setCircleColor(holoOrange);
        dataSet.setCircleColorHole(holoOrange);
        dataSet.setCircleRadius(8.0f);
        dataSet.setColor(hackWindsBlue);
        dataSet.setFillColor(hackWindsBlue);
        dataSet.setFillAlpha(255);
        dataSet.setDrawFilled(true);
        dataSet.setLineWidth(2.0f);
        dataSet.setDrawCubic(true);

        LineData chartData = new LineData(xVals, dataSet);
        chartData.setDrawValues(false);

        // Draw a limit line at now
        LimitLine nowLimit = new LimitLine(0);
        nowLimit.setLabel("Now");
        nowLimit.setTextSize(16);
        nowLimit.setLineColor(alternateBlue);
        nowLimit.setLineWidth(7.0f);
        tideChart.getXAxis().addLimitLine(nowLimit);

        // Adjust y axis'
        tideChart.getAxisLeft().setAxisMaxValue(max + 1.0f);
        tideChart.getAxisRight().setAxisMaxValue(max + 1.0f);
        tideChart.getAxisLeft().setAxisMinValue(min - 1.0f);
        tideChart.getAxisRight().setAxisMinValue(min - 1.0f);

        tideChart.setData(chartData);
    }

    public void updateOtherEventCard() {
        if (mTideModel.otherEvents.size() < 2) {
            return;
        }

        // For now there are always two events so no need for a list. Get all of the views!!
        TextView firstEventTypeText = (TextView) getActivity().findViewById(R.id.sun_first_event_type);
        if (firstEventTypeText == null) {
            return;
        }
        TextView firstEventTimeText = (TextView) getActivity().findViewById(R.id.sun_first_event_time);
        if (firstEventTimeText == null) {
            return;
        }
        ImageView firstEventIcon = (ImageView) getActivity().findViewById(R.id.sun_first_icon);
        if (firstEventIcon == null) {
            return;
        }
        TextView secondEventTypeText = (TextView) getActivity().findViewById(R.id.sun_second_event_type);
        if (secondEventTypeText == null) {
            return;
        }
        TextView secondEventTimeText = (TextView) getActivity().findViewById(R.id.sun_second_event_time);
        if (secondEventTimeText == null) {
            return;
        }
        ImageView secondEventIcon = (ImageView) getActivity().findViewById(R.id.sun_second_icon);
        if (secondEventIcon == null) {
            return;
        }

        Drawable sunriseDrawable = getResources().getDrawable(R.drawable.ic_brightness_high_white_36dp);
        Drawable sunsetDrawable = getResources().getDrawable(R.drawable.ic_brightness_low_white_36dp);

        // Fill the data!
        Tide firstEvent = mTideModel.otherEvents.get(0);
        firstEventTypeText.setText(firstEvent.eventType);
        firstEventTimeText.setText(firstEvent.getTimeString());
        if (firstEvent.isSunrise()) {
            firstEventIcon.setImageDrawable(sunriseDrawable);
        } else {
            firstEventIcon.setImageDrawable(sunsetDrawable);
        }

        Tide secondEvent = mTideModel.otherEvents.get(1);
        secondEventTypeText.setText(secondEvent.eventType);
        secondEventTimeText.setText(secondEvent.getTimeString());
        if (secondEvent.isSunrise()) {
            secondEventIcon.setImageDrawable(sunriseDrawable);
        } else {
            secondEventIcon.setImageDrawable(sunsetDrawable);
        }
    }

    public void updateWaterTempCard() {
        // Update the water temperature from the latest buoy reading
        TextView buoyLocationTV = (TextView) getActivity().findViewById(R.id.water_temp_location);
        if (buoyLocationTV != null) {
            buoyLocationTV.setText(mBuoyLocation);
        }

        TextView waterTemp = (TextView) getActivity().findViewById(R.id.water_temp_value);
        if (waterTemp != null) {
            String waterTempValue = mWaterTemp + " " + getResources().getString(R.string.water_temp_holder);
            waterTemp.setText(waterTempValue);
        }

        ImageView waterTempIcon = (ImageView) getActivity().findViewById(R.id.water_temp_icon);

        int tempColorTint = getResources().getColor(android.R.color.holo_purple);
        double waterTempValueD = Double.valueOf(mWaterTemp);
        if (waterTempValueD < 43) {
            // Its purple do nothing
        } else if (waterTempValueD < 50) {
            tempColorTint = getResources().getColor(R.color.accent_blue);
        } else if (waterTempValueD < 60) {
            tempColorTint = getResources().getColor(android.R.color.holo_green_dark);
        } else if (waterTempValueD < 70) {
            tempColorTint = getResources().getColor(android.R.color.holo_orange_dark);
        } else {
            tempColorTint = getResources().getColor(android.R.color.holo_red_dark);
        }

        if (waterTempIcon != null) {
            waterTempIcon.setColorFilter(tempColorTint);
        }
    }
}

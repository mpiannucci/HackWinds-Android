package com.nucc.hackwinds;


import android.app.ActionBar;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import info.hoang8f.android.segmented.SegmentedGroup;


public class DetailedForecastFragment extends Fragment implements SegmentedGroup.OnCheckedChangeListener{
    private enum ChartType {
        SWELL,
        WIND,
        PERIOD
    }

    // TODO: Rename and change types of parameters
    private int mDayIndex;
    private String mDayName;

    private ConditionModel mConditionModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the condition model
        if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            // Get the buoy model
            mConditionModel = ConditionModel.getInstance(getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.detailed_forecast_fragment, container, false);

        // Get the Segmented widget
        SegmentedGroup chartTypeGroup = (SegmentedGroup) V.findViewById(R.id.segmentedChart);
        chartTypeGroup.setOnCheckedChangeListener(this);

        // Set the tint color of the segmented group
        chartTypeGroup.setTintColor(getResources().getColor(R.color.hackwinds_blue));

        return V;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Show the swell chart when the fragment is launched
        RadioButton swellButton = (RadioButton) getActivity().findViewById(R.id.swellSegmentButton);
        swellButton.setChecked(true);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int index) {
        setChartImageForIndex(index);
    }

    public void setChartImageForIndex(int index) {
        String chartURL = "";
        switch (index) {
            case R.id.swellSegmentButton:
                chartURL = mConditionModel.getConditions(1).get(0).SwellChartURL;
                break;
            case R.id.windSegmentButton:
                chartURL = mConditionModel.getConditions(1).get(0).WindChartURL;
                break;
            case R.id.periodSegmentButton:
                chartURL = mConditionModel.getConditions(1).get(0).PeriodChartURL;
                break;
            default:
                // Do nothing
                return;
        }
        // Initialize the chart view to be the first swell image from the condition model
        ImageView chartImage = (ImageView) getActivity().findViewById(R.id.chartPreviewImage);
        if (chartURL.length() > 0) {
            Ion.with(chartImage).load(chartURL);
        } else {
            return;
        }

    }

    public class LoadAnimationImagesTask extends AsyncTask<ChartType, Void, Void> {

        @Override
        protected Void doInBackground(ChartType... arg0) {

            // Return
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }
}

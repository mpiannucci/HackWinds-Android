package com.nucc.hackwinds.views;


import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.ConditionArrayAdapter;
import com.nucc.hackwinds.models.ForecastModel;
import com.nucc.hackwinds.types.Forecast;
import com.nucc.hackwinds.utilities.ReachabilityHelper;

import java.util.ArrayList;
import java.util.Locale;

import info.hoang8f.android.segmented.SegmentedGroup;


public class DetailedForecastFragment extends ListFragment {
    private final int ANIMATION_DURATION = 500;
    private final int WAVE_WATCH_HOUR_STEP = 3;

    public enum ForecastChartType {
        WAVES,
        WIND,
        PERIOD
    }
    public ForecastChartType chartType;

    private ForecastModel mForecastModel;

    private int mDayIndex;
    private AnimationDrawable mChartAnimation;
    private FutureCallback<Bitmap> mChartLoadCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mForecastModel = ForecastModel.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.detailed_forecast_fragment, container, false);

        // Get the condition model for the given day
        mDayIndex = getArguments().getInt("dayIndex");

        // Hide the play button
        ImageView chartPlayButton = (ImageView) V.findViewById(R.id.forecast_animate_play_button);
        chartPlayButton.setVisibility(View.GONE);
        chartPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hide the play button
                view.setVisibility(View.GONE);

                // Start the animation
                mChartAnimation.start();
            }
        });

        // Set clicking the imageview to act as a pause button
        ImageView chartImage = (ImageView) V.findViewById(R.id.forecast_chart_image);
        chartImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChartAnimation.isRunning()) {
                    // Only call to stop the animation if it is currently running
                    mChartAnimation.stop();

                    // Show the play button
                    ImageView playButton = (ImageView) getActivity().findViewById(R.id.forecast_animate_play_button);
                    playButton.setVisibility(View.VISIBLE);
                }
            }
        });

        // Create the chart animation instance
        mChartAnimation = new AnimationDrawable();

        // Create chart loading callback
        mChartLoadCallback = new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(Exception e, Bitmap result) {
                if (result == null) {
                    ImageView chartImage = (ImageView) getActivity().findViewById(R.id.forecast_chart_image);
                    chartImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.photo_loading_error));
                    chartImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    return;
                }

                Bitmap croppedBitmap = Bitmap.createBitmap(result, 60, 50, 350, 250);
                BitmapDrawable chartFrame = new BitmapDrawable(getResources(), croppedBitmap);
                mChartAnimation.addFrame(chartFrame, ANIMATION_DURATION);

                int nFrames = mChartAnimation.getNumberOfFrames();
                if (nFrames == 1) {
                    // Set the chart preview image as bitmap that was just received
                    ImageView chartImage = (ImageView) getActivity().findViewById(R.id.forecast_chart_image);
                    chartImage.setImageDrawable(chartFrame);

                } else if (nFrames == 6) {
                    // Set the animation drawable as the imageview background
                    ImageView chartImage = (ImageView) getActivity().findViewById(R.id.forecast_chart_image);
                    chartImage.setImageDrawable(mChartAnimation);

                    // Show the play button
                    ImageView playButton = (ImageView) getActivity().findViewById(R.id.forecast_animate_play_button);
                    playButton.setVisibility(View.VISIBLE);
                }

                if (nFrames < 6) {
                    // Load the next image
                    getChartImageForIndex(chartType, nFrames);
                }
            }
        };

        return V;
    }

    @Override
    public void onResume() {
        super.onResume();

        loadImages();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mChartAnimation.isRunning()) {
            // Stop the animation if it is still running
            mChartAnimation.stop();
        }
    }

    public void loadImages() {

        if (mChartAnimation.isRunning()) {
            mChartAnimation.stop();
        }

        // Remove the animation from the imageview
        getActivity().findViewById(R.id.forecast_chart_image).setBackground(null);

        // Reset the chart animation object
        mChartAnimation = new AnimationDrawable();
        mChartAnimation.setOneShot(false);
        getChartImageForIndex(chartType, 0);
    }

    public void getChartImageForIndex(ForecastChartType forecastChartType, int index) {
        final String BASE_URL = "http://polar.ncep.noaa.gov/waves/WEB/multi_1.latest_run/plots/US_eastcoast.%s.%s%03dh.png";
        final String PAST_HOUR_TIME_PREFIX = "h";
        final String FUTURE_HOUR_TIME_PREFIX = "f";

        // Format the url for the next image load
        final String chartTimePrefix;
        if ((index == 0) && (mDayIndex == 0)) {
            chartTimePrefix = PAST_HOUR_TIME_PREFIX;
        } else {
            chartTimePrefix = FUTURE_HOUR_TIME_PREFIX;
        }
        final String chartTypePrefix = getChartURLPrefix(forecastChartType);
        final String nextImageURL = String.format(Locale.US, BASE_URL, chartTypePrefix, chartTimePrefix, (mForecastModel.getDayForecastStartingIndex(mDayIndex) + index) * WAVE_WATCH_HOUR_STEP);

        // Load the next image
        Ion.with(this).load(nextImageURL).asBitmap().setCallback(mChartLoadCallback);
    }

    private String getChartURLPrefix(ForecastChartType forecastChartType) {
        switch (forecastChartType) {
            case WAVES:
                return "hs";
            case PERIOD:
                return "tp_sw1";
            case WIND:
                return "u10";
            default:
                return "";
        }
    }

}

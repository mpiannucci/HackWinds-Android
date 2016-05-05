package com.nucc.hackwinds.views;


import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.ForecastModel;

import java.util.Locale;


public class DetailedForecastChartFragment extends Fragment {
    private final int ANIMATION_DURATION = 500;
    private final int WAVE_WATCH_HOUR_STEP = 3;

    public enum ForecastChartType {
        WAVES,
        WIND,
        PERIOD
    }

    public ForecastChartType chartType = ForecastChartType.WAVES;
    public int dayIndex = 0;

    private ForecastModel mForecastModel;
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
        View V = inflater.inflate(R.layout.detailed_forecast_chart_fragment, container, false);
        V.setId(chartType.ordinal());

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
                    ImageView playButton = (ImageView) getView().findViewById(R.id.forecast_animate_play_button);
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
                    ImageView chartImage = (ImageView) getView().findViewById(R.id.forecast_chart_image);
                    chartImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.photo_loading_error));
                    chartImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    return;
                }

                Bitmap croppedBitmap = Bitmap.createBitmap(result, 60, 50, 350, 225);
                BitmapDrawable chartFrame = new BitmapDrawable(getResources(), croppedBitmap);
                mChartAnimation.addFrame(chartFrame, ANIMATION_DURATION);

                int nFrames = mChartAnimation.getNumberOfFrames();
                if (nFrames == 1) {
                    // Set the chart preview image as bitmap that was just received
                    ImageView chartImage = (ImageView) getView().findViewById(R.id.forecast_chart_image);
                    chartImage.setImageDrawable(chartFrame);

                } else if (nFrames == 6) {
                    // Set the animation drawable as the imageview background
                    ImageView chartImage = (ImageView) getView().findViewById(R.id.forecast_chart_image);
                    chartImage.setImageDrawable(mChartAnimation);

                    // Show the play button
                    ImageView playButton = (ImageView) getView().findViewById(R.id.forecast_animate_play_button);
                    playButton.setVisibility(View.VISIBLE);
                }

                if (nFrames < 6) {
                    // Load the next image
                    getChartImageForIndex(nFrames);
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

    public int getDayIndex() {
        return dayIndex;
    }

    public void setDayIndex(int dayIndex) {
        this.dayIndex = dayIndex;
    }

    public ForecastChartType getChartType() {
        return chartType;
    }

    public void setChartType(ForecastChartType chartType) {
        this.chartType = chartType;
    }

    public void loadImages() {

        if (mChartAnimation.isRunning()) {
            mChartAnimation.stop();
        }

        // Remove the animation from the imageview
        getView().findViewById(R.id.forecast_chart_image).setBackground(null);

        // Reset the chart animation object
        mChartAnimation = new AnimationDrawable();
        mChartAnimation.setOneShot(false);
        getChartImageForIndex(0);
    }

    public void getChartImageForIndex(int index) {
        final String BASE_URL = "http://polar.ncep.noaa.gov/waves/WEB/multi_1.latest_run/plots/US_eastcoast.%s.%s%03dh.png";
        final String PAST_HOUR_TIME_PREFIX = "h";
        final String FUTURE_HOUR_TIME_PREFIX = "f";

        // Format the url for the next image load
        final String chartTimePrefix;
        if ((index == 0) && (dayIndex == 0)) {
            chartTimePrefix = PAST_HOUR_TIME_PREFIX;
        } else {
            chartTimePrefix = FUTURE_HOUR_TIME_PREFIX;
        }
        final String chartTypePrefix = getChartURLPrefix();
        final String nextImageURL = String.format(Locale.US, BASE_URL, chartTypePrefix, chartTimePrefix, (mForecastModel.getDayForecastStartingIndex(dayIndex) + index) * WAVE_WATCH_HOUR_STEP);

        // Load the next image
        Ion.with(this).load(nextImageURL).asBitmap().setCallback(mChartLoadCallback);
    }

    private String getChartURLPrefix() {
        switch (chartType) {
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

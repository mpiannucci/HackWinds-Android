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
import com.nucc.hackwinds.types.Condition;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.ConditionArrayAdapter;
import com.nucc.hackwinds.models.ForecastModel;
import com.nucc.hackwinds.utilities.ReachabilityHelper;

import java.util.ArrayList;

import info.hoang8f.android.segmented.SegmentedGroup;


public class DetailedForecastFragment extends ListFragment implements SegmentedGroup.OnCheckedChangeListener {
    private final int ANIMATION_DURATION = 500;

    private enum ForecastChartType {
        SWELL,
        WIND,
        PERIOD
    }

    private int mDayIndex;
    private ForecastChartType mCurrentForecastChartType;
    private AnimationDrawable mChartAnimation;
    private FutureCallback<Bitmap> mChartLoadCallback;
    private ForecastModel mForecastModel;
    private ArrayList<Condition> mDayConditions;
    private ConditionArrayAdapter mConditionArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the condition model
        if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            // Get the buoy model
            mForecastModel = ForecastModel.getInstance(getActivity());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.detailed_forecast_fragment, container, false);

        // Get the condition model for the given day
        mDayIndex = getArguments().getInt("dayIndex");
        mDayConditions = mForecastModel.getConditionsForIndex(mDayIndex);
        mConditionArrayAdapter = new ConditionArrayAdapter(getActivity(), mDayConditions);
        setListAdapter(mConditionArrayAdapter);

        // Get the Segmented widget
        SegmentedGroup chartTypeGroup = (SegmentedGroup) V.findViewById(R.id.forecast_segment_group);
        chartTypeGroup.setOnCheckedChangeListener(this);

        // Set the tint color of the segmented group
        chartTypeGroup.setTintColor(ContextCompat.getColor(getActivity().getApplicationContext(), R.color.hackwinds_blue));

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
                BitmapDrawable chartFrame = new BitmapDrawable(getResources(), result);
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
                    getChartImageForIndex(mCurrentForecastChartType, nFrames);
                }
            }
        };

        return V;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Show the swell chart when the fragment is launched
        RadioButton swellButton = (RadioButton) getActivity().findViewById(R.id.forecast_swell_segment_button);
        swellButton.setChecked(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mChartAnimation.isRunning()) {
            // Stop the animation if it is still running
            mChartAnimation.stop();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int index) {
        // Start loading the new images.
        if (index == R.id.forecast_swell_segment_button) {
            mCurrentForecastChartType = ForecastChartType.SWELL;
        } else if (index == R.id.forecast_wind_segment_button) {
            mCurrentForecastChartType = ForecastChartType.WIND;
        } else if (index == R.id.forecast_period_segment_button) {
            mCurrentForecastChartType = ForecastChartType.PERIOD;
        } else {
            return;
        }

        if (mChartAnimation.isRunning()) {
            mChartAnimation.stop();
        }

        // Remove the animation from the imageview
        getActivity().findViewById(R.id.forecast_chart_image).setBackground(null);

        // Reset the chart animation object
        mChartAnimation = new AnimationDrawable();
        mChartAnimation.setOneShot(false);
        getChartImageForIndex(mCurrentForecastChartType, 0);
    }

    public void getChartImageForIndex(ForecastChartType forecastChartType, int index) {
        switch(forecastChartType) {
            case SWELL:
                Ion.with(getActivity()).load(mDayConditions.get(index).swellChartURL).asBitmap().setCallback(mChartLoadCallback);
                break;
            case WIND:
                Ion.with(getActivity()).load(mDayConditions.get(index).windChartURL).asBitmap().setCallback(mChartLoadCallback);
                break;
            case PERIOD:
                Ion.with(getActivity()).load(mDayConditions.get(index).periodChartURL).asBitmap().setCallback(mChartLoadCallback);
                break;
            default:
                break;
        }
    }

}

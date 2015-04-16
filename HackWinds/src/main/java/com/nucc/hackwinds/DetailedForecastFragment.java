package com.nucc.hackwinds;


import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import info.hoang8f.android.segmented.SegmentedGroup;


public class DetailedForecastFragment extends ListFragment implements SegmentedGroup.OnCheckedChangeListener{
    private final int ANIMATION_DURATION = 500;

    private enum ChartType {
        SWELL,
        WIND,
        PERIOD
    }

    private int mDayIndex;
    private ChartType mCurrentChartType;
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
        SegmentedGroup chartTypeGroup = (SegmentedGroup) V.findViewById(R.id.segmentedChart);
        chartTypeGroup.setOnCheckedChangeListener(this);

        // Set the tint color of the segmented group
        chartTypeGroup.setTintColor(getResources().getColor(R.color.hackwinds_blue));

        // Hide the play button
        ImageView chartPlayButton = (ImageView) V.findViewById(R.id.animatePlayButton);
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
        ImageView chartImage = (ImageView) V.findViewById(R.id.chartImage);
        chartImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChartAnimation.isRunning()) {
                    // Only call to stop the animation if it is currently running
                    mChartAnimation.stop();

                    // Show the play button
                    ImageView playButton = (ImageView) getActivity().findViewById(R.id.animatePlayButton);
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
                    ImageView chartImage = (ImageView) getActivity().findViewById(R.id.chartImage);
                    chartImage.setBackground(chartFrame);

                } else if (nFrames == 6) {
                    // Set the animation drawable as the imageview background
                    ImageView chartImage = (ImageView) getActivity().findViewById(R.id.chartImage);
                    chartImage.setBackground(mChartAnimation);

                    // Show the play button
                    ImageView playButton = (ImageView) getActivity().findViewById(R.id.animatePlayButton);
                    playButton.setVisibility(View.VISIBLE);
                }

                if (nFrames < 6) {
                    // Load the next image
                    getChartImageForIndex(mCurrentChartType, nFrames);
                }
            }
        };

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
        if (mChartAnimation.isRunning()) {
            mChartAnimation.stop();
        }

        // Remove the animation from the imageview
        getActivity().findViewById(R.id.chartImage).setBackground(null);

        // Reset the chart animation object
        mChartAnimation = new AnimationDrawable();
        mChartAnimation.setOneShot(false);

        // Start loading the new images.
        if (index == R.id.swellSegmentButton ) {
            mCurrentChartType = ChartType.SWELL;
        } else if (index == R.id.windSegmentButton) {
            mCurrentChartType = ChartType.WIND;
        } else {
            // Assume period chart
            mCurrentChartType = ChartType.PERIOD;
        }
        getChartImageForIndex(mCurrentChartType, 0);
    }

    public void getChartImageForIndex(ChartType chartType, int index) {
        switch(chartType) {
            case SWELL:
                Ion.with(getActivity()).load(mDayConditions.get(index).SwellChartURL).asBitmap().setCallback(mChartLoadCallback);
                break;
            case WIND:
                Ion.with(getActivity()).load(mDayConditions.get(index).WindChartURL).asBitmap().setCallback(mChartLoadCallback);
                break;
            case PERIOD:
                Ion.with(getActivity()).load(mDayConditions.get(index).PeriodChartURL).asBitmap().setCallback(mChartLoadCallback);
                break;
            default:
                break;
        }
    }

}

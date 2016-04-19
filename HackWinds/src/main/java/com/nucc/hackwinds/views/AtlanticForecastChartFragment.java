package com.nucc.hackwinds.views;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.*;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;

/**
 * Created by matthew on 4/19/16.
 */
public class AtlanticForecastChartFragment extends Fragment {
    final private int WAVE_WATCH_HOUR_STEP = 3;
    final private int WAVE_WATCH_MAX_HOUR = 180;
    final private int WAVE_WATCH_MIN_HOUR = 0;
    final private int WAVE_WATCH_IMAGE_COUNT = (WAVE_WATCH_MAX_HOUR / WAVE_WATCH_HOUR_STEP) + 1;

    public enum WaveWatchChartType {
        WAVES,
        PERIOD,
        WIND
    }
    public WaveWatchChartType chartType = WaveWatchChartType.WAVES;

    private AnimationDrawable mChartAnimation;
    private FutureCallback<Bitmap> mChartLoadCallback;
    private int mAnimationDuration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.atlantic_forecast_chart_fragment, container, false);
        V.setId(chartType.ordinal());

        // Load in the settings and the controls so set them up
        Switch manualControlSwitch = (Switch) V.findViewById(R.id.wavewatch_manual_control_switch);
        manualControlSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LinearLayout mediaControlLayout = (LinearLayout) getView().findViewById(R.id.wavewatch_media_control_layout);
                ImageView playButton = (ImageView) getView().findViewById(R.id.wavewatch_animate_play_button);
                if (isChecked) {
                    mediaControlLayout.setVisibility(View.VISIBLE);
                    playButton.setVisibility(View.GONE);
                    if (mChartAnimation.isRunning()) {
                        mChartAnimation.stop();
                        mChartAnimation.selectDrawable(0);
                    }
                } else {
                    mediaControlLayout.setVisibility(View.GONE);

                    if (mChartAnimation.getNumberOfFrames() == WAVE_WATCH_IMAGE_COUNT) {
                        playButton.setVisibility(View.VISIBLE);
                        mChartAnimation.selectDrawable(0);
                    }
                }

                // Save the state
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).
                        edit().
                        putBoolean("WaveWatchManualControl", isChecked).
                        apply();
            }
        });

        // TODO: Get the users settings
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        //manualControlSwitch.setChecked(preferences.getBoolean("WaveWatchManualControl", false));

        ImageButton previousDayButton = (ImageButton) V.findViewById(R.id.wavewatch_previous_day_chart_button);
        previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementChartStep(-WAVE_WATCH_HOUR_STEP * 8);
            }
        });

        ImageButton previousStepButton = (ImageButton) V.findViewById(R.id.wavewatch_previous_chart_image_button);
        previousStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementChartStep(-WAVE_WATCH_HOUR_STEP);
            }
        });

        ImageButton nextDayButton = (ImageButton) V.findViewById(R.id.wavewatch_next_day_chart_button);
        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementChartStep(WAVE_WATCH_HOUR_STEP*8);
            }
        });

        ImageButton nextStepButton = (ImageButton) V.findViewById(R.id.wavewatch_next_chart_image_button);
        nextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementChartStep(WAVE_WATCH_HOUR_STEP);
            }
        });

        // Hide the play button and set the click listener
        ImageView chartPlayButton = (ImageView) V.findViewById(R.id.wavewatch_animate_play_button);
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
        ImageView chartImage = (ImageView) V.findViewById(R.id.wavewatch_chart_image);
        chartImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mChartAnimation.isRunning()) {
                    // Only call to stop the animation if it is currently running
                    mChartAnimation.stop();
                    mChartAnimation.selectDrawable(0);

                    // Show the play button
                    ImageView playButton = (ImageView) getView().findViewById(R.id.wavewatch_animate_play_button);
                    playButton.setVisibility(View.VISIBLE);
                }
            }
        });

        SeekBar animationSpeed = (SeekBar) V.findViewById(R.id.wavewatch_animation_speed_slider);
        mAnimationDuration = preferences.getInt("WaveWatchAnimationSpeed", 500);
        animationSpeed.setProgress((1000 - mAnimationDuration)/100);
        animationSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    progress = 1;
                } else if (progress == 10) {
                    progress = 9;
                }

                mAnimationDuration = 1000 - (progress*100);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                preferences.edit().putInt("WaveWatchAnimationSpeed", mAnimationDuration).apply();
                resetAnimation();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Nothing
            }
        });

        // Create the chart animation instance
        mChartAnimation = new AnimationDrawable();

        // Create chart loading callback
        mChartLoadCallback = new FutureCallback<Bitmap>() {
            @Override
            public void onCompleted(Exception e, Bitmap result) {
                if (result == null) {
                    ImageView chartImage = (ImageView) getView().findViewById(R.id.wavewatch_chart_image);
                    chartImage.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.photo_loading_error));
                    return;
                }

                BitmapDrawable chartFrame = new BitmapDrawable(getResources(), result);
                mChartAnimation.addFrame(chartFrame, mAnimationDuration);

                int nFrames = mChartAnimation.getNumberOfFrames();
                if (nFrames == 1) {
                    // Set the chart preview image as bitmap that was just received
                    ImageView chartImage = (ImageView) getView().findViewById(R.id.wavewatch_chart_image);
                    chartImage.setImageDrawable(chartFrame);

                } else if (nFrames == WAVE_WATCH_IMAGE_COUNT) {
                    // Set the animation drawable as the imageview background
                    ImageView chartImage = (ImageView) getView().findViewById(R.id.wavewatch_chart_image);
                    chartImage.setImageDrawable(mChartAnimation);

                    // Show the play button
                    ImageView playButton = (ImageView) getView().findViewById(R.id.wavewatch_animate_play_button);
                    Switch manualControlSwitch = (Switch) getView().findViewById(R.id.wavewatch_manual_control_switch);
                    if (!manualControlSwitch.isChecked()) {
                        playButton.setVisibility(View.VISIBLE);
                    }
                }

                if (nFrames < WAVE_WATCH_IMAGE_COUNT) {
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

        resetAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mChartAnimation.isRunning()) {
            mChartAnimation.stop();
        }
    }

    public WaveWatchChartType getChartType() {
        return chartType;
    }

    public void setChartType(WaveWatchChartType chartType) {
        this.chartType = chartType;
    }

    private void resetAnimation() {
        if (mChartAnimation.isRunning()) {
            mChartAnimation.stop();
        }

        resetCurrentHourEdit();

        // Remove the animation from the imageview
        getView().findViewById(R.id.wavewatch_chart_image).setBackground(null);

        // Reset the chart animation object
        ImageView chartImage = (ImageView) getView().findViewById(R.id.wavewatch_chart_image);
        chartImage.setImageDrawable(mChartAnimation.getFrame(0));

        mChartAnimation = new AnimationDrawable();
        mChartAnimation.setOneShot(true);
        getChartImageForIndex(0);
    }

    public void getChartImageForIndex(int index) {
        final String BASE_URL = "http://polar.ncep.noaa.gov/waves/WEB/multi_1.latest_run/plots/US_eastcoast.%s.%s%03dh.png";
        final String PAST_HOUR_TIME_PREFIX = "h";
        final String FUTURE_HOUR_TIME_PREFIX = "f";

        // Format the url for the next image load
        final String chartTimePrefix;
        if (index == 0) {
            chartTimePrefix = PAST_HOUR_TIME_PREFIX;
        } else {
            chartTimePrefix = FUTURE_HOUR_TIME_PREFIX;
        }
        final String chartTypePrefix = getChartURLPrefix();
        final String nextImageURL = String.format(BASE_URL, chartTypePrefix, chartTimePrefix, index * WAVE_WATCH_HOUR_STEP);

        // Load the next image
        Ion.with(this).load(nextImageURL).asBitmap().setCallback(mChartLoadCallback);
    }

    private void incrementChartStep(int hourStep) {
        TextView currentHour = (TextView) getView().findViewById(R.id.wavewatch_current_hour);
        int hour = Integer.valueOf(currentHour.getText().toString().split(" ")[0]);
        if ((hour == WAVE_WATCH_MAX_HOUR) && (hourStep > 0)) {
            return;
        } else if ((hour == WAVE_WATCH_MIN_HOUR) && (hourStep < 0)) {
            return;
        }

        hour += hourStep;

        if (hour > WAVE_WATCH_MAX_HOUR) {
            hour = WAVE_WATCH_MAX_HOUR;
        } else if (hour < WAVE_WATCH_MIN_HOUR) {
            hour = WAVE_WATCH_MIN_HOUR;
        }

        currentHour.setText(String.valueOf(hour) + " Hours");
        mChartAnimation.selectDrawable(hour / WAVE_WATCH_HOUR_STEP);
    }

    private void resetCurrentHourEdit() {
        TextView currentHour = (TextView) getView().findViewById(R.id.wavewatch_current_hour);
        currentHour.setText(String.valueOf(0) + " Hours");
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

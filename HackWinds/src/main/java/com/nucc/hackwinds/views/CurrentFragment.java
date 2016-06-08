package com.nucc.hackwinds.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.slider.library.SliderAdapter;
import com.daimajia.slider.library.SliderLayout;

import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.ConditionArrayAdapter;
import com.nucc.hackwinds.listeners.CameraChangedListener;
import com.nucc.hackwinds.listeners.ForecastChangedListener;
import com.nucc.hackwinds.types.Camera;
import com.nucc.hackwinds.models.CameraModel;
import com.nucc.hackwinds.models.ForecastModel;
import com.nucc.hackwinds.types.Forecast;
import com.nucc.hackwinds.utilities.ReachabilityHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


public class CurrentFragment extends ListFragment implements ForecastChangedListener, CameraChangedListener {
    // Initialize the other variables
    private ConditionArrayAdapter mConditionArrayAdapter;
    private Camera mCamera;
    private CameraModel mCameraModel;
    private ForecastModel mForecastModel;
    private SliderLayout mCameraSliderLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the menu
        setHasOptionsMenu(true);

        if (!ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            // Alert the user they need the network and close the app on completion
            new AlertDialog.Builder(getActivity())
                    .setTitle("Network Error")
                    .setMessage("No network detected! Make sure to connect to the internet and reopen the app")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue
                            getActivity().finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // Get the magicseaweed model instance
        mForecastModel = ForecastModel.getInstance(getActivity());
        mForecastModel.addForecastChangedListener(this);

        mCameraModel = CameraModel.getInstance(getActivity());
        mCameraModel.addCameraChangedListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.current_fragment, container, false);

        // Find the image slider, and initialize it
        mCameraSliderLayout = (SliderLayout) V.findViewById(R.id.camera_image_slider);
        int hackWindsColor = Color.parseColor("#47A3FF");
        mCameraSliderLayout.getPagerIndicator().setDefaultIndicatorColor(hackWindsColor, Color.WHITE);

        // return the view
        return V;
    }

    @Override
    public void onResume() {
        super.onResume();

        forecastDataUpdated();
        cameraDataUpdated();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.live_menu_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_alt_cameras:
                startActivity(new Intent(getActivity(), AlternateCameraActivity.class));
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void forecastDataUpdated() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Set the day header to the current day
                TextView date = (TextView) getView().findViewById(R.id.today_date_header);
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                String dayName = getResources().getStringArray(R.array.daysOfTheWeek)[day-1];
                date.setText(dayName);

                // Set the condition adapter for the list
                if (mConditionArrayAdapter == null) {
                    ArrayList<Forecast> conditions = mForecastModel.getForecastsForDay(0);
                    mConditionArrayAdapter = new ConditionArrayAdapter(getActivity(), conditions);
                    setListAdapter(mConditionArrayAdapter);
                } else {
                    ArrayList<Forecast> conditions = mForecastModel.getForecastsForDay(0);
                    mConditionArrayAdapter.setConditonData(conditions);
                }
            }
        });
    }

    @Override
    public void forecastDataUpdateFailed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // For now do nothing
            }
        });
    }

    @Override
    public void cameraDataUpdated() {
        if (mCameraModel.cameraCount < 1) {
            return;
        }

        if (mCamera == null) {
            mCamera = CameraModel.getInstance(getActivity()).cameraLocations.get("Narragansett").get("Warm Winds");
        }

        if (mCamera == null) {
            return;
        }

        loadCameraImages();
    }

    @Override
    public void cameraDataUpdateFailed() {

    }

    private void loadCameraImages() {
        final int CAMERA_IMAGE_COUNT = 8;

        if (mCameraSliderLayout == null) {
            return;
        }

        mCameraSliderLayout.stopAutoCycle();
        mCameraSliderLayout.removeAllSliders();

        for (int i = 1; i < CAMERA_IMAGE_COUNT+1; i++) {
            String cameraURL = mCamera.imageURL.replace("1.jpg", String.format(Locale.US, "%d.jpg", i));
            Ion.with(getActivity()).load(cameraURL).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    DefaultSliderView cameraSliderView = new DefaultSliderView(getActivity());
                    cameraSliderView.image(result);
                    mCameraSliderLayout.addSlider(cameraSliderView);
                }
            });
        }
    }
}

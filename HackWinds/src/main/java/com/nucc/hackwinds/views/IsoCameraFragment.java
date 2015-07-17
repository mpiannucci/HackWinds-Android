package com.nucc.hackwinds.views;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.os.Handler;
import android.widget.TextView;
import android.content.Context;
import android.widget.VideoView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.CameraModel;

import org.json.JSONException;

public class IsoCameraFragment extends Fragment {
    private final int NORMAL_REFRESH_DURATION = 3000;
    private final int NARRAGANSETT_REFRESH_DURATION = 35000;
    private final String POINT_JUDITH_STATIC_IMAGE = "http://www.asergeev.com/pictures/archives/2004/372/jpeg/20.jpg";

    private ImageView mCameraImage;
    private Context mContext;
    private String mCameraURL;
    private String mVideoURL;
    private String mLocation;
    private String mCamera;
    private boolean mAutoRefresh;
    private int mAutoRefreshDuration;

    Handler mHandler;
    Runnable mRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlternateCameraActivity alternateCameraActivity = (AlternateCameraActivity) getActivity();
        alternateCameraActivity.setToolbarTitle("Camera View");

        // Store the context so we can get resources later on
        mContext = getActivity().getApplicationContext();

        // This is a quick runnable to reload the images
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                loadCameraImage();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.iso_camera_fragment, container, false);

        mCameraImage = (ImageView) V.findViewById(R.id.latestCameraImage);

        final Switch autoRefreshSwitch = (Switch) V.findViewById(R.id.auto_refresh_toggle);
        mAutoRefresh = autoRefreshSwitch.isChecked();
        autoRefreshSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAutoRefresh = ((Switch) view).isChecked();
                if (mAutoRefresh) {
                    // Trigger a camera refresh
                    loadCameraImage();
                }

                // Update the auto refresh label
                updateAutoRefreshDurationLabel();
            }
        });

        return V;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCameraURL != null) {
            // Only trigger an image load if there is a url
            loadCameraImage();

            // Set the video play button to the correct visibility for the location
            ImageView playButtonView = (ImageView) getActivity().findViewById(R.id.isoVideoPlayButton);
            if (mLocation.equals("Narragansett") && mCamera.equals("Point Judith")) {
                playButtonView.setVisibility(View.VISIBLE);
            } else {
                playButtonView.setVisibility(View.GONE);
            }
        }

        // Update the auto refresh label
        updateAutoRefreshDurationLabel();
    }

    @Override
    public void onPause() {
        super.onPause();

        mHandler.removeCallbacks(mRunnable);
    }

    public void setCamera(String location, String camera) {
        try {
            if (location.equals("Narragansett") && camera.equals("Point Judith")) {
                mCameraURL = POINT_JUDITH_STATIC_IMAGE;

            } else {
                mCameraURL = CameraModel.getCameraLocations().getJSONObject(location).getString(camera);
            }
        } catch (JSONException e) {
            return;
        }

        if (location.equals("Narragansett") && camera.equals("Town Beach South")) {
            mAutoRefreshDuration = NARRAGANSETT_REFRESH_DURATION;
        } else {
            mAutoRefreshDuration = NORMAL_REFRESH_DURATION;
        }

        mLocation = location;
        mCamera = camera;
    }

    public void loadCameraImage() {
        if (mCamera.equals("Point Judith")) {
            // If it is the point judith camera, we want to hide everything but the image
            getActivity().findViewById(R.id.auto_refresh_label).setVisibility(View.GONE);
            Switch autoRefreshToggle = (Switch)getActivity().findViewById(R.id.auto_refresh_toggle);
            autoRefreshToggle.performClick();
            autoRefreshToggle.setVisibility(View.GONE);
        }

        if (mContext != null) {
            // If there is a context, then load the next image and create the callback to set it as the current image
            Ion.with(getActivity()).load(mCameraURL).noCache().asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    if (e != null) {
                        // Set the error image on exceptions
                        mCameraImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.photo_loading_error));
                    } else {
                        // Everything is ok, so set the image and load the next if applicable
                        mCameraImage.setImageBitmap(result);
                        if (mAutoRefresh) {
                            mHandler.postDelayed(mRunnable, mAutoRefreshDuration);
                        }
                    }
                }
            });
        } else {
            // If the context isn't ready yet, wait another half second and throw the runnable again.
            mHandler.postDelayed(mRunnable, 500);
        }
    }

    private void updateAutoRefreshDurationLabel() {
        TextView autoRefreshDurationLabel = (TextView) getActivity().findViewById(R.id.auto_refresh_duration);
        if (mAutoRefresh) {
            autoRefreshDurationLabel.setText("Refresh interval is " + String.valueOf(mAutoRefreshDuration / 1000) + " seconds");
            autoRefreshDurationLabel.setVisibility(View.VISIBLE);
        } else {
            autoRefreshDurationLabel.setVisibility(View.GONE);
        }
    }

}

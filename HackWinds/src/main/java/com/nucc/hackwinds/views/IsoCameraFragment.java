package com.nucc.hackwinds.views;


import android.graphics.Bitmap;
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

import com.appspot.hackwinds.hackwinds.model.MessagesCameraCameraMessage;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;

public class IsoCameraFragment extends Fragment {

    private MessagesCameraCameraMessage mCamera;
    private boolean mAutoRefresh;

    private Context mContext;
    private ImageView mCameraImage;

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

        mCameraImage = (ImageView) V.findViewById(R.id.latest_camera_image);

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

        if (mCamera != null) {
            // Only trigger an image load if there is a url
            AlternateCameraActivity alternateCameraActivity = (AlternateCameraActivity) getActivity();
            alternateCameraActivity.setToolbarTitle(mCamera.getName());
            loadCameraImage();
        }

        // Update the auto refresh label
        updateAutoRefreshDurationLabel();
    }

    @Override
    public void onPause() {
        super.onPause();

        // We need to remove all background threads if we leave the fragment
        mHandler.removeCallbacks(mRunnable);
    }

    public void setCamera(MessagesCameraCameraMessage camera) {
        if (camera == null) {
            return;
        }

        mCamera = camera;
        mAutoRefresh = mCamera.getRefreshable();
    }

    public void loadCameraImage() {
        if (mContext != null) {
            // If there is a context, then load the next image and create the callback to set it as the current image
            Ion.with(mContext).load(mCamera.getImageUrl()).noCache().asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    if (e != null) {
                        // Set the error image on exceptions
                        mCameraImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.photo_loading_error));
                    } else {
                        // Everything is ok, so set the image
                        mCameraImage.setImageBitmap(result);

                        // If enabled, start the countdown to loading the next view
                        if (mAutoRefresh) {
                            mHandler.postDelayed(mRunnable, mCamera.getRefreshInterval() * 1000);
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
            autoRefreshDurationLabel.setText("Refresh interval is " + String.valueOf(mCamera.getRefreshInterval()) + " seconds");
            autoRefreshDurationLabel.setVisibility(View.VISIBLE);
        } else {
            autoRefreshDurationLabel.setVisibility(View.GONE);
        }
    }

}

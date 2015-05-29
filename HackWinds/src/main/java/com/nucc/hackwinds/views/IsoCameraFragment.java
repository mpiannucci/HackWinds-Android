package com.nucc.hackwinds.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.os.Handler;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.CameraModel;

import org.json.JSONException;

public class IsoCameraFragment extends Fragment {
    private String mCameraURL;
    private boolean mAutoRefresh;
    private int mAutoRefreshDuration;

    Handler mHandler;
    Runnable mRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlternateCameraActivity alternateCameraActivity = (AlternateCameraActivity) getActivity();
        alternateCameraActivity.setToolbarTitle("Camera View");

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

        final Switch autoRefreshSwitch = (Switch) V.findViewById(R.id.autoRefreshToggle);
        mAutoRefresh = autoRefreshSwitch.isChecked();
        autoRefreshSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAutoRefresh = ((Switch) view).isChecked();
                loadCameraImage();
            }
        });

        return V;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCameraURL != null) {
            loadCameraImage();
        }
    }

    public void setCamera(String location, String camera) {
        try {
            mCameraURL = CameraModel.getCameraLocations().getJSONObject(location).getString(camera);
        } catch (JSONException e) {
            return;
        }

        if (location.equals("Narragansett") && camera.equals("Town Beach South")) {
            mAutoRefreshDuration = 35000;
        } else {
            mAutoRefreshDuration = 3000;
        }
    }

    public void loadCameraImage() {
        final ImageView cameraImage = (ImageView) getActivity().findViewById(R.id.latestCameraImage);
        Ion.with(getActivity()).load(mCameraURL).noCache().intoImageView(cameraImage).setCallback(new FutureCallback<ImageView>() {
            @Override
            public void onCompleted(Exception e, ImageView result) {
                if (mAutoRefresh) {
                    mHandler.postDelayed(mRunnable, mAutoRefreshDuration);
                }
            }
        });
    }

}

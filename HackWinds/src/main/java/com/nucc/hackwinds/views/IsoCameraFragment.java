package com.nucc.hackwinds.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.CameraModel;

import org.json.JSONException;

public class IsoCameraFragment extends Fragment {
    private String mCameraURL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlternateCameraActivity alternateCameraActivity = (AlternateCameraActivity) getActivity();
        alternateCameraActivity.setToolbarTitle("Camera View");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.iso_camera_fragment, container, false);

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
    }

    public void loadCameraImage() {
        ImageView cameraImage = (ImageView) getActivity().findViewById(R.id.latestCameraImage);
        Ion.with(getActivity()).load(mCameraURL).intoImageView(cameraImage);
    }

}

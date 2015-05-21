package com.nucc.hackwinds.views;


import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.AlternateCameraListAdapter;
import com.nucc.hackwinds.models.CameraModel;

public class AlternateCameraListFragment extends ListFragment {

    private AlternateCameraListAdapter mAlternateCameraListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlternateCameraListAdapter = new AlternateCameraListAdapter(getActivity(), CameraModel.getCameraLocations());
        setListAdapter(mAlternateCameraListAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.alternate_camera_list_fragment, container, false);

        return V;
    }
}

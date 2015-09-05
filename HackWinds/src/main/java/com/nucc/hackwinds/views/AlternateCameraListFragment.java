package com.nucc.hackwinds.views;


import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.adapters.AlternateCameraListAdapter;
import com.nucc.hackwinds.models.CameraModel;

public class AlternateCameraListFragment extends ListFragment {

    private AlternateCameraListAdapter mAlternateCameraListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CameraModel cameraModel = CameraModel.getInstance(getActivity().getApplicationContext());

        mAlternateCameraListAdapter = new AlternateCameraListAdapter(getActivity(), cameraModel);
        setListAdapter(mAlternateCameraListAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.alternate_camera_list_fragment, container, false);

        return V;
    }

    @Override
    public void onResume() {
        super.onResume();

        AlternateCameraActivity alternateCameraActivity = (AlternateCameraActivity) getActivity();
        alternateCameraActivity.resetToolbarTitle();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String camera = mAlternateCameraListAdapter.getItem(position);
        String location = mAlternateCameraListAdapter.getSectionForPosition(position);

        IsoCameraFragment cameraFragment = new IsoCameraFragment();
        cameraFragment.setCamera(location, camera);

        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, cameraFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}

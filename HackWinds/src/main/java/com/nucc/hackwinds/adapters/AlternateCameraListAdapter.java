package com.nucc.hackwinds.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.CameraModel;

import java.util.ArrayList;

public class AlternateCameraListAdapter extends BaseAdapter implements ListAdapter {

    private class CameraLocation {
        public String Location;
        public boolean isSection;
    }

    static class ViewHolder {
        public TextView locationTV;
        public int position;
    }

    private final Context context;
    private ArrayList<CameraLocation> cameraLocations;

    public AlternateCameraListAdapter(Context ctx, CameraModel cameraModel) {
        this.context = ctx;
        cameraLocations = new ArrayList<>();

        // Start the location index at 0
        int locationIndex = 0;

        // Rip the cameras from the model into the CameraLocation object list
        for (String location : cameraModel.locationKeys) {
            // Add a camera object to initialize the section
            CameraLocation thisLocation = new CameraLocation();
            thisLocation.Location = location;
            thisLocation.isSection = true;

            // Add the location to the list
            cameraLocations.add(thisLocation);

            for (String cameraName : cameraModel.cameraKeys.get(locationIndex)) {
                // Add a camera location for the next camera
                CameraLocation thisCamera = new CameraLocation();
                thisCamera.Location = cameraName;
                thisCamera.isSection = false;

                // Add the camera to the location list
                cameraLocations.add(thisCamera);
            }

            // Move to the next location
            locationIndex++;
        }
    }

    @Override
    public int getCount() {
        return cameraLocations.size();
    }

    @Override
    public String getItem(int position) {
        return cameraLocations.get(position).Location;
    }

    @Override
    public long getItemId(int position) {
        return position * 4;
    }

    public boolean isSection(int position) {
        return cameraLocations.get(position).isSection;
    }

    public String getSectionForPosition(int position) {
        if (isSection(position)) {
            // It has no parent section
            return "";
        }

        int prevPosition = position;
        while (prevPosition >= 0) {
            if (isSection(prevPosition)) {
                // Yayy we found the section
                return getItem(prevPosition);
            }
            prevPosition--;
        }

        // We somehow didnt find a section
        return "";
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // Make the view reusable
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.position = position;
            if (isSection(position)) {
                rowView = inflater.inflate(R.layout.alternate_camera_list_section, parent, false);
                viewHolder.locationTV = (TextView) rowView.findViewById(R.id.alternateCameraSectionText);
                rowView.setEnabled(false);
                rowView.setOnClickListener(null);
            } else {
                rowView = inflater.inflate(R.layout.alternate_camera_list_item, parent, false);
                viewHolder.locationTV = (TextView) rowView.findViewById(R.id.alternateCameraItemText);
            }
            rowView.setTag(viewHolder);
        }

        // Fill the data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.locationTV.setText(getItem(position));

        return rowView;
    }
}

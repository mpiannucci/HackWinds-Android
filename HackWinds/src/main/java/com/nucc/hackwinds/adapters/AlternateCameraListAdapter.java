package com.nucc.hackwinds.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.nucc.hackwinds.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class AlternateCameraListAdapter extends BaseAdapter implements ListAdapter {

    private class CameraLocation {
        public String Location;
        public boolean isSection;
    }

    static class ViewHolder {
        public TextView locationTV;
        public int position;
    }

    private final Context mContext;
    private ArrayList<CameraLocation> mCameraLocations;

    public AlternateCameraListAdapter(Context context, JSONObject jsonObject) {
        mContext = context;

        // Parse the json object into the CameraLocation object
        Iterator<String> locations = jsonObject.keys();
        while(locations.hasNext()) {
            // Get the location and its children
            String location = locations.next();
            JSONObject locationParent;
            try {
                // Grab the child so we can get its children
               locationParent = jsonObject.getJSONObject(location);
            } catch (JSONException e) {
                continue;
            }

            // Add the location
            CameraLocation thisLocation = new CameraLocation();
            thisLocation.Location = location;
            thisLocation.isSection = true;

            // Add the camera location to the list
            mCameraLocations.add(thisLocation);

            Iterator<String> cameras = locationParent.keys();
            while(cameras.hasNext()) {
                // Add each camera for every location to the list.
                String camera = cameras.next();
                CameraLocation childLocation = new CameraLocation();
                childLocation.Location = camera;
                childLocation.isSection = false;
            }
        }
    }

    @Override
    public int getCount() {
        return mCameraLocations.size();
    }

    @Override
    public String getItem(int position) {
        return mCameraLocations.get(position).Location;
    }

    @Override
    public long getItemId(int position) {
        return position * 4;
    }

    public boolean isSection(int position) {
        return mCameraLocations.get(position).isSection;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // Make the view reusable
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.position = position;
            if (isSection(position)) {
                rowView = inflater.inflate(R.layout.alternate_camera_list_section, parent, false);
                viewHolder.locationTV = (TextView) rowView.findViewById(R.id.alternateCameraSectionText);
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

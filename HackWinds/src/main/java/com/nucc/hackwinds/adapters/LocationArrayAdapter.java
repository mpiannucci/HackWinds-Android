package com.nucc.hackwinds.adapters;

import com.nucc.hackwinds.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class LocationArrayAdapter extends ArrayAdapter<String> {
    private final Context mContext;
    public ArrayList<String> values;

    static class ViewHolder {
        public TextView locationTextView;
    }

    public LocationArrayAdapter(Context ctx, ArrayList<String> vals) {
        super(ctx, R.layout.location_dropdown_item, vals);
        this.mContext = ctx;
        this.values = vals;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public String getItem(int position) {
        return values.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View spinnerItem = convertView;
        ViewHolder holder;

        if (convertView == null) {
            // Inflate the title view
            LayoutInflater inflater = LayoutInflater.from(mContext);
            spinnerItem = inflater.inflate(R.layout.location_dropdown_title_item, parent, false);

            // Create a new holder for easily caching views
            holder = new ViewHolder();
            holder.locationTextView = (TextView) spinnerItem.findViewById(R.id.location_subtitle_text);
            spinnerItem.setTag(holder);
        } else {
            holder = (ViewHolder) spinnerItem.getTag();
        }

        // Set the subtitle text view
        holder.locationTextView.setText(getItem(position));

        return spinnerItem;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View spinnerItem = convertView;
        ViewHolder holder;

        if (convertView == null) {
            // Inflate the default dropdown item
            LayoutInflater inflater = LayoutInflater.from(mContext);
            spinnerItem = inflater.inflate(R.layout.location_dropdown_item, parent, false);

            // Create a new holder for easily caching views
            holder = new ViewHolder();
            holder.locationTextView = (TextView) spinnerItem.findViewById(R.id.dropdown_text);
            spinnerItem.setTag(holder);
        } else {
            holder = (ViewHolder) spinnerItem.getTag();
        }

        // Set the text to be the correct location for the position
        holder.locationTextView.setText(getItem(position));

        return spinnerItem;
    }
}

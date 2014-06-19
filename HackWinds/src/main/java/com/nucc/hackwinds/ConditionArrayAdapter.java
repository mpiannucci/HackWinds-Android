package com.nucc.hackwinds;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.Condition;

import android.os.Bundle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.lang.Integer;

public class ConditionArrayAdapter extends ArrayAdapter<Condition> {

    public ConditionArrayAdapter(Context context, ArrayList<Condition> values) {
        super(context, 0, values);
    }

    // Returns the number of types of Views that will be created by getView(int, View, ViewGroup)
    @Override
    public int getViewTypeCount() {
       // Returns the number of types of Views that will be created by this adapter
       // Each type represents a set of views that can be converted
        return Condition.ConditionTypes.values().length;
    }

    // Get the type of View that will be created by getView(int, View, ViewGroup) for the specified item.
    @Override
    public int getItemViewType(int position) {
       // Return an integer here representing the type of View.
       // Note: Integers must be in the range 0 to getViewTypeCount() - 1
        return getItem(position).condition.ordinal();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Condition condition = getItem(position);
        int type = 0;
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            // Get the data item type for this position
            type = getItemViewType(position);
            // Inflate XML layout based on the type     
            convertView = getInflatedLayoutForType(type);
        }
        // Set the UI depending on the type
        updateCard(type, convertView, condition);

        // Return the completed view to render on screen
        return convertView;
    }

    // Given the item type, responsible for returning the correct inflated XML layout file
    private View getInflatedLayoutForType(int type) {
        if (type == Condition.ConditionTypes.WAVEHEIGHT.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.break_item, null);
        } else if (type == Condition.ConditionTypes.WIND.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.wind_item, null);
        } else if (type == Condition.ConditionTypes.SWELL.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.swell_item, null);
        } else if (type == Condition.ConditionTypes.TIDE.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.tide_item, null);
        } else if (type == Condition.ConditionTypes.DATA.ordinal()) {
            return LayoutInflater.from(getContext()).inflate(R.layout.data_item, null);
        } else {
            return null;
        }
    }

    // Given the item type, set the ui to the correct data
    private void updateCard(int type, View convertView, Condition condition) {
        if (type == Condition.ConditionTypes.WAVEHEIGHT.ordinal()) {
            TextView tvData = (TextView) convertView.findViewById(R.id.itemData);
            ImageView thumb = (ImageView) convertView.findViewById(R.id.itemThumb);
            tvData.setText(condition.text[0]+" - "+condition.text[1]+" feet");
            if (Integer.parseInt(condition.text[0]) >= 2 | Integer.parseInt(condition.text[1]) >= 3) {
                thumb.setImageResource(R.drawable.thumbs_up);
            } else {
                thumb.setImageResource(R.drawable.thumbs_down);
            }

        } else if (type == Condition.ConditionTypes.WIND.ordinal()) {

        } else if (type == Condition.ConditionTypes.SWELL.ordinal()) {

        } else if (type == Condition.ConditionTypes.TIDE.ordinal()) {

        } else if (type == Condition.ConditionTypes.DATA.ordinal()) {

        }
    }
}
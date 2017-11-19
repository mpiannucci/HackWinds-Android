package com.nucc.hackwinds.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.text.format.DateFormat;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.types.Forecast;
import com.nucc.hackwinds.utilities.Extensions;

import java.util.ArrayList;

public class ConditionArrayAdapter extends ArrayAdapter<Forecast> {
    private final Context context;
    public ArrayList<Forecast> values;

    // Class to hold view IDs so they can be recycled
    static class ViewHolder {
        public TextView dateTV;
        public TextView conditionsTV;
        public TextView primarySwellTV;
        public TextView secondarySwellTV;
        public int position;
    }

    public ConditionArrayAdapter(Context ctx, ArrayList<Forecast> vals) {
        super(ctx, R.layout.detailed_forecast_item, vals);
        this.context = ctx;
        this.values = vals;
    }

    public void setConditonData(ArrayList<Forecast> newValues) {
        this.values = newValues;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (values == null) {
            return 0;
        }
        return values.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // Make the view reusable
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                                      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.detailed_forecast_item, parent, false);

            // Set the view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.dateTV = (TextView) rowView.findViewById(R.id.time_data);
            viewHolder.conditionsTV = (TextView) rowView.findViewById(R.id.conditions_data);
            viewHolder.primarySwellTV = (TextView) rowView.findViewById(R.id.primary_swell_data);
            viewHolder.secondarySwellTV = (TextView) rowView.findViewById(R.id.secondary_swell_data);
            viewHolder.position = position;

            // Set the tag so the views can be recycled
            rowView.setTag(viewHolder);
        }
        // Fill the data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Get the data for the position in the list
        Forecast condition = values.get(position);

        // Set the textview for all of the data
        if (DateFormat.is24HourFormat(context)) {
            holder.dateTV.setText(condition.timeForTwentyFourHourFormat());
        } else {
            holder.dateTV.setText(condition.timeStringNoZero());
        }
        holder.conditionsTV.setText(condition.getConditionSummary());
        holder.primarySwellTV.setText(Extensions.getDetailedSwellSummary(condition.primarySwellComponent));
        if (condition.secondarySwellComponent.getCompassDirection().equals("NULL")) {
            holder.secondarySwellTV.setText("No Secondary Swell Component");
        } else {
            holder.secondarySwellTV.setText(Extensions.getDetailedSwellSummary(condition.secondarySwellComponent));
        }

        // Return the completed view to render on screen
        return rowView;
    }
}
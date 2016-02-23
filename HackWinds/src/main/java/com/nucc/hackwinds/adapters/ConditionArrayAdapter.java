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

import java.util.ArrayList;

public class ConditionArrayAdapter extends ArrayAdapter<Forecast> {
    private final Context context;
    public ArrayList<Forecast> values;

    // Class to hold view IDs so they can be recycled
    static class ViewHolder {
        public TextView dateTV;
        public TextView breakTV;
        public TextView windTV;
        public TextView swellTV;
        public int position;
    }

    public ConditionArrayAdapter(Context ctx, ArrayList<Forecast> vals) {
        super(ctx, R.layout.current_item, vals);
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
        return values.size() + 1;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // Make the view reusable
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                                      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.current_item, parent, false);

            // Set the view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.dateTV = (TextView) rowView.findViewById(R.id.timeData);
            viewHolder.breakTV = (TextView) rowView.findViewById(R.id.breakData);
            viewHolder.windTV = (TextView) rowView.findViewById(R.id.windData);
            viewHolder.swellTV = (TextView) rowView.findViewById(R.id.swellData);
            viewHolder.position = position;

            // Set the tag so the views can be recycled
            rowView.setTag(viewHolder);
        }
        // Fill the data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        if (position < 1) {
            // If its the first item, set the textviews to the headers
            holder.dateTV.setText("Time");
            holder.breakTV.setText("Waves");
            holder.windTV.setText("Wind");
            holder.swellTV.setText("Swell");

            // Set the text to bold because its the header
            holder.dateTV.setTypeface(null, Typeface.BOLD);
            holder.breakTV.setTypeface(null, Typeface.BOLD);
            holder.windTV.setTypeface(null, Typeface.BOLD);
            holder.swellTV.setTypeface(null, Typeface.BOLD);

            // And make it blue too
            holder.dateTV.setTextColor(ContextCompat.getColor(context, R.color.hackwinds_blue));
            holder.breakTV.setTextColor(ContextCompat.getColor(context, R.color.hackwinds_blue));
            holder.windTV.setTextColor(ContextCompat.getColor(context, R.color.hackwinds_blue));
            holder.swellTV.setTextColor(ContextCompat.getColor(context, R.color.hackwinds_blue));
        } else {
            // Get the data for the position in the list
            Forecast condition = values.get(position - 1);

            // Set the textview for all of the data
            if (DateFormat.is24HourFormat(context)) {
                holder.dateTV.setText(condition.timeForTwentyFourHourFormat());
            } else {
                holder.dateTV.setText(condition.time);
            }
            holder.breakTV.setText(String.valueOf((int)condition.minimumBreakingHeight) + " - " + String.valueOf((int)condition.maximumBreakingHeight));
            holder.windTV.setText(condition.windCompassDirection + " " + String.valueOf((int)condition.windSpeed));
            holder.swellTV.setText(condition.primarySwellComponent.getSwellSummary());

            // Make sure the text is normal and not bold
            holder.dateTV.setTypeface(null, Typeface.NORMAL);
            holder.breakTV.setTypeface(null, Typeface.NORMAL);
            holder.windTV.setTypeface(null, Typeface.NORMAL);
            holder.swellTV.setTypeface(null, Typeface.NORMAL);

            // Make sure the text is black
            holder.dateTV.setTextColor(Color.BLACK);
            holder.breakTV.setTextColor(Color.BLACK);
            holder.windTV.setTextColor(Color.BLACK);
            holder.swellTV.setTextColor(Color.BLACK);
        }
        // Return the completed view to render on screen
        return rowView;
    }
}
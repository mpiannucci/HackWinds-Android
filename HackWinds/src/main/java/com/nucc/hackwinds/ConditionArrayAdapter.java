package com.nucc.hackwinds;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class ConditionArrayAdapter extends ArrayAdapter<Condition> {
    private final Context context;
    private final ArrayList<Condition> values;

    // Class to hold view IDs so they can be recycled
    static class ViewHolder {
        public TextView dateTV;
        public TextView breakTV;
        public TextView windTV;
        public TextView swellTV;
        public int position;
    }


    public ConditionArrayAdapter(Context context, ArrayList<Condition> values) {
        super(context, R.layout.current_item, values);
        this.context = context;
        this.values = values;
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
            holder.dateTV.setTextColor(context.getResources().getColor(R.color.jblue));
            holder.breakTV.setTextColor(context.getResources().getColor(R.color.jblue));
            holder.windTV.setTextColor(context.getResources().getColor(R.color.jblue));
            holder.swellTV.setTextColor(context.getResources().getColor(R.color.jblue));
        } else {
            // Get the data for the position in the list
            Condition condition = values.get(position);

            // Set the textview for all of the data
            holder.dateTV.setText(condition.date);
            holder.breakTV.setText(condition.minBreak + " - " + condition.maxBreak);
            holder.windTV.setText(condition.windDir + " " + condition.windSpeed);
            holder.swellTV.setText(condition.swellDeg + " " + condition.swellHeight + " ft @ " + condition.swellPeriod + " s");

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
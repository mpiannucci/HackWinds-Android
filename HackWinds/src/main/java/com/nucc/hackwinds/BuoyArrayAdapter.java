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

import java.util.ArrayList;

public class BuoyArrayAdapter extends ArrayAdapter<Buoy> {
    final String[] DIRS = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};

    private final Context context;
    private final ArrayList<Buoy> values;

    static class ViewHolder {
        public TextView timeTV;
        public TextView wvhtTV;
        public TextView periodTV;
        public TextView directionTV;
    }

    public BuoyArrayAdapter(Context context, ArrayList<Buoy> values) {
        super(context, R.layout.buoy_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // Make the view reusable
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.buoy_item, parent, false);

            // Set the view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.timeTV = (TextView) rowView.findViewById(R.id.buoyTimeData);
            viewHolder.wvhtTV = (TextView) rowView.findViewById(R.id.buoyWaveData);
            viewHolder.periodTV = (TextView) rowView.findViewById(R.id.buoyPeriodData);
            viewHolder.directionTV = (TextView) rowView.findViewById(R.id.buoyDirData);

            // Set the tag so the view can be recycled
            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();

        if (position < 1) {
            // If its the first row, set the text to the headers
            holder.timeTV.setText("Time");
            holder.wvhtTV.setText("Waves");
            holder.periodTV.setText("Period");
            holder.directionTV.setText("Direction");

            // Since its a header make it bold
            holder.timeTV.setTypeface(null, Typeface.BOLD);
            holder.wvhtTV.setTypeface(null, Typeface.BOLD);
            holder.periodTV.setTypeface(null, Typeface.BOLD);
            holder.directionTV.setTypeface(null, Typeface.BOLD);

            // And make it blue
            holder.timeTV.setTextColor(context.getResources().getColor(R.color.hackwinds_blue));
            holder.wvhtTV.setTextColor(context.getResources().getColor(R.color.hackwinds_blue));
            holder.periodTV.setTextColor(context.getResources().getColor(R.color.hackwinds_blue));
            holder.directionTV.setTextColor(context.getResources().getColor(R.color.hackwinds_blue));
        } else {
            // Get the buoy item for the list position
            Buoy buoy = values.get(position-1);

            // Set the data into the text views
            holder.timeTV.setText(buoy.time);
            holder.wvhtTV.setText(buoy.wvht);
            holder.periodTV.setText(buoy.dpd);

            // Hack to make sure that buoy direction is wont crash
            int windIndex = Integer.valueOf(buoy.dir)/(360/DIRS.length);
            if (windIndex >= DIRS.length) {
                // If its past NNW, force it to be north
                windIndex = 0;
            }
            holder.directionTV.setText(DIRS[windIndex]);

            // Make sure the text isnt bold
            holder.timeTV.setTypeface(null, Typeface.NORMAL);
            holder.wvhtTV.setTypeface(null, Typeface.NORMAL);
            holder.periodTV.setTypeface(null, Typeface.NORMAL);
            holder.directionTV.setTypeface(null, Typeface.NORMAL);

            // Make sure the text color is black
            holder.timeTV.setTextColor(Color.BLACK);
            holder.wvhtTV.setTextColor(Color.BLACK);
            holder.periodTV.setTextColor(Color.BLACK);
            holder.directionTV.setTextColor(Color.BLACK);
        }

        // Return the row view to be rendered
        return rowView;
    }
}

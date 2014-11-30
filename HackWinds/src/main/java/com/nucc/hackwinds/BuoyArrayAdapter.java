package com.nucc.hackwinds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import java.util.ArrayList;

public class BuoyArrayAdapter extends ArrayAdapter<Buoy> {
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

            rowView.setTag(viewHolder);
        }
        // Get the buoy item for the list position
        Buoy buoy = values.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Set the data into the text views
        holder.timeTV.setText(buoy.time);
        holder.wvhtTV.setText(buoy.wvht);
        holder.periodTV.setText(buoy.dpd);
        holder.directionTV.setText(buoy.dir);

        // Return the row view
        return rowView;
    }
}

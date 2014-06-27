package com.nucc.hackwinds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class TideArrayAdapter extends ArrayAdapter<Tide> {
    private final Context context;
    private final ArrayList<Tide> values;

    static class ViewHolder {
        public TextView dayTV;
        public TextView lowtide1TV;
        public TextView lowtide2TV;
        public TextView hightide1TV;
        public TextView hightide2TV;
        public TextView sunriseTV;
        public TextView sunsetTV;
    }

    public TideArrayAdapter(Context context, ArrayList<Tide> values) {
        super(context, R.layout.tide_item, values);
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
            rowView = inflater.inflate(R.layout.tide_item, parent, false);

            // Set the view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.dayTV = (TextView) rowView.findViewById(R.id.tideHeader);
            viewHolder.lowtide1TV = (TextView) rowView.findViewById(R.id.lowTide1);
            viewHolder.lowtide2TV = (TextView) rowView.findViewById(R.id.lowTide2);
            viewHolder.hightide1TV = (TextView) rowView.findViewById(R.id.highTide1);
            viewHolder.hightide2TV = (TextView) rowView.findViewById(R.id.highsTide2);
            viewHolder.sunriseTV = (TextView) rowView.findViewById(R.id.sunriseData);
            viewHolder.sunsetTV = (TextView) rowView.findViewById(R.id.sunsetData);
            rowView.setTag(viewHolder);
        }
        // fill the data
        Tide tide = values.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.dayTV.setText(tide.day);
        holder.lowtide1TV.setText(tide.lowTide1);
        holder.lowtide2TV.setText(tide.lowTide2);
        holder.hightide1TV.setText(tide.highTide1);
        holder.hightide2TV.setText(tide.highTide2);
        holder.sunriseTV.setText(tide.sunrise);
        holder.sunsetTV.setText(tide.sunset);

        // Return the completed view to render on screen
        return rowView;
    }
}

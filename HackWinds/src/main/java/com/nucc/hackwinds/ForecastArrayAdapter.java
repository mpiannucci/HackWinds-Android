package com.nucc.hackwinds;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.Forecast;

import android.os.Bundle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class ForecastArrayAdapter extends ArrayAdapter<Forecast> {
    private final Context context;
    private final ArrayList<Forecast> values;

    static class ViewHolder {
        public TextView dayTV;
        public TextView overviewTV;
        public TextView detailTV;
    }

    public ForecastArrayAdapter(Context context, ArrayList<Forecast> values) {
        super(context, R.layout.forecast_item, values);
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
            rowView = inflater.inflate(R.layout.forecast_item, parent, false);

            // Set the view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.dayTV = (TextView) rowView.findViewById(R.id.forecastHeader);
            viewHolder.overviewTV = (TextView) rowView.findViewById(R.id.forecastOverview);
            viewHolder.detailTV = (TextView) rowView.findViewById(R.id.forecastDetail);

            rowView.setTag(viewHolder);
        }
        // Fill the data
        Forecast forecast = values.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.dayTV.setText(forecast.day);
        holder.overviewTV.setText(forecast.overview);
        holder.detailTV.setText(forecast.detail);
        
        // Return the completed view to render on screen
        return rowView;
    }
}
package com.nucc.hackwinds;

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

    // View holder class so views can be recycled
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

            // Set the tag so the views can be recycled
            rowView.setTag(viewHolder);
        }
        // Get the forecast object and the view holder
        Forecast forecast = values.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Set the day, brief, overview and detailed text views
        holder.dayTV.setText(forecast.Day);
        holder.overviewTV.setText(forecast.Overview);
        holder.detailTV.setText(forecast.Detailed);

        // Return the completed view to render on screen
        return rowView;
    }
}
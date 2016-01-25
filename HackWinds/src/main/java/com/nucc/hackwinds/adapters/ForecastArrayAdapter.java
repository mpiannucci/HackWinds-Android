package com.nucc.hackwinds.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nucc.hackwinds.types.Forecast;
import com.nucc.hackwinds.R;

import java.util.ArrayList;
import java.util.Calendar;

public class ForecastArrayAdapter extends ArrayAdapter<Forecast> {
    private final Context context;
    private final int currentDay;

    public ArrayList<Pair<Forecast, Forecast>> values;

    // View holder class so views can be recycled
    static class ViewHolder {
        public TextView dayTV;
        public TextView morningHeaderTV;
        public TextView morningDataTV;
        public TextView afternoonHeaderTV;
        public TextView afternoonDataTV;
    }

    public ForecastArrayAdapter(Context ctx, ArrayList<Forecast> vals) {
        super(ctx, R.layout.forecast_item, vals);
        this.context = ctx;

        ArrayList<Pair<Forecast, Forecast>> forecasts = new ArrayList<>();
        for (int i = 0; i < 10; i += 2) {
            Pair<Forecast, Forecast> thisDay = new Pair <> (vals.get(i), vals.get(i+1));
            forecasts.add(thisDay);
        }
        this.values = forecasts;

        Calendar calendar = Calendar.getInstance();
        this.currentDay = calendar.get(Calendar.DAY_OF_WEEK);
    }

    public void setForecastData(ArrayList<Forecast> newValues) {
        ArrayList<Pair<Forecast, Forecast>> forecasts = new ArrayList<>();
        for (int i = 0; i < 10; i += 2) {
            Pair<Forecast, Forecast> thisDay = new Pair <> (newValues.get(i), newValues.get(i+1));
            forecasts.add(thisDay);
        }
        this.values = forecasts;
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
            viewHolder.morningHeaderTV = (TextView) rowView.findViewById(R.id.forecastMorningHeader);
            viewHolder.morningDataTV = (TextView) rowView.findViewById(R.id.forecastMorningData);
            viewHolder.afternoonHeaderTV = (TextView) rowView.findViewById(R.id.forecastAfternoonHeader);
            viewHolder.afternoonDataTV = (TextView) rowView.findViewById(R.id.forecastAfternoonData);

            // Set the tag so the views can be recycled
            rowView.setTag(viewHolder);
        }

        // Get the forecast object and the view holder
        Pair<Forecast, Forecast> thisDay = values.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Set the day text view
        String day = getContext().getResources().getStringArray(R.array.daysOfTheWeek)[(((currentDay -1) + position)%7)];
        holder.dayTV.setText(day);

        // Set the morning and afternoon data
        holder.morningDataTV.setText(String.format("%s - %s feet, Wind %s %s mph", thisDay.first.minBreakHeight,
                thisDay.first.maxBreakHeight, thisDay.first.windSpeed, thisDay.first.windDirection));
        holder.afternoonDataTV.setText(String.format("%s - %s feet, Wind %s %s mph", thisDay.second.minBreakHeight,
                thisDay.second.maxBreakHeight, thisDay.second.windSpeed, thisDay.second.windDirection));

        // Set the color of the time of day header based on the swell
        if (Double.valueOf(thisDay.first.minBreakHeight) > 1.9) {
            if (thisDay.first.windDirection.equals("WSW") ||
                    thisDay.first.windDirection.equals("W") ||
                    thisDay.first.windDirection.equals("WNW") ||
                    thisDay.first.windDirection.equals("NW") ||
                    thisDay.first.windDirection.equals("N")) {
                holder.morningHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_green));
            } else if (Double.valueOf(thisDay.first.windSpeed) < 8.0) {
                holder.morningHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_green));
            } else {
                holder.morningHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_yellow));
            }
        } else {
            holder.morningHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_red));
        }

        if (Double.valueOf(thisDay.second.minBreakHeight) > 1.9) {
            if (thisDay.second.windDirection.equals("WSW") ||
                    thisDay.second.windDirection.equals("W") ||
                    thisDay.second.windDirection.equals("WNW") ||
                    thisDay.second.windDirection.equals("NW") ||
                    thisDay.second.windDirection.equals("N")) {
                holder.afternoonHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_green));
            } else if (Double.valueOf(thisDay.second.windSpeed) < 8.0) {
                holder.afternoonHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_green));
            } else {
                holder.afternoonHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_yellow));
            }
        } else {
            holder.afternoonHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_red));
        }

        // Return the completed view to render on screen
        return rowView;
    }
}
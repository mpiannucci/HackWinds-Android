package com.nucc.hackwinds;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class ForecastArrayAdapter extends ArrayAdapter<Forecast> {
    private final Context context;
    private final ArrayList<Pair<Forecast, Forecast>> values;
    private final int currentDay;

    // View holder class so views can be recycled
    static class ViewHolder {
        public TextView dayTV;
        public TextView morningHeaderTV;
        public TextView morningDataTV;
        public TextView afternoonHeaderTV;
        public TextView afternoonDataTV;
    }

    public ForecastArrayAdapter(Context context, ArrayList<Forecast> values) {
        super(context, R.layout.forecast_item, values);
        this.context = context;

        ArrayList<Pair<Forecast, Forecast>> forecasts = new ArrayList<>();
        for (int i = 0; i < 10; i += 2) {
            Pair<Forecast, Forecast> thisDay = new Pair <> (values.get(i), values.get(i+1));
            forecasts.add(thisDay);
        }
        this.values = forecasts;

        Calendar calendar = Calendar.getInstance();
        this.currentDay = calendar.get(Calendar.DAY_OF_WEEK);
    }

    @Override
    public int getCount() {
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
        String day = getContext().getResources().getStringArray(R.array.daysOfTheWeek)[(((currentDay-1) + position)%7)];
        holder.dayTV.setText(day);

        // Set the morning and afternoon data
        holder.morningDataTV.setText(String.format("%s - %s feet, Wind %s %s mph", thisDay.first.MinBreakHeight,
                thisDay.first.MaxBreakHeight, thisDay.first.WindSpeed, thisDay.first.WindDirection));
        holder.afternoonDataTV.setText(String.format("%s - %s feet, Wind %s %s mph", thisDay.second.MinBreakHeight,
                thisDay.second.MaxBreakHeight, thisDay.second.WindSpeed, thisDay.second.WindDirection));

        // Set the color of the time of day header based on the swell
        if (Double.valueOf(thisDay.first.MinBreakHeight) > 1.9) {
            if (thisDay.first.WindDirection.equals("WSW") ||
                    thisDay.first.WindDirection.equals("W") ||
                    thisDay.first.WindDirection.equals("WNW") ||
                    thisDay.first.WindDirection.equals("NW") ||
                    thisDay.first.WindDirection.equals("N")) {
                holder.morningHeaderTV.setTextColor(getContext().getResources().getColor(R.color.forecast_green));
            } else if (Double.valueOf(thisDay.first.WindSpeed) < 8.0) {
                holder.morningHeaderTV.setTextColor(getContext().getResources().getColor(R.color.forecast_green));
            } else {
                holder.morningHeaderTV.setTextColor(getContext().getResources().getColor(R.color.forecast_yellow));
            }
        } else {
            holder.morningHeaderTV.setTextColor(getContext().getResources().getColor(R.color.forecast_red));
        }

        if (Double.valueOf(thisDay.second.MinBreakHeight) > 1.9) {
            if (thisDay.second.WindDirection.equals("WSW") ||
                    thisDay.second.WindDirection.equals("W") ||
                    thisDay.second.WindDirection.equals("WNW") ||
                    thisDay.second.WindDirection.equals("NW") ||
                    thisDay.second.WindDirection.equals("N")) {
                holder.afternoonHeaderTV.setTextColor(getContext().getResources().getColor(R.color.forecast_green));
            } else if (Double.valueOf(thisDay.second.WindSpeed) < 8.0) {
                holder.afternoonHeaderTV.setTextColor(getContext().getResources().getColor(R.color.forecast_green));
            } else {
                holder.afternoonHeaderTV.setTextColor(getContext().getResources().getColor(R.color.forecast_yellow));
            }
        } else {
            holder.afternoonHeaderTV.setTextColor(getContext().getResources().getColor(R.color.forecast_red));
        }

        // Return the completed view to render on screen
        return rowView;
    }
}
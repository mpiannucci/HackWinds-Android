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
import com.nucc.hackwinds.types.ForecastDailySummary;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ForecastArrayAdapter extends ArrayAdapter<ForecastDailySummary> {
    private final Context context;
    private int currentDay;

    public ArrayList<ForecastDailySummary> values;

    // View holder class so views can be recycled
    static class ViewHolder {
        public TextView dayTV;
        public TextView morningHeaderTV;
        public TextView morningDataTV;
        public TextView afternoonHeaderTV;
        public TextView afternoonDataTV;
    }

    public ForecastArrayAdapter(Context ctx, ArrayList<ForecastDailySummary> vals) {
        super(ctx, R.layout.forecast_item, vals);
        this.context = ctx;

        this.values = vals;

        Calendar calendar = Calendar.getInstance();
        this.currentDay = calendar.get(Calendar.DAY_OF_WEEK);
    }

    public void setForecastData(ArrayList<ForecastDailySummary> newValues) {
        this.values = newValues;
        Calendar calendar = Calendar.getInstance();
        this.currentDay = calendar.get(Calendar.DAY_OF_WEEK);
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
            viewHolder.dayTV = (TextView) rowView.findViewById(R.id.forecast_header);
            viewHolder.morningHeaderTV = (TextView) rowView.findViewById(R.id.forecast_morning_header);
            viewHolder.morningDataTV = (TextView) rowView.findViewById(R.id.forecast_morning_data);
            viewHolder.afternoonHeaderTV = (TextView) rowView.findViewById(R.id.forecast_afternoon_header);
            viewHolder.afternoonDataTV = (TextView) rowView.findViewById(R.id.forecast_afternoon_data);

            // Set the tag so the views can be recycled
            rowView.setTag(viewHolder);
        }

        // Get the forecast object and the view holder
        ForecastDailySummary thisDay = values.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Set the day text view
        String day = getContext().getResources().getStringArray(R.array.daysOfTheWeek)[(((currentDay - 1) + position)%7)];
        holder.dayTV.setText(day);

        if (thisDay.morningWindCompassDirection.equals("")) {
            holder.morningHeaderTV.setVisibility(View.GONE);
            holder.morningDataTV.setVisibility(View.GONE);
        } else if (thisDay.afternoonWindCompassDirection.equals("")) {
            holder.afternoonHeaderTV.setVisibility(View.GONE);
            holder.afternoonDataTV.setVisibility(View.GONE);
        } else {
            holder.morningHeaderTV.setVisibility(View.VISIBLE);
            holder.morningDataTV.setVisibility(View.VISIBLE);
            holder.afternoonHeaderTV.setVisibility(View.VISIBLE);
            holder.afternoonDataTV.setVisibility(View.VISIBLE);
        }

        // Set the morning and afternoon data
        holder.morningDataTV.setText(String.format(Locale.US, "%d - %d feet, Wind %s %d mph", (int)thisDay.morningMinimumWaveHeight,
                (int)thisDay.morningMaximumWaveHeight, thisDay.morningWindCompassDirection, (int)thisDay.morningWindSpeed));
        holder.afternoonDataTV.setText(String.format(Locale.US, "%d - %d feet, Wind %s %d mph", (int)thisDay.afternoonMinimumWaveHeight, (int)thisDay.afternoonMaximumWaveHeight, thisDay.afternoonWindCompassDirection, (int)thisDay.afternoonWindSpeed));

        // Set the color of the time of day header based on the swell
        if (thisDay.morningMinimumWaveHeight > 1.9) {
            if (thisDay.morningWindCompassDirection.equals("WSW") ||
                    thisDay.morningWindCompassDirection.equals("W") ||
                    thisDay.morningWindCompassDirection.equals("WNW") ||
                    thisDay.morningWindCompassDirection.equals("NW") ||
                    thisDay.morningWindCompassDirection.equals("N")) {
                holder.morningHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_green));
            } else if (thisDay.morningWindSpeed < 8.0) {
                holder.morningHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_green));
            } else {
                holder.morningHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_yellow));
            }
        } else {
            holder.morningHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_red));
        }

        if (thisDay.afternoonMinimumWaveHeight > 1.9) {
            if (thisDay.afternoonWindCompassDirection.equals("WSW") ||
                    thisDay.afternoonWindCompassDirection.equals("W") ||
                    thisDay.afternoonWindCompassDirection.equals("WNW") ||
                    thisDay.afternoonWindCompassDirection.equals("NW") ||
                    thisDay.afternoonWindCompassDirection.equals("N")) {
                holder.afternoonHeaderTV.setTextColor(ContextCompat.getColor(context, R.color.forecast_green));
            } else if (thisDay.afternoonWindSpeed < 8.0) {
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
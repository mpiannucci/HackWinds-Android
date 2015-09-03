package com.nucc.hackwinds.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nucc.hackwinds.types.Buoy;
import com.nucc.hackwinds.R;

import java.util.ArrayList;

public class BuoyArrayAdapter extends ArrayAdapter<Buoy> {

    private final Context mContext;
    private ArrayList<Buoy> mValues;

    static class ViewHolder {
        public TextView timeTV;
        public TextView wvhtTV;
        public TextView periodTV;
        public TextView directionTV;
    }

    public BuoyArrayAdapter(Context context, ArrayList<Buoy> values) {
        super(context, R.layout.buoy_item, values);
        this.mContext = context;
        this.mValues = values;
    }

    public void setBuoyData(ArrayList<Buoy> values) {
        this.mValues = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // Make the view reusable
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
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
            holder.timeTV.setTextColor(mContext.getResources().getColor(R.color.hackwinds_blue));
            holder.wvhtTV.setTextColor(mContext.getResources().getColor(R.color.hackwinds_blue));
            holder.periodTV.setTextColor(mContext.getResources().getColor(R.color.hackwinds_blue));
            holder.directionTV.setTextColor(mContext.getResources().getColor(R.color.hackwinds_blue));
        } else {
            // Get the buoy item for the list position
            Buoy buoy = mValues.get(position-1);

            // Set the data into the text views
            holder.timeTV.setText(buoy.Time);
            holder.wvhtTV.setText(buoy.SignificantWaveHeight);
            holder.periodTV.setText(buoy.DominantPeriod);
            holder.directionTV.setText(Buoy.getCompassDirection(buoy.MeanDirection));

            // Make sure the text isn't bold
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

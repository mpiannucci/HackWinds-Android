package com.nucc.hackwinds;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TideArrayAdapter extends ArrayAdapter<Tide> {
    private final Context context;
    private final ArrayList<Tide> values;

    // Array of all of the possible textviews that could be set for the data field
    private int[] headerIDs = new int[] {R.id.tideHeader1, R.id.tideHeader2, R.id.tideHeader3,
        R.id.tideHeader4, R.id.tideHeader5, R.id.tideHeader6};

    // Array of all of the possible textviews that could be set for the data fields
    private int[] dataIDs = new int[] {R.id.tideData1, R.id.tideData2, R.id.tideData3,
            R.id.tideData4, R.id.tideData5, R.id.tideData6};

    static class ViewHolder {
        // View holder to save the views for recylcling
        public TextView dayTV;
        public TextView[] headers;
        public TextView[] datas;
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
            // Get the tide item layout into a new rowview
            rowView = inflater.inflate(R.layout.tide_item, parent, false);

            // Set the view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.headers = new TextView[6];
            viewHolder.datas = new TextView[6];
            viewHolder.dayTV = (TextView) rowView.findViewById(R.id.tideHeader);
            for (int i=0; i<6; i++) {
                viewHolder.headers[i] = (TextView)rowView.findViewById(headerIDs[i]);
                viewHolder.datas[i] = (TextView) rowView.findViewById(dataIDs[i]);
            }
            // Set the tag so it can be recycled
            rowView.setTag(viewHolder);
        }
        // fill the data
        Tide tide = values.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Set the day header
        holder.dayTV.setText(tide.day);

        // Loop through all of the possible days
        for (int i=0; i<6; i++) {
            if ((tide.dType[i] == null) || (tide.dValue[i] == null)) {
                // If there was not enough data to fill all of the views, hide them
                holder.headers[i].setVisibility(View.GONE);
                holder.datas[i].setVisibility(View.GONE);
            }
            else {
                // Make sure the views are visible since its possible they could have been hidden
                holder.headers[i].setVisibility(View.VISIBLE);
                holder.datas[i].setVisibility(View.VISIBLE);

                // Set the data and header value for the iteration
                holder.headers[i].setText(tide.dType[i]);
                holder.datas[i].setText(tide.dValue[i]);
                if ((tide.dType[i].equals("Sunrise")) || (tide.dType[i].equals("Sunset"))) {
                    // If its a sunrise or sunset, make the text bold
                    holder.headers[i].setTypeface(null, Typeface.BOLD);
                    holder.datas[i].setTypeface(null, Typeface.BOLD);

                    // Also make the text blue
                    holder.headers[i].setTextColor(context.getResources().getColor(R.color.hackwinds_blue));
                    holder.datas[i].setTextColor(context.getResources().getColor(R.color.hackwinds_blue));
                }
                else {
                    // Its not a sunrise or sunset so make sure the text is normal
                    holder.headers[i].setTypeface(null, Typeface.NORMAL);
                    holder.datas[i].setTypeface(null, Typeface.NORMAL);

                    // Also make sure that the color of the text is black
                    holder.headers[i].setTextColor(context.getResources().getColor(R.color.black));
                    holder.datas[i].setTextColor(context.getResources().getColor(R.color.black));
                }
            }
        }

        // Return the completed view to render on screen
        return rowView;
    }
}

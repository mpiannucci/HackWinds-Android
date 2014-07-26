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
    private int[] headerIDs = new int[] {R.id.tideHeader1, R.id.tideHeader2, R.id.tideHeader3,
        R.id.tideHeader4, R.id.tideHeader5, R.id.tideHeader6};
    private int[] dataIDs = new int[] {R.id.tideData1, R.id.tideData2, R.id.tideData3,
            R.id.tideData4, R.id.tideData5, R.id.tideData6};

    static class ViewHolder {
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
            rowView.setTag(viewHolder);
        }
        // fill the data
        Tide tide = values.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.dayTV.setText(tide.day);
        for (int i=0; i<6; i++) {
            if ((tide.dType[i] == null) || (tide.dValue[i] == null)) {
                holder.headers[i].setVisibility(View.GONE);
                holder.datas[i].setVisibility(View.GONE);
            }
            else {
                holder.headers[i].setVisibility(View.VISIBLE);
                holder.datas[i].setVisibility(View.VISIBLE);
                holder.headers[i].setText(tide.dType[i]);
                holder.datas[i].setText(tide.dValue[i]);
            }
        }

        // Return the completed view to render on screen
        return rowView;
    }
}

package com.nucc.hackwinds.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nucc.hackwinds.types.Tide;
import com.nucc.hackwinds.R;

import java.util.ArrayList;

public class TideScheduleArrayAdapter extends ArrayAdapter<Tide> {
    private final Context mContext;

    private ArrayList<Tide> mTides;
    private ArrayList<String> mDays;
    private ArrayList<Integer> mDayDataCounts;

    static class ViewHolder {
        public TextView dayTV;
        public ImageView tideIconView;
        public TextView tideEventTypeTV;
        public TextView tideEventTimeTV;
    }

    public TideScheduleArrayAdapter(Context ctx, ArrayList<Tide> vals) {
        super(ctx, R.layout.tide_item, vals);

        mContext = ctx;
        mTides = new ArrayList<>();
        mDays = new ArrayList<>();
        mDayDataCounts = new ArrayList<>();
    }

    public TideScheduleArrayAdapter(Context ctx, ArrayList<Tide> tides, ArrayList<String> days, ArrayList<Integer> dayDataCounts) {
        super(ctx, R.layout.tide_item, tides);

        mContext = ctx;
        mTides = tides;
        mDays = days;
        mDayDataCounts = dayDataCounts;
    }

    public void setTideData(ArrayList<Tide> tides, ArrayList<String> days, ArrayList<Integer> dayDataCounts) {
        mTides = tides;
        mDays = days;
        mDayDataCounts = dayDataCounts;
    }

    @Override
    public int getCount() {
        int count = 0;
        for (Integer dayCount : mDayDataCounts) {
            count += dayCount;
        }
        count += mDayDataCounts.size();
        return count;
    }

    public boolean isHeaderItem(int index) {
        // TODO
        return false;
    }

    public String getHeaderTitle(int index) {
        if (!isHeaderItem(index)) {
            return "";
        }

        // TODO
        return "";
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // Make the view reusable
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            ViewHolder viewHolder = new ViewHolder();
            if (isHeaderItem(position)) {
                rowView = inflater.inflate(R.layout.tide_header_item, parent, false);
                viewHolder.dayTV = (TextView) rowView.findViewById(R.id.tide_section_header_text);
            } else {
                rowView = inflater.inflate(R.layout.tide_item, parent, false);
                viewHolder.tideIconView = (ImageView) rowView.findViewById(R.id.tide_icon);
                viewHolder.tideEventTypeTV = (TextView) rowView.findViewById(R.id.tide_event_type);
                viewHolder.tideEventTimeTV = (TextView) rowView.findViewById(R.id.tide_event_time);
            }
            rowView.setTag(viewHolder);
        }

        return rowView;
    }
}

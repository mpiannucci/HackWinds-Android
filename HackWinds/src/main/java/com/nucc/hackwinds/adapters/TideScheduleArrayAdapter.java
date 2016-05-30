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
    private final int SECTION_TYPE = 0;
    private final int DATA_TYPE = 1;

    private ArrayList<Tide> mTides;

    static class SectionViewHolder {
        public TextView dayTV;
    }

    static class TideViewHolder {
        public ImageView tideIconView;
        public TextView tideEventTypeTV;
        public TextView tideEventTimeTV;
    }

    public TideScheduleArrayAdapter(Context ctx, ArrayList<Tide> tides) {
        super(ctx, R.layout.tide_item, tides);

        mContext = ctx;
        mTides = tides;
    }

    public void setTideData(ArrayList<Tide> tides) {
        mTides = tides;
    }

    @Override
    public int getCount() {
        return mTides.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        boolean isSection = mTides.get(position).isDayItem();
        if (isSection) {
            return SECTION_TYPE;
        } else {
            return DATA_TYPE;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        switch (viewType) {
            case SECTION_TYPE:
                View sectionView = convertView;
                SectionViewHolder sectionHolder;
                if (sectionView == null) {
                    LayoutInflater inflater = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    sectionView = inflater.inflate(R.layout.tide_header_item, parent, false);
                    sectionHolder = new SectionViewHolder();
                    sectionHolder.dayTV = (TextView) sectionView.findViewById(R.id.tide_section_header_text);
                    sectionView.setTag(sectionHolder);
                } else {
                    sectionHolder = (SectionViewHolder) sectionView.getTag();
                }

                Tide sectionTide = mTides.get(position);

                if (sectionHolder != null) {
                    if (sectionHolder.dayTV != null) {
                        sectionHolder.dayTV.setText(sectionTide.day);
                    }
                }

                return sectionView;
            case DATA_TYPE:
                View dataView = convertView;
                TideViewHolder tideViewHolder;
                if (dataView == null) {
                    LayoutInflater inflater = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    dataView = inflater.inflate(R.layout.tide_item, parent, false);
                    tideViewHolder = new TideViewHolder();
                    tideViewHolder.tideIconView = (ImageView) dataView.findViewById(R.id.tide_icon);
                    tideViewHolder.tideEventTypeTV = (TextView) dataView.findViewById(R.id.tide_event_type);
                    tideViewHolder.tideEventTimeTV = (TextView) dataView.findViewById(R.id.tide_event_time);
                    dataView.setTag(tideViewHolder);
                } else {
                    tideViewHolder = (TideViewHolder) dataView.getTag();
                }

                Tide dataTide = mTides.get(position);

                // Set the tide data
                if (dataTide.isTidalEvent()) {
                    tideViewHolder.tideEventTimeTV.setText(dataTide.getTimeString() + ": " + dataTide.height);
                } else {
                    tideViewHolder.tideEventTimeTV.setText(dataTide.getTimeString());
                }
                tideViewHolder.tideEventTypeTV.setText(dataTide.eventType);

                // Set the correct icon
                if (dataTide.isHighTide()) {
                    tideViewHolder.tideIconView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_trending_up_white_36dp));
                    tideViewHolder.tideIconView.setColorFilter(mContext.getResources().getColor(R.color.hackwinds_blue));
                } else if (dataTide.isLowTide()) {
                    tideViewHolder.tideIconView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_trending_down_white_36dp));
                    tideViewHolder.tideIconView.setColorFilter(mContext.getResources().getColor(R.color.hackwinds_blue));
                } else if (dataTide.isSunrise()) {
                    tideViewHolder.tideIconView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_brightness_high_white_36dp));
                    tideViewHolder.tideIconView.setColorFilter(mContext.getResources().getColor(android.R.color.holo_orange_dark));
                } else if (dataTide.isSunset()) {
                    tideViewHolder.tideIconView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_brightness_low_white_36dp));
                    tideViewHolder.tideIconView.setColorFilter(mContext.getResources().getColor(android.R.color.holo_orange_dark));
                }

                return dataView;
        }
        return null;
    }
}

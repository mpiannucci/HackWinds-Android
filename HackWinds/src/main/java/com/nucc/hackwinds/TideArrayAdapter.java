package com.nucc.hackwinds;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.Tide;

import android.os.Bundle;
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

    static class ViewHolder {
        public TextView dayTV;
        public TextView lowtide1TV;
        public TextView lowtide2TV;
        public TextView hightide1TV;
        public TextView hightide2TV;
        public TextView sunriseTV;
        public TextView sunsetTV;
    }

    public TideArrayAdapter(Context context, ArrayList<Tide> values) {
        super(context, R.layout.tide_item, values);
        this.context = context;
        this.values = values;
    }
}

package com.nucc.hackwinds;

import com.nucc.hackwinds.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConditionArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public ConditionArrayAdapter(Context context, String[] values) {
        super(context, R.layout.list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.itemHeader);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.itemThumb);
        imageView.setImageResource(R.drawable.arrow_up);
        textView.setText(values[position]);
        return rowView;
    }
}
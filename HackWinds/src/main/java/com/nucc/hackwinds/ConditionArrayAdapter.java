package com.nucc.hackwinds;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.Condition;

import android.os.Bundle;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.AsyncTask;
import java.util.ArrayList;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.BitmapFactory;
import java.lang.Double;
import java.lang.Float;

public class ConditionArrayAdapter extends ArrayAdapter<Condition> {
    private final Context context;
    private final ArrayList<Condition> values;

    static class ViewHolder {
        public TextView dateTV;
        public TextView breakTV;
        public ImageView breakIV;
        public TextView windTV;
        //public ImageView windIV;
        public TextView swellTV;
        //public ImageView swellIV;
        public int position;
    }


    public ConditionArrayAdapter(Context context, ArrayList<Condition> values) {
        super(context, R.layout.current_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // Make the view reusable
        if (rowView == null) { 
            LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.current_item, parent, false);

            // Set the view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.dateTV = (TextView) rowView.findViewById(R.id.itemHeader);
            viewHolder.breakTV = (TextView) rowView.findViewById(R.id.breakData);
            viewHolder.breakIV = (ImageView) rowView.findViewById(R.id.breakThumb);
            viewHolder.windTV = (TextView) rowView.findViewById(R.id.windData);
            //viewHolder.windIV = (ImageView) rowView.findViewById(R.id.windThumb);
            viewHolder.swellTV = (TextView) rowView.findViewById(R.id.swellData);
            //viewHolder.swellIV = (ImageView) rowView.findViewById(R.id.swellThumb);
            viewHolder.position = position;
            rowView.setTag(viewHolder);
        }
        // Fill the data
        Condition condition = values.get(position);
        ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.dateTV.setText(condition.date);
        holder.breakTV.setText(condition.minBreak+" - "+condition.maxBreak+" feet");
        if ((Double.valueOf(condition.minBreak) >= 2) || (Double.valueOf(condition.maxBreak) >= 3)) {
            holder.breakIV.setImageResource(R.drawable.thumbs_up);
        }else {
            holder.breakIV.setImageResource(R.drawable.thumbs_down);
        }
        holder.windTV.setText(condition.windDir+" "+condition.windSpeed+" mph");
        //holder.windIV.setImageResource(R.drawable.arrow_up);
        holder.swellTV.setText(condition.swellDeg+" "+condition.swellHeight+" ft @ "+condition.swellPeriod+" s");
        //holder.swellIV.setImageResource(R.drawable.arrow_up);
        
        // Return the completed view to render on screen
        return rowView;
    }

    private Bitmap rotateImage(Bitmap src, float degree) {
        // create new matrix object
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        // return new bitmap rotated using matrix
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    // Using an AsyncTask to load the slow images in a background thread
}
package com.nucc.hackwinds;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class CurrentFragment extends ListFragment {

    String streamURL = "http://162.243.101.197:1935/surfcam/live.stream/playlist.m3u8";
    public String mswURL = "http://magicseaweed.com/api/nFSL2f845QOAf1Tuv7Pf5Pd9PXa5sVTS/forecast/?spot_id=1103&fields=localTimestamp,swell.*,wind.*";
    String[] days = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    int cacheDuration = 3000;
    static String imgUrl = "http://www.warmwinds.com/wp-content/uploads/surf-cam-stills/image00001.jpg";

    ArrayList<Condition> conditionValues;
    ConditionArrayAdapter adapter;
    public VideoView streamView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // public Condition(String date, String minbreak, String maxBreak, 
        //     String windSpeed, String windDeg, String windDir, String swellHeight,
        //     String swellPeriod, String swellDeg) 
        conditionValues = new ArrayList<Condition>();
        new BackgroundMSWAsyncTask().execute(6);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.current_fragment, container, false);

        TextView date = (TextView) V.findViewById(R.id.dateHeader);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        date.setText(days[day-1]);

        ImageView img = (ImageView) V.findViewById(R.id.imageOverlay);
        Ion.with(getActivity()).load(imgUrl).intoImageView(img);
        img.getLayoutParams().width = ActionBar.LayoutParams.MATCH_PARENT;
        img.setScaleType(ImageView.ScaleType.FIT_XY);
        img.setAdjustViewBounds(true);

        ImageView pb = (ImageView) V.findViewById(R.id.pbOverlay);

        pb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                ImageView pic = (ImageView) getActivity().findViewById(R.id.imageOverlay);
                pic.setVisibility(View.GONE);
                streamView = (VideoView) getActivity().findViewById(R.id.currentVideoStreamView);
                streamView.setVisibility(View.VISIBLE);
                new BackgroundVideoAsyncTask().execute(streamURL);
            }
        });

        return V;
    }

    public class BackgroundVideoAsyncTask extends AsyncTask<String, Uri, Void> {
        ProgressDialog dialog;
 
        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading Live Stream...");
            dialog.setCancelable(true);
            dialog.show();
        }
 
        protected void onProgressUpdate(final Uri... uri) {
 
            try {
                streamView.setVideoURI(uri[0]);
                streamView.requestFocus();
                streamView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
 
                    public void onPrepared(MediaPlayer arg0) {
                        streamView.start();
                        dialog.dismiss();
                    }
                });
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
 
        @Override
        protected Void doInBackground(String... params) {
            try {
                Uri uri = Uri.parse(params[0]);
                 
                publishProgress(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }   
    }

    public class BackgroundMSWAsyncTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... ints) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
 
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(mswURL, ServiceHandler.GET);
            if (jsonStr != null) {
                try {
                    JSONArray jsonArr = new JSONArray(jsonStr);
                    int i = 0;
                    int j = 0;
                    while (i < ints[0]) {
                        
                        JSONObject jsonObj = jsonArr.getJSONObject(j);
                        j++;

                        // Fill a new Condition object and append it
                        String date = formatDate(jsonObj.getLong("localTimestamp"));
                        if (checkDate(date) == false) {
                            continue;
                        }

                        JSONObject swell = jsonObj.getJSONObject("swell");
                        JSONObject wind = jsonObj.getJSONObject("wind");

                        String minBreak = swell.getString("minBreakingHeight");
                        String maxBreak = swell.getString("maxBreakingHeight");
                        String windSpeed = wind.getString("speed");
                        String windDeg = wind.getString("direction");
                        String windDir = wind.getString("compassDirection");
                        String swellHeight = swell.getJSONObject("components").getJSONObject("primary").getString("height");
                        String swellPeriod = swell.getJSONObject("components").getJSONObject("primary").getString("period");
                        String swellDir = swell.getJSONObject("components").getJSONObject("primary").getString("compassDirection");
                        
                        conditionValues.add(new Condition(date, minBreak, maxBreak, windSpeed, windDeg,
                            windDir, swellHeight, swellPeriod, swellDir));
                        i++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("hackwinds", "Couldn't get any data from the msw url");
            }
 
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            adapter = new ConditionArrayAdapter(getActivity(), conditionValues);
            setListAdapter(adapter);
        }
 
    }

    // Return a pretty timestamp for headers
    public String formatDate(Long timestamp) {
        Date date = new Date(timestamp*1000);
        DateFormat format = new SimpleDateFormat("K a");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        String formatted = format.format(date);
        if (formatted.indexOf("0") > -1) {
            format = new SimpleDateFormat("HH a");
            format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            formatted = format.format(date);
        }
        return formatted;
    }

    // Check the time. If its irrelevant, skip the JSon Objects
    public boolean checkDate(String dateString) {
        int ampmStamp = dateString.indexOf("AM"); 
        int hour00 = dateString.indexOf("0");
        int hour03 = dateString.indexOf("3");
        if (((ampmStamp > -1) && (hour00 > -1)) || ((ampmStamp > -1) && (hour03 > -1))) {
            return false;
        }
        return true;
    }
}
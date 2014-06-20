package com.nucc.hackwinds;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.Condition;
import com.nucc.hackwinds.ConditionArrayAdapter;
import com.nucc.hackwinds.ServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;
import android.net.Uri;
import android.view.LayoutInflater;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import java.lang.Integer;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.lang.Long;


public class currentFragment extends ListFragment {

    String streamURL = "http://162.243.101.197:1935/surfcam/live.stream/playlist.m3u8";
    public String mswURL = "http://magicseaweed.com/api/nFSL2f845QOAf1Tuv7Pf5Pd9PXa5sVTS/forecast/?spot_id=1103&fields=localTimestamp,swell.*,wind.*";
    //String[] days = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    //String wuURL = "http://api.wunderground.com/api/2e5424aab8c91757/tide/q/RI/Point_Judith.json";

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
        new BackgroundMSWAsyncTask().execute(1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.current_fragment, container, false);
        streamView = (VideoView) V.findViewById(R.id.currentVideoStreamView);
        new BackgroundVideoAsyncTask().execute(streamURL);
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
                    for (int i = 0; i < ints[0]; i++) {
                        JSONObject jsonObj = jsonArr.getJSONObject(i);

                        // Fill a new Condition object and append it
                        String date = formatDate(jsonObj.getLong("localTimestamp"));
                        JSONObject swell = jsonObj.getJSONObject("swell");
                        JSONObject wind = jsonObj.getJSONObject("wind");

                        String minBreak = swell.getString("minBreakingHeight");
                        String maxBreak = swell.getString("maxBreakingHeight");
                        String windSpeed = wind.getString("speed");
                        String windDeg = wind.getString("direction");
                        String windDir = wind.getString("compassDirection");
                        String swellHeight = swell.getJSONObject("components").getJSONObject("primary").getString("height");
                        String swellPeriod = swell.getJSONObject("components").getJSONObject("primary").getString("period");
                        String swellDeg = swell.getJSONObject("components").getJSONObject("primary").getString("direction");
                        
                        conditionValues.add(new Condition(date, minBreak, maxBreak, windSpeed, windDeg,
                            windDir, swellHeight, swellPeriod, swellDeg));
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
            Log.e("hackwinds", String.valueOf(conditionValues.size()));
            adapter = new ConditionArrayAdapter(getActivity(), conditionValues);
            setListAdapter(adapter);
        }
 
    }

    // Return a pretty timestamp for headers
    public String formatDate(Long timestamp) {
        Date date = new Date(timestamp*1000);
        DateFormat format = new SimpleDateFormat("EEE HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        String formatted = format.format(date);
        return formatted;
    }
}
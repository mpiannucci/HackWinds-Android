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

    // Create constant variabes for all ofthe URLs and cache settings
    final String streamURL = "http://162.243.101.197:1935/surfcam/live.stream/playlist.m3u8";
    final public String mswURL =
        "http://magicseaweed.com/api/nFSL2f845QOAf1Tuv7Pf5Pd9PXa5sVTS/forecast/?spot_id=1103&fields=localTimestamp,swell.*,wind.*";
    final String[] days = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    final int cacheDuration = 3000;
    final String imgUrl = "http://www.warmwinds.com/wp-content/uploads/surf-cam-stills/image00001.jpg";

    // Initialize the other variables
    ArrayList<Condition> conditionValues;
    ConditionArrayAdapter adapter;
    public VideoView streamView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a new vector of condition objects
        conditionValues = new ArrayList<Condition>();

        // Execute the MagicSeaweed async task
        new BackgroundMSWAsyncTask().execute(6);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.current_fragment, container, false);

        // Set the day header to the current day
        TextView date = (TextView) V.findViewById(R.id.dateHeader);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        date.setText(days[day - 1]);

        // Get the imageview to set as the holder before the user calls
        // to play the videoview
        ImageView img = (ImageView) V.findViewById(R.id.imageOverlay);
        Ion.with(getActivity()).load(imgUrl).intoImageView(img);

        // Scale the image to fit the width of the screen
        img.getLayoutParams().width = ActionBar.LayoutParams.MATCH_PARENT;
        img.setScaleType(ImageView.ScaleType.FIT_XY);
        img.setAdjustViewBounds(true);

        // Set the play button image over the holder camera image
        ImageView pb = (ImageView) V.findViewById(R.id.pbOverlay);

        // Set the onClick callback for the play button to start the VideoView
        pb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Hide the playbutton and the holder image
                v.setVisibility(View.GONE);
                ImageView pic = (ImageView) getActivity().findViewById(R.id.imageOverlay);
                pic.setVisibility(View.GONE);

                // Show the videoview
                streamView = (VideoView) getActivity().findViewById(R.id.currentVideoStreamView);
                streamView.setVisibility(View.VISIBLE);

                // Execute the video loading asynctask
                new BackgroundVideoAsyncTask().execute(streamURL);
            }
        });
        // return the view
        return V;
    }

    public class BackgroundVideoAsyncTask extends AsyncTask<String, Uri, Void> {
        ProgressDialog dialog;

        protected void onPreExecute() {
            // Show a progress dialog so the user knows the videoview is loading
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading Live Stream...");
            dialog.setCancelable(true);
            dialog.show();
        }

        protected void onProgressUpdate(final Uri... uri) {

            try {
                // Set the video url to the stream
                streamView.setVideoURI(uri[0]);
                streamView.requestFocus();
                streamView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    public void onPrepared(MediaPlayer arg0) {
                        // When the stream is ready start the videoview and
                        // hide the progress dialog
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
                // Check the progress of the download
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

            // Get the magicseaweed json response into a string
            String jsonStr = sh.makeServiceCall(mswURL, ServiceHandler.GET);
            if (jsonStr != null) {
                try {
                    // Make a json array from the response string
                    JSONArray jsonArr = new JSONArray(jsonStr);

                    // i is the number of data points parsed
                    int i = 0;

                    // j is the number of total points iterated
                    int j = 0;

                    // Iterate while the number of parsed is less than what the
                    // user asked for
                    while (i < ints[0]) {

                        // Get the current json object
                        JSONObject jsonObj = jsonArr.getJSONObject(j);
                        j++;

                        // Check the date to see if it is valid
                        String date = formatDate(jsonObj.getLong("localTimestamp"));
                        if (checkDate(date) == false) {
                            // Its in a timerange we dont care about so continue
                            continue;
                        }

                        // Get the vlaues from the json object to fill the condition object
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

                        // Add the new condition object to the vector and iterate the number of parsed objects
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

            // Set the condition adapter for the list
            adapter = new ConditionArrayAdapter(getActivity(), conditionValues);
            setListAdapter(adapter);
        }

    }

    // Return a pretty timestamp for headers
    public String formatDate(Long timestamp) {
        // Parse the timestamp and turn it into a stamp that
        // looks like 12:41
        Date date = new Date(timestamp * 1000);
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
        // If its midnight or 3 am we dont care about it. otherwise its fine
        if (((ampmStamp > -1) && (hour00 > -1)) || ((ampmStamp > -1) && (hour03 > -1))) {
            return false;
        }
        return true;
    }
}

package com.nucc.hackwinds;

import com.nucc.hackwinds.R;
import com.nucc.hackwinds.Condition;
import com.nucc.hackwinds.ConditionArrayAdapter;

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


public class currentFragment extends ListFragment {

    String streamURL = "http://162.243.101.197:1935/surfcam/live.stream/playlist.m3u8";
    String mswURL = "http://magicseaweed.com/api/nFSL2f845QOAf1Tuv7Pf5Pd9PXa5sVTS/forecast/?spot_id=1103&fields=swell.*,wind.*";
    String wuURL = "http://api.wunderground.com/api/2e5424aab8c91757/tide/q/RI/Point_Judith.json";

    String[] breakk = {"1","2"};                         // {minheight, maxheight}
    String[] wind = {"15", "90"};                        // {speed, direction}
    String[] swelll = {"2", "8", "5"};                   // {size, period, direction}
    String[] tide = {"6:39", "12:39", "6:39", "12:39", "5:59", "7:05"};  // {Low1, High1, Low2, High2, sunrise, sunset}

    Condition swell;

    public VideoView streamView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<Condition> conditionValues = new ArrayList<Condition>();
        conditionValues.add(new Condition(Condition.ConditionTypes.WAVEHEIGHT, breakk));
        conditionValues.add(new Condition(Condition.ConditionTypes.WIND, wind));
        conditionValues.add(new Condition(Condition.ConditionTypes.SWELL, swelll));

        ConditionArrayAdapter adapter = new ConditionArrayAdapter(getActivity(), conditionValues);
        setListAdapter(adapter);
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
}
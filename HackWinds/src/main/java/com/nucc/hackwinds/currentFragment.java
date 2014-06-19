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


public class currentFragment extends ListFragment {

    String streamURL = "http://162.243.101.197:1935/surfcam/live.stream/playlist.m3u8";
    String mswURL = "http://magicseaweed.com/api/nFSL2f845QOAf1Tuv7Pf5Pd9PXa5sVTS/forecast/?spot_id=1103";
    String wuURL = "http://api.wunderground.com/api/2e5424aab8c91757/tide/q/RI/Point_Judith.json";

    String[] breakk = {"1","2"};                        // {minheight, maxheight}
    String[] wind = {"15", "90"};                       // {speed, direction}
    String[] swelll = {"2", "8", "5"};                   // {size, period, direction}
    String[] tide = {"6:39", "12:39", "6:39", "12:39"}; // {Low1, High1, Low2, High2}
    String[] data = {"5:59", "7:05", "63", "70"};       // {sunrise, sunset, watertemp, airtemp}

    Condition swell;

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

        try {
            // Create the VideoView
            VideoView streamView = (VideoView) V.findViewById(R.id.currentVideoStreamView);

            // Change the MediaController
            //MediaController mediaController = new MediaController(this.getActivity());
            //mediaController.setAnchorView(streamView);

            // Specify the URI of the live stream
            Uri uri = Uri.parse(streamURL);

            // Setting the media controller
            //streamView.setMediaController(mediaController);
            streamView.setVideoURI(uri);
            streamView.requestFocus();

            // Start the video
            streamView.start();
        }
        catch (Exception ex) {
            Log.e(this.toString(), ex.toString());
        }

        return V;
    }

}
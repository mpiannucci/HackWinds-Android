package com.nucc.hackwinds;

import com.nucc.hackwinds.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;
import android.net.Uri;


public class streamFragment extends Fragment {

    String streamURL = "http://162.243.101.197:1935/surfcam/live.stream/playlist.m3u8";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.forecast_fragment, container, false);

        // Create the VideoView
        VideoView streamView = (VideoView)findViewById(R.id.videoStreamView);

        // Change the MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(streamView);

        // Specify the URI of the live stream
        Uri uri = Uri.parse(streamURL);

        // Setting the media controller
        streamView.setMediaController(mediaController);
        streamView.setVideoURI(uri);

        // Start the video
        streamView.start();

        return V;
    }   

}
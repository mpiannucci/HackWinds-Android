package com.nucc.hackwinds;

import com.nucc.hackwinds.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;
import android.net.Uri;
import android.view.LayoutInflater;
import android.util.Log;


public class streamFragment extends Fragment {

    String streamURL = "http://162.243.101.197:1935/surfcam/live.stream/playlist.m3u8";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.stream_fragment, container, false);

        try {
            // Create the VideoView
            VideoView streamView = (VideoView) V.findViewById(R.id.videoStreamView);

            // Change the MediaController
            MediaController mediaController = new MediaController(this.getActivity());
            mediaController.setAnchorView(streamView);

            // Specify the URI of the live stream
            Uri uri = Uri.parse(streamURL);

            // Setting the media controller
            streamView.setMediaController(mediaController);
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
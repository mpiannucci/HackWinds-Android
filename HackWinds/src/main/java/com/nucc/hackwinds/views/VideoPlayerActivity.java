package com.nucc.hackwinds.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;
import android.view.WindowManager;

import com.nucc.hackwinds.R;

public class VideoPlayerActivity extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private VideoView videoView;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the status bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Get the view from the layout
        setContentView(R.layout.activity_video_player);
        videoView = (VideoView) findViewById(R.id.videoView);

        String url = null;
        if (getIntent().getExtras() != null) {
            url = getIntent().getExtras().getString("url");

            if (url != null) {
                videoView.setMediaController(new MediaController(this));
                videoView.setOnCompletionListener(this);
                videoView.setOnErrorListener(this);

                dialog = new ProgressDialog(this);

                new LoadLiveStreamTask().execute(url);
            }
        }

        if (url == null) {
            throw new IllegalArgumentException("Must set url extra paremeter in intent.");
        }
    }

    @Override
    public void onCompletion(MediaPlayer v) {
        finish();
    }

    @Override
    public boolean onError(MediaPlayer v, int err1, int err2) {
        finish();
        return true;
    }

    //Convenience method to show a video
    public static void showRemoteVideo(Context ctx, String url) {
        Intent i = new Intent(ctx, VideoPlayerActivity.class);

        i.putExtra("url", url);
        ctx.startActivity(i);
    }

    public class LoadLiveStreamTask extends AsyncTask<String, Uri, Void> {


        protected void onPreExecute() {
            // Show a progress dialog so the user knows the videoview is loading
            dialog.setMessage("Loading Live Stream...");
            dialog.setCancelable(false);
            dialog.show();
        }

        protected void onProgressUpdate(final Uri... uri) {

            try {
                // Set the video url to the stream
                videoView.setVideoURI(uri[0]);
                videoView.requestFocus();

                // On Prepared Listener
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        // When the stream is ready start the videoview and
                        // hide the progress dialog
                        videoView.start();
                        dialog.dismiss();
                    }
                });

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
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
}
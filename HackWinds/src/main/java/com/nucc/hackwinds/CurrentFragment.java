package com.nucc.hackwinds;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.koushikdutta.ion.Ion;

import java.util.Calendar;


public class CurrentFragment extends ListFragment {
    // Create constant variables for all of the URLs and cache settings
    final static public String STREAM_URL = "http://162.243.101.197:1935/surfcam/live.stream/playlist.m3u8";
    final private String IMG_URL = "http://www.warmwinds.com/wp-content/uploads/surf-cam-stills/image00001.jpg";

    // Initialize the other variables
    private ConditionArrayAdapter mConditionArrayAdapter;
    private ForecastModel mForecastModel;
    public VideoView mStreamView;
    private ForecastChangedListener mForecastChangedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            // Alert the user they need the network and close the app on completion
            new AlertDialog.Builder(getActivity())
                    .setTitle("Network Error")
                    .setMessage("No network detected! Make sure to connect to the internet and reopen the app")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue
                            getActivity().finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        // Get the magicseaweed model instance
        mForecastModel = ForecastModel.getInstance(getActivity());

        // Set the forecast location changed listener
        mForecastChangedListener = new ForecastChangedListener() {
            @Override
            public void forecastLocationChanged() {
                // When the forecast changes reload the condition data
                new FetchConditionDataTask().execute();
            }
        };
        mForecastModel.addForecastChangedListener(mForecastChangedListener);

        new FetchConditionDataTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.current_fragment, container, false);

        // Set the day header to the current day
        TextView date = (TextView) V.findViewById(R.id.dateHeader);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        String dayName = getResources().getStringArray(R.array.daysOfTheWeek)[day-1];
        date.setText(dayName);

        // Get the ImageView to set as the holder before the user calls
        // to play the VideoView
        ImageView img = (ImageView) V.findViewById(R.id.camHolderImage);
        if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
            Ion.with(getActivity()).load(IMG_URL).intoImageView(img);
        }

        // Set the play button image over the holder camera image
        ImageView playButton = (ImageView) V.findViewById(R.id.camPlayButton);

        // Set the onClick callback for the play button to start the VideoView
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Hide the play button and the holder image
                if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
                    v.setVisibility(View.GONE);
                    ImageView pic = (ImageView) getActivity().findViewById(R.id.camHolderImage);
                    pic.setVisibility(View.GONE);

                    // Show the VideoView
                    mStreamView = (VideoView) getActivity().findViewById(R.id.currentVideoStreamView);
                    mStreamView.setVisibility(View.VISIBLE);

                    // Execute the video loading AsyncTask
                    new LoadLiveStreamTask().execute(STREAM_URL);
                }
            }
        });

        // Set long click listener to launch in the full screen intent
        playButton.setLongClickable(true);
        playButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                // Launch the built in video intent instead of the default embedded video player
                if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(STREAM_URL), "video/*");
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }

            }
        });

        // return the view
        return V;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Clean up the video playing
        finishedWithVideo();
    }

    private void finishedWithVideo() {
        // Make sure the videoview stops
        if (mStreamView != null) {
            if (mStreamView.isPlaying()) {
                mStreamView.stopPlayback();

                // And make sure to hide it again
                mStreamView.setVisibility(View.GONE);
            }

            // Show the play button again
            ImageView playButton = (ImageView) getActivity().findViewById(R.id.camPlayButton);
            playButton.setVisibility(View.VISIBLE);

            // Show the holder image again
            ImageView holderPic = (ImageView) getActivity().findViewById(R.id.camHolderImage);
            holderPic.setVisibility(View.VISIBLE);
        }
    }

    public class LoadLiveStreamTask extends AsyncTask<String, Uri, Void> {
        ProgressDialog dialog;

        protected void onPreExecute() {
            // Show a progress dialog so the user knows the videoview is loading
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading Live Stream...");
            dialog.setCancelable(false);
            dialog.show();
        }

        protected void onProgressUpdate(final Uri... uri) {

            try {
                // Set the video url to the stream
                mStreamView.setVideoURI(uri[0]);
                mStreamView.requestFocus();

                // On Prepared Listener
                mStreamView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    public void onPrepared(MediaPlayer mp) {
                        // When the stream is ready start the videoview and
                        // hide the progress dialog
                        mStreamView.start();
                        dialog.dismiss();
                    }
                });

                // On Completion Listener
                mStreamView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        // The video is done so show the original interface
                        finishedWithVideo();
                    }
                });

                // On Error Listener
                mStreamView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        // This is expected because the video times out in a way that I can't catch.
                        // Just show the original interface
                        finishedWithVideo();
                        return true;
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

    public class FetchConditionDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Get the conditions from the model
            // We need 6 conditions for this view
            mForecastModel.getConditionsForIndex(0);

            // Return
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Set the condition adapter for the list
            if (mConditionArrayAdapter == null) {
                mConditionArrayAdapter = new ConditionArrayAdapter(getActivity(), mForecastModel.getConditionsForIndex(0));
                setListAdapter(mConditionArrayAdapter);
            } else {
                mConditionArrayAdapter.setConditonData(mForecastModel.getConditionsForIndex(0));
                mConditionArrayAdapter.notifyDataSetChanged();
            }
        }
    }
}

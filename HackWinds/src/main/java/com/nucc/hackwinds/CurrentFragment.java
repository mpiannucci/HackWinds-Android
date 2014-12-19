package com.nucc.hackwinds;

import android.app.ActionBar;
import android.app.ProgressDialog;
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
    final private String STREAM_URL = "http://162.243.101.197:1935/surfcam/live.stream/playlist.m3u8";
    final private String[] DAYS = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    final private String IMG_URL = "http://www.warmwinds.com/wp-content/uploads/surf-cam-stills/image00001.jpg";

    // Initialize the other variables
    private ConditionArrayAdapter mConditionArrayAdapter;
    private ConditionModel mConditionModel;
    private VideoView mStreamView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the magic seaweed model instance
        mConditionModel = ConditionModel.getInstance();

        new BackgroundMSWAsyncTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.current_fragment, container, false);

        // Set the day header to the current day
        TextView date = (TextView) V.findViewById(R.id.dateHeader);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        date.setText(DAYS[day - 1]);

        // Get the imageview to set as the holder before the user calls
        // to play the videoview
        ImageView img = (ImageView) V.findViewById(R.id.imageOverlay);
        Ion.with(getActivity()).load(IMG_URL).intoImageView(img);

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
                mStreamView = (VideoView) getActivity().findViewById(R.id.currentVideoStreamView);
                mStreamView.setVisibility(View.VISIBLE);

                // Execute the video loading asynctask
                new BackgroundVideoAsyncTask().execute(STREAM_URL);
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
                mStreamView.setVideoURI(uri[0]);
                mStreamView.requestFocus();
                mStreamView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    public void onPrepared(MediaPlayer arg0) {
                        // When the stream is ready start the videoview and
                        // hide the progress dialog
                        mStreamView.start();
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

    public class BackgroundMSWAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Get the conditions from the model
            // We need 6 conditions for this view
            mConditionModel.getConditions(6);

            // Return
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Set the condition adapter for the list
            mConditionArrayAdapter = new ConditionArrayAdapter(getActivity(), mConditionModel.conditions);
            setListAdapter(mConditionArrayAdapter);
        }

    }
}

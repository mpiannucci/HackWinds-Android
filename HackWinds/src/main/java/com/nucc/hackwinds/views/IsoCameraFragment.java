package com.nucc.hackwinds.views;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.os.Handler;
import android.widget.TextView;
import android.content.Context;
import android.widget.VideoView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.CameraModel;
import com.nucc.hackwinds.utilities.ReachabilityHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class IsoCameraFragment extends Fragment {
    private final int NORMAL_REFRESH_DURATION = 3000;
    private final int NARRAGANSETT_REFRESH_DURATION = 35000;
    private final String POINT_JUDITH_STATIC_IMAGE = "http://www.asergeev.com/pictures/archives/2004/372/jpeg/20.jpg";

    private ImageView mCameraImage;
    private Context mContext;
    private String mCameraURL;
    private String mVideoURL;
    private String mLocation;
    private String mCamera;
    private boolean mAutoRefresh;
    private int mAutoRefreshDuration;
    private ImageView mPlayButton;
    public VideoView mVideoView;

    Handler mHandler;
    Runnable mRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlternateCameraActivity alternateCameraActivity = (AlternateCameraActivity) getActivity();
        alternateCameraActivity.setToolbarTitle("Camera View");

        // Store the context so we can get resources later on
        mContext = getActivity().getApplicationContext();

        // This is a quick runnable to reload the images
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                loadCameraImage();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.iso_camera_fragment, container, false);

        mCameraImage = (ImageView) V.findViewById(R.id.latest_camera_image);
        mPlayButton = (ImageView) V.findViewById(R.id.iso_video_play_button);
        mVideoView = (VideoView) V.findViewById(R.id.iso_video_view);

        // Set the onClick callback for the play button to start the VideoView
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Hide the play button and the holder image
                if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
                    v.setVisibility(View.GONE);
                    ImageView holderImage = (ImageView) getActivity().findViewById(R.id.latest_camera_image);
                    holderImage.setVisibility(View.GONE);

                    // Show the VideoView
                    mVideoView.setVisibility(View.VISIBLE);

                    // Execute the video loading AsyncTask
                    new LoadVideoStreamTask().execute(mVideoURL);
                }
            }
        });

        // Set long click listener to launch in the full screen intent
        mPlayButton.setLongClickable(true);
        mPlayButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                // Launch the built in video intent instead of the default embedded video player
                if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(mVideoURL), "video/*");
                    startActivity(intent);
                    return true;
                } else {
                    return false;
                }

            }
        });

        final Switch autoRefreshSwitch = (Switch) V.findViewById(R.id.auto_refresh_toggle);
        mAutoRefresh = autoRefreshSwitch.isChecked();
        autoRefreshSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAutoRefresh = ((Switch) view).isChecked();
                if (mAutoRefresh) {
                    // Trigger a camera refresh
                    loadCameraImage();
                }

                // Update the auto refresh label
                updateAutoRefreshDurationLabel();
            }
        });

        return V;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCameraURL != null) {
            // Only trigger an image load if there is a url
            loadCameraImage();
        }

        // Update the auto refresh label
        updateAutoRefreshDurationLabel();
    }

    @Override
    public void onPause() {
        super.onPause();

        // We need to remove all background threads if we leave the fragment
        mHandler.removeCallbacks(mRunnable);
    }

    public void setCamera(String location, String camera) {
        try {
            if (location.equals("Narragansett") && camera.equals("Point Judith")) {
                // Point Judith has a static image and a video stream
                mCameraURL = POINT_JUDITH_STATIC_IMAGE;
                mVideoURL = CameraModel.getCameraLocations().getJSONObject(location)
                        .getJSONObject(camera).getString("file");
            } else {
                mCameraURL = CameraModel.getCameraLocations().getJSONObject(location).getString(camera);
            }
        } catch (JSONException e) {
            return;
        }

        // The town beach camra only updates every half minute or so
        if (location.equals("Narragansett") && camera.equals("Town Beach South")) {
            mAutoRefreshDuration = NARRAGANSETT_REFRESH_DURATION;
        } else {
            mAutoRefreshDuration = NORMAL_REFRESH_DURATION;
        }

        mLocation = location;
        mCamera = camera;
    }

    public void loadCameraImage() {
        if (mCamera.equals("Point Judith")) {
            // If it is the point judith camera, we want to hide the auto refresh info and show
            // the hidden video status info view
            Switch autoRefreshToggle = (Switch)getActivity().findViewById(R.id.auto_refresh_toggle);
            autoRefreshToggle.performClick();
            getActivity().findViewById(R.id.image_refresh_info_view).setVisibility(View.GONE);

            ((TextView) getActivity().findViewById(R.id.video_info_text)).setText(getVideoStatusInfo());
            getActivity().findViewById(R.id.video_info_view).setVisibility(View.VISIBLE);
        }

        if (mContext != null) {
            // If there is a context, then load the next image and create the callback to set it as the current image
            Ion.with(getActivity()).load(mCameraURL).noCache().asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    if (e != null) {
                        // Set the error image on exceptions
                        mCameraImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.photo_loading_error));
                    } else {
                        // Everything is ok, so set the image
                        mCameraImage.setImageBitmap(result);

                        // If its the point judith view, show the play button
                        if (mCamera.equals("Point Judith")) {
                            mPlayButton.setVisibility(View.VISIBLE);
                        }

                        // If enabled, start the countdown to loading the next view
                        if (mAutoRefresh) {
                            mHandler.postDelayed(mRunnable, mAutoRefreshDuration);
                        }
                    }
                }
            });
        } else {
            // If the context isn't ready yet, wait another half second and throw the runnable again.
            mHandler.postDelayed(mRunnable, 500);
        }
    }

    private void updateAutoRefreshDurationLabel() {
        TextView autoRefreshDurationLabel = (TextView) getActivity().findViewById(R.id.auto_refresh_duration);
        if (mAutoRefresh) {
            autoRefreshDurationLabel.setText("Refresh interval is " + String.valueOf(mAutoRefreshDuration / 1000) + " seconds");
            autoRefreshDurationLabel.setVisibility(View.VISIBLE);
        } else {
            autoRefreshDurationLabel.setVisibility(View.GONE);
        }
    }

    private String getVideoStatusInfo() {
        String videoStatus = "";
        if (mCamera.equals("Point Judith")) {
            try {
                JSONObject pjData = CameraModel.getCameraLocations().
                        getJSONObject(mLocation).getJSONObject(mCamera);

                videoStatus += "Camera Status: " + pjData.getString("camStatus") + "\n";
                videoStatus += "Date: " + pjData.getString("reportDate") + "\n";
                videoStatus += "Time: " + pjData.getString("reportTime") + "\n\n";
                videoStatus += "If the video does not play, it may be down. It is a " +
                        "daily upload during the summer and it becomes unavailable each evening.";
            } catch (JSONException e) {
                videoStatus = "Failed to get info for the video stream";
            }
        }
        return videoStatus;
    }

    private void finishedWithVideo() {
        if (mVideoView != null) {
            if (mVideoView.isPlaying()) {
                mVideoView.stopPlayback();

                // And make sure to hide it again
                mVideoView.setVisibility(View.GONE);
            }

            // Show the play button again
            mPlayButton.setVisibility(View.VISIBLE);

            // Show the holder image again
            ImageView holderPic = (ImageView) getActivity().findViewById(R.id.latest_camera_image);
            holderPic.setVisibility(View.VISIBLE);
        }
    }

    public class LoadVideoStreamTask extends AsyncTask<String, Uri, Void> {
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
                mVideoView.setVideoURI(uri[0]);
                mVideoView.requestFocus();

                // On Prepared Listener
                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    public void onPrepared(MediaPlayer mp) {
                        // When the stream is ready start the videoview and
                        // hide the progress dialog
                        mVideoView.start();
                        dialog.dismiss();
                    }
                });

                // On Completion Listener
                mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        // The video is done so show the original interface
                        finishedWithVideo();
                    }
                });

                // On Error Listener
                mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
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

}

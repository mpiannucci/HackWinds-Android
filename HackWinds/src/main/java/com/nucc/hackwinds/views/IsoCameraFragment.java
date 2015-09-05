package com.nucc.hackwinds.views;

import android.app.ProgressDialog;
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
import android.widget.MediaController;
import android.widget.Switch;
import android.os.Handler;
import android.widget.TextView;
import android.content.Context;
import android.widget.VideoView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nucc.hackwinds.R;
import com.nucc.hackwinds.models.CameraModel;
import com.nucc.hackwinds.types.Camera;
import com.nucc.hackwinds.utilities.ReachabilityHelper;

public class IsoCameraFragment extends Fragment {

    private Camera mCamera;
    private String mLocationName;
    private String mCameraName;
    private boolean mAutoRefresh;

    private Context mContext;
    private ImageView mCameraImage;
    private ImageView mPlayButton;
    private VideoView mVideoView;

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
                    new LoadVideoStreamTask().execute(mCamera.videoURL);
                }
            }
        });

        // Set long click listener to launch in the full screen intent
        mPlayButton.setLongClickable(true);
        mPlayButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (ReachabilityHelper.deviceHasInternetAccess(getActivity())) {
                    // Launch the full screen video activity
                    VideoPlayerActivity.showRemoteVideo(getActivity(), mCamera.videoURL);
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

        if (mCamera != null) {
            // Only trigger an image load if there is a url
            AlternateCameraActivity alternateCameraActivity = (AlternateCameraActivity) getActivity();
            alternateCameraActivity.setToolbarTitle(mCameraName);
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
        mLocationName = location;
        mCameraName = camera;

        // Calling this ona  null object reference is fine because the camera model already needs to have
        // a context
        mCamera = CameraModel.getInstance(null).cameraLocations.get(mLocationName).get(mCameraName);
        mAutoRefresh = mCamera.refreshable;
    }

    public void loadCameraImage() {
        if (!mCamera.videoURL.equals("")) {
            // If it is the point judith camera, we want to hide the auto refresh info and show
            // the hidden video status info view
            Switch autoRefreshToggle = (Switch)getActivity().findViewById(R.id.auto_refresh_toggle);
            autoRefreshToggle.performClick();
            getActivity().findViewById(R.id.image_refresh_info_view).setVisibility(View.GONE);

            if (mCamera.info != null) {
                ((TextView) getActivity().findViewById(R.id.video_info_text)).setText(mCamera.info);
                getActivity().findViewById(R.id.video_info_view).setVisibility(View.VISIBLE);
            }
        }

        if (mContext != null) {
            // If there is a context, then load the next image and create the callback to set it as the current image
            Ion.with(mContext).load(mCamera.imageURL).noCache().asBitmap().setCallback(new FutureCallback<Bitmap>() {
                @Override
                public void onCompleted(Exception e, Bitmap result) {
                    if (e != null) {
                        // Set the error image on exceptions
                        mCameraImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.photo_loading_error));
                    } else {
                        // Everything is ok, so set the image
                        mCameraImage.setImageBitmap(result);

                        // If its the point judith view, show the play button
                        if (!mCamera.videoURL.equals("")) {
                            mPlayButton.setVisibility(View.VISIBLE);
                        }

                        // If enabled, start the countdown to loading the next view
                        if (mAutoRefresh) {
                            mHandler.postDelayed(mRunnable, mCamera.refreshInterval * 1000);
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
            autoRefreshDurationLabel.setText("Refresh interval is " + String.valueOf(mCamera.refreshInterval) + " seconds");
            autoRefreshDurationLabel.setVisibility(View.VISIBLE);
        } else {
            autoRefreshDurationLabel.setVisibility(View.GONE);
        }
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

                // Set the media controls
                mVideoView.setMediaController(new MediaController(getActivity()));

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

package com.nucc.hackwinds.tasks;

import android.os.AsyncTask;

import com.appspot.hackwinds.hackwinds.Hackwinds;
import com.appspot.hackwinds.hackwinds.model.ModelCameraMessagesCameraLocationsMessage;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;


public class FetchCamerasTask extends AsyncTask<Boolean, Void, ModelCameraMessagesCameraLocationsMessage> {

    public interface CameraTaskListener {
        public void onFinished(ModelCameraMessagesCameraLocationsMessage cameraLocations);
    }

    private final CameraTaskListener mListener;
    private Hackwinds mCameraService;

    public FetchCamerasTask(CameraTaskListener taskListener) {
        mListener = taskListener;

        // Set up the service
        Hackwinds.Builder serviceBuilder = new Hackwinds.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),null);
        mCameraService = serviceBuilder.build();
    }

    @Override
    protected ModelCameraMessagesCameraLocationsMessage doInBackground(Boolean... premiums) {
        try {
            return mCameraService.camera().cameras(premiums[0]).setKey(Credentials.HACKWINDS_API_KEY).execute();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(ModelCameraMessagesCameraLocationsMessage result) {
        super.onPostExecute(result);

        // In onPostExecute we check if the listener is valid
        if(mListener != null) {

            // And if it is we call the callback function on it.
            mListener.onFinished(result);
        }
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}

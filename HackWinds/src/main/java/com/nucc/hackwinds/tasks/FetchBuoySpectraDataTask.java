package com.nucc.hackwinds.tasks;

import android.os.AsyncTask;

import com.appspot.mpitester_13.station.Station;
import com.appspot.mpitester_13.station.model.ApiApiMessagesDataMessage;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;


public class FetchBuoySpectraDataTask extends AsyncTask <String, Void, ApiApiMessagesDataMessage> {
    public interface BuoySpectraDataTaskListener {
        public void onFinished(ApiApiMessagesDataMessage data);
    }

    private final BuoySpectraDataTaskListener mListener;
    private Station mStationService;

    public FetchBuoySpectraDataTask(BuoySpectraDataTaskListener taskListener) {
        mListener = taskListener;

        // Set up the service
        Station.Builder serviceBuilder = new Station.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),null);
        mStationService = serviceBuilder.build();
    }

    @Override
    protected ApiApiMessagesDataMessage doInBackground(String... strings) {
        try {
            return mStationService.data("ENGLISH", strings[0]).setKey(Credentials.BUOYFINDER_API_KEY).setDataType("SPECTRA").execute();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(ApiApiMessagesDataMessage result) {
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

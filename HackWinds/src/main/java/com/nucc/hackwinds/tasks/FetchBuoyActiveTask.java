package com.nucc.hackwinds.tasks;

import android.os.AsyncTask;

import com.appspot.mpitester_13.station.Station;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;


public class FetchBuoyActiveTask extends AsyncTask<String, Void, Boolean> {

    public interface BuoyActiveTaskListener {
        public void onFinished(Boolean active);
    }

    private final BuoyActiveTaskListener mListener;
    private Station mStationService;

    public FetchBuoyActiveTask(BuoyActiveTaskListener taskListener) {
        mListener = taskListener;

        // Set up the service
        Station.Builder serviceBuilder = new Station.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(),null);
        mStationService = serviceBuilder.build();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        try {
            return mStationService.info(strings[0]).setKey(Credentials.BUOYFINDER_API_KEY).execute().getActive();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
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

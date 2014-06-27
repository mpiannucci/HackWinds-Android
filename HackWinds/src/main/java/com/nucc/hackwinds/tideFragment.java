package com.nucc.hackwinds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.util.Log;
import java.util.ArrayList;
import android.os.AsyncTask;
import java.lang.Integer;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.lang.Long;
import java.util.Calendar;

public class tideFragment extends ListFragment {

    String wuURL = "http://api.wunderground.com/api/2e5424aab8c91757/tide/q/RI/Point_Judith.json";

    ArrayList<Tide> tideValues;
    TideArrayAdapter adapter;
    String dateTideHolder = "";
    String lowTide1Holder = "";
    String lowTide2Holder = "";
    String highTide1Holder = "";
    String highTide2Holder = "";
    String sunriseHolder = "";
    String sunsetHolder = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tideValues = new ArrayList<Tide>();
        new BackgroundWunderAsyncTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.tide_fragment, container, false);
        return V;
    }

    public class BackgroundWunderAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(wuURL, ServiceHandler.GET);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray tideSummary = jsonObj.getJSONArray("tideSummary");
                    for (int k=0; k < tideSummary.length(); k++) {

                        tideValues.add(new Condition(day, lowTide1, lowTide2, highTide1, highTide2,
                                sunrise, sunset));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("hackwinds", "Couldn't get any data from the wunderground url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //Log.e("hackwinds", String.valueOf(tideValues.size()));
            adapter = new ConditionArrayAdapter(getActivity(), tideValues);
            setListAdapter(adapter);
        }

    }

    private Void resetStrings() {

        dateTideHolder = "";
        lowTide1Holder = "";
        lowTide2Holder = "";
        highTide1Holder = "";
        highTide2Holder = "";
        sunriseHolder = "";
        sunsetHolder = "";

        return null;
    }
}

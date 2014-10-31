package com.nucc.hackwinds;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class TideFragment extends ListFragment {

    String wuURL = "http://api.wunderground.com/api/2e5424aab8c91757/tide/q/RI/Point_Judith.json";

    String[] days = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    ArrayList<Tide> tideValues;
    TideArrayAdapter adapter;
    int today;
    int todayMonth;
    int todayWeek;

    String LOW_TIDE_TAG = "Low Tide";
    String HIGH_TIDE_TAG = "High Tide";
    String SUNRISE_TAG = "Sunrise";
    String SUNSET_TAG = "Sunset";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tideValues = new ArrayList<Tide>();
        getDate();
        new BackgroundWunderAsyncTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.tide_fragment, container, false);
        return V;
    }

    private void getDate() {
        // Get the date and set the labels correctly
        Time now = new Time();
        now.setToNow();
        today = now.monthDay;
        todayMonth = now.month;
        todayWeek = now.weekDay;

        // Set the header text to the date
        for (int i = 0; i<5; i++) {
            tideValues.add(new Tide(days[(now.weekDay + i) % days.length]));
        }
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
                    JSONArray tideSummary = jsonObj.getJSONObject("tide").getJSONArray("tideSummary");
                    int daycount = 0;
                    int datacount = 0;

                    for (int k=0; k < tideSummary.length(); k++) {

                        // Get the day and time
                        String type = tideSummary.getJSONObject(k).getJSONObject("data").getString("type");
                        String month = tideSummary.getJSONObject(k).getJSONObject("date").getString("mon");
                        String day = tideSummary.getJSONObject(k).getJSONObject("date").getString("mday");
                        String hour = tideSummary.getJSONObject(k).getJSONObject("date").getString("hour");
                        String min = tideSummary.getJSONObject(k).getJSONObject("date").getString("min");

                        // Check the date
                        if (Integer.parseInt(day) != today) {
                            // Increment the day indices
                            // Check if there was enough data, if not increment the array
                            if (datacount < 5) {
                                if (datacount < 2) {
                                    for (int l = 0; l < 5; l++) {
                                        // move each day up by one
                                        tideValues.get(l).day = days[(todayWeek + l + 1) % days.length];
                                    }
                                }
                            }
                            else {
                                daycount++;
                            }

                            // Check if its a new month
                            if (Integer.parseInt(month) != (todayMonth+1)) {
                                // Check if its a new month
                                today = 1;
                                todayMonth++;
                            }
                            else {
                                today++;
                            }

                            // Check index
                            if (daycount > 4) {
                                break;
                            }

                            // Reset the data count
                            datacount = 0;
                        }

                        // Append the data to the current tide object
                        if ((type.equals(HIGH_TIDE_TAG)) || (type.equals(LOW_TIDE_TAG)) || (type.equals(SUNRISE_TAG)) || (type.equals(SUNSET_TAG))) {
                            Log.e("hackwinds", String.valueOf(type));
                            tideValues.get(daycount).addDataItem(type, hour + ":" +min, datacount);
                            datacount++;
                        }
                        else {
                            // Do nothing cuz these values suck
                        }
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
            if (tideValues.get(tideValues.size()-1).dType[0] == null) {
                tideValues.remove(tideValues.size()-1);
            }
            adapter = new TideArrayAdapter(getActivity(), tideValues);
            setListAdapter(adapter);
        }

    }
}
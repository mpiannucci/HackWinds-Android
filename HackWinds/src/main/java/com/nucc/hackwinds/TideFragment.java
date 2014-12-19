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
    // Set the constants for the data tags and urls
    final private String WUNDER_URL = "http://api.wunderground.com/api/2e5424aab8c91757/tide/q/RI/Point_Judith.json";
    final private String[] DAYS = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    final private String LOW_TIDE_TAG = "Low Tide";
    final private String HIGH_TIDE_TAG = "High Tide";
    final private String SUNRISE_TAG = "Sunrise";
    final private String SUNSET_TAG = "Sunset";

    // Initialize some vars
    private ArrayList<Tide> mTideValues;
    private TideArrayAdapter mArrayAdapter;
    private int today;
    private int todayMonth;
    private int todayWeek;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a new vector of tides
        mTideValues = new ArrayList<Tide>();

        // Get the current data for initialization
        getDate();

        // deploy the wunderground async task
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
        for (int i = 0; i < 5; i++) {
            mTideValues.add(new Tide(DAYS[(now.weekDay + i) % DAYS.length]));
        }
    }

    public class BackgroundWunderAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(WUNDER_URL, ServiceHandler.GET);
            if (jsonStr != null) {
                try {
                    // Get the tide usmmary json object from the current json object
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray tideSummary = jsonObj.getJSONObject("tide").getJSONArray("tideSummary");
                    int daycount = 0;
                    int datacount = 0;

                    for (int k = 0; k < tideSummary.length(); k++) {

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
                                        mTideValues.get(l).day = DAYS[(todayWeek + l + 1) % DAYS.length];
                                    }
                                }
                            } else {
                                // Increment the day count
                                daycount++;
                            }

                            // Check if its a new month
                            if (Integer.parseInt(month) != (todayMonth + 1)) {
                                // Its a new month so reset todays index and increment the month index
                                today = 1;
                                todayMonth++;
                            } else {
                                // Its not so increment the day count
                                today++;
                            }

                            // If there are more than four days, break
                            if (daycount > 4) {
                                break;
                            }

                            // Reset the data count
                            datacount = 0;
                        }

                        // Append the data to the current tide object adn increment the data count
                        if ((type.equals(HIGH_TIDE_TAG)) || (type.equals(LOW_TIDE_TAG)) || (type.equals(SUNRISE_TAG))
                                || (type.equals(SUNSET_TAG))) {
                            mTideValues.get(daycount).addDataItem(type, hour + ":" + min, datacount);
                            datacount++;
                        } else {
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

            // If there arent enough values, remove it from the list
            if (mTideValues.get(mTideValues.size() - 1).dType[0] == null) {
                mTideValues.remove(mTideValues.size() - 1);
            }

            // Set the tide adapter to the list
            mArrayAdapter = new TideArrayAdapter(getActivity(), mTideValues);
            setListAdapter(mArrayAdapter);
        }

    }
}

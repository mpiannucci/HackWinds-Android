package com.nucc.hackwinds;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class ForecastFragment extends ListFragment {
    Fetcher fetch;
    ArrayList<Forecast> forecastValues;
    ForecastArrayAdapter adapter;

    // Create constants for days, urls, etc
    final String[] days = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    final String mswURL =
        "http://magicseaweed.com/api/nFSL2f845QOAf1Tuv7Pf5Pd9PXa5sVTS/forecast/?spot_id=1103&fields=localTimestamp,swell.*,wind.*";
    final String swinURL = "http://www.swellinfo.com/surf-forecast/newport-rhode-island";

    // Initialize dialog update views
    ArrayList<Condition> conditionValues;
    ConditionArrayAdapter conditionAdapter;
    ListView forcDialogList;
    Boolean tomorrow = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a new forecast array
        forecastValues = new ArrayList<Forecast>();

        // Get the date to initialize
        getDate();

        // Create and exectute the new object
        fetch = new Fetcher();
        fetch.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.forecast_fragment, container, false);
        return V;
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        // When the list item is clicked, create a new dialog and fill it with a
        // magic seaweed forecast list
        super.onListItemClick(l, v, pos, id);

        // Create a new dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.forecast_dialog);

        // Get the day of the week and set it as the title
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        dialog.setTitle(days[(day - 1 + pos) % days.length]);

        // Get the listview and create a new list of condition objects
        forcDialogList =  (ListView) dialog.findViewById(R.id.forcDialogList);
        conditionValues = new ArrayList<Condition>();
        // Handle swell info changing its forecast at 6pm to be the next days forecast
        if (tomorrow) {
            if (pos == 4) {
                // Its the last day, so magic seaweed will not match up. Show it as
                // data not yet available
                dialog.setContentView(R.layout.forecast_dialog_fail);
                Button doneButton = (Button) dialog.findViewById(R.id.failConfirm);
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            } else {
                // Deploy the background task asking for the next days magicseaweed data
                new BackgroundMSWAsyncTask().execute(6, pos + 1);
            }
        } else {
            // Deploy the background task asking for the days magicseaweed data
            new BackgroundMSWAsyncTask().execute(6, pos);
        }
        // Set the dialog to hide on click, and show it to the user
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void getDate() {
        // Get the date and set the labels correctly
        Time now = new Time();
        now.setToNow();

        // Set the header text to the date.
        // Handle if it is after 6 oclock when swell info changes the current day.
        if (now.hour < 18) {
            // Here the day is the same so start the index at zero
            for (int i = 0; i < 5; i++) {
                forecastValues.add(new Forecast(days[(now.weekDay + i) % days.length], "", ""));
            }
            tomorrow = false;
        } else {
            // Here the day is actually tomorrow, so start the index at one
            for (int i = 1; i < 6; i++) {
                forecastValues.add(new Forecast(days[(now.weekDay + i) % days.length], "", ""));
            }
            tomorrow = true;
        }
    }

    private void getForecast() {
        // Exectute the background task to get the swellinfo data
        fetch = new Fetcher();
        fetch.execute();
    }

    class Fetcher extends AsyncTask<Void, Void, Void> {
        Document doc;

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                // Get the http response from swellinfo
                doc = Jsoup.connect(swinURL).get();

                // Find all the paragraph elements
                Elements elements = doc.select("p");
                int count = 0;

                // loop and get the overview and detail data
                for (int i = 0; i < 10; i++) {
                    if ((i % 2) == 0) {
                        // Its an overview data object
                        forecastValues.get(count).overview = elements.get(i + 1).text();
                    } else {
                        // Its a detail data object
                        forecastValues.get(count).detail = elements.get(i + 1).text();

                        // Only increment after the detail is done
                        count++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Set the forecast adapter to the listview
            adapter = new ForecastArrayAdapter(getActivity(), forecastValues);
            setListAdapter(adapter);
        }
    }

    public class BackgroundMSWAsyncTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... ints) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Get the magicseaweed json response into a string
            String jsonStr = sh.makeServiceCall(mswURL, ServiceHandler.GET);
            if (jsonStr != null) {
                try {
                    // Make a json array from the response string
                    JSONArray jsonArr = new JSONArray(jsonStr);

                    // i is the number of data points parsed
                    int i = 0;

                    // j is the number of total points iterated
                    int j = 0;

                    // Start at the correct day in the msw data
                    j += 8 * (ints[1]);

                    // Iterate while the number of parsed is less than what the
                    // user asked for
                    while (i < ints[0]) {

                        // Get the current json object
                        JSONObject jsonObj = jsonArr.getJSONObject(j);
                        j++;

                        // Check the date to see if it is valid
                        String date = formatDate(jsonObj.getLong("localTimestamp"));
                        if (checkDate(date) == false) {
                            // Its in a timerange we dont care about so continue
                            continue;
                        }

                        // Get the vlaues from the json object to fill the condition object
                        JSONObject swell = jsonObj.getJSONObject("swell");
                        JSONObject wind = jsonObj.getJSONObject("wind");
                        String minBreak = swell.getString("minBreakingHeight");
                        String maxBreak = swell.getString("maxBreakingHeight");
                        String windSpeed = wind.getString("speed");
                        String windDeg = wind.getString("direction");
                        String windDir = wind.getString("compassDirection");
                        String swellHeight = swell.getJSONObject("components").getJSONObject("primary").getString("height");
                        String swellPeriod = swell.getJSONObject("components").getJSONObject("primary").getString("period");
                        String swellDir = swell.getJSONObject("components").getJSONObject("primary").getString("compassDirection");

                        // Add the new condition object to the vector and iterate the number of parsed objects
                        conditionValues.add(new Condition(date, minBreak, maxBreak, windSpeed, windDeg,
                                                          windDir, swellHeight, swellPeriod, swellDir));
                        i++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("hackwinds", "Couldn't get any data from the msw url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Set the condition adapter for the list
            conditionAdapter = new ConditionArrayAdapter(getActivity(), conditionValues);
            forcDialogList.setAdapter(conditionAdapter);
        }
    }

    // Return a pretty timestamp for headers
    public String formatDate(Long timestamp) {
        // Parse the timestamp and turn it into a stamp that
        // looks like 12:41
        Date date = new Date(timestamp * 1000);
        DateFormat format = new SimpleDateFormat("K a");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        String formatted = format.format(date);
        if (formatted.indexOf("0") > -1) {
            format = new SimpleDateFormat("HH a");
            format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            formatted = format.format(date);
        }
        return formatted;
    }

    // Check the time. If its irrelevant, skip the JSon Objects
    public boolean checkDate(String dateString) {
        int ampmStamp = dateString.indexOf("AM");
        int hour00 = dateString.indexOf("0");
        int hour03 = dateString.indexOf("3");
        // If its midnight or 3 am we dont care about it. otherwise its fine
        if ((ampmStamp > -1) && ((hour00 > -1)) || (hour03 > -1)) {
            return false;
        }
        return true;
    }
}

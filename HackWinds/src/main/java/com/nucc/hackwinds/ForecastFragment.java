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
    // Create constants for days, urls, etc
    final private String[] DAYS = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    final private String SWELL_INFO_URL = "http://www.swellinfo.com/surf-forecast/newport-rhode-island";

    // Initialize dialog update views
    private Fetcher mFetch;
    private ArrayList<Forecast> mForecastValues;
    private ForecastArrayAdapter mArrayAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a new forecast array
        mForecastValues = new ArrayList<Forecast>();

        // Get the date to initialize
        getDate();

        // Create and exectute the new object
        mFetch = new Fetcher();
        mFetch.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View V = inflater.inflate(R.layout.forecast_fragment, container, false);
        return V;
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
                mForecastValues.add(new Forecast(DAYS[(now.weekDay + i) % DAYS.length], "", ""));
            }
        } else {
            // Here the day is actually tomorrow, so start the index at one
            for (int i = 1; i < 6; i++) {
                mForecastValues.add(new Forecast(DAYS[(now.weekDay + i) % DAYS.length], "", ""));
            }
        }
    }

    private void getForecast() {
        // Exectute the background task to get the swellinfo data
        mFetch = new Fetcher();
        mFetch.execute();
    }

    class Fetcher extends AsyncTask<Void, Void, Void> {
        Document doc;

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                // Get the http response from swellinfo
                doc = Jsoup.connect(SWELL_INFO_URL).get();

                // Find all the paragraph elements
                Elements elements = doc.select("p");
                int count = 0;

                // loop and get the overview and detail data
                for (int i = 0; i < 10; i++) {
                    if ((i % 2) == 0) {
                        // Its an overview data object
                        mForecastValues.get(count).overview = elements.get(i + 1).text();
                    } else {
                        // Its a detail data object
                        mForecastValues.get(count).detail = elements.get(i + 1).text();

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
            mArrayAdapter = new ForecastArrayAdapter(getActivity(), mForecastValues);
            setListAdapter(mArrayAdapter);
        }
    }
}

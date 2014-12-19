package com.nucc.hackwinds;

import android.text.format.Time;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class ForecastModel {
    // Create constants for days, urls, etc
    final private String[] DAYS = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    final private String SWELL_INFO_URL = "http://www.swellinfo.com/surf-forecast/newport-rhode-island";

    // Member variables
    private static ForecastModel ourInstance = new ForecastModel();
    public ArrayList<Forecast> forecasts;

    public static ForecastModel getInstance() {
        return ourInstance;
    }

    private ForecastModel() {
        // Initialize the forecast array
        forecasts = new ArrayList<Forecast>();

        // Get the date objects
        getDate();
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
                forecasts.add(new Forecast(DAYS[(now.weekDay + i) % DAYS.length], "", ""));
            }
        } else {
            // Here the day is actually tomorrow, so start the index at one
            for (int i = 1; i < 6; i++) {
                forecasts.add(new Forecast(DAYS[(now.weekDay + i) % DAYS.length], "", ""));
            }
        }
    }

    public ArrayList<Forecast> getForecasts () {
        if (forecasts.get(0).detail.isEmpty()) {
            parseForecastData();
        }
        return forecasts;
    }

    private boolean parseForecastData () {
        // Get the http response from swellinfo
        try {
            Document doc = Jsoup.connect(SWELL_INFO_URL).get();

            // Find all the paragraph elements
            Elements elements = doc.select("p");
            int count = 0;

            // loop and get the overview and detail data
            for (int i = 0; i < 10; i++) {
                if ((i % 2) == 0) {
                    // Its an overview data object
                    forecasts.get(count).overview = elements.get(i + 1).text();
                } else {
                    // Its a detail data object
                    forecasts.get(count).detail = elements.get(i + 1).text();

                    // Only increment after the detail is done
                    count++;
                }
            }
            return true;
        } catch (IOException e) {
            Log.d("HackWinds", "IOException retrieving forecast data");
            return false;
        }
    }
}

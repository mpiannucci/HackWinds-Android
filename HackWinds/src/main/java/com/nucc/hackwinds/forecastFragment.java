package com.nucc.hackwinds;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class forecastFragment extends ListFragment {
	Fetcher fetch;
    ArrayList<Forecast> forecastValues;
    ForecastArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fill the list adapter with a background task
        forecastValues = new ArrayList<Forecast>();
        getDate();
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
        super.onListItemClick(l, v, pos, id);
        Toast.makeText(getActivity(), "Item " + pos + " was clicked", Toast.LENGTH_SHORT).show();
    }

    private void getDate() {
        // Get the date and set the labels correctly
        Time now = new Time();
        now.setToNow();

        String[] days = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        // Set the header text to the date
        for (int i = 0; i<5; i++) {
            forecastValues.add(new Forecast(days[(now.weekDay + i) % days.length], "",""));
        }
    }

    private void getForecast() {
        // Exectute the background task
        fetch = new Fetcher();
        fetch.execute();
    }

    class Fetcher extends AsyncTask<Void, Void, Void> {
        Document doc;

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                doc = Jsoup.connect("http://www.swellinfo.com/surf-forecast/newport-rhode-island").get();
                Elements elements = doc.select("p");
                int count = 0;
                for (int i=0; i<10; i++) {
                    if ((i % 2)==0) {
                        forecastValues.get(count).overview = elements.get(i+1).text();
                    } else {
                        forecastValues.get(count).detail = elements.get(i+1).text();
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
            adapter = new ForecastArrayAdapter(getActivity(), forecastValues);
            setListAdapter(adapter);
        }
    }
}

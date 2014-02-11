package com.nucc.hackwinds;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.nucc.hackwinds.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class forecastFragment extends Fragment {
	Fetcher fetch;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		View V = inflater.inflate(R.layout.forecast_fragment, container, false);
        getForecast();
		return V;
	}

    private void getDate(View rootView) {
        // Get the date and set the labels correctly
        Time now = new Time();
        now.setToNow();

        // Get the day of the week and the text fields
        int[] headers = new int[] {
                R.id.todayForecastHeader,
                R.id.tomorrowForecastHeader,
                R.id.nextDayForecastHeader,
                R.id.fourthDayForecastHeader,
                R.id.fifthDayForecastHeader };

        String[] days = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        // Set the header text to the date
        for (int i = 0; i<headers.length; i++) {
            TextView dayText = (TextView) rootView.findViewById(headers[i]);
            dayText.setText(days[(now.weekDay + i) % days.length]);
        }
    }

    private void getForecast() {
        // Exectute the background task
        fetch = new Fetcher();
        fetch.execute();
    }

    class Fetcher extends AsyncTask<Void, Void, Void> {
        Document doc;
        String[] forcData = new String[10];

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                doc = Jsoup.connect("http://www.swellinfo.com/surf-forecast/newport-rhode-island").get();
                Elements elements = doc.select("p");
                for (int i=0; i<10; i++) {
                	forcData[i] = elements.get(i+1).text();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            int[] views = {
                    R.id.todayForecast,
                    R.id.todayCond,
                    R.id.tomorrowForecast,
                    R.id.tomorrowCond,
                    R.id.nextDayForecast,
                    R.id.nextDayCond,
                    R.id.fourthDayForecast,
                    R.id.fourthDayCond,
                    R.id.fifthDayForecast,
                    R.id.fifthDayCond
            };
            getDate(getView());
            for (int i=0; i<views.length; i++) {
                TextView forcSet = (TextView) getView().findViewById(views[i]);
                forcSet.setText(forcData[i]);
            }
        }
    }
}

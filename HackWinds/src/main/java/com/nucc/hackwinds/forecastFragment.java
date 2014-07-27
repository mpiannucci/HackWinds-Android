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

public class forecastFragment extends ListFragment {
	Fetcher fetch;
    ArrayList<Forecast> forecastValues;
    ForecastArrayAdapter adapter;
    String[] days = new String[] {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    String mswURL = "http://magicseaweed.com/api/nFSL2f845QOAf1Tuv7Pf5Pd9PXa5sVTS/forecast/?spot_id=1103&fields=localTimestamp,swell.*,wind.*";
    String swinURL = "http://www.swellinfo.com/surf-forecast/newport-rhode-island";
    ArrayList<Condition> conditionValues;
    ConditionArrayAdapter conditionAdapter;
    ListView forcDialogList;
    Boolean tomorrow = false;

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
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.forecast_dialog);
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        dialog.setTitle(days[(day-1+pos)%days.length]);
        forcDialogList =  (ListView) dialog.findViewById(R.id.forcDialogList);
        conditionValues = new ArrayList<Condition>();
        if (tomorrow) {
            if (pos == 4) {
                dialog.setContentView(R.layout.forecast_dialog_fail);
                Button doneButton = (Button) dialog.findViewById(R.id.failConfirm);
                doneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
            else {
                new BackgroundMSWAsyncTask().execute(6, pos + 1);
            }
        }
        else {
            new BackgroundMSWAsyncTask().execute(6, pos);
        }
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void getDate() {
        // Get the date and set the labels correctly
        Time now = new Time();
        now.setToNow();

        // Set the header text to the date
        if (now.hour < 18) {
            for (int i = 0; i < 5; i++) {
                forecastValues.add(new Forecast(days[(now.weekDay + i) % days.length], "", ""));
            }
            tomorrow = false;
        }
        else {
            for (int i = 1; i < 6; i++) {
                forecastValues.add(new Forecast(days[(now.weekDay + i) % days.length], "", ""));
            }
            tomorrow = true;
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
                doc = Jsoup.connect(swinURL).get();
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

    public class BackgroundMSWAsyncTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... ints) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(mswURL, ServiceHandler.GET);
            if (jsonStr != null) {
                try {
                    JSONArray jsonArr = new JSONArray(jsonStr);
                    int i = 0;
                    int j = 0;
                    j += 8*(ints[1]);
                    while (i < ints[0]) {

                        JSONObject jsonObj = jsonArr.getJSONObject(j);
                        j++;

                        // Fill a new Condition object and append it
                        String date = formatDate(jsonObj.getLong("localTimestamp"));
                        if (checkDate(date) == false) {
                            continue;
                        }

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
            conditionAdapter = new ConditionArrayAdapter(getActivity(), conditionValues);
            forcDialogList.setAdapter(conditionAdapter);
        }
    }

    // Return a pretty timestamp for headers
    public String formatDate(Long timestamp) {
        Date date = new Date(timestamp*1000);
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
        if ((ampmStamp > -1) && ((hour00 > -1)) || (hour03 > -1)) {
            return false;
        }
        return true;
    }
}
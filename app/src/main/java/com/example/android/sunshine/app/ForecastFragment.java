package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.sunshine.app.api.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nerv on 5/2/16.
 */
public class ForecastFragment extends Fragment
{
    public final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public ArrayAdapter<String> mArrayAdapter;

    public ForecastFragment() {
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> forecastEntries = new ArrayList<>();
       /* forecastEntries.add("Today - Sunny - 88/63");
        forecastEntries.add("Tomorrow - Foggy - 70/46");
        forecastEntries.add("Weds - Cloudy - 72/46");
        forecastEntries.add("Thurs - Rainy - 64/36");
        forecastEntries.add("Fri - Foggy - 70/40");
        forecastEntries.add("Sat - Sunny - 76/68");*/

        mArrayAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forcast,
                R.id.list_item_forcast_textview,
                forecastEntries);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mArrayAdapter.getItem(position);
                Context context = getActivity();
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, forecast, duration);
                toast.show();

                Intent details = new Intent(context, DetailActivity.class);
                details.putExtra(Intent.EXTRA_TEXT, forecast);
                context.startActivity(details);
            }
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        updateWeather();
    }
    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.

        inflater.inflate(R.menu.forcastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh)
        {
            updateWeather();
            return true;
        }
        else if (id == R.id.action_location)
        {
            openMap();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void openMap()
    {
        int zipCode = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_location_key),
                        getString(R.string.pref_location_default)));

        String uri = "geo:0,0?q="+zipCode;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

        //verify intent
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().startActivity(intent);
        }

    }

    public void updateWeather()
    {
        FetchWeatherTask task = new FetchWeatherTask();
        task.execute(PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .getString(getString(R.string.pref_location_key),
                                getString(R.string.pref_location_default)),
                "us");
    }
  /*   @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }*/

    class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params)
        {
            if(params.length == 0)
            {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;

            //variables
            String unit = "metric";
            int numDays = 7;
            String mode = "json";

            try {
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String ZIP_PARAM = "zip";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "appid";
                final String MODE_PARAM = "mode";


                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(ZIP_PARAM,params[0]+","+params[1])
                        .appendQueryParameter(UNITS_PARAM, unit)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(MODE_PARAM, mode)
                        .appendQueryParameter(APPID_PARAM, Api.APPID).build();


                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null)
                {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0)
                {
                    return null;
                }

                forecastJsonStr = buffer.toString();
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            }
            finally
            {
                if (urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (final IOException e)
                    {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            String[] toRet = null;

            try
            {
                toRet = getWeatherDataFromJson(forecastJsonStr, numDays);
            }
            catch (Exception e)
            {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return toRet;
        }

        @Override
        protected void onPostExecute(String[] result)
        {
            List<String> strings = new ArrayList<String>();

            for(String str:result)
            {
                strings.add(str);
            }

            mArrayAdapter.clear();
            mArrayAdapter.addAll(strings);
        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
        * so for convenience we're breaking it out into its own method now.
        */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitType = sharedPrefs.getString(
                    getString(R.string.pref_unit_key),
                    getString(R.string.pref_units_label_metric));

            String[] unitValues = getActivity().getResources().getStringArray(R.array.pref_unit_values);

            if(unitType.equals(getString(R.string.pref_units_label_imperial)))
            {
                high = (high * 1.8) + 32;
                low = (low * 1.8) + 32;
            }
            else if(!unitType.equals(getString(R.string.pref_units_label_metric)))
            {
                Log.d(LOG_TAG, "Unit type not found: "+unitType);
            }


            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;
        }
    }
}

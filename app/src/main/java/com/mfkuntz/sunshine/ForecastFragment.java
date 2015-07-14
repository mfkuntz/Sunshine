package com.mfkuntz.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import com.mfkuntz.sunshine.data.WeatherContract;

/**
 * Created by matthew.f.k on 8/23/2014.
 */

public  class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    ForecastAdapter forecastAdapter;
    private final int LOADER_ID = 456;

    public ForecastFragment() {
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){

        getLoaderManager().initLoader(LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.action_refresh){

            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        forecastAdapter = new ForecastAdapter(
                getActivity(),
                null,
                0);

        ListView list = (ListView) rootView.findViewById(R.id.listView_forecast);
        list.setAdapter(forecastAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) parent.getItemAtPosition(position);

                if (c == null)
                    return;

                String locationSetting = Utility.getPreferredLocation(getActivity());


                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .setData(
                                WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting,
                                    c.getLong(Utility.COL_WEATHER_DATE)));

                startActivity(detailIntent);

            }
        });
        return rootView;
    }

    private void updateWeather(){
        FetchWeatherTask task = new FetchWeatherTask(getActivity());

        String location = Utility.getPreferredLocation(getActivity());

        task.execute(location);
    }

    void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String location = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE  +" ASC";

        Uri weatherUriForLocation = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(location,
                System.currentTimeMillis());

        return new CursorLoader(getActivity(),
            weatherUriForLocation, //URI
            Utility.FORECAST_COLUMNS, //projection
            null,null,sortOrder);


    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        forecastAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }
}
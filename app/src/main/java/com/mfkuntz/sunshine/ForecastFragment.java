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
import com.mfkuntz.sunshine.service.WeatherSyncService;
import com.mfkuntz.sunshine.tools.ICallback;

/**
 * Created by matthew.f.k on 8/23/2014.
 */

public  class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    private final int LOADER_ID = 456;
    private final String POSITION_TAG = "ListPosition";

    ForecastAdapter forecastAdapter;
    ListView forecastListView;

    private int mPosition = ListView.INVALID_POSITION;


    private boolean mTwoPane = false;
    public void setTwoPane(boolean twoPane) {
        mTwoPane = twoPane;
        if (forecastAdapter != null){
            forecastAdapter.setTwoPane(mTwoPane);
        }
    }

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
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        if (mPosition != ListView.INVALID_POSITION){
            outState.putInt(POSITION_TAG, mPosition);
        }

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
                             final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_TAG)){
            mPosition = savedInstanceState.getInt(POSITION_TAG);
        }


        forecastAdapter = new ForecastAdapter(
                getActivity(),
                null,
                0);

        forecastAdapter.setTwoPane(mTwoPane);

        forecastListView = (ListView) rootView.findViewById(R.id.listView_forecast);
        forecastListView.setAdapter(forecastAdapter);

        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) parent.getItemAtPosition(position);

                if (c == null)
                    return;

                String locationSetting = Utility.getPreferredLocation(getActivity());

                ICallback main = ((ICallback)getActivity());

                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        locationSetting,
                        c.getLong(Utility.COL_WEATHER_DATE));


                main.onItemSelected(weatherUri);

                mPosition = position;

            }
        });
        return rootView;
    }

    private void updateWeather(){

        String location = Utility.getPreferredLocation(getActivity());

        Intent serviceIntent = new Intent(getActivity(), WeatherSyncService.class);
        serviceIntent.putExtra(WeatherSyncService.ZIP_KEY ,location);

        getActivity().startService(serviceIntent);


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
        if (mPosition != ListView.INVALID_POSITION) {
            forecastListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }
}
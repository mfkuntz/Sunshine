package com.mfkuntz.sunshine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.Toast;

import com.mfkuntz.sunshine.data.WeatherContract;
import com.mfkuntz.sunshine.sync.SunshineSyncAdapter;
import com.mfkuntz.sunshine.tools.ICallback;
import com.mfkuntz.sunshine.tools.Notifications;

/**
 * Created by matthew.f.k on 8/23/2014.
 */

public  class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{


    private final int LOADER_ID = 456;
    private final String POSITION_TAG = "ListPosition";

    ForecastAdapter forecastAdapter;
    ListView forecastListView;
    SwipeRefreshLayout swipeRefresh;

    public static final String SYNC_FINISHED = "com.mfkuntz.sunshine.SYNC_FINISHED";
    private static IntentFilter syncIntentFilter = new IntentFilter(SYNC_FINISHED);

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
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(syncBroadcastReceiver, syncIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(syncBroadcastReceiver);
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

        if (id == R.id.action_map){
            openPreferredLocationMap();
            return true;
        }

        if (id == R.id.action_notify){
            Notifications.NotifyWeather(getActivity(), true);
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

                ICallback main = ((ICallback) getActivity());

                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        locationSetting,
                        c.getLong(Utility.COL_WEATHER_DATE));


                main.onItemSelected(weatherUri);

                mPosition = position;

            }
        });

        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.SwipeRefresh);

        (swipeRefresh).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                SunshineSyncAdapter.syncImmediately(getActivity());
            }
        });

        return rootView;
    }

    private BroadcastReceiver syncBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            swipeRefresh.setRefreshing(false);
        }
    };

    private void updateWeather(){
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void openPreferredLocationMap(){
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);

        Cursor c = forecastAdapter.getCursor();
        if (c == null) return;

        if (!c.moveToFirst()) return;

        String lat = c.getString(Utility.COL_COORD_LAT);
        String longitude = c.getString(Utility.COL_COORD_LONG);

        Uri uri = Uri.parse("geo:" + lat + "," + longitude);

        mapIntent.setData(uri);

        if (mapIntent.resolveActivity(getActivity().getPackageManager()) == null){
            Toast.makeText(getActivity(), "No Map Provider Installed", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(mapIntent);
        return;
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
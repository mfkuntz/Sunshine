package com.mfkuntz.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ShareActionProvider;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.mfkuntz.sunshine.data.WeatherContract;

import org.w3c.dom.Text;


public class DetailActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

        private ShareActionProvider shareActionProvider;
        private static final String FORECAST_SHARE_HASHTAG = "#sunshine";

        private final int LOADER_ID = 457;

        private String forecastString;

        public PlaceholderFragment() {
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState){

            getLoaderManager().initLoader(LOADER_ID, null, this);

            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public void onCreate(Bundle state){
            super.onCreate(state);

            setHasOptionsMenu(true);



        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){

            //locate menu item
            MenuItem item = menu.findItem(R.id.menu_item_share);
            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
            setShareIntent(shareActionProvider);

            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            return rootView;
        }

        private void setShareIntent(ShareActionProvider provider){
            Intent intent = new Intent(Intent.ACTION_SEND)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                    .putExtra(Intent.EXTRA_TEXT, forecastString + FORECAST_SHARE_HASHTAG)
                    .setType("text/plain");

            if (provider != null){
                provider.setShareIntent(intent);
            }


        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }

            Uri parsedUri = Uri.parse(intent.getDataString());
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE  +" ASC";

            return new CursorLoader(getActivity(),
                    parsedUri, //URI
                    Utility.FORECAST_COLUMNS, //projection
                    null,null,sortOrder);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            String s = "DEBUG";

            if (cursor == null)
                return;

            if (!cursor.moveToFirst())
                return;

            String dateString = Utility.formatDate(
                    cursor.getLong(Utility.COL_WEATHER_DATE));

            String weatherDescription =
                    cursor.getString(Utility.COL_WEATHER_DESC);

            boolean isMetric = Utility.isMetric(getActivity());

            String high = Utility.formatTemperature(
                    cursor.getDouble(Utility.COL_WEATHER_MAX_TEMP), isMetric);

            String low = Utility.formatTemperature(
                    cursor.getDouble(Utility.COL_WEATHER_MIN_TEMP), isMetric);

            forecastString = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);

            ((TextView) getView().findViewById(R.id.detail_text)).setText(forecastString);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}

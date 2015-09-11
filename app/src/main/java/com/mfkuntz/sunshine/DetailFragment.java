package com.mfkuntz.sunshine;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by matth on 7/18/2015.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private ShareActionProvider shareActionProvider;
    private static final String FORECAST_SHARE_HASHTAG = "#sunshine";

    private final int LOADER_ID = 457;

    private String forecastString;

    public DetailFragment() {
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
        setShareIntent();

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ViewHolder holder = new ViewHolder(rootView);

        rootView.setTag(holder);
        return rootView;
    }

    private void setShareIntent(){

        if (forecastString == null)
            return;

        Intent intent = new Intent(Intent.ACTION_SEND)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                .putExtra(Intent.EXTRA_TEXT, forecastString + " " + FORECAST_SHARE_HASHTAG)
                .setType("text/plain");

        if (shareActionProvider != null){
            shareActionProvider.setShareIntent(intent);
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Intent intent = getActivity().getIntent();
        if (intent == null || intent.getData() == null) {
            return null;
        }

        Uri parsedUri = Uri.parse(intent.getDataString());


        return new CursorLoader(getActivity(),
                parsedUri, //URI
                Utility.DETAIL_COLUMNS, //projection
                null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null)
            return;

        if (!cursor.moveToFirst())
            return;

        ViewHolder viewHolder = (ViewHolder) getView().getTag();

        int iconID = Utility.getArtResourceForWeatherCondition(cursor.getInt(Utility.COL_DETAIL_CONDITION_ID));
        viewHolder.icon.setImageResource(iconID);


        boolean isMetric = Utility.isMetric(getActivity());

        long dateLong = cursor.getLong(Utility.COL_WEATHER_DATE);
        String dateString = Utility.formatDate(dateLong);
        String weatherDescription =
                cursor.getString(Utility.COL_WEATHER_DESC);

        String high = Utility.formatTemperature(
                cursor.getDouble(Utility.COL_WEATHER_MAX_TEMP), isMetric, getActivity());

        String low = Utility.formatTemperature(
                cursor.getDouble(Utility.COL_WEATHER_MIN_TEMP), isMetric, getActivity());

        forecastString = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);



        String dayName = Utility.getDayName(getActivity(), dateLong);
        viewHolder.day.setText(dayName);

        String date = Utility.getFormattedMonthDay(getActivity(), dateLong);
        viewHolder.date.setText(date);

        viewHolder.high.setText(high);
        viewHolder.low.setText(low);

        float humidity = cursor.getFloat(Utility.COL_DETAIL_HUMIDITY);
        viewHolder.pressure.setText(getActivity().getString(R.string.format_humidity, humidity));


        float windSpeed = cursor.getFloat(Utility.COL_DETAIL_WIND_SPEED);
        float windDirection = cursor.getFloat(Utility.COL_DETAIL_WIND_DEGREES);
        viewHolder.wind.setText(Utility.getFormattedWind(getActivity(), windSpeed, windDirection));


        float pressure = cursor.getFloat(Utility.COL_DETAIL_PRESSURE);
        viewHolder.pressure.setText(getActivity().getString(R.string.format_pressure, pressure));


        setShareIntent();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    static class ViewHolder{

        ImageView icon;
        TextView date;
        TextView day;
        TextView description;
        TextView high;
        TextView low;
        TextView humidity;
        TextView wind;
        TextView pressure;

        public ViewHolder(View view){

            icon = (ImageView) view.findViewById(R.id.detail_icon);
            date = (TextView) view.findViewById(R.id.detail_date);
            day = (TextView) view.findViewById(R.id.detail_day);
//            description =
            high = (TextView) view.findViewById(R.id.detail_high);
            low = (TextView) view.findViewById(R.id.detail_low);
            humidity = (TextView) view.findViewById(R.id.detail_humidity);
            wind = (TextView) view.findViewById(R.id.detail_wind);
            pressure = (TextView) view.findViewById(R.id.detail_pressure);
        }

    }
}

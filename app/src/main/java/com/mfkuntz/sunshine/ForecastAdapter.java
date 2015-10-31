package com.mfkuntz.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mfkuntz.sunshine.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {



    public final int VIEW_TYPE_TODAY = 0;
    public final int VIEW_TYPE_FUTURE = 1;

    private boolean mTwoPane = false;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setTwoPane(boolean b){
        mTwoPane = b;
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
//    private String formatHighLows(double high, double low) {
//        boolean isMetric = Utility.isMetric(mContext);
//        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
//        return highLowStr;
//    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
//    private String convertCursorRowToUXFormat(Cursor cursor) {
//
//
//        String highAndLow = formatHighLows(
//                cursor.getDouble(Utility.COL_WEATHER_MAX_TEMP),
//                cursor.getDouble(Utility.COL_WEATHER_MIN_TEMP));
//
//        return Utility.formatDate(cursor.getLong(Utility.COL_WEATHER_DATE)) +
//                " - " + cursor.getString(Utility.COL_WEATHER_DESC) +
//                " - " + highAndLow;
//    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());
        int layoutId;

        if (viewType == VIEW_TYPE_TODAY)
            layoutId = R.layout.list_item_forecast_today;
        else
            layoutId = R.layout.list_item_forecast;


        View view =  LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public int getViewTypeCount(){
        return 2;
    }

    @Override
    public int getItemViewType(int position){
        //just use the normal, non special if we have two panes
        return (position == 0 && !mTwoPane)? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());

        int iconID;
        if (viewType == VIEW_TYPE_TODAY){
            iconID = Utility.getArtResourceForWeatherCondition(cursor.getInt(Utility.COL_WEATHER_CONDITION_ID));
        }

        else{
            iconID = Utility.getIconResourceForWeatherCondition(cursor.getInt(Utility.COL_WEATHER_CONDITION_ID));
        }

        viewHolder.icon.setImageResource(iconID);






        String date = Utility.getFriendlyDayString(context, cursor.getLong(Utility.COL_WEATHER_DATE));
        viewHolder.date.setText(date);


        String description = cursor.getString(Utility.COL_WEATHER_DESC);
        viewHolder.description.setText(description);
        viewHolder.icon.setContentDescription(description);


        boolean isMetric = Utility.isMetric(context);
        double temp = cursor.getDouble(Utility.COL_WEATHER_MAX_TEMP);

        viewHolder.high.setText(Utility.formatTemperature(temp, isMetric, context));


        temp = cursor.getDouble(Utility.COL_WEATHER_MIN_TEMP);

        viewHolder.low.setText(Utility.formatTemperature(temp, isMetric, context));



//        tv.setText(convertCursorRowToUXFormat(cursor));
    }

    public static class ViewHolder{
        public final ImageView icon;
        public final TextView date;
        public final TextView description;
        public final TextView high;
        public final TextView low;

        public ViewHolder(View view){
            icon = (ImageView) view.findViewById(R.id.list_item_icon);
            date = (TextView) view.findViewById(R.id.list_item_date_textview);;
            description = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            high = (TextView) view.findViewById(R.id.list_item_high_textview);
            low = (TextView)view.findViewById(R.id.list_item_low_textview);
        }
    }
}
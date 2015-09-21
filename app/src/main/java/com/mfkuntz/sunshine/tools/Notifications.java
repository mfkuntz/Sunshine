package com.mfkuntz.sunshine.tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import com.mfkuntz.sunshine.MainActivity;
import com.mfkuntz.sunshine.R;
import com.mfkuntz.sunshine.Utility;
import com.mfkuntz.sunshine.data.WeatherContract;

/**
 * Created by mkuntz on 9/21/15.
 */
public class Notifications {

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFICATION_PROJECTION = new String[]{
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    public static void NotifyWeather(Context context, boolean override){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        //check if we should notify
        String prefNotificationKey = context.getString(R.string.pref_notification_key);
        boolean notificationsDisabled = prefs.getBoolean(prefNotificationKey, false);
        if (notificationsDisabled && !override)
            return;



        //check last notification time
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);

        //notify if its the first one today
        if (override || System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS){

            String zipCode = Utility.getPreferredLocation(context);

            Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(zipCode, System.currentTimeMillis());

            Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFICATION_PROJECTION, null, null, null);

            if (cursor.moveToFirst()){

                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                double high = cursor.getDouble(INDEX_MAX_TEMP);
                double low = cursor.getDouble(INDEX_MIN_TEMP);
                String description = cursor.getString(INDEX_SHORT_DESC);

                int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                String title = context.getString(R.string.app_name);

                String content = String.format(context.getString(R.string.format_notification),
                        description,
                        Utility.formatTemperature(high, context),
                        Utility.formatTemperature(low, context)
                );


                //notify
                displayNotification(title, content, iconId, context);


                //set last sync time
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.commit();

            }
        }
    }

    private static void displayNotification(String title, String description, int iconId, Context context){

        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText(description);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(iconId)
                .setContentTitle(title)
                .setContentText(description)
                .setStyle(bigStyle);

        Intent resultIntent = new Intent(context, MainActivity.class);

        //create an artificial back stack;
        //this means hitting the back button actually goes somewhere, the home screen
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(WEATHER_NOTIFICATION_ID, builder.build());

    }
}

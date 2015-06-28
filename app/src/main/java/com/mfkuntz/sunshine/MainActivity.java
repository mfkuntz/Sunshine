package com.mfkuntz.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_map){
            openPreferredLocationMap();
            return true;


        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationMap(){
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);

        String location = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        mapIntent.setData(
                Uri.parse("geo:0,0?").buildUpon()
                        .appendQueryParameter("q", location)
                        .build()
        );

        if (mapIntent.resolveActivity(getPackageManager()) == null){
            Toast.makeText(this, "No Map Provider Installed", Toast.LENGTH_SHORT);
            return;
        }

        startActivity(mapIntent);
        return;
    }
}


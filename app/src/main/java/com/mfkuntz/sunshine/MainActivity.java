package com.mfkuntz.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.mfkuntz.sunshine.sync.SunshineSyncAdapter;
import com.mfkuntz.sunshine.tools.ICallback;


public class MainActivity extends ActionBarActivity implements ICallback{

    String currentLocation;
    final String DETAILFRAGMENT_TAG  = "DFTAG";
    boolean twoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentLocation = Utility.getPreferredLocation(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.weather_detail_container) != null){
            twoPane = true;

            if (savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                    .commit();
            }
        }
        getSupportActionBar().setElevation(0f);
        if (twoPane) {

            ForecastFragment fragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            fragment.setTwoPane(twoPane);
        }

        SunshineSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public void onResume(){
        super.onResume();

        String newLocation = Utility.getPreferredLocation(getApplicationContext());
        if (newLocation != null && !currentLocation.equals(newLocation)){
            ForecastFragment fragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if (fragment != null){
                fragment.onLocationChanged();
            }
            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (detailFragment != null){
                detailFragment.onLocationChanged(newLocation);
            }

            currentLocation = newLocation;
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



        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onItemSelected(Uri dateUri) {

        if (twoPane){
            DetailFragment df = DetailFragment.newInstance(dateUri);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, df, DETAILFRAGMENT_TAG)
                    .commit();

        }else{
            Intent detailIntent = new Intent(this, DetailActivity.class)
                    .setData(dateUri);

            startActivity(detailIntent);
        }


    }
}


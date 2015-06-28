package com.mfkuntz.sunshine;

import android.content.Intent;
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
    public static class PlaceholderFragment extends Fragment {

        private ShareActionProvider shareActionProvider;
        private String forecast;
        private static final String FORECAST_SHARE_HASHTAG = "#sunshine";

        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(Bundle state){
            super.onCreate(state);

            setHasOptionsMenu(true);


            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                forecast = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
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

            setText(rootView, forecast);

            return rootView;
        }

        private void setText(View rootView, String forecast){
            ((TextView) rootView.findViewById(R.id.detail_text))
                    .setText(forecast);
        }

        private void setShareIntent(ShareActionProvider provider){
            Intent intent = new Intent(Intent.ACTION_SEND)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                    .putExtra(Intent.EXTRA_TEXT, forecast + FORECAST_SHARE_HASHTAG)
                    .setType("text/plain");

            if (provider != null){
                provider.setShareIntent(intent);
            }


        }
    }
}

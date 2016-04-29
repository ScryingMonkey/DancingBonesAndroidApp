package net.dancingbones.dancingbonesapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NewsDetailActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container_news, new NewsDetailFragment())
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

        /*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    public static class NewsDetailFragment extends Fragment {

        private static final String LOG_TAG = NewsDetailFragment.class.getSimpleName();

        private static final String FORECAST_SHARE_HASHTAG = " #DB4Life";
        private String mNewsStr;

        public NewsDetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_news_detail, container, false);
            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getActivity().getIntent();
            // detect if intent is present and has data called EXTRA_TEXT attached
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                // Creates a new string from the text attached to the intent
                mNewsStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                // Displays the new string as a TextView referenced by id (detail_text).
                // The TextView (detail_text) is created and described in layout\fragment_news_detail.xml
                ((TextView) rootView.findViewById(R.id.detail_text))
                        .setText(mNewsStr);
            }

            return rootView;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.main, menu);
            // Retrieve the share menu item
            //MenuItem menuItem = menu.findItem(R.id.action_share);
            // Get the provider and hold onto it to the set/change the share intent.
            //ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            // Attach and intent to this ShareActionProvider. You can update this at anytime,
            // like when the user selects a new piece of data they might like to share.
            //if (mShareActionProvider != null) {
            //    mShareActionProvider.setShareIntent(createShareForecastIntent());
            //} else {
            //    Log.d(LOG_TAG, "Share Action Provider is null?");
            //}
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mNewsStr + FORECAST_SHARE_HASHTAG);
            Log.i(LOG_TAG, "Shared :" + mNewsStr + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }
    }
}
package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Custom loader ID.
    private static final int NEWS_LOADER_ID = 1;

    //Context
    private Context currentContext;

    //Our NewsAdapter instance
    private NewsAdapter newsAdapter;

    //This text view is visible when the list is empty
    private TextView emptyStateTextView;

    //News loader
    private final LoaderManager.LoaderCallbacks<List<News>> newsLoader = new LoaderManager.LoaderCallbacks<List<News>>() {
        @Override
        public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            //getString retrieves a string value from the preferences. The second parameter is the default value for this preference
            String searchTerm = sharedPrefs.getString(getString(R.string.settings_search_term_key), getString(R.string.settings_search_term_default_value));
            String orderBy = sharedPrefs.getString(getString(R.string.settings_order_by_key), getString(R.string.settings_order_by_default));

            // parse breaks apart the URI string that's passed into its parameter
            Uri baseUri = Uri.parse(QueryUtils.REQUEST_URL);

            //buildUpon prepares the base URI that we just parsed so we can add query parameters to it
            Uri.Builder uriBuilder = baseUri.buildUpon();

            //Append the query parameters and their values

            uriBuilder.appendQueryParameter("show-tags", "contributor");
            uriBuilder.appendQueryParameter("order-by", orderBy);
            uriBuilder.appendQueryParameter("api-key", "324ed141-8ecd-4f25-be0a-872bd02c6a8a");
            uriBuilder.appendQueryParameter("from-date", "2017-01-01");
            if (searchTerm != "Search for a single term") {
                uriBuilder.appendQueryParameter("q", searchTerm);
            }

            // Create new loader
            return new NewsLoader(currentContext, uriBuilder.toString());
        }

        @Override
        public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
            // Hide loading indicator
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Set the empty state text view to annotate that we have no news to display
            emptyStateTextView.setText(R.string.no_news_available);

            // If there is a valid list of {@link News}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (news != null && !news.isEmpty()) {
                newsAdapter.addAll(news);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<News>> loader) {
            // Clear the contents of the adapter
            newsAdapter.clear();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list);

        //Set context
        currentContext = this;

        // Find the listView reference
        ListView newsListView = findViewById(R.id.list);

        emptyStateTextView = findViewById(R.id.empty_view);
        newsListView.setEmptyView(emptyStateTextView);

        // Create a new adapter that takes an empty list of news as input
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());

        //Set the adapter
        newsListView.setAdapter(newsAdapter);

        // Set an onItemClickListener on the adapter that starts an intent to open
        // a browser in the user's device with the given URL.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the news item that was clicked on
                News currentNews = newsAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri URI = Uri.parse(currentNews.getUrl());
                // Create a new intent
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, URI);
                // "Start" the intent
                startActivity(websiteIntent);
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get info on the state of the connection
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            // Initialize the loaders.
            loaderManager.initLoader(NEWS_LOADER_ID, null, newsLoader);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_settings){
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

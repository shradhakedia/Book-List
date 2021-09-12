package com.example.booklists;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BookActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    private BookAdapter adapter;

    //base url to update according to searches
    public static String BOOK_URL_REQUEST = "";
    private static final String LOG_TAG = BookActivity.class.getSimpleName();

    //to store the previously loaded url with its id
    private static HashMap<String, Integer> url_id = new HashMap<>();
    private static int id = 0;

    private ProgressBar spinner;
    private ListView bookListview;
    private TextView EmptyTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        // Find a reference to the {@link ListView} in the layout
        bookListview = (ListView) findViewById(R.id.list);

        // Find a reference to the {@link TextView} in the layout
        EmptyTextview = findViewById(R.id.empty_textview);
        bookListview.setEmptyView(EmptyTextview);

        // Find a reference to the {@link ProgressBar} in the layout
        spinner = findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);

        // Create a new adapter that takes an empty list of books as input
        adapter = new BookAdapter(this, new ArrayList<Book>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        bookListview.setAdapter(adapter);

        //to send the intent on clicking the list item
        bookListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Book currentBook = adapter.getItem(i);
                Uri bookUri = Uri.parse(currentBook.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, bookUri);
                // Find an activity to hand the intent and start that activity.
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d("ImplicitIntents", "Can't handle this intent!");
                }
            }
        });
    }

    // Overriding the method to implement search view in action bar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        Log.w("myApp", "onCreateOptionsMenu -started-");

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.search_view);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.hint));

        /** calling the setOnQueryListener to listen the query input by the user
         * and creating an anonymous class to take the the input and update the url accordingly
         */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {

                        /** as user hits search button we want our screen to show the spinning bar
                         * to indicate the query is being processed, thus hiding the view of other views except {@link spinner}
                         */
                        EmptyTextview.setVisibility(View.GONE);
                        bookListview.setVisibility(View.GONE);
                        spinner.setVisibility(View.VISIBLE);

                        //Updating the url to base url again for further searching
                        BOOK_URL_REQUEST = "https://www.googleapis.com/books/v1/volumes?maxResults=10&q=";

                        s = s.toLowerCase();
                        String[] queries = s.split(" ");
                        for (int i = 0; i < queries.length; i++) {
                            if (i != queries.length - 1) {
                                BOOK_URL_REQUEST += queries[i] + "+";
                            } else {
                                BOOK_URL_REQUEST += queries[i];
                            }
                        }
                        if (url_id.containsKey(BOOK_URL_REQUEST)) {
                            Integer loaderId = url_id.get(BOOK_URL_REQUEST);
                            if (loaderId != null) {
                                getSupportLoaderManager().initLoader(loaderId, null, BookActivity.this).forceLoad();
                            } else {
                                url_id.put(BOOK_URL_REQUEST, id);
                                getSupportLoaderManager().initLoader(id, null, BookActivity.this).forceLoad();
                                id += 1;
                            }
                        } else {
                            url_id.put(BOOK_URL_REQUEST, id);
                            getSupportLoaderManager().initLoader(id, null, BookActivity.this).forceLoad();
                            id += 1;
                        }
                        /* BookAsyncTask task = new BookAsyncTask();
                         * task.execute(BOOK_URL_REQUEST); */

                        return true;
                    }

                    // we don't want to do anything if someone just enters text without submitting the query
                    // so we return false.
                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
        });
        return true;
    }

    //called when loader is initialised for the first time.
    @NonNull
    @Override
    public Loader<List<Book>> onCreateLoader(int id, @Nullable Bundle args) {

        Log.i(LOG_TAG, "onCreateLoader.() called");
        return new BookLoader(BookActivity.this, BOOK_URL_REQUEST);
    }

    //to set the thing when loading the data has finished.
    @Override
    public void onLoadFinished(@NonNull Loader<List<Book>> loader, List<Book> books) {

        // Spinner should no longer be visible as we get the result now.
        spinner.setVisibility(View.GONE);

        // Clear the adapter of previous book data
        adapter.clear();

        // Set empty state text to display "No Data found."
        EmptyTextview.setText(R.string.NoDataAvailable);

        // check internet connection
        if(books == null || !isConnected(BookActivity.this)) {
            EmptyTextview.setText(R.string.NoInternetConnection);
        }

        if(books != null && !books.isEmpty()) {
            adapter.addAll(books);
        }
    }

    //called after OnLoadFinished function
    public void onLoaderReset(@NonNull Loader<List<Book>> loader) {
        adapter.clear();
    }

    //to check internet connection
    private static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


//    private class BookAsyncTask extends AsyncTask<String, Void, List<Book>> {
//
//        @Override
//        protected List<Book> doInBackground(String... urls) {
//            if(urls == null || urls[0] == null) {
//                return null;
//            }
//
//            List<Book> books = QueryUtils.fetchBookData(urls[0]);
//            return books;
//        }
//
//        @Override
//        protected void onPostExecute(List<Book> books) {
//            adapter.clear();
//            BOOK_URL_REQUEST = "https://www.googleapis.com/books/v1/volumes?maxResults=10&q=";
//
//            if(books != null && !books.isEmpty()) {
//                adapter.addAll(books);
//            }
//
//        }
//    }
}
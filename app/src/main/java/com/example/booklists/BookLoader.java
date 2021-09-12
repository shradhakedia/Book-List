package com.example.booklists;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.util.List;

public class BookLoader extends AsyncTaskLoader<List<Book>>  {

    /** Tag for log messages */
    private static final String LOG_TAG = BookLoader.class.getSimpleName();
    public String url;

    /**
     * Constructs a new {@link Book}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public BookLoader(@NonNull Context context, String url) {
        super(context);
        this.url = url;
    }

//    @Override
//    protected void onStartLoading() {
//        Log.i(LOG_TAG,"TEST: onStartLoading()");
//        forceLoad();
//    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Book> loadInBackground() {
        if (url == null) {
            return null;
        }

        List<Book> result = QueryUtils.fetchBookData(url);
        return result;
    }
}

package com.example.booklists;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Helper methods related to requesting and receiving book data from google books api .
 */
public class QueryUtils {

    /** Tag for the log messages */
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the Google Books dataset and return an {@link List<Book>} object to represent a single book as one item of the list.
     */
    public static List<Book> fetchBookData(String requestUrl) {

        //Create Url Object
        URL url = createUrl(requestUrl);

        //make an http request with that url and receive the JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error making the Http request");
        }

        // Extract relevant fields from the JSON response and create an {@link List<Book>} object
        return extractBooks(jsonResponse);

    }

    private static URL createUrl(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error while creating Url", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if(url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /*milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if(urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else {
                Log.e(LOG_TAG, "Error Response Code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the data from JSON results.", e);
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        if(inputStream != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
        }
        return sb.toString();
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing a JSON response.
     */
    private static List<Book> extractBooks(String jsonResponse) {

        // If the JSON string is empty or null, then return early.
        //Note: we are not using jsonResponse.isEmpty() as it returns true if length is 0 for
        //the empty string but throws a null pointer exception when string is null(empty and null strings are different) but TextUtils returns true
        //even when string is null so prefer using TextUtils to avoid crashing of the app in any situations;
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        List<Book> books = new ArrayList<>();

        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray items = root.getJSONArray("items");

            // For each book in the BookArray, create an {@link Book} object
            for(int i = 0; i <items.length(); i++) {

                // Get a single book at position i within the list of books
                JSONObject book = items.getJSONObject(i);

                // For a given book, extract the JSONObject associated with the
                // key called "volumeInfo", which represents a list of all properties
                // for that book.
                JSONObject bookInfo = book.getJSONObject("volumeInfo");

                // Extract the title for the key called "title"
                String title = bookInfo.has("title")? bookInfo.optString("title", "") : "No Title";

                // Extract the value for the key called "publishedDate"
                String date = bookInfo.has("publishedDate")? bookInfo.optString("publishedDate", "") : "Date unavailable";

                // Extract the value for the key called "averageRating"
                int rating = bookInfo.has("averageRating")? bookInfo.optInt("averageRating", 0) : 0;

                // Extract the array for the key called "authors" and get the author at 0th index
                String author = bookInfo.has("authors")? bookInfo.getJSONArray("authors").getString(0) : "Author Data unavailable";

                // Extract the value for the key called "publisher"
                String publisher = bookInfo.has("publisher")? bookInfo.optString("publisher", "") : "";

                // Extract the value for the key called "description"
                String description = bookInfo.has("description")? bookInfo.optString("description", "") : "For more info, Tap";

                // Extract the value for the key called "canonicalVolumeLink"
                String url = bookInfo.has("canonicalVolumeLink")? bookInfo.optString("canonicalVolumeLink", "") : "";

                // Extract the value for the key called "imageLinks"
                String thumbnail = bookInfo.has("imageLinks")? bookInfo.getJSONObject("imageLinks").optString("thumbnail", "") : "";

                // getting bitmap value for the string url of image
                Bitmap bookCover = downloadImageTask(thumbnail);

                // For a given book, extract the JSONObject associated with the
                // key called "accessInfo", which represents a list of the information of how the book is accessible                // for that book.
                JSONObject pdfInfo = book.has("accessInfo")? book.optJSONObject("accessInfo") : null;
                JSONObject pdf;
                boolean isPdfAvailable = false;
                String TokenLink = "";
                if(pdfInfo != null) {
                    pdf = pdfInfo.has("pdf")? pdfInfo.getJSONObject("pdf") : null;
                    if(pdf != null) {
                        isPdfAvailable = pdf.has("isAvailable") && pdf.getBoolean("isAvailable");
                        TokenLink = pdf.has("acsTokenLink")? pdf.optString("acsTokenLink", "") : "";
                    }
                }

                // Create a new {@link Book} object with the title, author, publisher, date, url, description, rating, isPdfAvailable, TokenLink,
                //  and bookCover from the JSON response and Add the new {@link Book} to the list of earthquakes.
                books.add(new Book(title, author, publisher, date, url, description, rating, isPdfAvailable, TokenLink, bookCover));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem while parsing jsonString", e);
        }

        // Return the list of books
        return books;
    }

    // to generate bitmap object of the image from its string url
    private static Bitmap downloadImageTask(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.i(LOG_TAG, "won: " + myBitmap);
            return myBitmap;
        } catch (IOException e) {
            Log.e(LOG_TAG, "error while making book cover", e);
            e.printStackTrace();
            return null;
        }
    }
}

package com.example.booklists;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BookAdapter extends ArrayAdapter<Book> {
    public BookAdapter(@NonNull Context context, List<Book> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;

        if (listItemView == null) {

            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        // Get the {@link Book} object located at this position in the list
        Book currentBook = getItem(position);

        // Find the TextView in the list_item.xml layout with the ID title
        TextView title = (TextView) listItemView.findViewById(R.id.title);
        // set this text on the title TextView by getting it from {@link Book} object
        title.setText(currentBook.getTitle());

        // Find the TextView in the list_item.xml layout with the ID author
        TextView author = (TextView) listItemView.findViewById(R.id.author);

        //getting the author from the {@link Book} object
        String data = currentBook.getAuthor();
        if(!data.equals("Author Data unavailable")) {
            data = "By " + data;
        }
        author.setText(data);

        // Find the TextView in the list_item.xml layout with the ID publisher
        TextView publisher = (TextView) listItemView.findViewById(R.id.publisher);
        /* getting the publisher from the {@link Book} object */
        String publisherData = currentBook.getPublisher();
        if(!publisherData.equals("")) {
            publisherData += " Company";
            publisher.setVisibility(View.VISIBLE);
            publisher.setText(publisherData);
        }
        else {
            publisher.setVisibility(View.GONE);
        }

        // Find the TextView in the list_item.xml layout with the ID description
        TextView description = (TextView) listItemView.findViewById(R.id.description);
        //set this text on the description textview
        description.setText(currentBook.getDescription());

        // Find the TextView in the list_item.xml layout with the ID date
        TextView date = (TextView) listItemView.findViewById(R.id.date);
        // Get the date from {@link Book} object
        String dateData = currentBook.getDate();

        // If length is greater than 4 means we have got yyyy-mm-dd format so we format it in LLL-dd-yyyy format else it is yyyy format as seen in json format
        // so leave it as it is.
        if(dateData.length() > 4) {
            dateData = formatDate(dateData);
        }

        if(!dateData.equals("")) {
            listItemView.findViewById(R.id.firstPublished).setVisibility(View.VISIBLE);
            date.setText(dateData);
        }
        else {
            listItemView.findViewById(R.id.firstPublished).setVisibility(View.GONE);
        }

        // Find the RatingBar view in the list_item.xml layout with the ID rating_bar
        RatingBar stars = (RatingBar) listItemView.findViewById(R.id.rating_bar);
        // Get the ratings from the current Book object,
        int ratings = currentBook.getRating();

        if(ratings > 0) {
            stars.setVisibility(View.VISIBLE);
            stars.setRating((float) ratings);
        }
        else {
            stars.setRating(0.5f);
            stars.setVisibility(View.VISIBLE);
        }

        // Find the ImageView in the list_item.xml layout with the ID download
        ImageView download = (ImageView) listItemView.findViewById(R.id.download);

        // Find the TextView in the list_item.xml layout with the ID pdf
        TextView pdf = (TextView) listItemView.findViewById(R.id.pdf);
        // Get the boolean isPdfAvailable from the current book object
        boolean isPdfAvailable = currentBook.isPdf();

        // if available set accordingly
        if(isPdfAvailable) {
            pdf.setText(R.string.AvailableInPdf);
            download.setVisibility(View.VISIBLE);
        }
        else {
            pdf.setText(R.string.NotAvailableInPdf);
            download.setVisibility(View.GONE);
        }

        // Find the TextView in the list_item.xml layout with the ID book_cover
        ImageView bookCover = (ImageView) listItemView.findViewById(R.id.book_cover);
        // Get the image from the current Book object
        Bitmap image = currentBook.getBookCover();
        // if null set the default image of cover not available
        if(image == null){
            bookCover.setImageResource(R.drawable.bookcover);
        }
        //else set the image retrieved from json
        else {
            bookCover.setImageBitmap(image);
        }

        return listItemView;
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatDate(String dateData) {
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat outputFormat = new SimpleDateFormat("LLL dd, yyyy");
        Date date = null;
        String newDate = "";
        try {
            date = inputFormat.parse(dateData);
            newDate = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }
}

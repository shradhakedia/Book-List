package com.example.booklists;

import android.graphics.Bitmap;

public class Book {

    private String title;
    private String author;
    private String publisher;
    private String date;
    private String url;
    private String description;
    private String TokenLink;
    private Bitmap bookCover;
    private int rating;
    private boolean pdf;

    public Book(String title, String author, String publisher, String date, String url, String description, int rating, boolean pdf, String TokenLink, Bitmap bookCover) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.date = date;
        this.url = url;
        this.description = description;
        this.rating = rating;
        this.pdf = pdf;
        this.TokenLink = TokenLink;
        this.bookCover = bookCover;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public int getRating() {
        return rating;
    }

    public boolean isPdf() {
        return pdf;
    }

    public String getTitle() {
        return title;
    }

    public String getTokenLink() {
        return TokenLink;
    }

    public Bitmap getBookCover() {
        return bookCover;
    }
}

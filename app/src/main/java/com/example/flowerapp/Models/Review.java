package com.example.flowerapp.Models;

public class Review {
    private int reviewId; // Added reviewId
    private float rating;
    private String comment;
    private String reviewDate;
    private String username;

    public Review(int reviewId, float rating, String comment, String reviewDate, String username) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.username = username;
    }

    public int getReviewId() {
        return reviewId;
    }

    public float getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public String getUsername() {
        return username;
    }
}
package com.example.flowerapp.Models;

public class Order {
    private String title;
    private String status;
    private String date;
    private int imageResId;

    public Order(String title, String status, String date, int imageResId) {
        this.title = title;
        this.status = status;
        this.date = date;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public int getImageResId() { return imageResId; }
}

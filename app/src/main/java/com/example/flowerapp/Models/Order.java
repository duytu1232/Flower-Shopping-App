package com.example.flowerapp.Models;

import java.io.Serializable;

public class Order implements Serializable {
    private int orderId;
    private String title;
    private String status;
    private String date;
    private int imageResId;
    private String imageUrl;

    public Order(int orderId, String title, String status, String date, int imageResId, String imageUrl) {
        this.orderId = orderId;
        this.title = title;
        this.status = status;
        this.date = date;
        this.imageResId = imageResId;
        this.imageUrl = imageUrl;
    }

    public int getOrderId() { return orderId; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public int getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }
}
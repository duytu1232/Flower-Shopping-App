package com.example.flowerapp.Models;

import java.io.Serializable;

public class Order implements Serializable {
    private int id, userId;
    private String orderDate, status, shippingAddress, title;
    private double totalAmount;
    private int imageResId;
    private String imageUrl;

    public Order(int id, int userId, String orderDate, String status, double totalAmount, String shippingAddress,
                 String title, int imageResId, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.title = title;
        this.imageResId = imageResId;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public String getTitle() { return title; }
    public int getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }

    // Setters (nếu cần)
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setTitle(String title) { this.title = title; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Để tương thích với phiên bản cũ, thêm getDate() ánh xạ tới getOrderDate()
    public String getDate() { return orderDate; }
}
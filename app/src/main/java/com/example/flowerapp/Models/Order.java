package com.example.flowerapp.Models;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Order implements Serializable {
    private int id, userId;
    private String orderDate, status, shippingAddress, title, imageUrl;
    private double totalAmount;

    public Order(int id, int userId, String orderDate, String status, double totalAmount,
                 String shippingAddress, String title, String imageUrl) {
        if (totalAmount < 0) {
            throw new IllegalArgumentException("Tổng tiền không được âm");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(orderDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Ngày đặt hàng không hợp lệ (yyyy-MM-dd)");
        }
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.title = title != null ? title : "Unknown Product";
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getOrderDate() { return orderDate; }
    public String getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }
    public void setStatus(String status) { this.status = status; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public void setTitle(String title) { this.title = title; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDate() { return orderDate; }
}
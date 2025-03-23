package com.example.flowerapp.Models;

public class NotificationItem {
    private int orderId; // Thêm orderId để liên kết với đơn hàng
    private String title;
    private String message;
    private String timestamp;

    public NotificationItem(int orderId, String title, String message, String timestamp) {
        this.orderId = orderId;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
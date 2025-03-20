package com.example.flowerapp.Models;

import android.util.Patterns;

public class Product {
    private Object type;
    private int id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String imageUrl;
    private String category;

    public Product(int id, String name, String description, double price, int stock, String imageUrl, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.category = category;
        this.type = type;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public String getCategory() { return category; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public String getType() { return (String) type; }
    public void setType(String type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Giá phải lớn hơn 0");
        }
        this.price = price;
    }
    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }
        this.stock = stock;
    }
    public void setImageUrl(String imageUrl) {
        if (imageUrl != null && !Patterns.WEB_URL.matcher(imageUrl).matches()) {
            throw new IllegalArgumentException("URL hình ảnh không hợp lệ");
        }
        this.imageUrl = imageUrl;
    }
    public void setCategory(String category) { this.category = category; }
}
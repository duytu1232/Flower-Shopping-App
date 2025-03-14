package com.example.flowerapp.Models;

public class CartItem {
    private int cartId;
    private int productId;
    private String name;
    private double price;
    public int quantity; // Thay đổi từ private thành public để có thể chỉnh sửa trực tiếp
    private String imageUrl;

    public CartItem(int cartId, int productId, String name, double price, int quantity, String imageUrl) {
        this.cartId = cartId;
        this.productId = productId;
        this.name = name != null ? name : "Unknown Product";
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public int getCartId() {
        return cartId;
    }

    public int getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
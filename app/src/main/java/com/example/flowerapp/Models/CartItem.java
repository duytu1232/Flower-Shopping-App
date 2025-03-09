package com.example.flowerapp.Models;

public class CartItem {
    private int cartId;
    private int productId;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;

    public CartItem(int cartId, int productId, String name, double price, int quantity, String imageUrl) {
        this.cartId = cartId;
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    // Getters và Setters
    public int getCartId() { return cartId; }
    public int getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
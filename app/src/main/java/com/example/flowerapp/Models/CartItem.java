package com.example.flowerapp.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class CartItem implements Parcelable {
    private int cartId;
    private int productId;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;

    public CartItem(int cartId, int productId, String name, double price, int quantity, String imageUrl) {
        this.cartId = cartId;
        this.productId = productId;
        this.name = name != null ? name : "Unknown Product";
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    protected CartItem(Parcel in) {
        cartId = in.readInt();
        productId = in.readInt();
        name = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
        imageUrl = in.readString();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(cartId);
        dest.writeInt(productId);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeInt(quantity);
        dest.writeString(imageUrl);
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

    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
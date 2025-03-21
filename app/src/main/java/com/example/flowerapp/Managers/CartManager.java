package com.example.flowerapp.Managers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String TAG = "CartManager";
    private DatabaseHelper dbHelper;
    private Context context;

    public CartManager(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
    }

    public List<CartItem> loadCartItems(int userId) {
        List<CartItem> cartList = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();

            Cursor cursor = db.rawQuery(
                    "SELECT c.cart_id, c.product_id, c.quantity, p.name, p.price, p.image_url " +
                            "FROM Carts c " +
                            "INNER JOIN Products p ON c.product_id = p.product_id " +
                            "WHERE c.user_id = ?", new String[]{String.valueOf(userId)});

            if (cursor != null) {
                int cartIdIndex = cursor.getColumnIndex("cart_id");
                int productIdIndex = cursor.getColumnIndex("product_id");
                int quantityIndex = cursor.getColumnIndex("quantity");
                int nameIndex = cursor.getColumnIndex("name");
                int priceIndex = cursor.getColumnIndex("price");
                int imageUrlIndex = cursor.getColumnIndex("image_url");

                if (cartIdIndex == -1 || productIdIndex == -1 || quantityIndex == -1 ||
                        nameIndex == -1 || priceIndex == -1 || imageUrlIndex == -1) {
                    Log.e(TAG, "One or more columns do not exist in Carts or Products table!");
                    Toast.makeText(context, "Error: Database columns missing", Toast.LENGTH_SHORT).show();
                } else {
                    while (cursor.moveToNext()) {
                        int cartId = cursor.getInt(cartIdIndex);
                        int productId = cursor.getInt(productIdIndex);
                        int quantity = cursor.getInt(quantityIndex);
                        String name = cursor.getString(nameIndex);
                        double price = cursor.getDouble(priceIndex);
                        String imageUrl = cursor.getString(imageUrlIndex);
                        if (name == null) name = "Unknown Product";
                        cartList.add(new CartItem(cartId, productId, name, price, quantity, imageUrl));
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading cart items: " + e.getMessage());
            Toast.makeText(context, "Error loading cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                dbHelper.closeDatabase(db);
            }
        }
        return cartList;
    }

    public boolean updateCartItemQuantity(CartItem item) {
        return dbHelper.updateCartQuantity(item.getCartId(), item.getQuantity());
    }

    public boolean deleteCartItem(int cartId) {
        return dbHelper.deleteCartItem(cartId);
    }

    public double calculateTotalPrice(List<CartItem> cartList) {
        double totalPrice = 0.0;
        for (CartItem item : cartList) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
        return totalPrice;
    }
}
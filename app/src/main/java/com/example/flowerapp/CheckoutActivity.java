package com.example.flowerapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.CheckoutAdapter;
import com.example.flowerapp.MainActivity;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity";
    private DatabaseHelper dbHelper;
    private RecyclerView checkoutRecyclerView;
    private CheckoutAdapter checkoutAdapter;
    private List<CartItem> cartItems;
    private TextView totalPriceText;
    private Button confirmCheckoutButton;
    private EditText shippingAddressInput;
    private double totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbHelper = new DatabaseHelper(this);
        checkoutRecyclerView = findViewById(R.id.checkout_recycler_view);
        totalPriceText = findViewById(R.id.total_price_text);
        confirmCheckoutButton = findViewById(R.id.confirm_checkout_button);
        shippingAddressInput = findViewById(R.id.shipping_address_input);

        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartItems = new ArrayList<>();
        loadCartItems();

        checkoutAdapter = new CheckoutAdapter(cartItems, this);
        checkoutRecyclerView.setAdapter(checkoutAdapter);

        calculateTotalPrice();
        totalPriceText.setText(String.format("Total: %.2f VND", totalPrice));

        confirmCheckoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartItems.isEmpty()) {
                    Toast.makeText(CheckoutActivity.this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                } else if (!checkStockAvailability()) {
                    Toast.makeText(CheckoutActivity.this, "Cannot proceed due to stock issues", Toast.LENGTH_SHORT).show();
                } else {
                    saveOrder();
                    clearCart();
                    Toast.makeText(CheckoutActivity.this, "Payment successful! Order placed.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckoutActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void loadCartItems() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();

            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

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
                    Toast.makeText(this, "Error: Database columns missing", Toast.LENGTH_SHORT).show();
                } else {
                    while (cursor.moveToNext()) {
                        int cartId = cursor.getInt(cartIdIndex);
                        int productId = cursor.getInt(productIdIndex);
                        int quantity = cursor.getInt(quantityIndex);
                        String name = cursor.getString(nameIndex);
                        double price = cursor.getDouble(priceIndex);
                        String imageUrl = cursor.getString(imageUrlIndex);
                        if (name == null) name = "Unknown Product";
                        cartItems.add(new CartItem(cartId, productId, name, price, quantity, imageUrl));
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading cart items: " + e.getMessage());
            Toast.makeText(this, "Error loading cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private void calculateTotalPrice() {
        totalPrice = 0.0;
        for (CartItem item : cartItems) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
    }

    private boolean checkStockAvailability() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            for (CartItem item : cartItems) {
                Cursor cursor = db.rawQuery("SELECT stock FROM Products WHERE product_id = ?",
                        new String[]{String.valueOf(item.getProductId())});
                if (cursor != null && cursor.moveToFirst()) {
                    int stockIndex = cursor.getColumnIndex("stock");
                    if (stockIndex == -1) {
                        Toast.makeText(this, "Stock column not found in Products table!", Toast.LENGTH_SHORT).show();
                        cursor.close();
                        return false;
                    }
                    int stock = cursor.getInt(stockIndex);
                    if (item.getQuantity() > stock) {
                        Toast.makeText(this, "Product " + item.getName() + " is out of stock!", Toast.LENGTH_SHORT).show();
                        cursor.close();
                        return false;
                    }
                }
                if (cursor != null) cursor.close();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking stock: " + e.getMessage());
            Toast.makeText(this, "Error checking stock: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private void saveOrder() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();

            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String address = shippingAddressInput.getText().toString().trim();
            if (address.isEmpty()) {
                address = "Default Address";
            }

            ContentValues orderValues = new ContentValues();
            orderValues.put("user_id", userId);
            orderValues.put("order_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            orderValues.put("status", "Pending");
            orderValues.put("total_price", totalPrice);
            orderValues.put("address", address);
            orderValues.put("order_number", "Order #" + System.currentTimeMillis());
            orderValues.put("quantity", cartItems.size());
            orderValues.put("note", "");

            long orderId = db.insert("Orders", null, orderValues);
            if (orderId == -1) {
                Toast.makeText(this, "Error creating order", Toast.LENGTH_SHORT).show();
                return;
            }

            for (CartItem item : cartItems) {
                ContentValues orderItemValues = new ContentValues();
                orderItemValues.put("order_id", orderId);
                orderItemValues.put("product_id", item.getProductId());
                orderItemValues.put("quantity", item.getQuantity());
                orderItemValues.put("price", item.getPrice());
                db.insert("Order_Items", null, orderItemValues);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving order: " + e.getMessage());
            Toast.makeText(this, "Error saving order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private void clearCart() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();

            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            db.delete("Carts", "user_id = ?", new String[]{String.valueOf(userId)});
            cartItems.clear();
            checkoutAdapter.notifyDataSetChanged();
            totalPriceText.setText("Total: 0.00 VND");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing cart: " + e.getMessage());
            Toast.makeText(this, "Error clearing cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
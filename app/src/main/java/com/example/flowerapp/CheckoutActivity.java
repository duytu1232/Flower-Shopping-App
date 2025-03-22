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
    private int couponId = -1;
    private String couponCode;
    private double discountValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbHelper = new DatabaseHelper(this);
        checkoutRecyclerView = findViewById(R.id.checkout_recycler_view);
        totalPriceText = findViewById(R.id.total_price_text);
        confirmCheckoutButton = findViewById(R.id.confirm_checkout_button);
        shippingAddressInput = findViewById(R.id.shipping_address_input);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        totalPrice = intent.getDoubleExtra("total_price", 0.0);
        couponId = intent.getIntExtra("coupon_id", -1);
        couponCode = intent.getStringExtra("coupon_code");
        discountValue = intent.getDoubleExtra("discount_value", 0.0);

        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartItems = new ArrayList<>();
        loadCartItems();

        checkoutAdapter = new CheckoutAdapter(cartItems, this);
        checkoutRecyclerView.setAdapter(checkoutAdapter);

        updateTotalPriceText();

        confirmCheckoutButton.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            } else if (!checkStockAvailability()) {
                Toast.makeText(this, "Cannot proceed due to stock issues", Toast.LENGTH_SHORT).show();
            } else {
                saveOrder();
                clearCart();
                Toast.makeText(this, "Payment successful! Order placed.", Toast.LENGTH_SHORT).show();
                Intent mainIntent = new Intent(this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(mainIntent);
                finish();
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
                while (cursor.moveToNext()) {
                    int cartId = cursor.getInt(cursor.getColumnIndexOrThrow("cart_id"));
                    int productId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                    String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));
                    if (name == null) name = "Unknown Product";
                    cartItems.add(new CartItem(cartId, productId, name, price, quantity, imageUrl));
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

    private void updateTotalPriceText() {
        if (couponCode != null && discountValue > 0) {
            double discount = totalPrice * (discountValue / 100.0);
            totalPrice -= discount;
            totalPriceText.setText(String.format("Total: %.2f VND (Applied %s)", totalPrice, couponCode));
        } else {
            totalPriceText.setText(String.format("Total: %.2f VND", totalPrice));
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
                    int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                    if (item.getQuantity() > stock) {
                        Toast.makeText(this, "Product " + item.getName() + " is out of stock!", Toast.LENGTH_SHORT).show();
                        cursor.close();
                        return false;
                    }
                    cursor.close();
                }
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
            orderValues.put("status", "pending"); // Sửa thành "pending" để đồng bộ với schema
            orderValues.put("total_amount", totalPrice); // Sử dụng totalPrice đã giảm giá
            orderValues.put("shipping_address", address);
            if (couponId != -1) {
                orderValues.put("discount_code", couponId); // Lưu discount_id thay vì code
            }

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
                orderItemValues.put("unit_price", item.getPrice()); // Đổi thành unit_price để đồng bộ schema
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
            if (userId == -1) return;
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
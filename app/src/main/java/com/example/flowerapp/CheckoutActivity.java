package com.example.flowerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.CheckoutAdapter;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.Models.User;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity";
    private DatabaseHelper dbHelper;
    private RecyclerView checkoutRecyclerView;
    private CheckoutAdapter checkoutAdapter;
    private List<CartItem> cartItems;
    private TextView totalPriceText;
    private Button continueToPaymentButton;
    private RadioGroup shippingMethodRadioGroup;
    private TextInputEditText firstNameInput, lastNameInput, emailInput, phoneInput, countryInput, cityInput, postalCodeInput, streetInput, addressDetailsInput;
    private double totalPrice;
    private int couponId = -1;
    private String couponCode;
    private double discountValue;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbHelper = new DatabaseHelper(this);
        checkoutRecyclerView = findViewById(R.id.checkout_recycler_view);
        totalPriceText = findViewById(R.id.total_price_text);
        continueToPaymentButton = findViewById(R.id.continue_to_payment_button);
        shippingMethodRadioGroup = findViewById(R.id.shipping_method_radio_group);
        firstNameInput = findViewById(R.id.first_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
        countryInput = findViewById(R.id.country_input);
        cityInput = findViewById(R.id.city_input);
        postalCodeInput = findViewById(R.id.postal_code_input);
        streetInput = findViewById(R.id.street_input);
        addressDetailsInput = findViewById(R.id.address_details_input);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        totalPrice = intent.getDoubleExtra("total_price", 0.0);
        couponId = intent.getIntExtra("coupon_id", -1);
        couponCode = intent.getStringExtra("coupon_code");
        discountValue = intent.getDoubleExtra("discount_value", 0.0);
        cartItems = intent.getParcelableArrayListExtra("cart_items"); // Nhận cartItems từ Intent

        // Kiểm tra cartItems
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "No items in cart", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutAdapter = new CheckoutAdapter(cartItems, this);
        checkoutRecyclerView.setAdapter(checkoutAdapter);

        updateTotalPriceText();

        // Tải thông tin người dùng và điền sẵn vào form
        loadUserInfo();

        continueToPaymentButton.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validateInputs()) {
                return;
            }
            if (!checkStockAvailability()) {
                Toast.makeText(this, "Cannot proceed due to stock issues", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật thông tin người dùng trong database
            updateUserInfo();

            // Lấy phương thức vận chuyển
            String shippingMethod = getSelectedShippingMethod();

            // Tạo địa chỉ giao hàng
            String shippingAddress = formatShippingAddress();

            // Chuyển sang ConfirmationActivity
            Intent confirmationIntent = new Intent(CheckoutActivity.this, ConfirmationActivity.class);
            confirmationIntent.putExtra("total_price", totalPrice);
            confirmationIntent.putExtra("coupon_id", couponId);
            confirmationIntent.putExtra("shipping_address", shippingAddress);
            confirmationIntent.putExtra("shipping_method", shippingMethod);
            confirmationIntent.putParcelableArrayListExtra("cart_items", new ArrayList<>(cartItems));
            startActivity(confirmationIntent);
        });
    }

    private void updateTotalPriceText() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        totalPriceText.setText(String.format("Total: %.2f VND", total));
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.openDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE user_id = ?", new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                currentUser = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("username")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("avatar_uri"))
                );

                firstNameInput.setText(currentUser.getFullName() != null ? currentUser.getFullName().split(" ")[0] : "");
                lastNameInput.setText(currentUser.getFullName() != null && currentUser.getFullName().split(" ").length > 1 ? currentUser.getFullName().split(" ")[1] : "");
                emailInput.setText(currentUser.getEmail());
                phoneInput.setText(currentUser.getPhone());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error loading user info: " + e.getMessage());
        } finally {
            dbHelper.closeDatabase(db);
        }
    }

    private boolean validateInputs() {
        String firstName = firstNameInput.getText() != null ? firstNameInput.getText().toString().trim() : "";
        String lastName = lastNameInput.getText() != null ? lastNameInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";
        String country = countryInput.getText() != null ? countryInput.getText().toString().trim() : "";
        String city = cityInput.getText() != null ? cityInput.getText().toString().trim() : "";
        String postalCode = postalCodeInput.getText() != null ? postalCodeInput.getText().toString().trim() : "";
        String street = streetInput.getText() != null ? streetInput.getText().toString().trim() : "";

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                country.isEmpty() || city.isEmpty() || postalCode.isEmpty() || street.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!phone.matches("^[0-9]{10,11}$")) {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean checkStockAvailability() {
        SQLiteDatabase db = dbHelper.openDatabase();
        try {
            for (CartItem item : cartItems) {
                Cursor cursor = db.rawQuery("SELECT stock FROM Products WHERE product_id = ?",
                        new String[]{String.valueOf(item.getProductId())});
                if (cursor.moveToFirst()) {
                    int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                    if (stock < item.getQuantity()) {
                        cursor.close();
                        return false;
                    }
                }
                cursor.close();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error checking stock: " + e.getMessage());
            return false;
        } finally {
            dbHelper.closeDatabase(db);
        }
    }

    private void updateUserInfo() {
        String firstName = firstNameInput.getText() != null ? firstNameInput.getText().toString().trim() : "";
        String lastName = lastNameInput.getText() != null ? lastNameInput.getText().toString().trim() : "";
        String fullName = firstName + " " + lastName;
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";

        SQLiteDatabase db = dbHelper.openDatabase();
        try {
            db.execSQL(
                    "UPDATE Users SET full_name = ?, email = ?, phone = ? WHERE user_id = ?",
                    new Object[]{fullName, email, phone, currentUser.getUserId()}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error updating user info: " + e.getMessage());
        } finally {
            dbHelper.closeDatabase(db);
        }
    }

    private String getSelectedShippingMethod() {
        int selectedId = shippingMethodRadioGroup.getCheckedRadioButtonId();
        if (selectedId == -1) return "home_delivery"; // Mặc định
        RadioButton selectedRadioButton = findViewById(selectedId);
        String method = selectedRadioButton.getText().toString();
        if (method.equals("Home Delivery")) return "home_delivery";
        if (method.equals("Pickup Point")) return "pickup_point";
        if (method.equals("Pickup in Store")) return "pickup_in_store";
        return "home_delivery";
    }

    private String formatShippingAddress() {
        String country = countryInput.getText() != null ? countryInput.getText().toString().trim() : "";
        String city = cityInput.getText() != null ? cityInput.getText().toString().trim() : "";
        String postalCode = postalCodeInput.getText() != null ? postalCodeInput.getText().toString().trim() : "";
        String street = streetInput.getText() != null ? streetInput.getText().toString().trim() : "";
        String details = addressDetailsInput.getText() != null ? addressDetailsInput.getText().toString().trim() : "";
        return street + ", " + city + ", " + country + " " + postalCode + (details.isEmpty() ? "" : " (" + details + ")");
    }
}
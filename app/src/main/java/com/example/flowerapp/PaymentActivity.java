package com.example.flowerapp;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.example.flowerapp.User.Fragments.XemDonHang;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {
    private static final String TAG = "PaymentActivity";
    private DatabaseHelper dbHelper;
    private RadioGroup paymentMethodRadioGroup;
    private CardView creditCardDetailsCard;
    private TextInputEditText cardNumberInput, cardHolderNameInput, expiryDateInput, yearInput, cvvInput;
    private SwitchCompat saveCardSwitch;
    private Button payNowButton;
    private TextView totalPriceText, couponText;
    private double totalPrice;
    private int couponId;
    private String couponCode;
    private double discountValue;
    private String shippingAddress;
    private String shippingMethod;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        dbHelper = new DatabaseHelper(this);
        paymentMethodRadioGroup = findViewById(R.id.payment_method_radio_group);
        creditCardDetailsCard = findViewById(R.id.credit_card_details_card);
        cardNumberInput = findViewById(R.id.card_number_input);
        cardHolderNameInput = findViewById(R.id.card_holder_name_input);
        expiryDateInput = findViewById(R.id.expiry_date_input);
        yearInput = findViewById(R.id.year_input);
        cvvInput = findViewById(R.id.cvv_input);
        saveCardSwitch = findViewById(R.id.save_card_switch);
        payNowButton = findViewById(R.id.pay_now_button);
        totalPriceText = findViewById(R.id.payment_total_price_text);
        couponText = findViewById(R.id.payment_coupon_text);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        totalPrice = intent.getDoubleExtra("total_price", 0.0);
        couponId = intent.getIntExtra("coupon_id", -1);
        couponCode = intent.getStringExtra("coupon_code");
        discountValue = intent.getDoubleExtra("discount_value", 0.0);
        shippingAddress = intent.getStringExtra("shipping_address");
        shippingMethod = intent.getStringExtra("shipping_method");
        cartItems = intent.getParcelableArrayListExtra("cart_items");

        // Hiển thị tổng giá và thông tin coupon
        totalPriceText.setText(String.format("Total: %.2f VND", totalPrice));
        if (couponId != -1 && couponCode != null) {
            double originalTotal = 0;
            for (CartItem item : cartItems) {
                originalTotal += item.getPrice() * item.getQuantity();
            }
            double discount = originalTotal * (discountValue / 100.0);
            couponText.setText("Coupon: " + couponCode + " (-" + String.format("%.2f", discount) + " VND)");
        } else {
            couponText.setText("Coupon: None");
        }

        // Ẩn/hiện phần nhập thông tin thẻ dựa trên phương thức thanh toán
        paymentMethodRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_credit_card) {
                creditCardDetailsCard.setVisibility(View.VISIBLE);
            } else {
                creditCardDetailsCard.setVisibility(View.GONE);
            }
        });

        payNowButton.setOnClickListener(v -> {
            String paymentMethod = getSelectedPaymentMethod();
            if (paymentMethod.equals("credit_card") && !validateCreditCardInputs()) {
                return;
            }

            long orderId = saveOrder(paymentMethod);
            if (orderId != -1) {
                savePayment(orderId, paymentMethod);
                clearCart();
                Toast.makeText(this, "Payment successful! Order placed.", Toast.LENGTH_SHORT).show();

                // Chuyển về XemDonHang với tab "Shipping"
                Intent xemDonHangIntent = new Intent(this, XemDonHang.class);
                xemDonHangIntent.putExtra("selected_tab", 1); // Tab "Shipping"
                xemDonHangIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(xemDonHangIntent);
                finish();
            }
        });
    }

    private String getSelectedPaymentMethod() {
        int selectedId = paymentMethodRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_credit_card) {
            return "credit_card";
        } else if (selectedId == R.id.radio_momo) {
            return "momo";
        } else if (selectedId == R.id.radio_cod) {
            return "cod";
        }
        return "credit_card"; // Mặc định
    }

    private boolean validateCreditCardInputs() {
        boolean isValid = true;

        if (cardNumberInput.getText().toString().trim().isEmpty()) {
            cardNumberInput.setError("Card number is required");
            isValid = false;
        } else if (cardNumberInput.getText().toString().trim().length() != 16) {
            cardNumberInput.setError("Card number must be 16 digits");
            isValid = false;
        }
        if (cardHolderNameInput.getText().toString().trim().isEmpty()) {
            cardHolderNameInput.setError("Card holder's name is required");
            isValid = false;
        }
        if (expiryDateInput.getText().toString().trim().isEmpty()) {
            expiryDateInput.setError("Expiry date is required");
            isValid = false;
        }
        if (yearInput.getText().toString().trim().isEmpty()) {
            yearInput.setError("Year is required");
            isValid = false;
        }
        if (cvvInput.getText().toString().trim().isEmpty()) {
            cvvInput.setError("CVV is required");
            isValid = false;
        } else if (cvvInput.getText().toString().trim().length() != 3) {
            cvvInput.setError("CVV must be 3 digits");
            isValid = false;
        }

        return isValid;
    }

    private long saveOrder(String paymentMethod) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return -1;
            }

            // Chuẩn hóa shippingMethod
            String standardizedShippingMethod;
            if (shippingMethod.contains("Home delivery")) {
                standardizedShippingMethod = "home_delivery";
            } else if (shippingMethod.contains("Pickup point")) {
                standardizedShippingMethod = "pickup_point";
            } else if (shippingMethod.contains("Pickup in store")) {
                standardizedShippingMethod = "pickup_in_store";
            } else {
                standardizedShippingMethod = "home_delivery"; // Giá trị mặc định nếu không khớp
                Log.w(TAG, "Unknown shipping method: " + shippingMethod + ". Defaulting to home_delivery.");
            }

            ContentValues orderValues = new ContentValues();
            orderValues.put("user_id", userId);
            orderValues.put("order_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            orderValues.put("status", paymentMethod.equals("cod") ? "pending" : "processing");
            orderValues.put("total_amount", totalPrice);
            orderValues.put("shipping_address", shippingAddress);
            orderValues.put("shipping_method", standardizedShippingMethod); // Sử dụng giá trị chuẩn hóa
            if (couponId != -1) {
                orderValues.put("discount_code", couponId);
            }

            long orderId = db.insert("Orders", null, orderValues);
            if (orderId == -1) {
                Toast.makeText(this, "Error creating order", Toast.LENGTH_SHORT).show();
                return -1;
            }

            for (CartItem item : cartItems) {
                ContentValues orderItemValues = new ContentValues();
                orderItemValues.put("order_id", orderId);
                orderItemValues.put("product_id", item.getProductId());
                orderItemValues.put("quantity", item.getQuantity());
                orderItemValues.put("unit_price", item.getPrice());
                db.insert("Order_Items", null, orderItemValues);

                db.execSQL("UPDATE Products SET stock = stock - ? WHERE product_id = ?",
                        new Object[]{item.getQuantity(), item.getProductId()});
            }

            return orderId;
        } catch (Exception e) {
            Log.e(TAG, "Error saving order: " + e.getMessage());
            Toast.makeText(this, "Error saving order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return -1;
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private void savePayment(long orderId, String paymentMethod) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            ContentValues paymentValues = new ContentValues();
            paymentValues.put("order_id", orderId);
            paymentValues.put("payment_method", paymentMethod);
            paymentValues.put("amount", totalPrice); // Sử dụng totalPrice đã giảm giá
            paymentValues.put("payment_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            paymentValues.put("status", "success");

            db.insert("Payments", null, paymentValues);
        } catch (Exception e) {
            Log.e(TAG, "Error saving payment: " + e.getMessage());
            Toast.makeText(this, "Error saving payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
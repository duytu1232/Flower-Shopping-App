package com.example.flowerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.CheckoutAdapter;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CheckoutAdapter adapter;
    private TextView shippingAddressText, shippingMethodText, totalPriceText, couponText;
    private Button confirmPaymentButton;
    private List<CartItem> cartItems;
    private double totalPrice;
    private int couponId;
    private String couponCode;
    private double discountValue;
    private String shippingAddress;
    private String shippingMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        recyclerView = findViewById(R.id.confirmation_recycler_view);
        shippingAddressText = findViewById(R.id.confirmation_shipping_address_text);
        shippingMethodText = findViewById(R.id.confirmation_shipping_method_text);
        totalPriceText = findViewById(R.id.confirmation_total_price_text);
        couponText = findViewById(R.id.confirmation_coupon_text);
        confirmPaymentButton = findViewById(R.id.confirm_payment_button);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        totalPrice = intent.getDoubleExtra("total_price", 0.0);
        couponId = intent.getIntExtra("coupon_id", -1);
        couponCode = intent.getStringExtra("coupon_code");
        discountValue = intent.getDoubleExtra("discount_value", 0.0);
        shippingAddress = intent.getStringExtra("shipping_address");
        shippingMethod = intent.getStringExtra("shipping_method");
        cartItems = intent.getParcelableArrayListExtra("cart_items");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CheckoutAdapter(cartItems, this);
        recyclerView.setAdapter(adapter);

        shippingAddressText.setText("Shipping Address: " + shippingAddress);
        shippingMethodText.setText("Shipping Method: " + shippingMethod);
        totalPriceText.setText(String.format("Total: %.2f VND", totalPrice));

        // Hiển thị thông tin coupon
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

        confirmPaymentButton.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(ConfirmationActivity.this, PaymentActivity.class);
            paymentIntent.putExtra("total_price", totalPrice);
            paymentIntent.putExtra("coupon_id", couponId);
            paymentIntent.putExtra("coupon_code", couponCode);
            paymentIntent.putExtra("discount_value", discountValue);
            paymentIntent.putExtra("shipping_address", shippingAddress);
            paymentIntent.putExtra("shipping_method", shippingMethod);
            paymentIntent.putParcelableArrayListExtra("cart_items", new ArrayList<>(cartItems));
            startActivity(paymentIntent);
        });
    }
}
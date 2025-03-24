package com.example.flowerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.CheckoutAdapter;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationActivity extends AppCompatActivity {
    private RecyclerView confirmationRecyclerView;
    private CheckoutAdapter confirmationAdapter;
    private List<CartItem> cartItems;
    private TextView totalPriceText, shippingAddressText, shippingMethodText;
    private Button confirmPaymentButton;
    private double totalPrice;
    private int couponId;
    private String shippingAddress, shippingMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        confirmationRecyclerView = findViewById(R.id.confirmation_recycler_view);
        totalPriceText = findViewById(R.id.confirmation_total_price_text);
        shippingAddressText = findViewById(R.id.confirmation_shipping_address_text);
        shippingMethodText = findViewById(R.id.confirmation_shipping_method_text);
        confirmPaymentButton = findViewById(R.id.confirm_payment_button);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        totalPrice = intent.getDoubleExtra("total_price", 0.0);
        couponId = intent.getIntExtra("coupon_id", -1);
        shippingAddress = intent.getStringExtra("shipping_address");
        shippingMethod = intent.getStringExtra("shipping_method");
        cartItems = (ArrayList<CartItem>) intent.getSerializableExtra("cart_items");

        // Kiểm tra cartItems
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "No items in cart", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        confirmationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        confirmationAdapter = new CheckoutAdapter(cartItems, this);
        confirmationRecyclerView.setAdapter(confirmationAdapter);

        totalPriceText.setText(String.format("Total: %.2f VND", totalPrice));
        shippingAddressText.setText("Shipping Address: " + (shippingAddress != null ? shippingAddress : "N/A"));
        shippingMethodText.setText("Shipping Method: " + (shippingMethod != null ? shippingMethod : "N/A"));

        confirmPaymentButton.setOnClickListener(v -> {
            // Chuyển sang PaymentActivity
            Intent paymentIntent = new Intent(ConfirmationActivity.this, PaymentActivity.class);
            paymentIntent.putExtra("total_price", totalPrice);
            paymentIntent.putExtra("coupon_id", couponId);
            paymentIntent.putExtra("shipping_address", shippingAddress);
            paymentIntent.putExtra("shipping_method", shippingMethod);
            paymentIntent.putExtra("cart_items", new ArrayList<>(cartItems));
            startActivity(paymentIntent);
        });
    }
}
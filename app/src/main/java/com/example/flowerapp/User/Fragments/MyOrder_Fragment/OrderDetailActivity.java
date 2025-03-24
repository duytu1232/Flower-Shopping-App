package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

public class OrderDetailActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView orderTitle, orderStatus, orderDate, orderTotal, orderAddress, shippingMethod, paymentMethod;
    private ImageView orderImage;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        dbHelper = new DatabaseHelper(this);
        orderTitle = findViewById(R.id.order_title);
        orderStatus = findViewById(R.id.order_status);
        orderDate = findViewById(R.id.order_date);
        orderTotal = findViewById(R.id.order_total);
        orderAddress = findViewById(R.id.order_address);
        shippingMethod = findViewById(R.id.shipping_method); // Thêm TextView mới
        paymentMethod = findViewById(R.id.payment_method);   // Thêm TextView mới
        orderImage = findViewById(R.id.order_image);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        Order order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            orderTitle.setText(order.getTitle());
            orderStatus.setText(order.getStatus());
            orderDate.setText(order.getOrderDate());
            orderTotal.setText(String.format("%.2f VND", order.getTotalAmount()));
            orderAddress.setText(order.getShippingAddress());
            shippingMethod.setText(order.getShippingMethod());
            paymentMethod.setText(order.getPaymentMethod());

            if (order.getImageUrl() != null && !order.getImageUrl().isEmpty()) {
                Glide.with(this)
                        .load(order.getImageUrl())
                        .placeholder(R.drawable.rose)
                        .error(R.drawable.rose)
                        .into(orderImage);
            } else {
                orderImage.setImageResource(R.drawable.rose);
            }
        } else {
            Toast.makeText(this, "Order data not found", Toast.LENGTH_SHORT).show();
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
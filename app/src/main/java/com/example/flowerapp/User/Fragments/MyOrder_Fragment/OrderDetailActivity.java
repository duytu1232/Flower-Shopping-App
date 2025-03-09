package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import android.os.Bundle;
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
    private TextView orderTitle, orderStatus, orderDate, orderTotal, orderAddress;
    private ImageView orderImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        dbHelper = new DatabaseHelper(this);
        orderTitle = findViewById(R.id.orderTitle);
        orderStatus = findViewById(R.id.order_status);
        orderDate = findViewById(R.id.orderDate);
        orderTotal = findViewById(R.id.order_total); // Thêm TextView mới
        orderAddress = findViewById(R.id.order_address); // Thêm TextView mới
        orderImage = findViewById(R.id.orderImage);

        Order order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            orderTitle.setText(order.getTitle());
            orderStatus.setText("Status: " + order.getStatus());
            orderDate.setText("Date: " + order.getOrderDate());
            orderTotal.setText("Total: $" + order.getTotalAmount()); // Hiển thị tổng
            orderAddress.setText("Address: " + order.getShippingAddress()); // Hiển thị địa chỉ
            if (order.getImageUrl() != null && !order.getImageUrl().isEmpty()) {
                Glide.with(this).load(order.getImageUrl()).into(orderImage); // Tải ảnh động
            } else {
                orderImage.setImageResource(order.getImageResId()); // Fallback
            }
        } else {
            Toast.makeText(this, "Order data not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Không cần mở lại để đóng, chỉ cần đóng nếu đã mở
    }
}
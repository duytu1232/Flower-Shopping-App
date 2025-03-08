package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

public class OrderDetailActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView orderTitle, orderStatus, orderDate;
    private ImageView orderImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        dbHelper = new DatabaseHelper(this);
        orderTitle = findViewById(R.id.orderTitle);
        orderStatus = findViewById(R.id.order_status);
        orderDate = findViewById(R.id.orderDate);
        orderImage = findViewById(R.id.orderImage);

        Order order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            orderTitle.setText(order.getTitle());
            orderStatus.setText("Status: " + order.getStatus());
            orderDate.setText("Date: " + order.getDate());
            orderImage.setImageResource(order.getImageResId());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.closeDatabase(dbHelper.openDatabase());
        }
    }
}
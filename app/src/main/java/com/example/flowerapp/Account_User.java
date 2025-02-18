package com.example.flowerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Account_User extends AppCompatActivity {

    Button signOutButton, OrderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_user);

        // Fix lỗi: Sử dụng đúng ID của layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ nút đăng xuất
        signOutButton = findViewById(R.id.sign_out_btn);
        signOutButton.setOnClickListener(v -> {
            // Chuyển sang màn hình đăng nhập
            Intent intent = new Intent(Account_User.this, DangNhap.class);
            startActivity(intent);
            finish(); // Kết thúc Activity hiện tại
        });

        // Ánh xạ nút xem đơn hàng
        OrderButton = findViewById(R.id.order_btn);
        OrderButton.setOnClickListener(v -> {
            // Chuyển sang màn hình xem đơn hàng
            Intent intent = new Intent(Account_User.this, XemDonHang.class);
            startActivity(intent);
            finish(); // Kết thúc Activity hiện tại
        });
        // Ánh xạ các nút trên thanh navigation
        ImageView cartIcon = findViewById(R.id.cart_icon);
        ImageView favoriteIcon = findViewById(R.id.favorite_icon);
        ImageView accountIcon = findViewById(R.id.account_icon);

        // Xử lý sự kiện khi nhấn vào các nút

        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Account_User.this, GioHang.class);
            startActivity(intent);
            finish();
        });

        favoriteIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Account_User.this, Favorite.class);
            startActivity(intent);
            finish();
        });

        accountIcon.setOnClickListener(v -> {
            // Hiện tại ở trang Account_User, không cần chuyển Activity
        });
    }
}

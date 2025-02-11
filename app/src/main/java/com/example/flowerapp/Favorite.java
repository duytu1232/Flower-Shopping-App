package com.example.flowerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Favorite extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorite);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ các nút trên thanh navigation
        ImageView homeIcon = findViewById(R.id.home_icon);
        ImageView cartIcon = findViewById(R.id.cart_icon);
        ImageView accountIcon = findViewById(R.id.account_icon);

        // Xử lý sự kiện khi nhấn vào các nút
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Favorite.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Favorite.this, GioHang.class);
            startActivity(intent);
            finish();
        });


        accountIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Favorite.this, Account_User.class);
            startActivity(intent);
            finish();
        });
    }
}
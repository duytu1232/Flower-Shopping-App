package com.example.flowerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GioiThieu extends AppCompatActivity {

    Button BackButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gioi_thieu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Ánh xạ nút quay về trang người dùng
//        BackButton = findViewById(R.id.Back_btn_account_page);
//        BackButton.setOnClickListener(v -> {
//            // Chuyển sang màn hình đăng nhập
//            Intent intent = new Intent(GioiThieu.this, Account_User.class);
//            startActivity(intent);
//            finish();
//        });

    }
}
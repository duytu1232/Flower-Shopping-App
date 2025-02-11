package com.example.flowerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DangKy extends AppCompatActivity {

    Button Signup_btn;
    CheckBox checkboxTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_ky);

        // Ánh xạ nút đăng ký
        Signup_btn = findViewById(R.id.Sign_Up_btn);
        checkboxTerms = findViewById(R.id.checkbox_terms);

        // Xử lý khi nhấn nút Sign Up
        Signup_btn.setOnClickListener(v -> {
            // Kiểm tra người dùng có đồng ý điều khoản không
            if (!checkboxTerms.isChecked()) {
                Toast.makeText(DangKy.this, "Bạn phải đồng ý với điều khoản trước khi đăng ký!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chuyển sang màn hình chính
            Intent intent = new Intent(DangKy.this, MainActivity.class);
            intent.putExtra("Title", "Home");
            startActivity(intent);
            finish(); // Thoát màn hình đăng ký
        });

        // Fix lỗi: Cập nhật insets UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

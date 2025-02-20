package com.example.flowerapp.Security;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowerapp.MainActivity;
import com.example.flowerapp.R;

public class DangNhap extends AppCompatActivity {

    EditText edit_txt_Username, edit_txt_Password;
    Button Login_btn, Signup_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_nhap);

        // Ánh xạ các thành phần trong layout
        edit_txt_Username = findViewById(R.id.edit_txt_Username);
        edit_txt_Password = findViewById(R.id.edit_txt_Password);
        Login_btn = findViewById(R.id.Login_btn);
        Signup_btn = findViewById(R.id.Sign_Up_btn);

        // Sự kiện nhấn Login
        Login_btn.setOnClickListener(v -> {
            String username = edit_txt_Username.getText().toString().trim();
            String password = edit_txt_Password.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(DangNhap.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(DangNhap.this, MainActivity.class);
                intent.putExtra("Title", "Home");
                startActivity(intent);
                finish(); // Thoát màn hình đăng nhập
            }
        });

        // Sự kiện nhấn Sign Up
        Signup_btn.setOnClickListener(v -> {
            Intent intent = new Intent(DangNhap.this, DangKy.class);
            intent.putExtra("Title", "Home");
            startActivity(intent);
            finish(); // Thoát màn hình đăng nhập
        });

        // Sự kiện nhấn vào dòng "Forgot Password?"
        TextView forgotPasswordText = findViewById(R.id.textView2);
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(DangNhap.this, QuenMatKhau.class);
            startActivity(intent);
        });


        // Cập nhật insets để tránh che mất UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

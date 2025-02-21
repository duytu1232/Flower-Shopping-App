package com.example.flowerapp.Security;

import android.content.Intent;
import android.content.SharedPreferences;
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

    private EditText edit_txt_Username, edit_txt_Password;
    private Button Login_btn, Signup_btn;

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
        Login_btn.setOnClickListener(v -> loginUser());

        // Sự kiện nhấn Sign Up
        Signup_btn.setOnClickListener(v -> {
            Intent intent = new Intent(DangNhap.this, DangKy.class);
            startActivity(intent);
        });

        // Sự kiện nhấn "Forgot Password?"
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

    private void loginUser() {
        String username = edit_txt_Username.getText().toString().trim();
        String password = edit_txt_Password.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(DangNhap.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("loginUsername", null); // Đã sửa từ DangKy
        String savedPassword = sharedPreferences.getString("password", null);

        if (savedUsername == null || savedPassword == null) {
            Toast.makeText(this, "Tài khoản không tồn tại. Vui lòng đăng ký!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.equals(savedUsername) && password.equals(savedPassword)) {
            Toast.makeText(DangNhap.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DangNhap.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(DangNhap.this, "Tên người dùng hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
        }
    }
}
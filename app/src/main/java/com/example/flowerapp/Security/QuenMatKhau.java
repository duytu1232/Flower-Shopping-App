package com.example.flowerapp.Security;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowerapp.R;

public class QuenMatKhau extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordBtn, backToLoginBtn;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quen_mat_khau);

        initViews();
        setupWindowInsets();
        setupListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.email_edit_text);
        resetPasswordBtn = findViewById(R.id.reset_password_btn);
        backToLoginBtn = findViewById(R.id.back_to_login_btn);
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupListeners() {
        resetPasswordBtn.setOnClickListener(v -> resetPassword());
        backToLoginBtn.setOnClickListener(v -> navigateToLogin());
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Vui lòng nhập email!");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email không hợp lệ!");
            return;
        }

        if (!sharedPreferences.contains(email)) {
            showToast("Email này chưa được đăng ký!");
            return;
        }

        // Giả lập gửi email reset mật khẩu (có thể thay bằng API thực tế sau)
        showToast("Đã gửi email đặt lại mật khẩu tới " + email);
        navigateToLogin();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(QuenMatKhau.this, DangNhap.class);
        startActivity(intent);
        finish();
    }
}
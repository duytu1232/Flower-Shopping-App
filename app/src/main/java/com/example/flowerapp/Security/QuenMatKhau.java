package com.example.flowerapp.Security;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;

public class QuenMatKhau extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordBtn, backToLoginBtn;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


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

        // Sử dụng Firebase Authentication để gửi email reset password
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(QuenMatKhau.this, "Đã gửi email đặt lại mật khẩu tới " + email, Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        Toast.makeText(QuenMatKhau.this, "Gửi email thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(QuenMatKhau.this, DangNhap.class);
        startActivity(intent);
        finish();
    }
}
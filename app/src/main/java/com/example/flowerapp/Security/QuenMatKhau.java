package com.example.flowerapp.Security;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

public class QuenMatKhau extends AppCompatActivity {
    private EditText emailEditText;
    private Button resetPasswordBtn, backToLoginBtn;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mat_khau);

        dbHelper = new DatabaseHelper(this);

        emailEditText = findViewById(R.id.email_edit_text);
        resetPasswordBtn = findViewById(R.id.reset_password_btn);
        backToLoginBtn = findViewById(R.id.back_to_login_btn);

        resetPasswordBtn.setOnClickListener(v -> resetPassword());
        backToLoginBtn.setOnClickListener(v -> navigateToLogin());
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Vui lòng nhập email hợp lệ");
            return;
        }

        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE email = ?", new String[]{email});

            try {
                if (cursor.moveToFirst()) {
                    // Giả lập gửi email (cần tích hợp thư viện gửi email thực tế)
                    String resetLink = "https://yourapp.com/reset?email=" + email; // Ví dụ
                    Toast.makeText(this, "Đã gửi liên kết đặt lại mật khẩu tới " + email, Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                } else {
                    Toast.makeText(this, "Email không tồn tại!", Toast.LENGTH_SHORT).show();
                }
            } finally {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("QuenMatKhau", "Lỗi khi kiểm tra email: " + e.getMessage());
            Toast.makeText(this, "Lỗi hệ thống, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, DangNhap.class));
        finish();
    }
}
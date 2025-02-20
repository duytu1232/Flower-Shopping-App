package com.example.flowerapp.Security;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerapp.R;

public class QuenMatKhau extends AppCompatActivity {

    EditText emailEditText;
    Button resetPasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quen_mat_khau);

        // Ánh xạ các thành phần trong layout
        emailEditText = findViewById(R.id.email_edit_text);
        resetPasswordBtn = findViewById(R.id.reset_password_btn);

        // Xử lý sự kiện nhấn nút Reset Password
        resetPasswordBtn.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(QuenMatKhau.this, "Please enter your email!", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(QuenMatKhau.this, "Invalid email format!", Toast.LENGTH_SHORT).show();
            } else {
                // Thực hiện logic gửi email reset mật khẩu
                Toast.makeText(QuenMatKhau.this, "Password reset email sent to " + email, Toast.LENGTH_SHORT).show();
                finish(); // Đóng màn hình quên mật khẩu sau khi gửi email
            }
        });
    }
}

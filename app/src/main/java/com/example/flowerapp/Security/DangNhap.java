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

import com.example.flowerapp.Admin.AdminActivity;
import com.example.flowerapp.MainActivity;
import com.example.flowerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DangNhap extends AppCompatActivity {

    private EditText edit_txt_Username, edit_txt_Password;
    private Button Login_btn, Signup_btn;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        String email = edit_txt_Username.getText().toString().trim();  // Sử dụng email thay vì username
        String password = edit_txt_Password.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(DangNhap.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            db.collection("users").document(userId).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            String role = documentSnapshot.getString("role");
                                            Toast.makeText(DangNhap.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                            Intent intent;
                                            if ("admin".equals(role)) {
                                                intent = new Intent(DangNhap.this, AdminActivity.class);  // Chuyển đến giao diện Admin
                                            } else {
                                                intent = new Intent(DangNhap.this, MainActivity.class);  // Chuyển đến giao diện User
                                            }
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(DangNhap.this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(DangNhap.this, "Lỗi kiểm tra vai trò: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Toast.makeText(DangNhap.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
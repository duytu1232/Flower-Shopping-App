package com.example.flowerapp.Security;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowerapp.R;

public class DangKy extends AppCompatActivity {

    private EditText editTxtUsername, editTxtFullName, editTxtEmail, editTxtPhone, editTxtPassword, editTxtConfirmPassword;
    private CheckBox checkboxTerms;
    private Button signupBtn;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dang_ky);

        initViews();
        setupWindowInsets();
        setupListeners();
    }

    private void initViews() {
        editTxtUsername = findViewById(R.id.edit_txt_Username);
        editTxtFullName = findViewById(R.id.edit_txt_Full_Name);
        editTxtEmail = findViewById(R.id.edit_txt_New_Email);
        editTxtPhone = findViewById(R.id.edit_txt_Phone);
        editTxtPassword = findViewById(R.id.edit_txt_Password);
        editTxtConfirmPassword = findViewById(R.id.edit_txt_ConfirmPassword);
        checkboxTerms = findViewById(R.id.checkbox_terms);
        signupBtn = findViewById(R.id.Sign_Up_btn);
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
        signupBtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = editTxtUsername.getText().toString().trim();
        String fullName = editTxtFullName.getText().toString().trim();
        String email = editTxtEmail.getText().toString().trim();
        String phone = editTxtPhone.getText().toString().trim();
        String password = editTxtPassword.getText().toString().trim();
        String confirmPassword = editTxtConfirmPassword.getText().toString().trim();

        if (!validateInput(username, fullName, email, phone, password, confirmPassword)) {
            return;
        }

        if (!checkboxTerms.isChecked()) {
            showToast("Bạn phải đồng ý với điều khoản trước khi đăng ký!");
            return;
        }

        if (sharedPreferences.contains(email)) {
            showToast("Email đã được đăng ký!");
            return;
        }

        saveUserData(username, fullName, email, phone, password);
        showToast("Đăng ký thành công!");
        navigateToLogin();
    }

    private boolean validateInput(String username, String fullName, String email, String phone,
                                  String password, String confirmPassword) {
        boolean isValid = true;

        if (TextUtils.isEmpty(username)) {
            editTxtUsername.setError("Vui lòng nhập tên người dùng");
            isValid = false;
        }

        if (TextUtils.isEmpty(fullName)) {
            editTxtFullName.setError("Vui lòng nhập họ và tên");
            isValid = false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTxtEmail.setError("Vui lòng nhập email hợp lệ");
            isValid = false;
        }

        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            editTxtPhone.setError("Vui lòng nhập số điện thoại hợp lệ (ít nhất 10 số)");
            isValid = false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            editTxtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            editTxtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            isValid = false;
        }

        return isValid;
    }

    private void saveUserData(String username, String fullName, String email, String phone, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", fullName); // Lưu fullName làm username chính để hiển thị
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.putString("password", password);
        editor.putString("loginUsername", username); // Lưu username riêng để đăng nhập
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(DangKy.this, DangNhap.class);
        startActivity(intent);
        finish();
    }
}
package com.example.flowerapp.Security;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerapp.Models.User;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import org.mindrot.jbcrypt.BCrypt;

public class DangKy extends AppCompatActivity {
    private EditText editTxtUsername, editTxtFullName, editTxtEmail, editTxtPhone, editTxtPassword, editTxtConfirmPassword;
    private CheckBox checkboxTerms;
    private Button signupBtn;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);

        dbHelper = new DatabaseHelper(this);
        initViews();
        signupBtn.setOnClickListener(v -> registerUser());
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
    }

    private void registerUser() {
        String username = editTxtUsername.getText().toString().trim();
        String fullName = editTxtFullName.getText().toString().trim();
        String email = editTxtEmail.getText().toString().trim();
        String phone = editTxtPhone.getText().toString().trim();
        String password = editTxtPassword.getText().toString().trim();
        String confirmPassword = editTxtConfirmPassword.getText().toString().trim();

        if (!validateInput(username, fullName, email, phone, password, confirmPassword)) return;
        if (!checkboxTerms.isChecked()) {
            Toast.makeText(this, "Vui lòng đồng ý với điều khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEmailExists(email)) {
            editTxtEmail.setError("Email đã được sử dụng!");
            return;
        }
        if (isUsernameExists(username)) {
            editTxtUsername.setError("Tên người dùng đã được sử dụng!");
            return;
        }

        saveUserToSQLite(username, fullName, email, phone, password);
        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, DangNhap.class));
        finish();
    }

    private void saveUserToSQLite(String username, String fullName, String email, String phone, String password) {
        SQLiteDatabase db = dbHelper.openDatabase();
        try {
            User user = new User(
                    0, // userId sẽ được tự động tạo bởi AUTOINCREMENT
                    username,
                    email,
                    "customer",
                    "active",
                    fullName,
                    phone,
                    null // avatarUri mặc định là null
            );

            String hashedPassword = hashPassword(password);

            ContentValues values = new ContentValues();
            values.put("username", user.getUsername());
            values.put("password", hashedPassword);
            values.put("email", user.getEmail());
            values.put("role", user.getRole());
            values.put("status", user.getStatus());
            values.put("full_name", user.getFullName());
            values.put("phone", user.getPhone());

            long result = db.insert("Users", null, values);
            if (result != -1) {
                Log.d("DangKy", "Người dùng đã được lưu thành công, ID: " + result);
                user.setUserId((int) result);
            } else {
                Log.e("DangKy", "Lỗi khi lưu người dùng vào cơ sở dữ liệu");
                Toast.makeText(this, "Lỗi lưu thông tin người dùng!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("DangKy", "Lỗi lưu người dùng vào cơ sở dữ liệu: " + e.getMessage());
            Toast.makeText(this, "Lỗi hệ thống, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        } finally {
            dbHelper.closeDatabase(db);
        }
    }

    private boolean isEmailExists(String email) {
        SQLiteDatabase db = dbHelper.openDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE email = ?", new String[]{email});
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        } catch (Exception e) {
            Log.e("DangKy", "Lỗi kiểm tra email: " + e.getMessage());
            return false;
        } finally {
            dbHelper.closeDatabase(db);
        }
    }

    private boolean isUsernameExists(String username) {
        SQLiteDatabase db = dbHelper.openDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE username = ?", new String[]{username});
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        } catch (Exception e) {
            Log.e("DangKy", "Lỗi kiểm tra username: " + e.getMessage());
            return false;
        } finally {
            dbHelper.closeDatabase(db);
        }
    }

    private boolean validateInput(String username, String fullName, String email, String phone, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            editTxtUsername.setError("Vui lòng nhập tên người dùng");
            return false;
        }
        if (TextUtils.isEmpty(fullName)) {
            editTxtFullName.setError("Vui lòng nhập họ tên");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTxtEmail.setError("Vui lòng nhập email hợp lệ");
            return false;
        }
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            editTxtPhone.setError("Số điện thoại không hợp lệ");
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            editTxtPassword.setError("Mật khẩu phải từ 6 ký tự");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            editTxtConfirmPassword.setError("Mật khẩu không khớp");
            return false;
        }
        return true;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
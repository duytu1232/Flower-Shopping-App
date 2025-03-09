package com.example.flowerapp.Security;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.flowerapp.Admin.AdminActivity;
import com.example.flowerapp.MainActivity;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

public class DangNhap extends AppCompatActivity {
    private static final String TAG = "DangNhap";
    private EditText edit_txt_Username, edit_txt_Password;
    private Button Login_btn, Signup_btn;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_nhap);

        dbHelper = new DatabaseHelper(this);

        edit_txt_Username = findViewById(R.id.edit_txt_Username);
        edit_txt_Password = findViewById(R.id.edit_txt_Password);
        Login_btn = findViewById(R.id.Login_btn);
        Signup_btn = findViewById(R.id.Sign_Up_btn);

        Login_btn.setOnClickListener(v -> loginUser());
        Signup_btn.setOnClickListener(v -> startActivity(new Intent(this, DangKy.class)));
    }

    private void loginUser() {
        String email = edit_txt_Username.getText().toString().trim();
        String password = edit_txt_Password.getText().toString().trim();

        Log.d(TAG, "Đăng nhập với Email: " + email + ", Password: " + password);

        if (TextUtils.isEmpty(email)) {
            edit_txt_Username.setError("Vui lòng nhập email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            edit_txt_Password.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edit_txt_Username.setError("Email không hợp lệ");
            return;
        }

        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            // So sánh trực tiếp plaintext password (không dùng hash vì giữ nguyên plaintext)
            Cursor cursor = db.rawQuery("SELECT user_id, username, email, role, status FROM Users WHERE email = ? AND password = ?", new String[]{email, password});

            try {
                if (cursor.moveToFirst()) {
                    int userIdIndex = cursor.getColumnIndex("user_id");
                    int usernameIndex = cursor.getColumnIndex("username");
                    int roleIndex = cursor.getColumnIndex("role");
                    int statusIndex = cursor.getColumnIndex("status");

                    if (userIdIndex == -1 || usernameIndex == -1 || roleIndex == -1 || statusIndex == -1) {
                        Log.e(TAG, "Cột 'user_id', 'username', 'role' hoặc 'status' không tồn tại trong kết quả!");
                        Toast.makeText(this, "Lỗi cơ sở dữ liệu: Thiếu cột cần thiết!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int userId = cursor.getInt(userIdIndex);
                    String username = cursor.getString(usernameIndex);
                    String role = cursor.getString(roleIndex);
                    String status = cursor.getString(statusIndex);

                    Log.d(TAG, "Đăng nhập thành công - Email: " + email + ", Role: " + role + ", Status: " + status);

                    if ("locked".equals(status.toLowerCase())) {
                        Toast.makeText(this, "Tài khoản bị khóa!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Lưu thông tin người dùng vào SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("user_id", userId);
                    editor.putString("username", username);
                    editor.apply();

                    // Ngoại lệ đặc biệt cho tài khoản admin: luôn cho phép đăng nhập vào AdminActivity
                    if ("admin".equals(role.toLowerCase()) || email.equals("admin123")) {
                        Toast.makeText(this, "Đăng nhập thành công với vai trò Admin!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, AdminActivity.class));
                        finish();
                        return;
                    }

                    // Cho các vai trò khác (customer, staff) chuyển đến MainActivity
                    Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d(TAG, "Không tìm thấy người dùng với email: " + email + ", password: " + password);
                    Toast.makeText(this, "Đăng nhập thất bại: Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                }
            } finally {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi truy vấn cơ sở dữ liệu: " + e.getMessage());
            Toast.makeText(this, "Lỗi hệ thống, vui lòng thử lại!", Toast.LENGTH_SHORT).show();

            // Ngoại lệ đặc biệt cho tài khoản admin: cho phép đăng nhập mặc định nếu có lỗi cơ sở dữ liệu
            if (email.equals("admin123") && password.equals("admin")) {
                Log.w(TAG, "Đăng nhập admin bằng ngoại lệ do lỗi cơ sở dữ liệu");
                Toast.makeText(this, "Đăng nhập Admin thành công (ngoại lệ do lỗi hệ thống)!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AdminActivity.class));
                finish();
            }
        }
    }
}
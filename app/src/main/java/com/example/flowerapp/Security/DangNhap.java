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
import com.example.flowerapp.Models.User;
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

        SQLiteDatabase db = dbHelper.openDatabase();
        try {
            // Chỉ lấy các cột cơ bản để kiểm tra đăng nhập
            Cursor cursor = db.rawQuery("SELECT user_id, username, password, email, role, status FROM Users WHERE email = ?", new String[]{email});

            try {
                if (cursor.moveToFirst()) {
                    int userIdIndex = cursor.getColumnIndex("user_id");
                    int usernameIndex = cursor.getColumnIndex("username");
                    int passwordIndex = cursor.getColumnIndex("password");
                    int emailIndex = cursor.getColumnIndex("email");
                    int roleIndex = cursor.getColumnIndex("role");
                    int statusIndex = cursor.getColumnIndex("status");

                    if (userIdIndex == -1 || usernameIndex == -1 || passwordIndex == -1 || emailIndex == -1 ||
                            roleIndex == -1 || statusIndex == -1) {
                        Log.e(TAG, "Cột không tồn tại trong kết quả!");
                        Toast.makeText(this, "Lỗi cơ sở dữ liệu: Thiếu cột cần thiết!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String storedPassword = cursor.getString(passwordIndex);
                    if (storedPassword.equals(password)) { // So sánh trực tiếp (tạm thời, thay bằng BCrypt sau)
                        // Lấy thêm các cột khác nếu cần (full_name, phone, avatar_uri)
                        String fullName = cursor.getColumnIndex("full_name") >= 0 ? cursor.getString(cursor.getColumnIndex("full_name")) : null;
                        String phone = cursor.getColumnIndex("phone") >= 0 ? cursor.getString(cursor.getColumnIndex("phone")) : null;
                        String avatarUri = cursor.getColumnIndex("avatar_uri") >= 0 ? cursor.getString(cursor.getColumnIndex("avatar_uri")) : null;

                        User user = new User(
                                cursor.getInt(userIdIndex),
                                cursor.getString(usernameIndex),
                                cursor.getString(passwordIndex),
                                cursor.getString(emailIndex),
                                cursor.getString(roleIndex),
                                cursor.getString(statusIndex),
                                fullName,
                                phone,
                                avatarUri
                        );

                        Log.d(TAG, "Đăng nhập thành công - Email: " + user.getEmail() + ", Role: " + user.getRole() + ", Status: " + user.getStatus());

                        if ("locked".equals(user.getStatus().toLowerCase())) {
                            Toast.makeText(this, "Tài khoản bị khóa!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("user_id", user.getUserId());
                        editor.putString("username", user.getUsername());
                        editor.apply();
                        Log.d(TAG, "User ID sau khi lưu: " + prefs.getInt("user_id", -1));

                        if ("admin".equals(user.getRole().toLowerCase()) || email.equals("admin123")) {
                            Toast.makeText(this, "Đăng nhập thành công với vai trò Admin!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, AdminActivity.class));
                            finish();
                            return;
                        }

                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d(TAG, "Mật khẩu không đúng cho email: " + email);
                        Toast.makeText(this, "Đăng nhập thất bại: Mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Không tìm thấy người dùng với email: " + email);
                    Toast.makeText(this, "Đăng nhập thất bại: Email không tồn tại!", Toast.LENGTH_SHORT).show();
                }
            } finally {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi truy vấn cơ sở dữ liệu: " + e.getMessage());
            Toast.makeText(this, "Lỗi hệ thống, vui lòng thử lại!", Toast.LENGTH_SHORT).show();

            // Loại bỏ logic tạm thời cho admin123/admin (khuyến nghị xóa sau khi hoàn thiện)
            if (email.equals("admin123") && password.equals("admin")) {
                Log.w(TAG, "Đăng nhập admin bằng ngoại lệ do lỗi hệ thống");
                Toast.makeText(this, "Đăng nhập Admin thành công (ngoại lệ do lỗi hệ thống)!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, AdminActivity.class));
                finish();
            }
        } finally {
            dbHelper.closeDatabase(db);
        }
    }
}
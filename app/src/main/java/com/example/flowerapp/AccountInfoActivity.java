package com.example.flowerapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerapp.Models.User;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

public class AccountInfoActivity extends AppCompatActivity {
    private TextView usernameTextView, emailTextView, fullNameTextView, phoneTextView;
    private ImageButton backButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        dbHelper = new DatabaseHelper(this);
        usernameTextView = findViewById(R.id.account_username);
        emailTextView = findViewById(R.id.account_email);
        fullNameTextView = findViewById(R.id.account_full_name);
        phoneTextView = findViewById(R.id.account_phone);
        backButton = findViewById(R.id.back_button);

        // Xử lý nút quay lại
        backButton.setOnClickListener(v -> finish());

        loadUserInfo();
    }

    private void loadUserInfo() {
        int userId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getInt("user_id", -1);
        Log.d("AccountInfoActivity", "User ID từ SharedPreferences: " + userId);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SQLiteDatabase db = dbHelper.openDatabase();
        try {
            String[] columns = {"user_id", "username", "email", "full_name", "phone", "avatar_uri"};
            Cursor cursor = db.query("Users", columns, "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);
            if (cursor.moveToFirst()) {
                int userIdColumn = cursor.getColumnIndex("user_id");
                int usernameColumn = cursor.getColumnIndex("username");
                int emailColumn = cursor.getColumnIndex("email");
                int fullNameColumn = cursor.getColumnIndex("full_name");
                int phoneColumn = cursor.getColumnIndex("phone");
                int avatarUriColumn = cursor.getColumnIndex("avatar_uri");

                if (userIdColumn >= 0 && usernameColumn >= 0 && emailColumn >= 0) {
                    int id = cursor.getInt(userIdColumn);
                    String username = cursor.getString(usernameColumn);
                    String email = cursor.getString(emailColumn);
                    String fullName = fullNameColumn >= 0 ? cursor.getString(fullNameColumn) : null;
                    String phone = phoneColumn >= 0 ? cursor.getString(phoneColumn) : null;
                    String avatarUri = avatarUriColumn >= 0 ? cursor.getString(avatarUriColumn) : null;

                    User user = new User(
                            id,
                            username,
                            email,
                            null,
                            null,
                            fullName,
                            phone,
                            avatarUri
                    );

                    usernameTextView.setText(user.getUsername() != null ? user.getUsername() : "N/A");
                    emailTextView.setText(user.getEmail() != null ? user.getEmail() : "N/A");
                    fullNameTextView.setText(user.getFullName() != null ? user.getFullName() : "N/A");
                    phoneTextView.setText(user.getPhone() != null ? user.getPhone() : "N/A");
                } else {
                    Toast.makeText(this, "Thiếu cột bắt buộc trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Không tải được thông tin", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("AccountInfoActivity", "Lỗi truy vấn cơ sở dữ liệu: " + e.getMessage());
        } finally {
            dbHelper.closeDatabase(db);
        }
    }
}
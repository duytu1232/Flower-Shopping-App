package com.example.flowerapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            dbHelper.openDatabase();
            String[] columns = {"username", "email", "full_name", "phone"};
            Cursor cursor = dbHelper.getReadableDatabase().query("Users", columns, "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);
            if (cursor.moveToFirst()) {
                usernameTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("username")));
                emailTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
                fullNameTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
                phoneTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            } else {
                Toast.makeText(this, "Không tải được thông tin", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            dbHelper.closeDatabase(null);
        }
    }
}
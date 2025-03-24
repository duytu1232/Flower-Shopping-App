package com.example.flowerapp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerapp.Models.User;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

public class EditAccountInfoActivity extends AppCompatActivity {
    private TextInputEditText usernameInput, emailInput, fullNameInput, phoneInput, addressInput;
    private ImageButton backButton;
    private Button saveButton;
    private DatabaseHelper dbHelper;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account_info);

        dbHelper = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.edit_account_username);
        emailInput = findViewById(R.id.edit_account_email);
        fullNameInput = findViewById(R.id.edit_account_full_name);
        phoneInput = findViewById(R.id.edit_account_phone);
        addressInput = findViewById(R.id.edit_account_address);
        backButton = findViewById(R.id.back_button);
        saveButton = findViewById(R.id.save_button);

        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveUserInfo());

        loadUserInfo();
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.query("Users", null, "user_id = ?", new String[]{String.valueOf(userId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                currentUser = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("username")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        null,
                        null,
                        cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        null
                );
                usernameInput.setText(currentUser.getUsername());
                emailInput.setText(currentUser.getEmail());
                fullNameInput.setText(currentUser.getFullName());
                phoneInput.setText(currentUser.getPhone());
                // Lấy địa chỉ từ đơn hàng gần nhất
                Cursor addressCursor = db.rawQuery(
                        "SELECT shipping_address FROM Orders WHERE user_id = ? ORDER BY order_date DESC LIMIT 1",
                        new String[]{String.valueOf(userId)});
                if (addressCursor != null && addressCursor.moveToFirst()) {
                    String shippingAddress = addressCursor.getString(addressCursor.getColumnIndexOrThrow("shipping_address"));
                    addressInput.setText(shippingAddress != null ? shippingAddress : "");
                }
                if (addressCursor != null) addressCursor.close();
            }
            if (cursor != null) cursor.close();
        } catch (Exception e) {
            Log.e("EditAccountInfoActivity", "Error loading user info: " + e.getMessage());
            Toast.makeText(this, "Error loading user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private void saveUserInfo() {
        if (!validateInputs()) return;

        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            String username = usernameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String fullName = fullNameInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            db.execSQL(
                    "UPDATE Users SET username = ?, email = ?, full_name = ?, phone = ? WHERE user_id = ?",
                    new Object[]{username, email, fullName, phone, currentUser.getUserId()}
            );
            Toast.makeText(this, "User info updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Log.e("EditAccountInfoActivity", "Error saving user info: " + e.getMessage());
            Toast.makeText(this, "Error saving user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (usernameInput.getText().toString().trim().isEmpty()) {
            usernameInput.setError("Username is required");
            isValid = false;
        }
        if (emailInput.getText().toString().trim().isEmpty()) {
            emailInput.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.getText().toString().trim()).matches()) {
            emailInput.setError("Invalid email format");
            isValid = false;
        }
        if (fullNameInput.getText().toString().trim().isEmpty()) {
            fullNameInput.setError("Full name is required");
            isValid = false;
        }
        if (phoneInput.getText().toString().trim().isEmpty()) {
            phoneInput.setError("Phone number is required");
            isValid = false;
        }

        return isValid;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
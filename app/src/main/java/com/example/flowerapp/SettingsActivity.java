package com.example.flowerapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch notificationsSwitch = findViewById(R.id.notifications_switch);
        Button changePasswordBtn = findViewById(R.id.change_password_btn);

        if (notificationsSwitch != null && changePasswordBtn != null) {
            // Lưu trạng thái thông báo
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
            notificationsSwitch.setChecked(notificationsEnabled);

            notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("notifications_enabled", isChecked);
                editor.apply();
            });

            changePasswordBtn.setOnClickListener(v -> {
                // Chuyển đến activity đổi mật khẩu (tạo sau nếu cần)
                Toast.makeText(this, "Change Password clicked", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Settings components not found", Toast.LENGTH_SHORT).show();
        }
    }
}
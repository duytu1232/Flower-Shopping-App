package com.example.flowerapp.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.flowerapp.Admin.Fragments.CouponManagementFragment;
import com.example.flowerapp.Admin.Fragments.OrderManagementFragment;
import com.example.flowerapp.Admin.Fragments.ProductManagementFragment;
import com.example.flowerapp.Admin.Fragments.UserManagementFragment;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.DangNhap;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;

public class AdminActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavAdmin;
    private final HashMap<Integer, Fragment> fragmentMap = new HashMap<>();
    private ImageView logOutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        initViews();
        setupBottomNav();
        setupLogOutButton();

        // Mở Fragment mặc định (Quản lý hàng hóa)
        if (savedInstanceState == null) {
            bottomNavAdmin.setSelectedItemId(R.id.menu_product);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        bottomNavAdmin = findViewById(R.id.bottomNavAdmin);
        logOutButton = findViewById(R.id.LogOut);

        // Khởi tạo các fragment cho từng trang quản lý (sử dụng SQLite)
        fragmentMap.put(R.id.menu_product, new ProductManagementFragment());
        fragmentMap.put(R.id.menu_users, new UserManagementFragment());
        fragmentMap.put(R.id.menu_coupons, new CouponManagementFragment());
        fragmentMap.put(R.id.menu_orders, new OrderManagementFragment());
    }

    private void setupBottomNav() {
        bottomNavAdmin.setOnItemSelectedListener(item -> {
            try {
                Fragment selectedFragment = fragmentMap.get(item.getItemId());
                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                    return true;
                }
                return false;
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi chuyển đổi fragment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void setupLogOutButton() {
        logOutButton.setOnClickListener(v -> {
            // Hiển thị thông báo xác nhận trước khi logout
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận đăng xuất")
                    .setMessage("Bạn có chắc muốn đăng xuất?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        // Xóa thông tin đăng nhập (nếu có, ví dụ: SharedPreferences)
                        clearSession();
                        // Quay về màn hình đăng nhập
                        Intent intent = new Intent(AdminActivity.this, DangNhap.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        Toast.makeText(AdminActivity.this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void replaceFragment(Fragment newFragment) {
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_admin, newFragment)
                    .commitNow(); // Sử dụng commitNow để đảm bảo giao diện cập nhật ngay lập tức
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi thay thế fragment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void clearSession() {
        // Xóa thông tin đăng nhập (nếu bạn sử dụng SharedPreferences hoặc Firebase Auth)
        // Ví dụ:
        /*
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear().apply();
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đảm bảo đóng các tài nguyên nếu cần
    }
}
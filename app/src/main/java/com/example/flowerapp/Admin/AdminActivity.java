package com.example.flowerapp.Admin;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;

public class AdminActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavAdmin;
    private final HashMap<Integer, Fragment> fragmentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        initViews();
        setupBottomNav();

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

        // Khởi tạo các fragment cho từng trang quản lý (sử dụng SQLite)
        fragmentMap.put(R.id.menu_product, new ProductManagementFragment());
        fragmentMap.put(R.id.menu_users, new UserManagementFragment());
        fragmentMap.put(R.id.menu_coupons, new CouponManagementFragment());
        fragmentMap.put(R.id.menu_orders, new OrderManagementFragment());
    }

    private void setupBottomNav() {
        bottomNavAdmin.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = fragmentMap.get(item.getItemId());
            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void replaceFragment(Fragment newFragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_admin, newFragment)
                .addToBackStack(null)
                .commit();
    }
}
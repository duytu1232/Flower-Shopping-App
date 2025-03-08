package com.example.flowerapp.Admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.flowerapp.Admin.Fragments.CouponManagementFragment;
import com.example.flowerapp.Admin.Fragments.OrderManagementFragment;
import com.example.flowerapp.Admin.Fragments.ProductManagementFragment;
import com.example.flowerapp.Admin.Fragments.RevenueManagementFragment;
import com.example.flowerapp.Admin.Fragments.UserManagementFragment;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.DangNhap;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;

public class AdminActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavAdmin;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private final HashMap<Integer, Fragment> fragmentMap = new HashMap<>();
    private ImageView logOutButton;
    private ImageView sideMenuButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupBottomNav();
        setupDrawer();
        setupLogOutButton();
        setupUserInfo();

        if (savedInstanceState == null) {
            bottomNavAdmin.setSelectedItemId(R.id.menu_product);
            navView.setCheckedItem(R.id.nav_product);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initViews() {
        bottomNavAdmin = findViewById(R.id.bottomNavAdmin);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        logOutButton = findViewById(R.id.LogOut);
        sideMenuButton = findViewById(R.id.side_menu);

        fragmentMap.put(R.id.menu_product, new ProductManagementFragment());
        fragmentMap.put(R.id.menu_users, new UserManagementFragment());
        fragmentMap.put(R.id.menu_coupons, new CouponManagementFragment());
        fragmentMap.put(R.id.menu_orders, new OrderManagementFragment());
        fragmentMap.put(R.id.menu_revenue, new RevenueManagementFragment());

        fragmentMap.put(R.id.nav_product, fragmentMap.get(R.id.menu_product));
        fragmentMap.put(R.id.nav_users, fragmentMap.get(R.id.menu_users));
        fragmentMap.put(R.id.nav_coupons, fragmentMap.get(R.id.menu_coupons));
        fragmentMap.put(R.id.nav_orders, fragmentMap.get(R.id.menu_orders));
        fragmentMap.put(R.id.nav_revenue, fragmentMap.get(R.id.menu_revenue));
    }

    private void setupBottomNav() {
        bottomNavAdmin.setOnItemSelectedListener(item -> {
            try {
                int itemId = item.getItemId();
                Fragment selectedFragment = fragmentMap.get(itemId);
                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                    int navItemId = getNavItemIdFromBottomItemId(itemId);
                    navView.setCheckedItem(navItemId);
                    return true;
                }
                return false;
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi chuyển đổi fragment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void setupDrawer() {
        sideMenuButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navView.setNavigationItemSelectedListener(item -> {
            try {
                int itemId = item.getItemId();
                Fragment selectedFragment = fragmentMap.get(itemId);
                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                    int bottomItemId = getBottomItemIdFromNavItemId(itemId);
                    bottomNavAdmin.setSelectedItemId(bottomItemId);
                    drawerLayout.closeDrawer(GravityCompat.START);
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
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận đăng xuất")
                    .setMessage("Bạn có chắc muốn đăng xuất?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        clearSession();
                        Intent intent = new Intent(AdminActivity.this, DangNhap.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    private void setupUserInfo() {
        TextView userNameTextView = navView.getHeaderView(0).findViewById(R.id.user_name);
        SQLiteDatabase db = dbHelper.openDatabase();
        Cursor cursor = db.rawQuery("SELECT username FROM Users WHERE role = 'admin' LIMIT 1", null);
        if (cursor.moveToFirst()) {
            String username = cursor.getString(0);
            userNameTextView.setText(username);
        } else {
            userNameTextView.setText("Admin");
        }
        cursor.close();
        dbHelper.closeDatabase(db);
    }

    private void replaceFragment(Fragment newFragment) {
        try {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_admin, newFragment)
                    .commitNow();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi thay thế fragment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int getNavItemIdFromBottomItemId(int bottomItemId) {
        if (bottomItemId == R.id.menu_revenue) return R.id.nav_revenue;
        if (bottomItemId == R.id.menu_product) return R.id.nav_product;
        if (bottomItemId == R.id.menu_users) return R.id.nav_users;
        if (bottomItemId == R.id.menu_coupons) return R.id.nav_coupons;
        if (bottomItemId == R.id.menu_orders) return R.id.nav_orders;
        return R.id.nav_product;
    }

    private int getBottomItemIdFromNavItemId(int navItemId) {
        if (navItemId == R.id.nav_revenue) return R.id.menu_revenue;
        if (navItemId == R.id.nav_product) return R.id.menu_product;
        if (navItemId == R.id.nav_users) return R.id.menu_users;
        if (navItemId == R.id.nav_coupons) return R.id.menu_coupons;
        if (navItemId == R.id.nav_orders) return R.id.menu_orders;
        return R.id.menu_product;
    }

    private void clearSession() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.closeDatabase(dbHelper.openDatabase());
        }
    }
}
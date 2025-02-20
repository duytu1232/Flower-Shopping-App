package com.example.flowerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.flowerapp.Fragments.FragmentAccountUser;
import com.example.flowerapp.Fragments.FragmentCart;
import com.example.flowerapp.Fragments.FragmentFavorite;
import com.example.flowerapp.Fragments.FragmentHome;
import com.example.flowerapp.Fragments.FragmentShop;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private LinearLayout headerLayout;
    private LinearLayout khoangTrongMenu;

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        headerLayout = findViewById(R.id.header_layout);
        khoangTrongMenu = findViewById(R.id.khoang_trong_menu);
        bottomNav = findViewById(R.id.bottomNavMain);

        // Kiểm tra nếu có Intent mở FragmentAccountUser
        String openFragment = getIntent().getStringExtra("openFragment");

        if (savedInstanceState == null) {  // Tránh replaceFragment 2 lần khi mở app
            if ("account".equals(openFragment)) {
                replaceFragment(new FragmentAccountUser());
                bottomNav.setSelectedItemId(R.id.bottomItemAccount); // Đánh dấu tab "Account"
            } else {
                replaceFragment(new FragmentHome()); // Mặc định vào Home nếu không có yêu cầu đặc biệt
            }
        }

        // Xử lý sự kiện BottomNavigation
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.bottomItemHome) {
                selectedFragment = new FragmentHome();
            } else if (id == R.id.bottomItemShop) {
                selectedFragment = new FragmentShop();
            } else if (id == R.id.bottomItemFavorite) {
                selectedFragment = new FragmentFavorite();
            } else if (id == R.id.bottomItemCart) {
                selectedFragment = new FragmentCart();
            } else if (id == R.id.bottomItemAccount) {
                selectedFragment = new FragmentAccountUser();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true; // Chỉ trả về true một lần
            } else {
                return false;
            }
        });

        // Xử lý nút tìm kiếm
        ImageView searchIcon = findViewById(R.id.Search_bar_icon);
        searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TimKiem.class);
            startActivity(intent);
        });
    }


    // Hàm thay thế Fragment
    private void replaceFragment(Fragment newFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null); // Hỗ trợ nút Back để quay lại Fragment trước
        transaction.commit();

        // Kiểm tra nếu layout không null trước khi thay đổi visibility
        if (headerLayout != null && khoangTrongMenu != null) {
            if (newFragment instanceof FragmentAccountUser ||
                    newFragment instanceof FragmentFavorite ||
                    newFragment instanceof FragmentCart ||
                    newFragment instanceof FragmentShop) {

                headerLayout.setVisibility(View.GONE);
                khoangTrongMenu.setVisibility(View.GONE);
            } else {
                headerLayout.setVisibility(View.VISIBLE);
                khoangTrongMenu.setVisibility(View.VISIBLE);
            }
        }

    }




}

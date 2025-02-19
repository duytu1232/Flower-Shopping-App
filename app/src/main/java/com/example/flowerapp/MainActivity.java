package com.example.flowerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.flowerapp.Fragments.FragmentAccountUser;
import com.example.flowerapp.Fragments.FragmentCart;
import com.example.flowerapp.Fragments.FragmentFavorite;
import com.example.flowerapp.Fragments.FragmentHome;
import com.example.flowerapp.Fragments.FragmentShop;
import com.example.flowerapp.Fragments.TimKiem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Mặc định hiển thị FragmentHome khi mở app
        if (savedInstanceState == null) {
            replaceFragment(new FragmentHome());
        }

        // Xử lý sự kiện BottomNavigation
        bottomNav = findViewById(R.id.bottomNavMain);
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
    }
}

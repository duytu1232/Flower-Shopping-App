package com.example.flowerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.flowerapp.User.Fragments.FragmentAccountUser;
import com.example.flowerapp.User.Fragments.FragmentCart;
import com.example.flowerapp.User.Fragments.FragmentFavorite;
import com.example.flowerapp.User.Fragments.FragmentHome;
import com.example.flowerapp.User.Fragments.FragmentShop;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private LinearLayout headerLayout, khoangTrongMenu;
    private BottomNavigationView bottomNav;
    private EditText searchEditText;
    private ImageView filterIcon;

    private final HashMap<Integer, Fragment> fragmentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        initViews();
        setupBottomNav();
        setupSearch();
        setupFilter();

        // Mở Fragment mặc định
        String openFragment = getIntent().getStringExtra("openFragment");
        if (savedInstanceState == null) {
            if ("account".equals(openFragment)) {
                bottomNav.setSelectedItemId(R.id.bottomItemAccount);
            } else {
                bottomNav.setSelectedItemId(R.id.bottomItemHome);
            }
        }
    }

    private void initViews() {
        headerLayout = findViewById(R.id.header_layout);
        khoangTrongMenu = findViewById(R.id.khoang_trong_menu);
        bottomNav = findViewById(R.id.bottomNavMain);
        searchEditText = findViewById(R.id.EditText_Searching_Bar);
        filterIcon = findViewById(R.id.filter_icon);

        fragmentMap.put(R.id.bottomItemHome, new FragmentHome());
        fragmentMap.put(R.id.bottomItemShop, new FragmentShop());
        fragmentMap.put(R.id.bottomItemFavorite, new FragmentFavorite());
        fragmentMap.put(R.id.bottomItemCart, new FragmentCart());
        fragmentMap.put(R.id.bottomItemAccount, new FragmentAccountUser());
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = fragmentMap.get(item.getItemId());
            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void setupSearch() {
        findViewById(R.id.Search_bar_icon).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TimKiem.class))
        );

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Toast.makeText(MainActivity.this, "Searching: " + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilter() {
        filterIcon.setOnClickListener(v ->
                Toast.makeText(MainActivity.this, "Filter clicked", Toast.LENGTH_SHORT).show()
        );
    }

    private void replaceFragment(Fragment newFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        boolean hideHeader = newFragment instanceof FragmentAccountUser ||
                newFragment instanceof FragmentFavorite ||
                newFragment instanceof FragmentCart;
        headerLayout.setVisibility(hideHeader ? View.GONE : View.VISIBLE);
        khoangTrongMenu.setVisibility(hideHeader ? View.GONE : View.VISIBLE);
    }
}

package com.example.flowerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.flowerapp.User.Fragments.FragmentHome;
import com.example.flowerapp.User.Fragments.FragmentShop;
import com.example.flowerapp.Security.DangNhap;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private LinearLayout headerLayout, khoangTrongMenu;
    private BottomNavigationView bottomNav;
    private EditText searchEditText;
    private ImageView filterIcon;
    private FragmentShop fragmentShop;

    private final HashMap<Integer, Fragment> fragmentMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        if (prefs.getInt("user_id", -1) == -1) {
            Intent intent = new Intent(this, DangNhap.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        setupBottomNav();
        setupSearch();
        setupFilter();

        ImageView notificationIcon = findViewById(R.id.notificationIcon);
        if (notificationIcon != null) {
            notificationIcon.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
                startActivity(intent);
            });
        }

        String openFragment = getIntent().getStringExtra("openFragment");
        if (savedInstanceState == null) {
            if ("account".equals(openFragment)) {
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.bottomItemAccount);
                }
            } else if ("shop".equals(openFragment)) {
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.bottomItemShop);
                    Bundle args = new Bundle();
                    String searchQuery = getIntent().getStringExtra("search_query");
                    String filterType = getIntent().getStringExtra("filter_type");
                    float filterPriceMin = getIntent().getFloatExtra("filter_price_min", 0f);
                    float filterPriceMax = getIntent().getFloatExtra("filter_price_max", Float.MAX_VALUE);
                    if (searchQuery != null) args.putString("search_query", searchQuery);
                    if (filterType != null) args.putString("filter_type", filterType);
                    args.putFloat("filter_price_min", filterPriceMin);
                    args.putFloat("filter_price_max", filterPriceMax);
                    fragmentShop.setArguments(args);
                    replaceFragment(fragmentShop);
                }
            } else {
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.bottomItemHome);
                }
            }
        }
    }

    private void initViews() {
        Log.d(TAG, "Initializing views");
        headerLayout = findViewById(R.id.header_layout);
        khoangTrongMenu = findViewById(R.id.status_bar_spacer);
        bottomNav = findViewById(R.id.bottomNavMain);
        searchEditText = findViewById(R.id.EditText_Searching_Bar);
        filterIcon = findViewById(R.id.filter_icon);

        fragmentMap.put(R.id.bottomItemHome, new FragmentHome());
        fragmentMap.put(R.id.bottomItemShop, fragmentShop = new FragmentShop());
        fragmentMap.put(R.id.bottomItemCart, new FragmentCart());
        fragmentMap.put(R.id.bottomItemAccount, new FragmentAccountUser());
    }

    private void setupBottomNav() {
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = fragmentMap.get(item.getItemId());
                if (selectedFragment != null) {
                    replaceFragment(selectedFragment);
                    return true;
                }
                return false;
            });
        } else {
            Log.e(TAG, "Bottom navigation not found");
            Toast.makeText(this, "Bottom navigation not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearch() {
        ImageView searchIcon = findViewById(R.id.Search_bar_icon);
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, TimKiem.class);
                startActivity(intent);
            });
        }

        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }

    private void setupFilter() {
        if (filterIcon != null) {
            filterIcon.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, TimKiem.class);
                startActivity(intent);
            });
        }
    }

    private void replaceFragment(Fragment newFragment) {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            boolean hideHeader = newFragment instanceof FragmentAccountUser ||
                    newFragment instanceof FragmentCart ||
                    newFragment instanceof FragmentShop;

            headerLayout.setVisibility(hideHeader ? View.GONE : View.VISIBLE);
            khoangTrongMenu.setVisibility(hideHeader ? View.GONE : View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "Error replacing fragment: " + e.getMessage());
            Toast.makeText(this, "Error loading fragment", Toast.LENGTH_SHORT).show();
        }
    }

    public void openNotification(View view) {
        Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}
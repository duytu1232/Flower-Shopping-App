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
import com.example.flowerapp.User.Fragments.ProductDetail;
import com.example.flowerapp.User.Fragments.ReviewDetail;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
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

        // Xử lý Intent từ TimKiem.java
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String openFragment = intent.getStringExtra("openFragment");
        if (bottomNav == null) {
            Log.e(TAG, "Bottom navigation not initialized");
            return;
        }

        if ("account".equals(openFragment)) {
            bottomNav.setSelectedItemId(R.id.bottomItemAccount);
        } else if ("shop".equals(openFragment)) {
            bottomNav.setSelectedItemId(R.id.bottomItemShop);
            Bundle args = new Bundle();
            String searchQuery = intent.getStringExtra("search_query");
            String filterType = intent.getStringExtra("filter_type");
            float filterPriceMin = intent.getFloatExtra("filter_price_min", 0f);
            float filterPriceMax = intent.getFloatExtra("filter_price_max", Float.MAX_VALUE);
            if (searchQuery != null) args.putString("search_query", searchQuery);
            if (filterType != null) args.putString("filter_type", filterType);
            args.putFloat("filter_price_min", filterPriceMin);
            args.putFloat("filter_price_max", filterPriceMax);

            // Cập nhật arguments cho fragmentShop
            fragmentShop.setArguments(args);
            // Làm mới fragmentShop
            replaceFragment(fragmentShop);
            // Gọi loadProducts() để làm mới danh sách sản phẩm
            if (fragmentShop.isAdded()) {
                fragmentShop.loadProducts();
            }
        } else {
            bottomNav.setSelectedItemId(R.id.bottomItemHome);
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
            // Xử lý sự kiện nhấn Enter
            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    String query = searchEditText.getText().toString().trim();
                    if (!query.isEmpty()) {
                        // Chuyển hướng đến FragmentShop với từ khóa tìm kiếm
                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.putExtra("openFragment", "shop");
                        intent.putExtra("search_query", query);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        return true; // Sự kiện đã được xử lý
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                return false;
            });

            // Giữ TextWatcher nếu bạn cần xử lý thay đổi văn bản thời gian thực
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    // Có thể thêm gợi ý tìm kiếm real-time nếu cần
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

            // Logic ẩn/hiện header và khoảng trống
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
    }    }

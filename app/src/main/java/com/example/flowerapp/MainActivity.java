package com.example.flowerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        // Xử lý Intent ban đầu
        handleIntent(getIntent());
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

            // Load fragment mặc định nếu không có Intent
            if (getIntent().getStringExtra("openFragment") == null) {
                bottomNav.setSelectedItemId(R.id.bottomItemHome);
            }
        } else {
            Log.e(TAG, "Bottom navigation not found");
            Toast.makeText(this, "Bottom navigation not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSearch() {
        ImageView searchIcon = findViewById(R.id.Search_bar_icon);
        if (searchIcon != null) {
            searchIcon.setOnClickListener(v -> {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    // Gửi searchQuery đến FragmentShop và chuyển sang tab Shop
                    fragmentShop.updateSearchQuery(query);
                    replaceFragment(fragmentShop);
                    bottomNav.setSelectedItemId(R.id.bottomItemShop);
                } else {
                    Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (searchEditText != null) {
            // Xử lý sự kiện nhấn Enter
            searchEditText.setOnEditorActionListener((v, actionId, event) -> {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    String query = searchEditText.getText().toString().trim();
                    if (!query.isEmpty()) {
                        // Gửi searchQuery đến FragmentShop và chuyển sang tab Shop
                        fragmentShop.updateSearchQuery(query);
                        replaceFragment(fragmentShop);
                        bottomNav.setSelectedItemId(R.id.bottomItemShop);
                        return true;
                    } else {
                        Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            });
        }
    }

    private void setupFilter() {
        if (filterIcon != null) {
            filterIcon.setOnClickListener(v -> showFilterDialog());
        }
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null);
        dialog.setContentView(view);

        Spinner flowerTypeSpinner = view.findViewById(R.id.spinner_flower_type);
        RangeSlider priceRangeSlider = view.findViewById(R.id.price_range_slider);
        TextView priceRangeText = view.findViewById(R.id.price_range_text);
        Button applyFilterBtn = view.findViewById(R.id.apply_filter_btn);
        Button cancelFilterBtn = view.findViewById(R.id.cancel_filter_btn);

        // Đảm bảo RangeSlider có giá trị mặc định
        if (priceRangeSlider != null) {
            priceRangeSlider.setValues(0f, 1000000f); // Giá trị mặc định
            if (priceRangeText != null) {
                priceRangeText.setText("Giá: 0 - 1,000,000");
            }

            // Cập nhật TextView khi giá trị RangeSlider thay đổi
            priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
                List<Float> values = slider.getValues();
                if (values != null && values.size() == 2) {
                    priceRangeText.setText("Giá: " + values.get(0).intValue() + " - " + values.get(1).intValue());
                }
            });
        }

        if (applyFilterBtn != null) {
            applyFilterBtn.setOnClickListener(v -> {
                String flowerType = flowerTypeSpinner != null && flowerTypeSpinner.getSelectedItem() != null ?
                        flowerTypeSpinner.getSelectedItem().toString() : "Tất cả";

                List<Float> priceRange;
                if (priceRangeSlider != null) {
                    priceRange = priceRangeSlider.getValues();
                    if (priceRange == null || priceRange.size() < 2) {
                        priceRange = new ArrayList<>();
                        priceRange.add(0f);
                        priceRange.add(1000000f);
                    }
                } else {
                    priceRange = new ArrayList<>();
                    priceRange.add(0f);
                    priceRange.add(1000000f);
                }

                // Tạo mới FragmentShop để làm mới dữ liệu
                fragmentShop = new FragmentShop();
                fragmentMap.put(R.id.bottomItemShop, fragmentShop);

                // Truyền dữ liệu bộ lọc vào FragmentShop
                Bundle args = new Bundle();
                args.putString("filter_type", flowerType);
                args.putFloat("filter_price_min", priceRange.get(0));
                args.putFloat("filter_price_max", priceRange.get(1));
                fragmentShop.setArguments(args);

                // Đặt lại searchEditText
                searchEditText.setText("");

                // Chuyển sang FragmentShop
                replaceFragment(fragmentShop);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.bottomItemShop);
                }

                Toast.makeText(this, "Đã áp dụng bộ lọc: " + flowerType + ", Giá từ " + priceRange.get(0).intValue() + " đến " + priceRange.get(1).intValue(), Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            });
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy nút áp dụng bộ lọc", Toast.LENGTH_SHORT).show();
        }

        if (cancelFilterBtn != null) {
            cancelFilterBtn.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
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
            Log.e(TAG, "Lỗi khi thay thế fragment: " + e.getMessage());
            Toast.makeText(this, "Lỗi khi tải fragment", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            String openFragment = intent.getStringExtra("openFragment");
            if ("shop".equals(openFragment)) {
                fragmentShop = new FragmentShop();
                fragmentMap.put(R.id.bottomItemShop, fragmentShop);

                Bundle args = new Bundle();
                String searchQuery = intent.getStringExtra("search_query");
                String filterType = intent.getStringExtra("filter_type");
                float filterPriceMin = intent.getFloatExtra("filter_price_min", 0f);
                float filterPriceMax = intent.getFloatExtra("filter_price_max", Float.MAX_VALUE);
                if (searchQuery != null) args.putString("search_query", searchQuery);
                if (filterType != null) args.putString("filter_type", filterType);
                args.putFloat("filter_price_min", filterPriceMin);
                args.putFloat("filter_price_max", filterPriceMax);
                fragmentShop.setArguments(args);

                replaceFragment(fragmentShop);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.bottomItemShop);
                }
            } else if ("account".equals(openFragment)) {
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.bottomItemAccount);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
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
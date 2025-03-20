package com.example.flowerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TimKiem extends AppCompatActivity {
    private EditText searchEditText;
    private ImageView backButton, searchIcon, filterIcon;
    private RecyclerView searchHistoryRecyclerView;
    private SearchHistoryAdapter historyAdapter;
    private List<String> searchHistoryList;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tim_kiem);

        initViews();
        setupWindowInsets();
        setupSearchHistory();
        setupListeners();

        // Kiểm tra Intent để lấy từ khóa tìm kiếm ban đầu
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("search_query")) {
            String initialQuery = intent.getStringExtra("search_query");
            searchEditText.setText(initialQuery);
            performSearch();
        }
    }

    private void initViews() {
        searchEditText = findViewById(R.id.EditText_Searching_Bar);
        backButton = findViewById(R.id.back_account_page_btn);
        searchIcon = findViewById(R.id.Search_bar_icon);
        filterIcon = findViewById(R.id.filter_icon);
        searchHistoryRecyclerView = findViewById(R.id.search_history);
        sharedPreferences = getSharedPreferences("SearchPrefs", MODE_PRIVATE);

        if (searchEditText == null || backButton == null || searchIcon == null || filterIcon == null || searchHistoryRecyclerView == null) {
            Toast.makeText(this, "Lỗi: Thiếu thành phần giao diện", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        searchHistoryList = new ArrayList<>(sharedPreferences.getStringSet("history", new HashSet<>()));
        historyAdapter = new SearchHistoryAdapter(searchHistoryList, this::searchFromHistory, this::deleteHistoryItem);
        searchHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchHistoryRecyclerView.setAdapter(historyAdapter);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupSearchHistory() {
        // Hiển thị lịch sử tìm kiếm khi người dùng tập trung vào thanh tìm kiếm
        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !searchHistoryList.isEmpty() && searchEditText.getText().toString().trim().isEmpty()) {
                searchHistoryRecyclerView.setVisibility(View.VISIBLE);
            } else {
                searchHistoryRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> navigateBack());

        searchIcon.setOnClickListener(v -> performSearch());

        filterIcon.setOnClickListener(v -> showFilterDialog());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    searchHistoryRecyclerView.setVisibility(View.GONE);
                } else if (searchEditText.hasFocus() && !searchHistoryList.isEmpty()) {
                    searchHistoryRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.clear_all).setOnClickListener(v -> clearSearchHistory());
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            saveSearchHistory(query);
            // Chuyển từ khóa tìm kiếm sang MainActivity để hiển thị trong FragmentShop
            Intent intent = new Intent(TimKiem.this, MainActivity.class);
            intent.putExtra("openFragment", "shop");
            intent.putExtra("search_query", query);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Vui lòng nhập từ khóa tìm kiếm", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSearchHistory(String query) {
        new Thread(() -> {
            searchHistoryList.remove(query); // Xóa nếu đã tồn tại để tránh trùng lặp
            searchHistoryList.add(0, query); // Thêm vào đầu danh sách
            if (searchHistoryList.size() > 10) searchHistoryList.remove(searchHistoryList.size() - 1); // Giới hạn 10 mục

            // Cập nhật SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("history", new HashSet<>(searchHistoryList));
            editor.apply();

            // Cập nhật UI trên main thread
            runOnUiThread(() -> historyAdapter.notifyDataSetChanged());
        }).start();
    }

    private void clearSearchHistory() {
        new Thread(() -> {
            searchHistoryList.clear();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("history").apply();

            // Cập nhật UI trên main thread
            runOnUiThread(() -> {
                historyAdapter.notifyDataSetChanged();
                searchHistoryRecyclerView.setVisibility(View.GONE);
            });
        }).start();
    }

    private void searchFromHistory(String query) {
        searchEditText.setText(query);
        performSearch();
    }

    private void deleteHistoryItem(String query) {
        new Thread(() -> {
            int position = searchHistoryList.indexOf(query);
            if (position != -1) {
                searchHistoryList.remove(position);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putStringSet("history", new HashSet<>(searchHistoryList));
                editor.apply();

                // Cập nhật UI trên main thread
                runOnUiThread(() -> {
                    historyAdapter.notifyItemRemoved(position);
                    historyAdapter.notifyItemRangeChanged(position, searchHistoryList.size());
                    if (searchHistoryList.isEmpty()) {
                        searchHistoryRecyclerView.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
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
            // Cập nhật TextView với giá trị ban đầu
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

                // Kiểm tra giá trị của RangeSlider
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

                // Chuyển bộ lọc sang MainActivity để áp dụng trong FragmentShop
                Intent intent = new Intent(TimKiem.this, MainActivity.class);
                intent.putExtra("openFragment", "shop");
                intent.putExtra("filter_type", flowerType);
                intent.putExtra("filter_price_min", priceRange.get(0));
                intent.putExtra("filter_price_max", priceRange.get(1));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();

                // Thông báo áp dụng bộ lọc thành công
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

    private void navigateBack() {
        Intent intent = new Intent(TimKiem.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private static class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
        private final List<String> historyList;
        private final OnItemClickListener listener;
        private final OnDeleteClickListener deleteListener;

        interface OnItemClickListener {
            void onItemClick(String query);
        }

        interface OnDeleteClickListener {
            void onDeleteClick(String query);
        }

        SearchHistoryAdapter(List<String> historyList, OnItemClickListener listener, OnDeleteClickListener deleteListener) {
            this.historyList = historyList;
            this.listener = listener;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String query = historyList.get(position);
            holder.textView.setText(query);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(query));
            holder.deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(query));
        }

        @Override
        public int getItemCount() {
            return historyList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ImageView deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.search_history_item);
                deleteButton = itemView.findViewById(R.id.delete_history_item);
            }
        }
    }
}
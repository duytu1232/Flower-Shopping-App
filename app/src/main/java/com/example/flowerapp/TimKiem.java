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
    }

    private void initViews() {
        searchEditText = findViewById(R.id.EditText_Searching_Bar);
        backButton = findViewById(R.id.back_account_page_btn);
        searchIcon = findViewById(R.id.Search_bar_icon);
        filterIcon = findViewById(R.id.filter_icon);
        searchHistoryRecyclerView = findViewById(R.id.search_history);
        sharedPreferences = getSharedPreferences("SearchPrefs", MODE_PRIVATE);

        searchHistoryList = new ArrayList<>(sharedPreferences.getStringSet("history", new HashSet<>()));
        historyAdapter = new SearchHistoryAdapter(searchHistoryList, this::searchFromHistory);
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
        // Không cần thêm gì vì đã xử lý trong initViews()
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> navigateBack());

        searchIcon.setOnClickListener(v -> performSearch());

        filterIcon.setOnClickListener(v -> showFilterDialog());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Có thể thêm gợi ý tìm kiếm ở đây nếu cần
            }
        });

        findViewById(R.id.clear_all).setOnClickListener(v -> clearSearchHistory());
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            saveSearchHistory(query);
            Toast.makeText(this, "Searching: " + query, Toast.LENGTH_SHORT).show();
            // Thêm logic tìm kiếm sản phẩm thực tế ở đây (ví dụ: gọi API hoặc lọc danh sách)
        }
    }

    private void saveSearchHistory(String query) {
        searchHistoryList.add(0, query); // Thêm vào đầu danh sách
        if (searchHistoryList.size() > 10) searchHistoryList.remove(searchHistoryList.size() - 1); // Giới hạn 10 mục
        historyAdapter.notifyDataSetChanged(); // Sử dụng lại notifyDataSetChanged cho đơn giản
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("history", new HashSet<>(searchHistoryList));
        editor.apply();
    }

    private void clearSearchHistory() {
        searchHistoryList.clear();
        historyAdapter.notifyDataSetChanged(); // Sử dụng lại notifyDataSetChanged cho đơn giản
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("history").apply();
    }

    private void searchFromHistory(String query) {
        searchEditText.setText(query);
        performSearch();
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_filter, findViewById(R.id.main));
        dialog.setContentView(view);

        Spinner flowerTypeSpinner = view.findViewById(R.id.spinner_flower_type);
        RangeSlider priceRangeSlider = view.findViewById(R.id.price_range_slider);
        Button applyFilterBtn = view.findViewById(R.id.apply_filter_btn);

        if (applyFilterBtn != null) {
            applyFilterBtn.setOnClickListener(v -> {
                String flowerType = flowerTypeSpinner != null ? (flowerTypeSpinner.getSelectedItem() != null ? flowerTypeSpinner.getSelectedItem().toString() : "Tất cả") : "Tất cả";
                List<Float> priceRange = priceRangeSlider != null ? priceRangeSlider.getValues() : new ArrayList<>(List.of(0f, 1000000f));
                Toast.makeText(this, "Filter: Type=" + flowerType + ", Price=" + priceRange.get(0) + "-" + priceRange.get(1), Toast.LENGTH_SHORT).show();
                // Áp dụng bộ lọc vào danh sách sản phẩm ở đây
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    private void navigateBack() {
        Intent intent = new Intent(TimKiem.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // Adapter đơn giản hóa không dùng DiffUtil/ListAdapter
    private static class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.ViewHolder> {
        private final List<String> historyList;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(String query);
        }

        SearchHistoryAdapter(List<String> historyList, OnItemClickListener listener) {
            this.historyList = historyList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String query = historyList.get(position);
            holder.textView.setText(query);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(query));
        }

        @Override
        public int getItemCount() {
            return historyList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
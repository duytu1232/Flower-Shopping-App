package com.example.flowerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

        if (searchEditText == null || backButton == null || searchIcon == null || filterIcon == null || searchHistoryRecyclerView == null) {
            Toast.makeText(this, "Error: Missing UI components", Toast.LENGTH_SHORT).show();
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
        // Đã xử lý trong initViews
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
                // Có thể thêm gợi ý tìm kiếm real-time nếu cần
            }
        });

        findViewById(R.id.clear_all).setOnClickListener(v -> clearSearchHistory());
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            saveSearchHistory(query);
            Intent intent = new Intent(TimKiem.this, MainActivity.class);
            intent.putExtra("openFragment", "shop");
            intent.putExtra("search_query", query);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSearchHistory(String query) {
        searchHistoryList.remove(query); // Xóa nếu đã tồn tại để tránh trùng lặp
        searchHistoryList.add(0, query); // Thêm vào đầu danh sách
        if (searchHistoryList.size() > 10) searchHistoryList.remove(searchHistoryList.size() - 1); // Giới hạn 10 mục
        historyAdapter.notifyDataSetChanged();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("history", new HashSet<>(searchHistoryList));
        editor.apply();
    }

    private void clearSearchHistory() {
        searchHistoryList.clear();
        historyAdapter.notifyDataSetChanged();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("history").apply();
    }

    private void searchFromHistory(String query) {
        searchEditText.setText(query);
        performSearch();
    }

    private void deleteHistoryItem(String query) {
        int position = searchHistoryList.indexOf(query);
        if (position != -1) {
            searchHistoryList.remove(position);
            historyAdapter.notifyItemRemoved(position);
            historyAdapter.notifyItemRangeChanged(position, searchHistoryList.size());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("history", new HashSet<>(searchHistoryList));
            editor.apply();
        }
    }

    private void showFilterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null);

        // Kiểm tra và xóa parent nếu có
        if (dialogView.getParent() != null) {
            ((ViewGroup) dialogView.getParent()).removeView(dialogView);
        }
        dialog.setContentView(dialogView);

        Spinner flowerTypeSpinner = dialogView.findViewById(R.id.spinner_flower_type);
        RangeSlider priceRangeSlider = dialogView.findViewById(R.id.price_range_slider);
        Button applyFilterBtn = dialogView.findViewById(R.id.apply_filter_btn);

        // Kiểm tra các thành phần giao diện
        if (flowerTypeSpinner == null || priceRangeSlider == null || applyFilterBtn == null) {
            Toast.makeText(this, "Error: Missing filter dialog components", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        applyFilterBtn.setOnClickListener(v -> {
            // Lấy loại hoa từ Spinner
            String flowerType = flowerTypeSpinner.getSelectedItem() != null ?
                    flowerTypeSpinner.getSelectedItem().toString() : "Tất cả";

            // Lấy khoảng giá từ RangeSlider
            List<Float> priceRange = priceRangeSlider.getValues();
            float minPrice, maxPrice;
            if (priceRange.size() >= 2) {
                minPrice = priceRange.get(0);
                maxPrice = priceRange.get(1);
                // Đảm bảo minPrice nhỏ hơn maxPrice
                if (minPrice > maxPrice) {
                    float temp = minPrice;
                    minPrice = maxPrice;
                    maxPrice = temp;
                }
            } else {
                // Sử dụng giá trị mặc định nếu không đủ 2 giá trị
                minPrice = 0f;
                maxPrice = 1000000f;
                Log.w("TimKiem", "Price range not set correctly, using default values: min=" + minPrice + ", max=" + maxPrice);
            }

            Intent intent = new Intent(TimKiem.this, MainActivity.class);
            intent.putExtra("openFragment", "shop");
            intent.putExtra("filter_type", flowerType);
            intent.putExtra("filter_price_min", minPrice);
            intent.putExtra("filter_price_max", maxPrice);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            dialog.dismiss();
        });

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
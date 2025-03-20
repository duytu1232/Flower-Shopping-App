package com.example.flowerapp.User.Fragments;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.ProductAdapter;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.google.android.material.slider.RangeSlider;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FragmentShop extends Fragment {
    private static final String TAG = "FragmentShop";

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private TextView emptyMessage;
    private DatabaseHelper dbHelper;
    private String searchQuery;
    private String filterType;
    private float filterPriceMin;
    private float filterPriceMax;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerView = view.findViewById(R.id.recycler_shop_products);
        emptyMessage = view.findViewById(R.id.empty_message);

        if (recyclerView == null || emptyMessage == null) {
            Log.e(TAG, "RecyclerView or empty message not found in layout");
            return view;
        }

        // Sử dụng GridLayoutManager với 2 cột
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        productList = new ArrayList<>();

        adapter = new ProductAdapter(productList, requireContext());
        recyclerView.setAdapter(adapter);

        // Khởi tạo giá trị mặc định
        searchQuery = "";
        filterType = "Tất cả";
        filterPriceMin = 0f;
        filterPriceMax = 5000000f;

        // Lấy dữ liệu từ Bundle
        Bundle args = getArguments();
        if (args != null) {
            searchQuery = args.getString("search_query", "");
            filterType = args.getString("filter_type", "Tất cả");
            filterPriceMin = args.getFloat("filter_price_min", 0f);
            filterPriceMax = args.getFloat("filter_price_max", 5000000f);
        }

        // Tải sản phẩm ban đầu
        new LoadProductsTask().execute();

        // Xử lý nút Sort để mở dialog bộ lọc
        ImageView sortIcon = view.findViewById(R.id.sort_icon);
        if (sortIcon != null) {
            sortIcon.setOnClickListener(v -> showFilterDialog());
        }

        return view;
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        Spinner flowerTypeSpinner = dialogView.findViewById(R.id.spinner_flower_type);
        RangeSlider priceSlider = dialogView.findViewById(R.id.price_range_slider);
        TextView priceRangeText = dialogView.findViewById(R.id.price_range_text);

        // Cập nhật TextView khi RangeSlider thay đổi
        DecimalFormat formatter = new DecimalFormat("#,###");
        priceSlider.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            float min = values.get(0);
            float max = values.get(1);
            priceRangeText.setText("Giá: " + formatter.format(min) + " - " + formatter.format(max));
        });

        // Đặt giá trị ban đầu cho RangeSlider
        priceSlider.setValues(filterPriceMin, filterPriceMax);
        priceRangeText.setText("Giá: " + formatter.format(filterPriceMin) + " - " + formatter.format(filterPriceMax));

        // Xử lý nút Apply
        dialogView.findViewById(R.id.apply_filter_btn).setOnClickListener(v -> {
            filterType = flowerTypeSpinner.getSelectedItem().toString();
            List<Float> values = priceSlider.getValues();
            filterPriceMin = values.get(0);
            filterPriceMax = values.get(1);
            // Đặt lại searchQuery để ưu tiên bộ lọc
            searchQuery = "";
            new LoadProductsTask().execute();
        });

        // Xử lý nút Cancel
        dialogView.findViewById(R.id.cancel_filter_btn).setOnClickListener(v -> {
            // Đóng dialog mà không áp dụng bộ lọc
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class LoadProductsTask extends AsyncTask<Void, Void, List<Product>> {
        @Override
        protected List<Product> doInBackground(Void... voids) {
            List<Product> products = new ArrayList<>();
            SQLiteDatabase db = null;
            Cursor cursor = null;
            try {
                db = dbHelper.openDatabase();
                String query = "SELECT product_id, name, description, price, stock, image_url, category FROM Products WHERE 1=1";
                List<String> selectionArgs = new ArrayList<>();

                // Ưu tiên tìm kiếm nếu có searchQuery
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    query += " AND name LIKE ?";
                    selectionArgs.add("%" + searchQuery + "%");
                } else {
                    // Nếu không có searchQuery, áp dụng bộ lọc loại hoa
                    if (filterType != null && !filterType.equals("Tất cả")) {
                        query += " AND name LIKE ?";
                        selectionArgs.add("%" + filterType + "%");
                    }
                }

                // Thêm điều kiện lọc theo giá
                if (filterPriceMin > 0 || filterPriceMax < Float.MAX_VALUE) {
                    query += " AND price BETWEEN ? AND ?";
                    selectionArgs.add(String.valueOf(filterPriceMin));
                    selectionArgs.add(String.valueOf(filterPriceMax));
                }

                cursor = db.rawQuery(query, selectionArgs.toArray(new String[0]));
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                    int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                    String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    products.add(new Product(id, name, description, price, stock, imageUrl, category));
                    Log.d(TAG, "Thêm sản phẩm: " + name);
                }
                Log.d(TAG, "Số lượng sản phẩm trong list: " + products.size());
            } catch (Exception e) {
                Log.e(TAG, "Error loading products: " + e.getMessage(), e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
                if (db != null) {
                    dbHelper.closeDatabase(db);
                }
            }
            return products;
        }

        @Override
        protected void onPostExecute(List<Product> products) {
            productList.clear();
            productList.addAll(products);
            adapter.notifyDataSetChanged();
            updateEmptyState();
        }
    }

    private void updateEmptyState() {
        if (productList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyMessage.setVisibility(View.VISIBLE);
            emptyMessage.setText("No products available");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
        }
    }

    public void refreshProducts() {
        new LoadProductsTask().execute();
    }

    public void updateSearchQuery(String newQuery) {
        this.searchQuery = newQuery;
        // Đặt lại filterType để ưu tiên tìm kiếm
        this.filterType = "Tất cả";
        new LoadProductsTask().execute();
    }
}
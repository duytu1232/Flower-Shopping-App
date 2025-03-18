package com.example.flowerapp.User.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;

public class FragmentShop extends Fragment {
    private static final String TAG = "FragmentShop";

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private TextView emptyMessage;
    private DatabaseHelper dbHelper;

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

        loadProducts();
        updateEmptyState();

        // Xử lý nút Sort (nếu cần)
        ImageView sortIcon = view.findViewById(R.id.sort_icon);
        if (sortIcon != null) {
            sortIcon.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Sort functionality coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        return view;
    }

    private void loadProducts() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.openDatabase();
            String query = "SELECT product_id, name, description, price, stock, image_url, category FROM Products";
            List<String> selectionArgs = new ArrayList<>();
            String searchQuery = null;
            String flowerType = null;
            float priceMin = 0f;
            float priceMax = Float.MAX_VALUE;

            if (getArguments() != null) {
                searchQuery = getArguments().getString("search_query");
                flowerType = getArguments().getString("filter_type");
                priceMin = getArguments().getFloat("filter_price_min", 0f);
                priceMax = getArguments().getFloat("filter_price_max", Float.MAX_VALUE);

                List<String> conditions = new ArrayList<>();
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    conditions.add("name LIKE ?");
                    selectionArgs.add("%" + searchQuery + "%");
                }
                if (flowerType != null && !flowerType.equals("Tất cả")) {
                    conditions.add("category = ?");
                    selectionArgs.add(flowerType);
                }
                if (priceMin > 0 || priceMax < Float.MAX_VALUE) {
                    conditions.add("price BETWEEN ? AND ?");
                    selectionArgs.add(String.valueOf(priceMin));
                    selectionArgs.add(String.valueOf(priceMax));
                }

                if (!conditions.isEmpty()) {
                    query = "SELECT product_id, name, description, price, stock, image_url, category FROM Products WHERE " + String.join(" AND ", conditions);
                }
            }

            cursor = db.rawQuery(query, selectionArgs.toArray(new String[0]));
            productList.clear();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                productList.add(new Product(id, name, description, price, stock, imageUrl, category));
                Log.d(TAG, "Thêm sản phẩm: " + name);
            }
            Log.d(TAG, "Số lượng sản phẩm trong list: " + productList.size());
        } catch (Exception e) {
            Log.e(TAG, "Error loading products: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                dbHelper.closeDatabase(db);
            }
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
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
        productList.clear();
        loadProducts();
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }
}
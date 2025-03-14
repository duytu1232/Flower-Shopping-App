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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        recyclerView = view.findViewById(R.id.recycler_shop_products);
        emptyMessage = view.findViewById(R.id.empty_message);

        if (recyclerView == null || emptyMessage == null) {
            Log.e(TAG, "RecyclerView or empty message not found in layout");
            return view;
        }

        // Sử dụng GridLayoutManager với 2 cột
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        productList = new ArrayList<>();

        loadProducts();

        adapter = new ProductAdapter(productList, requireContext());
        recyclerView.setAdapter(adapter);

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
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            String query = "SELECT * FROM Products";
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
                    query = "SELECT * FROM Products WHERE " + String.join(" AND ", conditions);
                }
            }

            Cursor cursor = db.rawQuery(query, selectionArgs.toArray(new String[0]));
            if (cursor != null) {
                int idIndex = cursor.getColumnIndex("product_id");
                int nameIndex = cursor.getColumnIndex("name");
                int descriptionIndex = cursor.getColumnIndex("description");
                int priceIndex = cursor.getColumnIndex("price");
                int stockIndex = cursor.getColumnIndex("stock");
                int imageUrlIndex = cursor.getColumnIndex("image_url");
                int categoryIndex = cursor.getColumnIndex("category");

                if (idIndex == -1 || nameIndex == -1 || descriptionIndex == -1 || priceIndex == -1 ||
                        stockIndex == -1 || imageUrlIndex == -1 || categoryIndex == -1) {
                    Log.e(TAG, "One or more columns do not exist in Products table!");
                    return;
                }

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    String description = cursor.getString(descriptionIndex);
                    double price = cursor.getDouble(priceIndex);
                    int stock = cursor.getInt(stockIndex);
                    String imageUrl = cursor.getString(imageUrlIndex);
                    String category = cursor.getString(categoryIndex);
                    productList.add(new Product(id, name, description, price, stock, imageUrl, category));
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading products: " + e.getMessage());
        } finally {
            if (db != null) {
                dbHelper.closeDatabase(db);
            }
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
}
package com.example.flowerapp.User.Fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
    private List<Product> productList = new ArrayList<>();
    private TextView emptyMessage;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        recyclerView = view.findViewById(R.id.recycler_shop_products);
        emptyMessage = view.findViewById(R.id.empty_message);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(requireContext());

        // Khởi tạo adapter
        adapter = new ProductAdapter(productList, requireContext(), product -> {
            Intent intent = new Intent(requireContext(), ProductDetail.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Tải danh sách sản phẩm ban đầu
        loadProducts();

        // Cập nhật trạng thái rỗng
        updateEmptyState();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Làm mới danh sách sản phẩm mỗi khi fragment được hiển thị
        loadProducts();
        updateEmptyState();
    }

    public void loadProducts() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            Bundle args = getArguments();
            String query = "SELECT * FROM Products";
            List<String> selectionArgs = new ArrayList<>();

            if (args != null) {
                String searchQuery = args.getString("search_query");
                String filterType = args.getString("filter_type");
                float priceMin = args.getFloat("filter_price_min", 0f);
                float priceMax = args.getFloat("filter_price_max", Float.MAX_VALUE);

                List<String> whereClauses = new ArrayList<>();
                if (searchQuery != null && !searchQuery.isEmpty()) {
                    whereClauses.add("name LIKE ?");
                    selectionArgs.add("%" + searchQuery + "%");
                }
                if (filterType != null && !filterType.equals("Tất cả")) {
                    whereClauses.add("category = ?");
                    selectionArgs.add(filterType);
                }
                whereClauses.add("price BETWEEN ? AND ?");
                selectionArgs.add(String.valueOf(priceMin));
                selectionArgs.add(String.valueOf(priceMax));

                if (!whereClauses.isEmpty()) {
                    query += " WHERE " + String.join(" AND ", whereClauses);
                }
            }

            Cursor cursor = db.rawQuery(query, selectionArgs.toArray(new String[0]));
            productList.clear();
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                    int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                    String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    productList.add(new Product(id, name, description, price, stock, imageUrl, category));
                } while (cursor.moveToNext());
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error loading products: " + e.getMessage());
            Toast.makeText(requireContext(), "Error loading products", Toast.LENGTH_SHORT).show();
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
            // Kiểm tra xem có bộ lọc nào được áp dụng không
            Bundle args = getArguments();
            if (args != null && (args.containsKey("search_query") || args.containsKey("filter_type") || args.containsKey("filter_price_min"))) {
                emptyMessage.setText("Không tìm thấy sản phẩm phù hợp");
            } else {
                emptyMessage.setText("No products available");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
        }
    }
}
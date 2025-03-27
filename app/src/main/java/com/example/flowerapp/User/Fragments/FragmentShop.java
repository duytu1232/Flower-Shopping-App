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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);

        recyclerView = view.findViewById(R.id.recycler_shop_products);
        emptyMessage = view.findViewById(R.id.empty_message);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // Tải danh sách sản phẩm
        loadProducts();

        // Truyền OnProductClickListener vào ProductAdapter
        adapter = new ProductAdapter(productList, requireContext(), product -> {
            Intent intent = new Intent(requireContext(), ProductDetail.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Cập nhật trạng thái rỗng
        updateEmptyState();

        return view;
    }

    private void loadProducts() {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM Products", null);
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
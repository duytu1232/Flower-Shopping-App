package com.example.flowerapp.User.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.FavoriteAdapter;
import com.example.flowerapp.MainActivity;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {
    private static final String TAG = "FragmentFavorite";

    private RecyclerView recyclerView;
    private FavoriteAdapter adapter;
    private List<Product> favoriteList;
    private TextView emptyMessage;
    private Button exploreButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        recyclerView = view.findViewById(R.id.recycler_favorite_list);
        emptyMessage = view.findViewById(R.id.empty_message);
        exploreButton = view.findViewById(R.id.explore_button);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        favoriteList = new ArrayList<>();

        // Lấy dữ liệu từ cơ sở dữ liệu
        loadFavorites();

        // Khởi tạo adapter với context và listener
        adapter = new FavoriteAdapter(favoriteList, requireContext(), this::updateEmptyState);
        recyclerView.setAdapter(adapter);

        // Cập nhật trạng thái giao diện
        updateEmptyState();

        // Nút "Khám phá thêm"
        if (exploreButton != null) {
            exploreButton.setOnClickListener(v -> {
                FragmentShop fragmentShop = new FragmentShop();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragmentShop)
                        .addToBackStack(null)
                        .commit();
                // Cập nhật BottomNavigationView
                if (requireActivity() instanceof MainActivity) {
                    BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavMain);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.bottomItemShop);
                    }
                }
            });
        }

        return view;
    }

    private void loadFavorites() {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.openDatabase();

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = db.rawQuery(
                "SELECT p.* FROM Products p " +
                        "INNER JOIN Favorites f ON p.product_id = f.product_id " +
                        "WHERE f.user_id = ?", new String[]{String.valueOf(userId)});

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
                Log.e(TAG, "Một hoặc nhiều cột không tồn tại trong kết quả truy vấn!");
            } else {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    String description = cursor.getString(descriptionIndex);
                    double price = cursor.getDouble(priceIndex);
                    int stock = cursor.getInt(stockIndex);
                    String imageUrl = cursor.getString(imageUrlIndex);
                    String category = cursor.getString(categoryIndex);
                    favoriteList.add(new Product(id, name, description, price, stock, imageUrl, category));
                }
            }
            cursor.close();
        }
        dbHelper.closeDatabase(db);
    }

    public void updateEmptyState() {
        if (favoriteList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            if (emptyMessage != null) {
                emptyMessage.setVisibility(View.VISIBLE);
                emptyMessage.setText("No favorite products available");
            }
            if (exploreButton != null) {
                exploreButton.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            if (emptyMessage != null) {
                emptyMessage.setVisibility(View.GONE);
            }
            if (exploreButton != null) {
                exploreButton.setVisibility(View.GONE);
            }
        }
    }
}
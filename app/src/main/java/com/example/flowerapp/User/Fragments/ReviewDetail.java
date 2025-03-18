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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.ReviewAdapter;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.Models.Review;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.example.flowerapp.User.Fragments.ReviewActivity;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReviewDetail extends Fragment {
    private static final String TAG = "ReviewDetailFragment";
    private DatabaseHelper dbHelper;
    private RecyclerView reviewRecyclerView;
    private TextView productNameText, averageRatingText;
    private Button writeReviewButton, sortButton;
    private Spinner filterSpinner;
    private int productId;
    private boolean isSortNewest = true; // Mặc định sắp xếp theo mới nhất

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_review_detail, container, false);

        dbHelper = new DatabaseHelper(getContext());
        reviewRecyclerView = view.findViewById(R.id.review_recycler_view);
        productNameText = view.findViewById(R.id.product_name);
        averageRatingText = view.findViewById(R.id.average_rating_text);
        writeReviewButton = view.findViewById(R.id.write_review_button);
        filterSpinner = view.findViewById(R.id.filter_spinner);
        sortButton = view.findViewById(R.id.sort_button);

        Bundle args = getArguments();
        if (args != null) {
            productId = args.getInt("product_id", -1);
            if (productId == -1) {
                Toast.makeText(getContext(), "Product ID not found", Toast.LENGTH_SHORT).show();
                return view;
            }
            loadProductName();
            loadReviews();
        }

        // Xử lý Spinner lọc đánh giá
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadReviews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Xử lý nút sắp xếp
        sortButton.setOnClickListener(v -> {
            isSortNewest = !isSortNewest;
            sortButton.setText("Sort: " + (isSortNewest ? "Newest" : "Oldest"));
            loadReviews();
        });

        // Xử lý nút "Write a Review"
        writeReviewButton.setOnClickListener(v -> {
            int orderId = getOrderIdForProduct();
            if (orderId != -1) {
                SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);
                if (userId == -1) {
                    Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                    return;
                }

                String orderDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                Order order = new Order(
                        orderId,
                        userId,
                        orderDate,
                        "Pending",
                        0.0,
                        "Default Address",
                        "Order #" + orderId,
                        ""
                );
                Intent intent = new Intent(getContext(), ReviewActivity.class);
                intent.putExtra("order", order);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "You need to purchase this product to write a review", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadProductName() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery("SELECT name FROM Products WHERE product_id = ?", new String[]{String.valueOf(productId)});
            if (cursor.moveToFirst()) {
                productNameText.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error loading product name: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading product name", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private void loadReviews() {
        SQLiteDatabase db = null;
        List<Review> reviews = new ArrayList<>();
        float averageRating = 0.0f;
        int totalReviews = 0;
        try {
            db = dbHelper.openDatabase();
            String filter = filterSpinner.getSelectedItem().toString();
            String orderBy = isSortNewest ? "review_date DESC" : "review_date ASC";
            String whereClause = "r.product_id = ?";

            if (!filter.equals("All")) {
                int rating = Integer.parseInt(filter.split(" ")[0]);
                whereClause += " AND r.rating = ?";
            }

            String query = "SELECT r.rating, r.comment, r.review_date, u.username " +
                    "FROM Reviews r " +
                    "JOIN Users u ON r.user_id = u.user_id " +
                    "WHERE " + whereClause +
                    " ORDER BY " + orderBy;

            Cursor cursor;
            if (filter.equals("All")) {
                cursor = db.rawQuery(query, new String[]{String.valueOf(productId)});
            } else {
                int rating = Integer.parseInt(filter.split(" ")[0]);
                cursor = db.rawQuery(query, new String[]{String.valueOf(productId), String.valueOf(rating)});
            }

            if (cursor.moveToFirst()) {
                do {
                    float rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"));
                    String comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
                    String reviewDate = cursor.getString(cursor.getColumnIndexOrThrow("review_date"));
                    String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                    reviews.add(new Review(rating, comment, reviewDate, username));
                } while (cursor.moveToNext());
            }
            cursor.close();

            // Tính trung bình và số lượng đánh giá
            Cursor countCursor = db.rawQuery(
                    "SELECT AVG(rating), COUNT(*) FROM Reviews WHERE product_id = ?",
                    new String[]{String.valueOf(productId)});
            if (countCursor.moveToFirst()) {
                averageRating = countCursor.getFloat(0);
                totalReviews = countCursor.getInt(1);
            }
            countCursor.close();

            averageRatingText.setText(String.format("Average Rating: %.1f (%d reviews)", averageRating, totalReviews));

            ReviewAdapter adapter = new ReviewAdapter(reviews, getContext());
            reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            reviewRecyclerView.setAdapter(adapter);

            if (reviews.isEmpty()) {
                Toast.makeText(getContext(), "No reviews available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading reviews: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading reviews", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private int getOrderIdForProduct() {
        SQLiteDatabase db = null;
        int orderId = -1;
        try {
            db = dbHelper.openDatabase();
            SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                return -1;
            }

            // Kiểm tra xem người dùng đã mua sản phẩm này chưa
            Cursor orderCursor = db.rawQuery(
                    "SELECT o.order_id " +
                            "FROM Orders o " +
                            "JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "WHERE o.user_id = ? AND oi.product_id = ? LIMIT 1",
                    new String[]{String.valueOf(userId), String.valueOf(productId)});
            if (orderCursor.moveToFirst()) {
                orderId = orderCursor.getInt(orderCursor.getColumnIndexOrThrow("order_id"));
            }
            orderCursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting order ID: " + e.getMessage());
            Toast.makeText(getContext(), "Error checking purchase", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
        return orderId;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReviews();
    }
}
package com.example.flowerapp.User.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.ReviewAdapter;
import com.example.flowerapp.MainActivity;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.Models.Review;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReviewDetail extends AppCompatActivity {
    private static final String TAG = "ReviewDetailActivity";
    private DatabaseHelper dbHelper;
    private RecyclerView reviewRecyclerView;
    private TextView productNameText, averageRatingText;
    private Button writeReviewButton, sortButton;
    private Spinner filterSpinner;
    private ImageButton backButton;
    private int productId;
    private boolean isSortNewest = true; // Mặc định sắp xếp theo mới nhất

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_detail);

        dbHelper = new DatabaseHelper(this);
        reviewRecyclerView = findViewById(R.id.review_recycler_view);
        productNameText = findViewById(R.id.product_name);
        averageRatingText = findViewById(R.id.average_rating_text);
        writeReviewButton = findViewById(R.id.write_review_button);
        filterSpinner = findViewById(R.id.filter_spinner);
        sortButton = findViewById(R.id.sort_button);
        backButton = findViewById(R.id.back_button);

        // Lấy productId từ Intent
        productId = getIntent().getIntExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "Product ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductName();
        loadReviews();

        // Xử lý nút quay lại
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ReviewDetail.this, ProductDetail.class);
            intent.putExtra("product_id", productId);
            startActivity(intent);
            finish();
        });

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
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                int userId = prefs.getInt("user_id", -1);
                if (userId == -1) {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(this, ReviewActivity.class);
                intent.putExtra("order", order);
                startActivity(intent);
            } else {
                Toast.makeText(this, "You need to purchase this product to write a review", Toast.LENGTH_SHORT).show();
            }
        });
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
            Toast.makeText(this, "Error loading product name", Toast.LENGTH_SHORT).show();
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

            String query = "SELECT r.review_id, r.rating, r.comment, r.review_date, u.username " +
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
                    int reviewId = cursor.getInt(cursor.getColumnIndexOrThrow("review_id"));
                    float rating = cursor.getFloat(cursor.getColumnIndexOrThrow("rating"));
                    String comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"));
                    String reviewDate = cursor.getString(cursor.getColumnIndexOrThrow("review_date"));
                    String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                    reviews.add(new Review(reviewId, rating, comment, reviewDate, username));
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

            ReviewAdapter adapter = new ReviewAdapter(reviews, this);
            reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            reviewRecyclerView.setAdapter(adapter);

            if (reviews.isEmpty()) {
                Toast.makeText(this, "No reviews available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading reviews: " + e.getMessage());
            Toast.makeText(this, "Error loading reviews", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private int getOrderIdForProduct() {
        SQLiteDatabase db = null;
        int orderId = -1;
        try {
            db = dbHelper.openDatabase();
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
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
            Toast.makeText(this, "Error checking purchase", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
        return orderId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReviews();
    }

    @SuppressWarnings("deprecation") // Thêm annotation để tắt cảnh báo
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ReviewDetail.this, ProductDetail.class);
        intent.putExtra("product_id", productId);
        startActivity(intent);
        finish();
    }
}
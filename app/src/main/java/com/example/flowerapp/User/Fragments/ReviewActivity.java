package com.example.flowerapp.User.Fragments;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReviewActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView productNameTextView;
    private RatingBar ratingBar;
    private EditText reviewComment;
    private Button submitReviewBtn;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        dbHelper = new DatabaseHelper(this);
        productNameTextView = findViewById(R.id.product_name);
        ratingBar = findViewById(R.id.rating_bar);
        reviewComment = findViewById(R.id.review_comment);
        submitReviewBtn = findViewById(R.id.submit_review_btn);

        order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            productNameTextView.setText(order.getTitle());
        }

        submitReviewBtn.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = reviewComment.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(this, "Please provide a rating", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show();
                return;
            }

            saveReviewToDatabase(order.getId(), rating, comment);
            Toast.makeText(this, "Review submitted", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void saveReviewToDatabase(int orderId, float rating, String comment) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1); // Lấy từ SharedPreferences
            if (userId == -1) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy product_id từ Order_Items
            int productId = getProductIdFromOrder(db, orderId);
            if (productId == -1) {
                Toast.makeText(this, "Product not found for this order", Toast.LENGTH_SHORT).show();
                return;
            }

            ContentValues values = new ContentValues();
            values.put("order_id", orderId);
            values.put("user_id", userId);
            values.put("product_id", productId);
            values.put("rating", rating);
            values.put("comment", comment);
            values.put("review_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            long result = db.insert("Reviews", null, values);
            if (result == -1) {
                Toast.makeText(this, "Error submitting review", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error submitting review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private int getProductIdFromOrder(SQLiteDatabase db, int orderId) {
        Cursor cursor = db.rawQuery("SELECT product_id FROM Order_Items WHERE order_id = ?", new String[]{String.valueOf(orderId)});
        if (cursor.moveToFirst()) {
            int productId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
            cursor.close();
            return productId;
        }
        cursor.close();
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

            saveReviewToDatabase(order.getOrderId(), rating, comment);
            Toast.makeText(this, "Review submitted", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void saveReviewToDatabase(int orderId, float rating, String comment) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            ContentValues values = new ContentValues();
            values.put("order_id", orderId);
            values.put("user_id", 1); // Cần lấy user_id từ SharedPreferences hoặc logic đăng nhập
            values.put("product_id", 1); // Cần lấy product_id từ Order_Items
            values.put("rating", rating);
            values.put("comment", comment);
            values.put("review_date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            db.insert("Reviews", null, values);
            // Cập nhật trạng thái đơn hàng nếu cần (ví dụ: đánh dấu là đã đánh giá)
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error submitting review", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.closeDatabase(dbHelper.openDatabase());
        }
    }
}
package com.example.flowerapp.User.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

public class ProductDetail extends AppCompatActivity {
    private static final String TAG = "ProductDetail";
    private DatabaseHelper dbHelper;
    private TextView productName, productPrice, productDescription, productStock, seeAllReviews;
    private ImageView productImage;
    private RatingBar averageRating;
    private RecyclerView reviewRecyclerView;
    private Button addReviewButton, addToCartButton;
    private ImageButton backButton;
    private int productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        dbHelper = new DatabaseHelper(this);
        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price);
        productDescription = findViewById(R.id.product_description);
        productStock = findViewById(R.id.product_stock);
        productImage = findViewById(R.id.product_image);
        averageRating = findViewById(R.id.average_rating);
        reviewRecyclerView = findViewById(R.id.review_recycler_view);
        addReviewButton = findViewById(R.id.add_review_button);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        seeAllReviews = findViewById(R.id.see_all_reviews);
        backButton = findViewById(R.id.back_button);

        // Lấy productId từ Intent
        productId = getIntent().getIntExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "Product ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductDetails();
        loadReviews();

        // Xử lý nút quay lại
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetail.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Xử lý nút "See All Reviews"
        seeAllReviews.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetail.this, ReviewDetail.class);
            intent.putExtra("product_id", productId);
            startActivity(intent);
        });

        // Xử lý nút "Add Review"
        addReviewButton.setOnClickListener(v -> {
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
                Toast.makeText(this, "No order found for this product", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút "Add to Cart"
        addToCartButton.setOnClickListener(v -> {
            addToCart();
        });
    }

    private void addToCart() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();

            // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
            Cursor cursor = db.rawQuery(
                    "SELECT cart_id, quantity FROM Carts WHERE user_id = ? AND product_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(productId)});
            if (cursor.moveToFirst()) {
                // Sản phẩm đã có trong giỏ hàng, tăng số lượng
                int cartId = cursor.getInt(cursor.getColumnIndexOrThrow("cart_id"));
                int currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                boolean success = dbHelper.updateCartQuantity(cartId, currentQuantity + 1);
                if (success) {
                    Toast.makeText(this, "Increased quantity in cart", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update cart", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Sản phẩm chưa có trong giỏ hàng, thêm mới
                String insertQuery = "INSERT INTO Carts (user_id, product_id, quantity) VALUES (?, ?, ?)";
                db.execSQL(insertQuery, new Object[]{userId, productId, 1});
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart: " + e.getMessage());
            Toast.makeText(this, "Error adding to cart", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return -1;
            }

            Cursor cartCursor = db.rawQuery(
                    "SELECT cart_id FROM Carts WHERE user_id = ? AND product_id = ?",
                    new String[]{String.valueOf(userId), String.valueOf(productId)});
            if (cartCursor.moveToFirst()) {
                int cartId = cartCursor.getInt(cartCursor.getColumnIndexOrThrow("cart_id"));
                Cursor orderCursor = db.rawQuery(
                        "SELECT order_id FROM Orders WHERE user_id = ? LIMIT 1",
                        new String[]{String.valueOf(userId)});
                if (orderCursor.moveToFirst()) {
                    orderId = orderCursor.getInt(orderCursor.getColumnIndexOrThrow("order_id"));
                }
                orderCursor.close();
            }
            cartCursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting order ID: " + e.getMessage());
            Toast.makeText(this, "Error getting order", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
        return orderId;
    }

    private void loadProductDetails() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM Products WHERE product_id = ?", new String[]{String.valueOf(productId)});
            if (cursor.moveToFirst()) {
                productName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                productPrice.setText("Giá: " + String.format("%.2f VND", cursor.getDouble(cursor.getColumnIndexOrThrow("price"))));
                productDescription.setText(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                productStock.setText("Số lượng trong kho: " + cursor.getInt(cursor.getColumnIndexOrThrow("stock")));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));

                // Xử lý hiển thị ảnh
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    if (imageUrl.startsWith("assets/") || imageUrl.contains("/")) {
                        // Xử lý ảnh trong assets
                        String assetPath = "file:///android_asset/" + imageUrl.replace("assets/", "").replace("\\", "/");
                        Glide.with(this)
                                .load(assetPath)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_dialog_alert)
                                .into(productImage);
                    } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                        // Xử lý ảnh từ URL
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_dialog_alert)
                                .into(productImage);
                    } else {
                        // Xử lý ảnh từ drawable
                        int resourceId = getResources().getIdentifier(
                                imageUrl.replace(".png", "").replace(".jpg", ""), "drawable",
                                getPackageName());
                        if (resourceId != 0) {
                            Glide.with(this)
                                    .load(resourceId)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .into(productImage);
                        } else {
                            productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                        }
                    }
                } else {
                    productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error loading product details: " + e.getMessage());
            Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private void loadReviews() {
        SQLiteDatabase db = null;
        List<Review> reviews = new ArrayList<>();
        int totalReviews = 0;
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT r.review_id, r.rating, r.comment, r.review_date, u.username " +
                            "FROM Reviews r " +
                            "JOIN Users u ON r.user_id = u.user_id " +
                            "WHERE r.product_id = ? LIMIT 3",
                    new String[]{String.valueOf(productId)});
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

            // Đếm tổng số đánh giá
            Cursor countCursor = db.rawQuery(
                    "SELECT COUNT(*) FROM Reviews WHERE product_id = ?",
                    new String[]{String.valueOf(productId)});
            if (countCursor.moveToFirst()) {
                totalReviews = countCursor.getInt(0);
            }
            countCursor.close();

            float average = calculateAverageRating(reviews);
            averageRating.setRating(average);

            ReviewAdapter adapter = new ReviewAdapter(reviews, this);
            reviewRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            reviewRecyclerView.setAdapter(adapter);

            // Cập nhật văn bản của nút "See All Reviews"
            seeAllReviews.setText("See All Reviews (" + totalReviews + ")");
            seeAllReviews.setVisibility(reviews.isEmpty() ? View.GONE : View.VISIBLE);
        } catch (Exception e) {
            Log.e(TAG, "Error loading reviews: " + e.getMessage());
            Toast.makeText(this, "Error loading reviews", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private float calculateAverageRating(List<Review> reviews) {
        if (reviews.isEmpty()) return 0.0f;
        float sum = 0.0f;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại danh sách đánh giá khi quay lại Activity
        loadReviews();
    }

    @SuppressWarnings("deprecation") // Thêm annotation để tắt cảnh báo
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProductDetail.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
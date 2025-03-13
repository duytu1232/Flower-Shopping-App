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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Adapters.ReviewAdapter;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.Models.Review;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.ReviewActivity;

import java.util.ArrayList;
import java.util.List;

public class ProductDetail extends Fragment {
    private static final String TAG = "ProductDetailFragment";
    private DatabaseHelper dbHelper;
    private TextView productName, productPrice, productDescription, productStock;
    private ImageView productImage;
    private RatingBar averageRating;
    private RecyclerView reviewRecyclerView;
    private Button addReviewButton;
    private int productId;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_product_detail, container, false);

        dbHelper = new DatabaseHelper(getContext());
        productName = view.findViewById(R.id.product_name);
        productPrice = view.findViewById(R.id.product_price);
        productDescription = view.findViewById(R.id.product_description);
        productStock = view.findViewById(R.id.product_stock);
        productImage = view.findViewById(R.id.product_image);
        averageRating = view.findViewById(R.id.average_rating);
        reviewRecyclerView = view.findViewById(R.id.review_recycler_view);
        addReviewButton = view.findViewById(R.id.add_review_button);

        // Lấy productId từ Bundle (truyền từ FragmentShop hoặc nơi khác)
        Bundle args = getArguments();
        if (args != null) {
            productId = args.getInt("product_id", -1);
            if (productId == -1) {
                Toast.makeText(getContext(), "Product ID not found", Toast.LENGTH_SHORT).show();
                return view;
            }
            loadProductDetails();
            loadReviews();
        }

        addReviewButton.setOnClickListener(v -> {
            int orderId = getOrderIdForProduct(); // Lấy orderId từ giỏ hàng
            if (orderId != -1) {
                Order order = new Order(orderId, "Pending", 0.0, "Default Address"); // Cần lấy total và address thực tế
                Intent intent = new Intent(getContext(), ReviewActivity.class);
                intent.putExtra("order", order);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "No order found for this product", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private int getOrderIdForProduct() {
        SQLiteDatabase db = null;
        int orderId = -1;
        try {
            db = dbHelper.openDatabase();
            SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return -1;
            }

            // Tìm orderId từ Carts và Orders
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
            Toast.makeText(getContext(), "Error getting order", Toast.LENGTH_SHORT).show();
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
                int resourceId = getResources().getIdentifier(imageUrl, "drawable", getContext().getPackageName());
                if (resourceId != 0) {
                    Glide.with(this).load(resourceId).into(productImage);
                } else {
                    productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error loading product details: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading product", Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private void loadReviews() {
        SQLiteDatabase db = null;
        List<Review> reviews = new ArrayList<>();
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT r.rating, r.comment, r.review_date, u.username " +
                            "FROM Reviews r " +
                            "JOIN Users u ON r.user_id = u.user_id " +
                            "WHERE r.product_id = ? LIMIT 3", // Hiển thị 3 review gần nhất
                    new String[]{String.valueOf(productId)});
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

            // Tính rating trung bình
            float average = calculateAverageRating(reviews);
            averageRating.setRating(average);

            // Cài đặt adapter cho RecyclerView
            ReviewAdapter adapter = new ReviewAdapter(reviews, getContext());
            reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            reviewRecyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error loading reviews: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading reviews", Toast.LENGTH_SHORT).show();
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
}
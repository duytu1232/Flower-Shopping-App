package com.example.flowerapp.User.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.ReviewAdapter;
import com.example.flowerapp.Models.Review;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ReviewDetail extends Fragment {
    private static final String TAG = "ReviewDetailFragment";
    private DatabaseHelper dbHelper;
    private RecyclerView reviewRecyclerView;
    private TextView productNameText;
    private int productId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_review_detail, container, false);

        dbHelper = new DatabaseHelper(getContext());
        reviewRecyclerView = view.findViewById(R.id.review_recycler_view);
        productNameText = view.findViewById(R.id.product_name);

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
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT r.rating, r.comment, r.review_date, u.username " +
                            "FROM Reviews r " +
                            "JOIN Users u ON r.user_id = u.user_id " +
                            "WHERE r.product_id = ?",
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
}
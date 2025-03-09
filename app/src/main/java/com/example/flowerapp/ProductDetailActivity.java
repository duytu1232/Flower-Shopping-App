package com.example.flowerapp;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView detailImage;
    private TextView detailName, detailPrice, detailDescription;
    private Button addToCartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        detailImage = findViewById(R.id.detail_image);
        detailName = findViewById(R.id.detail_name);
        detailPrice = findViewById(R.id.detail_price);
        detailDescription = findViewById(R.id.detail_description);
        addToCartButton = findViewById(R.id.add_to_cart_button);

        int productId = getIntent().getIntExtra("product_id", -1);
        if (productId != -1) {
            loadProductDetails(productId);
        } else {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        addToCartButton.setOnClickListener(v -> {
            addToCart(productId);
        });
    }

    private void loadProductDetails(int productId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.openDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Products WHERE product_id = ?", new String[]{String.valueOf(productId)});
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex("name");
            int priceIndex = cursor.getColumnIndex("price");
            int descriptionIndex = cursor.getColumnIndex("description");
            int imageUrlIndex = cursor.getColumnIndex("image_url");

            String name = nameIndex >= 0 ? cursor.getString(nameIndex) : "No Name";
            double price = priceIndex >= 0 ? cursor.getDouble(priceIndex) : 0.0;
            String description = descriptionIndex >= 0 ? cursor.getString(descriptionIndex) : "No Description";
            String imageUrl = imageUrlIndex >= 0 ? cursor.getString(imageUrlIndex) : "";

            detailName.setText(name);
            detailPrice.setText(String.format("$%.2f", price));
            detailDescription.setText(description);
            Glide.with(this).load(imageUrl).placeholder(R.drawable.shop).into(detailImage);

            cursor.close();
        }
        dbHelper.closeDatabase(db);
    }

    private void addToCart(int productId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.openDatabase();

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        Cursor cursor = db.rawQuery("SELECT * FROM Cart WHERE user_id = ? AND product_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
        if (cursor != null && cursor.moveToFirst()) {
            int quantityIndex = cursor.getColumnIndex("quantity");
            int quantity = quantityIndex >= 0 ? cursor.getInt(quantityIndex) : 0;
            db.execSQL("UPDATE Cart SET quantity = ? WHERE user_id = ? AND product_id = ?",
                    new Object[]{quantity + 1, userId, productId});
            Toast.makeText(this, "Quantity updated in cart", Toast.LENGTH_SHORT).show();
        } else {
            db.execSQL("INSERT INTO Cart (user_id, product_id, quantity) VALUES (?, ?, ?)",
                    new Object[]{userId, productId, 1});
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        }
        if (cursor != null) cursor.close();
        dbHelper.closeDatabase(db);
    }
}
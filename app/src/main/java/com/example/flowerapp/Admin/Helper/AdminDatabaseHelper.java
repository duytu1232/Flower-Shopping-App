package com.example.flowerapp.Admin.Helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AdminDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AdminDatabase.db";
    private static final int DATABASE_VERSION = 1;

    // Tạo các bảng cho các loại dữ liệu
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_COUPONS = "coupons";
    private static final String TABLE_ORDERS = "orders";

    private static final String TABLE_FAVORITES = "favorites";

    public AdminDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Products (hàng hóa)
        db.execSQL("CREATE TABLE " + TABLE_PRODUCTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "price REAL, " +
                "quantity INTEGER)");

        // Tạo bảng Users (tài khoản)
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "email TEXT, " +
                "phone TEXT, " +
                "role TEXT, " +
                "status TEXT)");

        // Tạo bảng Coupons (coupon)
        db.execSQL("CREATE TABLE " + TABLE_COUPONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT, " +
                "discount REAL, " +
                "expiry_date TEXT)");

        // Tạo bảng Orders (đơn hàng)
        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "product_id INTEGER, " +
                "quantity INTEGER, " +
                "total REAL, " +
                "status TEXT, " +
                "order_date TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (product_id) REFERENCES products(id))");

        // Tạo bảng favorites
        db.execSQL("CREATE TABLE " + TABLE_FAVORITES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER, " +
                "FOREIGN KEY (product_id) REFERENCES products(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUPONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        onCreate(db);
    }
}

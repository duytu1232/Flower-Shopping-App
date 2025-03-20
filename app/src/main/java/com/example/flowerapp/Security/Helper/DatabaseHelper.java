package com.example.flowerapp.Security.Helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "FlowerApp.db";
    private static final int DB_VERSION = 9; // Tăng version để kích hoạt onUpgrade
    private final Context context;

    // Tên bảng và cột
    private static final String TABLE_USERS = "Users";
    private static final String TABLE_PRODUCTS = "Products";
    private static final String TABLE_ORDERS = "Orders";
    private static final String TABLE_DISCOUNT_CODES = "Discount_Codes";
    private static final String TABLE_PAYMENTS = "Payments";
    private static final String TABLE_CARTS = "Carts";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        // Kiểm tra và tạo cơ sở dữ liệu nếu cần
        SQLiteDatabase db = getWritableDatabase();
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng Users
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "full_name TEXT," +
                "password TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "role TEXT NOT NULL," +
                "status TEXT NOT NULL)";
        db.execSQL(createUsersTable);

        // Tạo bảng Products
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                "product_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "price REAL NOT NULL," +
                "stock INTEGER NOT NULL," +
                "image_url TEXT," +
                "category TEXT," +
                "type TEXT)";
        db.execSQL(createProductsTable);

        // Tạo bảng Orders
        String createOrdersTable = "CREATE TABLE " + TABLE_ORDERS + " (" +
                "order_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "order_date TEXT," +
                "total_amount REAL," +
                "status TEXT," +
                "FOREIGN KEY(user_id) REFERENCES Users(user_id))";
        db.execSQL(createOrdersTable);

        // Tạo bảng Discount_Codes
        String createDiscountCodesTable = "CREATE TABLE " + TABLE_DISCOUNT_CODES + " (" +
                "code_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "code TEXT NOT NULL UNIQUE," +
                "discount_percentage REAL," +
                "valid_from TEXT," +
                "valid_to TEXT)";
        db.execSQL(createDiscountCodesTable);

        // Tạo bảng Payments
        String createPaymentsTable = "CREATE TABLE " + TABLE_PAYMENTS + " (" +
                "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER," +
                "payment_date TEXT," +
                "amount REAL," +
                "payment_method TEXT," +
                "FOREIGN KEY(order_id) REFERENCES Orders(order_id))";
        db.execSQL(createPaymentsTable);

        // Tạo bảng Carts
        String createCartsTable = "CREATE TABLE " + TABLE_CARTS + " (" +
                "cart_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "product_id INTEGER," +
                "quantity INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES Users(user_id)," +
                "FOREIGN KEY(product_id) REFERENCES Products(product_id))";
        db.execSQL(createCartsTable);

        // Thêm dữ liệu mẫu
        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Thêm người dùng mẫu
        db.execSQL("INSERT INTO Users (username, full_name, password, email, role, status) VALUES " +
                "('admin', 'Admin User', 'duytu1232', 'duytu1232@gmail.com', 'admin', 'active')");
        db.execSQL("INSERT INTO Users (username, full_name, password, email, role, status) VALUES " +
                "('user1', 'User One', 'password123', 'user1@example.com', 'user', 'active')");

        // Thêm sản phẩm mẫu
        db.execSQL("INSERT INTO Products (name, description, price, stock, image_url, category) VALUES " +
                "('Hoa Hồng', 'Hoa hồng đỏ tươi', 50.0, 100, 'https://example.com/hoa_hong.jpg', 'Hoa Tươi')");
        db.execSQL("INSERT INTO Products (name, description, price, stock, image_url, category) VALUES " +
                "('Hoa Cúc', 'Hoa cúc trắng tinh khôi', 30.0, 150, 'https://example.com/hoa_cuc.jpg', 'Hoa Tươi')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa các bảng cũ
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISCOUNT_CODES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARTS);
        onCreate(db);
    }

    public SQLiteDatabase openDatabase() {
        return this.getWritableDatabase();
    }

    public void closeDatabase(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    // Thêm phương thức để cập nhật số lượng trong bảng Carts
    public boolean updateCartQuantity(int cartId, int quantity) {
        SQLiteDatabase db = null;
        try {
            db = openDatabase();
            db.execSQL(
                    "UPDATE " + TABLE_CARTS + " SET quantity = ? WHERE cart_id = ?",
                    new Object[]{quantity, cartId}
            );
            Log.d("DatabaseHelper", "Cập nhật số lượng giỏ hàng thành công: cart_id=" + cartId + ", quantity=" + quantity);
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi cập nhật số lượng giỏ hàng: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                closeDatabase(db);
            }
        }
    }

    // Thêm phương thức để xóa mục trong bảng Carts
    public boolean deleteCartItem(int cartId) {
        SQLiteDatabase db = null;
        try {
            db = openDatabase();
            db.execSQL(
                    "DELETE FROM " + TABLE_CARTS + " WHERE cart_id = ?",
                    new Object[]{cartId}
            );
            Log.d("DatabaseHelper", "Xóa mục giỏ hàng thành công: cart_id=" + cartId);
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi xóa mục giỏ hàng: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                closeDatabase(db);
            }
        }
    }
}
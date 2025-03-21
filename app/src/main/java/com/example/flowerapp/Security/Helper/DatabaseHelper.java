package com.example.flowerapp.Security.Helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.flowerapp.Models.Coupon;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.Models.Revenue;
import com.example.flowerapp.Models.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "FlowerApp.db";
    private static final int DB_VERSION = 10; // Đồng bộ với PRAGMA user_version trong script SQL
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        if (!checkDatabaseExists() || !isDatabaseVersionCorrect()) {
            copyDatabase();
        }
        if (!checkDatabaseIntegrity()) {
            Log.e("DatabaseHelper", "Cơ sở dữ liệu bị lỗi, sao chép lại...");
            copyDatabase();
        }
    }

    private boolean checkDatabaseExists() {
        File dbFile = context.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    private boolean isDatabaseVersionCorrect() {
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(
                    context.getDatabasePath(DB_NAME).getPath(),
                    null,
                    SQLiteDatabase.OPEN_READONLY
            );
            int userVersion = db.getVersion();
            return userVersion == DB_VERSION;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi kiểm tra phiên bản: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    private boolean checkDatabaseIntegrity() {
        SQLiteDatabase db = null;
        try {
            db = openDatabase();
            // Kiểm tra sự tồn tại của các bảng chính
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Users'", null);
            boolean hasUsersTable = cursor.getCount() > 0;
            cursor.close();
            return hasUsersTable;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi kiểm tra tính toàn vẹn: " + e.getMessage(), e);
            return false;
        } finally {
            if (db != null) {
                closeDatabase(db);
            }
        }
    }

    private void copyDatabase() {
        try {
            File dbFile = context.getDatabasePath(DB_NAME);
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            if (dbFile.exists()) {
                dbFile.delete();
            }
            try (InputStream inputStream = context.getAssets().open(DB_NAME);
                 OutputStream outputStream = new FileOutputStream(dbFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
            }
            Log.d("DatabaseHelper", "Sao chép cơ sở dữ liệu thành công");
        } catch (IOException e) {
            Log.e("DatabaseHelper", "Lỗi sao chép cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Không cần tạo bảng vì đã sao chép từ assets
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "Nâng cấp database từ phiên bản " + oldVersion + " lên " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS Notifications");
        db.execSQL("DROP TABLE IF EXISTS Order_Tracking");
        db.execSQL("DROP TABLE IF EXISTS Reviews");
        db.execSQL("DROP TABLE IF EXISTS Order_Items");
        db.execSQL("DROP TABLE IF EXISTS Payments");
        db.execSQL("DROP TABLE IF EXISTS Carts");
        db.execSQL("DROP TABLE IF EXISTS Orders");
        db.execSQL("DROP TABLE IF EXISTS Discount_Codes");
        db.execSQL("DROP TABLE IF EXISTS Products");
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Favorites");
        copyDatabase();
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public SQLiteDatabase openDatabase() {
        return this.getWritableDatabase();
    }

    public void closeDatabase(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    // Phương thức tiện ích để lấy danh sách Users
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = openDatabase();
            cursor = db.rawQuery("SELECT * FROM Users", null);
            if (cursor.moveToFirst()) {
                do {
                    User user = new User(
                            cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("username")),
                            cursor.getString(cursor.getColumnIndexOrThrow("password")),
                            cursor.getString(cursor.getColumnIndexOrThrow("email")),
                            cursor.getString(cursor.getColumnIndexOrThrow("role")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status")),
                            cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                            cursor.getString(cursor.getColumnIndexOrThrow("avatar_uri"))
                    );
                    userList.add(user);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy danh sách Users: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) closeDatabase(db);
        }
        return userList;
    }

    // Phương thức tiện ích để lấy danh sách Products
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = openDatabase();
            cursor = db.rawQuery("SELECT * FROM Products", null);
            if (cursor.moveToFirst()) {
                do {
                    Product product = new Product(
                            cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("stock")),
                            cursor.getString(cursor.getColumnIndexOrThrow("image_url")),
                            cursor.getString(cursor.getColumnIndexOrThrow("category"))
                    );
                    productList.add(product);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy danh sách Products: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) closeDatabase(db);
        }
        return productList;
    }

    // Phương thức tiện ích để lấy danh sách Orders
    public List<Order> getAllOrders() {
        List<Order> orderList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = openDatabase();
            cursor = db.rawQuery(
                    "SELECT o.*, p.name as product_name, p.image_url as product_image " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id", null);
            if (cursor.moveToFirst()) {
                do {
                    Order order = new Order(
                            cursor.getInt(cursor.getColumnIndexOrThrow("order_id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("shipping_address")),
                            cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("product_image"))
                    );
                    orderList.add(order);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy danh sách Orders: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) closeDatabase(db);
        }
        return orderList; // Sửa lỗi: trả về orderList thay vì userList
    }

    // Phương thức tiện ích để lấy danh sách Revenue
    public List<Revenue> getRevenueByPeriod(String period) {
        List<Revenue> revenueList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = openDatabase();
            String query;
            switch (period) {
                case "Hôm nay":
                    query = "SELECT payment_method, amount, payment_date FROM Payments WHERE date(payment_date) = date('now')";
                    break;
                case "Tuần này":
                    query = "SELECT payment_method, amount, payment_date FROM Payments WHERE strftime('%W', payment_date) = strftime('%W', 'now')";
                    break;
                case "Tháng này":
                    query = "SELECT payment_method, amount, payment_date FROM Payments WHERE strftime('%m', payment_date) = strftime('%m', 'now')";
                    break;
                case "Năm này":
                    query = "SELECT payment_method, amount, payment_date FROM Payments WHERE strftime('%Y', payment_date) = strftime('%Y', 'now')";
                    break;
                default:
                    query = "SELECT payment_method, amount, payment_date FROM Payments";
            }
            cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    Revenue revenue = new Revenue(
                            cursor.getString(cursor.getColumnIndexOrThrow("payment_method")),
                            cursor.getFloat(cursor.getColumnIndexOrThrow("amount")),
                            cursor.getString(cursor.getColumnIndexOrThrow("payment_date"))
                    );
                    revenueList.add(revenue);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy danh sách Revenue: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) closeDatabase(db);
        }
        return revenueList;
    }

    // Phương thức tiện ích để lấy danh sách Coupons
    public List<Coupon> getAllCoupons() {
        List<Coupon> couponList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = openDatabase();
            cursor = db.rawQuery("SELECT * FROM Discount_Codes", null);
            if (cursor.moveToFirst()) {
                do {
                    Coupon coupon = new Coupon(
                            cursor.getInt(cursor.getColumnIndexOrThrow("discount_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("code")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("discount_value")),
                            cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status"))
                    );
                    couponList.add(coupon);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy danh sách Coupons: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) closeDatabase(db);
        }
        return couponList;
    }

    // Thêm phương thức để cập nhật số lượng trong bảng Carts
    public boolean updateCartQuantity(int cartId, int quantity) {
        SQLiteDatabase db = null;
        try {
            db = openDatabase();
            db.execSQL(
                    "UPDATE Carts SET quantity = ? WHERE cart_id = ?",
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
                    "DELETE FROM Carts WHERE cart_id = ?",
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
package com.example.flowerapp.Security.Helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.flowerapp.Models.Coupon;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.Models.OrderItem;
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
    private static final int DB_VERSION = 11; // Tăng version do thay đổi schema
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
        int retries = 5; // Số lần thử lại
        int delay = 100; // Thời gian chờ giữa các lần thử (ms)

        for (int i = 0; i < retries; i++) {
            try {
                return getWritableDatabase();
            } catch (SQLiteDatabaseLockedException e) {
                if (i == retries - 1) {
                    Log.e("DatabaseHelper", "Không thể mở database sau " + retries + " lần thử: " + e.getMessage(), e);
                    throw e; // Nếu hết số lần thử, ném ngoại lệ
                }
                try {
                    Thread.sleep(delay); // Chờ trước khi thử lại
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new SQLiteDatabaseLockedException("Không thể mở database sau " + retries + " lần thử");
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
                    "SELECT o.*, p.name as product_name, p.image_url as product_image, pm.payment_method " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                            "LEFT JOIN Payments pm ON o.order_id = pm.order_id", null);
            if (cursor.moveToFirst()) {
                do {
                    int orderId = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                    String orderDate = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                    double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                    String shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow("shipping_address"));
                    String shippingMethod = cursor.getString(cursor.getColumnIndexOrThrow("shipping_method"));
                    String paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow("payment_method"));
                    String productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                    String productImage = cursor.getString(cursor.getColumnIndexOrThrow("product_image"));

                    // Lấy danh sách OrderItem cho đơn hàng này
                    List<OrderItem> orderItems = new ArrayList<>();
                    Cursor itemCursor = db.rawQuery(
                            "SELECT oi.order_item_id, oi.product_id, p.name, p.image_url, oi.quantity, oi.unit_price " +
                                    "FROM Order_Items oi " +
                                    "JOIN Products p ON oi.product_id = p.product_id " +
                                    "WHERE oi.order_id = ?", new String[]{String.valueOf(orderId)});
                    while (itemCursor.moveToNext()) {
                        int orderItemId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("order_item_id"));
                        int productId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("product_id"));
                        String itemProductName = itemCursor.getString(itemCursor.getColumnIndexOrThrow("name"));
                        String itemImageUrl = itemCursor.getString(itemCursor.getColumnIndexOrThrow("image_url"));
                        int quantity = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("quantity"));
                        double unitPrice = itemCursor.getDouble(itemCursor.getColumnIndexOrThrow("unit_price"));
                        orderItems.add(new OrderItem(orderItemId, orderId, productId, itemProductName, itemImageUrl, quantity, unitPrice));
                    }
                    itemCursor.close();

                    Order order = new Order(
                            orderId,
                            userId,
                            orderDate,
                            status,
                            totalAmount,
                            shippingAddress,
                            shippingMethod,
                            paymentMethod,
                            productName,
                            productImage,
                            orderItems // Truyền danh sách OrderItem
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
        return orderList;
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
            cursor = db.rawQuery("SELECT discount_id, code, discount_value, start_date, end_date, status, min_order_value FROM Discount_Codes", null);
            if (cursor.moveToFirst()) {
                do {
                    Coupon coupon = new Coupon(
                            cursor.getInt(cursor.getColumnIndexOrThrow("discount_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("code")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("discount_value")),
                            cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
                            cursor.getString(cursor.getColumnIndexOrThrow("status")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("min_order_value"))
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

    public Order getOrderById(int orderId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = openDatabase();
            cursor = db.rawQuery(
                    "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                            "o.shipping_method, pm.payment_method, p.name AS product_name, p.image_url " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                            "LEFT JOIN Payments pm ON o.order_id = pm.order_id " +
                            "WHERE o.order_id = ? LIMIT 1",
                    new String[]{String.valueOf(orderId)});
            if (cursor.moveToFirst()) {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String orderDate = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                String shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow("shipping_address"));
                String shippingMethod = cursor.getString(cursor.getColumnIndexOrThrow("shipping_method"));
                String paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow("payment_method"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));

                title = (title != null) ? title : "Unknown Product";
                imageUrl = (imageUrl != null) ? imageUrl : "";

                // Lấy danh sách OrderItem cho đơn hàng này
                List<OrderItem> orderItems = new ArrayList<>();
                Cursor itemCursor = db.rawQuery(
                        "SELECT oi.order_item_id, oi.product_id, p.name, p.image_url, oi.quantity, oi.unit_price " +
                                "FROM Order_Items oi " +
                                "JOIN Products p ON oi.product_id = p.product_id " +
                                "WHERE oi.order_id = ?", new String[]{String.valueOf(orderId)});
                while (itemCursor.moveToNext()) {
                    int orderItemId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("order_item_id"));
                    int productId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("product_id"));
                    String itemProductName = itemCursor.getString(itemCursor.getColumnIndexOrThrow("name"));
                    String itemImageUrl = itemCursor.getString(itemCursor.getColumnIndexOrThrow("image_url"));
                    int quantity = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("quantity"));
                    double unitPrice = itemCursor.getDouble(itemCursor.getColumnIndexOrThrow("unit_price"));
                    orderItems.add(new OrderItem(orderItemId, orderId, productId, itemProductName, itemImageUrl, quantity, unitPrice));
                }
                itemCursor.close();

                return new Order(
                        orderId,
                        userId,
                        orderDate,
                        status,
                        totalAmount,
                        shippingAddress,
                        shippingMethod,
                        paymentMethod,
                        title,
                        imageUrl,
                        orderItems // Truyền danh sách OrderItem
                );
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting order by ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) closeDatabase(db);
        }
        return null;
    }
}
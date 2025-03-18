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
    private static final int DB_VERSION = 6;
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        if (!checkDatabaseExists()) {
            copyDatabase();
        }
    }

    private boolean checkDatabaseExists() {
        File dbFile = context.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }

    private void copyDatabase() {
        try {
            File dbFile = context.getDatabasePath(DB_NAME);
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
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
        // Không cần tạo bảng nếu sao chép từ assets
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xử lý nâng cấp nếu cần (ví dụ: xóa và sao chép lại)
        db.execSQL("DROP TABLE IF EXISTS Products");
        db.execSQL("DROP TABLE IF EXISTS Users");
        db.execSQL("DROP TABLE IF EXISTS Orders");
        db.execSQL("DROP TABLE IF EXISTS Discount_Codes");
        db.execSQL("DROP TABLE IF EXISTS Payments");
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
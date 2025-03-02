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
    private static final String DATABASE_NAME = "FlowerApp.db";
    private static final int DATABASE_VERSION = 2; // Tăng version để áp dụng nâng cấp
    private static final String TAG = "DatabaseHelper";
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        copyOrVerifyDatabaseFromAssets();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo các bảng nếu cơ sở dữ liệu chưa có sẵn
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Nâng cấp cơ sở dữ liệu từ phiên bản " + oldVersion + " lên " + newVersion);
        try {
            if (oldVersion < 2) {
                // Thêm bảng products và các bảng khác nếu chưa có
                createTables(db);
            }
            // Tăng dần version và thêm logic tương ứng
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi nâng cấp cơ sở dữ liệu: " + e.getMessage());
            throw new RuntimeException("Không thể nâng cấp cơ sở dữ liệu: " + e.getMessage());
        }
    }

    private void createTables(SQLiteDatabase db) {
        // Tạo bảng Users (giả định đã có trong FlowerApp.db, không tạo lại)
        // Tạo bảng Products
        db.execSQL("CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "price REAL, " +
                "quantity INTEGER)");

        // Tạo bảng Orders (nếu cần)
        db.execSQL("CREATE TABLE IF NOT EXISTS orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "product_id INTEGER, " +
                "quantity INTEGER, " +
                "total REAL, " +
                "status TEXT, " +
                "order_date TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES Users(id), " +
                "FOREIGN KEY (product_id) REFERENCES products(id))");

        // Tạo bảng Coupons (nếu cần)
        db.execSQL("CREATE TABLE IF NOT EXISTS coupons (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "code TEXT, " +
                "discount REAL, " +
                "expiry_date TEXT)");

        // Tạo bảng Favorites (nếu cần)
        db.execSQL("CREATE TABLE IF NOT EXISTS favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "product_id INTEGER, " +
                "FOREIGN KEY (product_id) REFERENCES products(id))");
    }

    public SQLiteDatabase openDatabase() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            copyOrVerifyDatabaseFromAssets();
        }
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
            // Kiểm tra xem bảng Users có tồn tại không
            db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='Users'", null);
            Log.d(TAG, "Mở cơ sở dữ liệu thành công: " + dbFile.getPath());
            return db;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi mở cơ sở dữ liệu: " + e.getMessage());
            throw new RuntimeException("Không thể mở cơ sở dữ liệu: " + e.getMessage());
        }
    }

    public void closeDatabase(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
            Log.d(TAG, "Đóng cơ sở dữ liệu thành công");
        }
    }

    private void copyOrVerifyDatabaseFromAssets() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            try {
                InputStream inputStream = context.getAssets().open(DATABASE_NAME);
                // Kiểm tra kích thước file để đảm bảo không rỗng
                if (inputStream.available() == 0) {
                    throw new IOException("Cơ sở dữ liệu trong assets trống!");
                }
                File outDir = dbFile.getParentFile();
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }
                OutputStream outputStream = new FileOutputStream(dbFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
                Log.d(TAG, "Sao chép cơ sở dữ liệu từ assets thành công");
            } catch (IOException e) {
                Log.e(TAG, "Lỗi sao chép cơ sở dữ liệu: " + e.getMessage());
                throw new RuntimeException("Không thể sao chép cơ sở dữ liệu từ assets: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Cơ sở dữ liệu đã tồn tại: " + dbFile.getPath());
        }
    }
}
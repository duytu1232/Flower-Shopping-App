package com.example.flowerapp.Security.Helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FlowerApp.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TAG = "DatabaseHelper";
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        copyOrVerifyDatabaseFromAssets();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Không tạo bảng, vì cơ sở dữ liệu đã được sao chép từ assets
        Log.d(TAG, "Cơ sở dữ liệu đã được sao chép từ assets, không tạo bảng mới.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Nâng cấp cơ sở dữ liệu từ phiên bản " + oldVersion + " lên " + newVersion);
        try {
            if (oldVersion < 2) {
                // Thêm cột product_name vào Orders mà không xóa dữ liệu
                db.execSQL("ALTER TABLE Orders ADD COLUMN product_name TEXT DEFAULT NULL");
                Log.d(TAG, "Đã thêm cột product_name vào bảng Orders.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi nâng cấp cơ sở dữ liệu: " + e.getMessage());
            Toast.makeText(context, "Lỗi nâng cấp cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
            throw new RuntimeException("Không thể nâng cấp cơ sở dữ liệu: " + e.getMessage());
        }
    }

    public SQLiteDatabase openDatabase() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            copyOrVerifyDatabaseFromAssets();
        }
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
            db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='Users'", null).close();
            Log.d(TAG, "Mở cơ sở dữ liệu thành công: " + dbFile.getPath());
            return db;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi mở cơ sở dữ liệu: " + e.getMessage());
            Toast.makeText(context, "Không thể mở cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, "Lỗi sao chép cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                throw new RuntimeException("Không thể sao chép cơ sở dữ liệu từ assets: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Cơ sở dữ liệu đã tồn tại: " + dbFile.getPath());
        }
    }
}
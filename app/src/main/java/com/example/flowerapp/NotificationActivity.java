package com.example.flowerapp;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import com.example.flowerapp.Adapters.NotificationAdapter;
import com.example.flowerapp.Models.NotificationItem;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";
    private DatabaseHelper dbHelper;
    private LinearLayout emptyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        dbHelper = new DatabaseHelper(this);

        // Cập nhật tiêu đề
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setText("Notifications");
        } else {
            Log.e(TAG, "tvTitle is null! Check activity_notification.xml");
        }

        // Khởi tạo empty layout
        emptyLayout = findViewById(R.id.empty_layout);

        // Khởi tạo RecyclerView
        RecyclerView recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        if (recyclerViewNotifications != null) {
            recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));

            // Lấy user_id từ SharedPreferences
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Log.e(TAG, "User not logged in");
                return;
            }

            // Lấy dữ liệu từ cơ sở dữ liệu
            List<NotificationItem> notificationList = new ArrayList<>();
            try {
                SQLiteDatabase db = dbHelper.openDatabase();
                String query = "SELECT * FROM Notifications WHERE user_id = ?";
                Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

                int orderIdIndex = cursor.getColumnIndex("order_id");
                int messageIndex = cursor.getColumnIndex("message");
                int sentTimeIndex = cursor.getColumnIndex("sent_time");

                if (messageIndex == -1 || sentTimeIndex == -1) {
                    Log.e(TAG, "Cột 'message' hoặc 'sent_time' không tồn tại trong kết quả truy vấn!");
                    return;
                }

                while (cursor.moveToNext()) {
                    int orderId = orderIdIndex != -1 ? cursor.getInt(orderIdIndex) : -1;
                    String message = cursor.getString(messageIndex);
                    String sentTime = cursor.getString(sentTimeIndex);
                    String title = (message != null && message.contains(".")) ? message.split("\\.")[0].trim() : message;
                    notificationList.add(new NotificationItem(orderId, title, message, sentTime));
                }
                cursor.close();

                // Cập nhật trạng thái thông báo thành "read"
                ContentValues values = new ContentValues();
                values.put("status", "read");
                db.update("Notifications", values, "user_id = ? AND status = 'sent'", new String[]{String.valueOf(userId)});

                dbHelper.closeDatabase(db);
                Log.d(TAG, "Đã lấy " + notificationList.size() + " thông báo từ cơ sở dữ liệu.");
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy dữ liệu từ cơ sở dữ liệu: " + e.getMessage());
            }

            // Thiết lập adapter
            if (!notificationList.isEmpty()) {
                NotificationAdapter adapter = new NotificationAdapter(notificationList, dbHelper);
                recyclerViewNotifications.setAdapter(adapter);
                recyclerViewNotifications.setVisibility(View.VISIBLE);
                if (emptyLayout != null) emptyLayout.setVisibility(View.GONE);
            } else {
                recyclerViewNotifications.setVisibility(View.GONE);
                if (emptyLayout != null) emptyLayout.setVisibility(View.VISIBLE);
                Log.w(TAG, "Danh sách thông báo trống!");
            }
        } else {
            Log.e(TAG, "recyclerViewNotifications is null! Check activity_notification.xml");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
package com.example.flowerapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.util.Log;

import com.example.flowerapp.Adapters.NotificationAdapter;
import com.example.flowerapp.User.Fragments.Class.NotificationItem;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification); // Đảm bảo layout được inflate

        // Cập nhật tiêu đề
        TextView tvTitle = findViewById(R.id.tvTitle); // Khai báo biến để tránh lỗi
        if (tvTitle != null) {
            tvTitle.setText("Notifications");
        } else {
            Log.e(TAG, "tvTitle is null! Check activity_notification.xml");
        }

        // Khởi tạo RecyclerView
        RecyclerView recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications); // Khai báo biến
        if (recyclerViewNotifications != null) {
            recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));

            // Lấy dữ liệu từ cơ sở dữ liệu
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            List<NotificationItem> notificationList = new ArrayList<>();
            try {
                SQLiteDatabase db = dbHelper.openDatabase();
                // Truy vấn bảng Notifications
                String query = "SELECT * FROM Notifications WHERE user_id = ?"; // Thay ? bằng user_id thực tế
                Cursor cursor = db.rawQuery(query, new String[]{"1"}); // Giả sử user_id = 1 (cần thay bằng logic lấy user_id)

                int messageIndex = cursor.getColumnIndex("message");
                int sentTimeIndex = cursor.getColumnIndex("sent_time");

                if (messageIndex == -1 || sentTimeIndex == -1) {
                    Log.e(TAG, "Cột 'message' hoặc 'sent_time' không tồn tại trong kết quả truy vấn!");
                    return;
                }

                while (cursor.moveToNext()) {
                    String message = cursor.getString(messageIndex);
                    String sentTime = cursor.getString(sentTimeIndex);
                    // Tạo title từ message (lấy phần đầu tiên hoặc tùy chỉnh logic)
                    String title = (message != null && message.contains(".")) ? message.split("\\.")[0].trim() : message;
                    notificationList.add(new NotificationItem(title, message, sentTime));
                }
                cursor.close();
                dbHelper.closeDatabase(db);
                Log.d(TAG, "Đã lấy " + notificationList.size() + " thông báo từ cơ sở dữ liệu.");
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi lấy dữ liệu từ cơ sở dữ liệu: " + e.getMessage());
            }

            // Thiết lập adapter
            if (!notificationList.isEmpty()) {
                NotificationAdapter adapter = new NotificationAdapter(notificationList);
                recyclerViewNotifications.setAdapter(adapter);
            } else {
                Log.w(TAG, "Danh sách thông báo trống!");
                // Có thể hiển thị thông báo cho người dùng (ví dụ: Toast)
            }
        } else {
            Log.e(TAG, "recyclerViewNotifications is null! Check activity_notification.xml");
        }
    }
}
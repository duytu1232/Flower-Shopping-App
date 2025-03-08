package com.example.flowerapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;

import com.example.flowerapp.Adapters.NotificationAdapter;
import com.example.flowerapp.User.Fragments.Class.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification); // Đảm bảo layout được inflate

        // Cập nhật tiêu đề
        TextView tvTitle = findViewById(R.id.tvTitle); // Khai báo biến để tránh lỗi
        if (tvTitle != null) {
            tvTitle.setText("Notifications");
        } else {
            // Log hoặc xử lý lỗi nếu tvTitle không tìm thấy
        }

        // Khởi tạo RecyclerView
        RecyclerView recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications); // Khai báo biến
        if (recyclerViewNotifications != null) {
            recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));

            // Tạo danh sách thông báo mẫu
            List<NotificationItem> notificationList = new ArrayList<>();
            notificationList.add(new NotificationItem("New Message", "You have a new message from John Doe.", "2 min ago"));
            notificationList.add(new NotificationItem("Order Update", "Your order has been shipped.", "10 min ago"));
            notificationList.add(new NotificationItem("Promotion", "Get 20% off your next purchase!", "1 hour ago"));

            // Thiết lập adapter
            NotificationAdapter adapter = new NotificationAdapter(notificationList);
            recyclerViewNotifications.setAdapter(adapter);
        } else {
            // Log hoặc xử lý lỗi nếu recyclerViewNotifications không tìm thấy
        }
    }
}
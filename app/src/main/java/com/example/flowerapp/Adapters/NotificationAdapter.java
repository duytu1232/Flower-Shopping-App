package com.example.flowerapp.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.R;
import com.example.flowerapp.Models.NotificationItem;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.OrderDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationItem> notifications;
    private DatabaseHelper dbHelper;

    public NotificationAdapter(List<NotificationItem> notifications, DatabaseHelper dbHelper) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        this.dbHelper = dbHelper;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NotificationItem notification = notifications.get(position);
        if (holder.tvTitle != null) {
            holder.tvTitle.setText(notification.getTitle() != null ? notification.getTitle() : "No Title");
        }
        if (holder.tvMessage != null) {
            holder.tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "No Message");
        }
        if (holder.tvTimestamp != null) {
            holder.tvTimestamp.setText(notification.getTimestamp() != null ? notification.getTimestamp() : "No Time");
        }

        // Xử lý sự kiện nhấn vào thông báo
        holder.itemView.setOnClickListener(v -> {
            int orderId = notification.getOrderId();
            if (orderId != -1) {
                // Lấy thông tin đơn hàng từ database
                Order order = dbHelper.getOrderById(orderId);
                if (order != null) {
                    Intent intent = new Intent(holder.itemView.getContext(), OrderDetailActivity.class);
                    intent.putExtra("order", order);
                    holder.itemView.getContext().startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTimestamp;
        ImageView ivIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
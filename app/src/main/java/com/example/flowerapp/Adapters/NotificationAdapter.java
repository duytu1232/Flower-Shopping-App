package com.example.flowerapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.R;
import com.example.flowerapp.Models.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationItem> notifications;

    public NotificationAdapter(List<NotificationItem> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
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
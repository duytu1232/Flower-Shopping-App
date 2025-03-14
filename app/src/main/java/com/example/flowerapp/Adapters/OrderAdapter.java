package com.example.flowerapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.function.Consumer;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Consumer<Order> onEditClick;
    private Consumer<Integer> onDeleteClick;

    // Constructor mới cho hiển thị (không cần edit/delete)
    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
        this.onEditClick = null;
        this.onDeleteClick = null;
    }

    // Constructor cũ cho quản lý (giữ nguyên)
    public OrderAdapter(List<Order> orderList, Consumer<Order> onEditClick, Consumer<Integer> onDeleteClick) {
        this.orderList = orderList;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_user, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.orderTitle.setText(order.getTitle());
        holder.orderDate.setText("Ngày đặt: " + order.getOrderDate());
        holder.orderStatus.setText("Trạng thái: " + order.getStatus());
        holder.orderTotal.setText("Tổng tiền: " + String.format("%.2f VND", order.getTotalAmount()));
        holder.orderAddress.setText("Địa chỉ: " + order.getShippingAddress());

        // Tải hình ảnh nếu có
        if (order.getImageUrl() != null && !order.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(order.getImageUrl())
                    .placeholder(R.drawable.order_base_line)
                    .into(holder.orderImage);
        } else {
            holder.orderImage.setImageResource(R.drawable.order_base_line);
        }

        // Ẩn các nút edit và delete nếu không cần
        if (onEditClick == null || onDeleteClick == null) {
            if (holder.btnEditOrder != null) holder.btnEditOrder.setVisibility(View.GONE);
            if (holder.btnDeleteOrder != null) holder.btnDeleteOrder.setVisibility(View.GONE);
        } else {
            if (holder.btnEditOrder != null) {
                holder.btnEditOrder.setVisibility(View.VISIBLE);
                holder.btnEditOrder.setOnClickListener(v -> onEditClick.accept(order));
            }
            if (holder.btnDeleteOrder != null) {
                holder.btnDeleteOrder.setVisibility(View.VISIBLE);
                holder.btnDeleteOrder.setOnClickListener(v -> onDeleteClick.accept(order.getId()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderTitle, orderDate, orderStatus, orderTotal, orderAddress;
        ImageView orderImage;
        MaterialButton btnEditOrder, btnDeleteOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderTitle = itemView.findViewById(R.id.order_title);
            orderDate = itemView.findViewById(R.id.order_date);
            orderStatus = itemView.findViewById(R.id.order_status);
            orderTotal = itemView.findViewById(R.id.order_total);
            orderAddress = itemView.findViewById(R.id.order_address);
            orderImage = itemView.findViewById(R.id.order_image);
            btnEditOrder = itemView.findViewById(R.id.btn_edit_order);
            btnDeleteOrder = itemView.findViewById(R.id.btn_delete_order);
        }
    }
}
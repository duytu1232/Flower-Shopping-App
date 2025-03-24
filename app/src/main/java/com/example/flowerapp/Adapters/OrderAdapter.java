package com.example.flowerapp.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.OrderDetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Consumer<Order> onReviewClick;
    private Consumer<Order> onEditClick;
    private Consumer<Order> onDeleteClick;
    private boolean isAdminMode;

    public OrderAdapter(List<Order> orderList, Consumer<Order> onReviewClick) {
        this.orderList = orderList != null ? orderList : new ArrayList<>();
        this.onReviewClick = onReviewClick;
        this.isAdminMode = false;
    }

    public OrderAdapter(List<Order> orderList, Consumer<Order> onEditClick, Consumer<Order> onDeleteClick) {
        this.orderList = orderList != null ? orderList : new ArrayList<>();
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
        this.isAdminMode = true;
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

        // Xử lý null cho các giá trị từ Order
        String title = order.getTitle() != null ? order.getTitle() : "Unknown Product";
        String status = order.getStatus() != null ? order.getStatus() : "Unknown";
        String orderDate = order.getOrderDate() != null ? order.getOrderDate() : "Unknown Date";

        holder.orderTitle.setText(title);
        holder.orderStatus.setText("Trạng thái: " + status);
        holder.orderDate.setText("Ngày đặt: " + orderDate);

        String imageUrl = order.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.shop)
                    .error(R.drawable.shop)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.orderImage);
        } else {
            holder.orderImage.setImageResource(R.drawable.shop);
        }

        if (isAdminMode) {
            holder.reviewButton.setVisibility(View.GONE);
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            holder.editButton.setOnClickListener(v -> {
                if (onEditClick != null) {
                    onEditClick.accept(order);
                } else {
                    // Log hoặc thông báo khi callback không được thiết lập
                    android.util.Log.w("OrderAdapter", "onEditClick callback is not set for order ID: " + order.getId());
                }
            });
            holder.deleteButton.setOnClickListener(v -> {
                if (onDeleteClick != null) {
                    onDeleteClick.accept(order);
                } else {
                    android.util.Log.w("OrderAdapter", "onDeleteClick callback is not set for order ID: " + order.getId());
                }
            });
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
            if (onReviewClick != null && "delivered".equals(status)) {
                holder.reviewButton.setVisibility(View.VISIBLE);
                holder.reviewButton.setOnClickListener(v -> onReviewClick.accept(order));
            } else {
                holder.reviewButton.setVisibility(View.GONE);
                if (onReviewClick == null && "delivered".equals(status)) {
                    android.util.Log.w("OrderAdapter", "onReviewClick callback is not set for delivered order ID: " + order.getId());
                }
            }
        }

        if (!isAdminMode) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), OrderDetailActivity.class);
                intent.putExtra("order", order);
                holder.itemView.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    // Phương thức mới để cập nhật danh sách đơn hàng
    public void updateOrderList(List<Order> newOrderList) {
        this.orderList = newOrderList != null ? newOrderList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView orderImage;
        TextView orderTitle, orderStatus, orderDate;
        Button reviewButton, editButton, deleteButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderImage = itemView.findViewById(R.id.orderImage);
            orderTitle = itemView.findViewById(R.id.orderTitle);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderDate = itemView.findViewById(R.id.orderDate);
            reviewButton = itemView.findViewById(R.id.review_button);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
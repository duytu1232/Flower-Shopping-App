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
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.OrderDetailActivity;

import java.util.List;
import java.util.function.Consumer;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Consumer<Order> onReviewClick;
    private Consumer<Order> onEditClick;
    private Consumer<Order> onDeleteClick;
    private boolean isAdminMode;

    public OrderAdapter(List<Order> orderList, Consumer<Order> onReviewClick) {
        this.orderList = orderList;
        this.onReviewClick = onReviewClick;
        this.isAdminMode = false;
    }

    public OrderAdapter(List<Order> orderList, Consumer<Order> onEditClick, Consumer<Order> onDeleteClick) {
        this.orderList = orderList;
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

        holder.orderTitle.setText(order.getTitle());
        holder.orderStatus.setText("Trạng thái: " + order.getStatus());
        holder.orderDate.setText("Ngày đặt: " + order.getOrderDate());

        String imageUrl = order.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.shop)
                    .error(R.drawable.shop)
                    .into(holder.orderImage);
        } else {
            holder.orderImage.setImageResource(R.drawable.shop);
        }

        if (isAdminMode) {
            holder.reviewButton.setVisibility(View.GONE);
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            holder.editButton.setOnClickListener(v -> {
                if (onEditClick != null) onEditClick.accept(order);
            });
            holder.deleteButton.setOnClickListener(v -> {
                if (onDeleteClick != null) onDeleteClick.accept(order);
            });
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
            if (onReviewClick != null && "delivered".equals(order.getStatus())) {
                holder.reviewButton.setVisibility(View.VISIBLE);
                holder.reviewButton.setOnClickListener(v -> onReviewClick.accept(order));
            } else {
                holder.reviewButton.setVisibility(View.GONE);
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
        return orderList.size();
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
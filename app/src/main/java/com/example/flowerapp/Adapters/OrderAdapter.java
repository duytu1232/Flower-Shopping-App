package com.example.flowerapp.Adapters;

import android.content.Context;
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
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.ReviewActivity;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Context context;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_user, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.orderTitle.setText(order.getTitle() != null ? order.getTitle() : "No Title");
        holder.orderStatus.setText(order.getStatus() != null ? order.getStatus() : "Unknown");
        holder.orderDate.setText(order.getDate() != null ? order.getDate() : "N/A");
        Glide.with(context)
                .load(order.getImageUrl())  // Thêm getter getImageUrl() vào Order
                .placeholder(R.drawable.shop)
                .error(R.drawable.shop)
                .into(holder.orderImage);

        if ("ChuaDanhGiaFragment".equals(context.getClass().getSimpleName())) {
            holder.reviewButton.setVisibility(View.VISIBLE);
            holder.reviewButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, ReviewActivity.class);
                intent.putExtra("order", order);
                context.startActivity(intent);
            });
        } else {
            holder.reviewButton.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("order", order);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return (orderList != null) ? orderList.size() : 0;
    }

    public void updateOrders(List<Order> newOrderList) {
        this.orderList = newOrderList;
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderTitle, orderStatus, orderDate;
        ImageView orderImage;
        Button reviewButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderTitle = itemView.findViewById(R.id.orderTitle);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderDate = itemView.findViewById(R.id.orderDate);
            orderImage = itemView.findViewById(R.id.orderImage);
            reviewButton = itemView.findViewById(R.id.review_button);
        }
    }
}
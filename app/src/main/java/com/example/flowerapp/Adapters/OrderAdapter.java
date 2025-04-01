package com.example.flowerapp.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.OrderDetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_ADMIN = 1;

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

    @Override
    public int getItemViewType(int position) {
        return isAdminMode ? VIEW_TYPE_ADMIN : VIEW_TYPE_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADMIN) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_admin, parent, false);
            return new AdminOrderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_user, parent, false);
            return new UserOrderViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Order order = orderList.get(position);

        if (holder instanceof AdminOrderViewHolder) {
            AdminOrderViewHolder adminHolder = (AdminOrderViewHolder) holder;
            adminHolder.orderId.setText("Order #" + order.getId());
            adminHolder.orderStatus.setText("Status: " + order.getStatus());
            adminHolder.orderDate.setText("Date: " + order.getOrderDate());
            adminHolder.orderTotal.setText(String.format("Total: %.2f VND", order.getTotalAmount()));
            adminHolder.shippingAddress.setText("Address: " + order.getShippingAddress());
            adminHolder.shippingMethod.setText("Shipping: " + order.getShippingMethod());
            adminHolder.paymentMethod.setText("Payment: " + order.getPaymentMethod());

            OrderItemAdapter itemAdapter = new OrderItemAdapter(order.getOrderItems());
            adminHolder.orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(adminHolder.itemView.getContext()));
            adminHolder.orderItemsRecyclerView.setAdapter(itemAdapter);

            adminHolder.editButton.setOnClickListener(v -> onEditClick.accept(order));
            adminHolder.deleteButton.setOnClickListener(v -> onDeleteClick.accept(order));
        } else {
            UserOrderViewHolder userHolder = (UserOrderViewHolder) holder;
            String title = order.getTitle() != null ? order.getTitle() : "Unknown Product";
            String status = order.getStatus() != null ? order.getStatus() : "Unknown";
            String orderDate = order.getOrderDate() != null ? order.getOrderDate() : "Unknown Date";

            userHolder.orderTitle.setText(title);
            userHolder.orderStatus.setText("Status: " + status);
            userHolder.orderDate.setText("Date: " + orderDate);
            userHolder.shippingMethod.setText("Shipping: " + order.getShippingMethod());
            userHolder.paymentMethod.setText("Payment: " + order.getPaymentMethod());

            String imageUrl = order.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(userHolder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.shop)
                        .error(R.drawable.shop)
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(userHolder.orderImage);
            } else {
                userHolder.orderImage.setImageResource(R.drawable.shop);
            }

            if ("delivered".equals(status) && onReviewClick != null) {
                userHolder.reviewButton.setVisibility(View.VISIBLE);
                userHolder.reviewButton.setOnClickListener(v -> onReviewClick.accept(order));
            } else {
                userHolder.reviewButton.setVisibility(View.GONE);
            }

            userHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(userHolder.itemView.getContext(), OrderDetailActivity.class);
                intent.putExtra("order", order);
                userHolder.itemView.getContext().startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public void updateOrderList(List<Order> newOrderList) {
        this.orderList = newOrderList != null ? newOrderList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class UserOrderViewHolder extends RecyclerView.ViewHolder {
        ImageView orderImage;
        TextView orderTitle, orderStatus, orderDate, shippingMethod, paymentMethod;
        Button reviewButton;

        public UserOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderImage = itemView.findViewById(R.id.orderImage);
            orderTitle = itemView.findViewById(R.id.orderTitle);
            orderStatus = itemView.findViewById(R.id.orderStatus);
            orderDate = itemView.findViewById(R.id.orderDate);
            shippingMethod = itemView.findViewById(R.id.shippingMethod);
            paymentMethod = itemView.findViewById(R.id.paymentMethod);
            reviewButton = itemView.findViewById(R.id.review_button);
        }
    }

    static class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderId, orderStatus, orderDate, orderTotal, shippingAddress, shippingMethod, paymentMethod;
        RecyclerView orderItemsRecyclerView;
        Button editButton, deleteButton;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.order_id);
            orderStatus = itemView.findViewById(R.id.order_status);
            orderDate = itemView.findViewById(R.id.order_date);
            orderTotal = itemView.findViewById(R.id.order_total);
            shippingAddress = itemView.findViewById(R.id.shipping_address);
            shippingMethod = itemView.findViewById(R.id.shipping_method);
            paymentMethod = itemView.findViewById(R.id.payment_method);
            orderItemsRecyclerView = itemView.findViewById(R.id.order_items_recycler_view);
            editButton = itemView.findViewById(R.id.btn_edit_order);
            deleteButton = itemView.findViewById(R.id.btn_delete_order);
        }
    }
}
package com.example.flowerapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Models.OrderItem;
import com.example.flowerapp.R;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private List<OrderItem> orderItemList;

    public OrderItemAdapter(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem orderItem = orderItemList.get(position);
        holder.productName.setText(orderItem.getProductName());
        holder.productPrice.setText(String.format("Price: %.2f VND", orderItem.getUnitPrice()));
        holder.productQuantity.setText(String.format("Quantity: %d", orderItem.getQuantity()));
        holder.totalItemPrice.setText(String.format("Total: %.2f VND", orderItem.getUnitPrice() * orderItem.getQuantity()));

        if (orderItem.getImageUrl() != null && !orderItem.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(orderItem.getImageUrl())
                    .placeholder(R.drawable.rose)
                    .error(R.drawable.rose)
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.rose);
        }
    }

    @Override
    public int getItemCount() {
        return orderItemList.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productQuantity, totalItemPrice;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.checkout_product_image);
            productName = itemView.findViewById(R.id.checkout_product_name);
            productPrice = itemView.findViewById(R.id.checkout_product_price);
            productQuantity = itemView.findViewById(R.id.checkout_product_quantity);
            totalItemPrice = itemView.findViewById(R.id.checkout_total_item_price);
        }
    }
}
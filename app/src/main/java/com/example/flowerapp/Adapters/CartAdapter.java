package com.example.flowerapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartList;
    private Context context;

    public CartAdapter(List<CartItem> cartList, Context context) {
        this.cartList = cartList != null ? cartList : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        if (holder.nameTextView != null) {
            holder.nameTextView.setText(item.getName() != null ? item.getName() : "No Name");
        }
        if (holder.priceTextView != null) {
            holder.priceTextView.setText(String.format("$%.2f", item.getPrice()));
        }
        if (holder.quantityTextView != null) {
            holder.quantityTextView.setText(String.valueOf(item.getQuantity()));
        }
        if (holder.imageView != null) {
            Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.shop)
                    .error(R.drawable.shop)
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView, quantityTextView;
        ImageView imageView;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.cart_item_name);
            priceTextView = itemView.findViewById(R.id.cart_item_price);
            quantityTextView = itemView.findViewById(R.id.cart_item_quantity);
            imageView = itemView.findViewById(R.id.cart_item_image);
        }
    }
}
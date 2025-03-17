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

import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {
    private List<CartItem> cartItems;
    private Context context;

    public CheckoutAdapter(List<CartItem> cartItems, Context context) {
        this.cartItems = cartItems;
        this.context = context;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.productName.setText(item.getName());
        holder.productPrice.setText(String.format("Price: %.2f VND", item.getPrice()));
        holder.productQuantity.setText("Quantity: " + item.getQuantity());
        holder.totalItemPrice.setText(String.format("Total: %.2f VND", item.getPrice() * item.getQuantity()));

        // Load image using Glide
        String imageUrl = item.getImageUrl();
        int resourceId = context.getResources().getIdentifier(imageUrl, "drawable", context.getPackageName());
        if (resourceId != 0) {
            Glide.with(context).load(resourceId).into(holder.productImage);
        } else {
            holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity, totalItemPrice;
        ImageView productImage;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.checkout_product_name);
            productPrice = itemView.findViewById(R.id.checkout_product_price);
            productQuantity = itemView.findViewById(R.id.checkout_product_quantity);
            totalItemPrice = itemView.findViewById(R.id.checkout_total_item_price);
            productImage = itemView.findViewById(R.id.checkout_product_image);
        }
    }
}
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
        CartItem cartItem = cartItems.get(position);
        holder.productName.setText(cartItem.getName());
        holder.productPrice.setText(String.format("Giá: %.2f VND", cartItem.getPrice()));
        holder.productQuantity.setText("Số lượng: " + cartItem.getQuantity());
        holder.totalItemPrice.setText(String.format("Tổng: %.2f VND", cartItem.getPrice() * cartItem.getQuantity()));

        // Xử lý hiển thị ảnh
        String imageUrl = cartItem.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("assets/") || imageUrl.contains("/")) {
                String assetPath = "file:///android_asset/" + imageUrl.replace("assets/", "").replace("\\", "/");
                Glide.with(context)
                        .load(assetPath)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_dialog_alert)
                        .into(holder.productImage);
            } else {
                int resourceId = context.getResources().getIdentifier(
                        imageUrl.replace(".png", "").replace(".jpg", ""), "drawable",
                        context.getPackageName());
                if (resourceId != 0) {
                    Glide.with(context)
                            .load(resourceId)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(holder.productImage);
                } else {
                    holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
        } else {
            holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productQuantity, totalItemPrice;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.checkout_product_image);
            productName = itemView.findViewById(R.id.checkout_product_name);
            productPrice = itemView.findViewById(R.id.checkout_product_price);
            productQuantity = itemView.findViewById(R.id.checkout_product_quantity);
            totalItemPrice = itemView.findViewById(R.id.checkout_total_item_price);
        }
    }
}
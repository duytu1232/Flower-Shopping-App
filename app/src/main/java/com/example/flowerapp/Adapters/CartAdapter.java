package com.example.flowerapp.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.R;
import com.example.flowerapp.User.Fragments.ProductDetail;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.function.Consumer;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartList;
    private Context context;
    private Consumer<CartItem> onQuantityIncrease;
    private Consumer<CartItem> onQuantityDecrease;
    private Consumer<CartItem> onDeleteClick;

    public CartAdapter(List<CartItem> cartList, Context context,
                       Consumer<CartItem> onQuantityIncrease,
                       Consumer<CartItem> onQuantityDecrease,
                       Consumer<CartItem> onDeleteClick) {
        this.cartList = cartList;
        this.context = context;
        this.onQuantityIncrease = onQuantityIncrease;
        this.onQuantityDecrease = onQuantityDecrease;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        holder.cartItemName.setText(item.getName());
        holder.cartItemPrice.setText("Giá: " + String.format("%.2f VND", item.getPrice()));
        holder.cartItemQuantity.setText("Số lượng: " + item.getQuantity());

        String imageName = item.getImageUrl();
        if (imageName != null && !imageName.isEmpty()) {
            int resourceId = context.getResources().getIdentifier(
                    imageName, "drawable", context.getPackageName());
            Log.d("CartAdapter", "Image name: " + imageName + ", Resource ID: " + resourceId);
            if (resourceId != 0) {
                Glide.with(context)
                        .load(resourceId)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.cartItemImage);
            } else {
                holder.cartItemImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.cartItemImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Điều hướng đến ProductDetail khi nhấn vào item
        holder.itemView.setOnClickListener(v -> {
            ProductDetail productDetailFragment = new ProductDetail();
            Bundle args = new Bundle();
            args.putInt("product_id", item.getProductId());
            productDetailFragment.setArguments(args);

            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, productDetailFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Xử lý nút tăng số lượng
        holder.btnIncreaseQuantity.setOnClickListener(v -> {
            if (onQuantityIncrease != null) {
                onQuantityIncrease.accept(item);
                holder.cartItemQuantity.setText("Số lượng: " + item.getQuantity());
            }
        });

        // Xử lý nút giảm số lượng
        holder.btnDecreaseQuantity.setOnClickListener(v -> {
            if (onQuantityDecrease != null) {
                onQuantityDecrease.accept(item);
                holder.cartItemQuantity.setText("Số lượng: " + item.getQuantity());
            }
        });

        // Xử lý nút xóa
        holder.btnDeleteItem.setOnClickListener(v -> {
            if (onDeleteClick != null) {
                onDeleteClick.accept(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView cartItemImage;
        TextView cartItemName, cartItemPrice, cartItemQuantity;
        MaterialButton btnIncreaseQuantity, btnDecreaseQuantity, btnDeleteItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemImage = itemView.findViewById(R.id.cart_item_image);
            cartItemName = itemView.findViewById(R.id.cart_item_name);
            cartItemPrice = itemView.findViewById(R.id.cart_item_price);
            cartItemQuantity = itemView.findViewById(R.id.cart_item_quantity);
            btnIncreaseQuantity = itemView.findViewById(R.id.btn_increase_quantity);
            btnDecreaseQuantity = itemView.findViewById(R.id.btn_decrease_quantity);
            btnDeleteItem = itemView.findViewById(R.id.btn_delete_item);
        }
    }
}
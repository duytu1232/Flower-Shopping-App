package com.example.flowerapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Managers.CartManager;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems;
    private Context context;
    private CartManager cartManager;
    private OnCartChangeListener cartChangeListener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    public CartAdapter(List<CartItem> cartItems, Context context, OnCartChangeListener listener) {
        this.cartItems = cartItems;
        this.context = context;
        this.cartManager = new CartManager(context);
        this.cartChangeListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        // Hiển thị thông tin sản phẩm
        holder.cartItemName.setText(cartItem.getName());
        holder.cartItemPrice.setText(String.format("Giá: %.2f VND", cartItem.getPrice() * cartItem.getQuantity()));
        holder.cartItemQuantity.setText("Số lượng: " + cartItem.getQuantity());

        // Xử lý hiển thị ảnh
        String imageUrl = cartItem.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_dialog_alert)
                    .into(holder.cartItemImage);
        } else {
            holder.cartItemImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Xử lý nút tăng số lượng
        holder.btnIncreaseQuantity.setOnClickListener(v -> {
            int newQuantity = cartItem.getQuantity() + 1;
            cartItem.setQuantity(newQuantity);
            if (cartManager.updateCartItemQuantity(cartItem)) {
                notifyItemChanged(position);
                if (cartChangeListener != null) {
                    cartChangeListener.onCartChanged();
                }
            } else {
                cartItem.setQuantity(newQuantity - 1);
                Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút giảm số lượng
        holder.btnDecreaseQuantity.setOnClickListener(v -> {
            if (cartItem.getQuantity() > 1) {
                int newQuantity = cartItem.getQuantity() - 1;
                cartItem.setQuantity(newQuantity);
                if (cartManager.updateCartItemQuantity(cartItem)) {
                    notifyItemChanged(position);
                    if (cartChangeListener != null) {
                        cartChangeListener.onCartChanged();
                    }
                } else {
                    cartItem.setQuantity(newQuantity + 1);
                    Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý nút xóa sản phẩm
        holder.btnDeleteItem.setOnClickListener(v -> {
            if (cartManager.deleteCartItem(cartItem.getCartId())) {
                int currentPosition = holder.getAdapterPosition();
                cartItems.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                if (cartChangeListener != null) {
                    cartChangeListener.onCartChanged();
                }
            } else {
                Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView cartItemImage;
        TextView cartItemName, cartItemPrice, cartItemQuantity;
        MaterialButton btnDecreaseQuantity, btnIncreaseQuantity, btnDeleteItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemImage = itemView.findViewById(R.id.cart_item_image);
            cartItemName = itemView.findViewById(R.id.cart_item_name);
            cartItemPrice = itemView.findViewById(R.id.cart_item_price);
            cartItemQuantity = itemView.findViewById(R.id.cart_item_quantity);
            btnDecreaseQuantity = itemView.findViewById(R.id.btn_decrease_quantity);
            btnIncreaseQuantity = itemView.findViewById(R.id.btn_increase_quantity);
            btnDeleteItem = itemView.findViewById(R.id.btn_delete_item);
        }
    }
}
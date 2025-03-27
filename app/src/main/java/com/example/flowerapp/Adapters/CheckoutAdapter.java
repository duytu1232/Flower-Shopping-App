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
        // Kiểm tra null và hiển thị thông tin
        holder.productName.setText(cartItem.getName() != null ? cartItem.getName() : "Không có tên");
        holder.productPrice.setText(String.format("Giá: %.2f VND", cartItem.getPrice() >= 0 ? cartItem.getPrice() : 0));
        holder.productQuantity.setText("Số lượng: " + (cartItem.getQuantity() >= 0 ? cartItem.getQuantity() : 0));
        holder.totalItemPrice.setText(String.format("Tổng: %.2f VND",
                (cartItem.getPrice() >= 0 && cartItem.getQuantity() >= 0) ? cartItem.getPrice() * cartItem.getQuantity() : 0));

        // Thêm mô tả và danh mục
        holder.productDescription.setText(cartItem.getDescription() != null ? "Mô tả: " + cartItem.getDescription() : "Mô tả: N/A");
        holder.productCategory.setText(cartItem.getCategory() != null ? "Danh mục: " + cartItem.getCategory() : "Danh mục: N/A");

        // Xử lý hiển thị ảnh
        String imageUrl = cartItem.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
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
                                .error(android.R.drawable.ic_dialog_alert)
                                .into(holder.productImage);
                    } else {
                        holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            } catch (Exception e) {
                holder.productImage.setImageResource(android.R.drawable.ic_dialog_alert);
            }
        } else {
            holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productQuantity, totalItemPrice, productDescription, productCategory;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.checkout_product_image);
            productName = itemView.findViewById(R.id.checkout_product_name);
            productPrice = itemView.findViewById(R.id.checkout_product_price);
            productQuantity = itemView.findViewById(R.id.checkout_product_quantity);
            totalItemPrice = itemView.findViewById(R.id.checkout_total_item_price);
            productDescription = itemView.findViewById(R.id.checkout_product_description);
            productCategory = itemView.findViewById(R.id.checkout_product_category);
        }
    }
}
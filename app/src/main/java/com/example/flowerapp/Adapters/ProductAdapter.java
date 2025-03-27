package com.example.flowerapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;
import com.example.flowerapp.User.Fragments.ProductDetail;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private static final String TAG = "ProductAdapter";
    private List<Product> productList;
    private Context context;
    private final OnProductActionListener actionListener;
    private final OnProductClickListener clickListener; // Thêm listener cho click
    private final int layoutResId;

    // Interface cho sự kiện click trên sản phẩm
    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnProductActionListener {
        void onEditProduct(Product product);
        void onDeleteProduct(int productId);
    }

    // Constructor cho giao diện người dùng (có click listener)
    public ProductAdapter(List<Product> productList, Context context, OnProductClickListener clickListener) {
        this.productList = productList;
        this.context = context;
        this.clickListener = clickListener;
        this.actionListener = null;
        this.layoutResId = R.layout.item_product_main;
    }

    // Constructor cho giao diện Admin (có action listener)
    public ProductAdapter(List<Product> productList, OnProductActionListener actionListener) {
        this.productList = productList;
        this.actionListener = actionListener;
        this.clickListener = null;
        this.layoutResId = R.layout.item_product_admin;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getName() != null ? product.getName() : "Unknown Product");
        holder.productPrice.setText(String.format("Giá: %.2f VND", product.getPrice()));

        if (layoutResId == R.layout.item_product_admin) {
            if (holder.productDescription != null) {
                holder.productDescription.setText(product.getDescription() != null ? "Mô tả: " + product.getDescription() : "Mô tả: N/A");
            }
            if (holder.productCategory != null) {
                holder.productCategory.setText(product.getCategory() != null ? "Danh mục: " + product.getCategory() : "Danh mục: N/A");
            }
        }

        // Hiển thị ảnh sản phẩm
        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("http")) {
                if (holder.productImage != null) {
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_dialog_alert)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                                    Log.e(TAG, "Failed to load image for product " + product.getId() + ": " + imageUrl + ", error: " + (e != null ? e.getMessage() : "Unknown error"));
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                    return false;
                                }
                            })
                            .into(holder.productImage);
                } else {
                    Log.e(TAG, "productImage is null for product " + product.getId());
                }
            } else {
                String resourceName = imageUrl.replace(".jpg", "");
                int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
                if (resourceId != 0) {
                    if (holder.productImage != null) {
                        Glide.with(context)
                                .load(resourceId)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_dialog_alert)
                                .into(holder.productImage);
                    } else {
                        Log.e(TAG, "productImage is null for product " + product.getId());
                    }
                } else {
                    Log.w(TAG, "Resource not found for product " + product.getId() + ": " + imageUrl);
                    if (holder.productImage != null) {
                        holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    } else {
                        Log.e(TAG, "productImage is null for product " + product.getId());
                    }
                }
            }
        } else {
            Log.w(TAG, "Image URL is null or empty for product " + product.getId());
            if (holder.productImage != null) {
                holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
            } else {
                Log.e(TAG, "productImage is null for product " + product.getId());
            }
        }

        // Thêm sự kiện click cho toàn bộ item (cho giao diện người dùng)
        if (clickListener != null) {
            holder.itemView.setOnClickListener(v -> clickListener.onProductClick(product));
        }

        // Xử lý nút Edit và Delete (cho giao diện Admin)
        if (holder.btnEdit != null && actionListener != null) {
            holder.btnEdit.setOnClickListener(v -> actionListener.onEditProduct(product));
        }
        if (holder.btnDelete != null && actionListener != null) {
            holder.btnDelete.setOnClickListener(v -> actionListener.onDeleteProduct(product.getId()));
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productDescription, productCategory;
        MaterialButton btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productDescription = itemView.findViewById(R.id.product_description);
            productCategory = itemView.findViewById(R.id.product_category);
            btnEdit = itemView.findViewById(R.id.btn_edit_product);
            btnDelete = itemView.findViewById(R.id.btn_delete_product);

            if (productImage == null) {
                Log.e("ProductViewHolder", "productImage is null! Check layout for ID: product_image");
            }
        }
    }
}
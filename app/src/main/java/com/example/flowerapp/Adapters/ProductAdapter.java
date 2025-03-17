package com.example.flowerapp.Adapters;

import android.content.Context;
import android.os.Bundle;
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
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;
import com.example.flowerapp.User.Fragments.ProductDetail;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.function.Consumer;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private Consumer<Product> onEditClick;
    private Consumer<Integer> onDeleteClick;
    private Context context;
    private int layoutId;

    // Constructor cho hiển thị (sử dụng item_product_main.xml)
    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
        this.onEditClick = null;
        this.onDeleteClick = null;
        this.layoutId = R.layout.item_product_main;
    }

    // Constructor cho quản lý (sử dụng item_product_admin.xml)
    public ProductAdapter(List<Product> productList, Consumer<Product> onEditClick, Consumer<Integer> onDeleteClick) {
        this.productList = productList;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
        this.context = null;
        this.layoutId = R.layout.item_product_admin;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.productPrice.setText("Giá: " + String.format("%.2f VND", product.getPrice()));

        if (holder.productImage != null) {
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Kiểm tra nếu imageUrl là đường dẫn trong assets
                if (imageUrl.startsWith("assets/") || imageUrl.contains("/")) {
                    // Loại bỏ "assets/" và sử dụng file:///android_asset/
                    String assetPath = "file:///android_asset/" + imageUrl.replace("assets/", "").replace("\\", "/");
                    Glide.with(holder.itemView.getContext())
                            .load(assetPath)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_dialog_alert)
                            .into(holder.productImage);
                } else {
                    // Giả sử là tên file trong drawable (loại bỏ đuôi .png/.jpg)
                    int resourceId = holder.itemView.getContext().getResources().getIdentifier(
                            imageUrl.replace(".png", "").replace(".jpg", ""), "drawable",
                            holder.itemView.getContext().getPackageName());
                    if (resourceId != 0) {
                        Glide.with(holder.itemView.getContext())
                                .load(resourceId)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .into(holder.productImage);
                    } else {
                        // Nếu không tìm thấy, hiển thị placeholder
                        holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                }
            } else {
                holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // Điều hướng đến ProductDetail khi nhấn vào sản phẩm (chỉ áp dụng cho chế độ hiển thị)
        if (onEditClick == null && onDeleteClick == null) {
            holder.itemView.setOnClickListener(v -> {
                ProductDetail productDetailFragment = new ProductDetail();
                Bundle args = new Bundle();
                args.putInt("product_id", product.getId());
                productDetailFragment.setArguments(args);

                FragmentManager fragmentManager = ((FragmentActivity) holder.itemView.getContext()).getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, productDetailFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            });
        }

        if (onEditClick == null || onDeleteClick == null) {
            if (holder.btnEditProduct != null) holder.btnEditProduct.setVisibility(View.GONE);
            if (holder.btnDeleteProduct != null) holder.btnDeleteProduct.setVisibility(View.GONE);
        } else {
            if (holder.btnEditProduct != null) {
                holder.btnEditProduct.setVisibility(View.VISIBLE);
                holder.btnEditProduct.setOnClickListener(v -> onEditClick.accept(product));
            }
            if (holder.btnDeleteProduct != null) {
                holder.btnDeleteProduct.setVisibility(View.VISIBLE);
                holder.btnDeleteProduct.setOnClickListener(v -> onDeleteClick.accept(product.getId()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;
        ImageView productImage;
        MaterialButton btnEditProduct, btnDeleteProduct;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productImage = itemView.findViewById(R.id.product_image);
            btnEditProduct = itemView.findViewById(R.id.btn_edit_product);
            btnDeleteProduct = itemView.findViewById(R.id.btn_delete_product);
        }
    }
}
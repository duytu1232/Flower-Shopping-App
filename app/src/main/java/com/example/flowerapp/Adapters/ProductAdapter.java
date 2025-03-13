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
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;
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
            String imageName = product.getImageUrl();
            if (imageName != null && !imageName.isEmpty()) {
                // Lấy ID tài nguyên từ tên hình ảnh
                int resourceId = holder.itemView.getContext().getResources().getIdentifier(
                        imageName, "drawable", holder.itemView.getContext().getPackageName());
                if (resourceId != 0) {
                    Glide.with(holder.itemView.getContext())
                            .load(resourceId)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(holder.productImage);
                } else {
                    // Nếu không tìm thấy tài nguyên, hiển thị placeholder
                    holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } else {
                holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
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
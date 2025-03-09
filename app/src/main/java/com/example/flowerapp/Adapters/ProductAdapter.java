package com.example.flowerapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;
    private Context context;

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList != null ? productList : new ArrayList<>();
        this.context = context;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_main, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        if (holder.nameTextView != null) {
            holder.nameTextView.setText(product.getName() != null ? product.getName() : "No Name");
        }
        if (holder.priceTextView != null) {
            holder.priceTextView.setText(String.format("$%.2f", product.getPrice()));
        }
        if (holder.imageView != null) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.shop) // Thay placeholder tạm thời
                    .error(R.drawable.shop)      // Thay error tạm thời
                    .into(holder.imageView);
        }

        // Tạm thời bỏ sự kiện click cho đến khi có ProductDetailActivity
        // holder.itemView.setOnClickListener(v -> {
        //     Intent intent = new Intent(context, ProductDetailActivity.class);
        //     intent.putExtra("product_id", product.getId());
        //     context.startActivity(intent);
        // });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView;
        ImageView imageView;

        public ProductViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.product_name);
            priceTextView = itemView.findViewById(R.id.product_price);
            imageView = itemView.findViewById(R.id.product_image);
        }
    }
}
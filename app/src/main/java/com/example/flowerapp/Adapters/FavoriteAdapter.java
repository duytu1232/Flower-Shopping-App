package com.example.flowerapp.Adapters;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {
    private final List<Product> favoriteList;
    private final Context context;
    private DatabaseHelper dbHelper;
    private OnFavoriteRemovedListener listener;

    public interface OnFavoriteRemovedListener {
        void onFavoriteListEmpty();
    }

    public FavoriteAdapter(List<Product> favoriteList, Context context, OnFavoriteRemovedListener listener) {
        this.favoriteList = favoriteList;
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_product, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Product product = favoriteList.get(position);
        holder.productName.setText(product.getName() != null ? product.getName() : "No Name");
        holder.productPrice.setText(String.format("$%.2f", product.getPrice()));
        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.shop)
                .error(R.drawable.shop)
                .into(holder.productImage);

        holder.removeFavorite.setOnClickListener(v -> {
            removeFavorite(product.getId(), position);
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice;
        ImageButton removeFavorite;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            removeFavorite = itemView.findViewById(R.id.remove_favorite);
        }
    }

    private void removeFavorite(int productId, int position) {
        SQLiteDatabase db = dbHelper.openDatabase();
        try {
            String whereClause = "product_id = ?";
            String[] whereArgs = {String.valueOf(productId)};
            int deletedRows = db.delete("Favorites", whereClause, whereArgs);

            if (deletedRows > 0) {
                favoriteList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, favoriteList.size());
                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();

                if (favoriteList.isEmpty() && listener != null) {
                    listener.onFavoriteListEmpty();
                }
            } else {
                Toast.makeText(context, "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("FavoriteAdapter", "Error removing favorite: " + e.getMessage());
            Toast.makeText(context, "Error occurred", Toast.LENGTH_SHORT).show();
        } finally {
            dbHelper.closeDatabase(db);
        }
    }
}
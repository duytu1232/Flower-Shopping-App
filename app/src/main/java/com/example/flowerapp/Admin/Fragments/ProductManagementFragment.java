package com.example.flowerapp.Admin.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        productList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_product_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProductAdapter(productList, requireContext(), this);
        recyclerView.setAdapter(adapter);

        loadProducts();

        Button addButton = view.findViewById(R.id.btn_add_product);
        addButton.setOnClickListener(v -> showAddProductDialog());

        return view;
    }

    private void loadProducts() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT product_id, name, price, stock, image_url FROM Products", null);
            productList.clear();

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));
                productList.add(new Product(id, name, price, stock, imageUrl));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Sản Phẩm");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_product, null);
        EditText editName = view.findViewById(R.id.edit_product_name);
        EditText editPrice = view.findViewById(R.id.edit_product_price);
        EditText editStock = view.findViewById(R.id.edit_product_stock);
        EditText editImageUrl = view.findViewById(R.id.edit_product_image_url);

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    double price = Double.parseDouble(editPrice.getText().toString().trim());
                    int stock = Integer.parseInt(editStock.getText().toString().trim());
                    String imageUrl = editImageUrl.getText().toString().trim();

                    addProduct(name, price, stock, imageUrl);
                    loadProducts();
                    Toast.makeText(requireContext(), "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void addProduct(String name, double price, int stock, String imageUrl) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Products (name, price, stock, image_url) VALUES (?, ?, ?, ?)",
                    new Object[]{name, price, stock, imageUrl});
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi thêm sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProduct(int id, String name, double price, int stock, String imageUrl) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("UPDATE Products SET name = ?, price = ?, stock = ?, image_url = ? WHERE product_id = ?",
                    new Object[]{name, price, stock, imageUrl, id});
            loadProducts();
            Toast.makeText(requireContext(), "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi cập nhật sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct(int id) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("DELETE FROM Products WHERE product_id = ?", new Object[]{id});
            loadProducts();
            Toast.makeText(requireContext(), "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static class Product {
        int id;
        String name;
        double price;
        int stock;
        String imageUrl;

        public Product(int id, String name, double price, int stock, String imageUrl) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.imageUrl = imageUrl;
        }
    }

    public static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private List<Product> products;
        private Context context;
        private ProductManagementFragment fragment;

        public ProductAdapter(List<Product> products, Context context, ProductManagementFragment fragment) {
            this.products = products;
            this.context = context;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_product_admin, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Product product = products.get(position);
            holder.nameTextView.setText(product.name);
            holder.priceTextView.setText("Price: $" + product.price);
            holder.quantityTextView.setText("Stock: " + product.stock);

            holder.btnEdit.setOnClickListener(v -> fragment.showEditProductDialog(product));
            holder.btnDelete.setOnClickListener(v -> fragment.deleteProduct(product.id));
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        public static class ProductViewHolder extends RecyclerView.ViewHolder {
            public TextView nameTextView, priceTextView, quantityTextView;
            public Button btnEdit, btnDelete;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.product_name);
                priceTextView = itemView.findViewById(R.id.product_price);
                quantityTextView = itemView.findViewById(R.id.product_quantity);
                btnEdit = itemView.findViewById(R.id.btn_edit_product);
                btnDelete = itemView.findViewById(R.id.btn_delete_product);
            }
        }
    }

    private void showEditProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sửa Sản Phẩm");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_product, null);
        EditText editName = view.findViewById(R.id.edit_product_name);
        EditText editPrice = view.findViewById(R.id.edit_product_price);
        EditText editStock = view.findViewById(R.id.edit_product_stock);
        EditText editImageUrl = view.findViewById(R.id.edit_product_image_url);

        editName.setText(product.name);
        editPrice.setText(String.valueOf(product.price));
        editStock.setText(String.valueOf(product.stock));
        editImageUrl.setText(product.imageUrl);

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    double price = Double.parseDouble(editPrice.getText().toString().trim());
                    int stock = Integer.parseInt(editStock.getText().toString().trim());
                    String imageUrl = editImageUrl.getText().toString().trim();

                    updateProduct(product.id, name, price, stock, imageUrl);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
package com.example.flowerapp.Admin.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Admin.Helper.AdminDatabaseHelper;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementFragment extends Fragment {
    private AdminDatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_management, container, false);

        dbHelper = new AdminDatabaseHelper(requireContext());
        productList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_product_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo adapter
        adapter = new ProductAdapter(productList, requireContext());
        recyclerView.setAdapter(adapter);

        // Load dữ liệu từ SQLite
        loadProducts();

        Button addButton = view.findViewById(R.id.btn_add_product);
        addButton.setOnClickListener(v -> {
            // TODO: Mở dialog hoặc activity để thêm sản phẩm
            addProduct("New Flower", 100.0, 10); // Ví dụ
            loadProducts(); // Cập nhật danh sách sau khi thêm
            Toast.makeText(requireContext(), "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void loadProducts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM products", null);
        productList.clear();

        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex("id");
            int nameIndex = cursor.getColumnIndex("name");
            int priceIndex = cursor.getColumnIndex("price");
            int quantityIndex = cursor.getColumnIndex("quantity");

            if (idIndex >= 0 && nameIndex >= 0 && priceIndex >= 0 && quantityIndex >= 0) {
                int id = cursor.getInt(idIndex);
                String name = cursor.getString(nameIndex);
                double price = cursor.getDouble(priceIndex);
                int quantity = cursor.getInt(quantityIndex);
                productList.add(new Product(id, name, price, quantity));
            }
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void addProduct(String name, double price, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("INSERT INTO products (name, price, quantity) VALUES (?, ?, ?)",
                new Object[]{name, price, quantity});
    }

    // Class Product (model)
    public static class Product {
        int id;
        String name;
        double price;
        int quantity;

        public Product(int id, String name, double price, int quantity) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }

    // Adapter (giả định đơn giản, cần mở rộng)
    public static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
        private List<Product> products;
        private Context context;

        public ProductAdapter(List<Product> products, Context context) {
            this.products = products;
            this.context = context;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            Product product = products.get(position);
            holder.textView.setText(product.name + " - $" + product.price + " (Qty: " + product.quantity + ")");
        }

        @Override
        public int getItemCount() {
            return products.size();
        }

        public static class ProductViewHolder extends RecyclerView.ViewHolder {
            public TextView textView;

            public ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
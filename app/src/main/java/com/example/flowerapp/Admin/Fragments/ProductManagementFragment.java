package com.example.flowerapp.Admin.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.ProductAdapter;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerView = view.findViewById(R.id.recycler_product_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProductAdapter(productList, this::showEditProductDialog, this::deleteProduct);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_add_product).setOnClickListener(v -> showAddProductDialog());

        loadProducts();
        return view;
    }

    private void loadProducts() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT * FROM Products", null);
            productList.clear();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                productList.add(new Product(id, name, description, price, stock, imageUrl, category));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
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
        EditText editDescription = view.findViewById(R.id.edit_product_description);
        EditText editCategory = view.findViewById(R.id.edit_product_category);

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    try {
                        String name = editName.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            editName.setError("Tên sản phẩm không được để trống");
                            return;
                        }
                        double price = Double.parseDouble(editPrice.getText().toString().trim());
                        if (price <= 0) {
                            editPrice.setError("Giá phải lớn hơn 0");
                            return;
                        }
                        int stock = Integer.parseInt(editStock.getText().toString().trim());
                        if (stock < 0) {
                            editStock.setError("Số lượng không được âm");
                            return;
                        }
                        String imageUrl = editImageUrl.getText().toString().trim();
                        String description = editDescription.getText().toString().trim();
                        String category = editCategory.getText().toString().trim();

                        addProduct(name, description, price, stock, imageUrl, category);
                        loadProducts();
                        Toast.makeText(requireContext(), "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sửa Sản Phẩm");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_product, null);
        EditText editName = view.findViewById(R.id.edit_product_name);
        EditText editPrice = view.findViewById(R.id.edit_product_price);
        EditText editStock = view.findViewById(R.id.edit_product_stock);
        EditText editImageUrl = view.findViewById(R.id.edit_product_image_url);
        EditText editDescription = view.findViewById(R.id.edit_product_description);
        EditText editCategory = view.findViewById(R.id.edit_product_category);

        editName.setText(product.getName());
        editPrice.setText(String.valueOf(product.getPrice()));
        editStock.setText(String.valueOf(product.getStock()));
        editImageUrl.setText(product.getImageUrl());
        editDescription.setText(product.getDescription());
        editCategory.setText(product.getCategory());

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    try {
                        String name = editName.getText().toString().trim();
                        if (TextUtils.isEmpty(name)) {
                            editName.setError("Tên sản phẩm không được để trống");
                            return;
                        }
                        double price = Double.parseDouble(editPrice.getText().toString().trim());
                        if (price <= 0) {
                            editPrice.setError("Giá phải lớn hơn 0");
                            return;
                        }
                        int stock = Integer.parseInt(editStock.getText().toString().trim());
                        if (stock < 0) {
                            editStock.setError("Số lượng không được âm");
                            return;
                        }
                        String imageUrl = editImageUrl.getText().toString().trim();
                        String description = editDescription.getText().toString().trim();
                        String category = editCategory.getText().toString().trim();

                        updateProduct(product.getId(), name, description, price, stock, imageUrl, category);
                        loadProducts();
                        Toast.makeText(requireContext(), "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void addProduct(String name, String description, double price, int stock, String imageUrl, String category) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Products (name, description, price, stock, image_url, category) VALUES (?, ?, ?, ?, ?, ?)",
                    new Object[]{name, description, price, stock, imageUrl, category});
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi thêm sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProduct(int id, String name, String description, double price, int stock, String imageUrl, String category) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("UPDATE Products SET name = ?, description = ?, price = ?, stock = ?, image_url = ?, category = ? WHERE product_id = ?",
                    new Object[]{name, description, price, stock, imageUrl, category, id});
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi cập nhật sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isProductReferenced(int productId) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Order_Items WHERE product_id = ?", new String[]{String.valueOf(productId)});
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi kiểm tra tham chiếu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private void deleteProduct(int id) {
        if (isProductReferenced(id)) {
            Toast.makeText(requireContext(), "Không thể xóa: Sản phẩm đang được sử dụng trong đơn hàng!", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.openDatabase()) {
                        db.execSQL("DELETE FROM Products WHERE product_id = ?", new Object[]{id});
                        Toast.makeText(requireContext(), "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Lỗi xóa sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
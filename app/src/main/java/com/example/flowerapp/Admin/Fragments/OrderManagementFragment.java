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

import com.example.flowerapp.Adapters.OrderAdapter;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class OrderManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerView = view.findViewById(R.id.recycler_order_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrderAdapter(orderList, this::showEditOrderDialog, this::deleteOrder);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_add_order).setOnClickListener(v -> showAddOrderDialog());

        loadOrders();
        return view;
    }

    private void loadOrders() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT * FROM Orders", null);
            orderList.clear();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String orderDate = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                String shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow("shipping_address"));
                // Giả định các trường title, imageResId, imageUrl có giá trị mặc định nếu không có trong DB
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title")) != null ? cursor.getString(cursor.getColumnIndexOrThrow("title")) : "";
                int imageResId = cursor.getInt(cursor.getColumnIndexOrThrow("image_res_id"));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url")) != null ? cursor.getString(cursor.getColumnIndexOrThrow("image_url")) : "";
                orderList.add(new Order(id, userId, orderDate, status, totalAmount, shippingAddress, title, imageResId, imageUrl));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Đơn Hàng");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null);
        EditText editUserId = view.findViewById(R.id.edit_order_user_id);
        EditText editOrderDate = view.findViewById(R.id.edit_order_date);
        EditText editStatus = view.findViewById(R.id.edit_order_status);
        EditText editTotal = view.findViewById(R.id.edit_order_total);
        EditText editAddress = view.findViewById(R.id.edit_order_address);

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    try {
                        int userId = Integer.parseInt(editUserId.getText().toString().trim());
                        String orderDate = editOrderDate.getText().toString().trim();
                        String status = editStatus.getText().toString().trim();
                        double totalAmount = Double.parseDouble(editTotal.getText().toString().trim());
                        String shippingAddress = editAddress.getText().toString().trim();

                        if (!isUserExists(userId)) {
                            editUserId.setError("User ID không tồn tại");
                            return;
                        }
                        if (TextUtils.isEmpty(orderDate) || TextUtils.isEmpty(status) || totalAmount <= 0) {
                            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin hợp lệ!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        addOrder(userId, orderDate, status, totalAmount, shippingAddress, "", 0, ""); // Giá trị mặc định cho title, imageResId, imageUrl
                        loadOrders();
                        Toast.makeText(requireContext(), "Thêm đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditOrderDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sửa Đơn Hàng");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null);
        EditText editUserId = view.findViewById(R.id.edit_order_user_id);
        EditText editOrderDate = view.findViewById(R.id.edit_order_date);
        EditText editStatus = view.findViewById(R.id.edit_order_status);
        EditText editTotal = view.findViewById(R.id.edit_order_total);
        EditText editAddress = view.findViewById(R.id.edit_order_address);

        editUserId.setText(String.valueOf(order.getUserId()));
        editOrderDate.setText(order.getOrderDate());
        editStatus.setText(order.getStatus());
        editTotal.setText(String.valueOf(order.getTotalAmount()));
        editAddress.setText(order.getShippingAddress());

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    try {
                        int userId = Integer.parseInt(editUserId.getText().toString().trim());
                        String orderDate = editOrderDate.getText().toString().trim();
                        String status = editStatus.getText().toString().trim();
                        double totalAmount = Double.parseDouble(editTotal.getText().toString().trim());
                        String shippingAddress = editAddress.getText().toString().trim();

                        if (!isUserExists(userId)) {
                            editUserId.setError("User ID không tồn tại");
                            return;
                        }
                        if (TextUtils.isEmpty(orderDate) || TextUtils.isEmpty(status) || totalAmount <= 0) {
                            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin hợp lệ!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        updateOrder(order.getId(), userId, orderDate, status, totalAmount, shippingAddress, order.getTitle(), order.getImageResId(), order.getImageUrl());
                        loadOrders();
                        Toast.makeText(requireContext(), "Cập nhật đơn hàng thành công", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean isUserExists(int userId) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Users WHERE user_id = ?", new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi kiểm tra user_id: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void addOrder(int userId, String orderDate, String status, double totalAmount, String shippingAddress, String title, int imageResId, String imageUrl) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Orders (user_id, order_date, status, total_amount, shipping_address, title, image_res_id, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress, title, imageResId, imageUrl});
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi thêm đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOrder(int id, int userId, String orderDate, String status, double totalAmount, String shippingAddress, String title, int imageResId, String imageUrl) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("UPDATE Orders SET user_id = ?, order_date = ?, status = ?, total_amount = ?, shipping_address = ?, title = ?, image_res_id = ?, image_url = ? WHERE order_id = ?",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress, title, imageResId, imageUrl, id});
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi cập nhật đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteOrder(int id) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đơn hàng này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.openDatabase()) {
                        db.execSQL("DELETE FROM Orders WHERE order_id = ?", new Object[]{id});
                        Toast.makeText(requireContext(), "Xóa đơn hàng thành công", Toast.LENGTH_SHORT).show();
                        loadOrders();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Lỗi xóa đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
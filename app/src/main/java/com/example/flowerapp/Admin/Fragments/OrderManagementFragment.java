package com.example.flowerapp.Admin.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.OrderAdapter;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderManagementFragment extends Fragment {
    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private static final String[] VALID_STATUSES = {"pending", "processing", "shipped", "delivered", "canceled"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Khởi tạo RecyclerView cho danh sách đơn hàng
        recyclerViewOrders = view.findViewById(R.id.recycler_order_list);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderAdapter = new OrderAdapter(orderList, this::showEditOrderDialog, this::deleteOrder);
        recyclerViewOrders.setAdapter(orderAdapter);

        view.findViewById(R.id.btn_add_order).setOnClickListener(v -> showAddOrderDialog());

        loadOrders();
        return view;
    }

    private void loadOrders() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery(
                    "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                            "p.name AS product_name, p.image_url " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                            "GROUP BY o.order_id", null);
            orderList.clear();
            Log.d("OrderManagement", "Số lượng bản ghi: " + cursor.getCount());
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String orderDate = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                String shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow("shipping_address"));
                String productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));

                productName = (productName != null) ? productName : "Unknown Product";
                imageUrl = (imageUrl != null) ? imageUrl : "";

                orderList.add(new Order(id, userId, orderDate, status, totalAmount, shippingAddress, productName, imageUrl));
                Log.d("OrderManagement", "Thêm đơn hàng: " + id);
            }
            cursor.close();
            orderAdapter.notifyDataSetChanged();
            recyclerViewOrders.scheduleLayoutAnimation();
            Log.d("OrderManagement", "Số lượng đơn hàng trong list: " + orderList.size());
        } catch (Exception e) {
            Log.e("OrderManagement", "Lỗi tải đơn hàng: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Đơn Hàng");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null);
        TextInputEditText editUserId = view.findViewById(R.id.edit_order_user_id);
        TextInputEditText editOrderDate = view.findViewById(R.id.edit_order_date);
        TextInputEditText editStatus = view.findViewById(R.id.edit_order_status);
        TextInputEditText editTotal = view.findViewById(R.id.edit_order_total);
        TextInputEditText editAddress = view.findViewById(R.id.edit_order_address);

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
                        if (TextUtils.isEmpty(orderDate) || !isValidDateFormat(orderDate)) {
                            editOrderDate.setError("Ngày đặt hàng không hợp lệ (định dạng: yyyy-MM-dd)");
                            return;
                        }
                        if (TextUtils.isEmpty(status) || !isValidStatus(status)) {
                            editStatus.setError("Trạng thái không hợp lệ (pending, processing, shipped, delivered, canceled)");
                            return;
                        }
                        if (totalAmount <= 0) {
                            editTotal.setError("Tổng tiền phải lớn hơn 0");
                            return;
                        }
                        if (TextUtils.isEmpty(shippingAddress)) {
                            editAddress.setError("Địa chỉ giao hàng không được để trống");
                            return;
                        }

                        addOrder(userId, orderDate, status, totalAmount, shippingAddress);
                        loadOrders();
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
        TextInputEditText editUserId = view.findViewById(R.id.edit_order_user_id);
        TextInputEditText editOrderDate = view.findViewById(R.id.edit_order_date);
        TextInputEditText editStatus = view.findViewById(R.id.edit_order_status);
        TextInputEditText editTotal = view.findViewById(R.id.edit_order_total);
        TextInputEditText editAddress = view.findViewById(R.id.edit_order_address);

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
                        if (TextUtils.isEmpty(orderDate) || !isValidDateFormat(orderDate)) {
                            editOrderDate.setError("Ngày đặt hàng không hợp lệ (định dạng: yyyy-MM-dd)");
                            return;
                        }
                        if (TextUtils.isEmpty(status) || !isValidStatus(status)) {
                            editStatus.setError("Trạng thái không hợp lệ (pending, processing, shipped, delivered, canceled)");
                            return;
                        }
                        if (totalAmount <= 0) {
                            editTotal.setError("Tổng tiền phải lớn hơn 0");
                            return;
                        }
                        if (TextUtils.isEmpty(shippingAddress)) {
                            editAddress.setError("Địa chỉ giao hàng không được để trống");
                            return;
                        }

                        updateOrder(order.getId(), userId, orderDate, status, totalAmount, shippingAddress);
                        loadOrders();
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
            Log.e("OrderManagement", "Lỗi kiểm tra user_id: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi kiểm tra user_id: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean isValidDateFormat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidStatus(String status) {
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    private void addOrder(int userId, String orderDate, String status, double totalAmount, String shippingAddress) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Orders (user_id, order_date, status, total_amount, shipping_address) VALUES (?, ?, ?, ?, ?)",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress});
            Log.d("OrderManagement", "Thêm đơn hàng thành công: " + userId);
            Toast.makeText(requireContext(), "Thêm đơn hàng thành công", Toast.LENGTH_SHORT).show();
        } catch (SQLiteConstraintException e) {
            Log.e("OrderManagement", "Lỗi ràng buộc: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi: Dữ liệu không hợp lệ (có thể thiếu thông tin bắt buộc)", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("OrderManagement", "Lỗi thêm đơn hàng: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi thêm đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateOrder(int id, int userId, String orderDate, String status, double totalAmount, String shippingAddress) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("UPDATE Orders SET user_id = ?, order_date = ?, status = ?, total_amount = ?, shipping_address = ? WHERE order_id = ?",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress, id});
            Log.d("OrderManagement", "Cập nhật đơn hàng thành công: " + id);
            Toast.makeText(requireContext(), "Cập nhật đơn hàng thành công", Toast.LENGTH_SHORT).show();
        } catch (SQLiteConstraintException e) {
            Log.e("OrderManagement", "Lỗi ràng buộc: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi: Dữ liệu không hợp lệ", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("OrderManagement", "Lỗi cập nhật đơn hàng: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi cập nhật đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteOrder(Order order) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đơn hàng này? Các thông tin liên quan (Order_Items, Payments, Notifications, Order_Tracking) cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.openDatabase()) {
                        db.execSQL("DELETE FROM Order_Items WHERE order_id = ?", new Object[]{order.getId()});
                        db.execSQL("DELETE FROM Payments WHERE order_id = ?", new Object[]{order.getId()});
                        db.execSQL("DELETE FROM Notifications WHERE order_id = ?", new Object[]{order.getId()});
                        db.execSQL("DELETE FROM Order_Tracking WHERE order_id = ?", new Object[]{order.getId()});
                        db.execSQL("DELETE FROM Orders WHERE order_id = ?", new Object[]{order.getId()});
                        Log.d("OrderManagement", "Xóa đơn hàng thành công: " + order.getId());
                        Toast.makeText(requireContext(), "Xóa đơn hàng thành công", Toast.LENGTH_SHORT).show();
                        loadOrders();
                    } catch (Exception e) {
                        Log.e("OrderManagement", "Lỗi xóa đơn hàng: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Lỗi xóa đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
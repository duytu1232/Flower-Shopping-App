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

public class OrderManagementFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        orderList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_order_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo adapter
        adapter = new OrderAdapter(orderList, requireContext(), this);
        recyclerView.setAdapter(adapter);

        // Load dữ liệu từ SQLite
        loadOrders();

        Button addButton = view.findViewById(R.id.btn_add_order);
        addButton.setOnClickListener(v -> showAddOrderDialog());

        return view;
    }

    private void loadOrders() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT order_id, user_id, order_date, status, total_amount, shipping_address FROM Orders", null);
            orderList.clear();

            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex("order_id");
                int userIdIndex = cursor.getColumnIndex("user_id");
                int dateIndex = cursor.getColumnIndex("order_date");
                int statusIndex = cursor.getColumnIndex("status");
                int totalIndex = cursor.getColumnIndex("total_amount");
                int addressIndex = cursor.getColumnIndex("shipping_address");

                if (idIndex >= 0 && userIdIndex >= 0 && dateIndex >= 0 && statusIndex >= 0 && totalIndex >= 0 && addressIndex >= 0) {
                    int id = cursor.getInt(idIndex);
                    int userId = cursor.getInt(userIdIndex);
                    String date = cursor.getString(dateIndex);
                    String status = cursor.getString(statusIndex);
                    double total = cursor.getDouble(totalIndex);
                    String address = cursor.getString(addressIndex);
                    orderList.add(new Order(id, userId, date, status, total, address));
                }
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Đơn Hàng");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null);
        EditText editUserId = view.findViewById(R.id.edit_order_user_id);
        EditText editDate = view.findViewById(R.id.edit_order_date);
        EditText editStatus = view.findViewById(R.id.edit_order_status);
        EditText editTotal = view.findViewById(R.id.edit_order_total);
        EditText editAddress = view.findViewById(R.id.edit_order_address);

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    int userId = Integer.parseInt(editUserId.getText().toString().trim());
                    String date = editDate.getText().toString().trim();
                    String status = editStatus.getText().toString().trim().toLowerCase();
                    double total = Double.parseDouble(editTotal.getText().toString().trim());
                    String address = editAddress.getText().toString().trim();

                    if (!status.matches("pending|processing|shipped|delivered|canceled")) {
                        Toast.makeText(requireContext(), "Status phải là 'pending', 'processing', 'shipped', 'delivered', hoặc 'canceled'", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addOrder(userId, date, status, total, address);
                    loadOrders();
                    Toast.makeText(requireContext(), "Thêm đơn hàng thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void addOrder(int userId, String orderDate, String status, double totalAmount, String shippingAddress) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Orders (user_id, order_date, status, total_amount, shipping_address) VALUES (?, ?, ?, ?, ?)",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress});
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi thêm đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOrder(int id, int userId, String orderDate, String status, double totalAmount, String shippingAddress) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("UPDATE Orders SET user_id = ?, order_date = ?, status = ?, total_amount = ?, shipping_address = ? WHERE order_id = ?",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress, id});
            loadOrders();
            Toast.makeText(requireContext(), "Cập nhật đơn hàng thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi cập nhật đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteOrder(int id) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("DELETE FROM Orders WHERE order_id = ?", new Object[]{id});
            loadOrders();
            Toast.makeText(requireContext(), "Xóa đơn hàng thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi xóa đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Class Order (model) - Thêm thuộc tính shippingAddress
    public static class Order {
        int id, userId;
        String orderDate, status, shippingAddress;
        double totalAmount;

        public Order(int id, int userId, String orderDate, String status, double totalAmount, String shippingAddress) {
            this.id = id;
            this.userId = userId;
            this.orderDate = orderDate;
            this.status = status;
            this.totalAmount = totalAmount;
            this.shippingAddress = shippingAddress;
        }
    }

    // Adapter (giữ nguyên, không cần thay đổi vì đã dùng Order model mới)
    public static class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
        private List<Order> orders;
        private Context context;
        private OrderManagementFragment fragment;

        public OrderAdapter(List<Order> orders, Context context, OrderManagementFragment fragment) {
            this.orders = orders;
            this.context = context;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_order_admin, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.orderIdTextView.setText("Order #" + order.id);
            holder.statusTextView.setText("Status: " + order.status);
            holder.detailsTextView.setText("User ID: " + order.userId + " | Total: $" + order.totalAmount + " | Date: " + order.orderDate + " | Address: " + order.shippingAddress);

            holder.btnEdit.setOnClickListener(v -> fragment.showEditOrderDialog(order));
            holder.btnDelete.setOnClickListener(v -> fragment.deleteOrder(order.id));
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        public static class OrderViewHolder extends RecyclerView.ViewHolder {
            public TextView orderIdTextView, statusTextView, detailsTextView;
            public Button btnEdit, btnDelete;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                orderIdTextView = itemView.findViewById(R.id.order_id);
                statusTextView = itemView.findViewById(R.id.order_status);
                detailsTextView = itemView.findViewById(R.id.order_details);
                btnEdit = itemView.findViewById(R.id.btn_edit_order);
                btnDelete = itemView.findViewById(R.id.btn_delete_order);
            }
        }
    }

    private void showEditOrderDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sửa Đơn Hàng");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null);
        EditText editUserId = view.findViewById(R.id.edit_order_user_id);
        EditText editDate = view.findViewById(R.id.edit_order_date);
        EditText editStatus = view.findViewById(R.id.edit_order_status);
        EditText editTotal = view.findViewById(R.id.edit_order_total);
        EditText editAddress = view.findViewById(R.id.edit_order_address);

        editUserId.setText(String.valueOf(order.userId));
        editDate.setText(order.orderDate);
        editStatus.setText(order.status);
        editTotal.setText(String.valueOf(order.totalAmount));
        editAddress.setText(order.shippingAddress);

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    int userId = Integer.parseInt(editUserId.getText().toString().trim());
                    String date = editDate.getText().toString().trim();
                    String status = editStatus.getText().toString().trim().toLowerCase();
                    double total = Double.parseDouble(editTotal.getText().toString().trim());
                    String address = editAddress.getText().toString().trim();

                    if (!status.matches("pending|processing|shipped|delivered|canceled")) {
                        Toast.makeText(requireContext(), "Status phải là 'pending', 'processing', 'shipped', 'delivered', hoặc 'canceled'", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateOrder(order.id, userId, date, status, total, address);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
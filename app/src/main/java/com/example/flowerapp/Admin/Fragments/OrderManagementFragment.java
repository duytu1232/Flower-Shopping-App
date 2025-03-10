package com.example.flowerapp.Admin.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.OrderDetailActivity;

import java.util.ArrayList;
import java.util.Calendar;
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

    // Thêm animation trong loadOrders, addOrder, updateOrder, deleteOrder
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
                    orderList.add(new Order(id, userId, date, status, total, address, "Order #" + id, 0, null));
                }
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation(); // Animation khi load
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

        // Thêm DatePicker cho ngày đặt
        editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        String date = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                        editDate.setText(date);
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    try {
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
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    private void addOrder(int userId, String orderDate, String status, double totalAmount, String shippingAddress) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Orders (user_id, order_date, status, total_amount, shipping_address) VALUES (?, ?, ?, ?, ?)",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress});
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.fall_down));
            loadOrders();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi thêm đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOrder(int id, int userId, String orderDate, String status, double totalAmount, String shippingAddress) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("UPDATE Orders SET user_id = ?, order_date = ?, status = ?, total_amount = ?, shipping_address = ? WHERE order_id = ?",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress, id});
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.fall_down));
            loadOrders();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi cập nhật đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteOrder(int id) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("DELETE FROM Orders WHERE order_id = ?", new Object[]{id});
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.fall_down));
            loadOrders();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi xóa đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Adapter
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

        // Thêm animation trong onBindViewHolder của OrderAdapter
        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.itemView.setAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
            holder.orderIdTextView.setText("Order #" + order.getId());
            holder.statusTextView.setText("Status: " + order.getStatus());
            holder.detailsTextView.setText("User ID: " + order.getUserId() + " | Total: $" + order.getTotalAmount()
                    + " | Date: " + order.getOrderDate() + " | Address: " + order.getShippingAddress());

            holder.btnEdit.setOnClickListener(v -> fragment.showEditOrderDialog(order));
            holder.btnDelete.setOnClickListener(v -> fragment.deleteOrder(order.getId()));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("order", order);
                context.startActivity(intent);
            });
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

        editUserId.setText(String.valueOf(order.getUserId()));
        editDate.setText(order.getOrderDate());
        editStatus.setText(order.getStatus());
        editTotal.setText(String.valueOf(order.getTotalAmount()));
        editAddress.setText(order.getShippingAddress());

        // Thêm DatePicker cho ngày đặt
        editDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        String date = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
                        editDate.setText(date);
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    try {
                        int userId = Integer.parseInt(editUserId.getText().toString().trim());
                        String date = editDate.getText().toString().trim();
                        String status = editStatus.getText().toString().trim().toLowerCase();
                        double total = Double.parseDouble(editTotal.getText().toString().trim());
                        String address = editAddress.getText().toString().trim();

                        if (!status.matches("pending|processing|shipped|delivered|canceled")) {
                            Toast.makeText(requireContext(), "Status phải là 'pending', 'processing', 'shipped', 'delivered', hoặc 'canceled'", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        updateOrder(order.getId(), userId, date, status, total, address);
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }
}
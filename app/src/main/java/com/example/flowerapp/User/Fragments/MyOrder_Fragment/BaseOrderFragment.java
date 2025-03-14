package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.OrderAdapter;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseOrderFragment extends Fragment {
    protected RecyclerView orderRecyclerView;
    protected OrderAdapter orderAdapter;
    protected List<Order> orderList;
    protected LinearLayout emptyLayout;
    protected DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);

        dbHelper = new DatabaseHelper(requireContext());
        orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
        emptyLayout = view.findViewById(R.id.empty_layout);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        loadOrdersFromDatabase();

        orderAdapter = new OrderAdapter(orderList); // Sử dụng constructor mới
        orderRecyclerView.setAdapter(orderAdapter);
        updateEmptyState();

        return view;
    }

    protected abstract int getLayoutId();

    protected abstract void loadOrdersFromDatabase();

    protected void loadOrdersWithStatus(String status) {
        orderList.clear();
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, o.product_name, p.image_url " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                            "WHERE o.status = ? LIMIT 1", new String[]{status});
            while (cursor.moveToNext()) {
                int orderIdIndex = cursor.getColumnIndex("order_id");
                int userIdIndex = cursor.getColumnIndex("user_id");
                int dateIndex = cursor.getColumnIndex("order_date");
                int statusIndex = cursor.getColumnIndex("status");
                int totalAmountIndex = cursor.getColumnIndex("total_amount");
                int shippingAddressIndex = cursor.getColumnIndex("shipping_address");
                int productNameIndex = cursor.getColumnIndex("product_name");
                int imageUrlIndex = cursor.getColumnIndex("image_url");

                int orderId = orderIdIndex >= 0 ? cursor.getInt(orderIdIndex) : 0;
                int userId = userIdIndex >= 0 ? cursor.getInt(userIdIndex) : 0;
                String orderDate = dateIndex >= 0 ? cursor.getString(dateIndex) : "";
                String orderStatus = statusIndex >= 0 ? cursor.getString(statusIndex) : "";
                double totalAmount = totalAmountIndex >= 0 ? cursor.getDouble(totalAmountIndex) : 0.0;
                String shippingAddress = shippingAddressIndex >= 0 ? cursor.getString(shippingAddressIndex) : "Not specified";
                String title = productNameIndex >= 0 ? cursor.getString(productNameIndex) : "Unknown Product";
                String imageUrl = imageUrlIndex >= 0 ? cursor.getString(imageUrlIndex) : "";
                int imageRes = R.drawable.order_base_line;

                orderList.add(new Order(orderId, userId, orderDate, orderStatus, totalAmount, shippingAddress, title, imageRes, imageUrl));
            }
            cursor.close();
            orderAdapter.notifyDataSetChanged();
            updateEmptyState();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                dbHelper.closeDatabase(db);
            }
        }
    }

    protected void loadOrdersWithReturnStatus(String status) {
        orderList.clear();
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, o.product_name, p.image_url " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                            "WHERE o.return_status = ? LIMIT 1", new String[]{status});
            while (cursor.moveToNext()) {
                int orderIdIndex = cursor.getColumnIndex("order_id");
                int userIdIndex = cursor.getColumnIndex("user_id");
                int dateIndex = cursor.getColumnIndex("order_date");
                int statusIndex = cursor.getColumnIndex("status");
                int totalAmountIndex = cursor.getColumnIndex("total_amount");
                int shippingAddressIndex = cursor.getColumnIndex("shipping_address");
                int productNameIndex = cursor.getColumnIndex("product_name");
                int imageUrlIndex = cursor.getColumnIndex("image_url");

                int orderId = orderIdIndex >= 0 ? cursor.getInt(orderIdIndex) : 0;
                int userId = userIdIndex >= 0 ? cursor.getInt(userIdIndex) : 0;
                String orderDate = dateIndex >= 0 ? cursor.getString(dateIndex) : "";
                String orderStatus = statusIndex >= 0 ? cursor.getString(statusIndex) : "";
                double totalAmount = totalAmountIndex >= 0 ? cursor.getDouble(totalAmountIndex) : 0.0;
                String shippingAddress = shippingAddressIndex >= 0 ? cursor.getString(shippingAddressIndex) : "Not specified";
                String title = productNameIndex >= 0 ? cursor.getString(productNameIndex) : "Unknown Product";
                String imageUrl = imageUrlIndex >= 0 ? cursor.getString(imageUrlIndex) : "";
                int imageRes = R.drawable.order_base_line;

                orderList.add(new Order(orderId, userId, orderDate, orderStatus, totalAmount, shippingAddress, title, imageRes, imageUrl));
            }
            cursor.close();
            orderAdapter.notifyDataSetChanged();
            updateEmptyState();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                dbHelper.closeDatabase(db);
            }
        }
    }

    protected void loadOrdersNotReviewed() {
        orderList.clear();
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, o.product_name, p.image_url " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                            "LEFT JOIN Reviews r ON o.order_id = r.order_id " +
                            "WHERE o.status = 'delivered' AND r.order_id IS NULL LIMIT 1", null);
            while (cursor.moveToNext()) {
                int orderIdIndex = cursor.getColumnIndex("order_id");
                int userIdIndex = cursor.getColumnIndex("user_id");
                int dateIndex = cursor.getColumnIndex("order_date");
                int statusIndex = cursor.getColumnIndex("status");
                int totalAmountIndex = cursor.getColumnIndex("total_amount");
                int shippingAddressIndex = cursor.getColumnIndex("shipping_address");
                int productNameIndex = cursor.getColumnIndex("product_name");
                int imageUrlIndex = cursor.getColumnIndex("image_url");

                int orderId = orderIdIndex >= 0 ? cursor.getInt(orderIdIndex) : 0;
                int userId = userIdIndex >= 0 ? cursor.getInt(userIdIndex) : 0;
                String orderDate = dateIndex >= 0 ? cursor.getString(dateIndex) : "";
                String orderStatus = statusIndex >= 0 ? cursor.getString(statusIndex) : "";
                double totalAmount = totalAmountIndex >= 0 ? cursor.getDouble(totalAmountIndex) : 0.0;
                String shippingAddress = shippingAddressIndex >= 0 ? cursor.getString(shippingAddressIndex) : "Not specified";
                String title = productNameIndex >= 0 ? cursor.getString(productNameIndex) : "Unknown Product";
                String imageUrl = imageUrlIndex >= 0 ? cursor.getString(imageUrlIndex) : "";
                int imageRes = R.drawable.order_base_line;

                orderList.add(new Order(orderId, userId, orderDate, orderStatus, totalAmount, shippingAddress, title, imageRes, imageUrl));
            }
            cursor.close();
            orderAdapter.notifyDataSetChanged();
            updateEmptyState();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                dbHelper.closeDatabase(db);
            }
        }
    }

    protected void updateEmptyState() {
        if (orderList == null || orderList.isEmpty()) {
            orderRecyclerView.setVisibility(View.GONE);
            if (emptyLayout != null) {
                emptyLayout.setVisibility(View.VISIBLE);
            }
        } else {
            orderRecyclerView.setVisibility(View.VISIBLE);
            if (emptyLayout != null) {
                emptyLayout.setVisibility(View.GONE);
            }
        }
    }

    public void updateOrders(List<Order> newOrders) {
        orderList.clear();
        if (newOrders != null) {
            orderList.addAll(newOrders);
        }
        orderAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.closeDatabase(dbHelper.openDatabase());
        }
    }
}
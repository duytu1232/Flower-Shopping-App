package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

        orderAdapter = new OrderAdapter(orderList);
        orderRecyclerView.setAdapter(orderAdapter);
        updateEmptyState();

        return view;
    }

    protected abstract int getLayoutId();

    protected abstract void loadOrdersFromDatabase();

    protected void loadOrdersWithStatus(String status) {
        orderList.clear();
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery(
                    "SELECT o.order_id, o.product_name, o.status, o.order_date, p.image_url " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                            "WHERE o.status = ? LIMIT 1", new String[]{status});
            while (cursor.moveToNext()) {
                int orderIdIndex = cursor.getColumnIndex("order_id");
                int productNameIndex = cursor.getColumnIndex("product_name");
                int statusIndex = cursor.getColumnIndex("status");
                int dateIndex = cursor.getColumnIndex("order_date");
                int imageUrlIndex = cursor.getColumnIndex("image_url");

                int orderId = orderIdIndex >= 0 ? cursor.getInt(orderIdIndex) : 0;
                String productName = productNameIndex >= 0 ? cursor.getString(productNameIndex) : "";
                String orderStatus = statusIndex >= 0 ? cursor.getString(statusIndex) : "";
                String date = dateIndex >= 0 ? cursor.getString(dateIndex) : "";
                String imageUrl = imageUrlIndex >= 0 ? cursor.getString(imageUrlIndex) : "";

                int imageRes = R.drawable.order_base_line; // Giữ làm placeholder
                orderList.add(new Order(orderId, productName, orderStatus, date, imageRes, imageUrl));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadOrdersWithReturnStatus(String status) {
        orderList.clear();
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery(
                    "SELECT o.order_id, o.product_name, o.status, o.order_date, p.image_url " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                            "WHERE o.return_status = ? LIMIT 1", new String[]{status});
            while (cursor.moveToNext()) {
                int orderIdIndex = cursor.getColumnIndex("order_id");
                int productNameIndex = cursor.getColumnIndex("product_name");
                int statusIndex = cursor.getColumnIndex("status");
                int dateIndex = cursor.getColumnIndex("order_date");
                int imageUrlIndex = cursor.getColumnIndex("image_url");

                int orderId = orderIdIndex >= 0 ? cursor.getInt(orderIdIndex) : 0;
                String productName = productNameIndex >= 0 ? cursor.getString(productNameIndex) : "";
                String orderStatus = statusIndex >= 0 ? cursor.getString(statusIndex) : "";
                String date = dateIndex >= 0 ? cursor.getString(dateIndex) : "";
                String imageUrl = imageUrlIndex >= 0 ? cursor.getString(imageUrlIndex) : "";

                int imageRes = R.drawable.order_base_line;
                orderList.add(new Order(orderId, productName, orderStatus, date, imageRes, imageUrl));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadOrdersNotReviewed() {
        orderList.clear();
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery(
                    "SELECT o.order_id, o.product_name, o.status, o.order_date, p.image_url " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                            "LEFT JOIN Reviews r ON o.order_id = r.order_id " +
                            "WHERE o.status = 'delivered' AND r.order_id IS NULL LIMIT 1", null);
            while (cursor.moveToNext()) {
                int orderIdIndex = cursor.getColumnIndex("order_id");
                int productNameIndex = cursor.getColumnIndex("product_name");
                int statusIndex = cursor.getColumnIndex("status");
                int dateIndex = cursor.getColumnIndex("order_date");
                int imageUrlIndex = cursor.getColumnIndex("image_url");

                int orderId = orderIdIndex >= 0 ? cursor.getInt(orderIdIndex) : 0;
                String productName = productNameIndex >= 0 ? cursor.getString(productNameIndex) : "";
                String orderStatus = statusIndex >= 0 ? cursor.getString(statusIndex) : "";
                String date = dateIndex >= 0 ? cursor.getString(dateIndex) : "";
                String imageUrl = imageUrlIndex >= 0 ? cursor.getString(imageUrlIndex) : "";

                int imageRes = R.drawable.order_base_line;
                orderList.add(new Order(orderId, productName, orderStatus, date, imageRes, imageUrl));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
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
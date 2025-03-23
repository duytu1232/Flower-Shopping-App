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

public class OrderFragment extends Fragment {
    private static final String ARG_TYPE = "type";
    private String type;
    private RecyclerView orderRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private LinearLayout emptyLayout;
    private DatabaseHelper dbHelper;

    public static OrderFragment newInstance(String type) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
        emptyLayout = view.findViewById(R.id.empty_layout);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        loadOrders();

        orderAdapter = new OrderAdapter(orderList, this::onReviewClick);
        orderRecyclerView.setAdapter(orderAdapter);
        updateEmptyState();

        return view;
    }

    private void loadOrders() {
        orderList.clear();
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            String query;
            String[] selectionArgs;

            // Lấy user_id từ SharedPreferences
            int userId = requireContext().getSharedPreferences("MyPrefs", requireContext().MODE_PRIVATE)
                    .getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("not_reviewed".equals(type)) {
                query = "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                        "p.name AS product_name, p.image_url " +
                        "FROM Orders o " +
                        "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                        "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                        "LEFT JOIN Reviews r ON o.order_id = r.order_id " +
                        "WHERE o.status = 'delivered' AND r.order_id IS NULL AND o.user_id = ?";
                selectionArgs = new String[]{String.valueOf(userId)};
            } else if ("returned".equals(type)) {
                query = "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                        "p.name AS product_name, p.image_url " +
                        "FROM Orders o " +
                        "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                        "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                        "WHERE o.return_status = ? AND o.user_id = ?";
                selectionArgs = new String[]{"returned", String.valueOf(userId)};
            } else {
                query = "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                        "p.name AS product_name, p.image_url " +
                        "FROM Orders o " +
                        "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                        "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                        "WHERE o.status = ? AND o.user_id = ?";
                selectionArgs = new String[]{type, String.valueOf(userId)};
            }

            Cursor cursor = db.rawQuery(query, selectionArgs);
            while (cursor.moveToNext()) {
                int orderId = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                int userIdFromDb = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String orderDate = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                String orderStatus = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                String shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow("shipping_address"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));

                title = (title != null) ? title : "Unknown Product";
                imageUrl = (imageUrl != null) ? imageUrl : "";

                orderList.add(new Order(orderId, userIdFromDb, orderDate, orderStatus, totalAmount, shippingAddress, title, imageUrl));
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

    private void updateEmptyState() {
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

    private void onReviewClick(Order order) {
        Toast.makeText(requireContext(), "Review clicked for order: " + order.getId(), Toast.LENGTH_SHORT).show();
        // Thêm logic chuyển sang màn hình đánh giá (nếu cần)
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.closeDatabase(dbHelper.openDatabase());
        }
    }
}
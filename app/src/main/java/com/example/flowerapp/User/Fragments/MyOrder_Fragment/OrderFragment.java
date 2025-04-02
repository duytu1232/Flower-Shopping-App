package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import android.content.Intent;
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
import com.example.flowerapp.Models.OrderItem;
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
            int userId = requireContext().getSharedPreferences("MyPrefs", requireContext().MODE_PRIVATE)
                    .getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String query;
            String[] selectionArgs;
            switch (type) {
                case "pending":
                    query = "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                            "o.shipping_method, pm.payment_method " +
                            "FROM Orders o " +
                            "LEFT JOIN Payments pm ON o.order_id = pm.order_id " +
                            "WHERE o.status = ? AND o.user_id = ?";
                    selectionArgs = new String[]{"pending", String.valueOf(userId)};
                    break;
                case "shipped":
                    query = "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                            "o.shipping_method, pm.payment_method " +
                            "FROM Orders o " +
                            "LEFT JOIN Payments pm ON o.order_id = pm.order_id " +
                            "WHERE o.status IN ('processing', 'shipped') AND o.user_id = ?";
                    selectionArgs = new String[]{String.valueOf(userId)};
                    break;
                case "delivered":
                    query = "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                            "o.shipping_method, pm.payment_method " +
                            "FROM Orders o " +
                            "LEFT JOIN Payments pm ON o.order_id = pm.order_id " +
                            "WHERE o.status = ? AND o.user_id = ?";
                    selectionArgs = new String[]{"delivered", String.valueOf(userId)};
                    break;
                case "not_reviewed":
                    query = "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                            "o.shipping_method, pm.payment_method " +
                            "FROM Orders o " +
                            "LEFT JOIN Payments pm ON o.order_id = pm.order_id " +
                            "LEFT JOIN Reviews r ON o.order_id = r.order_id " +
                            "WHERE o.status = 'delivered' AND r.order_id IS NULL AND o.user_id = ?";
                    selectionArgs = new String[]{String.valueOf(userId)};
                    break;
                case "returned":
                    query = "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                            "o.shipping_method, pm.payment_method " +
                            "FROM Orders o " +
                            "LEFT JOIN Payments pm ON o.order_id = pm.order_id " +
                            "WHERE o.status = 'canceled' AND o.user_id = ?";
                    selectionArgs = new String[]{String.valueOf(userId)};
                    break;
                default:
                    query = "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                            "o.shipping_method, pm.payment_method " +
                            "FROM Orders o " +
                            "LEFT JOIN Payments pm ON o.order_id = pm.order_id " +
                            "WHERE o.user_id = ?";
                    selectionArgs = new String[]{String.valueOf(userId)};
                    break;
            }

            Cursor cursor = db.rawQuery(query, selectionArgs);
            while (cursor.moveToNext()) {
                int orderId = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                int userIdFromDb = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String orderDate = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                String orderStatus = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                String shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow("shipping_address"));
                String shippingMethod = cursor.getString(cursor.getColumnIndexOrThrow("shipping_method"));
                String paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow("payment_method"));

                List<OrderItem> orderItems = new ArrayList<>();
                Cursor itemCursor = db.rawQuery(
                        "SELECT oi.order_item_id, oi.product_id, p.name, p.image_url, oi.quantity, oi.unit_price " +
                                "FROM Order_Items oi " +
                                "JOIN Products p ON oi.product_id = p.product_id " +
                                "WHERE oi.order_id = ?", new String[]{String.valueOf(orderId)});
                while (itemCursor.moveToNext()) {
                    int orderItemId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("order_item_id"));
                    int productId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("product_id"));
                    String productName = itemCursor.getString(itemCursor.getColumnIndexOrThrow("name"));
                    String imageUrl = itemCursor.getString(itemCursor.getColumnIndexOrThrow("image_url"));
                    int quantity = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("quantity"));
                    double unitPrice = itemCursor.getDouble(itemCursor.getColumnIndexOrThrow("unit_price"));
                    orderItems.add(new OrderItem(orderItemId, orderId, productId, productName, imageUrl, quantity, unitPrice));
                }
                itemCursor.close();

                shippingMethod = shippingMethod != null ? formatShippingMethod(shippingMethod) : "Not specified";
                paymentMethod = paymentMethod != null ? formatPaymentMethod(paymentMethod) : "Not specified";
                String title = orderItems.isEmpty() ? "Unknown Product" : orderItems.get(0).getProductName();
                String imageUrl = orderItems.isEmpty() ? "" : orderItems.get(0).getImageUrl();

                orderList.add(new Order(orderId, userIdFromDb, orderDate, orderStatus, totalAmount, shippingAddress, shippingMethod, paymentMethod, title, imageUrl, orderItems));
            }
            cursor.close();
            orderAdapter.notifyDataSetChanged();
            updateEmptyState();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private String formatShippingMethod(String shippingMethod) {
        switch (shippingMethod) {
            case "home_delivery": return "Home Delivery";
            case "pickup_point": return "Pickup Point";
            case "pickup_in_store": return "Pickup in Store";
            default: return shippingMethod;
        }
    }

    private String formatPaymentMethod(String paymentMethod) {
        switch (paymentMethod) {
            case "credit_card": return "Credit Card";
            case "momo": return "Momo";
            case "cod": return "Cash on Delivery";
            default: return paymentMethod;
        }
    }

    private void updateEmptyState() {
        if (orderList == null || orderList.isEmpty()) {
            orderRecyclerView.setVisibility(View.GONE);
            if (emptyLayout != null) emptyLayout.setVisibility(View.VISIBLE);
        } else {
            orderRecyclerView.setVisibility(View.VISIBLE);
            if (emptyLayout != null) emptyLayout.setVisibility(View.GONE);
        }
    }

    private void onReviewClick(Order order) {
        Toast.makeText(requireContext(), "Review clicked for order: " + order.getId(), Toast.LENGTH_SHORT).show();
        // Logic chuyển sang màn hình đánh giá nếu cần
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
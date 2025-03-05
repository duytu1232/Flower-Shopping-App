package com.example.flowerapp.User.Fragments;

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

public class ChoThanhToanFragment extends Fragment {

    private RecyclerView orderRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private LinearLayout emptyLayout;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cho_thanh_toan, container, false);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(requireContext());

        // Khởi tạo các view
        orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
        emptyLayout = view.findViewById(R.id.empty_layout);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách đơn hàng
        orderList = new ArrayList<>();

        // Lấy dữ liệu từ cơ sở dữ liệu (ví dụ)
        loadOrdersFromDatabase("Chờ thanh toán");

        // Khởi tạo và gắn adapter
        orderAdapter = new OrderAdapter(orderList);
        orderRecyclerView.setAdapter(orderAdapter);

        // Kiểm tra và hiển thị layout trống nếu danh sách rỗng
        updateEmptyState();

        return view;
    }

    private void loadOrdersFromDatabase(String status) {
        orderList.clear();
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT * FROM Orders WHERE status = ?", new String[]{status});
            while (cursor.moveToNext()) {
                // Kiểm tra từng cột trước khi truy xuất
                int productNameIndex = cursor.getColumnIndex("product_name");
                int statusIndex = cursor.getColumnIndex("status");
                int dateIndex = cursor.getColumnIndex("order_date");

                String productName = "";
                String orderStatus = "";
                String date = "";

                // Gán giá trị nếu cột tồn tại
                if (productNameIndex >= 0) {
                    productName = cursor.getString(productNameIndex);
                }
                if (statusIndex >= 0) {
                    orderStatus = cursor.getString(statusIndex);
                }
                if (dateIndex >= 0) {
                    date = cursor.getString(dateIndex);
                }

                // Giả định imageRes là cố định (nếu cần, bạn có thể thêm cột cho ảnh trong DB)
                int imageRes = R.drawable.order_base_line;
                orderList.add(new Order(productName, orderStatus, date, imageRes));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateEmptyState() {
        if (orderList == null || orderList.isEmpty()) {
            orderRecyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            orderRecyclerView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
    }

    // Phương thức để cập nhật dữ liệu (nếu cần thêm đơn hàng hoặc xóa)
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
            dbHelper.closeDatabase(dbHelper.openDatabase()); // Đóng cơ sở dữ liệu
        }
    }
}
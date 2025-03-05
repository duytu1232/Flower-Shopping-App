package com.example.flowerapp.User.Fragments;

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

import java.util.ArrayList;
import java.util.List;

public class ChoGiaoHangFragment extends Fragment {

    private RecyclerView orderRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private LinearLayout emptyLayout;  // Thêm tham chiếu đến layout trống

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cho_giao_hang, container, false);

        // Khởi tạo các view
        orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
        emptyLayout = view.findViewById(R.id.empty_layout);  // Khởi tạo layout trống
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách đơn hàng (lấy từ DB hoặc API trong thực tế)
        orderList = new ArrayList<>();

        // Khởi tạo và gắn adapter
        orderAdapter = new OrderAdapter(orderList);
        orderRecyclerView.setAdapter(orderAdapter);

        // Kiểm tra và hiển thị layout trống nếu danh sách rỗng
        updateEmptyState();

        return view;
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
}
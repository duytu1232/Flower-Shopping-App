package com.example.flowerapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cho_giao_hang, container, false);

        // Khởi tạo RecyclerView
        orderRecyclerView = view.findViewById(R.id.orderRecyclerView);
        orderRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo dữ liệu mẫu
        orderList = new ArrayList<>();
        // Thêm dữ liệu đơn hàng vào orderList (ví dụ)
        orderList.add(new Order("Hoa hồng", "Chờ giao hàng", "20/02/2025", R.drawable.order_base_line));
        // ... Thêm các đơn hàng khác

        // Khởi tạo và gắn adapter
        orderAdapter = new OrderAdapter(orderList);
        orderRecyclerView.setAdapter(orderAdapter);

        return view;
    }
}
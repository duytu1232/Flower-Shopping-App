package com.example.flowerapp.Admin.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.RevenueAdapter;  // Cập nhật package nếu cần
import com.example.flowerapp.Models.Revenue;  // Cập nhật package nếu cần
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class RevenueManagementFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView tvRevenueSummary;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_revenue_management, container, false);

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(requireContext());

        // Ánh xạ các view
        tvRevenueSummary = view.findViewById(R.id.tv_revenue_summary);
        recyclerView = view.findViewById(R.id.recycler_revenue_list);
        progressBar = view.findViewById(R.id.progress_bar);
        Spinner spinnerFilterTime = view.findViewById(R.id.spinner_filter_time);
        Button btnRefresh = view.findViewById(R.id.btn_refresh);

        // Cấu hình RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Thêm sự kiện cho Spinner và Button
        spinnerFilterTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String timePeriod = parent.getItemAtPosition(position).toString();
                loadRevenueSummary(timePeriod); // Gọi lại với tham số thời gian
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì
            }
        });

        btnRefresh.setOnClickListener(v -> {
            String timePeriod = spinnerFilterTime.getSelectedItem().toString();
            loadRevenueSummary(timePeriod); // Làm mới dữ liệu
        });

        // Tải dữ liệu ban đầu
        loadRevenueSummary(spinnerFilterTime.getSelectedItem().toString());

        return view;
    }

    private void loadRevenueSummary(String timePeriod) {
        if (dbHelper == null || getContext() == null) {
            Log.e("RevenueManagementFragment", "DatabaseHelper hoặc Context là null");
            return;
        }

        // Hiển thị ProgressBar
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (tvRevenueSummary != null) {
            tvRevenueSummary.setText("Tổng doanh thu: Đang tải...");
        }

        SQLiteDatabase db = dbHelper.openDatabase();
        if (db == null) {
            Log.e("RevenueManagementFragment", "Không thể mở cơ sở dữ liệu");
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            return;
        }

        float totalRevenue = 0;
        List<Revenue> revenueList = new ArrayList<>();
        String query = "SELECT payment_method, amount, payment_date FROM Payments WHERE status = 'success'";

        // Thêm điều kiện lọc theo thời gian
        if ("Hôm nay".equals(timePeriod)) {
            query += " AND date(payment_date) = date('now')";
        } else if ("Tuần này".equals(timePeriod)) {
            query += " AND strftime('%Y-%W', payment_date) = strftime('%Y-%W', 'now')";
        } else if ("Tháng này".equals(timePeriod)) {
            query += " AND strftime('%Y-%m', payment_date) = strftime('%Y-%m', 'now')";
        } else if ("Năm này".equals(timePeriod)) {
            query += " AND strftime('%Y', payment_date) = strftime('%Y', 'now')";
        }

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                int paymentMethodIndex = cursor.getColumnIndex("payment_method");
                int amountIndex = cursor.getColumnIndex("amount");
                int paymentDateIndex = cursor.getColumnIndex("payment_date");

                if (paymentMethodIndex == -1 || amountIndex == -1 || paymentDateIndex == -1) {
                    Log.e("RevenueManagementFragment", "Cột 'payment_method', 'amount', hoặc 'payment_date' không tồn tại trong kết quả truy vấn");
                } else {
                    do {
                        String method = cursor.getString(paymentMethodIndex);
                        float amount = cursor.getFloat(amountIndex);
                        String date = cursor.getString(paymentDateIndex);

                        totalRevenue += amount;
                        revenueList.add(new Revenue(method, amount, date));
                    } while (cursor.moveToNext());
                }
            } else {
                Log.w("RevenueManagementFragment", "Không có dữ liệu doanh thu từ bảng Payments");
            }
        } catch (Exception e) {
            Log.e("RevenueManagementFragment", "Lỗi truy vấn cơ sở dữ liệu: " + e.getMessage());
        }

        // Ẩn ProgressBar
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        // Hiển thị tổng doanh thu
        if (tvRevenueSummary != null) {
            if (totalRevenue > 0) {
                tvRevenueSummary.setText(String.format("Tổng doanh thu: %.2f VND", totalRevenue));
            } else {
                tvRevenueSummary.setText("Tổng doanh thu: 0 VND");
            }
        } else {
            Log.e("RevenueManagementFragment", "tvRevenueSummary là null");
        }

        // Cập nhật RecyclerView với danh sách doanh thu
        if (recyclerView != null) {
            RevenueAdapter adapter = new RevenueAdapter(revenueList);
            recyclerView.setAdapter(adapter);
        } else {
            Log.e("RevenueManagementFragment", "recyclerView là null");
        }

        dbHelper.closeDatabase(db);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.closeDatabase(dbHelper.getReadableDatabase());
        }
    }
}
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.RevenueAdapter;
import com.example.flowerapp.Models.Revenue;
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

        dbHelper = new DatabaseHelper(requireContext());

        tvRevenueSummary = view.findViewById(R.id.tv_revenue_summary);
        recyclerView = view.findViewById(R.id.recycler_revenue_list);
        progressBar = view.findViewById(R.id.progress_bar);
        Spinner spinnerFilterTime = view.findViewById(R.id.spinner_filter_time);
        Button btnRefresh = view.findViewById(R.id.btn_refresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        spinnerFilterTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String timePeriod = parent.getItemAtPosition(position).toString();
                loadRevenueSummary(timePeriod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnRefresh.setOnClickListener(v -> {
            String timePeriod = spinnerFilterTime.getSelectedItem().toString();
            loadRevenueSummary(timePeriod);
        });

        loadRevenueSummary(spinnerFilterTime.getSelectedItem().toString());

        return view;
    }

    private void loadRevenueSummary(String timePeriod) {
        if (dbHelper == null || getContext() == null) {
            Log.e("RevenueManagement", "DatabaseHelper hoặc Context là null");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvRevenueSummary.setText("Tổng doanh thu: Đang tải...");

        SQLiteDatabase db = dbHelper.openDatabase();
        if (db == null) {
            Log.e("RevenueManagement", "Không thể mở cơ sở dữ liệu");
            progressBar.setVisibility(View.GONE);
            return;
        }

        float totalRevenue = 0;
        List<Revenue> revenueList = new ArrayList<>();
        String query = "SELECT payment_method, amount, payment_date FROM Payments WHERE status = 'success'";

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
            Log.d("RevenueManagement", "Số lượng bản ghi: " + cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    String method = cursor.getString(cursor.getColumnIndexOrThrow("payment_method"));
                    float amount = cursor.getFloat(cursor.getColumnIndexOrThrow("amount"));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow("payment_date"));

                    totalRevenue += amount;
                    revenueList.add(new Revenue(method, amount, date));
                    Log.d("RevenueManagement", "Thêm doanh thu: " + method + ", " + amount);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("RevenueManagement", "Lỗi truy vấn: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi tải doanh thu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        progressBar.setVisibility(View.GONE);
        tvRevenueSummary.setText(totalRevenue > 0 ? String.format("Tổng doanh thu: %.2f VND", totalRevenue) : "Tổng doanh thu: 0 VND");

        if (!revenueList.isEmpty()) {
            RevenueAdapter adapter = new RevenueAdapter(revenueList);
            recyclerView.setAdapter(adapter);
            recyclerView.scheduleLayoutAnimation();
        } else {
            recyclerView.setAdapter(null);
            Log.d("RevenueManagement", "Không có dữ liệu doanh thu");
        }

        dbHelper.closeDatabase(db);
    }
}
package com.example.flowerapp.Admin.Fragments;

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
    private RevenueAdapter adapter;
    private List<Revenue> revenueList = new ArrayList<>();

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
        adapter = new RevenueAdapter(revenueList);
        recyclerView.setAdapter(adapter);

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

        try {
            revenueList.clear();
            revenueList.addAll(dbHelper.getRevenueByPeriod(timePeriod));
            float totalRevenue = 0;
            for (Revenue revenue : revenueList) {
                totalRevenue += revenue.getAmount();
            }
            progressBar.setVisibility(View.GONE);
            tvRevenueSummary.setText(totalRevenue > 0 ? String.format("Tổng doanh thu: %.2f VND", totalRevenue) : "Tổng doanh thu: 0 VND");
            adapter.notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
            if (revenueList.isEmpty()) {
                Toast.makeText(requireContext(), "Không có dữ liệu doanh thu", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("RevenueManagement", "Lỗi tải doanh thu: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi tải doanh thu: " + e.getMessage(), Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            tvRevenueSummary.setText("Tổng doanh thu: 0 VND");
        }
    }
}
package com.example.flowerapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Models.Revenue;
import com.example.flowerapp.R;

import java.util.List;

public class RevenueAdapter extends RecyclerView.Adapter<RevenueAdapter.RevenueViewHolder> {
    private List<Revenue> revenueList;

    public RevenueAdapter(List<Revenue> revenueList) {
        this.revenueList = revenueList;
    }

    @NonNull
    @Override
    public RevenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_revenue, parent, false);
        return new RevenueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RevenueViewHolder holder, int position) {
        Revenue revenue = revenueList.get(position);
        holder.tvPaymentMethod.setText("Phương thức: " + revenue.getPaymentMethod());
        holder.tvAmount.setText("Số tiền: " + String.format("%,.0f VND", revenue.getAmount())); // Sửa định dạng
        holder.tvPaymentDate.setText("Ngày: " + revenue.getPaymentDate());
    }

    @Override
    public int getItemCount() {
        return revenueList.size();
    }

    public static class RevenueViewHolder extends RecyclerView.ViewHolder {
        TextView tvPaymentMethod, tvAmount, tvPaymentDate;

        public RevenueViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvPaymentDate = itemView.findViewById(R.id.tv_payment_date);
        }
    }
}
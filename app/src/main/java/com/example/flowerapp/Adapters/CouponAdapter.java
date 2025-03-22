package com.example.flowerapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Models.Coupon;
import com.example.flowerapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {
    private List<Coupon> couponList;
    private final OnEditClickListener editClickListener;
    private final OnDeleteClickListener deleteClickListener;

    // Interface cho sự kiện click
    public interface OnEditClickListener {
        void onEditClick(Coupon coupon);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int couponId);
    }

    public CouponAdapter(List<Coupon> couponList, OnEditClickListener editClickListener, OnDeleteClickListener deleteClickListener) {
        this.couponList = couponList;
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coupon_admin, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Coupon coupon = couponList.get(position);
        holder.couponCode.setText(coupon.getCode());
        holder.couponValue.setText(String.format("Giảm giá: %.2f%%", coupon.getDiscountValue()));
        holder.couponPeriod.setText(String.format("Thời gian: %s - %s | Trạng thái: %s | Tối thiểu: %.0f VND",
                coupon.getStartDate(), coupon.getEndDate(), coupon.getStatus(), coupon.getMinOrderValue()));

        holder.btnEdit.setOnClickListener(v -> editClickListener.onEditClick(coupon));
        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(coupon.getId()));
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }

    public void updateData(List<Coupon> newCouponList) {
        this.couponList = newCouponList;
        notifyDataSetChanged();
    }

    static class CouponViewHolder extends RecyclerView.ViewHolder {
        TextView couponCode, couponValue, couponPeriod;
        MaterialButton btnEdit, btnDelete;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            couponCode = itemView.findViewById(R.id.coupon_code);
            couponValue = itemView.findViewById(R.id.coupon_value);
            couponPeriod = itemView.findViewById(R.id.coupon_period);
            btnEdit = itemView.findViewById(R.id.btn_edit_coupon);
            btnDelete = itemView.findViewById(R.id.btn_delete_coupon);
        }
    }
}
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

public class UserCouponAdapter extends RecyclerView.Adapter<UserCouponAdapter.CouponViewHolder> {
    private List<Coupon> couponList;
    private final OnApplyClickListener applyClickListener;

    // Interface cho sự kiện click
    public interface OnApplyClickListener {
        void onApplyClick(Coupon coupon);
    }

    public UserCouponAdapter(List<Coupon> couponList, OnApplyClickListener applyClickListener) {
        this.couponList = couponList;
        this.applyClickListener = applyClickListener;
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_coupon_user, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Coupon coupon = couponList.get(position);
        holder.couponCode.setText(coupon.getCode());
        holder.couponValue.setText(String.format("Giảm giá: %.2f%%", coupon.getDiscountValue()));
        holder.couponPeriod.setText(String.format("Thời gian: %s - %s | Trạng thái: %s | Tối thiểu: %.0f VND",
                coupon.getStartDate(), coupon.getEndDate(), coupon.getStatus(), coupon.getMinOrderValue()));

        holder.btnApply.setOnClickListener(v -> applyClickListener.onApplyClick(coupon));
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
        MaterialButton btnApply;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            couponCode = itemView.findViewById(R.id.coupon_code);
            couponValue = itemView.findViewById(R.id.coupon_value);
            couponPeriod = itemView.findViewById(R.id.coupon_period);
            btnApply = itemView.findViewById(R.id.btn_apply_coupon);
        }
    }
}
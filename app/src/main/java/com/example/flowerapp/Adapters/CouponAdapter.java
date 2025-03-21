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
import java.util.function.Consumer;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {
    private List<Coupon> couponList;
    private Consumer<Coupon> onEditClick;
    private Consumer<Integer> onDeleteClick;

    public CouponAdapter(List<Coupon> couponList, Consumer<Coupon> onEditClick, Consumer<Integer> onDeleteClick) {
        this.couponList = couponList;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
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
        holder.couponValue.setText("Giá trị: " + String.format("%,.0f VND", coupon.getDiscountValue()));
        holder.couponPeriod.setText("Từ: " + coupon.getStartDate() + " đến " + coupon.getEndDate() + " | " + coupon.getStatus());

        holder.btnEditCoupon.setOnClickListener(v -> onEditClick.accept(coupon));
        holder.btnDeleteCoupon.setOnClickListener(v -> onDeleteClick.accept(coupon.getId()));
    }

    @Override
    public int getItemCount() {
        return couponList.size();
    }

    public static class CouponViewHolder extends RecyclerView.ViewHolder {
        TextView couponCode, couponValue, couponPeriod;
        MaterialButton btnEditCoupon, btnDeleteCoupon;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            couponCode = itemView.findViewById(R.id.coupon_code);
            couponValue = itemView.findViewById(R.id.coupon_value);
            couponPeriod = itemView.findViewById(R.id.coupon_period);
            btnEditCoupon = itemView.findViewById(R.id.btn_edit_coupon);
            btnDeleteCoupon = itemView.findViewById(R.id.btn_delete_coupon);
        }
    }
}
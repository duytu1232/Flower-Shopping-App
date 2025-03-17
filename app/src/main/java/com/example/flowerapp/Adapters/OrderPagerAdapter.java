package com.example.flowerapp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.flowerapp.User.Fragments.MyOrder_Fragment.ChoThanhToanFragment;
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.ChoVanChuyenFragment;
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.ChoGiaoHangFragment;
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.ChuaDanhGiaFragment;
import com.example.flowerapp.User.Fragments.MyOrder_Fragment.TraHangFragment;

public class OrderPagerAdapter extends FragmentStateAdapter {
    public OrderPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ChoThanhToanFragment(); // Pending Payment
            case 1:
                return new ChoVanChuyenFragment(); // Shipping
            case 2:
                return new ChoGiaoHangFragment(); // Delivered
            case 3:
                return new ChuaDanhGiaFragment(); // Not Reviewed
            case 4:
                return new TraHangFragment(); // Returned
            default:
                return new ChoThanhToanFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5; // Số lượng tabs
    }
}
package com.example.flowerapp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.flowerapp.User.Fragments.ChoThanhToanFragment;
import com.example.flowerapp.User.Fragments.ChoVanChuyenFragment;
import com.example.flowerapp.User.Fragments.ChoGiaoHangFragment;
import com.example.flowerapp.User.Fragments.ChuaDanhGiaFragment;
import com.example.flowerapp.User.Fragments.TraHangFragment;
import com.example.flowerapp.User.Fragments.HuyDonFragment;

public class OrderPagerAdapter extends FragmentStateAdapter {

    public OrderPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ChoThanhToanFragment();
            case 1:
                return new ChoVanChuyenFragment();
            case 2:
                return new ChoGiaoHangFragment();
            case 3:
                return new ChuaDanhGiaFragment();
            case 4:
                return new TraHangFragment();
            case 5:
                return new HuyDonFragment();
            default:
                return new ChoThanhToanFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 6; // Số lượng tabs
    }
}

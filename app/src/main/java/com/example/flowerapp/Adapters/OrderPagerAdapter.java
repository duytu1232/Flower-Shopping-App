package com.example.flowerapp.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.flowerapp.User.Fragments.MyOrder_Fragment.OrderFragment;

public class OrderPagerAdapter extends FragmentStateAdapter {
    private static final String[] TAB_TYPES = {"pending", "shipped", "delivered", "not_reviewed", "returned"};

    public OrderPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return OrderFragment.newInstance(TAB_TYPES[position]);
    }

    @Override
    public int getItemCount() {
        return TAB_TYPES.length;
    }
}
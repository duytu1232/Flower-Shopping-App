package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import com.example.flowerapp.R;

public class ChoVanChuyenFragment extends BaseOrderFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_cho_van_chuyen;
    }

    @Override
    protected void loadOrdersFromDatabase() {
        loadOrdersWithStatus("shipped");
    }
}
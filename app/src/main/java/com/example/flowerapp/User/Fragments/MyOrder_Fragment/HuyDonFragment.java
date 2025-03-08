package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import com.example.flowerapp.R;

public class HuyDonFragment extends BaseOrderFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_huy_don;
    }

    @Override
    protected void loadOrdersFromDatabase() {
        loadOrdersWithStatus("canceled");
    }
}
package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import com.example.flowerapp.R;

public class ChoThanhToanFragment extends BaseOrderFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_cho_thanh_toan;
    }

    @Override
    protected void loadOrdersFromDatabase() {
        loadOrdersWithStatus("pending");
    }
}
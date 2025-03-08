package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import com.example.flowerapp.R;

public class ChoGiaoHangFragment extends BaseOrderFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_cho_giao_hang;
    }

    @Override
    protected void loadOrdersFromDatabase() {
        loadOrdersWithStatus("delivered");
    }
}
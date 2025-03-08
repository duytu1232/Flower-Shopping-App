package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import com.example.flowerapp.R;

public class TraHangFragment extends BaseOrderFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_tra_hang;
    }

    @Override
    protected void loadOrdersFromDatabase() {
        loadOrdersWithReturnStatus("returned");
    }
}
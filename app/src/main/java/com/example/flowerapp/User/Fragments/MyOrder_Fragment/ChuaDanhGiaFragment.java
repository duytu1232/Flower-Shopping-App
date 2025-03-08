package com.example.flowerapp.User.Fragments.MyOrder_Fragment;

import com.example.flowerapp.R;

public class ChuaDanhGiaFragment extends BaseOrderFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chua_danh_gia;
    }

    @Override
    protected void loadOrdersFromDatabase() {
        loadOrdersNotReviewed();
    }
}
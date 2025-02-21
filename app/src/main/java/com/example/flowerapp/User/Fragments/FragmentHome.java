package com.example.flowerapp.User.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flowerapp.Adapters.ProductAdapter;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends Fragment {

    private ViewFlipper viewFlipper;
    private RecyclerView recyclerNewProducts, recyclerSaleProducts;

    private String mParam1;
    private String mParam2;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public FragmentHome() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo ViewFlipper
        viewFlipper = view.findViewById(R.id.viewFlipper);
        setupViewFlipper();

        // Khởi tạo RecyclerView cho sản phẩm mới và sản phẩm sale
        recyclerNewProducts = view.findViewById(R.id.listnewProduct);
        recyclerSaleProducts = view.findViewById(R.id.listsaleProduct);

        // Cấu hình RecyclerView cho danh sách ngang
        LinearLayoutManager newLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager saleLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerNewProducts.setLayoutManager(newLayoutManager);
        recyclerSaleProducts.setLayoutManager(saleLayoutManager);

        // Thêm dữ liệu mẫu cứng (hardcoded)
        List<Product> newProducts = new ArrayList<>();
        newProducts.add(new Product("Rose", "$10.00", R.drawable.rose));
        newProducts.add(new Product("Lily", "$15.00", R.drawable.lily));
        newProducts.add(new Product("Tulip", "$12.00", R.drawable.tulip));

        List<Product> saleProducts = new ArrayList<>();
        saleProducts.add(new Product("Orchid (Sale)", "$8.00", R.drawable.orchid));
        saleProducts.add(new Product("Sunflower (Sale)", "$9.00", R.drawable.sunflower));
        saleProducts.add(new Product("Daisy (Sale)", "$7.00", R.drawable.daisy));

        // Khởi tạo và gán adapter
        ProductAdapter newProductAdapter = new ProductAdapter(newProducts, requireContext());
        ProductAdapter saleProductAdapter = new ProductAdapter(saleProducts, requireContext());

        recyclerNewProducts.setAdapter(newProductAdapter);
        recyclerSaleProducts.setAdapter(saleProductAdapter);

        return view;
    }

    // Cấu hình ViewFlipper
    private void setupViewFlipper() {
        if (getContext() == null) return; // Tránh lỗi nếu context null

        List<String> mangqc = new ArrayList<>();
        mangqc.add("https://dichvutanghoa.com/wp-content/uploads/2019/12/hoa-mau-tim-4.jpg");
        mangqc.add("https://cdn.tgdd.vn/Files/2021/07/23/1370357/top-20-loai-hoa-dep-nhat-the-gioi-co-1-loai-moc-day-o-viet-nam-202107231836110639.jpg");
        mangqc.add("https://cdn-media.sforum.vn/storage/app/media/cac-loai-hoa-1.jpg");

        for (String url : mangqc) {
            ImageView imgView = new ImageView(getContext());
            Glide.with(this).load(url).into(imgView);
            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
            viewFlipper.addView(imgView);
            Log.d("ViewFlipper", "Đã thêm ảnh: " + url);
        }

        // Chỉ chạy animation nếu getContext() không null
        if (getContext() != null) {
            Animation slide_in = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right);
            Animation slide_out = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left);
            viewFlipper.setInAnimation(slide_in);
            viewFlipper.setOutAnimation(slide_out);
        }

        // Cấu hình hiển thị
        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);
    }
}
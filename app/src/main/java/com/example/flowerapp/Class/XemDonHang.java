package com.example.flowerapp.Class;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowerapp.MainActivity;
import com.example.flowerapp.R;

import androidx.viewpager2.widget.ViewPager2;
import com.example.flowerapp.Adapters.OrderPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class XemDonHang extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private OrderPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_xem_don_hang);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Xử lý nút Back để quay lại FragmentAccountUser
        ImageView backBtn = findViewById(R.id.back_account_page_btn);
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(XemDonHang.this, MainActivity.class);
            intent.putExtra("openFragment", "account"); // Gửi dữ liệu để mở đúng fragment
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // Đóng trang hiện tại
        });

        // Khởi tạo ViewPager và TabLayout
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Khởi tạo Adapter cho ViewPager
        adapter = new OrderPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Chờ thanh toán");
                    tab.view.setContentDescription("Tab chờ thanh toán");
                    break;
                case 1:
                    tab.setText("Chờ vận chuyển");
                    tab.view.setContentDescription("Tab chờ vận chuyển");
                    break;
                case 2:
                    tab.setText("Chờ giao hàng");
                    tab.view.setContentDescription("Tab chờ giao hàng");
                    break;
                case 3:
                    tab.setText("Chưa đánh giá");
                    tab.view.setContentDescription("Tab chưa đánh giá");
                    break;
                case 4:
                    tab.setText("Trả hàng/Hoàn tiền");
                    tab.view.setContentDescription("Tab trả hàng hoặc hoàn tiền");
                    break;
                case 5:
                    tab.setText("Hủy đơn");
                    tab.view.setContentDescription("Tab hủy đơn");
                    break;
            }
        }).attach();
    }
}

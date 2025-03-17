package com.example.flowerapp.User.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowerapp.MainActivity;
import com.example.flowerapp.R;
import com.example.flowerapp.Adapters.OrderPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.viewpager2.widget.ViewPager2;

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

        // Xử lý nút Back
        ImageView backBtn = findViewById(R.id.back_account_page_btn);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> {
                Intent intent = new Intent(XemDonHang.this, MainActivity.class);
                intent.putExtra("openFragment", "account");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        } else {
            Toast.makeText(this, "Back button not found", Toast.LENGTH_SHORT).show();
        }

        // Khởi tạo ViewPager và TabLayout
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        if (tabLayout != null && viewPager != null) {
            // Khởi tạo Adapter cho ViewPager
            adapter = new OrderPagerAdapter(this);
            viewPager.setAdapter(adapter);

            // Kết nối TabLayout với ViewPager2
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText(getString(R.string.tab_pending_payment));
                        tab.view.setContentDescription("Pending Payment Tab");
                        break;
                    case 1:
                        tab.setText(getString(R.string.tab_shipping));
                        tab.view.setContentDescription("Shipping Tab");
                        break;
                    case 2:
                        tab.setText(getString(R.string.tab_delivered));
                        tab.view.setContentDescription("Delivered Tab");
                        break;
                    case 3:
                        tab.setText(getString(R.string.tab_not_reviewed));
                        tab.view.setContentDescription("Not Reviewed Tab");
                        break;
                    case 4:
                        tab.setText(getString(R.string.tab_returned));
                        tab.view.setContentDescription("Returned Tab");
                        break;
                }
            }).attach();
        } else {
            Toast.makeText(this, "TabLayout or ViewPager not found", Toast.LENGTH_SHORT).show();
        }
    }
}
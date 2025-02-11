package com.example.flowerapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import com.bumptech.glide.Glide;

public class MainActivity extends AppCompatActivity {

    ViewFlipper viewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo ViewFlipper
        viewFlipper = findViewById(R.id.viewFlipper);
        actionViewFlipper(); // Chạy slideshow ảnh

        // Fix lỗi: Không đặt ViewFlipper trong ViewCompat.setOnApplyWindowInsetsListener
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo nút tìm kiếm
        ImageView searchIcon = findViewById(R.id.Search_bar_icon);
        searchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TimKiem.class);
            startActivity(intent);
            finish();
        });

        // Ánh xạ các nút trên thanh navigation
        ImageView cartIcon = findViewById(R.id.cart_icon);
        ImageView favoriteIcon = findViewById(R.id.favorite_icon);
        ImageView accountIcon = findViewById(R.id.account_icon);

        // Xử lý sự kiện khi nhấn vào các nút
        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GioHang.class);
            startActivity(intent);
            finish();
        });

        favoriteIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Favorite.class);
            startActivity(intent);
            finish();
        });

        accountIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Account_User.class);
            startActivity(intent);
            finish();
        });
    }

    // Hàm xử lý ViewFlipper
    private void actionViewFlipper() {
        List<String> mangqc = new ArrayList<>();
        mangqc.add("https://dichvutanghoa.com/wp-content/uploads/2019/12/hoa-mau-tim-4.jpg");
        mangqc.add("https://cdn.tgdd.vn/Files/2021/07/23/1370357/top-20-loai-hoa-dep-nhat-the-gioi-co-1-loai-moc-day-o-viet-nam-202107231836110639.jpg");
        mangqc.add("https://cdn-media.sforum.vn/storage/app/media/cac-loai-hoa-1.jpg");

        for (String url : mangqc) {
            ImageView imgView = new ImageView(getApplicationContext());
            Glide.with(this)
                    .load(url)
                    .into(imgView);
            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
            imgView.setLayoutParams(new ViewFlipper.LayoutParams(
                    ViewFlipper.LayoutParams.MATCH_PARENT,
                    ViewFlipper.LayoutParams.MATCH_PARENT));
            viewFlipper.addView(imgView);
            Log.d("ViewFlipper", "Đã thêm ảnh: " + url);
        }

        // Cấu hình chuyển đổi ảnh
        viewFlipper.setFlipInterval(3000); // 3 giây mỗi ảnh
        viewFlipper.setAutoStart(true);

        // Thêm animation
        Animation slide_in = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        Animation slide_out = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        viewFlipper.setInAnimation(slide_in);
        viewFlipper.setOutAnimation(slide_out);
    }
}

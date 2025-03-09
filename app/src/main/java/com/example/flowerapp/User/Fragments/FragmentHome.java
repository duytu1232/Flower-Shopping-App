package com.example.flowerapp.User.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class FragmentHome extends Fragment {
    private static final String TAG = "FragmentHome";

    private ViewFlipper viewFlipper;
    private RecyclerView recyclerNewProducts, recyclerSaleProducts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo ViewFlipper
        viewFlipper = view.findViewById(R.id.viewFlipper);
        setupViewFlipper();

        // Khởi tạo RecyclerView
        recyclerNewProducts = view.findViewById(R.id.listnewProduct);
        recyclerSaleProducts = view.findViewById(R.id.listsaleProduct);

        LinearLayoutManager newLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager saleLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerNewProducts.setLayoutManager(newLayoutManager);
        recyclerSaleProducts.setLayoutManager(saleLayoutManager);

        // Lấy dữ liệu từ cơ sở dữ liệu
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.openDatabase();

        List<Product> allProducts = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM Products LIMIT 10", null); // Giới hạn số lượng
        if (cursor != null) {
            int idIndex = cursor.getColumnIndex("product_id");
            int nameIndex = cursor.getColumnIndex("name");
            int descriptionIndex = cursor.getColumnIndex("description");
            int priceIndex = cursor.getColumnIndex("price");
            int stockIndex = cursor.getColumnIndex("stock");
            int imageUrlIndex = cursor.getColumnIndex("image_url");
            int categoryIndex = cursor.getColumnIndex("category");

            if (idIndex == -1 || nameIndex == -1 || descriptionIndex == -1 || priceIndex == -1 ||
                    stockIndex == -1 || imageUrlIndex == -1 || categoryIndex == -1) {
                Log.e(TAG, "Một hoặc nhiều cột không tồn tại trong bảng Products!");
            } else {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    String description = cursor.getString(descriptionIndex);
                    double price = cursor.getDouble(priceIndex);
                    int stock = cursor.getInt(stockIndex);
                    String imageUrl = cursor.getString(imageUrlIndex);
                    String category = cursor.getString(categoryIndex);
                    allProducts.add(new Product(id, name, description, price, stock, imageUrl, category));
                }
            }
            cursor.close();
        }
        dbHelper.closeDatabase(db);

        // Phân loại sản phẩm (giả lập: sản phẩm mới và sản phẩm sale)
        List<Product> newProducts = new ArrayList<>();
        List<Product> saleProducts = new ArrayList<>();
        for (Product product : allProducts) {
            // Cần thêm cột 'is_sale' hoặc 'discount' trong bảng Products để phân loại chính xác
            if (product.getPrice() < 70) { // Giả lập, cần thay bằng logic thực tế
                saleProducts.add(product);
            } else {
                newProducts.add(product);
            }
        }

        // Khởi tạo và gán adapter
        ProductAdapter newProductAdapter = new ProductAdapter(newProducts, requireContext());
        ProductAdapter saleProductAdapter = new ProductAdapter(saleProducts, requireContext());

        recyclerNewProducts.setAdapter(newProductAdapter);
        recyclerSaleProducts.setAdapter(saleProductAdapter);

        return view;
    }

    private void setupViewFlipper() {
        List<String> mangqc = new ArrayList<>();
        mangqc.add("https://dichvutanghoa.com/wp-content/uploads/2019/12/hoa-mau-tim-4.jpg");
        mangqc.add("https://cdn.tgdd.vn/Files/2021/07/23/1370357/top-20-loai-hoa-dep-nhat-the-gioi-co-1-loai-moc-day-o-viet-nam-202107231836110639.jpg");
        mangqc.add("https://cdn-media.sforum.vn/storage/app/media/cac-loai-hoa-1.jpg");

        for (String url : mangqc) {
            ImageView imgView = new ImageView(requireContext());
            Glide.with(this).load(url).into(imgView);
            imgView.setScaleType(ImageView.ScaleType.FIT_XY);
            viewFlipper.addView(imgView);
            Log.d("ViewFlipper", "Đã thêm ảnh: " + url);
        }

        Animation slide_in = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_right);
        Animation slide_out = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_left);
        viewFlipper.setInAnimation(slide_in);
        viewFlipper.setOutAnimation(slide_out);

        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);
    }
}
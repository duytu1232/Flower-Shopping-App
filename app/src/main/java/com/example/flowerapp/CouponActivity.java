package com.example.flowerapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.UserCouponAdapter;
import com.example.flowerapp.Models.Coupon;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CouponActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserCouponAdapter adapter;
    private List<Coupon> couponList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private double totalPrice;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recycler_coupon_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserCouponAdapter(couponList, this::applyCoupon);
        recyclerView.setAdapter(adapter);

        totalPrice = getIntent().getDoubleExtra("total_price", 0.0);

        // Xử lý nút Hủy
        findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        loadCoupons();
    }

    private void loadCoupons() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT discount_id, code, discount_value, start_date, end_date, status, min_order_value FROM Discount_Codes", null);
            couponList.clear();
            String today = dateFormat.format(new Date());
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("discount_id"));
                String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
                double discountValue = cursor.getDouble(cursor.getColumnIndexOrThrow("discount_value"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double minOrderValue = cursor.getDouble(cursor.getColumnIndexOrThrow("min_order_value"));

                // Chỉ hiển thị coupon hợp lệ: status là "active", trong thời gian hiệu lực, và tổng giá đơn hàng đáp ứng min_order_value
                if ("active".equals(status) &&
                        today.compareTo(startDate) >= 0 &&
                        today.compareTo(endDate) <= 0 &&
                        totalPrice >= minOrderValue) {
                    couponList.add(new Coupon(id, code, discountValue, startDate, endDate, status, minOrderValue));
                }
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
        } catch (Exception e) {
            Log.e("CouponActivity", "Lỗi tải mã giảm giá: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi tải mã giảm giá: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void applyCoupon(Coupon coupon) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("coupon_id", coupon.getId());
        resultIntent.putExtra("coupon_code", coupon.getCode());
        resultIntent.putExtra("discount_value", coupon.getDiscountValue());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
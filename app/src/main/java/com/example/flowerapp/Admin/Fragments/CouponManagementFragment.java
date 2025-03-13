package com.example.flowerapp.Admin.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.CouponAdapter;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.example.flowerapp.Models.Coupon;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class CouponManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private CouponAdapter adapter;
    private List<Coupon> couponList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coupon_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerView = view.findViewById(R.id.recycler_coupon_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CouponAdapter(couponList, this::showEditCouponDialog, this::deleteCoupon);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_add_coupon).setOnClickListener(v -> showAddCouponDialog());

        loadCoupons();
        return view;
    }

    private void loadCoupons() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT * FROM Discount_Codes", null);
            couponList.clear();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("discount_id"));
                String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
                double discountValue = cursor.getDouble(cursor.getColumnIndexOrThrow("discount_value"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                couponList.add(new Coupon(id, code, discountValue, startDate, endDate, status));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi tải mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddCouponDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Mã Giảm Giá");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_coupon, null);
        EditText editCode = view.findViewById(R.id.edit_coupon_code);
        EditText editValue = view.findViewById(R.id.edit_coupon_value);
        EditText editStart = view.findViewById(R.id.edit_coupon_start);
        EditText editEnd = view.findViewById(R.id.edit_coupon_end);
        EditText editStatus = view.findViewById(R.id.edit_coupon_status);

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    try {
                        String code = editCode.getText().toString().trim();
                        double discountValue = Double.parseDouble(editValue.getText().toString().trim());
                        String startDate = editStart.getText().toString().trim();
                        String endDate = editEnd.getText().toString().trim();
                        String status = editStatus.getText().toString().trim();

                        if (TextUtils.isEmpty(code) || discountValue <= 0 || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate) || TextUtils.isEmpty(status)) {
                            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin hợp lệ!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        addCoupon(code, discountValue, startDate, endDate, status);
                        loadCoupons();
                        Toast.makeText(requireContext(), "Thêm mã giảm giá thành công", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditCouponDialog(Coupon coupon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sửa Mã Giảm Giá");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_coupon, null);
        EditText editCode = view.findViewById(R.id.edit_coupon_code);
        EditText editValue = view.findViewById(R.id.edit_coupon_value);
        EditText editStart = view.findViewById(R.id.edit_coupon_start);
        EditText editEnd = view.findViewById(R.id.edit_coupon_end);
        EditText editStatus = view.findViewById(R.id.edit_coupon_status);

        editCode.setText(coupon.getCode());
        editValue.setText(String.valueOf(coupon.getDiscountValue()));
        editStart.setText(coupon.getStartDate());
        editEnd.setText(coupon.getEndDate());
        editStatus.setText(coupon.getStatus());

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    try {
                        String code = editCode.getText().toString().trim();
                        double discountValue = Double.parseDouble(editValue.getText().toString().trim());
                        String startDate = editStart.getText().toString().trim();
                        String endDate = editEnd.getText().toString().trim();
                        String status = editStatus.getText().toString().trim();

                        if (TextUtils.isEmpty(code) || discountValue <= 0 || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate) || TextUtils.isEmpty(status)) {
                            Toast.makeText(requireContext(), "Vui lòng điền đầy đủ thông tin hợp lệ!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        updateCoupon(coupon.getId(), code, discountValue, startDate, endDate, status);
                        loadCoupons();
                        Toast.makeText(requireContext(), "Cập nhật mã giảm giá thành công", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean isCodeExists(String code, int excludeId) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Discount_Codes WHERE code = ? AND discount_id != ?", new String[]{code, String.valueOf(excludeId)});
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi kiểm tra mã: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void addCoupon(String code, double discountValue, String startDate, String endDate, String status) {
        if (isCodeExists(code, -1)) {
            Toast.makeText(requireContext(), "Mã giảm giá đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Discount_Codes (code, discount_value, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)",
                    new Object[]{code, discountValue, startDate, endDate, status});
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi thêm mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCoupon(int id, String code, double discountValue, String startDate, String endDate, String status) {
        if (isCodeExists(code, id)) {
            Toast.makeText(requireContext(), "Mã giảm giá đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("UPDATE Discount_Codes SET code = ?, discount_value = ?, start_date = ?, end_date = ?, status = ? WHERE discount_id = ?",
                    new Object[]{code, discountValue, startDate, endDate, status, id});
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi cập nhật mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCouponReferenced(int discountId) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Orders WHERE discount_code = ?", new String[]{String.valueOf(discountId)});
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi kiểm tra tham chiếu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    private void deleteCoupon(int id) {
        if (isCouponReferenced(id)) {
            Toast.makeText(requireContext(), "Không thể xóa: Mã giảm giá đang được sử dụng!", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa mã giảm giá này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.openDatabase()) {
                        db.execSQL("DELETE FROM Discount_Codes WHERE discount_id = ?", new Object[]{id});
                        Toast.makeText(requireContext(), "Xóa mã giảm giá thành công", Toast.LENGTH_SHORT).show();
                        loadCoupons();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Lỗi xóa mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
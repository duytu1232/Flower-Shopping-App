package com.example.flowerapp.Admin.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.example.flowerapp.Models.Coupon;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CouponManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private CouponAdapter adapter;
    private List<Coupon> couponList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private static final String[] VALID_STATUSES = {"active", "expired"};

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
            Cursor cursor = db.rawQuery("SELECT discount_id, code, discount_value, start_date, end_date, status FROM Discount_Codes", null);
            couponList.clear();
            Log.d("CouponManagement", "Số lượng bản ghi: " + cursor.getCount());
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("discount_id"));
                String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
                double discountValue = cursor.getDouble(cursor.getColumnIndexOrThrow("discount_value"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                couponList.add(new Coupon(id, code, discountValue, startDate, endDate, status));
                Log.d("CouponManagement", "Thêm mã giảm giá: " + code);
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
            Log.d("CouponManagement", "Số lượng mã giảm giá trong list: " + couponList.size());
        } catch (Exception e) {
            Log.e("CouponManagement", "Lỗi tải mã giảm giá: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi tải mã giảm giá: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

                        if (TextUtils.isEmpty(code)) {
                            editCode.setError("Mã giảm giá không được để trống");
                            return;
                        }
                        if (discountValue <= 0) {
                            editValue.setError("Giá trị giảm giá phải lớn hơn 0");
                            return;
                        }
                        if (TextUtils.isEmpty(startDate) || !isValidDateFormat(startDate)) {
                            editStart.setError("Ngày bắt đầu không hợp lệ (định dạng: yyyy-MM-dd)");
                            return;
                        }
                        if (TextUtils.isEmpty(endDate) || !isValidDateFormat(endDate)) {
                            editEnd.setError("Ngày kết thúc không hợp lệ (định dạng: yyyy-MM-dd)");
                            return;
                        }
                        if (!isValidDateRange(startDate, endDate)) {
                            editEnd.setError("Ngày kết thúc phải sau ngày bắt đầu");
                            return;
                        }
                        if (TextUtils.isEmpty(status) || !isValidStatus(status)) {
                            editStatus.setError("Trạng thái không hợp lệ (active, expired)");
                            return;
                        }

                        addCoupon(code, discountValue, startDate, endDate, status);
                        loadCoupons();
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

                        if (TextUtils.isEmpty(code)) {
                            editCode.setError("Mã giảm giá không được để trống");
                            return;
                        }
                        if (discountValue <= 0) {
                            editValue.setError("Giá trị giảm giá phải lớn hơn 0");
                            return;
                        }
                        if (TextUtils.isEmpty(startDate) || !isValidDateFormat(startDate)) {
                            editStart.setError("Ngày bắt đầu không hợp lệ (định dạng: yyyy-MM-dd)");
                            return;
                        }
                        if (TextUtils.isEmpty(endDate) || !isValidDateFormat(endDate)) {
                            editEnd.setError("Ngày kết thúc không hợp lệ (định dạng: yyyy-MM-dd)");
                            return;
                        }
                        if (!isValidDateRange(startDate, endDate)) {
                            editEnd.setError("Ngày kết thúc phải sau ngày bắt đầu");
                            return;
                        }
                        if (TextUtils.isEmpty(status) || !isValidStatus(status)) {
                            editStatus.setError("Trạng thái không hợp lệ (active, expired)");
                            return;
                        }

                        updateCoupon(coupon.getId(), code, discountValue, startDate, endDate, status);
                        loadCoupons();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean isValidDateFormat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidDateRange(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            return sdf.parse(startDate).before(sdf.parse(endDate));
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidStatus(String status) {
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
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
            Log.e("CouponManagement", "Lỗi kiểm tra mã: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi kiểm tra mã: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            Log.d("CouponManagement", "Thêm mã giảm giá thành công: " + code);
            Toast.makeText(requireContext(), "Thêm mã giảm giá thành công", Toast.LENGTH_SHORT).show();
        } catch (SQLiteConstraintException e) {
            Log.e("CouponManagement", "Lỗi ràng buộc: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi: Dữ liệu không hợp lệ (có thể thiếu thông tin bắt buộc)", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("CouponManagement", "Lỗi thêm mã giảm giá: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi thêm mã giảm giá: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            Log.d("CouponManagement", "Cập nhật mã giảm giá thành công: " + code);
            Toast.makeText(requireContext(), "Cập nhật mã giảm giá thành công", Toast.LENGTH_SHORT).show();
        } catch (SQLiteConstraintException e) {
            Log.e("CouponManagement", "Lỗi ràng buộc: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi: Dữ liệu không hợp lệ", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("CouponManagement", "Lỗi cập nhật mã giảm giá: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi cập nhật mã giảm giá: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteCoupon(int id) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa mã giảm giá này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.openDatabase()) {
                        db.execSQL("DELETE FROM Discount_Codes WHERE discount_id = ?", new Object[]{id});
                        Log.d("CouponManagement", "Xóa mã giảm giá thành công: " + id);
                        Toast.makeText(requireContext(), "Xóa mã giảm giá thành công", Toast.LENGTH_SHORT).show();
                        loadCoupons();
                    } catch (Exception e) {
                        Log.e("CouponManagement", "Lỗi xóa mã giảm giá: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Lỗi xóa mã giảm giá: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
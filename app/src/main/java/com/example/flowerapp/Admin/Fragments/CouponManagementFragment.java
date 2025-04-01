package com.example.flowerapp.Admin.Fragments;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.CouponAdapter;
import com.example.flowerapp.Models.Coupon;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CouponManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private CouponAdapter adapter;
    private List<Coupon> couponList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private static final String[] VALID_STATUSES = {"active", "expired"};
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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
            Cursor cursor = db.rawQuery("SELECT discount_id, code, discount_value, start_date, end_date, status, min_order_value FROM Discount_Codes", null);
            couponList.clear();
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("discount_id"));
                String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
                double discountValue = cursor.getDouble(cursor.getColumnIndexOrThrow("discount_value"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double minOrderValue = cursor.getDouble(cursor.getColumnIndexOrThrow("min_order_value"));
                couponList.add(new Coupon(id, code, discountValue, startDate, endDate, status, minOrderValue));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
        } catch (Exception e) {
            Log.e("CouponManagement", "Lỗi tải mã giảm giá: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi tải mã giảm giá: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddCouponDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Mã Giảm Giá");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_coupon, null);
        TextInputEditText editCode = view.findViewById(R.id.edit_coupon_code);
        TextInputEditText editValue = view.findViewById(R.id.edit_coupon_value);
        TextInputEditText editStart = view.findViewById(R.id.edit_coupon_start);
        TextInputEditText editEnd = view.findViewById(R.id.edit_coupon_end);
        Spinner spinnerStatus = view.findViewById(R.id.spinner_coupon_status); // Sử dụng Spinner
        TextInputEditText editMinOrderValue = view.findViewById(R.id.edit_coupon_min_order_value);

        // Đặt mặc định nếu cần
        editMinOrderValue.setText("0");

        // Thêm DatePicker cho edit_coupon_start
        editStart.setOnClickListener(v -> showDatePickerDialog(editStart));

        // Thêm DatePicker cho edit_coupon_end
        editEnd.setOnClickListener(v -> showDatePickerDialog(editEnd));

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    try {
                        String code = editCode.getText().toString().trim();
                        String valueStr = editValue.getText().toString().trim();
                        String startDate = editStart.getText().toString().trim();
                        String endDate = editEnd.getText().toString().trim();
                        String status = spinnerStatus.getSelectedItem().toString(); // Lấy giá trị từ Spinner
                        String minOrderValueStr = editMinOrderValue.getText().toString().trim();

                        if (TextUtils.isEmpty(code)) {
                            editCode.setError("Mã giảm giá không được để trống");
                            return;
                        }
                        double discountValue = Double.parseDouble(valueStr);
                        if (discountValue <= 0 || discountValue > 100) {
                            editValue.setError("Giá trị giảm giá phải từ 0 đến 100%");
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
                        // Không cần kiểm tra status nữa vì Spinner đảm bảo giá trị hợp lệ
                        double minOrderValue = Double.parseDouble(minOrderValueStr);
                        if (minOrderValue < 0) {
                            editMinOrderValue.setError("Giá trị tối thiểu phải lớn hơn hoặc bằng 0");
                            return;
                        }

                        addCoupon(code, discountValue, startDate, endDate, status, minOrderValue);
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
        TextInputEditText editCode = view.findViewById(R.id.edit_coupon_code);
        TextInputEditText editValue = view.findViewById(R.id.edit_coupon_value);
        TextInputEditText editStart = view.findViewById(R.id.edit_coupon_start);
        TextInputEditText editEnd = view.findViewById(R.id.edit_coupon_end);
        Spinner spinnerStatus = view.findViewById(R.id.spinner_coupon_status); // Sử dụng Spinner
        TextInputEditText editMinOrderValue = view.findViewById(R.id.edit_coupon_min_order_value);

        editCode.setText(coupon.getCode());
        editValue.setText(String.valueOf(coupon.getDiscountValue()));
        editStart.setText(coupon.getStartDate());
        editEnd.setText(coupon.getEndDate());
        editMinOrderValue.setText(String.valueOf(coupon.getMinOrderValue()));

        // Đặt giá trị hiện tại cho Spinner
        ArrayAdapter<String> statusAdapter = (ArrayAdapter<String>) spinnerStatus.getAdapter();
        spinnerStatus.setSelection(statusAdapter.getPosition(coupon.getStatus()));

        // Thêm DatePicker cho edit_coupon_start
        editStart.setOnClickListener(v -> showDatePickerDialog(editStart));

        // Thêm DatePicker cho edit_coupon_end
        editEnd.setOnClickListener(v -> showDatePickerDialog(editEnd));

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    try {
                        String code = editCode.getText().toString().trim();
                        String valueStr = editValue.getText().toString().trim();
                        String startDate = editStart.getText().toString().trim();
                        String endDate = editEnd.getText().toString().trim();
                        String status = spinnerStatus.getSelectedItem().toString(); // Lấy giá trị từ Spinner
                        String minOrderValueStr = editMinOrderValue.getText().toString().trim();

                        if (TextUtils.isEmpty(code)) {
                            editCode.setError("Mã giảm giá không được để trống");
                            return;
                        }
                        double discountValue = Double.parseDouble(valueStr);
                        if (discountValue <= 0 || discountValue > 100) {
                            editValue.setError("Giá trị giảm giá phải từ 0 đến 100%");
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
                        // Không cần kiểm tra status nữa vì Spinner đảm bảo giá trị hợp lệ
                        double minOrderValue = Double.parseDouble(minOrderValueStr);
                        if (minOrderValue < 0) {
                            editMinOrderValue.setError("Giá trị tối thiểu phải lớn hơn hoặc bằng 0");
                            return;
                        }

                        updateCoupon(coupon.getId(), code, discountValue, startDate, endDate, status, minOrderValue);
                        loadCoupons();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDatePickerDialog(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        try {
            // Nếu đã có ngày, parse để hiển thị ngày hiện tại trong DatePicker
            String currentDate = editText.getText().toString().trim();
            if (!TextUtils.isEmpty(currentDate)) {
                calendar.setTime(dateFormat.parse(currentDate));
            }
        } catch (ParseException e) {
            // Nếu không parse được, giữ ngày hiện tại
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Định dạng ngày thành yyyy-MM-dd
                    String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    editText.setText(selectedDate);
                    editText.setError(null); // Xóa lỗi nếu có
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private boolean isValidDateFormat(String date) {
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isValidDateRange(String startDate, String endDate) {
        try {
            return dateFormat.parse(startDate).before(dateFormat.parse(endDate));
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
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Discount_Codes WHERE code = ? AND discount_id != ?",
                    new String[]{code, String.valueOf(excludeId)});
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

    private void addCoupon(String code, double discountValue, String startDate, String endDate, String status, double minOrderValue) {
        if (isCodeExists(code, -1)) {
            Toast.makeText(requireContext(), "Mã giảm giá đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Discount_Codes (code, discount_value, start_date, end_date, status, min_order_value) VALUES (?, ?, ?, ?, ?, ?)",
                    new Object[]{code, discountValue, startDate, endDate, status, minOrderValue});
            Toast.makeText(requireContext(), "Thêm mã giảm giá thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("CouponManagement", "Lỗi thêm mã giảm giá: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi thêm mã giảm giá: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateCoupon(int id, String code, double discountValue, String startDate, String endDate, String status, double minOrderValue) {
        if (isCodeExists(code, id)) {
            Toast.makeText(requireContext(), "Mã giảm giá đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("UPDATE Discount_Codes SET code = ?, discount_value = ?, start_date = ?, end_date = ?, status = ?, min_order_value = ? WHERE discount_id = ?",
                    new Object[]{code, discountValue, startDate, endDate, status, minOrderValue, id});
            Toast.makeText(requireContext(), "Cập nhật mã giảm giá thành công", Toast.LENGTH_SHORT).show();
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
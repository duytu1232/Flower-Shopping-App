package com.example.flowerapp.Admin.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.DatePicker;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CouponManagementFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private CouponAdapter adapter;
    private List<Coupon> couponList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coupon_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        couponList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_coupon_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CouponAdapter(couponList, requireContext(), this);
        recyclerView.setAdapter(adapter);

        loadCoupons();

        Button addButton = view.findViewById(R.id.btn_add_coupon);
        addButton.setOnClickListener(v -> showAddCouponDialog());

        return view;
    }

    private void loadCoupons() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery("SELECT discount_id, code, discount_value, start_date, end_date, status FROM Discount_Codes", null);
            couponList.clear();

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("discount_id"));
                String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
                double value = cursor.getDouble(cursor.getColumnIndexOrThrow("discount_value"));
                String start = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                String end = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                couponList.add(new Coupon(id, code, value, start, end, status));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi tải mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
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
                    if (!validateInput(editCode, editValue, editStart, editEnd, editStatus)) return;

                    String code = editCode.getText().toString().trim();
                    double value = Double.parseDouble(editValue.getText().toString().trim());
                    String start = editStart.getText().toString().trim();
                    String end = editEnd.getText().toString().trim();
                    String status = editStatus.getText().toString().trim().toLowerCase();

                    addCoupon(code, value, start, end, status);
                    loadCoupons();
                    Toast.makeText(requireContext(), "Thêm mã giảm giá thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private boolean validateInput(EditText code, EditText value, EditText start, EditText end, EditText status) {
        if (TextUtils.isEmpty(code.getText())) {
            code.setError("Mã không được để trống");
            return false;
        }
        if (TextUtils.isEmpty(value.getText()) || Double.parseDouble(value.getText().toString().trim()) <= 0) {
            value.setError("Giá trị phải lớn hơn 0");
            return false;
        }
        String startDate = start.getText().toString().trim();
        String endDate = end.getText().toString().trim();
        if (TextUtils.isEmpty(startDate) || !isValidDate(startDate)) {
            start.setError("Ngày bắt đầu không hợp lệ (yyyy-MM-dd)");
            return false;
        }
        if (TextUtils.isEmpty(endDate) || !isValidDate(endDate)) {
            end.setError("Ngày kết thúc không hợp lệ (yyyy-MM-dd)");
            return false;
        }
        if (compareDates(startDate, endDate) > 0) {
            start.setError("Ngày bắt đầu phải nhỏ hơn ngày kết thúc");
            return false;
        }
        if (TextUtils.isEmpty(status.getText()) || !status.getText().toString().trim().toLowerCase().matches("active|expired")) {
            status.setError("Status phải là 'active' hoặc 'expired'");
            return false;
        }
        return true;
    }

    private int compareDates(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            return start.compareTo(end);
        } catch (ParseException e) {
            return 1; // Giả định lỗi, ưu tiên không hợp lệ
        }
    }

    private boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateStr);
            return date != null;
        } catch (ParseException e) {
            return false;
        }
    }

    // Trong showAddCouponDialog và showEditCouponDialog, thêm kiểm tra trùng lặp code
    private void addCoupon(String code, double discountValue, String startDate, String endDate, String status) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Discount_Codes WHERE code = ?", new String[]{code});
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                Toast.makeText(requireContext(), "Mã giảm giá đã tồn tại!", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }
            cursor.close();
            db.execSQL("INSERT INTO Discount_Codes (code, discount_value, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)",
                    new Object[]{code, discountValue, startDate, endDate, status});
            loadCoupons();
            Toast.makeText(requireContext(), "Thêm mã giảm giá thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi thêm mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private void updateCoupon(int id, String code, double discountValue, String startDate, String endDate, String status) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            db.execSQL("UPDATE Discount_Codes SET code = ?, discount_value = ?, start_date = ?, end_date = ?, status = ? WHERE discount_id = ?",
                    new Object[]{code, discountValue, startDate, endDate, status, id});
            loadCoupons();
            Toast.makeText(requireContext(), "Cập nhật mã giảm giá thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi cập nhật mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    private void deleteCoupon(int id) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            // Kiểm tra trước khi xóa
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Orders WHERE discount_code = ?", new String[]{String.valueOf(id)});
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                Toast.makeText(requireContext(), "Không thể xóa: Mã giảm giá đang được sử dụng!", Toast.LENGTH_SHORT).show();
                cursor.close();
                return;
            }
            cursor.close();

            db.execSQL("DELETE FROM Discount_Codes WHERE discount_id = ?", new Object[]{id});
            loadCoupons();
            Toast.makeText(requireContext(), "Xóa mã giảm giá thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi xóa mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) dbHelper.closeDatabase(db);
        }
    }

    public static class Coupon {
        int id;
        String code, startDate, endDate, status;
        double discountValue;

        public Coupon(int id, String code, double discountValue, String startDate, String endDate, String status) {
            this.id = id;
            this.code = code;
            this.discountValue = discountValue;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
        }
    }

    public static class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {
        private List<Coupon> coupons;
        private Context context;
        private CouponManagementFragment fragment;

        public CouponAdapter(List<Coupon> coupons, Context context, CouponManagementFragment fragment) {
            this.coupons = coupons;
            this.context = context;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_coupon_admin, parent, false);
            return new CouponViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
            Coupon coupon = coupons.get(position);
            holder.codeTextView.setText("Code: " + coupon.code);
            holder.valueTextView.setText("Value: $" + coupon.discountValue);
            holder.periodTextView.setText("Period: " + coupon.startDate + " to " + coupon.endDate + " | Status: " + coupon.status);

            holder.btnEdit.setOnClickListener(v -> fragment.showEditCouponDialog(coupon));
            holder.btnDelete.setOnClickListener(v -> fragment.deleteCoupon(coupon.id));
        }

        @Override
        public int getItemCount() {
            return coupons.size();
        }

        public static class CouponViewHolder extends RecyclerView.ViewHolder {
            public TextView codeTextView, valueTextView, periodTextView;
            public Button btnEdit, btnDelete;

            public CouponViewHolder(@NonNull View itemView) {
                super(itemView);
                codeTextView = itemView.findViewById(R.id.coupon_code);
                valueTextView = itemView.findViewById(R.id.coupon_value);
                periodTextView = itemView.findViewById(R.id.coupon_period);
                btnEdit = itemView.findViewById(R.id.btn_edit_coupon);
                btnDelete = itemView.findViewById(R.id.btn_delete_coupon);
            }
        }
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

        editCode.setText(coupon.code);
        editValue.setText(String.valueOf(coupon.discountValue));
        editStart.setText(coupon.startDate);
        editEnd.setText(coupon.endDate);
        editStatus.setText(coupon.status);

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    if (!validateInput(editCode, editValue, editStart, editEnd, editStatus)) return;

                    String code = editCode.getText().toString().trim();
                    double value = Double.parseDouble(editValue.getText().toString().trim());
                    String start = editStart.getText().toString().trim();
                    String end = editEnd.getText().toString().trim();
                    String status = editStatus.getText().toString().trim().toLowerCase();

                    updateCoupon(coupon.id, code, value, start, end, status);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
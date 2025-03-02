package com.example.flowerapp.Admin.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.ArrayList;
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

        // Khởi tạo adapter
        adapter = new CouponAdapter(couponList, requireContext(), this);
        recyclerView.setAdapter(adapter);

        // Load dữ liệu từ SQLite
        loadCoupons();

        Button addButton = view.findViewById(R.id.btn_add_coupon);
        addButton.setOnClickListener(v -> showAddCouponDialog());

        return view;
    }

    private void loadCoupons() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT discount_id, code, discount_value, start_date, end_date, status FROM Discount_Codes", null);
            couponList.clear();

            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex("discount_id");
                int codeIndex = cursor.getColumnIndex("code");
                int valueIndex = cursor.getColumnIndex("discount_value");
                int startIndex = cursor.getColumnIndex("start_date");
                int endIndex = cursor.getColumnIndex("end_date");
                int statusIndex = cursor.getColumnIndex("status");

                if (idIndex >= 0 && codeIndex >= 0 && valueIndex >= 0 && startIndex >= 0 && endIndex >= 0 && statusIndex >= 0) {
                    int id = cursor.getInt(idIndex);
                    String code = cursor.getString(codeIndex);
                    double value = cursor.getDouble(valueIndex);
                    String start = cursor.getString(startIndex);
                    String end = cursor.getString(endIndex);
                    String status = cursor.getString(statusIndex);
                    couponList.add(new Coupon(id, code, value, start, end, status));
                }
            }
            cursor.close();
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi tải mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddCouponDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Mã Giảm Giá");

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_coupon, null);
        EditText editCode = view.findViewById(R.id.edit_coupon_code); // Sửa từ findValueById thành findViewById
        EditText editValue = view.findViewById(R.id.edit_coupon_value);
        EditText editStart = view.findViewById(R.id.edit_coupon_start);
        EditText editEnd = view.findViewById(R.id.edit_coupon_end);
        EditText editStatus = view.findViewById(R.id.edit_coupon_status);

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String code = editCode.getText().toString().trim();
                    double value = Double.parseDouble(editValue.getText().toString().trim());
                    String start = editStart.getText().toString().trim();
                    String end = editEnd.getText().toString().trim();
                    String status = editStatus.getText().toString().trim().toLowerCase();

                    if (!status.matches("active|expired")) {
                        Toast.makeText(requireContext(), "Status phải là 'active' hoặc 'expired'", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addCoupon(code, value, start, end, status);
                    loadCoupons();
                    Toast.makeText(requireContext(), "Thêm mã giảm giá thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void addCoupon(String code, double discountValue, String startDate, String endDate, String status) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Discount_Codes (code, discount_value, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)",
                    new Object[]{code, discountValue, startDate, endDate, status});
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi thêm mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCoupon(int id, String code, double discountValue, String startDate, String endDate, String status) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("UPDATE Discount_Codes SET code = ?, discount_value = ?, start_date = ?, end_date = ?, status = ? WHERE discount_id = ?",
                    new Object[]{code, discountValue, startDate, endDate, status, id});
            loadCoupons();
            Toast.makeText(requireContext(), "Cập nhật mã giảm giá thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi cập nhật mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteCoupon(int id) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("DELETE FROM Discount_Codes WHERE discount_id = ?", new Object[]{id});
            loadCoupons();
            Toast.makeText(requireContext(), "Xóa mã giảm giá thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi xóa mã giảm giá: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Class Coupon (model)
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

    // Adapter (cập nhật để hỗ trợ sửa, xóa)
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
        EditText editCode = view.findViewById(R.id.edit_coupon_code); // Sửa từ findValueById thành findViewById
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
                    String code = editCode.getText().toString().trim();
                    double value = Double.parseDouble(editValue.getText().toString().trim());
                    String start = editStart.getText().toString().trim();
                    String end = editEnd.getText().toString().trim();
                    String status = editStatus.getText().toString().trim().toLowerCase();

                    if (!status.matches("active|expired")) {
                        Toast.makeText(requireContext(), "Status phải là 'active' hoặc 'expired'", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateCoupon(coupon.id, code, value, start, end, status);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
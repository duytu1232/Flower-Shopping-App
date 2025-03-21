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

import com.example.flowerapp.Adapters.UserAdapter;
import com.example.flowerapp.Models.User;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private static final String[] VALID_ROLES = {"customer", "admin", "staff"};
    private static final String[] VALID_STATUSES = {"active", "locked"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerView = view.findViewById(R.id.recycler_user_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UserAdapter(userList, this::showEditUserDialog, this::deleteUser);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btn_add_user).setOnClickListener(v -> showAddUserDialog());

        loadUsers();
        return view;
    }

    private void loadUsers() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT user_id, username, email, role, status, full_name, phone, avatar_uri FROM Users", null);
            userList.clear();
            Log.d("UserManagement", "Số lượng bản ghi: " + cursor.getCount());
            while (cursor.moveToNext()) {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String avatarUri = cursor.getString(cursor.getColumnIndexOrThrow("avatar_uri"));
                userList.add(new User(userId, username, email, role, status, fullName, phone, avatarUri));
                Log.d("UserManagement", "Thêm người dùng: " + username);
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
            Log.d("UserManagement", "Số lượng người dùng trong list: " + userList.size());
        } catch (Exception e) {
            Log.e("UserManagement", "Lỗi tải người dùng: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi tải người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Người Dùng");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null);
        EditText editUsername = view.findViewById(R.id.edit_user_username);
        EditText editEmail = view.findViewById(R.id.edit_user_email);
        EditText editPassword = view.findViewById(R.id.edit_user_password);
        EditText editRole = view.findViewById(R.id.edit_user_role);
        EditText editStatus = view.findViewById(R.id.edit_user_status);
        EditText editFullName = view.findViewById(R.id.edit_user_full_name);
        EditText editPhone = view.findViewById(R.id.edit_user_phone);

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String username = editUsername.getText().toString().trim();
                    String email = editEmail.getText().toString().trim();
                    String password = editPassword.getText().toString().trim();
                    String role = editRole.getText().toString().trim();
                    String status = editStatus.getText().toString().trim();
                    String fullName = editFullName.getText().toString().trim();
                    String phone = editPhone.getText().toString().trim();

                    if (TextUtils.isEmpty(username)) {
                        editUsername.setError("Tên người dùng không được để trống");
                        return;
                    }
                    if (TextUtils.isEmpty(email)) {
                        editEmail.setError("Email không được để trống");
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        editPassword.setError("Mật khẩu không được để trống");
                        return;
                    }
                    if (TextUtils.isEmpty(role) || !isValidRole(role)) {
                        editRole.setError("Vai trò không hợp lệ (customer, admin, staff)");
                        return;
                    }
                    if (TextUtils.isEmpty(status) || !isValidStatus(status)) {
                        editStatus.setError("Trạng thái không hợp lệ (active, locked)");
                        return;
                    }

                    addUser(username, password, email, role, status, fullName, phone, null);
                    loadUsers();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sửa Người Dùng");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null);
        EditText editUsername = view.findViewById(R.id.edit_user_username);
        EditText editEmail = view.findViewById(R.id.edit_user_email);
        EditText editPassword = view.findViewById(R.id.edit_user_password);
        EditText editRole = view.findViewById(R.id.edit_user_role);
        EditText editStatus = view.findViewById(R.id.edit_user_status);
        EditText editFullName = view.findViewById(R.id.edit_user_full_name);
        EditText editPhone = view.findViewById(R.id.edit_user_phone);

        editUsername.setText(user.getUsername());
        editEmail.setText(user.getEmail());
        editRole.setText(user.getRole());
        editStatus.setText(user.getStatus());
        editFullName.setText(user.getFullName());
        editPhone.setText(user.getPhone());

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String username = editUsername.getText().toString().trim();
                    String email = editEmail.getText().toString().trim();
                    String password = editPassword.getText().toString().trim();
                    String role = editRole.getText().toString().trim();
                    String status = editStatus.getText().toString().trim();
                    String fullName = editFullName.getText().toString().trim();
                    String phone = editPhone.getText().toString().trim();

                    if (TextUtils.isEmpty(username)) {
                        editUsername.setError("Tên người dùng không được để trống");
                        return;
                    }
                    if (TextUtils.isEmpty(email)) {
                        editEmail.setError("Email không được để trống");
                        return;
                    }
                    if (TextUtils.isEmpty(role) || !isValidRole(role)) {
                        editRole.setError("Vai trò không hợp lệ (customer, admin, staff)");
                        return;
                    }
                    if (TextUtils.isEmpty(status) || !isValidStatus(status)) {
                        editStatus.setError("Trạng thái không hợp lệ (active, locked)");
                        return;
                    }

                    updateUser(user.getUserId(), username, password, email, role, status, fullName, phone, user.getAvatarUri());
                    loadUsers();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean isValidRole(String role) {
        for (String validRole : VALID_ROLES) {
            if (validRole.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidStatus(String status) {
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsernameExists(String username, int excludeId) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE username = ? AND user_id != ?", new String[]{username, String.valueOf(excludeId)});
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        } catch (Exception e) {
            Log.e("UserManagement", "Lỗi kiểm tra username: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi kiểm tra username: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void addUser(String username, String password, String email, String role, String status, String fullName, String phone, String avatarUri) {
        if (isUsernameExists(username, -1)) {
            Toast.makeText(requireContext(), "Tên người dùng đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            db.execSQL("INSERT INTO Users (username, password, email, role, status, full_name, phone, avatar_uri) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{username, hashedPassword, email, role, status, fullName, phone, avatarUri});
            Log.d("UserManagement", "Thêm người dùng thành công: " + username);
            Toast.makeText(requireContext(), "Thêm người dùng thành công", Toast.LENGTH_SHORT).show();
        } catch (SQLiteConstraintException e) {
            Log.e("UserManagement", "Lỗi ràng buộc: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi: Dữ liệu không hợp lệ (có thể thiếu thông tin bắt buộc)", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("UserManagement", "Lỗi thêm người dùng: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi thêm người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateUser(int userId, String username, String password, String email, String role, String status, String fullName, String phone, String avatarUri) {
        if (isUsernameExists(username, userId)) {
            Toast.makeText(requireContext(), "Tên người dùng đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            if (TextUtils.isEmpty(password)) {
                db.execSQL("UPDATE Users SET username = ?, email = ?, role = ?, status = ?, full_name = ?, phone = ?, avatar_uri = ? WHERE user_id = ?",
                        new Object[]{username, email, role, status, fullName, phone, avatarUri, userId});
            } else {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                db.execSQL("UPDATE Users SET username = ?, password = ?, email = ?, role = ?, status = ?, full_name = ?, phone = ?, avatar_uri = ? WHERE user_id = ?",
                        new Object[]{username, hashedPassword, email, role, status, fullName, phone, avatarUri, userId});
            }
            Log.d("UserManagement", "Cập nhật người dùng thành công: " + username);
            Toast.makeText(requireContext(), "Cập nhật người dùng thành công", Toast.LENGTH_SHORT).show();
        } catch (SQLiteConstraintException e) {
            Log.e("UserManagement", "Lỗi ràng buộc: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi: Dữ liệu không hợp lệ", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("UserManagement", "Lỗi cập nhật người dùng: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi cập nhật người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteUser(int userId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng này? Các thông tin liên quan (Orders, Carts, Reviews, Notifications) cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.openDatabase()) {
                        // Xóa các bản ghi liên quan trước
                        db.execSQL("DELETE FROM Orders WHERE user_id = ?", new Object[]{userId});
                        db.execSQL("DELETE FROM Carts WHERE user_id = ?", new Object[]{userId});
                        db.execSQL("DELETE FROM Reviews WHERE user_id = ?", new Object[]{userId});
                        db.execSQL("DELETE FROM Notifications WHERE user_id = ?", new Object[]{userId});
                        // Xóa người dùng
                        db.execSQL("DELETE FROM Users WHERE user_id = ?", new Object[]{userId});
                        Log.d("UserManagement", "Xóa người dùng thành công: " + userId);
                        Toast.makeText(requireContext(), "Xóa người dùng thành công", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } catch (Exception e) {
                        Log.e("UserManagement", "Lỗi xóa người dùng: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Lỗi xóa người dùng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
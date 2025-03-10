package com.example.flowerapp.Admin.Fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Models.User;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        userList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_user_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new UserAdapter(userList, requireContext(), this);
        recyclerView.setAdapter(adapter);

        loadUsers();

        Button addButton = view.findViewById(R.id.btn_add_user);
        addButton.setOnClickListener(v -> showAddUserDialog());

        return view;
    }

    private void loadUsers() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT user_id, username, email, role, status, full_name, phone, avatar_uri FROM Users", null);
            userList.clear();

            if (cursor.moveToFirst()) {
                int userIdColumn = cursor.getColumnIndex("user_id");
                int usernameColumn = cursor.getColumnIndex("username");
                int emailColumn = cursor.getColumnIndex("email");
                int roleColumn = cursor.getColumnIndex("role");
                int statusColumn = cursor.getColumnIndex("status");
                int fullNameColumn = cursor.getColumnIndex("full_name");
                int phoneColumn = cursor.getColumnIndex("phone");
                int avatarUriColumn = cursor.getColumnIndex("avatar_uri");

                if (userIdColumn < 0 || usernameColumn < 0 || emailColumn < 0 || roleColumn < 0 || statusColumn < 0) {
                    Toast.makeText(requireContext(), "Thiếu cột bắt buộc trong cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }

                do {
                    int id = cursor.getInt(userIdColumn);
                    String username = cursor.getString(usernameColumn);
                    String email = cursor.getString(emailColumn);
                    String role = cursor.getString(roleColumn);
                    String status = cursor.getString(statusColumn);
                    String fullName = fullNameColumn >= 0 ? cursor.getString(fullNameColumn) : null;
                    String phone = phoneColumn >= 0 ? cursor.getString(phoneColumn) : null;
                    String avatarUri = avatarUriColumn >= 0 ? cursor.getString(avatarUriColumn) : null;
                    userList.add(new User(id, username, email, role, status, fullName, phone, avatarUri));
                } while (cursor.moveToNext());
            }
            cursor.close();
            adapter.notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation(); // Thêm animation khi load danh sách
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi tải người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    String role = editRole.getText().toString().trim().toLowerCase();
                    String status = editStatus.getText().toString().trim().toLowerCase();
                    String fullName = editFullName.getText().toString().trim();
                    String phone = editPhone.getText().toString().trim();

                    if (!validateInput(username, email, password, role, status, phone)) {
                        return;
                    }

                    if (isEmailExists(email)) {
                        Toast.makeText(requireContext(), "Email đã được sử dụng!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                    addUser(username, hashedPassword, email, role, status, fullName, phone, null);
                    loadUsers();
                    Toast.makeText(requireContext(), "Thêm người dùng thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    private boolean validateInput(String username, String email, String password, String role, String status, String phone) {
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên người dùng", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Vui lòng nhập email hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(requireContext(), "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!role.matches("customer|admin|staff")) {
            Toast.makeText(requireContext(), "Role phải là 'customer', 'admin', hoặc 'staff'", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!status.matches("active|locked")) {
            Toast.makeText(requireContext(), "Status phải là 'active' hoặc 'locked'", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!TextUtils.isEmpty(phone) && !phone.matches("\\d{10}")) {
            Toast.makeText(requireContext(), "Số điện thoại phải là 10 chữ số", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isEmailExists(String email) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT * FROM Users WHERE email = ?", new String[]{email});
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi kiểm tra email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void addUser(String username, String password, String email, String role, String status, String fullName, String phone, String avatarUri) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Users (username, password, email, role, status, full_name, phone, avatar_uri) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new Object[]{username, password, email, role, status, fullName, phone, avatarUri});
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.fall_down)); // Animation khi thêm
            loadUsers();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi thêm người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
        editEmail.setText(user.getEmail() != null ? user.getEmail() : "");
        editPassword.setText("");
        editRole.setText(user.getRole() != null ? user.getRole() : "");
        editStatus.setText(user.getStatus() != null ? user.getStatus() : "");
        editFullName.setText(user.getFullName() != null ? user.getFullName() : "");
        editPhone.setText(user.getPhone() != null ? user.getPhone() : "");

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String username = editUsername.getText().toString().trim();
                    String email = editEmail.getText().toString().trim();
                    String password = editPassword.getText().toString().trim();
                    String role = editRole.getText().toString().trim().toLowerCase();
                    String status = editStatus.getText().toString().trim().toLowerCase();
                    String fullName = editFullName.getText().toString().trim();
                    String phone = editPhone.getText().toString().trim();

                    if (!validateInput(username, email, null, role, status, phone)) {
                        return;
                    }

                    if (!email.equals(user.getEmail()) && isEmailExists(email)) {
                        Toast.makeText(requireContext(), "Email đã được sử dụng!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateUser(user.getUserId(), username, password.isEmpty() ? null : password, email, role, status, fullName, phone, user.getAvatarUri());
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
    }

    private void updateUser(int id, String username, String password, String email, String role, String status, String fullName, String phone, String avatarUri) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            if (password == null || password.isEmpty()) {
                db.execSQL("UPDATE Users SET username = ?, email = ?, role = ?, status = ?, full_name = ?, phone = ?, avatar_uri = ? WHERE user_id = ?",
                        new Object[]{username, email, role, status, fullName, phone, avatarUri, id});
            } else {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                db.execSQL("UPDATE Users SET username = ?, password = ?, email = ?, role = ?, status = ?, full_name = ?, phone = ?, avatar_uri = ? WHERE user_id = ?",
                        new Object[]{username, hashedPassword, email, role, status, fullName, phone, avatarUri, id});
            }
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.fall_down)); // Animation khi cập nhật
            loadUsers();
            Toast.makeText(requireContext(), "Cập nhật người dùng thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi cập nhật người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteUser(int id) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa người dùng này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.openDatabase()) {
                        db.execSQL("DELETE FROM Users WHERE user_id = ?", new Object[]{id});
                        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.fall_down)); // Animation khi xóa
                        loadUsers();
                        Toast.makeText(requireContext(), "Xóa người dùng thành công", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Lỗi xóa người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private List<User> users;
        private Context context;
        private UserManagementFragment fragment;

        public UserAdapter(List<User> users, Context context, UserManagementFragment fragment) {
            this.users = users;
            this.context = context;
            this.fragment = fragment;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            User user = users.get(position);
            holder.itemView.setAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in)); // Animation cho mỗi item
            holder.usernameTextView.setText(user.getUsername() != null ? user.getUsername() : "N/A");
            holder.emailTextView.setText("Email: " + (user.getEmail() != null ? user.getEmail() : "N/A"));
            holder.fullNameTextView.setText("Họ tên: " + (user.getFullName() != null ? user.getFullName() : "N/A"));
            holder.phoneTextView.setText("SĐT: " + (user.getPhone() != null ? user.getPhone() : "N/A"));
            holder.roleStatusTextView.setText("Role: " + (user.getRole() != null ? user.getRole() : "N/A") +
                    " | Status: " + (user.getStatus() != null ? user.getStatus() : "N/A"));

            holder.btnEdit.setOnClickListener(v -> fragment.showEditUserDialog(user));
            holder.btnDelete.setOnClickListener(v -> fragment.deleteUser(user.getUserId()));
        }

        @Override
        public int getItemCount() {
            return users != null ? users.size() : 0;
        }

        public static class UserViewHolder extends RecyclerView.ViewHolder {
            public TextView usernameTextView, emailTextView, roleStatusTextView, fullNameTextView, phoneTextView;
            public com.google.android.material.button.MaterialButton btnEdit, btnDelete;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                usernameTextView = itemView.findViewById(R.id.user_username);
                emailTextView = itemView.findViewById(R.id.user_email);
                roleStatusTextView = itemView.findViewById(R.id.user_role_status);
                fullNameTextView = itemView.findViewById(R.id.user_full_name);
                phoneTextView = itemView.findViewById(R.id.user_phone);
                btnEdit = itemView.findViewById(R.id.btn_edit_user);
                btnDelete = itemView.findViewById(R.id.btn_delete_user);
            }
        }
    }
}
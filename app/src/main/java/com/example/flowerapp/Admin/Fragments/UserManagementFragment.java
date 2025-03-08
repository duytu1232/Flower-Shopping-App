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
            Cursor cursor = db.rawQuery("SELECT user_id, username, email, role, status FROM Users", null);
            userList.clear();

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                userList.add(new User(id, username, email, role, status));
            }
            cursor.close();
            adapter.notifyDataSetChanged();
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

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String username = editUsername.getText().toString().trim();
                    String email = editEmail.getText().toString().trim();
                    String password = editPassword.getText().toString().trim();
                    String role = editRole.getText().toString().trim().toLowerCase();
                    String status = editStatus.getText().toString().trim().toLowerCase();

                    if (!role.matches("customer|admin|staff") || !status.matches("active|locked")) {
                        Toast.makeText(requireContext(), "Role phải là 'customer', 'admin', hoặc 'staff'. Status phải là 'active' hoặc 'locked'", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                    addUser(username, hashedPassword, email, role, status);
                    loadUsers();
                    Toast.makeText(requireContext(), "Thêm người dùng thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void addUser(String username, String password, String email, String role, String status) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("INSERT INTO Users (username, password, email, role, status) VALUES (?, ?, ?, ?, ?)",
                    new Object[]{username, password, email, role, status});
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi thêm người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUser(int id, String username, String password, String email, String role, String status) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            if (password == null || password.isEmpty()) {
                db.execSQL("UPDATE Users SET username = ?, email = ?, role = ?, status = ? WHERE user_id = ?",
                        new Object[]{username, email, role, status, id});
            } else {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                db.execSQL("UPDATE Users SET username = ?, password = ?, email = ?, role = ?, status = ? WHERE user_id = ?",
                        new Object[]{username, hashedPassword, email, role, status, id});
            }
            loadUsers();
            Toast.makeText(requireContext(), "Cập nhật người dùng thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi cập nhật người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteUser(int id) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            db.execSQL("DELETE FROM Users WHERE user_id = ?", new Object[]{id});
            loadUsers();
            Toast.makeText(requireContext(), "Xóa người dùng thành công", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi xóa người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static class User {
        int id;
        String username, email, role, status;

        public User(int id, String username, String email, String role, String status) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
            this.status = status;
        }
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
            holder.usernameTextView.setText(user.username);
            holder.emailTextView.setText("Email: " + user.email);
            holder.roleStatusTextView.setText("Role: " + user.role + " | Status: " + user.status);

            holder.btnEdit.setOnClickListener(v -> fragment.showEditUserDialog(user));
            holder.btnDelete.setOnClickListener(v -> fragment.deleteUser(user.id));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        public static class UserViewHolder extends RecyclerView.ViewHolder {
            public TextView usernameTextView, emailTextView, roleStatusTextView;
            public Button btnEdit, btnDelete;

            public UserViewHolder(@NonNull View itemView) {
                super(itemView);
                usernameTextView = itemView.findViewById(R.id.user_username);
                emailTextView = itemView.findViewById(R.id.user_email);
                roleStatusTextView = itemView.findViewById(R.id.user_role_status);
                btnEdit = itemView.findViewById(R.id.btn_edit_user);
                btnDelete = itemView.findViewById(R.id.btn_delete_user);
            }
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

        editUsername.setText(user.username);
        editEmail.setText(user.email);
        editPassword.setText("");
        editRole.setText(user.role);
        editStatus.setText(user.status);

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String username = editUsername.getText().toString().trim();
                    String email = editEmail.getText().toString().trim();
                    String password = editPassword.getText().toString().trim();
                    String role = editRole.getText().toString().trim().toLowerCase();
                    String status = editStatus.getText().toString().trim().toLowerCase();

                    if (!role.matches("customer|admin|staff") || !status.matches("active|locked")) {
                        Toast.makeText(requireContext(), "Role phải là 'customer', 'admin', hoặc 'staff'. Status phải là 'active' hoặc 'locked'", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateUser(user.id, username, password.isEmpty() ? null : password, email, role, status);
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
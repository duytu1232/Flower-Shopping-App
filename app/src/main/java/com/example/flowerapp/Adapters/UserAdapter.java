package com.example.flowerapp.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Models.User;
import com.example.flowerapp.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.function.Consumer;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private Consumer<User> onEditClick;
    private Consumer<Integer> onDeleteClick;

    public UserAdapter(List<User> userList, Consumer<User> onEditClick, Consumer<Integer> onDeleteClick) {
        this.userList = userList;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userUsername.setText(user.getUsername());
        holder.userEmail.setText("Email: " + user.getEmail());
        holder.userFullName.setText("Họ tên: " + user.getFullName());
        holder.userPhone.setText("SĐT: " + user.getPhone());
        holder.userRoleStatus.setText("Vai trò: " + user.getRole() + " | Trạng thái: " + user.getStatus());

        holder.btnEditUser.setOnClickListener(v -> onEditClick.accept(user));
        // Sửa lỗi: thay user.getId() thành user.getUserId()
        holder.btnDeleteUser.setOnClickListener(v -> onDeleteClick.accept(user.getUserId()));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userUsername, userEmail, userFullName, userPhone, userRoleStatus;
        MaterialButton btnEditUser, btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userUsername = itemView.findViewById(R.id.user_username);
            userEmail = itemView.findViewById(R.id.user_email);
            userFullName = itemView.findViewById(R.id.user_full_name);
            userPhone = itemView.findViewById(R.id.user_phone);
            userRoleStatus = itemView.findViewById(R.id.user_role_status);
            btnEditUser = itemView.findViewById(R.id.btn_edit_user);
            btnDeleteUser = itemView.findViewById(R.id.btn_delete_user);
        }
    }
}
package com.example.flowerapp.User.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.GetContent;
import androidx.fragment.app.Fragment;

import com.example.flowerapp.AboutUsActivity;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.DangNhap;
import com.example.flowerapp.SettingsActivity;

public class FragmentAccountUser extends Fragment {

    private ImageButton userAvatar;
    private ImageView editAvatar;
    // Launcher dùng để mở thư viện ảnh và nhận URI ảnh được chọn
    private ActivityResultLauncher<String> imagePickerLauncher;

    public FragmentAccountUser() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo launcher cho việc chọn ảnh
        imagePickerLauncher = registerForActivityResult(new GetContent(), uri -> {
            if (uri != null) {
                // Cập nhật ảnh avatar khi người dùng chọn ảnh
                userAvatar.setImageURI(uri);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_user, container, false);

        // Lấy tham chiếu đến các view trong layout
        userAvatar = view.findViewById(R.id.UserAvatar);
        editAvatar = view.findViewById(R.id.editAvatar);

        // Thêm phần hiển thị tên người dùng
        TextView userNameTextView = view.findViewById(R.id.User_name);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", getActivity().MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "User");
        userNameTextView.setText(username);

        // Khi nhấn editAvatar, mở thư viện để chọn ảnh
        editAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Các nút chức năng khác
        Button signOutBtn = view.findViewById(R.id.sign_out_btn);
        signOutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DangNhap.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        Button aboutUsBtn = view.findViewById(R.id.about_us_btn);
        aboutUsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AboutUsActivity.class);
            startActivity(intent);
        });

        Button orderBtn = view.findViewById(R.id.order_btn);
        orderBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), XemDonHang.class);
            startActivity(intent);
        });

        Button settingsBtn = view.findViewById(R.id.settings_btn);
        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });
        return view;
    }
}
package com.example.flowerapp.User.Fragments;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.flowerapp.AboutUsActivity;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.DangNhap;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.example.flowerapp.AccountInfoActivity;
import com.example.flowerapp.SettingsActivity;

public class FragmentAccountUser extends Fragment {
    private ImageButton userAvatar;
    private ImageView editAvatar;
    private TextView userNameTextView;
    private Button accountInfoBtn, signOutBtn, aboutUsBtn, orderBtn, settingsBtn;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private DatabaseHelper dbHelper;

    public FragmentAccountUser() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        // Khởi tạo launcher cho chọn ảnh từ thư viện
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                userAvatar.setImageURI(uri);
                saveAvatarToDatabase(uri.toString());
            }
        });

        // Khởi tạo launcher cho camera
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == requireActivity().RESULT_OK) {
                Uri photoUri = result.getData() != null ? result.getData().getData() : null;
                if (photoUri != null) {
                    userAvatar.setImageURI(photoUri);
                    saveAvatarToDatabase(photoUri.toString());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_user, container, false);

        // Khởi tạo các view
        userAvatar = view.findViewById(R.id.UserAvatar);
        editAvatar = view.findViewById(R.id.editAvatar);
        userNameTextView = view.findViewById(R.id.User_name);
        accountInfoBtn = view.findViewById(R.id.account_info_btn);
        signOutBtn = view.findViewById(R.id.sign_out_btn);
        aboutUsBtn = view.findViewById(R.id.about_us_btn);
        orderBtn = view.findViewById(R.id.order_btn);
        settingsBtn = view.findViewById(R.id.settings_btn);

        // Hiển thị tên người dùng
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        userNameTextView.setText(username);

        // Khôi phục ảnh avatar từ database
        String avatarUri = getAvatarFromDatabase(prefs.getInt("user_id", -1));
        if (avatarUri != null) {
            userAvatar.setImageURI(Uri.parse(avatarUri));
        }

        // Xử lý chọn ảnh avatar
        if (editAvatar != null) {
            editAvatar.setOnClickListener(v -> showAvatarOptions());
        }

        // Thiết lập các sự kiện click
        setupButtonListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật tên người dùng và avatar khi fragment được quay lại
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        userNameTextView.setText(username);
        String avatarUri = getAvatarFromDatabase(prefs.getInt("user_id", -1));
        if (avatarUri != null) {
            userAvatar.setImageURI(Uri.parse(avatarUri));
        }
    }

    private void setupButtonListeners() {
        if (signOutBtn != null) {
            signOutBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Sign Out")
                        .setMessage("Are you sure you want to sign out?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.clear(); // Xóa toàn bộ dữ liệu trong SharedPreferences
                            editor.apply();
                            Intent intent = new Intent(getActivity(), DangNhap.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });
        }

        if (aboutUsBtn != null) {
            aboutUsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AboutUsActivity.class);
                startActivity(intent);
            });
        }

        if (orderBtn != null) {
            orderBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), XemDonHang.class);
                startActivity(intent);
            });
        }

        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            });
        }

        if (accountInfoBtn != null) {
            accountInfoBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), AccountInfoActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showAvatarOptions() {
        final CharSequence[] options = {"Chọn từ thư viện", "Chụp ảnh", "Hủy"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn ảnh đại diện");
        builder.setItems(options, (dialog, which) -> {
            if (options[which].equals("Chọn từ thư viện")) {
                imagePickerLauncher.launch("image/*");
            } else if (options[which].equals("Chụp ảnh")) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(takePictureIntent);
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                }
            } else if (options[which].equals("Hủy")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void saveAvatarToDatabase(String avatarUri) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId != -1) {
            try (SQLiteDatabase db = dbHelper.openDatabase()) {
                ContentValues values = new ContentValues();
                values.put("avatar_uri", avatarUri);
                db.update("Users", values, "user_id = ?", new String[]{String.valueOf(userId)});
            } catch (Exception e) {
                Log.e("FragmentAccountUser", "Lỗi lưu avatar: " + e.getMessage());
                Toast.makeText(requireContext(), "Lỗi khi lưu ảnh đại diện", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getAvatarFromDatabase(int userId) {
        if (userId == -1) return null;
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT avatar_uri FROM Users WHERE user_id = ?", new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst()) {
                int avatarIndex = cursor.getColumnIndex("avatar_uri");
                if (avatarIndex != -1) {
                    return cursor.getString(avatarIndex);
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("FragmentAccountUser", "Lỗi lấy avatar: " + e.getMessage());
        }
        return null;
    }
}
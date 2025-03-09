package com.example.flowerapp.User.Fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.CartAdapter;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class FragmentCart extends Fragment {
    private static final String TAG = "FragmentCart";

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartList;
    private TextView emptyMessage;
    private Button checkoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recycler_cart_items);
        emptyMessage = view.findViewById(R.id.empty_message);
        checkoutButton = view.findViewById(R.id.checkout_button);

        if (recyclerView == null || emptyMessage == null || checkoutButton == null) {
            Log.e(TAG, "One or more views not found in layout");
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        cartList = new ArrayList<>();

        loadCartItems();

        adapter = new CartAdapter(cartList, requireContext());
        recyclerView.setAdapter(adapter);

        updateEmptyState();

        checkoutButton.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                // Chuyển đến activity thanh toán (tạo sau nếu cần)
                Toast.makeText(requireContext(), "Proceed to checkout", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadCartItems() {
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = dbHelper.openDatabase();

        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = db.rawQuery(
                "SELECT c.cart_id, c.product_id, c.quantity, p.name, p.price, p.image_url " +
                        "FROM Cart c " +
                        "INNER JOIN Products p ON c.product_id = p.product_id " +
                        "WHERE c.user_id = ?", new String[]{String.valueOf(userId)});

        if (cursor != null) {
            int cartIdIndex = cursor.getColumnIndex("cart_id");
            int productIdIndex = cursor.getColumnIndex("product_id");
            int quantityIndex = cursor.getColumnIndex("quantity");
            int nameIndex = cursor.getColumnIndex("name");
            int priceIndex = cursor.getColumnIndex("price");
            int imageUrlIndex = cursor.getColumnIndex("image_url");

            while (cursor.moveToNext()) {
                int cartId = cartIdIndex >= 0 ? cursor.getInt(cartIdIndex) : 0;
                int productId = productIdIndex >= 0 ? cursor.getInt(productIdIndex) : 0;
                int quantity = quantityIndex >= 0 ? cursor.getInt(quantityIndex) : 0;
                String name = nameIndex >= 0 ? cursor.getString(nameIndex) : "";
                double price = priceIndex >= 0 ? cursor.getDouble(priceIndex) : 0.0;
                String imageUrl = imageUrlIndex >= 0 ? cursor.getString(imageUrlIndex) : "";
                cartList.add(new CartItem(cartId, productId, name, price, quantity, imageUrl));
            }
            cursor.close();
        }
        dbHelper.closeDatabase(db);
    }

    private void updateEmptyState() {
        if (cartList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyMessage.setVisibility(View.VISIBLE);
            emptyMessage.setText("Your cart is empty");
            checkoutButton.setEnabled(false);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
            checkoutButton.setEnabled(true);
        }
    }
}
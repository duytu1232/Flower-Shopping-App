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
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recycler_cart_items);
        emptyMessage = view.findViewById(R.id.empty_message);
        checkoutButton = view.findViewById(R.id.checkout_button);
        dbHelper = new DatabaseHelper(getContext());

        if (recyclerView == null || emptyMessage == null || checkoutButton == null) {
            Log.e(TAG, "One or more views not found in layout");
            Toast.makeText(getContext(), "Error: Missing views in layout", Toast.LENGTH_SHORT).show();
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartList = new ArrayList<>();

        loadCartItems();

        adapter = new CartAdapter(cartList, getContext(),
                this::increaseQuantity,
                this::decreaseQuantity,
                this::deleteCartItem);
        recyclerView.setAdapter(adapter);

        updateEmptyState();

        checkoutButton.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(getContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Proceed to checkout", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadCartItems() {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();

            SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor cursor = db.rawQuery(
                    "SELECT c.cart_id, c.product_id, c.quantity, p.name, p.price, p.image_url " +
                            "FROM Carts c " +
                            "INNER JOIN Products p ON c.product_id = p.product_id " +
                            "WHERE c.user_id = ?", new String[]{String.valueOf(userId)});

            if (cursor != null) {
                int cartIdIndex = cursor.getColumnIndex("cart_id");
                int productIdIndex = cursor.getColumnIndex("product_id");
                int quantityIndex = cursor.getColumnIndex("quantity");
                int nameIndex = cursor.getColumnIndex("name");
                int priceIndex = cursor.getColumnIndex("price");
                int imageUrlIndex = cursor.getColumnIndex("image_url");

                if (cartIdIndex == -1 || productIdIndex == -1 || quantityIndex == -1 ||
                        nameIndex == -1 || priceIndex == -1 || imageUrlIndex == -1) {
                    Log.e(TAG, "One or more columns do not exist in Carts or Products table!");
                    Toast.makeText(getContext(), "Error: Database columns missing", Toast.LENGTH_SHORT).show();
                } else {
                    while (cursor.moveToNext()) {
                        int cartId = cursor.getInt(cartIdIndex);
                        int productId = cursor.getInt(productIdIndex);
                        int quantity = cursor.getInt(quantityIndex);
                        String name = cursor.getString(nameIndex);
                        double price = cursor.getDouble(priceIndex);
                        String imageUrl = cursor.getString(imageUrlIndex);
                        if (name == null) name = "Unknown Product";
                        cartList.add(new CartItem(cartId, productId, name, price, quantity, imageUrl));
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading cart items: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                dbHelper.closeDatabase(db);
            }
        }
    }

    private void increaseQuantity(CartItem item) {
        item.quantity = item.getQuantity() + 1;
        updateCartItemQuantity(item);
        adapter.notifyDataSetChanged();
    }

    private void decreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            item.quantity = item.getQuantity() - 1;
            updateCartItemQuantity(item);
            adapter.notifyDataSetChanged();
        }
    }

    private void deleteCartItem(CartItem item) {
        deleteItemFromCart(item.getCartId());
        cartList.remove(item);
        updateEmptyState();
        adapter.notifyDataSetChanged();
    }

    private void updateCartItemQuantity(CartItem item) {
        boolean success = dbHelper.updateCartQuantity(item.getCartId(), item.getQuantity());
        if (!success) {
            Toast.makeText(getContext(), "Failed to update quantity", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteItemFromCart(int cartId) {
        boolean success = dbHelper.deleteCartItem(cartId);
        if (!success) {
            Toast.makeText(getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
        }
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
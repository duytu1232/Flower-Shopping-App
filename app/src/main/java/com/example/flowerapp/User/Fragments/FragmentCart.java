package com.example.flowerapp.User.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.CartAdapter;
import com.example.flowerapp.CheckoutActivity;
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
    private LinearLayout emptyMessage;
    private TextView totalPriceTextView;
    private Button checkoutButton;
    private Button continueShoppingButton;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recycler_cart_items);
        emptyMessage = view.findViewById(R.id.empty_message);
        totalPriceTextView = view.findViewById(R.id.total_price);
        checkoutButton = view.findViewById(R.id.checkout_button);
        continueShoppingButton = view.findViewById(R.id.continue_shopping_button);
        dbHelper = new DatabaseHelper(getContext());

        if (recyclerView == null || emptyMessage == null || totalPriceTextView == null || checkoutButton == null || continueShoppingButton == null) {
            Log.e(TAG, "One or more views not found in layout");
            Toast.makeText(getContext(), "Error: Missing views in layout", Toast.LENGTH_SHORT).show();
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartList = new ArrayList<>();

        adapter = new CartAdapter(cartList, getContext(),
                this::increaseQuantity,
                this::decreaseQuantity,
                this::deleteCartItem);
        recyclerView.setAdapter(adapter);

        loadCartItems();
        updateTotalPrice();
        updateEmptyState();

        checkoutButton.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(getContext(), "Your cart is empty. Add products to proceed!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), CheckoutActivity.class);
                startActivity(intent);
            }
        });

        continueShoppingButton.setOnClickListener(v -> {
            FragmentShop fragmentShop = new FragmentShop();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragmentShop);
            transaction.addToBackStack(null);
            transaction.commit();
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

            cartList.clear();
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
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error loading cart items: " + e.getMessage());
            Toast.makeText(getContext(), "Error loading cart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null) {
                dbHelper.closeDatabase(db);
            }
        }
    }

    private void increaseQuantity(CartItem item, int position) {
        int newQuantity = item.getQuantity() + 1;
        item.setQuantity(newQuantity);
        if (updateCartItemQuantity(item)) {
            adapter.notifyItemChanged(position);
            updateTotalPrice();
        } else {
            // Hoàn tác nếu cập nhật thất bại
            item.setQuantity(newQuantity - 1);
            Toast.makeText(getContext(), "Failed to update quantity", Toast.LENGTH_SHORT).show();
        }
    }

    private void decreaseQuantity(CartItem item, int position) {
        if (item.getQuantity() > 1) {
            int newQuantity = item.getQuantity() - 1;
            item.setQuantity(newQuantity);
            if (updateCartItemQuantity(item)) {
                adapter.notifyItemChanged(position);
                updateTotalPrice();
            } else {
                // Hoàn tác nếu cập nhật thất bại
                item.setQuantity(newQuantity + 1);
                Toast.makeText(getContext(), "Failed to update quantity", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteCartItem(CartItem item, int position) {
        if (deleteItemFromCart(item.getCartId())) {
            cartList.remove(item);
            adapter.notifyItemRemoved(position);
            updateTotalPrice();
            updateEmptyState();
        } else {
            Toast.makeText(getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean updateCartItemQuantity(CartItem item) {
        return dbHelper.updateCartQuantity(item.getCartId(), item.getQuantity());
    }

    private boolean deleteItemFromCart(int cartId) {
        return dbHelper.deleteCartItem(cartId);
    }

    private void updateTotalPrice() {
        double totalPrice = 0.0;
        for (CartItem item : cartList) {
            totalPrice += item.getPrice() * item.getQuantity();
        }
        totalPriceTextView.setText(String.format("Tổng tiền: %.2f VND", totalPrice));
        totalPriceTextView.setVisibility(cartList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateEmptyState() {
        if (cartList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            totalPriceTextView.setVisibility(View.GONE);
            emptyMessage.setVisibility(View.VISIBLE);
            checkoutButton.setEnabled(false);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            totalPriceTextView.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
            checkoutButton.setEnabled(true);
        }
    }
}
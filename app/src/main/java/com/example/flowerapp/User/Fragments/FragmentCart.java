package com.example.flowerapp.User.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.flowerapp.Managers.CartManager;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.R;

import java.util.ArrayList;
import java.util.List;

public class FragmentCart extends Fragment implements CartAdapter.OnCartChangeListener {
    private static final String TAG = "FragmentCart";

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartList;
    private LinearLayout emptyMessage;
    private TextView totalPriceTextView;
    private Button checkoutButton;
    private Button continueShoppingButton;
    private CartManager cartManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recycler_cart_items);
        emptyMessage = view.findViewById(R.id.empty_message);
        totalPriceTextView = view.findViewById(R.id.total_price);
        checkoutButton = view.findViewById(R.id.checkout_button);
        continueShoppingButton = view.findViewById(R.id.continue_shopping_button);
        cartManager = new CartManager(getContext());

        if (recyclerView == null || emptyMessage == null || totalPriceTextView == null || checkoutButton == null || continueShoppingButton == null) {
            Log.e(TAG, "One or more views not found in layout");
            Toast.makeText(getContext(), "Error: Missing views in layout", Toast.LENGTH_SHORT).show();
            return view;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartList = new ArrayList<>();

        adapter = new CartAdapter(cartList, getContext(), this);
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
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyPrefs", requireActivity().MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        cartList.clear();
        cartList.addAll(cartManager.loadCartItems(userId));
        adapter.notifyDataSetChanged();
    }

    private void updateTotalPrice() {
        double totalPrice = cartManager.calculateTotalPrice(cartList);
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

    @Override
    public void onCartChanged() {
        updateTotalPrice();
        updateEmptyState();
    }
}
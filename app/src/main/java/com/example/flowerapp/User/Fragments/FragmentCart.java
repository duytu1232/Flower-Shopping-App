package com.example.flowerapp.User.Fragments;

import static android.app.Activity.RESULT_OK;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.CartAdapter;
import com.example.flowerapp.CheckoutActivity;
import com.example.flowerapp.Managers.CartManager;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.Models.Coupon;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FragmentCart extends Fragment implements CartAdapter.OnCartChangeListener {
    private static final String TAG = "FragmentCart";
    private static final double MINIMUM_ORDER_VALUE = 50000.0;
    private static final int REQUEST_CODE_CHECKOUT = 1001;

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private List<CartItem> cartList;
    private LinearLayout emptyMessage;
    private TextView totalPriceTextView;
    private Button checkoutButton;
    private Button continueShoppingButton;
    private LinearLayout couponContainer;
    private TextView selectedCouponText;
    private Button applyCouponButton;
    private CartManager cartManager;
    private DatabaseHelper dbHelper;
    private Coupon selectedCoupon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        recyclerView = view.findViewById(R.id.recycler_cart_items);
        emptyMessage = view.findViewById(R.id.empty_message);
        totalPriceTextView = view.findViewById(R.id.total_price);
        checkoutButton = view.findViewById(R.id.checkout_button);
        continueShoppingButton = view.findViewById(R.id.continue_shopping_button);
        couponContainer = view.findViewById(R.id.coupon_container);
        selectedCouponText = view.findViewById(R.id.selected_coupon);
        applyCouponButton = view.findViewById(R.id.apply_coupon_button);
        cartManager = new CartManager(getContext());
        dbHelper = new DatabaseHelper(getContext());

        if (recyclerView == null || emptyMessage == null || totalPriceTextView == null ||
                checkoutButton == null || continueShoppingButton == null || couponContainer == null ||
                selectedCouponText == null || applyCouponButton == null) {
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

        applyCouponButton.setOnClickListener(v -> showCouponDialog());

        checkoutButton.setOnClickListener(v -> {
            if (cartList.isEmpty()) {
                Toast.makeText(getContext(), "Your cart is empty. Add products to proceed!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(getActivity(), CheckoutActivity.class);
                intent.putExtra("total_price", cartManager.calculateTotalPrice(cartList));
                intent.putParcelableArrayListExtra("cart_items", new ArrayList<>(cartList));
                if (selectedCoupon != null) {
                    intent.putExtra("coupon_id", selectedCoupon.getId());
                    intent.putExtra("coupon_code", selectedCoupon.getCode());
                    intent.putExtra("discount_value", selectedCoupon.getDiscountValue());
                }
                startActivityForResult(intent, REQUEST_CODE_CHECKOUT);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHECKOUT && resultCode == RESULT_OK && data != null) {
            int couponId = data.getIntExtra("coupon_id", -1);
            String couponCode = data.getStringExtra("coupon_code");
            double discountValue = data.getDoubleExtra("discount_value", 0.0);
            if (couponId != -1 && couponCode != null) {
                selectedCoupon = new Coupon(couponId, couponCode, discountValue, "", "", "active", 0.0);
            } else {
                selectedCoupon = null;
            }
            updateTotalPrice();
        }
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
        double discount = 0.0;

        if (selectedCoupon != null) {
            discount = calculateDiscount(totalPrice);
            if (discount > 0.0 && selectedCoupon != null) {
                totalPrice -= discount;
                selectedCouponText.setText(selectedCoupon.getCode() + " (-" + String.format("%.2f", discount) + " VND)");
            } else {
                selectedCoupon = null;
                selectedCouponText.setText("None");
            }
        } else {
            selectedCouponText.setText("None");
        }

        totalPriceTextView.setText(String.format("Tổng tiền: %.2f VND", totalPrice));
        totalPriceTextView.setVisibility(cartList.isEmpty() ? View.GONE : View.VISIBLE);
        couponContainer.setVisibility(cartList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private double calculateDiscount(double totalPrice) {
        if (selectedCoupon == null) return 0.0;

        if (totalPrice < MINIMUM_ORDER_VALUE) {
            Toast.makeText(getContext(),
                    String.format("Đơn hàng cần tối thiểu %.0f VND để áp dụng coupon.", MINIMUM_ORDER_VALUE),
                    Toast.LENGTH_LONG).show();
            selectedCoupon = null;
            return 0.0;
        }

        if (totalPrice < selectedCoupon.getMinOrderValue()) {
            Toast.makeText(getContext(),
                    String.format("Đơn hàng cần tối thiểu %.0f VND để sử dụng coupon %s (yêu cầu %.0f VND).",
                            MINIMUM_ORDER_VALUE, selectedCoupon.getCode(), selectedCoupon.getMinOrderValue()),
                    Toast.LENGTH_LONG).show();
            selectedCoupon = null;
            return 0.0;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (today.compareTo(selectedCoupon.getStartDate()) < 0) {
            Toast.makeText(getContext(),
                    String.format("Coupon %s chưa có hiệu lực. Hiệu lực từ %s.",
                            selectedCoupon.getCode(), selectedCoupon.getStartDate()),
                    Toast.LENGTH_LONG).show();
            selectedCoupon = null;
            return 0.0;
        }
        if (today.compareTo(selectedCoupon.getEndDate()) > 0) {
            Toast.makeText(getContext(),
                    String.format("Coupon %s đã hết hạn vào %s.",
                            selectedCoupon.getCode(), selectedCoupon.getEndDate()),
                    Toast.LENGTH_LONG).show();
            selectedCoupon = null;
            return 0.0;
        }

        if (!"active".equals(selectedCoupon.getStatus())) {
            Toast.makeText(getContext(),
                    String.format("Coupon %s hiện không khả dụng (trạng thái: %s).",
                            selectedCoupon.getCode(), selectedCoupon.getStatus()),
                    Toast.LENGTH_LONG).show();
            selectedCoupon = null;
            return 0.0;
        }

        return totalPrice * (selectedCoupon.getDiscountValue() / 100.0);
    }

    private void showCouponDialog() {
        List<Coupon> coupons = dbHelper.getAllCoupons();
        List<String> couponNames = new ArrayList<>();
        List<Coupon> validCoupons = new ArrayList<>();
        for (Coupon coupon : coupons) {
            if ("active".equals(coupon.getStatus())) {
                couponNames.add(coupon.getCode() + " (" + coupon.getDiscountValue() + "% off, Min: " + coupon.getMinOrderValue() + " VND)");
                validCoupons.add(coupon);
            }
        }
        couponNames.add("None");

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Coupon")
                .setItems(couponNames.toArray(new String[0]), (dialog, which) -> {
                    if (which == couponNames.size() - 1) {
                        selectedCoupon = null;
                    } else {
                        selectedCoupon = validCoupons.get(which);
                    }
                    updateTotalPrice();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState() {
        if (cartList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            totalPriceTextView.setVisibility(View.GONE);
            couponContainer.setVisibility(View.GONE);
            emptyMessage.setVisibility(View.VISIBLE);
            checkoutButton.setEnabled(false);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            totalPriceTextView.setVisibility(View.VISIBLE);
            couponContainer.setVisibility(View.VISIBLE);
            emptyMessage.setVisibility(View.GONE);
            checkoutButton.setEnabled(true);
        }
    }

    @Override
    public void onCartChanged() {
        loadCartItems();
        updateTotalPrice();
        updateEmptyState();
    }
}
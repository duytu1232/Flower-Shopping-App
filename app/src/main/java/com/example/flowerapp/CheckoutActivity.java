package com.example.flowerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.CheckoutAdapter;
import com.example.flowerapp.api.AddressApiService;
import com.example.flowerapp.api.ApiClient;
import com.example.flowerapp.Models.CartItem;
import com.example.flowerapp.Models.Coupon;
import com.example.flowerapp.Models.Address.District;
import com.example.flowerapp.Models.Address.Province;
import com.example.flowerapp.Models.Address.Ward;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity";
    private RecyclerView checkoutRecyclerView;
    private CheckoutAdapter checkoutAdapter;
    private List<CartItem> cartItems;
    private TextView totalPriceText, selectedCouponText;
    private Button continueToPaymentButton, applyCouponButton;
    private RadioGroup shippingMethodRadioGroup;
    private TextInputEditText firstNameInput, lastNameInput, emailInput, phoneInput, streetInput, addressDetailsInput;
    private Spinner provinceSpinner, districtSpinner, wardSpinner;
    private double totalPrice;
    private int couponId = -1;
    private String couponCode;
    private double discountValue;
    private Coupon selectedCoupon;
    private AddressApiService addressApiService;
    private List<Province> provinces;
    private List<District> districts;
    private List<Ward> wards;
    private Province selectedProvince;
    private District selectedDistrict;
    private Ward selectedWard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        addressApiService = ApiClient.getAddressApiService();
        checkoutRecyclerView = findViewById(R.id.checkout_recycler_view);
        totalPriceText = findViewById(R.id.total_price_text);
        selectedCouponText = findViewById(R.id.selected_coupon);
        applyCouponButton = findViewById(R.id.apply_coupon_button);
        continueToPaymentButton = findViewById(R.id.continue_to_payment_button);
        shippingMethodRadioGroup = findViewById(R.id.shipping_method_radio_group);
        firstNameInput = findViewById(R.id.first_name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
        provinceSpinner = findViewById(R.id.province_spinner);
        districtSpinner = findViewById(R.id.district_spinner);
        wardSpinner = findViewById(R.id.ward_spinner);
        streetInput = findViewById(R.id.street_input);
        addressDetailsInput = findViewById(R.id.address_details_input);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        totalPrice = intent.getDoubleExtra("total_price", 0.0);
        couponId = intent.getIntExtra("coupon_id", -1);
        couponCode = intent.getStringExtra("coupon_code");
        discountValue = intent.getDoubleExtra("discount_value", 0.0);
        cartItems = intent.getParcelableArrayListExtra("cart_items");

        // Kiểm tra cartItems
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "No items in cart", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo selectedCoupon từ dữ liệu Intent
        if (couponId != -1 && couponCode != null) {
            selectedCoupon = new Coupon(couponId, couponCode, discountValue, "", "", "active", 0.0);
        }

        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutAdapter = new CheckoutAdapter(cartItems, this);
        checkoutRecyclerView.setAdapter(checkoutAdapter);

        updateTotalPriceText();

        // Tải thông tin người dùng và điền sẵn vào form
        loadUserInfo();

        // Tải danh sách tỉnh/thành phố
        loadProvinces();

        // Xử lý sự kiện chọn tỉnh/thành phố
        provinceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProvince = provinces.get(position);
                loadDistricts(selectedProvince.getCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProvince = null;
                districts = null;
                wards = null;
                updateDistrictSpinner();
                updateWardSpinner();
            }
        });

        // Xử lý sự kiện chọn quận/huyện
        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDistrict = districts.get(position);
                loadWards(selectedDistrict.getCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedDistrict = null;
                wards = null;
                updateWardSpinner();
            }
        });

        // Xử lý sự kiện chọn phường/xã
        wardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWard = wards.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedWard = null;
            }
        });

        // Xử lý nút chọn coupon
        applyCouponButton.setOnClickListener(v -> {
            Intent couponIntent = new Intent(CheckoutActivity.this, CouponActivity.class);
            couponIntent.putExtra("total_price", totalPrice);
            startActivityForResult(couponIntent, 1);
        });

        // Xử lý nút tiếp tục đến thanh toán
        continueToPaymentButton.setOnClickListener(v -> {
            if (validateInputs()) {
                String shippingMethod = getSelectedShippingMethod();
                String shippingAddress = buildShippingAddress();

                Intent paymentIntent = new Intent(CheckoutActivity.this, PaymentActivity.class);
                paymentIntent.putExtra("total_price", totalPrice);
                paymentIntent.putExtra("coupon_id", couponId);
                paymentIntent.putExtra("coupon_code", couponCode);
                paymentIntent.putExtra("discount_value", discountValue);
                paymentIntent.putParcelableArrayListExtra("cart_items", new ArrayList<>(cartItems));
                paymentIntent.putExtra("shipping_method", shippingMethod);
                paymentIntent.putExtra("shipping_address", shippingAddress);
                startActivity(paymentIntent);
            }
        });
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId != -1) {
            // Giả sử bạn có một phương thức để lấy thông tin người dùng từ Firestore hoặc Room
            // Đây là ví dụ giả định
            firstNameInput.setText("John");
            lastNameInput.setText("Doe");
            emailInput.setText("john.doe@example.com");
            phoneInput.setText("1234567890");
        }
    }

    private void loadProvinces() {
        addressApiService.getProvinces().enqueue(new Callback<List<Province>>() {
            @Override
            public void onResponse(Call<List<Province>> call, Response<List<Province>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinces = response.body();
                    ArrayAdapter<Province> provinceAdapter = new ArrayAdapter<>(
                            CheckoutActivity.this,
                            android.R.layout.simple_spinner_item,
                            provinces
                    );
                    provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    provinceSpinner.setAdapter(provinceAdapter);
                } else {
                    Toast.makeText(CheckoutActivity.this, "Failed to load provinces", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                Log.e(TAG, "Error loading provinces: " + t.getMessage());
                Toast.makeText(CheckoutActivity.this, "Error loading provinces", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDistricts(int provinceCode) {
        addressApiService.getDistricts(provinceCode, 2).enqueue(new Callback<Province>() {
            @Override
            public void onResponse(Call<Province> call, Response<Province> response) {
                if (response.isSuccessful() && response.body() != null) {
                    districts = response.body().getDistricts();
                    updateDistrictSpinner();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Failed to load districts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Province> call, Throwable t) {
                Log.e(TAG, "Error loading districts: " + t.getMessage());
                Toast.makeText(CheckoutActivity.this, "Error loading districts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWards(int districtCode) {
        addressApiService.getWards(districtCode, 2).enqueue(new Callback<District>() {
            @Override
            public void onResponse(Call<District> call, Response<District> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wards = response.body().getWards();
                    updateWardSpinner();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Failed to load wards", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<District> call, Throwable t) {
                Log.e(TAG, "Error loading wards: " + t.getMessage());
                Toast.makeText(CheckoutActivity.this, "Error loading wards", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDistrictSpinner() {
        ArrayAdapter<District> districtAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                districts != null ? districts : new ArrayList<>()
        );
        districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpinner.setAdapter(districtAdapter);
    }

    private void updateWardSpinner() {
        ArrayAdapter<Ward> wardAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                wards != null ? wards : new ArrayList<>()
        );
        wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wardSpinner.setAdapter(wardAdapter);
    }

    private void updateTotalPriceText() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        double discount = 0.0;
        if (selectedCoupon != null) {
            discount = total * (discountValue / 100.0);
            total -= discount;
            selectedCouponText.setText(couponCode + " (-" + String.format("%.2f", discount) + " VND)");
        } else {
            selectedCouponText.setText("None");
        }
        totalPriceText.setText(String.format("Total: %.2f VND", total));
        totalPrice = total;
    }

    private boolean validateInputs() {
        if (firstNameInput.getText().toString().trim().isEmpty()) {
            firstNameInput.setError("First name is required");
            return false;
        }
        if (lastNameInput.getText().toString().trim().isEmpty()) {
            lastNameInput.setError("Last name is required");
            return false;
        }
        if (emailInput.getText().toString().trim().isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }
        if (phoneInput.getText().toString().trim().isEmpty()) {
            phoneInput.setError("Phone number is required");
            return false;
        }
        if (selectedProvince == null) {
            Toast.makeText(this, "Please select a province", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedDistrict == null) {
            Toast.makeText(this, "Please select a district", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedWard == null) {
            Toast.makeText(this, "Please select a ward", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (streetInput.getText().toString().trim().isEmpty()) {
            streetInput.setError("Street is required");
            return false;
        }
        return true;
    }

    private String getSelectedShippingMethod() {
        int selectedId = shippingMethodRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        return selectedRadioButton != null ? selectedRadioButton.getText().toString() : "Home delivery";
    }

    private String buildShippingAddress() {
        StringBuilder address = new StringBuilder();
        address.append(streetInput.getText().toString().trim()).append(", ");
        address.append(selectedWard.getName()).append(", ");
        address.append(selectedDistrict.getName()).append(", ");
        address.append(selectedProvince.getName());
        String details = addressDetailsInput.getText().toString().trim();
        if (!details.isEmpty()) {
            address.append(", ").append(details);
        }
        return address.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            couponId = data.getIntExtra("coupon_id", -1);
            couponCode = data.getStringExtra("coupon_code");
            discountValue = data.getDoubleExtra("discount_value", 0.0);
            if (couponId != -1 && couponCode != null) {
                selectedCoupon = new Coupon(couponId, couponCode, discountValue, "", "", "active", 0.0);
            } else {
                selectedCoupon = null;
            }
            updateTotalPriceText();
        }
    }
}
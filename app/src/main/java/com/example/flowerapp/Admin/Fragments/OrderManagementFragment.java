package com.example.flowerapp.Admin.Fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerapp.Adapters.OrderAdapter;
import com.example.flowerapp.Models.Order;
import com.example.flowerapp.Models.OrderItem;
import com.example.flowerapp.Models.Product;
import com.example.flowerapp.R;
import com.example.flowerapp.Security.Helper.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderManagementFragment extends Fragment {
    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private static final String[] VALID_STATUSES = {"pending", "processing", "shipped", "delivered", "canceled"};
    private static final String[] SHIPPING_METHODS = {"home_delivery", "pickup_point", "pickup_in_store"};
    private static final String[] PAYMENT_METHODS = {"credit_card", "momo", "cod"};
    private List<Product> productList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerViewOrders = view.findViewById(R.id.recycler_order_list);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderAdapter = new OrderAdapter(orderList, this::showEditOrderDialog, this::deleteOrder);
        recyclerViewOrders.setAdapter(orderAdapter);

        view.findViewById(R.id.btn_add_order).setOnClickListener(v -> showAddOrderDialog());

        loadProducts();
        loadOrders();
        return view;
    }

    private void loadProducts() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT product_id, name, price, stock FROM Products", null);
            productList.clear();
            while (cursor.moveToNext()) {
                int productId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                int stock = cursor.getInt(cursor.getColumnIndexOrThrow("stock"));
                productList.add(new Product(productId, name, null, price, stock, null, null));
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("OrderManagement", "Error loading products: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error loading products: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadOrders() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            orderList.clear();
            Cursor orderCursor = db.rawQuery(
                    "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                            "o.shipping_method, pm.payment_method " +
                            "FROM Orders o " +
                            "LEFT JOIN Payments pm ON o.order_id = pm.order_id", null);
            while (orderCursor.moveToNext()) {
                int id = orderCursor.getInt(orderCursor.getColumnIndexOrThrow("order_id"));
                int userId = orderCursor.getInt(orderCursor.getColumnIndexOrThrow("user_id"));
                String orderDate = orderCursor.getString(orderCursor.getColumnIndexOrThrow("order_date"));
                String status = orderCursor.getString(orderCursor.getColumnIndexOrThrow("status"));
                double totalAmount = orderCursor.getDouble(orderCursor.getColumnIndexOrThrow("total_amount"));
                String shippingAddress = orderCursor.getString(orderCursor.getColumnIndexOrThrow("shipping_address"));
                String shippingMethod = orderCursor.getString(orderCursor.getColumnIndexOrThrow("shipping_method"));
                String paymentMethod = orderCursor.getString(orderCursor.getColumnIndexOrThrow("payment_method"));

                List<OrderItem> orderItems = new ArrayList<>();
                Cursor itemCursor = db.rawQuery(
                        "SELECT oi.order_item_id, oi.product_id, p.name, p.image_url, oi.quantity, oi.unit_price " +
                                "FROM Order_Items oi " +
                                "JOIN Products p ON oi.product_id = p.product_id " +
                                "WHERE oi.order_id = ?", new String[]{String.valueOf(id)});
                while (itemCursor.moveToNext()) {
                    int orderItemId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("order_item_id"));
                    int productId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("product_id"));
                    String productName = itemCursor.getString(itemCursor.getColumnIndexOrThrow("name"));
                    String imageUrl = itemCursor.getString(itemCursor.getColumnIndexOrThrow("image_url"));
                    int quantity = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("quantity"));
                    double unitPrice = itemCursor.getDouble(itemCursor.getColumnIndexOrThrow("unit_price"));
                    orderItems.add(new OrderItem(orderItemId, id, productId, productName, imageUrl, quantity, unitPrice));
                }
                itemCursor.close();

                shippingMethod = shippingMethod != null ? shippingMethod : "home_delivery";
                paymentMethod = paymentMethod != null ? paymentMethod : "Unknown";
                String title = orderItems.isEmpty() ? "Unknown Product" : orderItems.get(0).getProductName();
                String imageUrl = orderItems.isEmpty() ? "" : orderItems.get(0).getImageUrl();

                orderList.add(new Order(id, userId, orderDate, status, totalAmount, shippingAddress, shippingMethod, paymentMethod, title, imageUrl, orderItems));
            }
            orderCursor.close();
            orderAdapter.notifyDataSetChanged();
            recyclerViewOrders.scheduleLayoutAnimation();
        } catch (Exception e) {
            Log.e("OrderManagement", "Error loading orders: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error loading orders: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Order");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null);
        TextInputEditText editUserId = view.findViewById(R.id.edit_order_user_id);
        TextInputEditText editOrderDate = view.findViewById(R.id.edit_order_date);
        Spinner spinnerStatus = view.findViewById(R.id.spinner_order_status); // Thay TextInputEditText bằng Spinner
        TextInputEditText editTotal = view.findViewById(R.id.edit_order_total);
        TextInputEditText editAddress = view.findViewById(R.id.edit_order_address);
        Spinner spinnerProduct = view.findViewById(R.id.spinner_product);
        TextInputEditText editQuantity = view.findViewById(R.id.edit_quantity);
        Spinner spinnerShippingMethod = view.findViewById(R.id.spinner_shipping_method); // Thêm Spinner mới
        Spinner spinnerPaymentMethod = view.findViewById(R.id.spinner_payment_method);   // Thêm Spinner mới

        ArrayAdapter<Product> productAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, productList);
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(productAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, VALID_STATUSES);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<String> shippingAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, SHIPPING_METHODS);
        shippingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShippingMethod.setAdapter(shippingAdapter);

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, PAYMENT_METHODS);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(paymentAdapter);

        builder.setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    try {
                        int userId = Integer.parseInt(editUserId.getText().toString().trim());
                        String orderDate = editOrderDate.getText().toString().trim();
                        String status = spinnerStatus.getSelectedItem().toString();
                        double totalAmount = Double.parseDouble(editTotal.getText().toString().trim());
                        String shippingAddress = editAddress.getText().toString().trim();
                        Product selectedProduct = (Product) spinnerProduct.getSelectedItem();
                        int quantity = Integer.parseInt(editQuantity.getText().toString().trim());
                        String shippingMethod = spinnerShippingMethod.getSelectedItem().toString();
                        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString();

                        if (!isUserExists(userId)) {
                            editUserId.setError("User ID does not exist");
                            return;
                        }
                        if (TextUtils.isEmpty(orderDate) || !isValidDateFormat(orderDate)) {
                            editOrderDate.setError("Invalid order date (format: yyyy-MM-dd)");
                            return;
                        }
                        if (totalAmount <= 0) {
                            editTotal.setError("Total must be greater than 0");
                            return;
                        }
                        if (TextUtils.isEmpty(shippingAddress)) {
                            editAddress.setError("Shipping address cannot be empty");
                            return;
                        }
                        if (selectedProduct == null) {
                            Toast.makeText(requireContext(), "Please select a product", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (quantity <= 0) {
                            editQuantity.setError("Quantity must be greater than 0");
                            return;
                        }
                        if (quantity > selectedProduct.getStock()) {
                            editQuantity.setError("Quantity exceeds stock (" + selectedProduct.getStock() + ")");
                            return;
                        }

                        addOrder(userId, orderDate, status, totalAmount, shippingAddress, selectedProduct.getId(), quantity, shippingMethod, paymentMethod);
                        loadOrders();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Please enter valid number format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditOrderDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Order");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null);
        TextInputEditText editOrderId = view.findViewById(R.id.edit_order_id);
        TextInputEditText editUserId = view.findViewById(R.id.edit_order_user_id);
        TextInputEditText editOrderDate = view.findViewById(R.id.edit_order_date);
        Spinner spinnerStatus = view.findViewById(R.id.spinner_order_status); // Thay TextInputEditText bằng Spinner
        TextInputEditText editTotal = view.findViewById(R.id.edit_order_total);
        TextInputEditText editAddress = view.findViewById(R.id.edit_order_address);
        Spinner spinnerProduct = view.findViewById(R.id.spinner_product);
        TextInputEditText editQuantity = view.findViewById(R.id.edit_quantity);
        Spinner spinnerShippingMethod = view.findViewById(R.id.spinner_shipping_method); // Thêm Spinner mới
        Spinner spinnerPaymentMethod = view.findViewById(R.id.spinner_payment_method);   // Thêm Spinner mới

        ArrayAdapter<Product> productAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, productList);
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(productAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, VALID_STATUSES);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        ArrayAdapter<String> shippingAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, SHIPPING_METHODS);
        shippingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerShippingMethod.setAdapter(shippingAdapter);

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, PAYMENT_METHODS);
        paymentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMethod.setAdapter(paymentAdapter);

        editOrderId.setText(String.valueOf(order.getId()));
        editUserId.setText(String.valueOf(order.getUserId()));
        editOrderDate.setText(order.getOrderDate());
        for (int i = 0; i < VALID_STATUSES.length; i++) {
            if (VALID_STATUSES[i].equals(order.getStatus())) {
                spinnerStatus.setSelection(i);
                break;
            }
        }
        editTotal.setText(String.valueOf(order.getTotalAmount()));
        editAddress.setText(order.getShippingAddress());
        if (!order.getOrderItems().isEmpty()) {
            int productId = order.getOrderItems().get(0).getProductId();
            int quantity = order.getOrderItems().get(0).getQuantity();
            for (int i = 0; i < productList.size(); i++) {
                if (productList.get(i).getId() == productId) {
                    spinnerProduct.setSelection(i);
                    break;
                }
            }
            editQuantity.setText(String.valueOf(quantity));
        }
        for (int i = 0; i < SHIPPING_METHODS.length; i++) {
            if (SHIPPING_METHODS[i].equals(order.getShippingMethod())) {
                spinnerShippingMethod.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < PAYMENT_METHODS.length; i++) {
            if (PAYMENT_METHODS[i].equals(order.getPaymentMethod())) {
                spinnerPaymentMethod.setSelection(i);
                break;
            }
        }

        builder.setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    try {
                        int userId = Integer.parseInt(editUserId.getText().toString().trim());
                        String orderDate = editOrderDate.getText().toString().trim();
                        String status = spinnerStatus.getSelectedItem().toString();
                        double totalAmount = Double.parseDouble(editTotal.getText().toString().trim());
                        String shippingAddress = editAddress.getText().toString().trim();
                        Product selectedProduct = (Product) spinnerProduct.getSelectedItem();
                        int quantity = Integer.parseInt(editQuantity.getText().toString().trim());
                        String shippingMethod = spinnerShippingMethod.getSelectedItem().toString();
                        String paymentMethod = spinnerPaymentMethod.getSelectedItem().toString();

                        if (!isUserExists(userId)) {
                            editUserId.setError("User ID does not exist");
                            return;
                        }
                        if (TextUtils.isEmpty(orderDate) || !isValidDateFormat(orderDate)) {
                            editOrderDate.setError("Invalid order date (format: yyyy-MM-dd)");
                            return;
                        }
                        if (totalAmount <= 0) {
                            editTotal.setError("Total must be greater than 0");
                            return;
                        }
                        if (TextUtils.isEmpty(shippingAddress)) {
                            editAddress.setError("Shipping address cannot be empty");
                            return;
                        }
                        if (selectedProduct == null) {
                            Toast.makeText(requireContext(), "Please select a product", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (quantity <= 0) {
                            editQuantity.setError("Quantity must be greater than 0");
                            return;
                        }
                        if (quantity > selectedProduct.getStock()) {
                            editQuantity.setError("Quantity exceeds stock (" + selectedProduct.getStock() + ")");
                            return;
                        }

                        updateOrder(order.getId(), userId, orderDate, status, totalAmount, shippingAddress, selectedProduct.getId(), quantity, shippingMethod, paymentMethod);
                        loadOrders();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Please enter valid number format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean isUserExists(int userId) {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM Users WHERE user_id = ?", new String[]{String.valueOf(userId)});
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
            return false;
        } catch (Exception e) {
            Log.e("OrderManagement", "Error checking user_id: " + e.getMessage(), e);
            return false;
        }
    }

    private boolean isValidDateFormat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private void addOrder(int userId, String orderDate, String status, double totalAmount, String shippingAddress, int productId, int quantity, String shippingMethod, String paymentMethod) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            db.beginTransaction();

            db.execSQL("INSERT INTO Orders (user_id, order_date, status, total_amount, shipping_address, shipping_method) VALUES (?, ?, ?, ?, ?, ?)",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress, shippingMethod});

            Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            int orderId = -1;
            if (cursor.moveToFirst()) {
                orderId = cursor.getInt(0);
            }
            cursor.close();

            if (orderId == -1) throw new Exception("Failed to get order_id");

            double unitPrice = 0;
            Cursor priceCursor = db.rawQuery("SELECT price FROM Products WHERE product_id = ?", new String[]{String.valueOf(productId)});
            if (priceCursor.moveToFirst()) {
                unitPrice = priceCursor.getDouble(0);
            }
            priceCursor.close();

            db.execSQL("INSERT INTO Order_Items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
                    new Object[]{orderId, productId, quantity, unitPrice});

            db.execSQL("UPDATE Products SET stock = stock - ? WHERE product_id = ?",
                    new Object[]{quantity, productId});

            db.execSQL("INSERT INTO Payments (order_id, payment_method) VALUES (?, ?)",
                    new Object[]{orderId, paymentMethod});

            db.setTransactionSuccessful();
            Toast.makeText(requireContext(), "Order added successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("OrderManagement", "Error adding order: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error adding order: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) {
                if (db.inTransaction()) db.endTransaction();
                dbHelper.closeDatabase(db);
            }
        }
    }

    private void updateOrder(int id, int userId, String orderDate, String status, double totalAmount, String shippingAddress, int productId, int quantity, String shippingMethod, String paymentMethod) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            db.beginTransaction();

            int oldQuantity = 0, oldProductId = 0;
            Cursor oldItemCursor = db.rawQuery("SELECT product_id, quantity FROM Order_Items WHERE order_id = ? LIMIT 1", new String[]{String.valueOf(id)});
            if (oldItemCursor.moveToFirst()) {
                oldProductId = oldItemCursor.getInt(oldItemCursor.getColumnIndexOrThrow("product_id"));
                oldQuantity = oldItemCursor.getInt(oldItemCursor.getColumnIndexOrThrow("quantity"));
            }
            oldItemCursor.close();

            if (oldProductId != 0) {
                db.execSQL("UPDATE Products SET stock = stock + ? WHERE product_id = ?",
                        new Object[]{oldQuantity, oldProductId});
            }

            db.execSQL("UPDATE Orders SET user_id = ?, order_date = ?, status = ?, total_amount = ?, shipping_address = ?, shipping_method = ? WHERE order_id = ?",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress, shippingMethod, id});

            db.execSQL("DELETE FROM Order_Items WHERE order_id = ?", new Object[]{id});

            double unitPrice = 0;
            Cursor priceCursor = db.rawQuery("SELECT price FROM Products WHERE product_id = ?", new String[]{String.valueOf(productId)});
            if (priceCursor.moveToFirst()) {
                unitPrice = priceCursor.getDouble(0);
            }
            priceCursor.close();

            db.execSQL("INSERT INTO Order_Items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
                    new Object[]{id, productId, quantity, unitPrice});

            db.execSQL("UPDATE Products SET stock = stock - ? WHERE product_id = ?",
                    new Object[]{quantity, productId});

            db.execSQL("UPDATE Payments SET payment_method = ? WHERE order_id = ?",
                    new Object[]{paymentMethod, id});

            db.setTransactionSuccessful();
            Toast.makeText(requireContext(), "Order updated successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("OrderManagement", "Error updating order: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error updating order: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) {
                if (db.inTransaction()) db.endTransaction();
                dbHelper.closeDatabase(db);
            }
        }
    }

    private void deleteOrder(Order order) {
        // Giữ nguyên hàm này
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            db.beginTransaction();

            int quantity = 0, productId = 0;
            Cursor itemCursor = db.rawQuery("SELECT product_id, quantity FROM Order_Items WHERE order_id = ?", new String[]{String.valueOf(order.getId())});
            if (itemCursor.moveToFirst()) {
                productId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("product_id"));
                quantity = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("quantity"));
            }
            itemCursor.close();

            if (productId != 0) {
                db.execSQL("UPDATE Products SET stock = stock + ? WHERE product_id = ?",
                        new Object[]{quantity, productId});
            }

            db.execSQL("DELETE FROM Order_Items WHERE order_id = ?", new Object[]{order.getId()});
            db.execSQL("DELETE FROM Payments WHERE order_id = ?", new Object[]{order.getId()});
            db.execSQL("DELETE FROM Notifications WHERE order_id = ?", new Object[]{order.getId()});
            db.execSQL("DELETE FROM Order_Tracking WHERE order_id = ?", new Object[]{order.getId()});
            db.execSQL("DELETE FROM Orders WHERE order_id = ?", new Object[]{order.getId()});

            db.setTransactionSuccessful();
            Toast.makeText(requireContext(), "Order deleted successfully", Toast.LENGTH_SHORT).show();
            loadOrders();
        } catch (Exception e) {
            Log.e("OrderManagement", "Error deleting order: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error deleting order: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) {
                if (db.inTransaction()) db.endTransaction();
                dbHelper.closeDatabase(db);
            }
        }
    }
}
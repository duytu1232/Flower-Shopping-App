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
    private List<Product> productList = new ArrayList<>(); // Danh sách sản phẩm để chọn

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_management, container, false);

        dbHelper = new DatabaseHelper(requireContext());

        // Khởi tạo RecyclerView cho danh sách đơn hàng
        recyclerViewOrders = view.findViewById(R.id.recycler_order_list);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderAdapter = new OrderAdapter(orderList, this::showEditOrderDialog, this::deleteOrder);
        recyclerViewOrders.setAdapter(orderAdapter);

        view.findViewById(R.id.btn_add_order).setOnClickListener(v -> showAddOrderDialog());

        loadProducts(); // Tải danh sách sản phẩm để chọn
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
            Log.e("OrderManagement", "Lỗi tải sản phẩm: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadOrders() {
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery(
                    "SELECT o.order_id, o.user_id, o.order_date, o.status, o.total_amount, o.shipping_address, " +
                            "o.shipping_method, pm.payment_method, p.name AS product_name, p.image_url " +
                            "FROM Orders o " +
                            "LEFT JOIN Order_Items oi ON o.order_id = oi.order_id " +
                            "LEFT JOIN Products p ON oi.product_id = p.product_id " +
                            "LEFT JOIN Payments pm ON o.order_id = pm.order_id", null);
            orderList.clear();
            Log.d("OrderManagement", "Số lượng bản ghi: " + cursor.getCount());
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("order_id"));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
                String orderDate = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                String shippingAddress = cursor.getString(cursor.getColumnIndexOrThrow("shipping_address"));
                String shippingMethod = cursor.getString(cursor.getColumnIndexOrThrow("shipping_method"));
                String paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow("payment_method"));
                String productName = cursor.getString(cursor.getColumnIndexOrThrow("product_name"));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow("image_url"));

                productName = (productName != null) ? productName : "Unknown Product";
                imageUrl = (imageUrl != null) ? imageUrl : "";
                shippingMethod = (shippingMethod != null) ? shippingMethod : "home_delivery";
                paymentMethod = (paymentMethod != null) ? paymentMethod : "Unknown";

                orderList.add(new Order(id, userId, orderDate, status, totalAmount, shippingAddress, shippingMethod, paymentMethod, productName, imageUrl));
                Log.d("OrderManagement", "Thêm đơn hàng: " + id);
            }
            cursor.close();
            orderAdapter.notifyDataSetChanged();
            recyclerViewOrders.scheduleLayoutAnimation();
            Log.d("OrderManagement", "Số lượng đơn hàng trong list: " + orderList.size());
        } catch (Exception e) {
            Log.e("OrderManagement", "Lỗi tải đơn hàng: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddOrderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Thêm Đơn Hàng");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null);
        TextInputEditText editUserId = view.findViewById(R.id.edit_order_user_id);
        TextInputEditText editOrderDate = view.findViewById(R.id.edit_order_date);
        TextInputEditText editStatus = view.findViewById(R.id.edit_order_status);
        TextInputEditText editTotal = view.findViewById(R.id.edit_order_total);
        TextInputEditText editAddress = view.findViewById(R.id.edit_order_address);
        Spinner spinnerProduct = view.findViewById(R.id.spinner_product);
        TextInputEditText editQuantity = view.findViewById(R.id.edit_quantity);

        // Thiết lập Spinner cho danh sách sản phẩm
        ArrayAdapter<Product> productAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, productList);
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(productAdapter);

        builder.setView(view)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    try {
                        int userId = Integer.parseInt(editUserId.getText().toString().trim());
                        String orderDate = editOrderDate.getText().toString().trim();
                        String status = editStatus.getText().toString().trim();
                        double totalAmount = Double.parseDouble(editTotal.getText().toString().trim());
                        String shippingAddress = editAddress.getText().toString().trim();
                        Product selectedProduct = (Product) spinnerProduct.getSelectedItem();
                        int quantity = Integer.parseInt(editQuantity.getText().toString().trim());

                        if (!isUserExists(userId)) {
                            editUserId.setError("User ID không tồn tại");
                            return;
                        }
                        if (TextUtils.isEmpty(orderDate) || !isValidDateFormat(orderDate)) {
                            editOrderDate.setError("Ngày đặt hàng không hợp lệ (định dạng: yyyy-MM-dd)");
                            return;
                        }
                        if (TextUtils.isEmpty(status) || !isValidStatus(status)) {
                            editStatus.setError("Trạng thái không hợp lệ (pending, processing, shipped, delivered, canceled)");
                            return;
                        }
                        if (totalAmount <= 0) {
                            editTotal.setError("Tổng tiền phải lớn hơn 0");
                            return;
                        }
                        if (TextUtils.isEmpty(shippingAddress)) {
                            editAddress.setError("Địa chỉ giao hàng không được để trống");
                            return;
                        }
                        if (selectedProduct == null) {
                            Toast.makeText(requireContext(), "Vui lòng chọn một sản phẩm", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (quantity <= 0) {
                            editQuantity.setError("Số lượng phải lớn hơn 0");
                            return;
                        }
                        // Kiểm tra số lượng tồn kho
                        if (quantity > selectedProduct.getStock()) {
                            editQuantity.setError("Số lượng vượt quá tồn kho (" + selectedProduct.getStock() + ")");
                            return;
                        }

                        addOrder(userId, orderDate, status, totalAmount, shippingAddress, selectedProduct.getId(), quantity);
                        loadOrders();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditOrderDialog(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Sửa Đơn Hàng");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_order, null);
        TextInputEditText editOrderId = view.findViewById(R.id.edit_order_id); // Thêm dòng này
        TextInputEditText editUserId = view.findViewById(R.id.edit_order_user_id);
        TextInputEditText editOrderDate = view.findViewById(R.id.edit_order_date);
        TextInputEditText editStatus = view.findViewById(R.id.edit_order_status);
        TextInputEditText editTotal = view.findViewById(R.id.edit_order_total);
        TextInputEditText editAddress = view.findViewById(R.id.edit_order_address);
        Spinner spinnerProduct = view.findViewById(R.id.spinner_product);
        TextInputEditText editQuantity = view.findViewById(R.id.edit_quantity);

        // Thiết lập Spinner cho danh sách sản phẩm
        ArrayAdapter<Product> productAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, productList);
        productAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProduct.setAdapter(productAdapter);

        // Điền dữ liệu hiện tại
        editOrderId.setText(String.valueOf(order.getId())); // Thêm dòng này
        editUserId.setText(String.valueOf(order.getUserId()));
        editOrderDate.setText(order.getOrderDate());
        editStatus.setText(order.getStatus());
        editTotal.setText(String.valueOf(order.getTotalAmount()));
        editAddress.setText(order.getShippingAddress());

        // Tìm sản phẩm hiện tại trong Order_Items (giả định chỉ có 1 sản phẩm cho đơn giản)
        try (SQLiteDatabase db = dbHelper.openDatabase()) {
            Cursor cursor = db.rawQuery("SELECT product_id, quantity FROM Order_Items WHERE order_id = ? LIMIT 1", new String[]{String.valueOf(order.getId())});
            if (cursor.moveToFirst()) {
                int productId = cursor.getInt(cursor.getColumnIndexOrThrow("product_id"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                editQuantity.setText(String.valueOf(quantity));
                for (int i = 0; i < productList.size(); i++) {
                    if (productList.get(i).getId() == productId) {
                        spinnerProduct.setSelection(i);
                        break;
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("OrderManagement", "Lỗi tải Order_Items: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi tải Order_Items: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        builder.setView(view)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    try {
                        int userId = Integer.parseInt(editUserId.getText().toString().trim());
                        String orderDate = editOrderDate.getText().toString().trim();
                        String status = editStatus.getText().toString().trim();
                        double totalAmount = Double.parseDouble(editTotal.getText().toString().trim());
                        String shippingAddress = editAddress.getText().toString().trim();
                        Product selectedProduct = (Product) spinnerProduct.getSelectedItem();
                        int quantity = Integer.parseInt(editQuantity.getText().toString().trim());

                        if (!isUserExists(userId)) {
                            editUserId.setError("User ID không tồn tại");
                            return;
                        }
                        if (TextUtils.isEmpty(orderDate) || !isValidDateFormat(orderDate)) {
                            editOrderDate.setError("Ngày đặt hàng không hợp lệ (định dạng: yyyy-MM-dd)");
                            return;
                        }
                        if (TextUtils.isEmpty(status) || !isValidStatus(status)) {
                            editStatus.setError("Trạng thái không hợp lệ (pending, processing, shipped, delivered, canceled)");
                            return;
                        }
                        if (totalAmount <= 0) {
                            editTotal.setError("Tổng tiền phải lớn hơn 0");
                            return;
                        }
                        if (TextUtils.isEmpty(shippingAddress)) {
                            editAddress.setError("Địa chỉ giao hàng không được để trống");
                            return;
                        }
                        if (selectedProduct == null) {
                            Toast.makeText(requireContext(), "Vui lòng chọn một sản phẩm", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (quantity <= 0) {
                            editQuantity.setError("Số lượng phải lớn hơn 0");
                            return;
                        }
                        // Kiểm tra số lượng tồn kho
                        if (quantity > selectedProduct.getStock()) {
                            editQuantity.setError("Số lượng vượt quá tồn kho (" + selectedProduct.getStock() + ")");
                            return;
                        }

                        updateOrder(order.getId(), userId, orderDate, status, totalAmount, shippingAddress, selectedProduct.getId(), quantity);
                        loadOrders();
                    } catch (NumberFormatException e) {
                        Toast.makeText(requireContext(), "Vui lòng nhập đúng định dạng số", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
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
            Log.e("OrderManagement", "Lỗi kiểm tra user_id: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi kiểm tra user_id: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

    private boolean isValidStatus(String status) {
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    private void addOrder(int userId, String orderDate, String status, double totalAmount, String shippingAddress, int productId, int quantity) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            db.beginTransaction();

            // Thêm vào bảng Orders
            db.execSQL("INSERT INTO Orders (user_id, order_date, status, total_amount, shipping_address, shipping_method) VALUES (?, ?, ?, ?, ?, ?)",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress, "home_delivery"});

            // Lấy order_id vừa thêm
            Cursor cursor = db.rawQuery("SELECT last_insert_rowid()", null);
            int orderId = -1;
            if (cursor.moveToFirst()) {
                orderId = cursor.getInt(0);
            }
            cursor.close();

            if (orderId == -1) {
                throw new Exception("Không thể lấy order_id sau khi thêm đơn hàng");
            }

            // Lấy giá sản phẩm để tính unit_price
            double unitPrice = 0;
            Cursor priceCursor = db.rawQuery("SELECT price FROM Products WHERE product_id = ?", new String[]{String.valueOf(productId)});
            if (priceCursor.moveToFirst()) {
                unitPrice = priceCursor.getDouble(0);
            }
            priceCursor.close();

            // Thêm vào bảng Order_Items
            db.execSQL("INSERT INTO Order_Items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
                    new Object[]{orderId, productId, quantity, unitPrice});

            // Cập nhật số lượng tồn kho
            db.execSQL("UPDATE Products SET stock = stock - ? WHERE product_id = ?",
                    new Object[]{quantity, productId});

            db.setTransactionSuccessful();
            Log.d("OrderManagement", "Thêm đơn hàng thành công: " + orderId);
            Toast.makeText(requireContext(), "Thêm đơn hàng thành công", Toast.LENGTH_SHORT).show();
        } catch (SQLiteConstraintException e) {
            Log.e("OrderManagement", "Lỗi ràng buộc: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi: Dữ liệu không hợp lệ (có thể thiếu thông tin bắt buộc)", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("OrderManagement", "Lỗi thêm đơn hàng: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi thêm đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) {
                try {
                    if (db.isOpen() && db.inTransaction()) {
                        db.endTransaction();
                    }
                    if (db.isOpen()) {
                        dbHelper.closeDatabase(db);
                    }
                } catch (Exception e) {
                    Log.e("OrderManagement", "Lỗi khi đóng database: " + e.getMessage(), e);
                }
            }
        }
    }

    private void updateOrder(int id, int userId, String orderDate, String status, double totalAmount, String shippingAddress, int productId, int quantity) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.openDatabase();
            db.beginTransaction();

            // Lấy số lượng cũ từ Order_Items để cập nhật lại stock
            int oldQuantity = 0;
            int oldProductId = 0;
            Cursor oldItemCursor = db.rawQuery("SELECT product_id, quantity FROM Order_Items WHERE order_id = ? LIMIT 1", new String[]{String.valueOf(id)});
            if (oldItemCursor.moveToFirst()) {
                oldProductId = oldItemCursor.getInt(oldItemCursor.getColumnIndexOrThrow("product_id"));
                oldQuantity = oldItemCursor.getInt(oldItemCursor.getColumnIndexOrThrow("quantity"));
            }
            oldItemCursor.close();

            // Cập nhật stock cho sản phẩm cũ
            if (oldProductId != 0) {
                db.execSQL("UPDATE Products SET stock = stock + ? WHERE product_id = ?",
                        new Object[]{oldQuantity, oldProductId});
            }

            // Cập nhật bảng Orders
            db.execSQL("UPDATE Orders SET user_id = ?, order_date = ?, status = ?, total_amount = ?, shipping_address = ? WHERE order_id = ?",
                    new Object[]{userId, orderDate, status, totalAmount, shippingAddress, id});

            // Xóa các Order_Items cũ
            db.execSQL("DELETE FROM Order_Items WHERE order_id = ?", new Object[]{id});

            // Lấy giá sản phẩm để tính unit_price
            double unitPrice = 0;
            Cursor priceCursor = db.rawQuery("SELECT price FROM Products WHERE product_id = ?", new String[]{String.valueOf(productId)});
            if (priceCursor.moveToFirst()) {
                unitPrice = priceCursor.getDouble(0);
            }
            priceCursor.close();

            // Thêm Order_Items mới
            db.execSQL("INSERT INTO Order_Items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
                    new Object[]{id, productId, quantity, unitPrice});

            // Cập nhật số lượng tồn kho cho sản phẩm mới
            db.execSQL("UPDATE Products SET stock = stock - ? WHERE product_id = ?",
                    new Object[]{quantity, productId});

            db.setTransactionSuccessful();
            Log.d("OrderManagement", "Cập nhật đơn hàng thành công: " + id);
            Toast.makeText(requireContext(), "Cập nhật đơn hàng thành công", Toast.LENGTH_SHORT).show();
        } catch (SQLiteConstraintException e) {
            Log.e("OrderManagement", "Lỗi ràng buộc: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi: Dữ liệu không hợp lệ", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("OrderManagement", "Lỗi cập nhật đơn hàng: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Lỗi cập nhật đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) {
                try {
                    if (db.isOpen() && db.inTransaction()) {
                        db.endTransaction();
                    }
                    if (db.isOpen()) {
                        dbHelper.closeDatabase(db);
                    }
                } catch (Exception e) {
                    Log.e("OrderManagement", "Lỗi khi đóng database: " + e.getMessage(), e);
                }
            }
        }
    }

    private void deleteOrder(Order order) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đơn hàng này? Các thông tin liên quan (Order_Items, Payments, Notifications, Order_Tracking) cũng sẽ bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    SQLiteDatabase db = null;
                    try {
                        db = dbHelper.openDatabase();
                        db.beginTransaction();

                        // Lấy số lượng và product_id từ Order_Items để cập nhật stock
                        int quantity = 0;
                        int productId = 0;
                        Cursor itemCursor = db.rawQuery("SELECT product_id, quantity FROM Order_Items WHERE order_id = ?", new String[]{String.valueOf(order.getId())});
                        if (itemCursor.moveToFirst()) {
                            productId = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("product_id"));
                            quantity = itemCursor.getInt(itemCursor.getColumnIndexOrThrow("quantity"));
                        }
                        itemCursor.close();

                        // Cập nhật stock
                        if (productId != 0) {
                            db.execSQL("UPDATE Products SET stock = stock + ? WHERE product_id = ?",
                                    new Object[]{quantity, productId});
                        }

                        // Xóa các bảng liên quan trước
                        db.execSQL("DELETE FROM Order_Items WHERE order_id = ?", new Object[]{order.getId()});
                        db.execSQL("DELETE FROM Payments WHERE order_id = ?", new Object[]{order.getId()});
                        db.execSQL("DELETE FROM Notifications WHERE order_id = ?", new Object[]{order.getId()});
                        db.execSQL("DELETE FROM Order_Tracking WHERE order_id = ?", new Object[]{order.getId()});
                        db.execSQL("DELETE FROM Orders WHERE order_id = ?", new Object[]{order.getId()});

                        db.setTransactionSuccessful();
                        Log.d("OrderManagement", "Xóa đơn hàng thành công: " + order.getId());
                        Toast.makeText(requireContext(), "Xóa đơn hàng thành công", Toast.LENGTH_SHORT).show();
                        loadOrders();
                    } catch (Exception e) {
                        Log.e("OrderManagement", "Lỗi xóa đơn hàng: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Lỗi xóa đơn hàng: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        if (db != null) {
                            try {
                                if (db.isOpen() && db.inTransaction()) {
                                    db.endTransaction(); // Đảm bảo giao dịch kết thúc ngay cả khi có lỗi
                                }
                                if (db.isOpen()) {
                                    dbHelper.closeDatabase(db);
                                }
                            } catch (Exception e) {
                                Log.e("OrderManagement", "Lỗi khi đóng database: " + e.getMessage(), e);
                            }
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (dbHelper != null) {
//            dbHelper.close();
//        }
    }
}
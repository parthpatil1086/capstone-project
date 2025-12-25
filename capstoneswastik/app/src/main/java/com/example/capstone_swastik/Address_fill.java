package com.example.capstone_swastik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Address_fill extends AppCompatActivity {

    TextView tvProductName, tvProductPrice, tvQuantity, tvTotalAmount;
    TextView tvPrevName, tvPrevPhone, tvPrevAddress, tvPrevPin;
    EditText etName, etPhone, etAddress, etPin, etInfo;
    Button btnSubmit, btnUseAddress, btnEditAddress;
    LinearLayout layoutPreviousAddress;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String productName;
    int productPrice, quantity, totalAmount, img;
    String productId; // ✅ class-level
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_address_fill);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ---------- UI BINDING ----------
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPin = findViewById(R.id.etPin);
        etInfo = findViewById(R.id.etInfo);

        btnSubmit = findViewById(R.id.btnSubmit);

        layoutPreviousAddress = findViewById(R.id.layoutPreviousAddress);
        tvPrevName = findViewById(R.id.tvPrevName);
        tvPrevPhone = findViewById(R.id.tvPrevPhone);
        tvPrevAddress = findViewById(R.id.tvPrevAddress);
        tvPrevPin = findViewById(R.id.tvPrevPin);

        btnUseAddress = findViewById(R.id.btnUseAddress);
        btnEditAddress = findViewById(R.id.btnEditAddress);

        layoutPreviousAddress.setVisibility(View.GONE);

        // ---------- RECEIVE PRODUCT DATA ----------
        Intent i = getIntent();
        productName = i.getStringExtra("name");
        productPrice = i.getIntExtra("price", 0);
        quantity = i.getIntExtra("quantity", 1);
        totalAmount = i.getIntExtra("totalValue", 0);
        productId = i.getStringExtra("productId"); // ✅ Receive productId
        img = i.getIntExtra("img", R.drawable.shree_swastik_default);

        tvProductName.setText("Product: " + productName);
        tvProductPrice.setText("Price: ₹" + productPrice);
        tvQuantity.setText("Quantity: " + quantity);
        tvTotalAmount.setText("Total: ₹" + totalAmount);

        // ---------- LOAD SAVED ADDRESS ----------
        loadSavedAddress();

        btnSubmit.setOnClickListener(v -> placeOrderOrSaveAddress());
    }

    // Load saved address
    private void loadSavedAddress() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.contains("address")) {

                        layoutPreviousAddress.setVisibility(View.VISIBLE);

                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        String address = doc.getString("address");
                        String pin = doc.getString("pin");

                        tvPrevName.setText(name);
                        tvPrevPhone.setText(phone);
                        tvPrevAddress.setText(address);
                        tvPrevPin.setText(pin);

                        btnUseAddress.setOnClickListener(v -> {
                            etName.setText(name);
                            etPhone.setText(phone);
                            etAddress.setText(address);
                            etPin.setText(pin);
                        });

                        btnEditAddress.setOnClickListener(v -> enableEditMode(name, phone, address, pin));
                    }
                });
    }

    // Enable edit mode
    private void enableEditMode(String name, String phone, String address, String pin) {
        isEditMode = true;
        layoutPreviousAddress.setVisibility(View.GONE);

        etName.setText(name);
        etPhone.setText(phone);
        etAddress.setText(address);
        etPin.setText(pin);

        btnSubmit.setText("Save Address");
    }

    // Place order or save edited address
    private void placeOrderOrSaveAddress() {

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String info = etInfo.getText().toString().trim();

        if (name.isEmpty() || !phone.matches("\\d{10}") || address.isEmpty() || !pin.matches("\\d{6}")) {
            Toast.makeText(this, "Enter valid address details", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        // Save/update address
        Map<String, Object> addr = new HashMap<>();
        addr.put("name", name);
        addr.put("phone", phone);
        addr.put("address", address);
        addr.put("pin", pin);

        db.collection("users").document(uid)
                .set(addr, SetOptions.merge())
                .addOnSuccessListener(unused -> {

                    // If editing address, reload previous address UI
                    if (isEditMode) {
                        isEditMode = false;
                        btnSubmit.setText("Place Order");
                        loadSavedAddress();
                        Toast.makeText(this, "Address Updated", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ---------- CREATE ORDER ----------
                    String orderId = UUID.randomUUID().toString();
                    Map<String, Object> order = new HashMap<>();
                    order.put("orderId", orderId);
                    order.put("userID", uid);
                    order.put("productName", productName);
                    order.put("productPrice", productPrice);
                    order.put("quantity", quantity);
                    order.put("totalAmount", totalAmount);
                    order.put("image", img);
                    order.put("productId", productId); // ✅ Include productId
                    order.put("status", "Pending");
                    order.put("paymentStatus", "COD");
                    order.put("placedAt", System.currentTimeMillis());

                    order.put("name", name);
                    order.put("phone", phone);
                    order.put("address", address);
                    order.put("pin", pin);
                    order.put("info", info);

                    db.collection("orders").add(order)
                            .addOnSuccessListener(doc -> {
                                Intent intent = new Intent(this, OrderSuccessActivity.class);
                                intent.putExtra("orderId", orderId);
                                intent.putExtra("productName", productName);
                                intent.putExtra("totalAmount", totalAmount);
                                startActivity(intent);
                                finish();
                            });
                });
    }
}

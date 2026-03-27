package com.example.capstone_swastik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Address_fill extends AppCompatActivity {

    TextView tvProductName, tvProductPrice, tvQuantity, tvTotalAmount;
    TextView tvPrevName, tvPrevPhone, tvPrevAddress, tvPrevPin;
    EditText etName, etPhone, etAddress, etPin;
    Spinner spinnerPayment;
    Button btnSubmit, btnUseAddress, btnEditAddress;
    LinearLayout layoutOnlinePayment;
    TextView tvOnlineAmount;
    LinearLayout layoutPreviousAddress;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String productName, productId;
    int productPrice, quantity, totalAmount, img;

    String selectedPayment = "COD";
    boolean isEditMode = false;
    String tempAddressDocId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_fill);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ===== Bind Views =====
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPin = findViewById(R.id.etPin);

        spinnerPayment = findViewById(R.id.spinnerPayment);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUseAddress = findViewById(R.id.btnUseAddress);
        btnEditAddress = findViewById(R.id.btnEditAddress);

        layoutPreviousAddress = findViewById(R.id.layoutPreviousAddress);
        tvPrevName = findViewById(R.id.tvPrevName);
        tvPrevPhone = findViewById(R.id.tvPrevPhone);
        tvPrevAddress = findViewById(R.id.tvPrevAddress);
        tvPrevPin = findViewById(R.id.tvPrevPin);

        layoutPreviousAddress.setVisibility(View.GONE);

        // ===== Get Intent Data =====
        Intent i = getIntent();
        productName = i.getStringExtra("name");
        productPrice = i.getIntExtra("price", 0);
        quantity = i.getIntExtra("quantity", 1);
        totalAmount = i.getIntExtra("totalValue", 0);
        productId = i.getStringExtra("productId");
        img = i.getIntExtra("img", 0);

        tvProductName.setText("Product: " + productName);
        tvProductPrice.setText("Price: ₹" + productPrice);
        tvQuantity.setText("Quantity: " + quantity);
        tvTotalAmount.setText("Total: ₹" + totalAmount);

        // ===== Spinner Setup =====
        String[] options = {"Cash on Delivery", "Online Payment"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                options
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(getResources().getColor(android.R.color.black));
                tv.setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(getResources().getColor(android.R.color.black));
                return tv;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPayment.setAdapter(adapter);


        layoutOnlinePayment = findViewById(R.id.layoutOnlinePayment);
        tvOnlineAmount = findViewById(R.id.tvOnlineAmount);
        layoutOnlinePayment.setVisibility(View.GONE);
        tvOnlineAmount.setText("Amount to Pay: ₹" + totalAmount);


        spinnerPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPayment = position == 0 ? "COD" : "ONLINE";

                if (selectedPayment.equals("ONLINE")) {
                    layoutOnlinePayment.setVisibility(View.VISIBLE);
                } else {
                    layoutOnlinePayment.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadTempAddress();

        btnSubmit.setOnClickListener(v -> validateAndProceed());
    }

    private void validateAndProceed() {

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String pin = etPin.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Name required"); return; }
        if (TextUtils.isEmpty(phone) || !phone.matches("\\d{10}")) {
            etPhone.setError("Enter valid 10 digit phone"); return;
        }
        if (TextUtils.isEmpty(address)) { etAddress.setError("Address required"); return; }
        if (TextUtils.isEmpty(pin) || !pin.matches("\\d{6}")) {
            etPin.setError("Enter valid 6 digit PIN"); return;
        }

        saveTempAddress(name, phone, address, pin);

        if (isEditMode) {
            Toast.makeText(this, "Address Updated Successfully", Toast.LENGTH_SHORT).show();
            isEditMode = false;
            btnSubmit.setText("Submit");
            spinnerPayment.setEnabled(true);
            loadTempAddress();
            return;
        }

        if (selectedPayment.equals("COD")) {

            createOrder("COD", "NA", "Pending");

        } else {

            // Navigate to Razorpay PaymentActivity
            Intent intent = new Intent(Address_fill.this, PaymentActivity.class);
            intent.putExtra("amount", totalAmount);
            startActivityForResult(intent, 101);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {

            String paymentId = data.getStringExtra("paymentId");

            if (paymentId != null) {
                createOrder("ONLINE", paymentId, "Paid");
            }
        }
    }

    private void saveTempAddress(String name, String phone, String address, String pin) {

        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> temp = new HashMap<>();
        temp.put("userId", uid);
        temp.put("name", name);
        temp.put("phone", phone);
        temp.put("address", address);
        temp.put("pin", pin);
        temp.put("updatedAt", System.currentTimeMillis());

        if (tempAddressDocId != null) {
            db.collection("temp_address").document(tempAddressDocId).set(temp);
        } else {
            db.collection("temp_address").add(temp);
        }
    }

    private void loadTempAddress() {

        String uid = auth.getCurrentUser().getUid();

        db.collection("temp_address")
                .whereEqualTo("userId", uid)
                .limit(1)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!qs.isEmpty()) {

                        var doc = qs.getDocuments().get(0);
                        tempAddressDocId = doc.getId();

                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        String address = doc.getString("address");
                        String pin = doc.getString("pin");

                        layoutPreviousAddress.setVisibility(View.VISIBLE);

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

                        btnEditAddress.setOnClickListener(v -> {
                            isEditMode = true;
                            layoutPreviousAddress.setVisibility(View.GONE);
                            etName.setText(name);
                            etPhone.setText(phone);
                            etAddress.setText(address);
                            etPin.setText(pin);
                            btnSubmit.setText("Save Address");
                            spinnerPayment.setEnabled(false);
                        });
                    }
                });
    }

    private void createOrder(String paymentMode, String txnId, String paymentStatus) {

        String orderId = UUID.randomUUID().toString();

        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userID", auth.getCurrentUser().getUid());
        order.put("productId", productId);
        order.put("productName", productName);
        order.put("productPrice", productPrice);
        order.put("quantity", quantity);
        order.put("totalAmount", totalAmount);
        order.put("paymentMode", paymentMode);
        order.put("paymentStatus", paymentStatus);
        order.put("transactionId", txnId);
        order.put("name", etName.getText().toString().trim());
        order.put("phone", etPhone.getText().toString().trim());
        order.put("address", etAddress.getText().toString().trim());
        order.put("pin", etPin.getText().toString().trim());
        order.put("placedAt", FieldValue.serverTimestamp());
        order.put("image", img);


        db.collection("orders").add(order)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Order Placed Successfully", Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}
package com.example.capstone_swastik;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Address_fill extends AppCompatActivity {

    TextView tvProductName, tvProductPrice, tvQuantity, tvTotalAmount;
    TextView tvPrevName, tvPrevPhone, tvPrevAddress, tvPrevPin;

    EditText etName, etPhone, etAddress, etPin, etUtr;

    Button btnSubmit, btnUseAddress, btnEditAddress, btnShowQr;

    LinearLayout layoutPreviousAddress, layoutOnlinePayment;

    Spinner spinnerPayment;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String productName, productId;
    int productPrice, quantity, totalAmount, img;

    boolean isEditMode = false;
    String tempAddressDocId = null;

    String selectedPayment = "COD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_address_fill);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ====== PRODUCT INFO ======
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        // ====== ADDRESS INPUT ======
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPin = findViewById(R.id.etPin);
        etUtr = findViewById(R.id.etUtr);

        // ====== BUTTONS ======
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUseAddress = findViewById(R.id.btnUseAddress);
        btnEditAddress = findViewById(R.id.btnEditAddress);
        btnShowQr = findViewById(R.id.btnShowQr); // NEW QR BUTTON

        // ====== LAYOUTS ======
        layoutPreviousAddress = findViewById(R.id.layoutPreviousAddress);
        layoutOnlinePayment = findViewById(R.id.layoutOnlinePayment);

        // ====== SPINNER ======
        spinnerPayment = findViewById(R.id.spinnerPayment);

        // ====== PREVIOUS ADDRESS TEXT ======
        tvPrevName = findViewById(R.id.tvPrevName);
        tvPrevPhone = findViewById(R.id.tvPrevPhone);
        tvPrevAddress = findViewById(R.id.tvPrevAddress);
        tvPrevPin = findViewById(R.id.tvPrevPin);

        layoutPreviousAddress.setVisibility(View.GONE);
        layoutOnlinePayment.setVisibility(View.GONE);

        // ====== GET INTENT DATA ======
        Intent i = getIntent();
        productName = i.getStringExtra("name");
        productPrice = i.getIntExtra("price", 0);
        quantity = i.getIntExtra("quantity", 1);
        totalAmount = i.getIntExtra("totalValue", 0);
        productId = i.getStringExtra("productId");
        img = i.getIntExtra("img", R.drawable.shree_swastik_default);

        tvProductName.setText("Product: " + productName);
        tvProductPrice.setText("Price: ₹" + productPrice);
        tvQuantity.setText("Quantity: " + quantity);
        tvTotalAmount.setText("Total: ₹" + totalAmount);

        // ====== SPINNER SETUP ======
        String[] paymentOptions = new String[]{getString(R.string.cash_on_delivery), getString(R.string.online_payment)};

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                paymentOptions
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                tv.setBackgroundColor(Color.WHITE);
                tv.setPadding(16, 16, 16, 16);
                return view;
            }
        };
        spinnerPayment.setAdapter(paymentAdapter);

        spinnerPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    selectedPayment = "ONLINE";
                    layoutOnlinePayment.setVisibility(View.VISIBLE);
                } else {
                    selectedPayment = "COD";
                    layoutOnlinePayment.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // ====== LOAD TEMP ADDRESS ======
        loadTempAddress();

        // ====== QR BUTTON CLICK ======
        btnShowQr.setOnClickListener(v -> showQrDialog());

        // ====== SUBMIT CLICK ======
        btnSubmit.setOnClickListener(v -> saveAddressOrPlaceOrder());
    }

    // ================= LOAD TEMP ADDRESS =================
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

                        btnEditAddress.setOnClickListener(v ->
                                enableEditMode(name, phone, address, pin));
                    }
                });
    }

    private void enableEditMode(String name, String phone, String address, String pin) {
        isEditMode = true;
        layoutPreviousAddress.setVisibility(View.GONE);

        etName.setText(name);
        etPhone.setText(phone);
        etAddress.setText(address);
        etPin.setText(pin);

        btnSubmit.setText(R.string.save_address);
    }

    private void saveAddressOrPlaceOrder() {

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String utr = etUtr.getText().toString().trim();

        // ====== VALIDATION ======
        if (name.isEmpty()) {
            etName.setError(getString(R.string.name_cannot_be_empty));
            etName.requestFocus();
            return;
        }

        if (phone.isEmpty() || !phone.matches("\\d{10}")) {
            etPhone.setError(getString(R.string.enter_valid_10_digit_phone_number));
            etPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError(getString(R.string.address_cannot_be_empty));
            etAddress.requestFocus();
            return;
        }

        if (pin.isEmpty() || !pin.matches("\\d{6}")) {
            etPin.setError(getString(R.string.enter_valid_6_digit_pin_code));
            etPin.requestFocus();
            return;
        }

        if (selectedPayment.equals("ONLINE") && utr.isEmpty()) {
            etUtr.setError(getString(R.string.enter_transaction_id_utr_for_online_payment));
            etUtr.requestFocus();
            return;
        }

        // ====== SAVE TEMP ADDRESS ======
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> tempAddr = new HashMap<>();
        tempAddr.put("userId", uid);
        tempAddr.put("name", name);
        tempAddr.put("phone", phone);
        tempAddr.put("address", address);
        tempAddr.put("pin", pin);
        tempAddr.put("updatedAt", System.currentTimeMillis());

        if (tempAddressDocId != null) {
            db.collection("temp_address").document(tempAddressDocId)
                    .set(tempAddr)
                    .addOnSuccessListener(unused ->
                            afterAddressSaved(name, phone, address, pin));
        } else {
            db.collection("temp_address")
                    .add(tempAddr)
                    .addOnSuccessListener(doc -> {
                        tempAddressDocId = doc.getId();
                        afterAddressSaved(name, phone, address, pin);
                    });
        }
    }


    // ================= CREATE ORDER =================
    private void afterAddressSaved(String name, String phone,
                                   String address, String pin) {

        if (isEditMode) {
            isEditMode = false;
            btnSubmit.setText(R.string.place_order);
            loadTempAddress();
            Toast.makeText(this, R.string.address_updated, Toast.LENGTH_SHORT).show();
            return;
        }

        String orderId = UUID.randomUUID().toString();
        String utr = selectedPayment.equals("ONLINE")
                ? etUtr.getText().toString().trim()
                : "NA";

        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userID", auth.getCurrentUser().getUid());
        order.put("productId", productId);
        order.put("productName", productName);
        order.put("productPrice", productPrice);
        order.put("quantity", quantity);
        order.put("totalAmount", totalAmount);
        order.put("image", img);
        order.put("status", "Pending");

        order.put("paymentMode", selectedPayment);
        order.put("paymentStatus", selectedPayment.equals("ONLINE") ? "Paid" : "COD");
        order.put("utr", utr);

        order.put("name", name);
        order.put("phone", phone);
        order.put("address", address);
        order.put("pin", pin);
        order.put("placedAt", System.currentTimeMillis());

        db.collection("orders").add(order)
                .addOnSuccessListener(doc -> {
                    Intent intent = new Intent(this, OrderSuccessActivity.class);
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("productName", productName);
                    intent.putExtra("totalAmount", totalAmount);
                    startActivity(intent);
                    finish();
                });
    }

    // ================= SHOW QR DIALOG =================
    private void showQrDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_qr);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        ImageView qrImage = dialog.findViewById(R.id.imgQrDialog);
        qrImage.setImageResource(R.drawable.qr_code);

        Button btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}

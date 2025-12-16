package com.example.capstone_swastik;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Address_fill extends AppCompatActivity {

    TextView tvProductName, tvProductPrice, tvQuantity, tvTotalAmount;
    EditText etName, etPhone, etAddress, etPin, etInfo;
    Button btnSubmit;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_address_fill);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        String productName = getIntent().getStringExtra("name");
        int productPrice = getIntent().getIntExtra("price", 0);
        int quantity = getIntent().getIntExtra("quantity", 0);
        int totalAmount = getIntent().getIntExtra("totalValue", 0);
        int img = getIntent().getIntExtra("img", R.drawable.shree_swastik_default);

        tvProductName.setText(getString(R.string.product_name)+ productName);
        tvProductPrice.setText(getString(R.string.product_price)+ productPrice);
        tvQuantity.setText(getString(R.string.quantity)+ quantity);
        tvTotalAmount.setText(getString(R.string.total_amount) + totalAmount);

        btnSubmit.setOnClickListener(v -> {

            String inputName = etName.getText().toString().trim();
            String inputPhone = etPhone.getText().toString().trim();
            String inputAddress = etAddress.getText().toString().trim();
            String inputPin = etPin.getText().toString().trim();
            String inputInfo = etInfo.getText().toString().trim();

            // Validation checks
            if(inputName.isEmpty()) {
                etName.setError(getString(R.string.please_enter_your_name));
                etName.requestFocus();
                return;
            }

            if(inputPhone.isEmpty()) {
                etPhone.setError(getString(R.string.please_enter_your_phone_number));
                etPhone.requestFocus();
                return;
            }

            if(!inputPhone.matches("\\d{10}")) { // exactly 10 digits
                etPhone.setError(getString(R.string.phone_number_must_be_10_digits));
                etPhone.requestFocus();
                return;
            }

            if(inputAddress.isEmpty()) {
                etAddress.setError(getString(R.string.please_enter_your_address));
                etAddress.requestFocus();
                return;
            }

            if(inputPin.isEmpty()) {
                etPin.setError(getString(R.string.please_enter_pin_code));
                etPin.requestFocus();
                return;
            }

            if(!inputPin.matches("\\d{6}")) { // exactly 6 digits
                etPin.setError(getString(R.string.pin_code_must_be_6_digits));
                etPin.requestFocus();
                return;
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("userID", uid);
            orderData.put("productName", productName);
            orderData.put("productPrice", productPrice);
            orderData.put("quantity", quantity);
            orderData.put("totalAmount", totalAmount);
            orderData.put("image", img);

            orderData.put("name", inputName);
            orderData.put("phone", inputPhone);
            orderData.put("address", inputAddress);
            orderData.put("pin", inputPin);
            orderData.put("info", inputInfo);
            orderData.put("timestamp", System.currentTimeMillis());

            db.collection("orders")
                    .add(orderData)
                    .addOnSuccessListener(doc -> {
                        // Pass info to success screen
                        Intent intent = new Intent(Address_fill.this, OrderSuccessActivity.class);
                        intent.putExtra("userName", inputName);
                        intent.putExtra("productName", productName);
                        intent.putExtra("totalAmount", totalAmount);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}

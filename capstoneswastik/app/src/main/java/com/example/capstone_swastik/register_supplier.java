package com.example.capstone_swastik;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import android.widget.ProgressBar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class register_supplier extends AppCompatActivity {

    TextInputEditText etName, etPhone, etEmail, etSupplierID;
    MaterialButton btnRegister;
    ProgressBar progressBar;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String userID;
    String generatedSupplierID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_supplier);

        etName = findViewById(R.id.name);
        etPhone = findViewById(R.id.phnumber);
        etEmail = findViewById(R.id.email);
        etSupplierID = findViewById(R.id.supplier_id);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userID = auth.getCurrentUser().getUid();
        btnRegister.setEnabled(false);
        progressBar.setVisibility(ProgressBar.VISIBLE);

        // 1️⃣ Prefill user info
        db.collection("users").document(userID)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        etName.setText(document.getString("name"));
                        etPhone.setText(document.getString("number"));
                        etEmail.setText(document.getString("email"));
                    }
                });

        // 2️⃣ Check if user already registered
        db.collection("supplier").document(userID)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Already registered
                        generatedSupplierID = doc.getString("supplierID");
                        etSupplierID.setText(generatedSupplierID);
                        btnRegister.setText("Go to Main Page");
                        btnRegister.setEnabled(true);
                        btnRegister.setOnClickListener(v -> {
                            startActivity(new Intent(register_supplier.this, MainActivity.class));
                            finish();
                        });
                    } else {
                        // Generate sequential Supplier ID
                        generateSequentialSupplierID();
                    }
                    progressBar.setVisibility(ProgressBar.GONE);
                });

        // 3️⃣ Button click for registration
        btnRegister.setOnClickListener(view -> registerSupplier());
    }

    private void generateSequentialSupplierID() {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        // Query suppliers ordered by supplierID descending, take first
        db.collection("supplier")
                .orderBy("supplierID", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long nextID = 111; // default start

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String lastIDStr = doc.getString("supplierID");
                        if (lastIDStr != null && !lastIDStr.isEmpty()) {
                            try {
                                nextID = Long.parseLong(lastIDStr) + 1;
                            } catch (NumberFormatException e) {
                                nextID = 111; // fallback
                            }
                        }
                    }

                    if (nextID > 999) {
                        Toast.makeText(this, "Supplier limit reached!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(ProgressBar.GONE);
                        return;
                    }

                    generatedSupplierID = String.valueOf(nextID);
                    etSupplierID.setText(generatedSupplierID);
                    btnRegister.setEnabled(true);
                    progressBar.setVisibility(ProgressBar.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to generate Supplier ID", Toast.LENGTH_SHORT).show();
                    btnRegister.setEnabled(true);
                    progressBar.setVisibility(ProgressBar.GONE);
                });
    }

    private void registerSupplier() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // --- Validation ---
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }
        if (!name.matches("[a-zA-Z\\s]+")) {
            etName.setError("Only letters allowed");
            return;
        }
        if (!phone.matches("\\d{10}")) {
            etPhone.setError("Enter valid 10-digit number");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter valid email");
            return;
        }
        if (generatedSupplierID == null || generatedSupplierID.isEmpty()) {
            Toast.makeText(this, "Supplier ID not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);

        Map<String, Object> supplierData = new HashMap<>();
        supplierData.put("name", name);
        supplierData.put("phone", phone);
        supplierData.put("email", email);
        supplierData.put("userID", userID);
        supplierData.put("supplierID", generatedSupplierID);

        // Save supplier info
        db.collection("supplier").document(userID)
                .set(supplierData, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Registered! Your Supplier ID: " + generatedSupplierID, Toast.LENGTH_SHORT).show();

                    btnRegister.setText("Go to Main Page");
                    btnRegister.setOnClickListener(v -> {
                        startActivity(new Intent(register_supplier.this, MainActivity.class));
                        finish();
                    });

                    progressBar.setVisibility(ProgressBar.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(ProgressBar.GONE);
                });
    }
}

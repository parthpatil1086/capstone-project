package com.example.capstone_swastik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BecomeSupplier extends AppCompatActivity {

    Button registerButton;
    TextView becomeTag, supplierIdTag, btnViewProcurement;
    ProgressBar progressBar;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_become_supplier);

        registerButton = findViewById(R.id.registerButton);
        becomeTag = findViewById(R.id.becomeTag);
        supplierIdTag = findViewById(R.id.supplierIdTag);
        btnViewProcurement = findViewById(R.id.btnViewProcurement);
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Hide everything initially
        registerButton.setVisibility(View.GONE);
        supplierIdTag.setVisibility(View.GONE);
        btnViewProcurement.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String userID = auth.getCurrentUser().getUid();

        // Check if user is already a supplier
        db.collection("supplier").document(userID)
                .get()
                .addOnSuccessListener(document -> {

                    progressBar.setVisibility(View.GONE);

                    if (document.exists()) {

                        String supplierID = document.getString("supplierID");
                        if (supplierID == null) supplierID = document.getString("supplierId");
                        if (supplierID == null) supplierID = document.getString("supplierid");

                        if (supplierID != null && !supplierID.isEmpty()) {
                            supplierIdTag.setText("ID: " + supplierID);
                            supplierIdTag.setVisibility(View.VISIBLE);
                        }

                        // Go to Supplier Page
                        becomeTag.setText("Proceed to Supplier Page");
                        becomeTag.setOnClickListener(v ->
                                startActivity(new Intent(getApplicationContext(), SupplierPage.class))
                        );

                        btnViewProcurement.setVisibility(View.VISIBLE);
                        btnViewProcurement.setOnClickListener(v ->
                                startActivity(new Intent(getApplicationContext(), ProcurementListActivity.class))
                        );

                    } else {
                        // ----- NOT A SUPPLIER -----
                        becomeTag.setText("Become a Supplier");

                        registerButton.setVisibility(View.VISIBLE);
                        supplierIdTag.setVisibility(View.GONE);

                        registerButton.setOnClickListener(view -> {
                            startActivity(new Intent(getApplicationContext(), register_supplier.class));
                            finish();
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    becomeTag.setText("Error checking supplier status");
                    registerButton.setVisibility(View.GONE);
                });
    }
}

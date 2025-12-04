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
    TextView becomeTag, supplierIdTag;
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
        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerButton.setVisibility(View.GONE);
        supplierIdTag.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String userID = auth.getCurrentUser().getUid();

        // CHECK IF USER IS SUPPLIER
        db.collection("supplier").document(userID)
                .get()
                .addOnSuccessListener(document -> {
                    progressBar.setVisibility(View.GONE);

                    if (document.exists()) {
                        // Fallbacks for field name
                        String supplierID = document.getString("supplierID");
                        if (supplierID == null) supplierID = document.getString("supplierId");
                        if (supplierID == null) supplierID = document.getString("supplierid");

                        if (supplierID != null && !supplierID.isEmpty()) {
                            supplierIdTag.setText("ID: " + supplierID);
                            supplierIdTag.setVisibility(View.VISIBLE);
                        }

                        becomeTag.setText("Proceed to Supplier Page");

                        becomeTag.setOnClickListener(v -> {
                            Intent intent = new Intent(getApplicationContext(), SupplierPage.class);
                            startActivity(intent);
                        });

                    } else {
                        // If NOT supplier
                        becomeTag.setText("Become a Supplier");
                        registerButton.setVisibility(View.VISIBLE);
                        supplierIdTag.setVisibility(View.GONE);

                        registerButton.setOnClickListener(view -> {
                            Intent intent = new Intent(getApplicationContext(), register_supplier.class);
                            startActivity(intent);
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

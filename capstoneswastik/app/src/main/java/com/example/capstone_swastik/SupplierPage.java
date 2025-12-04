package com.example.capstone_swastik;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SupplierPage extends AppCompatActivity {

    private EditText etProductName, etQuantity, etLocation;
    private Button btnSubmit,statusbtn;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private EditText etGrowthMonths;

    // Store supplier info
    private String supplierName = "Unknown Supplier";
    private String supplierId = "unknown";
    private String supplierPhone = "unknown";  // Added phone field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_page);

        // Initialize views
        etProductName = findViewById(R.id.etProductName);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        btnSubmit = findViewById(R.id.btnSubmitRequest);
        statusbtn =findViewById(R.id.statusbtn);
        etGrowthMonths = findViewById(R.id.etGrowthMonths);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Set fixed product name
        etProductName.setText("Sugarcane");

        // Get current logged-in user's UID
        if (auth.getCurrentUser() != null) {
            String currentUid = auth.getCurrentUser().getUid();

            // Fetch supplier info from Firestore using userID
            db.collection("supplier")
                    .whereEqualTo("userID", currentUid)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            supplierName = documentSnapshot.getString("name");
                            supplierId = documentSnapshot.getString("supplierID");
                            supplierPhone = documentSnapshot.getString("phone"); // fetch phone
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch supplier info", Toast.LENGTH_SHORT).show();
                    });
        }

        // Submit button click
        btnSubmit.setOnClickListener(v -> sendRequest());

        statusbtn.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ViewRequestsActivity.class);
            startActivity(intent);
        });
    }

    private void sendRequest() {
        String productName = etProductName.getText().toString().trim();
        String area = etQuantity.getText().toString().trim(); // changed from quantity to area
        String location = etLocation.getText().toString().trim();
        String growthMonths = etGrowthMonths.getText().toString().trim();

        if (area.isEmpty() || location.isEmpty() || growthMonths.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }


        // Prepare request data
        Map<String, Object> request = new HashMap<>();
        request.put("productName", productName);
        request.put("areaInAcres", area);
        request.put("location", location);
        request.put("supplierId", supplierId);
        request.put("supplierName", supplierName);
        request.put("supplierPhone", supplierPhone);
        request.put("status", "Pending");
        request.put("visitDate", "");
        request.put("growthMonths", growthMonths);
        request.put("timestamp", System.currentTimeMillis());

        // Store in Firestore
        db.collection("supplier_requests")
                .add(request)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Request sent successfully get notify soon ", Toast.LENGTH_SHORT).show();
                    etQuantity.setText("");
                    etLocation.setText("");
                    etGrowthMonths.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

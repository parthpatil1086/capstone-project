package com.example.capstone_swastik;

import android.os.Bundle;
import android.view.View;
import android.widget.Button; // Don't forget this import
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference; // Don't forget this import

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OrderDetailsActivity extends AppCompatActivity {

    TextView tvPName, tvPPrice, tvQty, tvTotal, tvAddress, tvDate, tvStatus;
    ImageView imgProduct;
    ProgressBar progressBar;
    Button btnCancelOrder; // *** ADDED: Declare the button ***
    String currentOrderID; // *** ADDED: To store the order ID globally for cancel method ***

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        tvPName = findViewById(R.id.tvPName);
        tvPPrice = findViewById(R.id.tvPPrice);
        tvQty = findViewById(R.id.tvQty);
        tvTotal = findViewById(R.id.tvTotal);
        tvAddress = findViewById(R.id.tvAddress);
        tvDate = findViewById(R.id.tvDate);
        tvStatus = findViewById(R.id.tvStatus);
        imgProduct = findViewById(R.id.imgProduct);
        progressBar = findViewById(R.id.progressBar);
        btnCancelOrder = findViewById(R.id.btnCancelOrder); // *** ADDED: Initialize the button ***

        String orderID = getIntent().getStringExtra("orderID");
        if (orderID != null) {
            currentOrderID = orderID; // Store the ID
            loadDetails(orderID);
        } else {
            Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if ID is missing
        }

        // *** ADDED: Set click listener for the cancel button ***
        btnCancelOrder.setOnClickListener(v -> cancelOrder());
    }

    private void loadDetails(String id) {
        progressBar.setVisibility(View.VISIBLE);
        btnCancelOrder.setVisibility(View.GONE); // Hide button while loading

        FirebaseFirestore.getInstance()
                .collection("orders")
                .document(id)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);

                    if (doc.exists()) {
                        // ... (Existing code for populating TextViews) ...

                        // Product name
                        tvPName.setText(doc.getString("productName"));

                        // Price
                        Number price = doc.get("productPrice") instanceof Number ? (Number) doc.get("productPrice") : 0;
                        tvPPrice.setText("Price: ₹ " + price.intValue());

                        // Quantity
                        Number qty = doc.get("quantity") instanceof Number ? (Number) doc.get("quantity") : 1;
                        tvQty.setText("Quantity: " + qty.intValue());

                        // Total
                        Number total = doc.get("totalAmount") instanceof Number ? (Number) doc.get("totalAmount") : 0;
                        tvTotal.setText("Total: ₹ " + total.intValue());

                        // Address
                        tvAddress.setText(
                                "Delivery Details:\n" +
                                        doc.getString("name") + "\n" +
                                        doc.getString("phone") + "\n" +
                                        doc.getString("address") + "\nPIN: " + doc.getString("pin")
                        );

                        // Date
                        Long timestamp = doc.getLong("timestamp");
                        if (timestamp != null) {
                            String formattedDate = new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(new Date(timestamp));
                            tvDate.setText("Order Date: " + formattedDate);
                        }


                        // Status
                        String status = doc.getString("status") != null ? doc.getString("status") : "Pending";
                        tvStatus.setText("Status: " + status);

                        // *** ADDED LOGIC: Show button only if status is Pending ***
                        if ("Pending".equalsIgnoreCase(status)) {
                            btnCancelOrder.setVisibility(View.VISIBLE);
                        } else {
                            btnCancelOrder.setVisibility(View.GONE);
                        }
                        // *********************************************************

                        // Load product image safely
                        Object imgObj = doc.get("image");
                        int imageRes = R.drawable.shree_swastik_default; // fallback

                        if (imgObj instanceof Number) {
                            imageRes = ((Number) imgObj).intValue(); // old stored int
                        } else if (imgObj instanceof String) {
                            String imageName = (String) imgObj;
                            int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                            if (resId != 0) imageRes = resId;
                        }

                        imgProduct.setImageResource(imageRes);

                    } else {
                        Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load order details", Toast.LENGTH_SHORT).show();
                });
    }

    // *** ADDED: Method to cancel the order ***
    private void cancelOrder() {
        if (currentOrderID == null) {
            Toast.makeText(this, "Cannot cancel. Order ID is missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCancelOrder.setEnabled(false); // Prevent multiple clicks

        DocumentReference orderRef = FirebaseFirestore.getInstance().collection("orders").document(currentOrderID);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Cancelled"); // Set the new status
        updates.put("cancellationTime", System.currentTimeMillis()); // Optional: Log cancellation time

        orderRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrderDetailsActivity.this, "Order has been CANCELLED successfully.", Toast.LENGTH_LONG).show();

                    // Update UI immediately
                    tvStatus.setText("Status: Cancelled");
                    btnCancelOrder.setVisibility(View.GONE); // Hide the button

                    // You might want to call finish() or navigate back here
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnCancelOrder.setEnabled(true); // Re-enable button on failure
                    Toast.makeText(OrderDetailsActivity.this, "Failed to cancel order: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    // *****************************************
}
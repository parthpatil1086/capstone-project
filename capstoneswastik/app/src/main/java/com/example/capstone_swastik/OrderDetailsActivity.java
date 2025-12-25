package com.example.capstone_swastik;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsActivity extends AppCompatActivity {

    ImageView imgProduct;
    TextView tvPName, tvPPrice, tvQty, tvTotal, tvAddress, tvDate;
    Button btnCancelOrder;
    ProgressBar progressBar;

    FirebaseFirestore db;
    String currentOrderID;

    Handler handler = new Handler();
    int animDelay = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        imgProduct = findViewById(R.id.imgProduct);
        tvPName = findViewById(R.id.tvPName);
        tvPPrice = findViewById(R.id.tvPPrice);
        tvQty = findViewById(R.id.tvQty);
        tvTotal = findViewById(R.id.tvTotal);
        tvAddress = findViewById(R.id.tvAddress);
        tvDate = findViewById(R.id.tvDate);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        currentOrderID = getIntent().getStringExtra("orderID");
        if (currentOrderID == null) {
            Toast.makeText(this, "Invalid Order", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrderDetails(currentOrderID);
        btnCancelOrder.setOnClickListener(v -> cancelOrder());
    }

    private void loadOrderDetails(String orderId) {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("orders")
                .document(orderId)
                .addSnapshotListener(this, (doc, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (doc == null || !doc.exists()) return;

                    tvPName.setText(doc.getString("productName"));

                    long price = doc.getLong("productPrice") != null ? doc.getLong("productPrice") : 0;
                    long qty = doc.getLong("quantity") != null ? doc.getLong("quantity") : 1;
                    long total = doc.getLong("totalAmount") != null ? doc.getLong("totalAmount") : 0;

                    tvPPrice.setText("Price: ₹" + price);
                    tvQty.setText("Qty: " + qty);
                    tvTotal.setText("Total: ₹" + total);

                    tvAddress.setText(
                            doc.getString("name") + "\n" +
                                    doc.getString("phone") + "\n" +
                                    doc.getString("address") + "\nPIN: " +
                                    doc.getString("pin")
                    );

                    Long time = doc.getLong("placedAt");
                    if (time != null) {
                        tvDate.setText("Order Date: " +
                                new SimpleDateFormat("dd MMM yyyy, hh:mm a",
                                        Locale.getDefault()).format(new Date(time)));
                    }

                    // ✅ SAFE IMAGE LOADING
                    Object imgObj = doc.get("image");
                    if (imgObj instanceof String) {
                        Glide.with(this)
                                .load((String) imgObj)
                                .placeholder(R.drawable.shree_swastik_default)
                                .error(R.drawable.shree_swastik_default)
                                .centerCrop()
                                .into(imgProduct);
                    } else if (imgObj instanceof Number) {
                        imgProduct.setImageResource(((Number) imgObj).intValue());
                    } else {
                        imgProduct.setImageResource(R.drawable.shree_swastik_default);
                    }

                    String status = doc.getString("status");

                    animDelay = 0;
                    if ("Cancelled".equalsIgnoreCase(status)) {
                        updateTimelineCancelled(doc);
                    } else {
                        updateTimelineAnimated(doc, status);
                    }

                    btnCancelOrder.setVisibility(
                            status != null &&
                                    !status.equalsIgnoreCase("Delivered") &&
                                    !status.equalsIgnoreCase("Cancelled")
                                    ? View.VISIBLE : View.GONE
                    );
                });
    }

    /* ===================== TIMELINE ===================== */

    private void updateTimelineAnimated(DocumentSnapshot doc, String status) {
        animateStepWithDelay(R.id.stepPlaced, "Order Placed", true, doc.getLong("placedAt"));
        animateStepWithDelay(R.id.stepConfirmed, "Order Confirmed",
                status.matches("Confirmed|Dispatched|In Transit|Delivered"),
                doc.getLong("confirmedAt"));
        animateStepWithDelay(R.id.stepDispatched, "Dispatched",
                status.matches("Dispatched|In Transit|Delivered"),
                doc.getLong("dispatchedAt"));
        animateStepWithDelay(R.id.stepTransit, "In Transit",
                status.matches("In Transit|Delivered"),
                doc.getLong("inTransitAt"));
        animateStepWithDelay(R.id.stepDelivered, "Delivered",
                "Delivered".equalsIgnoreCase(status),
                doc.getLong("deliveredAt"));
    }

    private void updateTimelineCancelled(DocumentSnapshot doc) {
        // Placed step ✅
        updateStep(R.id.stepPlaced, "Order Placed", true, doc.getLong("placedAt"));
        // Pending steps ⏳
        updateStep(R.id.stepConfirmed, "Order Confirmed", false, doc.getLong("confirmedAt"));
        updateStep(R.id.stepDispatched, "Dispatched", false, doc.getLong("dispatchedAt"));
        updateStep(R.id.stepTransit, "In Transit", false, doc.getLong("inTransitAt"));

        // Cancelled step ❌
        View stepDelivered = findViewById(R.id.stepDelivered);
        ((TextView) stepDelivered.findViewById(R.id.tvStatusTitle)).setText("Cancelled");
        ((ImageView) stepDelivered.findViewById(R.id.imgStatus))
                .setImageResource(R.drawable.outline_cancel_24);
        stepDelivered.findViewById(R.id.viewLine).setVisibility(View.GONE);
        ((TextView) stepDelivered.findViewById(R.id.tvTime))
                .setText(new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        .format(new Date(doc.getLong("cancelledAt") != null
                                ? doc.getLong("cancelledAt") : System.currentTimeMillis())));
    }

    private void animateStepWithDelay(int stepId, String title, boolean completed, @Nullable Long time) {
        handler.postDelayed(() -> updateStep(stepId, title, completed, time), animDelay);
        animDelay += 450;
    }

    private void updateStep(int stepId, String title, boolean completed, @Nullable Long time) {
        View step = findViewById(stepId);
        ImageView imgStatus = step.findViewById(R.id.imgStatus);
        TextView tvTitle = step.findViewById(R.id.tvStatusTitle);
        TextView tvTime = step.findViewById(R.id.tvTime);
        View line = step.findViewById(R.id.viewLine);

        tvTitle.setText(title);
        imgStatus.setImageResource(
                completed ? R.drawable.ic_done_green : R.drawable.ic_pending
        );

        int color = completed
                ? ContextCompat.getColor(this, R.color.teal_700)
                : ContextCompat.getColor(this, android.R.color.darker_gray);

        if (line != null) {
            line.setBackgroundColor(color);
            line.setVisibility("Delivered".equalsIgnoreCase(title) || "Cancelled".equalsIgnoreCase(title)
                    ? View.GONE : View.VISIBLE);
        }

        if (completed) animateIcon(imgStatus);

        if (time != null) {
            tvTime.setText(new SimpleDateFormat("dd MMM, hh:mm a",
                    Locale.getDefault()).format(new Date(time)));
        } else tvTime.setText("");
    }

    private void animateIcon(View view) {
        PropertyValuesHolder sx = PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1f);
        PropertyValuesHolder sy = PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1f);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(view, sx, sy);
        anim.setDuration(300);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();
    }

    /* ===================== CANCEL ORDER ===================== */

    private void cancelOrder() {
        progressBar.setVisibility(View.VISIBLE);

        DocumentReference ref = db.collection("orders").document(currentOrderID);
        Map<String, Object> update = new HashMap<>();
        update.put("status", "Cancelled");
        update.put("cancelledAt", System.currentTimeMillis());

        ref.update(update)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Order cancelled successfully",
                            Toast.LENGTH_LONG).show();

                    db.collection("orders").document(currentOrderID).get()
                            .addOnSuccessListener(doc -> updateTimelineCancelled(doc));
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}

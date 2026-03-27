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
import com.google.firebase.Timestamp;
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

                    long price = getSafeLong(doc, "productPrice", 0);
                    long qty = getSafeLong(doc, "quantity", 1);
                    long total = getSafeLong(doc, "totalAmount", 0);

                    tvPPrice.setText("Price: ₹" + price);
                    tvQty.setText("Qty: " + qty);
                    tvTotal.setText("Total: ₹" + total);

                    tvAddress.setText(
                            doc.getString("name") + "\n" +
                                    doc.getString("phone") + "\n" +
                                    doc.getString("address") + "\nPIN: " +
                                    doc.getString("pin")
                    );

                    Long placedTime = getTimestampMillis(doc, "placedAt");
                    if (placedTime != null) {
                        tvDate.setText("Order Date: " +
                                new SimpleDateFormat("dd MMM yyyy, hh:mm a",
                                        Locale.getDefault()).format(new Date(placedTime)));
                    }
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
                    if (status == null) status = "Pending";

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

    private void updateTimelineAnimated(DocumentSnapshot doc, String status) {
        animateStepWithDelay(R.id.stepPlaced, "Order Placed", true, getTimestampMillis(doc, "placedAt"));
        animateStepWithDelay(R.id.stepConfirmed, "Order Confirmed",
                status.matches("Confirmed|Dispatched|In Transit|Delivered"),
                getTimestampMillis(doc, "confirmedAt"));
        animateStepWithDelay(R.id.stepDispatched, "Dispatched",
                status.matches("Dispatched|In Transit|Delivered"),
                getTimestampMillis(doc, "dispatchedAt"));
        animateStepWithDelay(R.id.stepTransit, "In Transit",
                status.matches("In Transit|Delivered"),
                getTimestampMillis(doc, "inTransitAt"));
        animateStepWithDelay(R.id.stepDelivered, "Delivered",
                "Delivered".equalsIgnoreCase(status),
                getTimestampMillis(doc, "deliveredAt"));
    }

    private void updateTimelineCancelled(DocumentSnapshot doc) {
        updateStep(R.id.stepPlaced, "Order Placed", true, getTimestampMillis(doc, "placedAt"));
        updateStep(R.id.stepConfirmed, "Order Confirmed", false, getTimestampMillis(doc, "confirmedAt"));
        updateStep(R.id.stepDispatched, "Dispatched", false, getTimestampMillis(doc, "dispatchedAt"));
        updateStep(R.id.stepTransit, "In Transit", false, getTimestampMillis(doc, "inTransitAt"));

        View stepDelivered = findViewById(R.id.stepDelivered);
        ((TextView) stepDelivered.findViewById(R.id.tvStatusTitle)).setText("Cancelled");
        ((ImageView) stepDelivered.findViewById(R.id.imgStatus))
                .setImageResource(R.drawable.outline_cancel_24);
        stepDelivered.findViewById(R.id.viewLine).setVisibility(View.GONE);
        Long cancelledTime = getTimestampMillis(doc, "cancelledAt");
        ((TextView) stepDelivered.findViewById(R.id.tvTime))
                .setText(new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        .format(new Date(cancelledTime != null ? cancelledTime : System.currentTimeMillis())));
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
        imgStatus.setImageResource(completed ? R.drawable.ic_done_green : R.drawable.ic_pending);

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
            tvTime.setText(new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    .format(new Date(time)));
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

    private void cancelOrder() {
        progressBar.setVisibility(View.VISIBLE);

        DocumentReference ref = db.collection("orders").document(currentOrderID);
        Map<String, Object> update = new HashMap<>();
        update.put("status", "Cancelled"); // <-- add this line
        update.put("cancelledAt", new Timestamp(new Date()));

        ref.update(update)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Order cancelled successfully", Toast.LENGTH_LONG).show();
                    // Update timeline immediately
                    db.collection("orders").document(currentOrderID).get()
                            .addOnSuccessListener(doc -> {
                                updateTimelineCancelled(doc);
                                btnCancelOrder.setVisibility(View.GONE); // hide button immediately
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private Long getTimestampMillis(DocumentSnapshot doc, String field) {
        Object obj = doc.get(field);

        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        } else if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toDate().getTime();
        } else if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private long getSafeLong(DocumentSnapshot doc, String field, long defaultVal) {
        Object obj = doc.get(field);
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof String) {
            try { return Long.parseLong((String) obj); } catch (NumberFormatException ignored) {}
        }
        return defaultVal;
    }
}

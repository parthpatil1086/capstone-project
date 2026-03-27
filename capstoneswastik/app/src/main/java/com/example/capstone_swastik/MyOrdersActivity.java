package com.example.capstone_swastik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MyOrdersActivity extends AppCompatActivity {

    RecyclerView recyclerOrders;
    ProgressBar progressBar;
    FirebaseFirestore db;
    FirebaseAuth auth;

    ArrayList<OrderModel> list;
    OrderAdapter adapter;

    ListenerRegistration registration; // ✅ prevent memory leak

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        recyclerOrders = findViewById(R.id.recyclerOrders);
        progressBar = findViewById(R.id.progressBar);
        TextView tvNoOrders = findViewById(R.id.tvNoOrders);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        list = new ArrayList<>();

        adapter = new OrderAdapter(list, order -> {
            Intent i = new Intent(MyOrdersActivity.this, OrderDetailsActivity.class);
            i.putExtra("orderID", order.orderID);
            startActivity(i);
        });

        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);

        loadOrders(tvNoOrders);
    }

    private void loadOrders(TextView tvNoOrders) {

        if (auth.getCurrentUser() == null) {
            tvNoOrders.setVisibility(View.VISIBLE);
            tvNoOrders.setText("User not logged in.");
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);

        registration = db.collection("orders")
                .whereEqualTo("userID", uid)
                .orderBy("placedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((query, error) -> {

                    if (error != null) {
                        progressBar.setVisibility(View.GONE);
                        tvNoOrders.setVisibility(View.VISIBLE);
                        tvNoOrders.setText("Failed to load orders.");
                        return;
                    }

                    list.clear();

                    if (query == null || query.isEmpty()) {
                        recyclerOrders.setVisibility(View.GONE);
                        tvNoOrders.setVisibility(View.VISIBLE);
                        tvNoOrders.setText("No orders found.");
                    } else {

                        for (DocumentSnapshot doc : query.getDocuments()) {

                            OrderModel model = new OrderModel();
                            model.orderID = doc.getId();
                            model.productName = doc.getString("productName");

                            Number price = doc.get("productPrice") instanceof Number
                                    ? (Number) doc.get("productPrice") : 0;
                            model.productPrice = price.longValue();

                            Number total = doc.get("totalAmount") instanceof Number
                                    ? (Number) doc.get("totalAmount") : 0;
                            model.totalAmount = total.longValue();

                            Number qty = doc.get("quantity") instanceof Number
                                    ? (Number) doc.get("quantity") : 1;
                            model.quantity = qty.longValue();

                            Object imgObj = doc.get("image");
                            int imageRes = R.drawable.shree_swastik_default;

                            if (imgObj instanceof Number) {
                                imageRes = ((Number) imgObj).intValue();
                            }

                            model.img = imageRes;

                            Long placedTime = null;

                            Object tsObj = doc.get("placedAt");
                            if (tsObj instanceof Number) {
                                placedTime = ((Number) tsObj).longValue();
                            } else if (tsObj instanceof Timestamp) {
                                placedTime = ((Timestamp) tsObj).toDate().getTime();
                            }

                            if (placedTime != null) {
                                model.date = new SimpleDateFormat(
                                        "dd MMM yyyy, hh:mm a",
                                        Locale.getDefault()
                                ).format(new Date(placedTime));
                            } else {
                                model.date = "-";
                            }

                            model.orderStatus = doc.contains("status")
                                    ? doc.getString("status")
                                    : "Pending";

                            list.add(model);
                        }

                        adapter.notifyDataSetChanged();
                        recyclerOrders.setVisibility(View.VISIBLE);
                        tvNoOrders.setVisibility(View.GONE);
                    }

                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) {
            registration.remove();
        }
    }
}
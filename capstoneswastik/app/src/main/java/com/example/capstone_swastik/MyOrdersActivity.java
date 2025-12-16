package com.example.capstone_swastik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
        progressBar.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.GONE);
        tvNoOrders.setVisibility(View.GONE);

        String uid = auth.getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("userID", uid)
                .get()
                .addOnSuccessListener(query -> {
                    list.clear();

                    if (query.isEmpty()) {
                        tvNoOrders.setVisibility(View.VISIBLE);
                        tvNoOrders.setText("No orders found.");
                    } else {
                        ArrayList<OrderModel> tempList = new ArrayList<>(); // temporary list
                        for (DocumentSnapshot doc : query) {
                            OrderModel model = new OrderModel();
                            model.orderID = doc.getId();
                            model.productName = doc.getString("productName");

                            Number price = doc.get("productPrice") instanceof Number ? (Number) doc.get("productPrice") : 0;
                            model.productPrice = price.longValue();

                            Number total = doc.get("totalAmount") instanceof Number ? (Number) doc.get("totalAmount") : 0;
                            model.totalAmount = total.longValue();

                            Number qty = doc.get("quantity") instanceof Number ? (Number) doc.get("quantity") : 1;
                            model.quantity = qty.longValue();

                            // Image handling
                            Object imgObj = doc.get("image");
                            int imageRes = R.drawable.shree_swastik_default;

                            if (imgObj instanceof String) {
                                String imageName = (String) imgObj;
                                int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
                                if (resId != 0) imageRes = resId;
                            } else if (imgObj instanceof Number) {
                                imageRes = ((Number) imgObj).intValue();
                            }

                            model.img = imageRes;

                            long time = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : System.currentTimeMillis();
                            model.date = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                    .format(new Date(time));

                            model.orderStatus = doc.contains("status") ? doc.getString("status") : "Pending";

                            tempList.add(model);
                        }

                        // Only update list and notify adapter after all data processed
                        list.addAll(tempList);
                        adapter.notifyDataSetChanged();
                        recyclerOrders.setVisibility(View.VISIBLE); // show RecyclerView
                    }

                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvNoOrders.setVisibility(View.VISIBLE);
                    tvNoOrders.setText("Failed to load orders.");
                });
    }

}

package com.example.capstone_swastik;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<RequestModel> requestList;
    private RequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        rvRequests = findViewById(R.id.rvRequests);
        progressBar = findViewById(R.id.progressBar);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        requestList = new ArrayList<>();
        adapter = new RequestAdapter(requestList);
        rvRequests.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        progressBar.setVisibility(View.VISIBLE);

        String currentUid = auth.getCurrentUser().getUid();

        db.collection("supplier")
                .whereEqualTo("userID", currentUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot supplierDoc = querySnapshot.getDocuments().get(0);
                        String supplierId = supplierDoc.getString("supplierID");

                        db.collection("supplier_requests")
                                .whereEqualTo("supplierId", supplierId)
                                .get()
                                .addOnSuccessListener(requestsSnapshot -> {
                                    requestList.clear();
                                    for (DocumentSnapshot doc : requestsSnapshot) {
                                        RequestModel request = new RequestModel(
                                                doc.getString("productName"),
                                                doc.getString("areaInAcres"),
                                                doc.getString("growthMonths"),
                                                doc.getString("location"),
                                                doc.getString("supplierName"),
                                                doc.getString("status"),
                                                doc.getString("visitDate")
                                        );

                                        requestList.add(request);
                                    }
                                    adapter.notifyDataSetChanged();
                                    progressBar.setVisibility(View.GONE);

                                    if (requestList.isEmpty()) {
                                        Toast.makeText(this, R.string.no_requests_found, Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Supplier info not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to fetch supplier info", Toast.LENGTH_SHORT).show();
                });
    }
}

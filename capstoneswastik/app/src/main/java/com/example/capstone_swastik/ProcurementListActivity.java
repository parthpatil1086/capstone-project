package com.example.capstone_swastik;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class ProcurementListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProcurementListAdapter adapter;
    ArrayList<Procurement> list = new ArrayList<>();
    TextView emptyMessage;

    FirebaseFirestore db;
    FirebaseAuth auth;
    String currentUserUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procurement_list);

        recyclerView = findViewById(R.id.procurementRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        emptyMessage = findViewById(R.id.emptyMessage);

        adapter = new ProcurementListAdapter(list, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadProcurementForCurrentUser();
    }

    private void loadProcurementForCurrentUser() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUserUID = auth.getCurrentUser().getUid();

        db.collection("procurement")
                .whereEqualTo("userUID", currentUserUID)
                .get()
                .addOnSuccessListener(query -> {
                    list.clear();
                    if (query.isEmpty()) {
                        emptyMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        for (DocumentSnapshot doc : query) {
                            Procurement p = doc.toObject(Procurement.class);
                            if (p != null) list.add(p);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading procurement: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}

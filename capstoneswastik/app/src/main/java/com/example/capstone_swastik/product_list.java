package com.example.capstone_swastik;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class product_list extends AppCompatActivity {
ArrayList<ProductModel> arrProducts = new ArrayList<>();
    RecyclerView recyclerView;
    RecyclerProduct_listAdapter adapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerProduct);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecyclerProduct_listAdapter(this , arrProducts);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Fetch products from Firestore
        fetchProductsFromFirestore();
    }
    private void fetchProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        arrProducts.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId(); // ← auto-generated document ID
                            String name = document.getString("name");
                            String price = document.getString("price");
                            String imageName = document.getString("image");
                            String description = document.getString("description");

                            // Convert drawable name to resource ID
                            int imageRes = getResources().getIdentifier(imageName, "drawable", getPackageName());

                            arrProducts.add(new ProductModel(id,imageRes, name, price,description));
                        }
                        adapter.notifyDataSetChanged(); // Refresh RecyclerView
                    } else {
                        // Handle errors
                        System.out.println("Error fetching products: " + task.getException());
                    }
                });
    }
}
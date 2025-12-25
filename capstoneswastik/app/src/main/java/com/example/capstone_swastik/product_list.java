package com.example.capstone_swastik;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerProduct);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new RecyclerProduct_listAdapter(this, arrProducts);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchProductsFromFirestore();
    }

    private void fetchProductsFromFirestore() {
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    arrProducts.clear();

                    for (QueryDocumentSnapshot document : task.getResult()) {

                        String id = document.getId();
                        String name = document.getString("name");
                        String price = document.getString("price");
                        String imageName = document.getString("image");
                        String description = document.getString("description");

                        if (imageName == null || imageName.trim().isEmpty()) {
                            imageName = "placeholder";
                        }
                        int imageRes = getResources().getIdentifier(
                                imageName,
                                "drawable",
                                getPackageName()
                        );

                        if (imageRes == 0) {
                            imageRes = R.drawable.placeholder;
                        }

                        arrProducts.add(
                                new ProductModel(
                                        id,
                                        imageRes,
                                        name,
                                        price,
                                        description
                                )
                        );
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}

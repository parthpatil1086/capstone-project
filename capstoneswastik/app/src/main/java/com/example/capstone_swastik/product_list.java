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

import java.util.ArrayList;

public class product_list extends AppCompatActivity {
ArrayList<ProductModel> arrProducts = new ArrayList<>();
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.recyclerProduct);

        recyclerView.setLayoutManager(new GridLayoutManager(this,2));

        arrProducts.add(new ProductModel(R.drawable.img_p,"example1","2000"));
        arrProducts.add(new ProductModel(R.drawable.img_p,"example2","4000"));
        arrProducts.add(new ProductModel(R.drawable.img_p,"example3","6000"));
        arrProducts.add(new ProductModel(R.drawable.img_p,"example4","8000"));

        RecyclerProduct_listAdapter adapter = new RecyclerProduct_listAdapter(this , arrProducts);
        recyclerView.setAdapter(adapter);
    }
}
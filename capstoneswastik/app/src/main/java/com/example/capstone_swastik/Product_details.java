package com.example.capstone_swastik;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Product_details extends AppCompatActivity {

    TextView textViewProductName, textViewProductPrice, textViewProductDescription;
    ImageView imageViewProductimg;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);

        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        imageViewProductimg = findViewById(R.id.imageViewProductimg);
        textViewProductDescription = findViewById(R.id.textViewProductDescription);

        String name = getIntent().getStringExtra("name");
        String price = getIntent().getStringExtra("price");
        int img = getIntent().getIntExtra("img", R.drawable.shree_swastik_default);

        String description = getIntent().getStringExtra("description");

        textViewProductName.setText("Product Name : " + name);
        textViewProductPrice.setText("Product Price: " + price);
        imageViewProductimg.setImageResource(img);
        textViewProductDescription.setText(description != null ? description : "Loading description...");

    }
}


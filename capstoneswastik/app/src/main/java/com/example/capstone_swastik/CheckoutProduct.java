package com.example.capstone_swastik;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class CheckoutProduct extends AppCompatActivity {

    TextView textViewProductPrice, textViewProductName, totalprice;
    ImageView imageViewProductimg;
    TextView textQuantity;
    Button btnNext, btnPlus, btnMinus;

    int productPrice = 0;
    int quantity = 1;
    final int MAX_QUANTITY = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout_product);

        // UI binding
        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        imageViewProductimg = findViewById(R.id.imageViewProductimg);
        totalprice = findViewById(R.id.totalprice);

        textQuantity = findViewById(R.id.textQuantity);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        btnNext = findViewById(R.id.btnNext);

        // Receive data
        String name = getIntent().getStringExtra("name");
        String price = getIntent().getStringExtra("price");
        int img = getIntent().getIntExtra("img", R.drawable.shree_swastik_default);

        textViewProductName.setText(getString(R.string.product_name) + name);
        textViewProductPrice.setText(getString(R.string.product_price) + price);
        imageViewProductimg.setImageResource(img);

        try {
            productPrice = Integer.parseInt(price);
        } catch (NumberFormatException e) {
            productPrice = 0;
        }

        // Default quantity
        quantity = 1;
        textQuantity.setText(String.valueOf(quantity));
        updateTotal();

        btnNext.setEnabled(true);
        btnNext.setAlpha(1f);

        // PLUS button
        btnPlus.setOnClickListener(v -> {
            if (quantity < MAX_QUANTITY) {
                quantity++;
                textQuantity.setText(String.valueOf(quantity));
                updateTotal();
            } else {
                Toast.makeText(this,
                        R.string.maximum_quantity_allowed_is_15,
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                textQuantity.setText(String.valueOf(quantity));
                updateTotal();
            } else {
                Toast.makeText(
                        this,
                        "Minimum quantity is 1",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // NEXT button
        btnNext.setOnClickListener(view -> {
            int totalValue = quantity * productPrice;

            Intent intent = new Intent(this, Address_fill.class);
            intent.putExtra("quantity", quantity);
            intent.putExtra("totalValue", totalValue);
            intent.putExtra("name", name);
            intent.putExtra("price", productPrice);
            intent.putExtra("img", img);
            intent.putExtra("productId",
                    getIntent().getStringExtra("productId"));

            startActivity(intent);
        });
    }

    private void updateTotal() {
        int total = quantity * productPrice;
        totalprice.setText("TOTAL PRICE: ₹ " + total);
    }
}

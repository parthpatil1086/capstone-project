package com.example.capstone_swastik;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class CheckoutProduct extends AppCompatActivity {

    TextView textViewProductPrice, textViewProductName, totalprice;
    ImageView imageViewProductimg;
    EditText editTextQuantity;
    Button btnNext;

    int productPrice = 0;
    final int MAX_QUANTITY = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout_product);

        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        imageViewProductimg = findViewById(R.id.imageViewProductimg);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        totalprice = findViewById(R.id.totalprice);
        btnNext = findViewById(R.id.btnNext);

        btnNext.setEnabled(false);
        btnNext.setAlpha(0.5f);

        String name = getIntent().getStringExtra("name");
        String price = getIntent().getStringExtra("price");
        int img = getIntent().getIntExtra("img", R.drawable.shree_swastik_default);

        textViewProductName.setText("Product Name: " + name);
        textViewProductPrice.setText("Product Price: ₹ " + price);
        imageViewProductimg.setImageResource(img);

        try {
            productPrice = Integer.parseInt(price);
        } catch (NumberFormatException e) {
            productPrice = 0;
        }

        editTextQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().isEmpty()) {
                    totalprice.setText("TOTAL PRICE: ₹ 0");
                    btnNext.setEnabled(false);
                    btnNext.setAlpha(0.5f);
                    return;
                }

                int quantity = Integer.parseInt(s.toString());

                if (quantity > MAX_QUANTITY) {
                    Toast.makeText(CheckoutProduct.this, "Maximum quantity allowed is 15", Toast.LENGTH_SHORT).show();
                    quantity = MAX_QUANTITY;
                    editTextQuantity.setText(String.valueOf(MAX_QUANTITY));
                    editTextQuantity.setSelection(editTextQuantity.getText().length());
                }

                int total = quantity * productPrice;
                totalprice.setText("TOTAL PRICE: ₹ " + total);

                btnNext.setEnabled(quantity > 0);
                btnNext.setAlpha(quantity > 0 ? 1f : 0.5f);
            }
        });

        btnNext.setOnClickListener(view -> {
            String quantityStr = editTextQuantity.getText().toString().trim();
            int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);
            int totalValue = quantity * productPrice;

            Intent intent = new Intent(getApplicationContext(), Address_fill.class);
            intent.putExtra("quantity", quantity);
            intent.putExtra("totalValue", totalValue);
            intent.putExtra("name", name);
            intent.putExtra("price", productPrice);
            intent.putExtra("img", img);
            startActivity(intent);
        });
    }
}

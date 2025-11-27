package com.example.capstone_swastik;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OrderSuccessActivity extends AppCompatActivity {

    TextView tvMessage;
    Button btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        tvMessage = findViewById(R.id.tvMessage);
        btnContinue = findViewById(R.id.btnContinue);

        // Get data from Intent
        String userName = getIntent().getStringExtra("userName");
        String productName = getIntent().getStringExtra("productName");
        int totalAmount = getIntent().getIntExtra("totalAmount", 0);

        // Set dynamic success message
        tvMessage.setText("Hi " + userName + ", your order for \"" + productName + "\" has been placed Successfully!\n\nTotal Amount: ₹ " + totalAmount);

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);

            // Clear all previous activities
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            finish(); // Optional, activity will be cleared anyway
        });
    }
}

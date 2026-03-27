package com.example.capstone_swastik;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    int amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        amount = getIntent().getIntExtra("amount", 0);

        startPayment();
    }

    private void startPayment() {

        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_SJrtiKorRiqzPl"); // Your Razorpay Key ID

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Swastik Agro");
            options.put("description", "Order Payment");
            options.put("currency", "INR");
            options.put("amount", amount * 100); // Convert to paise

            checkout.open(this, options);

        } catch (Exception e) {
            Toast.makeText(this, "Payment Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {

        Intent intent = new Intent();
        intent.putExtra("paymentId", razorpayPaymentID);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show();
        finish();
    }
}
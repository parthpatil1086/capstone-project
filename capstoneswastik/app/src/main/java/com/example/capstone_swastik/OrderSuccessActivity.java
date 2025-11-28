package com.example.capstone_swastik;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

public class OrderSuccessActivity extends AppCompatActivity {

    TextView tvMessage;
    Button btnContinue;

    private static final String CHANNEL_ID = "ORDER_SUCCESS_CHANNEL";

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
        tvMessage.setText(
                "Hello " + userName +
                        ", your order for \"" + productName +
                        "\" has been placed Successfully!\n\nTotal Amount: ₹ " + totalAmount
        );


        showNotificationWithDelay(productName, totalAmount);

        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // Small delay to show notification smoothly
    private void showNotificationWithDelay(String productName, int totalAmount) {
        new android.os.Handler().postDelayed(() -> {
            makeNotification(productName, totalAmount);
        }, 500); // 0.5 second delay
    }

    public void makeNotification(String productName, int totalAmount) {

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Notification Channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    notificationManager.getNotificationChannel(CHANNEL_ID);

            if (channel == null) {
                channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Order Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifications for order success");
                channel.enableLights(true);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);

                notificationManager.createNotificationChannel(channel);
            }
        }

        // Intent when notification is clicked
        Intent intent = new Intent(getApplicationContext(), MyOrdersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        // Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Order Placed Successfully!")
                .setContentText(productName +" for ₹" + totalAmount + " Tap to Track Your Orders")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(100, builder.build());
    }
}

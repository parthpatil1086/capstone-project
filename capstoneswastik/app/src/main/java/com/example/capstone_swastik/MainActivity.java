package com.example.capstone_swastik;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    TextView user_name, user_number, user_email;
    Button btnlogout, browse_collection, btnMyorder,btnNotifications,btn_supplier;
    String userID;
    FirebaseAuth auth;
    FirebaseFirestore ftstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        ftstore = FirebaseFirestore.getInstance();

        user_name = findViewById(R.id.user_name);
        btnlogout = findViewById(R.id.btnlogout);
        browse_collection = findViewById(R.id.browse_collection);
        btnMyorder = findViewById(R.id.btnMyorder);
        btnNotifications = findViewById(R.id.btnNotifications);
        btn_supplier = findViewById(R.id.btn_supplier);

        userID = auth.getCurrentUser().getUid();

        DocumentReference documentReference = ftstore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    user_name.setText("Hey ," + documentSnapshot.getString("name"));
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
            }
        });

        browse_collection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), product_list.class);
                startActivity(intent);
            }
        });

        btnMyorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MyOrdersActivity.class);
                startActivity(intent);
            }
        });

        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NotificationStoreActivity.class);
                startActivity(intent);
            }
        });

        btn_supplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BecomeSupplier.class);
                startActivity(intent);
            }
        });

        // ===========================
        // ✅ Firestore Notification Listener
        // ===========================
        listenNotifications(); // start listening for new notifications
    }

    // Handle runtime permission result (optional logging)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted.");
            } else {
                Log.d(TAG, "Notification permission denied.");
            }
        }
    }

    // ===========================
    // Firestore Notification Methods
    // ===========================

    /**
     * Listen for new notifications in Firestore collection "notifications"
     * Shows a local notification whenever a new document is added
     */
    private void listenNotifications() {
        ftstore.collection("notifications")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;

                    if (snapshots != null) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String title = dc.getDocument().getString("title");
                                String body = dc.getDocument().getString("body");
                                String notifId = dc.getDocument().getId();
                                ArrayList<String> seenBy = (ArrayList<String>) dc.getDocument().get("seenBy");

                                if (seenBy == null) seenBy = new ArrayList<>();

                                // Show notification only if user hasn't seen it yet
                                if (!seenBy.contains(userID)) {
                                    showLocalNotification(title, body);

                                    // Add this user to seenBy array
                                    seenBy.add(userID);
                                    ftstore.collection("notifications")
                                            .document(notifId)
                                            .update("seenBy", seenBy);
                                }
                            }
                        }
                    }
                });
    }



    /**
     * Display a local notification on Android
     */
    private void showLocalNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "default";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true);

        // Unique ID to show multiple notifications
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}

package com.example.capstone_swastik;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    TextView user_name;
    ImageView nav_home, nav_browse, nav_notifications, nav_menu;

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
        nav_home = findViewById(R.id.nav_home);
        nav_browse = findViewById(R.id.nav_browse);
        nav_notifications = findViewById(R.id.nav_notifications);
        nav_menu = findViewById(R.id.nav_menu);

        userID = auth.getCurrentUser().getUid();

        // ================= USER NAME =================
        DocumentReference docRef =
                ftstore.collection("users").document(userID);

        docRef.addSnapshotListener(this, (snapshot, error) -> {
            if (snapshot != null && snapshot.exists()) {
                user_name.setText("Welcome, " + snapshot.getString("name"));
            }
        });

        // ================= NOTIFICATION PERMISSION =================
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        // ================= BOTTOM NAV =================
        nav_home.setOnClickListener(v -> {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
        });

        nav_browse.setOnClickListener(v ->
                startActivity(new Intent(this, product_list.class)));

        nav_notifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationStoreActivity.class)));

        nav_menu.setOnClickListener(v -> showBottomMenu());

        listenNotifications();
        listenProcurementNotifications();
    }

    // ================= SLIDER MENU (HALF SCREEN) =================
    private void showBottomMenu() {

        BottomSheetDialog dialog =
                new BottomSheetDialog(this,
                        com.google.android.material.R.style.Theme_Design_BottomSheetDialog);

        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottom_menu, null);

        dialog.setContentView(view);

        // ----- Half screen height -----
        View bottomSheet =
                dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior =
                    BottomSheetBehavior.from(bottomSheet);

            behavior.setPeekHeight(
                    getResources().getDisplayMetrics().heightPixels / 2
            );
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        // ----- Views -----
        ImageView btnClose = view.findViewById(R.id.btn_close);
        TextView menuUserName = view.findViewById(R.id.menu_user_name);
        TextView menuSupplier = view.findViewById(R.id.menu_supplier);
        TextView menuAbout = view.findViewById(R.id.menu_about);
        TextView menuContact = view.findViewById(R.id.menu_contact);
        TextView menuLogout = view.findViewById(R.id.menu_logout);

        menuUserName.setText(user_name.getText());

        btnClose.setOnClickListener(v -> dialog.dismiss());

        menuSupplier.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, BecomeSupplier.class));
        });

        menuAbout.setOnClickListener(v ->
                Toast.makeText(this, "Currently not available", Toast.LENGTH_SHORT).show()
        );

        menuContact.setOnClickListener(v ->
                Toast.makeText(this, "Currently not available", Toast.LENGTH_SHORT).show()
        );

        menuLogout.setOnClickListener(v -> {
            dialog.dismiss();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, login.class));
            finish();
        });

        dialog.show();
    }

    // ================= PERMISSION RESULT =================
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            Log.d(TAG, grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    ? "Notification permission granted"
                    : "Notification permission denied");
        }
    }

    // ================= GENERAL NOTIFICATIONS =================
    private void listenNotifications() {
        ftstore.collection("notifications")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {

                            String title = dc.getDocument().getString("title");
                            String body = dc.getDocument().getString("body");
                            String notifId = dc.getDocument().getId();

                            ArrayList<String> seenBy =
                                    (ArrayList<String>) dc.getDocument().get("seenBy");

                            if (seenBy == null) seenBy = new ArrayList<>();

                            if (!seenBy.contains(userID)) {
                                showLocalNotification(title, body);
                                seenBy.add(userID);

                                ftstore.collection("notifications")
                                        .document(notifId)
                                        .update("seenBy", seenBy);
                            }
                        }
                    }
                });
    }

    private void showLocalNotification(String title, String message) {
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String channelId = "default";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                    new NotificationChannel(channelId,
                            "Default",
                            NotificationManager.IMPORTANCE_HIGH));
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ================= PROCUREMENT =================
    private void listenProcurementNotifications() {
        ftstore.collection("procurement")
                .whereEqualTo("userUID", userID)
                .whereEqualTo("notify", true)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {

                            DocumentSnapshot doc = dc.getDocument();

                            String supplierName = doc.getString("supplierName");
                            double totalAmount =
                                    doc.getDouble("totalAmount") != null
                                            ? doc.getDouble("totalAmount") : 0;
                            String status = doc.getString("status");

                            showProcurementNotification(
                                    "Procurement Update",
                                    supplierName + " - ₹" + totalAmount + " (" + status + ")"
                            );

                            ftstore.collection("procurement")
                                    .document(doc.getId())
                                    .update("notify", false);
                        }
                    }
                });
    }

    private void showProcurementNotification(String title, String message) {
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String channelId = "procurement_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                    new NotificationChannel(channelId,
                            "Procurement Notifications",
                            NotificationManager.IMPORTANCE_HIGH));
        }

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0,
                        new Intent(this, ProcurementListActivity.class),
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}

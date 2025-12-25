package com.example.capstone_swastik;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
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
    ImageView nav_home, nav_browse, nav_notifications, nav_menu, nav_cart;
    String userID;
    FirebaseAuth auth;
    FirebaseFirestore ftstore;

    @Override
    protected void attachBaseContext(Context newBase) {
        String lang = LocaleHelper.getLanguage(newBase);
        LocaleHelper.setLocale(newBase, lang);
        super.attachBaseContext(newBase);
    }

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
        nav_cart = findViewById(R.id.nav_cart);

        userID = auth.getCurrentUser().getUid();

        DocumentReference docRef =
                ftstore.collection("users").document(userID);

        docRef.addSnapshotListener(this, (snapshot, error) -> {
            if (snapshot != null && snapshot.exists()) {
                user_name.setText(
                        getString(R.string.welcome) + ", " +
                                snapshot.getString("name")
                );
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        nav_home.setOnClickListener(v ->
                Toast.makeText(this, R.string.home, Toast.LENGTH_SHORT).show());

        nav_browse.setOnClickListener(v ->
                startActivity(new Intent(this, product_list.class)));

        nav_notifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationStoreActivity.class)));

        nav_menu.setOnClickListener(v -> showBottomMenu());

        nav_cart.setOnClickListener(v ->
                startActivity(new Intent(this, MyOrdersActivity.class)));

        listenNotifications();
        listenProcurementNotifications();
    }

    private void showBottomMenu() {
        BottomSheetDialog dialog =
                new BottomSheetDialog(this,
                        com.google.android.material.R.style.Theme_Design_BottomSheetDialog);

        View view = LayoutInflater.from(this)
                .inflate(R.layout.bottom_menu, null);

        dialog.setContentView(view);

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

        ImageView btnClose = view.findViewById(R.id.btn_close);
        TextView menuUserName = view.findViewById(R.id.menu_user_name);
        TextView menuSupplier = view.findViewById(R.id.menu_supplier);
        TextView menuAbout = view.findViewById(R.id.menu_about);
        TextView menuLogout = view.findViewById(R.id.menu_logout);

        // Language UI
        TextView tvLanguageLabel = view.findViewById(R.id.tv_language_label);
        Switch switchLanguage = view.findViewById(R.id.switch_language);

        menuUserName.setText(user_name.getText());

        // ================= LANGUAGE STATE =================
        String currentLang = LocaleHelper.getLanguage(this);

        if ("mr".equals(currentLang)) {
            // Marathi active → option to switch to English
            switchLanguage.setChecked(true);
            tvLanguageLabel.setText("English");
        } else {
            // English active → option to switch to Marathi
            switchLanguage.setChecked(false);
            tvLanguageLabel.setText("मराठी");
        }

        // ================= ACTIONS =================
        btnClose.setOnClickListener(v -> dialog.dismiss());

        menuSupplier.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, BecomeSupplier.class));
        });

        menuAbout.setOnClickListener(v ->
                Toast.makeText(this,
                        getString(R.string.not_available),
                        Toast.LENGTH_SHORT).show());

        // ================= LANGUAGE SWITCH =================
        switchLanguage.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                // ON → Marathi
                LocaleHelper.setLocale(this, "mr");
            } else {
                // OFF → English
                LocaleHelper.setLocale(this, "en");
            }

            dialog.dismiss();
            recreate(); // reload activity with new language
        });

        // ================= LOGOUT =================
        menuLogout.setOnClickListener(v -> {
            dialog.dismiss();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, login.class));
            finish();
        });

        dialog.show();
    }

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

    /* ================= GENERAL NOTIFICATIONS ================= */
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
                        .setSmallIcon(R.drawable.ic_notifications)
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    /* ================= PROCUREMENT ================= */
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

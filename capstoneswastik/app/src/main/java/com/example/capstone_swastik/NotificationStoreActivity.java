package com.example.capstone_swastik;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationStoreActivity extends AppCompatActivity {

    ListView listView;
    List<NotificationModel> notificationsList;
    NotificationAdapter adapter;
    FirebaseFirestore ftstore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_store);

        listView = findViewById(R.id.listViewNotifications);
        notificationsList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationsList);
        listView.setAdapter(adapter);

        ftstore = FirebaseFirestore.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadNotifications();
    }

    private void loadNotifications() {
        ftstore.collection("notifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationsList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String body = doc.getString("body");
                        long timestamp = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0;

                        String time = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                .format(new Date(timestamp));

                        notificationsList.add(new NotificationModel(title, body, time));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}

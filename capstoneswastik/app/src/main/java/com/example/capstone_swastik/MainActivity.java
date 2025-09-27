package com.example.capstone_swastik;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MainActivity extends AppCompatActivity {

    TextView user_name,user_number,user_email ;
    Button btnlogout,browse_collection;
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

        userID = auth.getCurrentUser().getUid();

        DocumentReference documentReference = ftstore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    user_name.setText("Hey ,"+documentSnapshot.getString("name"));
                }
            }
        });

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
              //  Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), product_list.class);
                startActivity(intent);
//
            }

        });
    }
}
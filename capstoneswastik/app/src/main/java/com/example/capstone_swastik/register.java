package com.example.capstone_swastik;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class register extends AppCompatActivity {

    private FirebaseAuth mAuth;
    FirebaseFirestore ftstore;
    private TextInputEditText editTextEmail, editTextPassword,editTextName,editTextNumber;
    private ProgressBar progressBar;
    private Button buttonReg;
    private TextView textViewLogin;

    String userID;


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        ftstore = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textViewLogin = findViewById(R.id.loginNow);
        editTextName =findViewById(R.id.name);
        editTextNumber = findViewById(R.id.phnumber);

        // Navigate to Login activity if user clicks "Login Now"
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(register.this, login.class);
                startActivity(intent);
                finish();
            }
        });

        // Register button click
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString().trim();
                String number = editTextNumber.getText().toString().trim();
                String name = editTextName.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Input validation
                if (TextUtils.isEmpty(name)) {
                    editTextName.setError("Enter Name");
                    editTextName.requestFocus();
                    return;
                }
                if (TextUtils.isEmpty(number)) {
                    editTextNumber.setError("Enter Number");
                    editTextNumber.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    editTextEmail.setError("Enter Email");
                    editTextEmail.requestFocus();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editTextEmail.setError("Enter a valid Email");
                    editTextEmail.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    editTextPassword.setError("Enter Password");
                    editTextPassword.requestFocus();
                    return;
                }

                if (password.length() < 6) {
                    editTextPassword.setError("Password must be at least 6 characters");
                    editTextPassword.requestFocus();
                    return;
                }

                // Show progress
                progressBar.setVisibility(View.VISIBLE);

                // Firebase registration
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    userID = mAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = ftstore.collection("users").document(userID);
                                    Map<String,Object> user = new HashMap<>();

                                    user.put("name",name);
                                    user.put("number",number);
                                    user.put("email",email);
                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d(TAG, "onSuccess: user profile is created "+userID);
                                        }
                                    });
                                    Toast.makeText(register.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(register.this, MainActivity.class));
                                    finish();
                                } else {
                                    // Show exact Firebase error
                                    Toast.makeText(register.this, "Authentication failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                    Log.d(TAG, "onComplete: "+task.getException().getMessage());
                                }
                            }
                        });
            }
        });
    }
}
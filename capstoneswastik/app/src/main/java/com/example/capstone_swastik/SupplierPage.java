package com.example.capstone_swastik;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SupplierPage extends AppCompatActivity {

    private EditText etProductName, etQuantity, etLocation, etGrowthMonths;
    private Button btnSubmit, statusbtn, btnPickImage;
    private TextView tvSelectedImageName;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Uri selectedImageUri;
    private String uploadedImageUrl = "";

    private String supplierName = "Unknown Supplier";
    private String supplierId = "unknown";
    private String supplierPhone = "unknown";

    private static final int CAMERA_PERMISSION_CODE = 101;

    private static final String IMGBB_API_KEY =
            "b777809c8e4c8bfb4761b4374cae12e3";

    // ===== Gallery Launcher =====
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            showImageName(selectedImageUri);
                            uploadImageToImgBB();
                        }
                    });

    // ===== Camera Launcher =====
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bitmap bitmap = (Bitmap) result.getData()
                                    .getExtras().get("data");
                            uploadCameraImage(bitmap);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_page);

        etProductName = findViewById(R.id.etProductName);
        etQuantity = findViewById(R.id.etQuantity);
        etLocation = findViewById(R.id.etLocation);
        etGrowthMonths = findViewById(R.id.etGrowthMonths);

        btnSubmit = findViewById(R.id.btnSubmitRequest);
        statusbtn = findViewById(R.id.statusbtn);
        btnPickImage = findViewById(R.id.btnPickImage);
        tvSelectedImageName = findViewById(R.id.tvSelectedImageName);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etProductName.setText(R.string.sugarcane);

        // Load supplier info
        if (auth.getCurrentUser() != null) {
            db.collection("supplier")
                    .whereEqualTo("userID", auth.getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(qs -> {
                        if (!qs.isEmpty()) {
                            DocumentSnapshot d = qs.getDocuments().get(0);
                            supplierName = d.getString("name");
                            supplierId = d.getString("supplierID");
                            supplierPhone = d.getString("phone");
                        }
                    });
        }

        btnPickImage.setOnClickListener(v -> showImageChooser());

        btnSubmit.setOnClickListener(v -> sendRequest());

        statusbtn.setOnClickListener(v ->
                startActivity(new Intent(this, ViewRequestsActivity.class)));
    }

    // ===== Image chooser dialog =====
    private void showImageChooser() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // ===== CAMERA PERMISSION SAFE OPEN =====
    private void openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Toast.makeText(this,
                        "Camera permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showImageName(Uri uri) {
        String name = "Selected Image";
        Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(
                    MediaStore.Images.Media.DISPLAY_NAME);
            if (index != -1) name = cursor.getString(index);
            cursor.close();
        }
        tvSelectedImageName.setText(name);
    }

    private void uploadImageToImgBB() {
        try {
            InputStream inputStream =
                    getContentResolver().openInputStream(selectedImageUri);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            uploadBase64Image(
                    Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));

        } catch (Exception e) {
            Toast.makeText(this, "Image error", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadCameraImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);

        uploadBase64Image(
                Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT));

        tvSelectedImageName.setText("Camera Image");
    }

    private void uploadBase64Image(String encodedImage) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                "https://api.imgbb.com/1/upload",
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        uploadedImageUrl =
                                json.getJSONObject("data").getString("url");
                        Toast.makeText(this,
                                "Image uploaded successfully",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Upload parse error",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this,
                        "Upload failed",
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("key", IMGBB_API_KEY);
                params.put("image", encodedImage);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void sendRequest() {
        String area = etQuantity.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String growthMonths = etGrowthMonths.getText().toString().trim();

        if (area.isEmpty() || location.isEmpty()
                || growthMonths.isEmpty() || uploadedImageUrl.isEmpty()) {
            Toast.makeText(this,
                    "Fill all fields & upload image",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("productName", "Sugarcane");
        request.put("areaInAcres", area);
        request.put("location", location);
        request.put("growthMonths", growthMonths);
        request.put("supplierId", supplierId);
        request.put("supplierName", supplierName);
        request.put("supplierPhone", supplierPhone);
        request.put("imageUrl", uploadedImageUrl);
        request.put("status", "Pending");
        request.put("visitDate", "");
        request.put("timestamp", System.currentTimeMillis());

        db.collection("supplier_requests")
                .add(request)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this,
                            "Request sent successfully",
                            Toast.LENGTH_SHORT).show();
                    etQuantity.setText("");
                    etLocation.setText("");
                    etGrowthMonths.setText("");
                    tvSelectedImageName.setText("No image selected");
                    uploadedImageUrl = "";
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}

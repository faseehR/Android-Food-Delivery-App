package com.example.foodx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class VendorSignUpActivity extends AppCompatActivity {

    EditText nameInput, emailInput, passwordInput, catchPhraseInput;
    ImageView logoImage;
    Button signUpButton;
    TextView signInText;
    Uri logoUri;
    Bitmap logoBitmap;

    FirebaseAuth auth;
    FirebaseFirestore db;

    ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_sign_up);

        nameInput = findViewById(R.id.vendorName);
        emailInput = findViewById(R.id.vendorEmail);
        passwordInput = findViewById(R.id.vendorPassword);
        catchPhraseInput = findViewById(R.id.vendorCatchPhrase);
        logoImage = findViewById(R.id.vendorLogo);
        signUpButton = findViewById(R.id.vendorSignUpBtn);
        signInText = findViewById(R.id.signInText);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        logoUri = uri;
                        logoImage.setImageURI(uri);
                        try {
                            logoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        logoImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        signUpButton.setOnClickListener(v -> signUpVendor());

        signInText.setOnClickListener(v -> {
            startActivity(new Intent(this, VendorSignInActivity.class));
            finish();
        });
    }

    private void signUpVendor() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String phrase = catchPhraseInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phrase.isEmpty() || logoBitmap == null) {
            Toast.makeText(this, "Please fill all fields and select a logo", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUser().getUid();

                    // Convert Bitmap to Base64
                    String base64Image="";
                    if (logoBitmap != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        logoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageBytes = baos.toByteArray();

                        if (imageBytes.length > 0) {
                             base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                            Log.d("Base64Image", base64Image.substring(0, 100)); // Just print first 100 chars
                        } else {
                            Log.e("ImageEncoding", "Image byte array is empty");
                        }
                    } else {
                        Log.e("ImageEncoding", "Bitmap is null");
                    }

                    Map<String, Object> vendorData = new HashMap<>();
                    vendorData.put("name", name);
                    vendorData.put("email", email);
                    vendorData.put("catchPhrase", phrase);
                    vendorData.put("logoBase64", base64Image);

                    db.collection("vendors").document(uid).set(vendorData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Sign-up successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, VendorSignInActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to save vendor info:"+e.getMessage(), Toast.LENGTH_SHORT).show());

                })
                .addOnFailureListener(e -> Toast.makeText(this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }
}

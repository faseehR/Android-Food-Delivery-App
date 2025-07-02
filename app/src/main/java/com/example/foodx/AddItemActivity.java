package com.example.foodx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    EditText itemNameEditText, itemSizeEditText, itemPriceEditText;
    ImageView itemImageView;
    Button addItemButton;

    String base64Image = "";
    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        itemNameEditText = findViewById(R.id.itemNameEditText);
        itemSizeEditText = findViewById(R.id.itemSizeEditText);
        itemPriceEditText = findViewById(R.id.itemPriceEditText);
        itemImageView = findViewById(R.id.itemImageView);
        addItemButton = findViewById(R.id.submitItemButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        itemImageView.setOnClickListener(view -> openGallery());

        addItemButton.setOnClickListener(view -> saveItemToDatabase());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            itemImageView.setImageURI(imageUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                base64Image = convertBitmapToBase64(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void saveItemToDatabase() {
        String itemName = itemNameEditText.getText().toString().trim();
        String itemSize = itemSizeEditText.getText().toString().trim();
        String itemPrice = itemPriceEditText.getText().toString().trim();
        String vendorId = auth.getCurrentUser().getUid();

        if (itemName.isEmpty() || itemSize.isEmpty() || itemPrice.isEmpty() || base64Image.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select image", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> item = new HashMap<>();
        item.put("name", itemName);
        item.put("size", itemSize);
        item.put("price", itemPrice);
        item.put("image", base64Image);
        item.put("vendorId", vendorId);

        db.collection("items")
                .add(item)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();
                    finish(); // close AddItemActivity and go back
                })
                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());


    }
}

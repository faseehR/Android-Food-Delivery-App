package com.example.foodx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class VendorMenuActivity extends AppCompatActivity {

    RecyclerView vendorRecyclerView;
    VendorAdapter vendorAdapter;
    List<Vendor> vendorList = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_menu);

        vendorRecyclerView = findViewById(R.id.vendorRecyclerView);
        vendorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        vendorAdapter = new VendorAdapter(this, vendorList);
        vendorRecyclerView.setAdapter(vendorAdapter);

        // Load vendors from Firestore
        loadVendors();
    }

    private void loadVendors() {
        // Query Firestore for vendors
        db.collection("vendors")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    // Clear the vendor list and add new data from Firestore
                    vendorList.clear();
                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            Vendor vendor = doc.toObject(Vendor.class);
                            if (vendor != null) {
                                vendor.setVendorId(doc.getId());  // Set vendorId from Firestore document ID
                                vendorList.add(vendor);
                            }
                        }
                        vendorAdapter.notifyDataSetChanged();
                    } else {
                        // No vendors found
                        Toast.makeText(this, "No vendors available", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    Toast.makeText(this, "Failed to load vendors: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

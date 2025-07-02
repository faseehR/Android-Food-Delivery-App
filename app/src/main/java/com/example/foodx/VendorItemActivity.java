package com.example.foodx;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class VendorItemActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private FirebaseFirestore db;
    private Button btnAddNewItem, btnCurrentOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_item);

        recyclerView = findViewById(R.id.recyclerView);
        btnAddNewItem = findViewById(R.id.btnAddNewItem);
        btnCurrentOrders = findViewById(R.id.btnCurrentOrders);

        itemList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter(itemList);
        recyclerView.setAdapter(itemAdapter);

        fetchItemsFromFirestore();

        btnAddNewItem.setOnClickListener(v -> {
            Intent intent = new Intent(VendorItemActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        btnCurrentOrders.setOnClickListener(v -> {
            String vendorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent intent = new Intent(VendorItemActivity.this, CurrentOrdersActivity.class);
            intent.putExtra("vendorId", vendorId);
            startActivity(intent);
        });

    }

    private void fetchItemsFromFirestore() {
        String vendorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("items")
                .whereEqualTo("vendorId", vendorId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        itemList.clear();
                        QuerySnapshot querySnapshot = task.getResult();

                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            try {
                                Item item = document.toObject(Item.class);
                                itemList.add(item);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error parsing item :"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        itemAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to fetch items", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchItemsFromFirestore(); // Refresh list when returning
    }
}

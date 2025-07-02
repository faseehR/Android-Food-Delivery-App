package com.example.foodx;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class UserItemActivity extends AppCompatActivity {

    private static final int CART_REQUEST_CODE = 1001;

    private RecyclerView recyclerView;
    private Button viewCartBtn;
    private EditText searchEditText;

    private UserItemAdapter adapter;
    private List<Item> itemList = new ArrayList<>();
    private List<Item> cartList = new ArrayList<>();

    private FirebaseFirestore db;
    private String vendorId;
    private boolean orderExists = false;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_item);

        recyclerView = findViewById(R.id.recyclerView);
        viewCartBtn = findViewById(R.id.btnViewCart);
        searchEditText = findViewById(R.id.searchEditText);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        vendorId = getIntent().getStringExtra("vendorId");
        if (vendorId == null) {
            Toast.makeText(this, "Vendor ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new UserItemAdapter(this, itemList, item -> {
            if (!orderExists) {
                cartList.add(item);
                Toast.makeText(this, item.getName() + " added to cart", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "You already have a pending order with this vendor.", Toast.LENGTH_LONG).show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        viewCartBtn.setOnClickListener(v -> {
            if (!orderExists) {
                if (cartList.isEmpty()) {
                    Toast.makeText(this, "Cart is empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(UserItemActivity.this, CartActivity.class);
                ArrayList<String> names = new ArrayList<>();
                ArrayList<String> prices = new ArrayList<>();

                for (Item item : cartList) {
                    names.add(item.getName());
                    prices.add(item.getPrice());
                }

                intent.putStringArrayListExtra("itemNames", names);
                intent.putStringArrayListExtra("itemPrices", prices);
                intent.putExtra("vendorId", vendorId);
                startActivityForResult(intent, CART_REQUEST_CODE);
            } else {
                Intent intent = new Intent(UserItemActivity.this, CartActivity.class);
                intent.putExtra("vendorId", vendorId);
                startActivityForResult(intent, CART_REQUEST_CODE);
            }
        });

        checkIfActiveOrderExists();
        loadVendorItems();
    }

    private void filterItems(String query) {
        List<Item> filteredList = new ArrayList<>();
        for (Item item : itemList) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        adapter.updateData(filteredList);
    }

    private void checkIfActiveOrderExists() {
        db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("vendorId", vendorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderExists = false;
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String status = doc.getString("status");
                        if (status != null && !status.equalsIgnoreCase("completed")) {
                            orderExists = true;
                            Toast.makeText(this, "You already have an active order with this vendor.", Toast.LENGTH_LONG).show();
                            break;
                        }
                    }

                    adapter.setOrderExists(orderExists);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to check order status", Toast.LENGTH_SHORT).show()
                );
    }

    private void loadVendorItems() {
        db.collection("items")
                .whereEqualTo("vendorId", vendorId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Item item = doc.toObject(Item.class);
                        if (item != null) {
                            itemList.add(item);
                        }
                    }
                    adapter.updateData(itemList); // also update here
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load items", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CART_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("pickupConfirmed", false)) {
                cartList.clear();
                checkIfActiveOrderExists();
            }
        }
    }
}

package com.example.foodx;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class CurrentOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private FirebaseFirestore db;
    private OrderAdapter ordersAdapter;
    private ArrayList<OrderModel> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_orders);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        db = FirebaseFirestore.getInstance();

        orderList = new ArrayList<>();
        ordersAdapter = new OrderAdapter(this, orderList);

        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(ordersAdapter);

        fetchVendorOrders();
    }

    private void fetchVendorOrders() {
        String vendorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("vendorId", vendorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        ArrayList<String> itemNames = (ArrayList<String>) doc.get("itemNames");
                        ArrayList<String> itemPrices = (ArrayList<String>) doc.get("itemPrices");
                        String status = doc.getString("status");
                        String userId = doc.getString("userId");

                        orderList.add(new OrderModel(id, itemNames, itemPrices, status, userId));
                    }
                    ordersAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch orders", Toast.LENGTH_SHORT).show();
                });
    }
}

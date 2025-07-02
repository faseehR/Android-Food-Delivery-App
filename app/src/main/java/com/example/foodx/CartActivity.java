package com.example.foodx;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private TextView cartSummaryText, orderStatusText;
    private Button placeOrderButton, confirmPickupButton;

    private FirebaseFirestore db;
    private ArrayList<String> itemNames;
    private ArrayList<String> itemPrices;

    private String vendorId;
    private String userId;
    private String currentOrderId = null;

    private ListenerRegistration orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        cartSummaryText = findViewById(R.id.cartSummaryText);
        orderStatusText = findViewById(R.id.orderStatusText);
        placeOrderButton = findViewById(R.id.placeOrderButton);
        confirmPickupButton = findViewById(R.id.confirmPickupButton);
        confirmPickupButton.setVisibility(View.GONE);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize from Intent but will override if active order found
        itemNames = getIntent().getStringArrayListExtra("itemNames");
        itemPrices = getIntent().getStringArrayListExtra("itemPrices");
        vendorId = getIntent().getStringExtra("vendorId");

        if (vendorId == null) {
            Toast.makeText(this, "Vendor ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkExistingOrder();

        placeOrderButton.setOnClickListener(v -> placeOrder());
        confirmPickupButton.setOnClickListener(v -> confirmPickup());
    }

    private void displayCartSummary() {
        if (itemNames == null || itemPrices == null || itemNames.isEmpty()) {
            cartSummaryText.setText("Your cart is empty!");
            placeOrderButton.setEnabled(false);
            return;
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format("%-20s %-10s %-10s\n", "Name", "Price", "Qty"));
        summary.append("---------------------------------------------\n");

        double total = 0.0;

        for (int i = 0; i < itemNames.size(); i++) {
            String name = itemNames.get(i);
            String priceStr = itemPrices.get(i).replaceAll("[^\\d.]", "");
            double price = 0.0;

            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price for " + name, Toast.LENGTH_SHORT).show();
            }

            summary.append(String.format("%-20s $%-9.2f %-10s\n", name, price, "1"));
            total += price;
        }

        summary.append("\nTotal: $").append(String.format("%.2f", total));
        cartSummaryText.setText(summary.toString());
        placeOrderButton.setEnabled(true);
    }

    private void placeOrder() {
        if (currentOrderId != null) {
            Toast.makeText(this, "You already have a placed order.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (itemNames == null || itemPrices == null || itemNames.isEmpty() || itemPrices.isEmpty()) {
            Toast.makeText(this, "Your cart is empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> order = new HashMap<>();
        order.put("itemNames", itemNames);
        order.put("itemPrices", itemPrices);
        order.put("status", "order placed");
        order.put("vendorId", vendorId);
        order.put("userId", userId);

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    currentOrderId = documentReference.getId();
                    Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show();
                    placeOrderButton.setEnabled(false);
                    attachStatusListener(documentReference);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Order failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void checkExistingOrder() {
        db.collection("orders")
                .whereEqualTo("userId", userId)
                .whereEqualTo("vendorId", vendorId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean foundOrder = false;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String status = doc.getString("status");

                        // ✅ Skip completed orders
                        if ("completed".equalsIgnoreCase(status)) {
                            continue;
                        }

                        currentOrderId = doc.getId();

                        ArrayList<String> namesFromDb = (ArrayList<String>) doc.get("itemNames");
                        ArrayList<String> pricesFromDb = (ArrayList<String>) doc.get("itemPrices");

                        if (namesFromDb != null && pricesFromDb != null && !namesFromDb.isEmpty()) {
                            itemNames = new ArrayList<>(namesFromDb);
                            itemPrices = new ArrayList<>(pricesFromDb);
                            displayCartSummary();
                        } else {
                            itemNames = new ArrayList<>();
                            itemPrices = new ArrayList<>();
                            cartSummaryText.setText("Order exists but contains no items.");
                            Toast.makeText(this, "Active order found with missing item data.", Toast.LENGTH_LONG).show();
                        }

                        updateStatusUI(status);
                        placeOrderButton.setEnabled(false);
                        attachStatusListener(doc.getReference());
                        foundOrder = true;
                        break;
                    }

                    if (!foundOrder) {
                        if (itemNames == null || itemNames.isEmpty()) {
                            cartSummaryText.setText("Your cart is empty!");
                            placeOrderButton.setEnabled(false);
                        } else {
                            displayCartSummary();
                            placeOrderButton.setEnabled(true);
                        }
                        orderStatusText.setText("Status: No active order");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check existing orders", Toast.LENGTH_SHORT).show();
                    if (itemNames == null || itemNames.isEmpty()) {
                        cartSummaryText.setText("Your cart is empty!");
                        placeOrderButton.setEnabled(false);
                    } else {
                        displayCartSummary();
                        placeOrderButton.setEnabled(true);
                    }
                });
    }



    private void attachStatusListener(DocumentReference orderRef) {
        if (orderListener != null) orderListener.remove();
        orderListener = orderRef.addSnapshotListener((snapshot, error) -> {
            if (error != null || snapshot == null || !snapshot.exists()) return;
            String status = snapshot.getString("status");
            updateStatusUI(status);
        });
    }

    private void updateStatusUI(String status) {
        orderStatusText.setText("Status: " + status);

        int colorRes;
        switch (status.toLowerCase()) {
            case "order placed":
                colorRes = android.R.color.holo_blue_light;
                confirmPickupButton.setVisibility(View.GONE);
                break;
            case "accepted":
                colorRes = android.R.color.holo_green_light;
                confirmPickupButton.setVisibility(View.GONE);
                break;
            case "cooking":
                colorRes = android.R.color.holo_orange_light;
                confirmPickupButton.setVisibility(View.GONE);
                break;
            case "pick up":
                colorRes = android.R.color.holo_red_light;
                confirmPickupButton.setVisibility(View.VISIBLE);
                break;
            default:
                colorRes = android.R.color.white;
                confirmPickupButton.setVisibility(View.GONE);
        }

        orderStatusText.setTextColor(ContextCompat.getColor(this, colorRes));
    }

    private void confirmPickup() {
        if (currentOrderId != null) {
            db.collection("orders")
                    .document(currentOrderId)
                    .update("status", "completed")
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Order marked as completed. Thank you!", Toast.LENGTH_SHORT).show();

                        // Reset UI and clear cart
                        confirmPickupButton.setVisibility(View.GONE);
                        orderStatusText.setText("Status: No active order");
                        placeOrderButton.setEnabled(true);
                        currentOrderId = null;

                        if (itemNames != null) itemNames.clear();
                        if (itemPrices != null) itemPrices.clear();
                        cartSummaryText.setText("Cart is now empty.");

                        // Inform UserItemActivity (or parent)
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("pickupConfirmed", true);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to mark order as completed", Toast.LENGTH_SHORT).show()
                    );
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderListener != null) {
            orderListener.remove();
        }
    }
}

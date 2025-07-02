package com.example.foodx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private ArrayList<OrderModel> orderList;
    private FirebaseFirestore db;

    public OrderAdapter(Context context, ArrayList<OrderModel> orderList) {
        this.context = context;
        this.orderList = orderList;
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orderList.get(position);
        holder.orderDetails.setText(formatOrder(order));
        holder.statusText.setText("Status: " + order.status);

        holder.acceptButton.setOnClickListener(v -> updateStatus(order.orderId, "accepted"));
        holder.cookingButton.setOnClickListener(v -> updateStatus(order.orderId, "cooking"));
        holder.pickupButton.setOnClickListener(v -> updateStatus(order.orderId, "pick up"));
    }

    private void updateStatus(String orderId, String newStatus) {
        db.collection("orders").document(orderId)
                .update("status", newStatus);
    }

    private String formatOrder(OrderModel order) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < order.itemNames.size(); i++) {
            sb.append(order.itemNames.get(i)).append(" - ").append(order.itemPrices.get(i)).append("\n");
        }
        sb.append("User: ").append(order.userId);
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDetails, statusText;
        Button acceptButton, cookingButton, pickupButton;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDetails = itemView.findViewById(R.id.orderDetails1);
            statusText = itemView.findViewById(R.id.statusText1);
            acceptButton = itemView.findViewById(R.id.acceptButton1);
            cookingButton = itemView.findViewById(R.id.cookingButton1);
            pickupButton = itemView.findViewById(R.id.pickupButton1);
        }
    }
}

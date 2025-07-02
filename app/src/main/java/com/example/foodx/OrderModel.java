package com.example.foodx;

import java.util.ArrayList;

public class OrderModel {
    public String orderId;
    public ArrayList<String> itemNames;
    public ArrayList<String> itemPrices;
    public String status;
    public String userId;

    public OrderModel(String orderId, ArrayList<String> itemNames, ArrayList<String> itemPrices, String status, String userId) {
        this.orderId = orderId;
        this.itemNames = itemNames;
        this.itemPrices = itemPrices;
        this.status = status;
        this.userId = userId;
    }
}

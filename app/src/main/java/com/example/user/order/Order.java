package com.example.user.order;

import com.example.user.model.CartItem;
import com.google.firebase.Timestamp;

import java.util.List;

public class Order {
    public String orderID;
    public Timestamp orderTime;
    public String name, phoneNo;
    public List<CartItem> orderItems;
    public int action;
    public int total_price, total_items;

    public Order() {
    }

    public Order(String orderID, Timestamp orderTime, String name, String phoneNo, List<CartItem> orderItems, int action, int total_price, int total_items) {
        this.orderID = orderID;
        this.orderTime = orderTime;
        this.name = name;
        this.phoneNo = phoneNo;
        this.orderItems = orderItems;
        this.action = action;
        this.total_price = total_price;
        this.total_items = total_items;
    }

    public static class OrderStatus {

        public static final int PLACED = 1 // Initially (U)
                , DELIVERED = 0, DECLINED = -1;     //(A)

    }
}
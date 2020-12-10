package com.example.user.model;

import java.io.Serializable;
import java.util.List;

public class Inventory implements Serializable {
    public List<Product> products;

    public Inventory() {
    }

    public Inventory(List<Product> productList) {
        this.products = productList;
    }
}
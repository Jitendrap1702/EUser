package com.example.user.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Product implements Serializable {
    public static final int WEIGHT_BASED = 0, VARIENTS_BASED = 1;

    public String name;
    public int type;
    public int pricePerKg;
    public float minQty;
    public List<Varient> variants = new ArrayList<>();

    public Product() {
    }

    public Product(String name, int pricePerKg, float minQty) {
        type = WEIGHT_BASED;
        this.name = name;
        this.pricePerKg = pricePerKg;
        this.minQty = minQty;
    }

    public Product(String name) {
        type = VARIENTS_BASED;
        this.name = name;
    }

    public void fromVarientStrings(String[] vs) {
        variants = new ArrayList<>();
        for (String s : vs) {
            String[] v = s.split(",");
            variants.add(new Varient(v[0], Integer.parseInt(v[1])));
        }
    }


    @Override
    public @NotNull String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", pricePerKg=" + pricePerKg +
                ", minQty=" + minQty +
                ", varientsList=" + variants +
                '}';
    }

    public String varientsString() {
        String variantsString = variants.toString();
//        return varients.replace("[", "")
//                .replace("]", "")
//                .replace(",", ", ");
        return variantsString
                .replaceFirst("\\[","")
                .replaceFirst("]","")
                .replaceAll(",",", ");
    }

    public void makeWeightProduct(String name, int pricePerKg, float minQty) {
        type = WEIGHT_BASED;
        this.name = name;
        this.pricePerKg = pricePerKg;
        this.minQty = minQty;
    }

    public void makeVarientProduct(String name) {
        type = VARIENTS_BASED;
        this.name = name;
    }

    public String convertMinQtyToWeight(float quantity) {
        if (minQty < 1) {
            return (int) (minQty * 1000) + "g";
        }
        return (int) (minQty) + "kg";
    }

    public String minQtyToWeight(float qty) {
        StringBuilder builder = new StringBuilder();
        builder.append((int) Math.floor(qty) + "kg ");
        qty -= Math.floor(qty);
        builder.append((int) (qty * 1000) + "g");
        return builder.toString();
    }

    public String priceString() {
        if (type == WEIGHT_BASED) {
            return "Rs. " + pricePerKg + "/kg";
        }
        return varientsString();
    }

}

package com.example.user.controllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.example.user.MainActivity;
import com.example.user.databinding.SingleVbItemBinding;
import com.example.user.model.Cart;
import com.example.user.model.Product;

public class SingleVBProductViewBinder {
    SingleVbItemBinding binding;
    Product product;
    Cart cart;

    public SingleVBProductViewBinder(SingleVbItemBinding binding, Product product, Cart cart) {
        this.binding = binding;
        this.product = product;
        this.cart = cart;
    }

    public void bindData() {
        binding.addBtnSingleVb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cart.addVarientBasedProductToCart(product, product.variants.get(0));

                updateViews(1);
            }
        });

        binding.incrementBtnSingleVb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = cart.addVarientBasedProductToCart(product, product.variants.get(0));

                updateViews(quantity);
            }
        });

        binding.decrementBtnSingleVb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = cart.removeVarientBasedProductFromCart(product, product.variants.get(0));

                updateViews(quantity);
            }
        });
        if (product.variants != null && product.variants.size() != 0){
            if (cart.totalItemMap.containsKey(product.name + " " + product.variants.get(0).name)) {
            updateViews(cart.totalItemMap.get(product.name + " " + product.variants.get(0).name));
        }
        }

    }

    @SuppressLint("SetTextI18n")
    private void updateViews(int quantity) {
        if (quantity == 1) {
            binding.addBtnSingleVb.setVisibility(View.GONE);
            binding.decrementBtnSingleVb.setVisibility(View.VISIBLE);
            binding.quantitySingleVb.setVisibility(View.VISIBLE);
            binding.incrementBtnSingleVb.setVisibility(View.VISIBLE);
        } else if (quantity == 0) {
            binding.addBtnSingleVb.setVisibility(View.VISIBLE);
            binding.decrementBtnSingleVb.setVisibility(View.GONE);
            binding.quantitySingleVb.setVisibility(View.GONE);
            binding.incrementBtnSingleVb.setVisibility(View.GONE);
        }

        binding.quantitySingleVb.setText(quantity + "");

        updateCheckOutSummary();

    }

    private void updateCheckOutSummary() {
        Context context = binding.getRoot().getContext();
        if (context instanceof MainActivity) {
            ((MainActivity) context).updateCheckOutSummary();
        } else {
            Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }
}
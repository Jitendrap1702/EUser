package com.example.user;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.user.constants.Constants;
import com.example.user.databinding.ActivityCartBinding;
import com.example.user.databinding.CartItemViewBinding;
import com.example.user.fcmsender.FCMSender;
import com.example.user.fcmsender.MessageFormatter;
import com.example.user.model.Cart;
import com.example.user.model.CartItem;
import com.example.user.order.Order;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CartActivity extends AppCompatActivity {

    ActivityCartBinding binding;
    Cart cart;
    MyApp myApp;

    // userDetails
    public String myId;
    public String username;
    public String phoneNo;

    SharedPreferences preferences;
    String sharedPreferencesFile = "com.example.android.userapp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences(sharedPreferencesFile, MODE_PRIVATE);

        myApp = (MyApp) getApplicationContext();

        Intent intent = getIntent();
        cart = (Cart) intent.getSerializableExtra("data");
        myId = intent.getStringExtra(Constants.MYID);
        username = intent.getStringExtra(Constants.USERNAME);
        phoneNo = intent.getStringExtra(Constants.PHONENO);

        showCartItems();

        showItemsAndPrice();

    }


    @SuppressLint("SetTextI18n")
    private void showCartItems() {
        for (Map.Entry<String, CartItem> map : cart.map.entrySet()) {
            CartItemViewBinding b = CartItemViewBinding.inflate(
                    getLayoutInflater()
            );

            b.cartItemName.setText("" + map.getKey());

            b.cartItemPrice.setText("Rs. " + map.getValue().price);

            if (map.getValue().name.contains("kg")) {
                b.cartItemWeight.setText((int) (map.getValue().quantity) + " x Rs. " + (map.getValue().price) / ((int) (map.getValue().quantity)));
            } else {
                b.cartItemWeight.setText((int) (map.getValue().quantity) + "kg x Rs. " + (map.getValue().price) / ((int) (map.getValue().quantity)) + "/kg");
            }

            setupDeleteButton(b, map.getKey(), map.getValue());


            binding.cartItems.addView(b.getRoot());
        }
    }

    private void setupDeleteButton(CartItemViewBinding b, String key, CartItem value) {
        b.deleteCartItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                cart.removeItemWithKey(key, value);

                binding.cartItems.removeView(b.getRoot());

                showItemsAndPrice();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showItemsAndPrice() {
        binding.items.setText("Items : " + cart.noOfItems);
        binding.price.setText("Price : Rs. " + cart.totalPrice);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent latestCartIntent = new Intent();
            latestCartIntent.putExtra("new", cart);
            setResult(RESULT_OK, latestCartIntent);
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.place_order_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.place_order){
            askForConfirm();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void askForConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Do you want to order?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        placeOrder();
                        Toast.makeText(CartActivity.this, "Ordered",Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(CartActivity.this,"Cancelled!", Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void placeOrder() {

        List<CartItem> orderItems = new ArrayList<>();

        for(Map.Entry<String, CartItem> map : cart.map.entrySet()){
            orderItems.add(map.getValue());
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String format = simpleDateFormat.format(new Date());

        String orderID =  format + " " + myId ;
        Order newOrder = new Order(
                orderID,
                Timestamp.now(),
                preferences.getString(Constants.USERNAME,username ),
                preferences.getString(Constants.PHONENO, phoneNo),
                orderItems,
                Order.OrderStatus.PLACED,
                cart.totalPrice,
                cart.noOfItems
        );

        myApp.db.collection("orders").document(orderID)
                .set(newOrder)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(com.example.user.CartActivity.this, "Order Placed", Toast.LENGTH_SHORT).show();
                        sendnotification();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(com.example.user.CartActivity.this, "Failed to place order!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void sendnotification(){
        String message = MessageFormatter.getSampleMessage("admin","New Order","From:"+ username);

        new FCMSender().send(message
                , new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CartActivity.this, "Failure!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(CartActivity.this)
                                        .setTitle("Order Successfully placed!")
                                        .setMessage(response.toString())
                                        .show();
                            }
                        });
                    }
                });
    }
}
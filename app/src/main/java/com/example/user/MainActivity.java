package com.example.user;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.user.adapter.ProductsAdapter;
import com.example.user.constants.Constants;
import com.example.user.databinding.ActivityMainBinding;
import com.example.user.fcmsender.FCMSender;
import com.example.user.fcmsender.MessageFormatter;
import com.example.user.model.Cart;
import com.example.user.model.Inventory;
import com.example.user.model.Product;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Callback;

import static com.example.user.databinding.ActivityMainBinding.inflate;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    Cart cart = new Cart();
    private ProductsAdapter adapter;
    private List<Product> list;
    private MyApp app;

    SharedPreferences preferences;
    String sharedPreferencesFile = "com.example.android.userapp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#880061")));

        preferences = getSharedPreferences(sharedPreferencesFile, MODE_PRIVATE);

        setup();

        Intent signInIntent = getIntent();
        String myEmailId = signInIntent.getStringExtra(Constants.MYID);
        String username = signInIntent.getStringExtra(Constants.USERNAME);
        String phoneNo = signInIntent.getStringExtra(Constants.PHONENO);

        setupTopic();

        fetchProductsListFromCloudFirestore();

        binding.checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                intent.putExtra("data", cart)
                        .putExtra(Constants.MYID, myEmailId)
                        .putExtra(Constants.USERNAME, username)
                        .putExtra(Constants.PHONENO, phoneNo);
                startActivityForResult(intent, 1);
                sendNotification();
            }
        });

    }
    private void setupTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("users");
    }

    private void sendNotification() {
        String message = MessageFormatter.getSampleMessage("users", "Test2", "Tes2");

        new FCMSender()
                .send(message
                        , new Callback() {
                            @Override
                            public void onFailure(okhttp3.Call call, IOException e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Failure")
                                                .setMessage(e.toString())
                                                .show();
                                    }
                                });
                            }

                            @Override
                            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new AlertDialog.Builder(MainActivity.this)
                                                .setTitle("Success")
                                                .setMessage(response.toString())
                                                .show();
                                    }
                                });

                            }
                        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Cart newCart = (Cart) data.getSerializableExtra("new");

                cart.changeCart(newCart);

                adapter.notifyDataSetChanged();

                updateCheckOutSummary();
            }
        }

    }

    private void setup() {
        app = (MyApp) getApplicationContext();
    }

    private void fetchProductsListFromCloudFirestore() {

        if (app.isOffline()) {
            app.showToast(MainActivity.this, "No Internet!");
            return;
        }

        app.showLoadingDialog(this);

        app.db.collection(Constants.INVENTORY).document(Constants.PRODUCTS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            //Toast.makeText(MainActivity.this, "Loading ......\n Fetch data from cloud", Toast.LENGTH_SHORT).show();
                            Inventory inventory = documentSnapshot.toObject(Inventory.class);
                            list = inventory.products;
                        } else {
                            list = new ArrayList<>();
                        }
                        setupList();
                        app.hideLoadingDialog();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Can't load", Toast.LENGTH_SHORT).show();
                        app.hideLoadingDialog();
                    }
                });
    }

    private void setupList() {
//        list = new ArrayList<>();
//        Product apple = new Product("Apple", 100, 1);
//        Product banana = new Product("Banana", 30, 2);
//        Product orange = new Product("Orange", 80, 3);
//        list.add(apple);
//        list.add(banana);
//        list.add(orange);

        adapter = new ProductsAdapter(this, list, cart);
        binding.recyclerView.setAdapter(adapter);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration itemDecor = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        binding.recyclerView.addItemDecoration(itemDecor);


    }

    public void updateCheckOutSummary() {
        if (cart.noOfItems == 0) {
            binding.checkout.setVisibility(View.GONE);
        } else {
            binding.checkout.setVisibility(View.VISIBLE);
            binding.cartSummary.setText("Total: Rs. " + cart.totalPrice + "\n" + cart.noOfItems + " items");
        }
    }

    /** Options Menu **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_options_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signout_btn){
            askForConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void askForConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure to sign out?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeIfFromSharedPref();
                        setupSignOut();

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        app.showToast(MainActivity.this,"Cancelled!");
                    }
                })
                .show();
    }

    private void setupSignOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        app.showToast(MainActivity.this,"SIGNED OUT!");
                        //startActivity(new Intent(MainActivity.this,SignInActivity.class));
                        finish();
                    }
                });
    }


    private void removeIfFromSharedPref(){
        preferences = getSharedPreferences("SignInID", MODE_PRIVATE);
        preferences.edit().remove(SignInActivity.MY_ID)
                .remove(Constants.USERNAME)
                .apply();
    }
}
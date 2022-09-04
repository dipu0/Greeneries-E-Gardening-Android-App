package com.chowdhuryelab.greeneries;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chowdhuryelab.greeneries.SendNotificationPack.APIService;
import com.chowdhuryelab.greeneries.SendNotificationPack.Client;
import com.chowdhuryelab.greeneries.SendNotificationPack.Data;
import com.chowdhuryelab.greeneries.SendNotificationPack.MyResponse;
import com.chowdhuryelab.greeneries.SendNotificationPack.NotificationSender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private TextInputEditText upiEt, nameEt, noteEt;
    private Button payBtn;
    private TextView amountTv;

    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;

    private String shopId, myLatitude, myLongitude, grandTotal, token;

    APIService apiService_payment;

    final int UPI_PAYMENT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        amountTv = findViewById(R.id.amountTV);
        upiEt = findViewById(R.id.upiET);
        nameEt = findViewById(R.id.upiET);
        noteEt = findViewById(R.id.noteET);
        payBtn = findViewById(R.id.payBtn);

        shopId = getIntent().getStringExtra("ShopId");
        myLatitude = getIntent().getStringExtra("Latitude");
        myLongitude = getIntent().getStringExtra("Longitude");
        grandTotal = getIntent().getStringExtra("GrandTotal");
        grandTotal = grandTotal.replace("\u09F3", "").replaceAll("\\(.*?\\)", "").trim();
        amountTv.setText(grandTotal);
        Log.d("Amount", amountTv.getText()+"");

        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        apiService_payment = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        UpdateToken();
        findUserToken(shopId);

        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = amountTv.getText().toString();
                String upiId = upiEt.getText().toString();
                String name = nameEt.getText().toString();
                String note = noteEt.getText().toString();
                submitOrder();

            }
        });
    }
    private void UpdateToken(){

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            System.out.println("Fetching FCM registration token failed "+ task.getResult());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("token", ""+token);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(mAuth.getUid())
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //Toast.makeText(OrderDetailsShopActivity.this, "Token updated", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(PaymentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }
                });

    }

    private void submitOrder() {
        mProgressDialog.setMessage("Placing Order....");
        mProgressDialog.show();

        final String timestamp = ""+System.currentTimeMillis();
        String cost = grandTotal;

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", timestamp);
        hashMap.put("orderTime", timestamp);
        hashMap.put("orderStatus", "Request Pending");
        hashMap.put("orderCost", cost);
        hashMap.put("orderBy", ""+mAuth.getUid());
        hashMap.put("OrderFrom", ""+shopId);
        hashMap.put("latitude", myLatitude);
        hashMap.put("longitude", myLongitude);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopId).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Using SingleEventListener instead of ValueEventListener
                        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getUid());
                        ref1.child("CartItem").child(shopId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    String finalPrice =""+ds.child("finalPrice").getValue();
                                    String productCategory =""+ds.child("prductCategory").getValue();
                                    String ItemImage = ""+ds.child("profileImage").getValue();
                                    String quantity = ""+ds.child("quantity").getValue();
                                    String title = ""+ds.child("title").getValue();
                                    String pId = ""+ds.child("productId").getValue();

                                    HashMap<String, String> hashMap1 = new HashMap<>();
                                    hashMap1.put("finalPrice", finalPrice);
                                    hashMap1.put("productCategory", productCategory);
                                    hashMap1.put("ItemImage", ItemImage);
                                    hashMap1.put("quantity", quantity);
                                    hashMap1.put("title", title);
                                    hashMap1.put("pId", pId);

                                    Log.d("title", title);

                                    ref.child(timestamp).child("items").child(pId).setValue(hashMap1);

                                }
                                mProgressDialog.dismiss();
                                Toast.makeText(PaymentActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(PaymentActivity.this, OrderDetailsBuyerActivity.class);
                                intent.putExtra("orderFrom", shopId);
                                intent.putExtra("orderId", timestamp);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        sendNotifications(token,"Hello!","You have new order","MainSellerActivity");
                        ref1.child("CartItem").removeValue().equals(shopId);
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(PaymentActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private String findUserToken(String user){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(user)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        token = ""+dataSnapshot.child("token").getValue();
                        System.out.println("Token found from payment: "+ token);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        return token;
    }

    private void sendNotifications(String user, String title, String message, String activity) {
        Data data = new Data(title, message, activity);

        NotificationSender sender1 = new NotificationSender(data, token);
        System.out.println("User Token: "+token);
        apiService_payment.sendNotifcation(sender1).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(PaymentActivity.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }


//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.d("lifecycle","onStart invoked");
//    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Payment lifecycle","onResume invoked");
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Payment lifecycle","onPause invoked");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Payment lifecycle","onStop invoked");
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Payment lifecycle","onRestart invoked");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Payment lifecycle","onDestroy invoked");
    }


}

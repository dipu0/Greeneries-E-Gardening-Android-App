package com.chowdhuryelab.greeneries;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.chowdhuryelab.greeneries.SendNotificationPack.APIService;
import com.chowdhuryelab.greeneries.SendNotificationPack.Client;
import com.chowdhuryelab.greeneries.SendNotificationPack.Data;
import com.chowdhuryelab.greeneries.SendNotificationPack.MyResponse;
import com.chowdhuryelab.greeneries.SendNotificationPack.NotificationSender;
import com.chowdhuryelab.greeneries.adapters.AdapterOrderedItem;
import com.chowdhuryelab.greeneries.models.ModelOrderedItems;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailsShopActivity extends AppCompatActivity {

   // public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private MapView mMapView;

    private String orderId, orderBy, myLatitude, myLongitude, buyerLatitude,buyerLongitude, deliveryFee, userToken;

    private ImageButton backBtn, editStatusBtn;
    private TextView orderIdTv, dateTv, orderStatusTv, buyerEmailTv, buyerPhoneTv, totalItemTv, amountTv, deliveryAddressTv;
    private RecyclerView itemsRv;

    private ArrayList<ModelOrderedItems> orderedItemsArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    private FirebaseAuth mAuth;

    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_shop);

        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        mAuth = FirebaseAuth.getInstance();

        backBtn = findViewById(R.id.backBtn);
        editStatusBtn = findViewById(R.id.editStatusBtn);
        orderIdTv = findViewById(R.id.orderIdTV);
        dateTv = findViewById(R.id.dateTV);
        orderStatusTv = findViewById(R.id.orderStatusTV);
        buyerEmailTv = findViewById(R.id.buyerEmailTV);
        buyerPhoneTv = findViewById(R.id.buyerPhoneTV);
        totalItemTv = findViewById(R.id.totalItemTV);
        amountTv = findViewById(R.id.amountTV);
        deliveryAddressTv = findViewById(R.id.deliveryAddressTV);
        itemsRv = findViewById(R.id.itemsRV);

//        mMapView = findViewById(R.id.user_location_map);
//        initGoogleMap(savedInstanceState);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        UpdateToken();
        loadMyInfo();
        loadBuyerInfo();
        loadOrdersdetail();
        loadOrdersItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        deliveryAddressTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        editStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editStatusDialog();
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
                                        Toast.makeText(OrderDetailsShopActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }
                });

    }


//    private void initGoogleMap(Bundle savedInstanceState){
//        // *** IMPORTANT ***
//        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
//        // objects or sub-Bundles.
//        Bundle mapViewBundle = null;
//        if (savedInstanceState != null) {
//            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
//        }
//
//        mMapView.onCreate(mapViewBundle);
//
//        mMapView.getMapAsync(this);
//    }

    private void editStatusDialog() {
        final String[] options = {"Package Processing Started", "Package being Prepared", "Shipped","Completed", "Cancelled"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedOption  = options[which];
                        editOrderStatus(selectedOption);
                        sendNotifications(userToken, "Order Status",""+selectedOption, "MainBuyerActivity");
                        System.out.println("USER TOKEN:"+ userToken);
                    }
                }).show();
    }

    private void sendNotifications(String usertoken, String title, String message, String activity) {
        Data data = new Data(title, message, activity);
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                if (response.code() == 200) {
                    if (response.body().success != 1) {
                        Toast.makeText(OrderDetailsShopActivity.this, "Failed ", Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {

            }
        });
    }

    private void editOrderStatus(final String selectedOption) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", ""+selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(OrderDetailsShopActivity.this, "Order is now "+selectedOption, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderDetailsShopActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrdersdetail() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String orderBy = ""+dataSnapshot.child("orderBy").getValue();
                        String orderCost = ""+dataSnapshot.child("orderCost").getValue();
                        String orderId = ""+dataSnapshot.child("orderId").getValue();
                        String orderStatus = ""+dataSnapshot.child("orderStatus").getValue();
                        String orderTime = ""+dataSnapshot.child("orderTime").getValue();
                        String orderFrom = ""+dataSnapshot.child("orderFrom").getValue();

                        orderCost = orderCost.replaceAll("\\(.*[\\)]", "").replaceAll(" \\(", "").replaceAll("\\)", "");

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formateDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

                        if (orderStatus.equals("Package Processing Started")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }
                        else if (orderStatus.equals("Package being Prepared")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorAccent));
                        }
                        else if (orderStatus.equals("Shipped")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        }
                        else if (orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        }
                        else {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed01));
                        }

                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        double total = Double.parseDouble(orderCost)+Double.parseDouble(deliveryFee);
                        amountTv.setText("\u09F3"+ total +" [including \u09F3"+ deliveryFee +" Delivery Fee]");
                        dateTv.setText(formateDate);

                        findAddress(buyerLatitude, buyerLongitude);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            deliveryAddressTv.setText(address + ", " + city + ", " + state + ", " + country);
        }
        catch (Exception e) {

        }
    }

    String totalItems;
    private void loadOrdersItems() {
        orderedItemsArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("Orders").child(orderId).child("items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        orderedItemsArrayList.clear();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ModelOrderedItems modelOrderedItems = ds.getValue(ModelOrderedItems.class);
                            orderedItemsArrayList.add(modelOrderedItems);
                        }
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsShopActivity.this, orderedItemsArrayList);
                        itemsRv.setAdapter(adapterOrderedItem);
                        totalItems = ""+dataSnapshot.getChildrenCount();
                        totalItemTv.setText(totalItems);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        myLatitude = ""+dataSnapshot.child("latitude").getValue();
                        myLongitude = ""+dataSnapshot.child("longitude").getValue();
                        deliveryFee = ""+dataSnapshot.child("deliveryFee").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        buyerLatitude = ""+dataSnapshot.child("latitude").getValue();
                        buyerLongitude = ""+dataSnapshot.child("longitude").getValue();
                        String email = ""+dataSnapshot.child("email").getValue();
                        String phone = ""+dataSnapshot.child("phone").getValue();
                        userToken = ""+dataSnapshot.child("token").getValue();

                        buyerEmailTv.setText(email);
                        buyerPhoneTv.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void openMap() {
        String address = "https://maps.google.com/maps?saddr="+myLatitude+","+myLongitude+"&daddr="+buyerLatitude+","+buyerLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }
//    @Override
//    public void onMapReady(GoogleMap map) {
//        map.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(myLatitude),Double.parseDouble(myLongitude))).title("Marker"));
//    }
//    @Override
//    public void onResume() {
//        super.onResume();
//        mMapView.onResume();
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        mMapView.onStart();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        mMapView.onStop();
//    }
//
//
//
//    @Override
//    public void onPause() {
//        mMapView.onPause();
//        super.onPause();
//    }
//
//    @Override
//    public void onDestroy() {
//        mMapView.onDestroy();
//        super.onDestroy();
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        mMapView.onLowMemory();
//    }
}
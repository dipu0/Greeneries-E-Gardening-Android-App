package com.chowdhuryelab.greeneries;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chowdhuryelab.greeneries.SendNotificationPack.APIService;
import com.chowdhuryelab.greeneries.SendNotificationPack.Client;
import com.chowdhuryelab.greeneries.adapters.AdapterAllBlogShow;
import com.chowdhuryelab.greeneries.adapters.AdapterOrderUser;
import com.chowdhuryelab.greeneries.adapters.AdapterAllProductShow;
import com.chowdhuryelab.greeneries.adapters.AdapterShop;
import com.chowdhuryelab.greeneries.models.ModelBlog;
import com.chowdhuryelab.greeneries.models.ModelOrderUser;
import com.chowdhuryelab.greeneries.models.ModelProduct;
import com.chowdhuryelab.greeneries.models.ModelShop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainBuyerActivity extends AppCompatActivity {

    private String cartcount;
    private TextView nameTv, emailTv, phoneTv, tabShopsTv, tabOrdersTv,tabAllTv, tabBlogTv, cartCountTv;
    private ImageButton logoutBtn, editProfileBtn, cartBtn;
    private FloatingActionButton addBlogBtn;
    private RelativeLayout shopRl, ordersRl, showAllProductRl, showAllBlogsRl;
    private ImageView profileIv;
    private RecyclerView shopRv, orderRv,productRv, showAllProductRv, showAllBlogsRv;
    private int count;

    FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;

    private ArrayList<ModelOrderUser> orderList;
    private AdapterOrderUser adapterOrderUser;

    private ArrayList<ModelShop> shopList;
    private AdapterShop adapterShop;

    private ArrayList<ModelProduct> productList;
    private AdapterAllProductShow adapterProductShow;

    private ArrayList<ModelBlog> blogList;
    private AdapterAllBlogShow adapterAllBlogShow;


    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_buyer);

        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        cartcount = getIntent().getStringExtra("shopUid");

        checkUser();

        nameTv = findViewById(R.id.nameTV);
        emailTv = findViewById(R.id.emailTV);
        phoneTv = findViewById(R.id.phoneTV);
        tabShopsTv = findViewById(R.id.tabShopsTV);
        tabAllTv = findViewById(R.id.tabAllTv);
        tabBlogTv = findViewById(R.id.tabBlogTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTV);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        cartBtn = findViewById(R.id.cartBtn);
        shopRl = findViewById(R.id.shopRL);
        showAllProductRl = findViewById(R.id.allRL);
        showAllBlogsRl = findViewById(R.id.blogRL);
        ordersRl = findViewById(R.id.ordersRL);
        profileIv = findViewById(R.id.profileIV);
        shopRv = findViewById(R.id.shopRV);
        showAllProductRv =findViewById(R.id.allRv);
        showAllBlogsRv = findViewById(R.id.blogRv);
        orderRv = findViewById(R.id.orderRv);
        cartCountTv = findViewById(R.id.cartCounterTV);
        addBlogBtn = findViewById(R.id.addBlogBtn);
        productRv = findViewById(R.id.productRV);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        showShopUI();
        UpdateToken();



        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                checkUser();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainBuyerActivity.this, ProfileEditBuyerActivity.class));
            }
        });

//        cartBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainBuyerActivity.this, CartActivity.class);
//                startActivity(intent);
//            }
//        });

        addBlogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainBuyerActivity.this, AddBlogActivity.class));
            }
        });


        tabShopsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShopUI();
            }
        });

        tabAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllproductUI();
            }
        });

        tabBlogTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllBlogUI();
            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrdersUI();
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
                                        //Toast.makeText(MainBuyerActivity.this, "Token updated", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainBuyerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });


                    }
                });

    }

    private void showShopUI() {
        shopRl.setVisibility(View.VISIBLE);
        showAllProductRl.setVisibility(View.GONE);
        showAllBlogsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.GONE);

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabShopsTv.setBackgroundResource(R.drawable.shap_rect04);

        tabAllTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabAllTv.setBackgroundResource(android.R.color.transparent);

        tabBlogTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabBlogTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void showAllproductUI() {
        shopRl.setVisibility(View.GONE);
        showAllProductRl.setVisibility(View.VISIBLE);
        showAllBlogsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.GONE);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabAllTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabAllTv.setBackgroundResource(R.drawable.shap_rect04);

        tabBlogTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabBlogTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }
    private void showAllBlogUI(){

        shopRl.setVisibility(View.GONE);
        showAllProductRl.setVisibility(View.GONE);
        showAllBlogsRl.setVisibility(View.VISIBLE);
        ordersRl.setVisibility(View.GONE);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabBlogTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabBlogTv.setBackgroundResource(R.drawable.shap_rect04);

        tabAllTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabAllTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    private void showOrdersUI() {
        shopRl.setVisibility(View.GONE);
        showAllProductRl.setVisibility(View.GONE);
        showAllBlogsRl.setVisibility(View.GONE);
        ordersRl.setVisibility(View.VISIBLE);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.colorBlack));
        tabOrdersTv.setBackgroundResource(R.drawable.shap_rect04);

        tabAllTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabAllTv.setBackgroundResource(android.R.color.transparent);

        tabBlogTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabBlogTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabShopsTv.setTextColor(getResources().getColor(R.color.colorWhite));
        tabShopsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void checkUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(MainBuyerActivity.this, LoginActivity.class));
            finish();;
        }
        else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String phone = ""+ds.child("phone").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();
                            String city = ""+ds.child("city").getValue();

                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.ic_person_gray).into(profileIv);
                            }
                            catch (Exception e) {
                                profileIv.setImageResource(R.drawable.ic_person_gray);
                            }

                            loadShop(city);
                            loadOrders();
                            loadAllProduct();
                            LoadAllBlogs();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void loadOrders() {
        orderList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                orderList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Orders");
                    ref.orderByChild("orderBy").equalTo(mAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    orderList.clear();
                                    if (dataSnapshot.exists()){
                                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                                            ModelOrderUser modelOrderUser = ds.getValue(ModelOrderUser.class);

                                            orderList.add(modelOrderUser);
                                        }
                                        adapterOrderUser = new AdapterOrderUser(MainBuyerActivity.this, orderList);
                                        orderRv.setAdapter(adapterOrderUser);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadShop(final String myCity) {

        shopList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountType").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        shopList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelShop modelShop = ds.getValue(ModelShop.class);

//                            String shopCity = ""+ds.child("city").getValue();

//                            if (shopCity.equals(myCity)){
                                shopList.add(modelShop);
//                            }
                        }

                        adapterShop = new AdapterShop(MainBuyerActivity.this, shopList);
                        shopRv.setAdapter(adapterShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadAllProduct(){
        productList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference ref = (DatabaseReference) FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Products");
                         //   ref.addValueEventListener(new ValueEventListener() {
                            ref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    productList.clear();
                                    if (dataSnapshot.exists()){
                                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);

                                            productList.add(modelProduct);
                                        }

                                        adapterProductShow = new AdapterAllProductShow(MainBuyerActivity.this, productList);
                                        showAllProductRv.setAdapter(adapterProductShow);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


//        ref.child(mAuth.getUid()).child("CartItem")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()){
//                            cartCountTv.setVisibility(View.VISIBLE);
//                            count = (int) dataSnapshot.getChildrenCount();
//                            cartCountTv.setText(String.valueOf(count));
//                        }else {
//                            cartCountTv.setVisibility(View.GONE);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
   }

    private void LoadAllBlogs(){

        blogList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                blogList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    String uid = ""+ds.getRef().getKey();

                    DatabaseReference ref = (DatabaseReference) FirebaseDatabase.getInstance().getReference("Users").child(uid).child("Blogs");
                    //   ref.addValueEventListener(new ValueEventListener() {
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                for (DataSnapshot ds : dataSnapshot.getChildren()){
                                    ModelBlog modelBlog = ds.getValue(ModelBlog.class);

                                    blogList.add(modelBlog);
                                }

                                adapterAllBlogShow = new AdapterAllBlogShow(MainBuyerActivity.this, blogList);
                                showAllBlogsRv.setAdapter(adapterAllBlogShow);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}

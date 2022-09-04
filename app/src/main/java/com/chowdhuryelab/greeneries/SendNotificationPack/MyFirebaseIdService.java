package com.chowdhuryelab.greeneries.SendNotificationPack;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.chowdhuryelab.greeneries.MainBuyerActivity;
import com.chowdhuryelab.greeneries.OrderDetailsShopActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import java.util.HashMap;

public class MyFirebaseIdService extends FirebaseMessagingService {
    FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public void onNewToken(String s){
        super.onNewToken(s);
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
                        if(mAuth!=null){
                            updateToken(token);
                        }

                    }
                });
    }
    private void updateToken(String token){
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
                        //Toast.makeText(MainBuyerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

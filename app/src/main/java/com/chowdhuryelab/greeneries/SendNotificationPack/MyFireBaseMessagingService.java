package com.chowdhuryelab.greeneries.SendNotificationPack;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.chowdhuryelab.greeneries.LoginActivity;
import com.chowdhuryelab.greeneries.MainBuyerActivity;
import com.chowdhuryelab.greeneries.MainSellerActivity;
import com.chowdhuryelab.greeneries.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFireBaseMessagingService extends FirebaseMessagingService {

    String title,message, activity="";
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
            super.onMessageReceived(remoteMessage);
            title=remoteMessage.getData().get("Title");
            message=remoteMessage.getData().get("Message");
            activity = remoteMessage.getData().get("Activity");


        System.out.println("aaaaaaaaaaaaaaaaaaaaaaa");
        if(activity != null){
            if (activity.equals("MainBuyerActivity")){
                showNotificationBuyer(title, message);

            }
            else if(activity.equals("MainSellerActivity")){

                showNotificationSeller(title, message);
            }else showNotification(title, message);
        }else showNotification(title, message);



    }

    //
    public void showNotification(String title, String message)
    {

//        Intent notificationIntent = new Intent(this, LoginActivity.class);
//        PendingIntent conPendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"channelID")
                .setSmallIcon(R.drawable.icon_shop)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = 1;
        createChannel(notificationManager);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void createChannel(NotificationManager notificationManager){
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationChannel channel = new NotificationChannel("channelID","name", NotificationManager.IMPORTANCE_DEFAULT);
      //  channel.setDescription("notification");
        notificationManager.createNotificationChannel(channel);
    }

    // notification for buyers
    public void showNotificationBuyer(String title, String message)
    {
//
//        Intent notificationIntent = new Intent(this, MainBuyerActivity.class);
//        @SuppressLint("UnspecifiedImmutableFlag")
//        PendingIntent conPendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder1 = new NotificationCompat.Builder(this,"channelID1")
                .setSmallIcon(R.drawable.icon_shop)
                .setContentTitle(title)
                .setContentText(message)
                //.setContentIntent(conPendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager1 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = 1;
        createChannel1(notificationManager1);
        notificationManager1.notify(notificationId, notificationBuilder1.build());
    }

    public void createChannel1(NotificationManager notificationManager1){
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationChannel channel1 = new NotificationChannel("channelID1","name", NotificationManager.IMPORTANCE_DEFAULT);
        //  channel.setDescription("notification");
        notificationManager1.createNotificationChannel(channel1);
    }

    //notifications for seller on product purchased
    public void showNotificationSeller(String title, String message)
    {
//        Intent notificationIntent = new Intent(this, MainSellerActivity.class);
//        @SuppressLint("UnspecifiedImmutableFlag")
//        PendingIntent conPendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder2 = new NotificationCompat.Builder(this,"channelId2")
                .setSmallIcon(R.drawable.icon_shop)
                .setContentTitle(title)
                .setContentText(message)
                //.setContentIntent(conPendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager2 = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = 2;
        createChannel2(notificationManager2);
        notificationManager2.notify(notificationId, notificationBuilder2.build());
    }

    public void createChannel2(NotificationManager notificationManager2){
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationChannel channel2 = new NotificationChannel("channelId2","name", NotificationManager.IMPORTANCE_DEFAULT);
        //  channel2.setDescription("notification");
        notificationManager2.createNotificationChannel(channel2);
    }


}

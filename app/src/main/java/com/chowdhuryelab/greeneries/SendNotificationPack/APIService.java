package com.chowdhuryelab.greeneries.SendNotificationPack;

import com.chowdhuryelab.greeneries.SendNotificationPack.MyResponse;
import com.chowdhuryelab.greeneries.SendNotificationPack.NotificationSender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAVtfzrRo:APA91bHZwiKnBcTPtJm_1ob4wuwsElrpGWzXKsTlzHgEfvMmqNKZxOfufsLaasTmFlwNNgTgACZ38XY3JIeKI7hFkZ1rUVWBWpOl3SG5zatk0remf067ENwCtItAFglvIz1wuII57ssp" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}


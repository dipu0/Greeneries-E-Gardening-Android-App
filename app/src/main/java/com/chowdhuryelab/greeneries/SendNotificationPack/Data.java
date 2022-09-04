package com.chowdhuryelab.greeneries.SendNotificationPack;

import android.app.Activity;

public class Data {
    private String Title;
    private String Message;
    private  String Activity;

    public Data(String title, String message) {
        Title = title;
        Message = message;
    }

    public Data(String title, String message, String activity) {
        Title = title;
        Message = message;
        Activity = activity;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getActivity() {
        return Activity;
    }

    public void setActivity(String activity) {
        Activity = activity;
    }
}

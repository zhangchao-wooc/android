package com.cenobots.myapplication.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "初始化 MyFirebaseMessagingService");
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // 这里处理收到的消息
//        String message = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "No Body";
//
//        Log.d(TAG, "获取到Firebase Token: " + message);
//        // 通过广播通知Activity
//        Intent intent = new Intent("com.yourapp.FCM_MESSAGE");
//        intent.putExtra("message", message);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // 处理收到的推送消息
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // 这里可以自行处理，比如存储数据、弹通知等
            Log.d(TAG, "获取到Firebase Token: " + title + "body:" + body);
//            handlePushMessage(title, body);
        } else if (remoteMessage.getData().size() > 0) {
            // 处理携带数据的消息
            Map<String, String> data = remoteMessage.getData();
//            handlePushData(data);
            printMapAsJson(data);
        }
    }

    public void printMapAsJson(Map<?, ?> map) {
        try {
            JSONObject jsonObject = new JSONObject(map);
            String jsonString = jsonObject.toString(2); // 缩进2个空格
            Log.d("MAP_JSON", jsonString);
        } catch (Exception e) {
            Log.e("MAP_JSON", "Error converting map to JSON", e);
            // 如果 JSON 转换失败，回退到基本打印
//            printMap(map);
        }
    }
}
package com.cenobots.myapplication.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.cenobots.myapplication.R;
import com.cenobots.myapplication.activities.MainActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
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
    public void onNewToken(String token) {
        super.onNewToken(token);
        // 当FCM Token刷新时，获取到最新的Token并上报
        Log.d("FCM", "Refreshed token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = null;
        Log.d(TAG, "这里处理收到的消息: ");

        if (remoteMessage.getData().size() > 0) {
            // 处理携带数据的消息
            data = remoteMessage.getData();
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            printMapAsJson(data);
        }


        // 处理收到的推送消息
        if (remoteMessage.getNotification() != null) {
//            Map<String, String> notification = null;
//            notification.put("title", remoteMessage.getNotification().getTitle());
//            notification.put("body", remoteMessage.getNotification().getBody());
//            notification.put("channelId", remoteMessage.getNotification().getChannelId());
//            notification.put("icon", remoteMessage.getNotification().getIcon());

            RemoteMessage.Notification notification = remoteMessage.getNotification();

            // 这里可以自行处理，比如存储数据、弹通知等
            Log.d(TAG, "Message Notification Body: " + notification + "data:" + data);
            showNotification(notification, data);
        }
    }

    private void showNotification(RemoteMessage.Notification notification, Map<String, String> data) {
        // 创建跳转到 MainActivity 的 Intent
        Intent intent = new Intent(this, MainActivity.class);

        // 将data数据添加到intent中
        String dataJsonString = new JSONObject(data).toString();
        intent.putExtra("data", dataJsonString);

        // 设置标志，确保每次点击都启动新的 MainActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 创建 PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String CHANNEL_ID = notification.getChannelId();

        // Android 8.0开始，所有通知都必须通过通知渠道发送，否则通知将不会显示。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "通知渠道", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.cenobots_icon)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setPriority(notification.getNotificationPriority())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1, builder.build());
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
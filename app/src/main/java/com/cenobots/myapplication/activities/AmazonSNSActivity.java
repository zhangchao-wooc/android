package com.cenobots.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

//import com.amazonaws.auth.StaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.cenobots.myapplication.R;
import com.google.firebase.messaging.FirebaseMessaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class AmazonSNSActivity extends AppCompatActivity {
    private static final String TAG = "Amazon SNS";

    private TextView tokenTextView;
    private TextView progressTextView;

    private static AmazonSNSClient snsClient;
    static String arnStorage = null;
    static String region = "us-west-2";
    // 替换为您的AWS SNS平台应用程序ARN
    private static final String PLATFORM_ARN = "arn:aws:sns:us-west-2:190322700616:app/GCM/myapplication";

    private BroadcastReceiver fcmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // 在这里处理接收到的推送消息
            Toast.makeText(context, "Received FCM message: " + message, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amazon_sns);

        // 注册广播接收器
        LocalBroadcastManager.getInstance(this).registerReceiver(fcmReceiver,
                new IntentFilter("com.yourapp.FCM_MESSAGE"));

        // 设置标题
        setTitle("Amazon SNS");

        // 获取 tokenTextView
        tokenTextView = findViewById(R.id.tokenTextView);
        progressTextView = findViewById(R.id.progressTextView);

        // 绑定按钮
        Button btn = findViewById(R.id.button_in_push);

        // 设置点击事件
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressTextView.setText("开始注册！");
                // 可以在这里添加其他操作，比如跳转、网络请求等
                initAmazonSNS();
            }
        });
    }

    public void initAmazonSNS() {
        getTokenWithFirebase();
    }

    public static AmazonSNSClient getSNSClient() {
        if (snsClient == null) {
            // 替换为你的AWS访问密钥和密钥
            String accessKeyId = "";
            String secretKey = "";

            // 创建基本凭证对象
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);

            // 创建 sns 实例
            snsClient = new AmazonSNSClient(credentials);
            // 设置区域 默认为 us-east-1
            snsClient.setRegion(Region.getRegion(region));
        }
        return snsClient;
    }

    // 获取Firebase Token
    public void getTokenWithFirebase() {
        if (progressTextView != null) {
            progressTextView.setText("正在获取Firebase Token...");
        }
        try {
        // 使用FirebaseMessaging获取token
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                Log.d(TAG, "获取到Firebase Token: " + task);
                if (!task.isSuccessful()) {
                    Log.w(TAG, "获取Firebase Token失败", task.getException());
                    if (progressTextView != null) {
                        progressTextView.setText("获取Token失败：" + task.getException().getMessage());
                    }
                    return;
                }
                
                // 获取新的FCM token
                String token = task.getResult();
                Log.d(TAG, "获取到Firebase Token: " + token);
                
                // 在UI上显示token
                if (tokenTextView != null) {
                    tokenTextView.setText("FCM Token: " + token);
                }

                // 将token注册到Amazon SNS
                registerDeviceWithSNS(token);
            });
    } catch (Exception e) {
        Log.e(TAG, "获取Firebase Token时发生异常", e);
        if (progressTextView != null) {
            progressTextView.setText("获取Token异常：" + e.getMessage());
        }
    }
    }

    // 将设备注册到Amazon SNS的方法
    private void registerDeviceWithSNS(String token) {
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "无法注册到SNS：token为空");
            return;
        }

        // 在新线程中执行SNS注册，避免阻塞主线程
        new Thread(() -> {
            try {
                // 获取MyApplication中的SNS客户端实例
                AmazonSNSClient snsClient = getSNSClient();
                if (snsClient == null) {
                    Log.e(TAG, "SNS客户端初始化失败");
                    return;
                }

                // 创建平台端点请求
                CreatePlatformEndpointRequest request = new CreatePlatformEndpointRequest();
                request.setPlatformApplicationArn(PLATFORM_ARN);
                request.setToken(token);

                // 可选：添加自定义数据
                Map<String, String> attributes = new HashMap<>();
                attributes.put("CustomUserData", "Android 测试设备");
                request.setAttributes(attributes);

                Log.d(TAG, "发送请求到SNS：" + arnStorage);
                progressTextView.setText("发送请求到SNS");
                // 发送请求到SNS
                CreatePlatformEndpointResult result = snsClient.createPlatformEndpoint(request);
                arnStorage = result.getEndpointArn();

                Log.d(TAG, "成功注册到SNS，端点ARN：" + arnStorage);

                // 在UI线程上显示结果
                runOnUiThread(() -> {
                    if (progressTextView != null) {
                        progressTextView.setText("\n成功注册到SNS");
                    }
                    Toast.makeText(AmazonSNSActivity.this, "成功注册到Amazon SNS", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "注册到SNS失败", e);
                runOnUiThread(() -> {
                    if (progressTextView != null) {
                        progressTextView.append("\n注册到SNS失败：" + e.getMessage());
                    }
                    Toast.makeText(AmazonSNSActivity.this, "注册到SNS失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}
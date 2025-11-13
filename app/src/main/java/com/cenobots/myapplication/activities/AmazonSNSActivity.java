package com.cenobots.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.cenobots.myapplication.R;
import com.google.firebase.messaging.FirebaseMessaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AmazonSNSActivity extends AppCompatActivity {
    private static final String TAG = "Amazon SNS";

    private TextView tokenTextView;
    private TextView progressTextView;

    private static AmazonSNSClient snsClient;
    static String arnStorage = null;
    static String region = "us-west-2"; // SNS region
    static String accessKeyId = ""; // AWS accessKeyId
    static String secretKey = ""; // AWS secretKey
    // 替换为您的AWS SNS平台应用程序ARN
    private static final String PLATFORM_ARN = "arn:aws:sns:us-west-2:190322700616:app/GCM/myapplication";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amazon_sns);

        // 设置标题
        setTitle("Amazon SNS");

        // 获取 tokenTextView
        tokenTextView = findViewById(R.id.tokenTextView);
        progressTextView = findViewById(R.id.progressTextView);

        init();

        // 绑定按钮
        Button btn = findViewById(R.id.button_in_push);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressTextView.setText("开始注册！");
                // 可以在这里添加其他操作，比如跳转、网络请求等
                initAmazonSNS();
            }
        });

        // unregister
        Button unregisterBtn = findViewById(R.id.button_unregister);
        unregisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressTextView.setText("反注册开始！");
                unregister();
            }
        });
    }

    private void init () {
        String jsonStr = loadJSONFromAsset(this, "aws-configs.json");
        try {
            JSONObject config = new JSONObject(jsonStr);
            accessKeyId = config.getString("access_key_id");
            secretKey = config.getString("secret_key");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initAmazonSNS() {
        getTokenWithFirebase();
    }

    public static AmazonSNSClient getSNSClient() {
        if (snsClient == null) {
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
                });
            } catch (Exception e) {
                Log.e(TAG, "注册到SNS失败", e);
                runOnUiThread(() -> {
                    if (progressTextView != null) {
                        progressTextView.append("\n注册到SNS失败：" + e.getMessage());
                    }
                });
            }
        }).start();
    }

    // 在 Amazon SNS 平台将设备注销的方法
    public void unregister () {
        new Thread(() -> {
            // 获取MyApplication中的SNS客户端实例
            AmazonSNSClient snsClient = getSNSClient();
            if (snsClient == null) {
                Log.e(TAG, "SNS客户端初始化失败");
                return;
            }
            try {
                // 删除平台端点请求
                DeleteEndpointRequest request = new DeleteEndpointRequest();
                request.setEndpointArn(arnStorage);
                snsClient.deleteEndpoint(request);
                progressTextView.setText("设备已成功反注册");
                Log.i("SNS", "设备已成功反注册");
            } catch (Exception e) {
                progressTextView.setText("反注册失败");
                Log.e("SNS", "反注册失败", e);
            }
        }).start();
    }

    public String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
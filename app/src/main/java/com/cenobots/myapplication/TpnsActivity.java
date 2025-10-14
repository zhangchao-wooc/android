package com.cenobots.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

public class TpnsActivity extends AppCompatActivity {
    private TextView tokenTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tpns);

        // 设置标题
        setTitle("TPNS");

        // 获取 tokenTextView
        tokenTextView = findViewById(R.id.tokenTextView);

        // 绑定按钮
        Button btn = findViewById(R.id.button_in_push);

        // 设置点击事件
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(TpnsActivity.this, "开始注册！", Toast.LENGTH_SHORT).show();
                // 可以在这里添加其他操作，比如跳转、网络请求等
                initTPNS();
            }
        });
    }

    public void initTPNS() {
        tokenTextView.setText("TPNS 注册中...");
        XGPushConfig.enableDebug(this,true);
        // 开启第三方 token 注册，如 FCM
        XGPushConfig.enableOtherPush(this, true);
        XGPushManager.registerPush(this, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                String token = String.valueOf(data);
                //token在设备卸载重装的时候有可能会变
                Log.d("TPush", "注册成功，设备token为：" + token);

                if (token != null && !token.isEmpty()) {
                    runOnUiThread(() -> {
                        tokenTextView.setText("注册成功，设备token为：" + token);
                    });

                } else {
                    tokenTextView.setText("注册成功，但无法获取设备token");
                }
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                Log.d("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
                // 设置文本内容
                tokenTextView.setText("注册失败，错误码：" + errCode + ",错误信息：" + msg);
            }
        });
    }
}
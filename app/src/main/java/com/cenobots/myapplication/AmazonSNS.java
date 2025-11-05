package com.cenobots.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AmazonSNS extends AppCompatActivity {

    private TextView tokenTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amazon_sns);

        // 设置标题
        setTitle("Amazon SNS");

        // 获取 tokenTextView
        tokenTextView = findViewById(R.id.tokenTextView);

        // 绑定按钮
        Button btn = findViewById(R.id.button_in_push);

        // 设置点击事件
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AmazonSNS.this, "开始注册！", Toast.LENGTH_SHORT).show();
                // 可以在这里添加其他操作，比如跳转、网络请求等
                initTPNS();
            }
        });
    }

    public void initTPNS() {}
}
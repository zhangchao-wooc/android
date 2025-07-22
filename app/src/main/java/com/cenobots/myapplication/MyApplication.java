package com.cenobots.myapplication;

import android.app.Application;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 应用初始化的逻辑
        System.out.println("应用启动");
    }
}
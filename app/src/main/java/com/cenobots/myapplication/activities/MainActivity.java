package com.cenobots.myapplication.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cenobots.myapplication.R;
import com.cenobots.myapplication.databinding.ActivityMainBinding;
import com.cenobots.myapplication.utils.ConversionUtil;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private WebView webView;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 这行很重要，确保接收到新的intent
        handleIntentData(intent);
    }

    public String bundleToJson(Bundle bundle) {
        try {
            return ConversionUtil.convertBundleToJson(bundle).toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }



    private void handleIntentData(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
//      NotificationChannel      {"data":"{\"message\":\"Sample message for Android endpoints\"}"}

            if (extras != null) {
                Log.d("handleIntent", "Extras: " + bundleToJson(extras));
                for (String key : extras.keySet()) {
                    Object value = extras.get(key);
                    // 可以在这里处理每个key对应的值
                    Log.d("handleIntent", "Key: " + key + ", Value: " + value);
                }
            } else {
                Log.d("handleIntent", "没有extras数据");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 处理启动时带的数据
        handleIntentData(getIntent());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        InitWebView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void InitWebView () {
        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                JSONObject jsonData = new JSONObject();
                JSONArray phoneNumbers = new JSONArray();
                JSONArray emailAddresses = new JSONArray();
                try {
                    phoneNumbers.put("+1 123456789");
                    phoneNumbers.put("13170027668");
                    jsonData.put("phoneNumbers", phoneNumbers);
                    emailAddresses.put("service@cenobots.com");
                    emailAddresses.put("wooc@cz-robots.com");
                    jsonData.put("emailAddresses", emailAddresses);

                    JSONObject json = new JSONObject();
                    json.put("action", "SERVICE_INFO");
                    json.put("data", jsonData);
                    String jsonString = json.toString();
                    System.out.println(jsonString);
                    webView.loadUrl("javascript:window.postMessage(JSON.stringify(" + jsonString + "), '*');");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

//        webView.loadUrl("http://192.168.111.90:3000/solutions/L50/201/zh_CN.html");

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        switch (item.getItemId()) {
            case R.id.push:
//                // 创建Intent跳转到目标Activity（比如 PushActivity）
//                Intent intent = new Intent(this, TpnsActivity.class);
//                startActivity(intent);
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.action_LoginFragment_to_TPNSFragment);
                return true;
            case R.id.amazon_sns:
                // 创建Intent跳转到目标Activity（比如 PushActivity）
                Intent amazonSnsIntent = new Intent(this, AmazonSNSActivity.class);
                startActivity(amazonSnsIntent);
                return true;
            // 其他菜单项的处理（如果有）
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
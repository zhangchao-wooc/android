package com.cenobots.myapplication.fragments;

import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cenobots.myapplication.R;
import com.cenobots.myapplication.databinding.FragmentTpnsBinding;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

public class TPNSFragment extends Fragment {
    private Context mContext;
    private FragmentTpnsBinding binding;
    private TextView tokenTextView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context; // 在此处保存
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentTpnsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化
        binding.tokenTextView.setText("");

        // 绑定按钮
        Button btn = view.findViewById(R.id.button_in_push);

        // 设置点击事件
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "开始注册！", Toast.LENGTH_SHORT).show();
                // 可以在这里添加其他操作，比如跳转、网络请求等
                initTPNS();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void initTPNS() {
        tokenTextView.setText("TPNS 注册中...");
//        XGPushConfig.enableDebug(this,true);
        // 开启第三方 token 注册，如 FCM
        XGPushConfig.enableOtherPush(mContext, true);
        XGPushManager.registerPush(mContext, new XGIOperateCallback() {
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
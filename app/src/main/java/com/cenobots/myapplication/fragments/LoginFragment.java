package com.cenobots.myapplication.fragments;

import android.util.Log;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.cenobots.myapplication.R;
import com.cenobots.myapplication.databinding.FragmentLoginBinding;

import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment"; // 定义日志标签
    private FragmentLoginBinding binding;
    private IPublicClientApplication msalClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initMsal(); // 初始化MSAL客户端
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (msalClient != null) {
                    // 获取登录token
                    msalClient.acquireToken(getActivity(),
                            new String[]{"User.Read"}, // 请求的权限
                            new AuthenticationCallback() {
                                @Override
                                public void onSuccess(IAuthenticationResult authenticationResult) {
                                    // 登录成功
                                    String accessToken = authenticationResult.getAccessToken();
                                    IAccount account = authenticationResult.getAccount();
                                    showSuccessDialog(accessToken);
                                    Log.d(TAG, account.getUsername()); // 添加调试日志
                                    Log.d(TAG, account.getId()); // 添加调试日志
                                    Log.d(TAG, accessToken); // 添加调试日志
                                }

                                @Override
                                public void onError(MsalException exception) {
                                    // 登录失败
                                    showErrorDialog(exception.getMessage());
                                }

                                @Override
                                public void onCancel() {
                                    // 用户取消
                                    showCancelDialog();
                                }
                            });
                } else {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("提示")
                            .setMessage("msalClient is null")
                            .setPositiveButton("确定", null)
                            .show();
                }
            }
        });
    }

    private void initMsal () {
        Log.d(TAG, "初始化MSAL客户端");
        PublicClientApplication.create(getActivity(),
                R.raw.auth_config, // 配置文件，放在res/raw目录下
                new IPublicClientApplication.ApplicationCreatedListener() {
                    @Override
                    public void onCreated(IPublicClientApplication application) {
                        Log.d(TAG, "msalClient 初始化成功");
                        msalClient = application;
                    }

                    @Override
                    public void onError(MsalException exception) {
                        // 处理错误
                        Log.d(TAG, "msalClient 初始化失败" + exception.getMessage());
                    }
                });
    }

    private void showSuccessDialog(String token) {
        new AlertDialog.Builder(requireContext())
                .setTitle("登录成功")
                .setMessage("Token: " + token.substring(0, 20) + "...")
                .setPositiveButton("确定", null)
                .show();
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("登录失败")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("提示")
                .setMessage("您已取消登录")
                .setPositiveButton("确定", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
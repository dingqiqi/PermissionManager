package com.lakala.appcomponent.permissionManager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;


/**
 * 权限检测申请活动
 */

@SuppressWarnings("unused")
public class PermissionsActivity extends Activity {
    private static final int PERMISSIONS_GRANTED = 0; // 权限授权
    private static final int PERMISSIONS_DENIED = 1; // 权限拒绝

    private static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    private static final String EXTRA_PERMISSIONS =
            "com.lakala.sh.cmmp.permission.extra_permission"; // 权限参数

    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案

    private boolean isRequireCheck; // 是否需要系统权限检测

    private static PermissionsManager.requestPermissionListener mListener;

    // 提示语
    private static String mHintText;

    // 启动当前权限页面的公开接口
    public static void startActivityForResult(Activity activity, PermissionsManager.requestPermissionListener listener, String[] permissions, String hintText) {
        mListener = listener;
        mHintText = hintText;
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("PermissionsActivity需要使用静态startActivityForResult方法启动!");
        }

        isRequireCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            String[] permissions = getPermissions();
            if (PermissionsManager.getInstance().checkPermissions(this, permissions)) {
                requestPermissions(permissions); // 请求权限
            } else {
                allPermissionsGranted(); // 全部权限都已获取
            }
        } else {
            isRequireCheck = true;
        }
    }

    // 返回传递的权限参数
    private String[] getPermissions() {
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    // 请求权限兼容低版本
    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    // 全部权限均已获取
    private void allPermissionsGranted() {
        sendMessage(true);

        setResult(RESULT_OK);
        finish();
    }

    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = true;
            allPermissionsGranted();
        } else {
            isRequireCheck = false;
            showMissingPermissionDialog();
        }
    }

    // 含有全部的权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private Dialog mDialog;

    // 显示缺失权限提示
    private void showMissingPermissionDialog() {
        mDialog = DialogFactory.getInstance().showAlertDialog(this, "系统提示", mHintText
                , "取消", "设置", new DialogFactory.ClickCallback() {
                    @Override
                    public void onClick(boolean isOk) {
                        if (isOk) {
                            startAppSettings();
                        } else {
                            mDialog.dismiss();

                            sendMessage(false);
                            setResult(PERMISSIONS_DENIED);
                            finish();
                        }
                    }
                });
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }

    /**
     * 权限申请回调
     *
     * @param isSuccess 是否发送申请成功消息
     */
    private void sendMessage(boolean isSuccess) {
        if (mListener != null) {
            if (isSuccess) {
                mListener.onSuccess();
            } else {
                mListener.onFail();
            }
        }

    }

}

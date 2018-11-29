package com.lakala.appcomponent.permissionManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;


/**
 * 权限工具检查类
 */

@SuppressWarnings("unused")
public class PermissionsManager {

    @SuppressLint("StaticFieldLeak")
    private static PermissionsManager mInstance;

    // 请求码
    private static final int REQUEST_CODE = 9999;

    private PermissionsManager() {
    }

    public static PermissionsManager getInstance() {
        if (mInstance == null) {
            mInstance = new PermissionsManager();
        }

        return mInstance;
    }

    // 判断权限集合
    public boolean checkPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (checkPermissions(context, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否缺少权限
     *
     * @param permission 权限
     * @return true 缺少
     */
    private boolean checkPermissions(Context context, String permission) {
        boolean ret = false;

        //系统版本大于等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //target版本大等于于6.0
            if (context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M) {
                ret = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
            } else {
                ret = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_DENIED;
            }
        }

        return ret;
    }

    /**
     * 申请权限
     *
     * @param PERMISSIONS 权限组
     * @param listener    权限申请回调
     * @param hintText    权限缺少提示语
     */
    public void requestPermissions(Activity activity, String[] PERMISSIONS, requestPermissionListener listener, String hintText) {
        if (checkPermissions(activity, PERMISSIONS)) {
            PermissionsActivity.startActivityForResult(activity, listener, PERMISSIONS, hintText);
        } else {
            if (listener != null) {
                listener.onSuccess();
            }
        }
    }


    public interface requestPermissionListener {
        void onSuccess();

        void onFail();
    }


}

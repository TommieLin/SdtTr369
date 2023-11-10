package com.sdt.android.tr369;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sdt.diagnose.Device.X_Skyworth.App.AppX;

/**
 * @Description: 监听应用安装
 * @CreateDate: 2021/10/27 18:14
 */
public class PackageReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = intent.getDataString();
        String action = intent.getAction();
        Log.d(TAG, "packageName: " + packageName + ", action: " + action);
        // 安装
        if (("android.intent.action.PACKAGE_ADDED").equals(action)) {
            AppX.updateAppList();
        }
        // 移除
        if (("android.intent.action.PACKAGE_REMOVED").equals(action)) {
            AppX.updateAppList();
        }
        if (("android.intent.action.PACKAGE_CHANGED").equals(action)) {
            AppX.updateAppList();
        }
    }
}

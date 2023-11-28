package com.sdt.android.tr369.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.common.bean.AppUpgradeResponseBean;
import com.sdt.diagnose.common.log.LogUtils;
import com.sdt.diagnose.common.net.HttpsUtils;
import com.sdt.diagnose.database.DbManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @Author Outis
 * @Date 2022/12/16 14:32
 * @Version 1.0
 */
public class ExternalAppUpgradeReceiver extends BroadcastReceiver {
    private static final String TAG = "ExternalAppUpgradeReceiver";

    // 接收来自SkyUpdate.apk的广播，接收下载/安装APP的状态
    private static final String ACTION_BOOT_DIAGNOSE_APP_DOWNLOAD
            = "com.skyworth.diagnose.Broadcast.DownloadStatus";
    private static final String ACTION_BOOT_DIAGNOSE_APP_UPGRADE
            = "com.skyworth.diagnose.Broadcast.UpgradeStatus";

    // 上报APP下载和安装状态的接口，IP和Port由Device.X_Skyworth.ManagementServer.Url决定
    private static final String URL_DOWNLOAD_RESULT_REPORT = "/appList/downloadResult";
    private static final String URL_INSTALL_RESULT_REPORT = "/appList/installResult";

    // 重试发送状态的次数
    private static final int DEFAULT_REQUEST_RETRY_COUNT = 6;
    // 重试发送状态的延迟 (ms)
    private static final int DEFAULT_REQUEST_RETRY_DELAY = 10 * 1000;

    private static Timer mTimer;
    private static TimerTask mTask;
    private static int mRetryCount = 0;
    private static final int MSG_REQUEST_RETRY = 3308;
    private static boolean isRequestSuccess = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.d(TAG, "onReceive action: " + action);
        if (ACTION_BOOT_DIAGNOSE_APP_DOWNLOAD.equals(action)) {
            LogUtils.d(TAG, "ACTION_BOOT_DIAGNOSE_APP_DOWNLOAD");
            if (checkPackageNameValidity(intent)) handleExternalAppDownloadStatus(intent);
        } else if (ACTION_BOOT_DIAGNOSE_APP_UPGRADE.equals(action)) {
            LogUtils.d(TAG, "ACTION_BOOT_DIAGNOSE_APP_UPGRADE");
            if (checkPackageNameValidity(intent)) handleExternalAppUpgradeStatus(intent);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            if (isConnected(context.getApplicationContext())) {
                if (DbManager.getDBParam("Device.X_Skyworth.UpgradeResponse.Enable").equals("1")) {
                    retryRequestUpdateStatus();
                }
            }
        }
    }

    private boolean checkPackageNameValidity(Intent input) {
        String pkgName = input.getStringExtra("packageName");
        if (pkgName != null) {
            return pkgName.equals(GlobalContext.getContext().getPackageName());
        }
        return false;
    }

    private boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void handleExternalAppDownloadStatus(Intent input) {
        // 将下载状态通知http
        AppUpgradeResponseBean requestBean = new AppUpgradeResponseBean(input);
        HashMap<String, String> hashMap = requestBean.toHashMap();
        LogUtils.d(TAG, "execute POST download request, params: " + hashMap);

        String url = DbManager.getDBParam("Device.X_Skyworth.ManagementServer.Url");
        if (!url.isEmpty()) {
            HttpsUtils.noticeResponse(url + URL_DOWNLOAD_RESULT_REPORT, hashMap);
        } else {
            LogUtils.e(TAG, "The URL of the download result report is illegal");
        }
    }

    private void handleExternalAppUpgradeStatus(Intent intent) {
        // 将安装状态通知http
        AppUpgradeResponseBean requestBean = new AppUpgradeResponseBean(intent);
        HashMap<String, String> hashMap = requestBean.toHashMap();
        LogUtils.d(TAG, "execute POST install request, params: " + hashMap);

        String url = DbManager.getDBParam("Device.X_Skyworth.ManagementServer.Url");
        if (url.isEmpty()) {
            LogUtils.e(TAG, "The URL of the install result report is illegal");
            return;
        }

        String reportUrl = url + URL_INSTALL_RESULT_REPORT;
        HttpsUtils.requestAppUpdateStatus(reportUrl, hashMap, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtils.e(TAG, "Failed to report installation status");
                DbManager.setDBParam("Device.X_Skyworth.UpgradeResponse.Enable", "1");
                requestBean.setResponseEnableDBParam();
                retryRequestUpdateStatus();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LogUtils.d(TAG, "requestAppUpdateStatus Protocol: " + response.protocol()
                        + " ,Code: " + response.code());
                if (response.code() == 200) {
                    DbManager.setDBParam("Device.X_Skyworth.UpgradeResponse.Enable", "0");
                } else {
                    DbManager.setDBParam("Device.X_Skyworth.UpgradeResponse.Enable", "1");
                    requestBean.setResponseEnableDBParam();
                    retryRequestUpdateStatus();
                }
            }
        });
    }

    private static void handleRequest() {
        final HashMap<String, String> hashMap = AppUpgradeResponseBean.getResponseEnableDBParam();
        LogUtils.d(TAG, "execute POST install request, params: " + hashMap);

        String url = DbManager.getDBParam("Device.X_Skyworth.ManagementServer.Url");
        if (url.isEmpty()) {
            LogUtils.e(TAG, "The URL of the install result report is illegal");
            return;
        }

        String reportUrl = url + URL_INSTALL_RESULT_REPORT;
        HttpsUtils.requestAppUpdateStatus(reportUrl, hashMap,
                new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        LogUtils.e(TAG, "Reporting the installation status failed again");
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        LogUtils.d(TAG, "requestAppUpdateStatus Protocol: " + response.protocol()
                                + " ,Code: " + response.code());
                        if (response.code() == 200) {
                            DbManager.setDBParam("Device.X_Skyworth.UpgradeResponse.Enable", "0");
                            isRequestSuccess = true;
                        }
                    }
                });
    }

    private static final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REQUEST_RETRY:
                    if (mRetryCount > DEFAULT_REQUEST_RETRY_COUNT || isRequestSuccess) {
                        mTask.cancel();
                    } else {
                        handleRequest();
                    }
                    mRetryCount++;
                    break;
                default:
                    break;
            }
        }
    };

    private static class RequestTimerTask extends TimerTask {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(MSG_REQUEST_RETRY);
        }
    }

    public static void retryRequestUpdateStatus() {
        mRetryCount = 0;
        isRequestSuccess = false;
        if (mTimer == null) mTimer = new Timer();
        if (mTask != null) mTask.cancel();
        mTask = new RequestTimerTask();
        mTimer.schedule(mTask, 0, DEFAULT_REQUEST_RETRY_DELAY);
    }
}


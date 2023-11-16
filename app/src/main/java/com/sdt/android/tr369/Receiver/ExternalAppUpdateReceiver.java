package com.sdt.android.tr369.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

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
public class ExternalAppUpdateReceiver extends BroadcastReceiver {
    private static final String TAG = "ExternalAppUpdateReceiver";

    // 接收来自SkyUpdate.apk的广播，接收下载/安装APP的状态
    private static final String ACTION_BOOT_DIAGNOSE_APP_DOWNLOAD
                                    = "com.skyworth.diagnose.Broadcast.DownloadStatus";
    private static final String ACTION_BOOT_DIAGNOSE_APP_UPGRADE
                                    = "com.skyworth.diagnose.Broadcast.UpgradeStatus";

//    // 回传Download状态给SkyTr069，SkyTr069用于回复Transfer Complete报文给服务器
//    private static final String ACTION_BOOT_TR069_APP_DOWNLOAD
//                                    = "com.skyworth.tr069.Broadcast.DownloadStatus";

    // 上报APP下载和安装状态的接口，IP和Port由Device.ManagementServer.URL决定
    private static final String URL_DOWNLOAD_RESULT_REPORT = "/appList/downloadResult";
    private static final String URL_INSTALL_RESULT_REPORT = "/appList/installResult";

    // 重试发送状态的次数
    private static final int DEFAULT_REQUEST_RETRY_COUNT = 6;
    // 重试发送状态的延迟 (ms)
    private static final int DEFAULT_REQUEST_RETRY_DELAY = 10 * 1000;

    private static Timer mTimer;
    private static TimerTask mTask;
    private static int mRetryCount = 0;
    private static final int MSG_REQUEST_RETRY = 0;

    private static boolean isRequestSuccess = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Get action: " + action);

        if (ACTION_BOOT_DIAGNOSE_APP_DOWNLOAD.equals(action)) {
            handleExternalAppDownloadStatus(intent);
        } else if (ACTION_BOOT_DIAGNOSE_APP_UPGRADE.equals(action)) {
            handleExternalAppUpgradeStatus(intent);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            if (isConnected(context.getApplicationContext())) {
                retryRequestUpdateStatus();
            }
        }

    }

    private boolean isConnected(Context context){
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return  networkInfo != null && networkInfo.isConnected() ;
    }

    private void handleExternalAppDownloadStatus(Intent input) {
//        int status = input.getIntExtra("status", FailCode.DOWNLOAD_FILE_FAULT);
//        String msg = input.getStringExtra("msg");
//        LogUtils.d("Get from com.skw.ota.update, status: " + status + " msg: " + msg);
//
//        // 将下载状态回传给TR069
//        Intent output = new Intent();
//        output.setAction(ACTION_BOOT_TR069_APP_DOWNLOAD);
//        output.setPackage("com.sdt.android.tr069");
//        if (status != 0) {
//            // Download Failed
//            output.putExtra("status", FailCode.DOWNLOAD_FILE_FAULT);
//        } else {
//            // Download successfully
//            output.putExtra("status", 0);
//        }
//        output.putExtra("msg", msg);
//        HwContext.getContext().sendBroadcast(output);
//
//        // 将下载状态通知http
//        AppUpdateRequestBean requestBean = new AppUpdateRequestBean(input);
//        HashMap<String, String> hashMap = requestBean.toHashMap();
//        LogUtils.d("execute POST download request, params: " + hashMap);
//
//        String[] split = DbManager.getDBParam("Device.ManagementServer.URL").split("/");
//        if (split.length >= 3) {
//            HttpsUtils.noticeResponse(split[0] + "//" + split[2] + URL_DOWNLOAD_RESULT_REPORT, hashMap);
//        } else {
//            LogUtils.e("The URL of the download result report is illegal");
//        }
    }

    private void handleExternalAppUpgradeStatus(Intent intent) {
//        AppUpdateRequestBean requestBean = new AppUpdateRequestBean(intent);
//
//        HashMap<String, String> hashMap = requestBean.toHashMap();
//        LogUtils.d("execute POST install request, params: " + hashMap);
//
//        String url;
//        String[] split = DbManager.getDBParam("Device.ManagementServer.URL").split("/");
//        if (split.length >= 3) {
//            url = split[0] + "//" + split[2] + URL_INSTALL_RESULT_REPORT;
//        } else {
//            LogUtils.e("The URL of the install result report is illegal");
//            return;
//        }
//        HttpsUtils.requestAppUpdateStatus(url, hashMap, new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                LogUtils.e("Failed to report installation status");
//                DbManager.setDBParam("Device.X_Skyworth.Update.Request.Enable", "1");
//                requestBean.setRequestEnableDBParam();
//                retryRequestUpdateStatus();
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                LogUtils.d("[requestAppUpdateStatus] Protocol: " + response.protocol()
//                                + " ,Code: " + response.code());
//                if (response.code() == 200) {
//                    DbManager.setDBParam("Device.X_Skyworth.Update.Request.Enable", "0");
//                } else {
//                    DbManager.setDBParam("Device.X_Skyworth.Update.Request.Enable", "1");
//                    requestBean.setRequestEnableDBParam();
//                    retryRequestUpdateStatus();
//                }
//            }
//        });
    }

    private static void handleRequest() {
//        final HashMap<String, String> hashMap = AppUpdateRequestBean.getRequestEnableDBParam();
//        LogUtils.d("execute POST install request, params: " + hashMap);
//
//        String url;
//        String[] split = DbManager.getDBParam("Device.ManagementServer.URL").split("/");
//        if (split.length >= 3) {
//            url = split[0] + "//" + split[2] + URL_INSTALL_RESULT_REPORT;
//        } else {
//            LogUtils.e("The URL of the install result report is illegal");
//            return;
//        }
//        HttpsUtils.requestAppUpdateStatus(url, hashMap,
//                new Callback() {
//                    @Override
//                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                        LogUtils.e("Reporting the installation status failed again");
//                    }
//
//                    @Override
//                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                        LogUtils.d("[requestAppUpdateStatus] Protocol: " + response.protocol()
//                                        + " ,Code: " + response.code());
//                        if (response.code() == 200) {
//                            DbManager.setDBParam("Device.X_Skyworth.Update.Request.Enable", "0");
//                            isRequestSuccess = true;
//                        }
//                    }
//                });
    }

//    private static Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MSG_REQUEST_RETRY:
//                    if (mRetryCount > DEFAULT_REQUEST_RETRY_COUNT || isRequestSuccess) {
//                        mTask.cancel();
//                    } else {
//                        handleRequest();
//                    }
//                    mRetryCount++;
//                    break;
//                default:
//                    break;
//            }
//        }
//    };
//
//    private static class RequestTimerTask extends TimerTask {
//        @Override
//        public void run() {
//            if (mHandler != null) {
//                mHandler.sendEmptyMessage(MSG_REQUEST_RETRY);
//            }
//        }
//    };

    public static void retryRequestUpdateStatus() {
//        String enable = DbManager.getDBParam("Device.X_Skyworth.Update.Request.Enable");
//        if (!enable.equals("1")) {
//            return;
//        }
//
//        mRetryCount = 0;
//        isRequestSuccess = false;
//        if (mTimer == null) mTimer = new Timer();
//        if (mTask != null) mTask.cancel();
//        mTask = new RequestTimerTask();
//        mTimer.schedule(mTask, 0, DEFAULT_REQUEST_RETRY_DELAY);
    }
}


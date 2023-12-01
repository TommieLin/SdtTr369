package com.sdt.diagnose;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.common.bean.SpeedTestBean;
import com.sdt.diagnose.common.log.LogUtils;
import com.sdt.diagnose.common.net.HttpsUtils;
import com.skyworthdigital.speedtest.ui.SpeedTestService;


public class DiagnoseServiceManager {
    private static final String TAG = "DiagnoseServiceManager";
    private static DiagnoseServiceManager instance = null;
    public SpeedTestService service;
    private final Context mContext;
    Handler handler;
    HandlerThread handlerThread;

    protected DiagnoseServiceManager() {
        mContext = GlobalContext.getContext();
    }

    public static DiagnoseServiceManager getInstance() {
        synchronized (DiagnoseServiceManager.class) {
            if (instance == null) {
                instance = new DiagnoseServiceManager();
            }
            return instance;
        }
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            handlerThread = new HandlerThread("SpeedTestServiceConnected");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            SpeedTestService.MyBinder binder = (SpeedTestService.MyBinder) iBinder;
            service = binder.getService();
            service.setReadyCallback(new SpeedTestService.ReadyCallback() {
                @Override
                public void isReady() {
                    startNetSpeedTest();
                }
            });
            service.setCallback(new SpeedTestService.Callback() {
                @Override
                public void setResult(String mDownloadSpeed, String mUploadSpeed) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String url = SpeedTestBean.getInstance().getUrl();
                            LogUtils.d(TAG, "setResult url: " + url);
                            if (!url.startsWith("https")) {
                                LogUtils.e(TAG, "The URL set for network speed test is invalid. Please use the HTTPS protocol.");
                                return;
                            }
                            if (Double.parseDouble(mDownloadSpeed) <= 0
                                    || Double.parseDouble(mUploadSpeed) <= 0) {
                                HttpsUtils.uploadSpeedData(
                                        url,
                                        mDownloadSpeed,
                                        "failure",
                                        SpeedTestBean.getInstance().getTransactionId(),
                                        "true");
                            } else {
                                HttpsUtils.uploadSpeedData(
                                        url,
                                        mUploadSpeed,
                                        "upload",
                                        SpeedTestBean.getInstance().getTransactionId(),
                                        "true");
                            }
                            SpeedTestBean.getInstance().setUrl("");
                            SpeedTestBean.getInstance().setTransactionId("");
                            SpeedTestBean.getInstance().setEnable("0");
                            LogUtils.d(TAG, "unbindService");
                            mContext.unbindService(serviceConnection);
                        }
                    });
                }

                @Override
                public void setDownloadSpeed(String mDownloadSpeed) {
                    if (SpeedTestBean.getInstance().getEnable().equals("1")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                String url = SpeedTestBean.getInstance().getUrl();
                                LogUtils.d(TAG, "setDownloadSpeed url: " + url);
                                if (!url.startsWith("https")) {
                                    LogUtils.e(TAG, "The URL set for network speed test is invalid. Please use the HTTPS protocol.");
                                    return;
                                }
                                HttpsUtils.uploadSpeedData(
                                        url,
                                        mDownloadSpeed,
                                        "download",
                                        SpeedTestBean.getInstance().getTransactionId(),
                                        "false");
                            }
                        });
                    }
                }

                @Override
                public void setUploadSpeed(String mUploadSpeed) {
                    if (SpeedTestBean.getInstance().getEnable().equals("1")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                String url = SpeedTestBean.getInstance().getUrl();
                                LogUtils.d(TAG, "setUploadSpeed url: " + url);
                                if (!url.startsWith("https")) {
                                    LogUtils.e(TAG, "The URL set for network speed test is invalid. Please use the HTTPS protocol.");
                                    return;
                                }
                                HttpsUtils.uploadSpeedData(
                                        url,
                                        mUploadSpeed,
                                        "upload",
                                        SpeedTestBean.getInstance().getTransactionId(),
                                        "false");
                            }
                        });
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public void bindSpeedTestService() {
        Intent intent = new Intent(mContext, SpeedTestService.class);
        mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void startNetSpeedTest() {
        service.startNetSpeedTest();
    }
}

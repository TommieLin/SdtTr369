package com.sdt.diagnose;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.sdt.diagnose.common.GlobalContext;
import com.sdt.diagnose.common.bean.SpeedTestBean;
import com.sdt.diagnose.common.net.HttpsUtils;
import com.skyworthdigital.speedtest.ui.SpeedTestService;


/**
 * @Description: java类作用描述
 * @CreateDate: 2021/8/18 16:51
 */
public class DiagnoseServiceManager {
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
                            if (Double.parseDouble(mDownloadSpeed) <= 0
                                    || Double.parseDouble(mUploadSpeed) <= 0) {
                                HttpsUtils.uploadSpeedData(
                                        SpeedTestBean.getInstance().getUrl(),
                                        mDownloadSpeed,
                                        "failure",
                                        SpeedTestBean.getInstance().getTransactionId(),
                                        "true");
                            } else {
                                HttpsUtils.uploadSpeedData(
                                        SpeedTestBean.getInstance().getUrl(),
                                        mUploadSpeed,
                                        "upload",
                                        SpeedTestBean.getInstance().getTransactionId(),
                                        "true");
                            }
                            SpeedTestBean.getInstance().setUrl("");
                            SpeedTestBean.getInstance().setTransactionId("");
                            SpeedTestBean.getInstance().setEnable("0");
                            Log.e("TAG", "unbindService");
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
                                HttpsUtils.uploadSpeedData(
                                        SpeedTestBean.getInstance().getUrl(),
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
                                HttpsUtils.uploadSpeedData(
                                        SpeedTestBean.getInstance().getUrl(),
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

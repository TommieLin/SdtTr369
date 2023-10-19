package com.sdt.android.tr369;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sdt.android.tr369.Utils.FileUtil;
import com.sdt.opentr369.OpenTR369Native;

import java.io.File;

public class SdtTr369Service extends Service {
    private static final String TAG = "SdtTr369Service";
    private static final String CHANNEL_ID = "SdtTr369ServiceChannelId";
    private static final String CHANNEL_NAME = "SdtTr369ServiceChannelName";
    private SdtTr369Receiver mSdtTr369Receiver = null;
    private HandlerThread mHandlerThread = null;
    private Handler mHandler = null;
    public static final int MSG_START_TR369 = 3300;
    public static final int MSG_STOP_TR369 = 3301;

    public void handleTr369Message(@NonNull Message msg) {
        switch (msg.what) {
            case MSG_START_TR369:
                Log.e(TAG, " ####### Outis ### handleTr369Message MSG_START_TR369");
                startTR369();
                break;
            case MSG_STOP_TR369:
                Log.e(TAG, " ####### Outis ### handleTr369Message MSG_STOP_TR369");
                break;
            default:
                break;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.e(TAG, " ####### Outis ### onBind start");
        return null;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, " ####### Outis ### onCreate start");
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID).build();
        startForeground(1, notification);
        Log.e(TAG, " ####### Outis ### onCreate return");
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, " ####### Outis ### onStartCommand start");
        initTr369Service();
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    private void initTr369Service() {
        Log.e(TAG, " ####### Outis ### initTr369Service start");
        mHandlerThread = new HandlerThread("tr369_worker");
        // 先启动，再初始化handler
        mHandlerThread.start();
        if (mHandler == null) {
            mHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    handleTr369Message(msg);
                }
            };
        }
        registerSdtTr369Receiver();
    }

    private void registerSdtTr369Receiver() {
        Log.e(TAG, " ####### Outis ### registerSdtTr369Receiver start");
        mSdtTr369Receiver = new SdtTr369Receiver(mHandler);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mSdtTr369Receiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, " ####### Outis ### onDestroy start");
        if (mSdtTr369Receiver != null) {
            unregisterReceiver(mSdtTr369Receiver);
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_STOP_TR369);
            mHandler = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        super.onDestroy();
    }

    private void startTR369() {
        Log.e(TAG, " ############ Outis ### startTR369 start~~");
        String dbFilePath = getApplicationContext().getDataDir().getPath() + "/sdt_tms_usp.db"; // "/databases/sk_usp.db";
        Log.e(TAG, " ############ Outis ### startTR369 dbFilePath: " + dbFilePath);

        FileUtil.copyTr369AssetsToFile(getApplicationContext());
        String defaultFilePath = getApplicationContext().getDataDir().getPath() + "/" + FileUtil.PLATFORM_TMS_TR369_MODEL_DEFAULT;
        Log.e(TAG, " ############ Outis ### startTR369 defaultFilePath: " + defaultFilePath);

        int ret = OpenTR369Native.SetInitFilePath(dbFilePath, defaultFilePath);
        Log.e(TAG, " ############ Outis ### startTR369 SetInitFilePath ret: " + ret);

        String modelFile = getApplicationContext().getDataDir().getPath() + "/" + FileUtil.PLATFORM_TMS_TR369_MODEL_XML;
        ret = OpenTR369Native.OpenTR369Init(modelFile);
        Log.e(TAG, " ############ Outis ### startTR369 ret: " + ret);

        String test_str = OpenTR369Native.stringFromJNI();
        Log.e(TAG, " ############ Outis ### startTR369 test_str: " + test_str);
    }

}

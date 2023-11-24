package com.sdt.android.tr369;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sdt.android.tr369.Utils.FileUtil;
import com.sdt.opentr369.OpenTR369Native;

public class SdtTr369Receiver extends BroadcastReceiver {
    private final static String TAG = "SdtTr369Receiver";
    private final Context mContext;
    private final HandlerThread mHandlerThread;
    private Handler mHandler = null;
    public static final int MSG_START_TR369_PROTOCOL = 3302;

    public void handleProtocolMessage(@NonNull Message msg) {
        if (msg.what == MSG_START_TR369_PROTOCOL) {
            mHandler.removeMessages(MSG_START_TR369_PROTOCOL);
            Log.e(TAG, " ####### Outis ### handleProtocolMessage MSG_START_TR369_PROTOCOL");
            startTr369Protocol();
        }
    }

    public SdtTr369Receiver(Context context) {
        Log.e(TAG, " ############ Outis ### SdtTr369Receiver create");
        mContext = context;
        mHandlerThread = new HandlerThread("tr369_protocol");
        // 先启动，再初始化handler
        mHandlerThread.start();
        if (mHandler == null) {
            mHandler = new Handler(mHandlerThread.getLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    handleProtocolMessage(msg);
                }
            };
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, " ############ Outis ### action = " + action);
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            // 获取承有网络连接信恿
            if (isConnected(context.getApplicationContext())) {
                Log.d(TAG, " ############ Outis ### isConnected true");
                mHandler.sendEmptyMessage(MSG_START_TR369_PROTOCOL);
            } else {
                Log.e(TAG, " ############ Outis ### isConnected false");
            }
        }
    }

    private boolean isConnected(Context context) {
        Log.e(TAG, " ############ Outis ### isConnected start");
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void startTr369Protocol() {
        FileUtil.copyTr369AssetsToFile(mContext);
        String defaultFilePath = mContext.getDataDir().getPath() + "/" + FileUtil.PLATFORM_TMS_TR369_MODEL_DEFAULT;
        Log.e(TAG, " ############ Outis ### startTr369Protocol defaultFilePath: " + defaultFilePath);

        int ret = OpenTR369Native.SetInitFilePath(defaultFilePath);
        Log.e(TAG, " ############ Outis ### startTr369Protocol SetInitFilePath ret: " + ret);

        String modelFile = mContext.getDataDir().getPath() + "/" + FileUtil.PLATFORM_TMS_TR369_MODEL_XML;
        ret = OpenTR369Native.OpenTR369Init(modelFile);
        Log.e(TAG, " ############ Outis ### startTr369Protocol ret: " + ret);

        String test_str = OpenTR369Native.stringFromJNI();
        Log.e(TAG, " ############ Outis ### startTr369Protocol test_str: " + test_str);
    }

}

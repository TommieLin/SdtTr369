package com.sdt.android.tr369;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

public class SdtTr369Receiver extends BroadcastReceiver {
    private final static String TAG = "SdtTr369Receiver";
    private Handler mHandler = null;

    public SdtTr369Receiver() {
        Log.d(TAG, " ############ Outis ### network changed create SdtTr369Receiver");
    }

    public SdtTr369Receiver(Handler handler) {
        Log.e(TAG, " ############ Outis ### SdtTr369Receiver create");
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, " ############ Outis ### action = " + action);
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            // 获取承有网络连接信恿
            if (isConnected(context.getApplicationContext())) {
                if (mHandler != null) {
                    Log.e(TAG, " ############ Outis ### isConnected true ");
                    mHandler.sendEmptyMessage(SdtTr369Service.MSG_START_TR369);
                }
            } else {
                Log.e(TAG, " ############ Outis ### isConnected false ");
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

}

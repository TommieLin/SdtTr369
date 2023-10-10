package com.sdt.android.tr369;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    private final static String TAG = "BootCompletedReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, " ############ Outis ### action = " + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // 启动SdtTr369Service
            context.startForegroundService(new Intent(context, SdtTr369Service.class));
        }
    }
}
